package ru.yandex.practicum.embedded;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class AddressEmbeddable {

    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;

}
