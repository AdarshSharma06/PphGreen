package com.pphgreen.backend.gallery.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pphgreen.backend.common.exception.BadRequestException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.gallery.dto.GalleryResponse;
import com.pphgreen.backend.gallery.dto.GalleryUserSummary;
import com.pphgreen.backend.gallery.entity.Gallery;
import com.pphgreen.backend.gallery.repository.GalleryRepository;
import com.pphgreen.backend.storage.StorageService;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class GalleryService {

    private static final String GALLERY_FOLDER = "gallery";

    private final GalleryRepository galleryRepository;
    private final StorageService storageService;
    private final UserService userService;

    public GalleryService(GalleryRepository galleryRepository, StorageService storageService,
                          UserService userService) {
        this.galleryRepository = galleryRepository;
        this.storageService = storageService;
        this.userService = userService;
    }

    public GalleryResponse upload(MultipartFile file, String uploaderEmail) {
        validateFile(file);

        String mediaType = file.getContentType();
        if (mediaType == null || !isSupported(mediaType)) {
            throw new BadRequestException("Unsupported media type");
        }

        String fileName = sanitizeFileName(file.getOriginalFilename());
        String storagePath = GALLERY_FOLDER + "/" + UUID.randomUUID() + "-" + fileName;

        User uploader = userService.findByEmail(uploaderEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        byte[] bytes = readBytes(file);

        String fileUrl = storageService.upload(bytes, mediaType, storagePath);

        Gallery gallery = new Gallery();
        gallery.setFileName(fileName);
        gallery.setStoragePath(storagePath);
        gallery.setFileUrl(fileUrl);
        gallery.setMediaType(mediaType);
        gallery.setUploadedBy(uploader);

        try {
            Gallery saved = galleryRepository.save(gallery);
            return toResponse(saved);
        } catch (RuntimeException e) {
            try {
                storageService.delete(storagePath);
            } catch (RuntimeException ignored) {
                // best-effort cleanup of the uploaded object
            }
            throw e;
        }
    }

    public List<GalleryResponse> getAll() {
        return galleryRepository.findAllWithUploader()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
    }

    private boolean isSupported(String contentType) {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Unable to read file");
        }
    }

    private String sanitizeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "file";
        }
        String base = originalName.replace("\\", "/");
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        String cleaned = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isEmpty() || cleaned.equals(".") || cleaned.equals("..")) {
            return "file";
        }
        if (cleaned.length() > 100) {
            cleaned = cleaned.substring(cleaned.length() - 100);
        }
        return cleaned;
    }

    private GalleryResponse toResponse(Gallery gallery) {
        User uploader = gallery.getUploadedBy();
        return new GalleryResponse(gallery.getId(), gallery.getFileName(), gallery.getMediaType(),
                gallery.getFileUrl(), new GalleryUserSummary(uploader.getId(), uploader.getName()),
                gallery.getCreatedAt());
    }
}