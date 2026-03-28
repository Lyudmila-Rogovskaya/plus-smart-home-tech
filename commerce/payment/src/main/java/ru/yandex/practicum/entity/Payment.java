package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.dto.PaymentState;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "total_payment")
    private Double totalPayment;

    @Column(name = "delivery_total")
    private Double deliveryTotal;

    @Column(name = "fee_total")
    private Double feeTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentState status;

    @Column(name = "created", nullable = false)
    private Instant created;

    @PrePersist
    protected void onCreate() {
        created = Instant.now();
    }

}
