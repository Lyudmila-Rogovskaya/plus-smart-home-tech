package ru.yandex.practicum.config;

public enum TopicType {
    SENSORS_EVENTS("sensors-events"),
    HUBS_EVENTS("hubs-events");

    private final String key;

    TopicType(String key) {
        this.key = key;
    }

    public static TopicType from(String key) {
        for (TopicType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown topic key: " + key);
    }

    public String getKey() {
        return key;
    }

}
