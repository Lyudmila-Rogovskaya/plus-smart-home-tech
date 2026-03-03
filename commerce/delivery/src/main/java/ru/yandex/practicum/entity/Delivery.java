package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.dto.DeliveryState;
import ru.yandex.practicum.embedded.AddressEmbeddable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deliveries", schema = "delivery")
@Data
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "from_country")),
            @AttributeOverride(name = "city", column = @Column(name = "from_city")),
            @AttributeOverride(name = "street", column = @Column(name = "from_street")),
            @AttributeOverride(name = "house", column = @Column(name = "from_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "from_flat"))
    })
    private AddressEmbeddable fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country")),
            @AttributeOverride(name = "city", column = @Column(name = "to_city")),
            @AttributeOverride(name = "street", column = @Column(name = "to_street")),
            @AttributeOverride(name = "house", column = @Column(name = "to_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private AddressEmbeddable toAddress;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false)
    private DeliveryState deliveryState;

    @Column(name = "created", nullable = false)
    private Instant created;

    @PrePersist
    protected void onCreate() {
        created = Instant.now();
    }

}
