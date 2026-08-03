package com.ayrotek.reckon.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {

    private String secretKey;
    private String publishableKey;
    private String webhookSecret;
    private int connectTimeout;
    private int readTimeout;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        if (connectTimeout > 0) {
            Stripe.setConnectTimeout(connectTimeout);
        }
        if (readTimeout > 0) {
            Stripe.setReadTimeout(readTimeout);
        }
    }
}
