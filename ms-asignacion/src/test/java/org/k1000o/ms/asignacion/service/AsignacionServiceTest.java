package org.k1000o.ms.asignacion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.k1000o.ms.asignacion.config.FileProperties;
import org.k1000o.ms.asignacion.dto.AsignacionRequest;
import org.k1000o.ms.asignacion.dto.AsignacionResponse;
import org.k1000o.ms.asignacion.dto.DriverResponse;
import org.k1000o.ms.asignacion.dto.OrdenResponse;
import org.k1000o.ms.asignacion.dto.Status;
import org.k1000o.ms.asignacion.entity.Asignacion;
import org.k1000o.ms.asignacion.exception.BadRequestException;
import org.k1000o.ms.asignacion.exception.ResourceNotFoundException;
import org.k1000o.ms.asignacion.feign.DriverFeign;
import org.k1000o.ms.asignacion.feign.OrdenFeign;
import org.k1000o.ms.asignacion.repository.AsignacionRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock
    private AsignacionRepository repository;

    @Mock
    private OrdenFeign ordenFeign;

    @Mock
    private DriverFeign driverFeign;

    @TempDir
    Path tempDir;

    private FileProperties fileProperties;
    private AsignacionService service;

    @BeforeEach
    void setUp() {
        fileProperties = new FileProperties();
        fileProperties.setPath(tempDir.toString());
        service = new AsignacionService(repository, ordenFeign, driverFeign, fileProperties);
    }

    @Test
    void asignarPersistsAssignmentWhenOrderIsCreatedAndDriverIsActive() {
        UUID ordenId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();
        when(ordenFeign.getById(ordenId)).thenReturn(orden(ordenId, Status.CREATED));
        when(driverFeign.getById(driverId)).thenReturn(driver(driverId, true));
        when(repository.save(any(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0);
            asignacion.setId(asignacionId);
            return asignacion;
        });

        UUID result = service.asignar(new AsignacionRequest(ordenId, driverId));

        assertThat(result).isEqualTo(asignacionId);

        ArgumentCaptor<Asignacion> captor = ArgumentCaptor.forClass(Asignacion.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOrdenId()).isEqualTo(ordenId);
        assertThat(captor.getValue().getDriverId()).isEqualTo(driverId);
    }

    @Test
    void asignarRejectsOrderThatIsNotInCreatedStatus() {
        UUID ordenId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        when(ordenFeign.getById(ordenId)).thenReturn(orden(ordenId, Status.IN_TRANSIT));

        assertThatThrownBy(() -> service.asignar(new AsignacionRequest(ordenId, driverId)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Solamente ordenes con status CREATED");

        verifyNoInteractions(driverFeign);
        verify(repository, never()).save(any(Asignacion.class));
    }

    @Test
    void asignarRejectsInactiveDriver() {
        UUID ordenId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        when(ordenFeign.getById(ordenId)).thenReturn(orden(ordenId, Status.CREATED));
        when(driverFeign.getById(driverId)).thenReturn(driver(driverId, false));

        assertThatThrownBy(() -> service.asignar(new AsignacionRequest(ordenId, driverId)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Driver debe ser active");

        verify(repository, never()).save(any(Asignacion.class));
    }

    @Test
    void getByIdReturnsAssignmentWithOrderAndDriver() {
        UUID id = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        LocalDateTime assignedAt = LocalDateTime.of(2026, 1, 10, 8, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 11, 9, 0);
        Asignacion asignacion = Asignacion.builder()
                .id(id)
                .ordenId(ordenId)
                .driverId(driverId)
                .pdfFileName("document.pdf")
                .imageFileName("image.png")
                .assignedAt(assignedAt)
                .updatedAt(updatedAt)
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(asignacion));
        when(ordenFeign.getById(ordenId)).thenReturn(orden(ordenId, Status.CREATED));
        when(driverFeign.getById(driverId)).thenReturn(driver(driverId, true));

        AsignacionResponse response = service.getById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.orden().id()).isEqualTo(ordenId);
        assertThat(response.driver().id()).isEqualTo(driverId);
        assertThat(response.pdfFileName()).isEqualTo("document.pdf");
        assertThat(response.imageFileName()).isEqualTo("image.png");
        assertThat(response.assignedAt()).isEqualTo(assignedAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void getByIdDoesNotCallRemoteServicesWhenIdsAreNull() {
        UUID id = UUID.randomUUID();
        Asignacion asignacion = Asignacion.builder().id(id).build();
        when(repository.findById(id)).thenReturn(Optional.of(asignacion));

        AsignacionResponse response = service.getById(id);

        assertThat(response.orden()).isNull();
        assertThat(response.driver()).isNull();
        verifyNoInteractions(ordenFeign, driverFeign);
    }

    @Test
    void getByIdThrowsWhenAssignmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Asignacion no encontrado con id : " + id);
    }

    @Test
    void uploadPdfStoresFileAndUpdatesAssignment() throws Exception {
        UUID id = UUID.randomUUID();
        Asignacion asignacion = Asignacion.builder().id(id).build();
        when(repository.findById(id)).thenReturn(Optional.of(asignacion));

        service.uploadPdf(id, multipart("document.PDF", "application/pdf", "pdf-content"));

        assertThat(asignacion.getPdfFileName()).endsWith(".pdf");
        Path stored = tempDir.resolve(asignacion.getPdfFileName());
        assertThat(stored).exists();
        assertThat(Files.readString(stored)).isEqualTo("pdf-content");
        verify(repository).save(asignacion);
    }

    @Test
    void uploadPdfRejectsFileWithoutPdfExtension() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Asignacion.builder().id(id).build()));

        assertThatThrownBy(() -> service.uploadPdf(id, multipart("document.txt", "text/plain", "text")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Solamente archivos PDF estan permitidos");

        verify(repository, never()).save(any(Asignacion.class));
    }

    @Test
    void uploadPdfRejectsFileWithoutExtension() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Asignacion.builder().id(id).build()));

        assertThatThrownBy(() -> service.uploadPdf(id, multipart("document", "application/pdf", "text")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Solamente archivos PDF estan permitidos");
    }

    @Test
    void uploadPdfRejectsEmptyFile() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Asignacion.builder().id(id).build()));

        assertThatThrownBy(() -> service.uploadPdf(id, multipart("document.pdf", "application/pdf", "")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Archivo es requerido");
    }

    @Test
    void uploadPdfRejectsNullFile() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Asignacion.builder().id(id).build()));

        assertThatThrownBy(() -> service.uploadPdf(id, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Archivo es requerido");
    }

    @Test
    void uploadPdfWrapsIOExceptionWhenPathIsNotADirectory() throws Exception {
        UUID id = UUID.randomUUID();
        Path notADirectory = tempDir.resolve("not-a-directory");
        Files.writeString(notADirectory, "content");
        fileProperties.setPath(notADirectory.toString());
        when(repository.findById(id)).thenReturn(Optional.of(Asignacion.builder().id(id).build()));

        assertThatThrownBy(() -> service.uploadPdf(id, multipart("document.pdf", "application/pdf", "data")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Error mientras se guardaba el archivo");
    }

    @Test
    void uploadImageStoresPngFileAndUpdatesAssignment() throws Exception {
        UUID id = UUID.randomUUID();
        Asignacion asignacion = Asignacion.builder().id(id).build();
        when(repository.findById(id)).thenReturn(Optional.of(asignacion));

        service.uploadImage(id, multipart("photo.PNG", "image/png", "png-content"));

        assertThat(asignacion.getImageFileName()).endsWith(".png");
        Path stored = tempDir.resolve(asignacion.getImageFileName());
        assertThat(stored).exists();
        assertThat(Files.readString(stored)).isEqualTo("png-content");
        verify(repository).save(asignacion);
    }

    @Test
    void uploadImageAcceptsJpegExtension() {
        UUID id = UUID.randomUUID();
        Asignacion asignacion = Asignacion.builder().id(id).build();
        when(repository.findById(id)).thenReturn(Optional.of(asignacion));

        service.uploadImage(id, multipart("photo.jpeg", "image/jpeg", "jpeg-content"));

        assertThat(asignacion.getImageFileName()).endsWith(".jpeg");
        verify(repository).save(asignacion);
    }

    @Test
    void uploadImageRejectsUnsupportedExtension() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Asignacion.builder().id(id).build()));

        assertThatThrownBy(() -> service.uploadImage(id, multipart("photo.gif", "image/gif", "gif-content")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Solamente archivos PNG o JPG/JPEG estan permitidos");

        verify(repository, never()).save(any(Asignacion.class));
    }

    @Test
    void uploadImageRejectsPdfFile() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Asignacion.builder().id(id).build()));

        assertThatThrownBy(() -> service.uploadImage(id, multipart("document.pdf", "application/pdf", "pdf-content")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Solamente archivos PNG o JPG/JPEG estan permitidos");
    }

    private OrdenResponse orden(UUID id, Status status) {
        return OrdenResponse.builder()
                .id(id)
                .origin("Bogota")
                .destination("Cali")
                .status(status)
                .build();
    }

    private DriverResponse driver(UUID id, boolean active) {
        return DriverResponse.builder()
                .id(id)
                .name("John Doe")
                .licenseNumber("AB-123")
                .active(active)
                .build();
    }

    private org.springframework.mock.web.MockMultipartFile multipart(String fileName, String contentType, String content) {
        return new org.springframework.mock.web.MockMultipartFile(
                "file", fileName, contentType, content.getBytes(StandardCharsets.UTF_8));
    }
}
