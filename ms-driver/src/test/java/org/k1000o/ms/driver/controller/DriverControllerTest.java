package org.k1000o.ms.driver.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.k1000o.ms.driver.dto.DriverRequest;
import org.k1000o.ms.driver.dto.DriverResponse;
import org.k1000o.ms.driver.exception.GlobalExceptionHandler;
import org.k1000o.ms.driver.exception.ResourceNotFoundException;
import org.k1000o.ms.driver.service.DriverService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DriverControllerTest {

    private DriverService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(DriverService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DriverController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getByIdReturnsDriver() throws Exception {
        UUID id = UUID.randomUUID();
        DriverResponse response = DriverResponse.builder()
                .id(id)
                .name("John Doe")
                .licenseNumber("AB-123")
                .active(true)
                .createdAt(Instant.parse("2026-01-10T08:00:00Z"))
                .build();
        when(service.getById(id)).thenReturn(response);

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.licenseNumber").value("AB-123"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getByIdReturnsNotFoundWhenDriverDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new ResourceNotFoundException("No hay Driver con id : " + id));

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.path").value("/" + id))
                .andExpect(jsonPath("$.message").value("No hay Driver con id : " + id));
    }

    @Test
    void saveReturnsCreatedStatusAndId() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.save(any(DriverRequest.class))).thenReturn(id);

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"licenseNumber\":\"AB-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(id.toString()));

        verify(service).save(new DriverRequest("John Doe", "AB-123"));
    }

    @Test
    void saveReturnsBadRequestWhenLicenseNumberHasInvalidCharacters() throws Exception {
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"licenseNumber\":\"AB 123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors.licenseNumber").exists());
    }

    @Test
    void updateReturnsAccepted() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jane Doe\",\"licenseNumber\":\"CD-456\"}"))
                .andExpect(status().isAccepted());

        verify(service).update(eq(id), eq(new DriverRequest("Jane Doe", "CD-456")));
    }

    @Test
    void searchReturnsFilteredDrivers() throws Exception {
        DriverResponse response = DriverResponse.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .licenseNumber("AB-123")
                .active(true)
                .build();
        when(service.search("John", "AB-123", true, null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/search")
                        .param("name", "John")
                        .param("licenseNumber", "AB-123")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].active").value(true));
    }
}
