package com.pphgreen.backend.gallery.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.pphgreen.backend.common.exception.BadRequestException;
import com.pphgreen.backend.common.exception.StorageException;
import com.pphgreen.backend.gallery.dto.GalleryResponse;
import com.pphgreen.backend.gallery.dto.GalleryUserSummary;
import com.pphgreen.backend.gallery.entity.Gallery;
import com.pphgreen.backend.gallery.repository.GalleryRepository;
import com.pphgreen.backend.storage.StorageService;
import com.pphgreen.backend.user.entity.AccountStatus;
import com.pphgreen.backend.user.entity.Role;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

    private static final String PUBLIC_URL = "https://proj.supabase.co/storage/v1/object/public/gallery/abc.jpg";

    @Mock
    private GalleryRepository galleryRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private UserService userService;

    private GalleryService galleryService;

    @BeforeEach
    void setUp() {
        galleryService = new GalleryService(galleryRepository, storageService, userService);
    }

    @Test
    void uploadSucceedsForAdmin() {
        stubSuccessfulUpload();

        GalleryResponse response = galleryService.upload(image(), "admin@example.com");

        assertEquals("photo.jpg", response.fileName());
        assertEquals("image/jpeg", response.mediaType());
        assertEquals(PUBLIC_URL, response.fileUrl());
        assertEquals("Jane Doe", response.uploadedBy().name());
    }

    @Test
    void imageAccepted() {
        stubSuccessfulUpload();

        GalleryResponse response = galleryService.upload(image(), "admin@example.com");

        assertEquals("image/jpeg", response.mediaType());
        verify(galleryRepository).save(any(Gallery.class));
    }

    @Test
    void videoAccepted() {
        stubSuccessfulUpload();

        GalleryResponse response = galleryService.upload(video(), "admin@example.com");

        assertEquals("video/mp4", response.mediaType());
        verify(galleryRepository).save(any(Gallery.class));
    }

    @Test
    void emptyFileRejected() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(BadRequestException.class, () -> galleryService.upload(empty, "admin@example.com"));
        verify(galleryRepository, never()).save(any(Gallery.class));
    }

    @Test
    void missingFileRejected() {
        assertThrows(BadRequestException.class, () -> galleryService.upload(null, "admin@example.com"));
    }

    @Test
    void unsupportedMediaRejected() {
        MockMultipartFile exe = new MockMultipartFile("file", "virus.exe", "application/x-msdownload", new byte[]{'M', 'Z'});
        MockMultipartFile html = new MockMultipartFile("file", "page.html", "text/html", "<html></html>".getBytes());
        MockMultipartFile js = new MockMultipartFile("file", "script.js", "application/javascript", new byte[]{'x'});

        assertThrows(BadRequestException.class, () -> galleryService.upload(exe, "admin@example.com"));
        assertThrows(BadRequestException.class, () -> galleryService.upload(html, "admin@example.com"));
        assertThrows(BadRequestException.class, () -> galleryService.upload(js, "admin@example.com"));
        verify(galleryRepository, never()).save(any(Gallery.class));
    }

    @Test
    void storageServiceCalledWithBytesTypeAndSafePath() {
        stubSuccessfulUpload();

        galleryService.upload(image(), "admin@example.com");

        ArgumentCaptor<byte[]> bytes = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> type = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(storageService).upload(bytes.capture(), type.capture(), path.capture());

        assertArrayEquals(new byte[]{1, 2, 3}, bytes.getValue());
        assertEquals("image/jpeg", type.getValue());
        assertTrue(path.getValue().startsWith("gallery/"));
        assertTrue(path.getValue().endsWith("-photo.jpg"));
        assertFalse(path.getValue().contains(".."));
    }

    @Test
    void metadataPersistedAfterSuccessfulUpload() {
        stubSuccessfulUpload();

        galleryService.upload(image(), "admin@example.com");

        ArgumentCaptor<Gallery> captor = ArgumentCaptor.forClass(Gallery.class);
        verify(galleryRepository).save(captor.capture());

        Gallery saved = captor.getValue();
        assertEquals("photo.jpg", saved.getFileName());
        assertEquals("image/jpeg", saved.getMediaType());
        assertEquals(PUBLIC_URL, saved.getFileUrl());
        assertTrue(saved.getStoragePath().startsWith("gallery/"));
        assertEquals("admin@example.com", saved.getUploadedBy().getEmail());
    }

    @Test
    void storageFailureDoesNotCreateGalleryRecord() {
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser()));
        when(storageService.upload(any(byte[].class), anyString(), anyString()))
                .thenThrow(new StorageException("Storage upload failed"));

        assertThrows(StorageException.class, () -> galleryService.upload(image(), "admin@example.com"));
        verify(galleryRepository, never()).save(any(Gallery.class));
    }

    @Test
    void getReturnsSavedGalleryMetadata() {
        Gallery gallery = new Gallery();
        gallery.setId(1L);
        gallery.setFileName("photo.jpg");
        gallery.setMediaType("image/jpeg");
        gallery.setFileUrl(PUBLIC_URL);
        gallery.setUploadedBy(adminUser());
        when(galleryRepository.findAllWithUploader()).thenReturn(List.of(gallery));

        List<GalleryResponse> responses = galleryService.getAll();

        assertEquals(1, responses.size());
        assertEquals("photo.jpg", responses.get(0).fileName());
        assertEquals("image/jpeg", responses.get(0).mediaType());
        assertEquals(PUBLIC_URL, responses.get(0).fileUrl());
        assertEquals("Jane Doe", responses.get(0).uploadedBy().name());
    }

    @Test
    void responseDoesNotExposeSecrets() {
        stubSuccessfulUpload();

        GalleryResponse response = galleryService.upload(image(), "admin@example.com");

        assertInstanceOf(GalleryUserSummary.class, response.uploadedBy());
        List<String> responseComponents = Arrays.stream(GalleryResponse.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();
        assertFalse(responseComponents.contains("storagePath"));
        assertFalse(responseComponents.contains("passwordHash"));
        assertFalse(responseComponents.contains("email"));
        assertFalse(responseComponents.contains("serviceRoleKey"));

        List<String> summaryComponents = Arrays.stream(GalleryUserSummary.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();
        assertEquals(List.of("id", "name"), summaryComponents);
    }

    private void stubSuccessfulUpload() {
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser()));
        when(storageService.upload(any(byte[].class), anyString(), anyString())).thenReturn(PUBLIC_URL);
        when(galleryRepository.save(any(Gallery.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private MockMultipartFile image() {
        return new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    private MockMultipartFile video() {
        return new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[]{9, 9, 9, 9});
    }

    private User adminUser() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(Role.ADMIN);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setName("Jane Doe");
        return user;
    }
}