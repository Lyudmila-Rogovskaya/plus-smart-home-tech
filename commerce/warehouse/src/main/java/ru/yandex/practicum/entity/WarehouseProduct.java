package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Data
public class WarehouseProduct {
    @Id
    private UUID productId;

    @Column(nullable = false)
    private Boolean fragile;

    @Embedded
    private Dimension dimension;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Long quantity;

}
