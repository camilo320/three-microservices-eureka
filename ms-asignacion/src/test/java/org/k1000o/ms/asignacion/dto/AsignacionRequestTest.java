package org.k1000o.ms.asignacion.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AsignacionRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequestHasNoViolations() {
        Set<ConstraintViolation<AsignacionRequest>> violations = validator.validate(
                new AsignacionRequest(UUID.randomUUID(), UUID.randomUUID()));

        assertThat(violations).isEmpty();
    }

    @Test
    void nullOrdenIdIsRejected() {
        Set<ConstraintViolation<AsignacionRequest>> violations = validator.validate(
                new AsignacionRequest(null, UUID.randomUUID()));

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("ordenId");
    }

    @Test
    void nullDriverIdIsRejected() {
        Set<ConstraintViolation<AsignacionRequest>> violations = validator.validate(
                new AsignacionRequest(UUID.randomUUID(), null));

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("driverId");
    }

    @Test
    void bothIdsNullProducesTwoViolations() {
        Set<ConstraintViolation<AsignacionRequest>> violations = validator.validate(
                new AsignacionRequest(null, null));

        assertThat(violations).hasSize(2);
    }
}
