package com.smartcampus.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file);

    byte[] loadFileAsBytes(String fileName);

    void deleteFile(String fileName);
}
