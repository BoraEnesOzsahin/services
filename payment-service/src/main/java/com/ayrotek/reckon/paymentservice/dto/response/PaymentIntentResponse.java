package com.ayrotek.reckon.paymentservice.dto.response;

import com.ayrotek.reckon.paymentservice.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentIntentResponse {
    private UUID paymentId;
    private String clientSecret;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
}
