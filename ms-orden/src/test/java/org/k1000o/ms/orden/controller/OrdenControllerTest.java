package org.k1000o.ms.orden.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.k1000o.ms.orden.dto.OrdenRequest;
import org.k1000o.ms.orden.dto.OrdenResponse;
import org.k1000o.ms.orden.entity.Status;
import org.k1000o.ms.orden.exception.GlobalExceptionHandler;
import org.k1000o.ms.orden.exception.ResourceNotFoundException;
import org.k1000o.ms.orden.service.OrdenService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
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

class OrdenControllerTest {

    private OrdenService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(OrdenService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrdenController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getByIdReturnsOrder() throws Exception {
        UUID id = UUID.randomUUID();
        OrdenResponse response = OrdenResponse.builder()
                .id(id)
                .origin("Bogota")
                .destination("Cali")
                .status(Status.CREATED)
                .createdAt(LocalDateTime.of(2026, 1, 10, 8, 0))
                .build();
        when(service.getById(id)).thenReturn(response);

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.origin").value("Bogota"))
                .andExpect(jsonPath("$.destination").value("Cali"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getByIdReturnsNotFoundWhenOrderDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new ResourceNotFoundException("No hay orden con id : " + id));

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.path").value("/" + id))
                .andExpect(jsonPath("$.message").value("No hay orden con id : " + id));
    }

    @Test
    void saveReturnsCreatedStatusAndId() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.save(any(OrdenRequest.class))).thenReturn(id);

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Bogota\",\"destination\":\"Cali\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(id.toString()));

        verify(service).save(new OrdenRequest("Bogota", "Cali"));
    }

    @Test
    void saveReturnsBadRequestWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"\",\"destination\":\"A\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors.origin").exists())
                .andExpect(jsonPath("$.errors.destination").exists());
    }

    @Test
    void updateReturnsAccepted() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Cartagena\",\"destination\":\"Santa Marta\"}"))
                .andExpect(status().isAccepted());

        verify(service).update(eq(id), eq(new OrdenRequest("Cartagena", "Santa Marta")));
    }

    @Test
    void searchReturnsFilteredOrders() throws Exception {
        OrdenResponse response = OrdenResponse.builder()
                .id(UUID.randomUUID())
                .origin("Bogota")
                .destination("Cali")
                .status(Status.CREATED)
                .build();
        when(service.search(Status.CREATED, null, null, "bog", null)).thenReturn(List.of(response));

        mockMvc.perform(get("/search")
                        .param("status", "CREATED")
                        .param("origin", "bog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origin").value("Bogota"))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }
}
