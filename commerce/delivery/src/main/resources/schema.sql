CREATE SCHEMA IF NOT EXISTS delivery;

CREATE TABLE IF NOT EXISTS delivery.deliveries (
    id UUID PRIMARY KEY,
    from_country VARCHAR(100),
    from_city VARCHAR(100),
    from_street VARCHAR(100),
    from_house VARCHAR(20),
    from_flat VARCHAR(20),
    to_country VARCHAR(100),
    to_city VARCHAR(100),
    to_street VARCHAR(100),
    to_house VARCHAR(20),
    to_flat VARCHAR(20),
    order_id UUID NOT NULL,
    delivery_state VARCHAR(20) NOT NULL,
    created TIMESTAMP NOT NULL
);
