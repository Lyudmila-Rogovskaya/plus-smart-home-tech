CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE IF NOT EXISTS payment.payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    total_payment DOUBLE PRECISION,
    delivery_total DOUBLE PRECISION,
    fee_total DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL,
    created TIMESTAMP NOT NULL
);
