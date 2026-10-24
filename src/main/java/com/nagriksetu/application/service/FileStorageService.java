package com.nagriksetu.application.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    @Value("${app.upload-dir}") private String uploadDirectory;
    private Path storagePath;
    @PostConstruct void initialise() { try { storagePath = Paths.get(uploadDirectory).toAbsolutePath().normalize(); Files.createDirectories(storagePath); } catch (IOException exception) { throw new IllegalStateException("Could not create the local upload directory", exception); } }
    public String storeImage(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) return null;
        if (!IMAGE_TYPES.contains(file.getContentType())) throw new IllegalArgumentException("Please upload a JPG, PNG, WEBP, or GIF image.");
        String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase() : ".jpg";
        String storedName = prefix + "-" + UUID.randomUUID() + extension;
        try { Files.copy(file.getInputStream(), storagePath.resolve(storedName), StandardCopyOption.REPLACE_EXISTING); return "/uploads/" + storedName; }
        catch (IOException exception) { throw new IllegalStateException("The image could not be saved.", exception); }
    }
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) return;
        Path imagePath = storagePath.resolve(imageUrl.substring("/uploads/".length())).normalize();
        if (!imagePath.getParent().equals(storagePath)) throw new IllegalArgumentException("Invalid image path.");
        try { Files.deleteIfExists(imagePath); } catch (IOException exception) { throw new IllegalStateException("The saved evidence image could not be deleted.", exception); }
    }
}
