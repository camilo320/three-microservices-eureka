package org.k1000o.ms.asignacion.service;

import jakarta.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import org.k1000o.ms.asignacion.config.FileProperties;
import org.k1000o.ms.asignacion.dto.*;
import org.k1000o.ms.asignacion.entity.Asignacion;
import org.k1000o.ms.asignacion.exception.ResourceNotFoundException;
import org.k1000o.ms.asignacion.feign.DriverFeign;
import org.k1000o.ms.asignacion.feign.OrdenFeign;
import org.k1000o.ms.asignacion.repository.AsignacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AsignacionService {

    private final AsignacionRepository repository;
    private final OrdenFeign ordenFeign;
    private final DriverFeign driverFeign;
    private final FileProperties fileProperties;

    @Transactional
    public UUID asignar(AsignacionRequest request) {
        OrdenResponse orden = ordenFeign.getById(request.ordenId());

        if (orden.status() != Status.CREATED) {
            throw new BadRequestException("Solamente ordenes con status CREATED pueden ser asignados");
        }

        DriverResponse driver = driverFeign.getById(request.driverId());

        if ( !driver.active()) {
            throw new BadRequestException("Driver debe ser active para ser asignado");
        }

        Asignacion asignacion = Asignacion.builder()
                .ordenId(orden.id())
                .driverId(driver.id())
                .build();
        return repository.save(asignacion).getId();
    }
    public AsignacionResponse getById(UUID id){
        return this.toResponse( findAsignacionById(id) );
    }

    @Transactional
    public void uploadPdf(UUID id, MultipartFile file) {
        Asignacion asignacion = findAsignacionById(id);
        asignacion.setPdfFileName(saveFile(file, "pdf"));
        repository.save(asignacion);
    }

    @Transactional
    public void uploadImage(UUID id, MultipartFile file) {
        Asignacion asignacion = findAsignacionById(id);
        asignacion.setImageFileName(saveFile(file, "image"));
        repository.save(asignacion);
    }

    public Asignacion findAsignacionById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrado con id : " + id));
    }

    private AsignacionResponse toResponse(Asignacion asignacion) {
        OrdenResponse orden = asignacion.getOrdenId() == null ? null: ordenFeign.getById(asignacion.getOrdenId());
        DriverResponse driver = asignacion.getDriverId() == null ? null :driverFeign.getById(asignacion.getDriverId());

        return AsignacionResponse.builder()
                .id(asignacion.getId())
                .orden(orden)
                .driver(driver)
                .assignedAt(asignacion.getAssignedAt())
                .updatedAt(asignacion.getUpdatedAt())
                .pdfFileName(asignacion.getPdfFileName())
                .imageFileName(asignacion.getImageFileName())
                .build();
    }
    private String saveFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Archivo es requerido");
        }

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";

        if ("pdf".equals(type) && !"pdf".equals(extension)) {
            throw new BadRequestException("Solamente archivos PDF estan permitidos");
        }
        if ("image".equals(type) && !("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension))) {
            throw new BadRequestException("Solamente archivos PNG o JPG/JPEG estan permitidos");
        }

        String fileName = UUID.randomUUID() + "." + extension;
        try {
            File targetDir = new File(fileProperties.getPath());
            if(!targetDir.exists()){
                Files.createDirectories( targetDir.toPath() );
            }
            Path target = targetDir.toPath().resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new BadRequestException("Error mientras se guardaba el archivo");
        }
    }
}
