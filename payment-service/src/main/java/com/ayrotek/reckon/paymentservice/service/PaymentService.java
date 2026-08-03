package com.ayrotek.reckon.paymentservice.service;

import com.ayrotek.reckon.paymentservice.config.StripeConfig;
import com.ayrotek.reckon.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.ayrotek.reckon.paymentservice.dto.response.PaymentIntentResponse;
import com.ayrotek.reckon.paymentservice.dto.response.PaymentResponse;
import com.ayrotek.reckon.paymentservice.entity.Payment;
import com.ayrotek.reckon.paymentservice.entity.PaymentStatus;
import com.ayrotek.reckon.paymentservice.exception.InvalidWebhookSignatureException;
import com.ayrotek.reckon.paymentservice.exception.PaymentNotFoundException;
import com.ayrotek.reckon.paymentservice.exception.StripeIntegrationException;
import com.ayrotek.reckon.paymentservice.repository.PaymentRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of(
            "BIF", "CLP", "DJF", "GNF", "JPY", "KMF", "KRW", "MGA",
            "PYG", "RWF", "UGX", "VND", "VUV", "XAF", "XOF", "XPF"
    );

    private final PaymentRepository paymentRepository;
    private final StripeConfig stripeConfig;

    @Transactional
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        String currency = request.getCurrency().toUpperCase();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(toMinorUnits(request.getAmount(), currency))
                .setCurrency(currency.toLowerCase())
                .putMetadata("orderReference", request.getOrderReference())
                .putMetadata("userId", request.getUserId())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build())
                .build();

        PaymentIntent intent;
        try {
            intent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new StripeIntegrationException("Failed to create Stripe PaymentIntent", e);
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .orderReference(request.getOrderReference())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId(intent.getId())
                .build());

        return PaymentIntentResponse.builder()
                .paymentId(payment.getId())
                .clientSecret(intent.getClientSecret())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    @Transactional
    public void handleStripeEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new InvalidWebhookSignatureException("Invalid Stripe webhook signature");
        }

        log.info("Received Stripe event type={}", event.getType());

        Object dataObject;
        try {
            dataObject = event.getDataObjectDeserializer().deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            log.warn("Could not deserialize payload for Stripe event {}", event.getType(), e);
            return;
        }
        if (!(dataObject instanceof PaymentIntent paymentIntent)) {
            log.info("Ignoring Stripe event {} with no PaymentIntent payload", event.getType());
            return;
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> applyStatus(paymentIntent.getId(), PaymentStatus.SUCCEEDED, null);
            case "payment_intent.canceled" -> applyStatus(paymentIntent.getId(), PaymentStatus.CANCELED, null);
            case "payment_intent.payment_failed" -> {
                String reason = paymentIntent.getLastPaymentError() != null
                        ? paymentIntent.getLastPaymentError().getMessage()
                        : "Payment failed";
                applyStatus(paymentIntent.getId(), PaymentStatus.FAILED, reason);
            }
            case "payment_intent.processing", "payment_intent.requires_action" ->
                    applyStatus(paymentIntent.getId(), PaymentStatus.REQUIRES_ACTION, null);
            default -> log.info("Ignoring unhandled Stripe event type {}", event.getType());
        }
    }

    private void applyStatus(String stripePaymentIntentId, PaymentStatus newStatus, String failureReason) {
        paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId).ifPresentOrElse(payment -> {
            if (isTerminal(payment.getStatus())) {
                log.debug("Payment {} already in terminal state {}, ignoring event", payment.getId(), payment.getStatus());
                return;
            }
            payment.setStatus(newStatus);
            payment.setFailureReason(failureReason);
            log.info("Payment {} status updated to {} from Stripe webhook", payment.getId(), newStatus);
            paymentRepository.save(payment);
        }, () -> log.warn("Received Stripe event for unknown PaymentIntent {}", stripePaymentIntentId));
    }

    private boolean isTerminal(PaymentStatus status) {
        return status == PaymentStatus.SUCCEEDED || status == PaymentStatus.FAILED || status == PaymentStatus.CANCELED;
    }

    private long toMinorUnits(BigDecimal amount, String currency) {
        if (ZERO_DECIMAL_CURRENCIES.contains(currency)) {
            return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderReference(payment.getOrderReference())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
