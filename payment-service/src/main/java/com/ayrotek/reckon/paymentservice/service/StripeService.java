package com.ayrotek.reckon.paymentservice.service;

import com.stripe.Stripe;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.secret-key}")
    private String secretKey;

    public String getPublishableKey() {
        return publishableKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void initializeStripe() {
        Stripe.apiKey = secretKey;
    }
}
