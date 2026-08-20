package com.pphgreen.backend.storage;

public interface StorageService {

    String upload(byte[] bytes, String contentType, String objectPath);

    void delete(String objectPath);
}