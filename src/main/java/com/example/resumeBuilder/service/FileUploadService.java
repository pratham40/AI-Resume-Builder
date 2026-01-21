package com.example.resumeBuilder.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class FileUploadService {
    private final Cloudinary cloudinary;

    public FileUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map uploadSingleImage(MultipartFile file) throws IOException {
        try{

            log.info("Uploading image: {}", file.getOriginalFilename());

            Map uploadResponse = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "image"));
            log.info("Image uploaded successfully: {}", uploadResponse);
            return uploadResponse;
        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage());
            throw e;
        }
    }
}