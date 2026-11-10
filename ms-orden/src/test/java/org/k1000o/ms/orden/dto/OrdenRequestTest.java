package org.k1000o.ms.orden.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrdenRequestTest {

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
        Set<ConstraintViolation<OrdenRequest>> violations = validator.validate(
                new OrdenRequest("Bogota", "Medellin"));

        assertThat(violations).isEmpty();
    }

    @Test
    void blankOriginIsRejected() {
        Set<ConstraintViolation<OrdenRequest>> violations = validator.validate(
                new OrdenRequest("", "Medellin"));

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("origin"));
    }

    @Test
    void blankDestinationIsRejected() {
        Set<ConstraintViolation<OrdenRequest>> violations = validator.validate(
                new OrdenRequest("Bogota", " "));

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("destination"));
    }

    @Test
    void tooShortValuesAreRejected() {
        Set<ConstraintViolation<OrdenRequest>> violations = validator.validate(
                new OrdenRequest("A", "B"));

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("origin", "destination");
    }

    @Test
    void nullValuesAreRejected() {
        Set<ConstraintViolation<OrdenRequest>> violations = validator.validate(
                new OrdenRequest(null, null));

        assertThat(violations).hasSize(2);
    }
}
