CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE IF NOT EXISTS warehouse.warehouse_products (
    product_id UUID PRIMARY KEY,
    fragile BOOLEAN NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    quantity BIGINT NOT NULL,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    depth DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS warehouse.order_bookings (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    delivery_id UUID,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouse.booking_items (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES warehouse.order_bookings(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL
);
