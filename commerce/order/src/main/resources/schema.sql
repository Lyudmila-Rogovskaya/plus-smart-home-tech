CREATE SCHEMA IF NOT EXISTS "order";

CREATE TABLE IF NOT EXISTS "order".orders (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    shopping_cart_id UUID,
    payment_id UUID,
    delivery_id UUID,
    state VARCHAR(50) NOT NULL,
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,
    total_price DOUBLE PRECISION,
    delivery_price DOUBLE PRECISION,
    product_price DOUBLE PRECISION,
    country VARCHAR(100),
    city VARCHAR(100),
    street VARCHAR(100),
    house VARCHAR(20),
    flat VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "order".order_products (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES "order".orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL
);
