CREATE TABLE payments (
    id                        UUID PRIMARY KEY,
    order_reference           VARCHAR(100)   NOT NULL,
    user_id                   VARCHAR(100)   NOT NULL,
    amount                    NUMERIC(19, 2) NOT NULL,
    currency                  VARCHAR(3)     NOT NULL,
    status                    VARCHAR(30)    NOT NULL,
    stripe_payment_intent_id  VARCHAR(255)   NOT NULL UNIQUE,
    failure_reason            VARCHAR(500),
    created_at                TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_user_id ON payments (user_id);
CREATE INDEX idx_payments_stripe_payment_intent_id ON payments (stripe_payment_intent_id);
