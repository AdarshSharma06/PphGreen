package com.pphgreen.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.pphgreen.backend.common.exception.StorageException;

@Component
public class SupabaseStorageService implements StorageService {

    private final RestClient restClient;
    private final String baseUrl;
    private final String baseObjectUrl;
    private final String serviceRoleKey;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.service-role-key:}") String serviceRoleKey,
            @Value("${app.supabase.storage-bucket:}") String bucket) {
        this.baseUrl = supabaseUrl == null ? "" : supabaseUrl.trim().replaceAll("/+$", "");
        this.baseObjectUrl = baseUrl + "/storage/v1/object";
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.bucket = bucket == null ? "" : bucket.trim();
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String upload(byte[] bytes, String contentType, String objectPath) {
        checkConfigured();

        String object = bucket + "/" + objectPath;
        try {
            restClient.post()
                    .uri(baseObjectUrl + "/" + object)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new StorageException("Storage upload failed");
        }

        return publicUrl(objectPath);
    }

    @Override
    public void delete(String objectPath) {
        checkConfigured();

        String object = bucket + "/" + objectPath;
        try {
            restClient.delete()
                    .uri(baseObjectUrl + "/" + object)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new StorageException("Storage delete failed");
        }
    }

    private String publicUrl(String objectPath) {
        return baseObjectUrl + "/public/" + bucket + "/" + objectPath;
    }

    private void checkConfigured() {
        if (baseUrl.isBlank() || serviceRoleKey.isBlank() || bucket.isBlank()) {
            throw new StorageException("Storage is not configured");
        }
    }
}