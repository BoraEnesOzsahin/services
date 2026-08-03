package com.ayrotek.reckon.paymentservice.controller;

import com.ayrotek.reckon.paymentservice.dto.request.CreatePaymentIntentRequest;
import com.ayrotek.reckon.paymentservice.dto.response.PaymentIntentResponse;
import com.ayrotek.reckon.paymentservice.dto.response.PaymentResponse;
import com.ayrotek.reckon.paymentservice.service.PaymentService;
import com.ayrotek.reckon.paymentservice.service.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeService stripeService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getStripeConfig() {
        return ResponseEntity.ok(Map.of(
            "publishableKey", stripeService.getPublishableKey()
        ));
    }

    @com.ayrotek.paymentservice.annotation.AuditLog(action = "create_payment_intent", resource = "StripePayment")
    @PostMapping("/intents")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPaymentIntent(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
