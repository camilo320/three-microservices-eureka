package org.k1000o.ms.driver.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DriverRequestTest {

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
        Set<ConstraintViolation<DriverRequest>> violations = validator.validate(
                new DriverRequest("John Doe", "AB-123"));

        assertThat(violations).isEmpty();
    }

    @Test
    void blankNameIsRejected() {
        Set<ConstraintViolation<DriverRequest>> violations = validator.validate(
                new DriverRequest(" ", "AB-123"));

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void licenseNumberWithInvalidCharactersIsRejected() {
        Set<ConstraintViolation<DriverRequest>> violations = validator.validate(
                new DriverRequest("John Doe", "AB 123"));

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("licenseNumber")
                        && v.getMessage().contains("solamente puede contener letras"));
    }

    @Test
    void tooShortLicenseNumberIsRejected() {
        Set<ConstraintViolation<DriverRequest>> violations = validator.validate(
                new DriverRequest("John Doe", "AB"));

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("licenseNumber"));
    }

    @Test
    void nullValuesAreRejected() {
        Set<ConstraintViolation<DriverRequest>> violations = validator.validate(
                new DriverRequest(null, null));

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name", "licenseNumber");
    }
}
