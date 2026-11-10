package org.k1000o.ms.driver.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DriverTest {

    @Test
    void prePersistAssignsDefaultsWhenValuesAreNull() {
        Driver driver = Driver.builder()
                .name("John Doe")
                .licenseNumber("AB-123")
                .build();
        Instant before = Instant.now();

        driver.prePersist();

        assertThat(driver.getActive()).isTrue();
        assertThat(driver.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(driver.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void prePersistKeepsValuesWhenAlreadySet() {
        Instant createdAt = Instant.parse("2026-01-10T08:30:00Z");
        Instant updatedAt = Instant.parse("2026-01-11T09:00:00Z");
        Driver driver = Driver.builder()
                .name("John Doe")
                .licenseNumber("AB-123")
                .active(false)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        driver.prePersist();

        assertThat(driver.getActive()).isFalse();
        assertThat(driver.getCreatedAt()).isEqualTo(createdAt);
        assertThat(driver.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void preUpdateRefreshesUpdatedAt() {
        Instant oldUpdatedAt = Instant.parse("2026-01-10T08:30:00Z");
        Driver driver = Driver.builder()
                .name("John Doe")
                .licenseNumber("AB-123")
                .updatedAt(oldUpdatedAt)
                .build();
        Instant before = Instant.now();

        driver.preUpdate();

        assertThat(driver.getUpdatedAt()).isAfterOrEqualTo(before);
    }
}
