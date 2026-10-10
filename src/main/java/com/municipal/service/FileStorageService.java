package com.municipal.service;

import com.municipal.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the upload directory: " + this.fileStorageLocation, ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");

        // Validate path traversal
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFilename);
        }

        // Validate file extension
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only image files (JPG, JPEG, PNG, WEBP) are allowed. Provided: " + extension);
        }

        // Validate Content-Type
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid image content type: " + contentType);
        }

        // Generate safe unique filename
        String cleanBaseName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String targetFilename = UUID.randomUUID().toString() + "_" + cleanBaseName;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(targetFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + targetFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + targetFilename + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            // Remove /uploads/ prefix if present
            if (fileName.startsWith("/uploads/")) {
                fileName = fileName.substring("/uploads/".length());
            } else if (fileName.startsWith("uploads/")) {
                fileName = fileName.substring("uploads/".length());
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File not found: " + fileName);
        }
    }
}
