package org.k1000o.ms.asignacion.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.k1000o.ms.asignacion.dto.AsignacionRequest;
import org.k1000o.ms.asignacion.dto.AsignacionResponse;
import org.k1000o.ms.asignacion.dto.DriverResponse;
import org.k1000o.ms.asignacion.dto.OrdenResponse;
import org.k1000o.ms.asignacion.dto.Status;
import org.k1000o.ms.asignacion.exception.BadRequestException;
import org.k1000o.ms.asignacion.exception.GlobalExceptionHandler;
import org.k1000o.ms.asignacion.exception.ResourceNotFoundException;
import org.k1000o.ms.asignacion.service.AsignacionService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AsignacionControllerTest {

    private AsignacionService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AsignacionService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AsignacionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getByIdReturnsAssignmentWithOrderAndDriver() throws Exception {
        UUID id = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        AsignacionResponse response = AsignacionResponse.builder()
                .id(id)
                .orden(OrdenResponse.builder().id(ordenId).origin("Bogota").destination("Cali").status(Status.CREATED).build())
                .driver(DriverResponse.builder().id(driverId).name("John Doe").licenseNumber("AB-123").active(true).build())
                .pdfFileName("document.pdf")
                .imageFileName("image.png")
                .build();
        when(service.getById(id)).thenReturn(response);

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orden.id").value(ordenId.toString()))
                .andExpect(jsonPath("$.orden.status").value("CREATED"))
                .andExpect(jsonPath("$.driver.id").value(driverId.toString()))
                .andExpect(jsonPath("$.pdfFileName").value("document.pdf"))
                .andExpect(jsonPath("$.imageFileName").value("image.png"));
    }

    @Test
    void getByIdReturnsNotFoundWhenAssignmentDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id))
                .thenThrow(new ResourceNotFoundException("Asignacion no encontrado con id : " + id));

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Asignacion no encontrado con id : " + id));
    }

    @Test
    void asignarReturnsCreatedStatusAndId() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();
        when(service.asignar(any(AsignacionRequest.class))).thenReturn(asignacionId);

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ordenId\":\"" + ordenId + "\",\"driverId\":\"" + driverId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(asignacionId.toString()));

        verify(service).asignar(new AsignacionRequest(ordenId, driverId));
    }

    @Test
    void asignarReturnsBadRequestWhenIdsAreMissing() throws Exception {
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ordenId\":null,\"driverId\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors.ordenId").exists())
                .andExpect(jsonPath("$.errors.driverId").exists());
    }

    @Test
    void asignarReturnsBadRequestWhenServiceRejectsAssignment() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        when(service.asignar(any(AsignacionRequest.class)))
                .thenThrow(new BadRequestException("Solamente ordenes con status CREATED pueden ser asignados"));

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ordenId\":\"" + ordenId + "\",\"driverId\":\"" + driverId + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Solamente ordenes con status CREATED pueden ser asignados"));
    }

    @Test
    void uploadPdfReturnsAccepted() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).uploadPdf(eq(id), any(MultipartFile.class));
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/{id}/pdf", id).file(file))
                .andExpect(status().isAccepted());

        verify(service).uploadPdf(eq(id), any(MultipartFile.class));
    }

    @Test
    void uploadImageReturnsAccepted() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).uploadImage(eq(id), any(MultipartFile.class));
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "content".getBytes());

        mockMvc.perform(multipart("/{id}/image", id).file(file))
                .andExpect(status().isAccepted());

        verify(service).uploadImage(eq(id), any(MultipartFile.class));
    }
}
