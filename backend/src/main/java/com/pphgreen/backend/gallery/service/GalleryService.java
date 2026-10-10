package com.pphgreen.backend.gallery.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pphgreen.backend.common.exception.BadRequestException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.event.entity.Event;
import com.pphgreen.backend.event.repository.EventRepository;
import com.pphgreen.backend.gallery.dto.GalleryAlbumResponse;
import com.pphgreen.backend.gallery.dto.GalleryResponse;
import com.pphgreen.backend.gallery.dto.GalleryUserSummary;
import com.pphgreen.backend.gallery.entity.Gallery;
import com.pphgreen.backend.gallery.repository.GalleryLikeRepository;
import com.pphgreen.backend.gallery.repository.GalleryRepository;
import com.pphgreen.backend.storage.StorageService;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class GalleryService {

    private static final String GALLERY_FOLDER = "gallery";

    private final GalleryRepository galleryRepository;
    private final StorageService storageService;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final GalleryLikeRepository galleryLikeRepository;

    public GalleryService(GalleryRepository galleryRepository, StorageService storageService,
                          UserService userService, EventRepository eventRepository,
                          GalleryLikeRepository galleryLikeRepository) {
        this.galleryRepository = galleryRepository;
        this.storageService = storageService;
        this.userService = userService;
        this.eventRepository = eventRepository;
        this.galleryLikeRepository = galleryLikeRepository;
    }

    // Backward compatible constructor for existing tests
    public GalleryService(GalleryRepository galleryRepository, StorageService storageService,
                          UserService userService) {
        this(galleryRepository, storageService, userService, null, null);
    }

    public GalleryResponse upload(MultipartFile file, String uploaderEmail) {
        return upload(file, uploaderEmail, null);
    }

    public GalleryResponse upload(MultipartFile file, String uploaderEmail, Long eventId) {
        validateFile(file);

        String mediaType = file.getContentType();
        if (mediaType == null || !isSupported(mediaType)) {
            throw new BadRequestException("Unsupported media type");
        }

        String fileName = sanitizeFileName(file.getOriginalFilename());
        String storagePath = GALLERY_FOLDER + "/" + UUID.randomUUID() + "-" + fileName;

        User uploader = userService.findByEmail(uploaderEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        Event event = null;
        if (eventId != null) {
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        }

        byte[] bytes = readBytes(file);

        String fileUrl = storageService.upload(bytes, mediaType, storagePath);

        Gallery gallery = new Gallery();
        gallery.setFileName(fileName);
        gallery.setStoragePath(storagePath);
        gallery.setFileUrl(fileUrl);
        gallery.setMediaType(mediaType);
        gallery.setUploadedBy(uploader);
        gallery.setEvent(event);
        gallery.setHighlight(false);

        try {
            Gallery saved = galleryRepository.save(gallery);
            return toResponse(saved, uploaderEmail);
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
        return getAll(null);
    }

    public List<GalleryResponse> getAll(String currentUserEmail) {
        return galleryRepository.findAllWithUploader()
                .stream()
                .map(g -> toResponse(g, currentUserEmail))
                .toList();
    }

    public List<GalleryResponse> getByEvent(Long eventId, String currentUserEmail) {
        return galleryRepository.findByEventIdWithUploader(eventId)
                .stream()
                .map(g -> toResponse(g, currentUserEmail))
                .toList();
    }

    public List<GalleryResponse> getStandalone(String currentUserEmail) {
        return galleryRepository.findStandaloneWithUploader()
                .stream()
                .map(g -> toResponse(g, currentUserEmail))
                .toList();
    }

    public List<GalleryResponse> getHighlights(Long eventId, String currentUserEmail) {
        return galleryRepository.findHighlightsByEventId(eventId)
                .stream()
                .map(g -> toResponse(g, currentUserEmail))
                .toList();
    }

    public List<GalleryResponse> getEventMediaSorted(Long eventId, String currentUserEmail, Pageable pageable) {
        // Fetch all for event, then sort by likeCount desc, createdAt desc
        List<Gallery> items = galleryRepository.findByEventIdWithUploader(eventId);
        // Build like counts map
        java.util.Map<Long, Long> likeCounts = new java.util.HashMap<>();
        for (Gallery g : items) {
            long cnt = galleryLikeRepository != null ? galleryLikeRepository.countByGalleryId(g.getId()) : 0;
            likeCounts.put(g.getId(), cnt);
        }
        return items.stream()
                .sorted((a, b) -> {
                    long ca = likeCounts.getOrDefault(a.getId(), 0L);
                    long cb = likeCounts.getOrDefault(b.getId(), 0L);
                    int cmp = Long.compare(cb, ca); // most liked first
                    if (cmp != 0) return cmp;
                    // deterministic secondary: newest first
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(g -> toResponse(g, currentUserEmail))
                .toList();
    }

    public GalleryResponse toggleHighlight(Long galleryId, String actorEmail, Boolean highlight, Integer order) {
        User actor = userService.findByEmail(actorEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can modify highlights");
        }
        Gallery gallery = galleryRepository.findWithUploaderAndEvent(galleryId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery", galleryId));
        if (gallery.getEvent() == null) {
            throw new BadRequestException("Only event media can be highlighted");
        }
        gallery.setHighlight(highlight != null ? highlight : !Boolean.TRUE.equals(gallery.getHighlight()));
        if (order != null) {
            gallery.setHighlightOrder(order);
        } else if (Boolean.TRUE.equals(gallery.getHighlight()) && gallery.getHighlightOrder() == null) {
            // auto assign next order
            gallery.setHighlightOrder((int) (galleryRepository.countByEventId(gallery.getEvent().getId()) + 1000));
        }
        if (!Boolean.TRUE.equals(gallery.getHighlight())) {
            gallery.setHighlightOrder(null);
        }
        Gallery saved = galleryRepository.save(gallery);
        return toResponse(saved, actorEmail);
    }

    public List<GalleryAlbumResponse> getAlbums() {
        List<Event> events = eventRepository != null ? eventRepository.findAllWithCreatedBy() : List.of();
        // Pre-group counts
        java.util.Map<Long, List<Gallery>> grouped = new java.util.HashMap<>();
        java.util.Map<Long, Long> totalLikesMap = new java.util.HashMap<>();
        if (galleryLikeRepository != null) {
            // fetch all galleries to compute thumbnail/likes efficiently single query
            List<Gallery> all = galleryRepository.findAllWithUploader();
            for (Gallery g : all) {
                if (g.getEvent() != null) {
                    grouped.computeIfAbsent(g.getEvent().getId(), k -> new java.util.ArrayList<>()).add(g);
                }
            }
            for (var e : grouped.entrySet()) {
                long total = 0;
                for (Gallery g : e.getValue()) {
                    total += galleryLikeRepository.countByGalleryId(g.getId());
                }
                totalLikesMap.put(e.getKey(), total);
            }
        } else {
            List<Gallery> all = galleryRepository.findAllWithUploader();
            for (Gallery g : all) {
                if (g.getEvent() != null) {
                    grouped.computeIfAbsent(g.getEvent().getId(), k -> new java.util.ArrayList<>()).add(g);
                }
            }
        }

        List<GalleryAlbumResponse> albums = new java.util.ArrayList<>();
        for (Event event : events) {
            List<Gallery> media = grouped.getOrDefault(event.getId(), List.of());
            int photoCount = (int) media.stream().filter(m -> m.getMediaType().startsWith("image/")).count();
            int videoCount = media.size() - photoCount;

            // Thumbnail selection: first highlight, else first media, else event.image, else placeholder null
            String thumbnail = null;
            // 1. highlight
            var highlights = media.stream().filter(m -> Boolean.TRUE.equals(m.getHighlight())).sorted(
                java.util.Comparator.comparing(Gallery::getHighlightOrder, java.util.Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(Gallery::getCreatedAt)).toList();
            if (!highlights.isEmpty() && highlights.get(0).getFileUrl() != null) {
                thumbnail = highlights.get(0).getFileUrl();
            } else if (!media.isEmpty()) {
                thumbnail = media.get(0).getFileUrl();
            } else if (event.getImage() != null) {
                thumbnail = event.getImage();
            }

            long totalLikes = totalLikesMap.getOrDefault(event.getId(), 0L);

            albums.add(new GalleryAlbumResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getTime(),
                event.getVenue(),
                event.getImage(),
                photoCount,
                videoCount,
                media.size(),
                thumbnail,
                totalLikes,
                new GalleryUserSummary(event.getCreatedBy().getId(), event.getCreatedBy().getName())
            ));
        }
        // Sort albums by event date desc (newest first) for homepage
        albums.sort((a, b) -> {
            if (a.date() == null && b.date() == null) return 0;
            if (a.date() == null) return 1;
            if (b.date() == null) return -1;
            return b.date().compareTo(a.date());
        });
        return albums;
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
        return toResponse(gallery, null);
    }

    private GalleryResponse toResponse(Gallery gallery, String currentUserEmail) {
        User uploader = gallery.getUploadedBy();
        Long eventId = gallery.getEvent() != null ? gallery.getEvent().getId() : null;
        long likeCount = 0;
        boolean liked = false;
        if (galleryLikeRepository != null) {
            try {
                likeCount = galleryLikeRepository.countByGalleryId(gallery.getId());
                if (currentUserEmail != null) {
                    var cur = userService.findByEmail(currentUserEmail);
                    if (cur.isPresent()) {
                        liked = galleryLikeRepository.existsByGalleryIdAndUserId(gallery.getId(), cur.get().getId());
                    }
                }
            } catch (Exception ignored) {}
        }
        // Handle case where event is lazy and not initialized, avoid NPE
        return new GalleryResponse(gallery.getId(), gallery.getFileName(), gallery.getMediaType(),
                gallery.getFileUrl(), new GalleryUserSummary(uploader.getId(), uploader.getName()),
                gallery.getCreatedAt(), eventId, gallery.getHighlight(), gallery.getHighlightOrder(),
                likeCount, liked);
    }
}