package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientDownloadController {

    @Value("${spring.upload.path}")
    private String commonPath;

    @GetMapping("/client/download")
    public ResponseEntity<Resource> downloadClientFile(
            @RequestParam String date,
            @RequestParam String filename
    ) throws IOException {

        Path baseDir = Paths.get(commonPath, "clientfiles").toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(date).resolve(filename).normalize();

        if (!filePath.startsWith(baseDir) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String downloadName = filePath.getFileName().toString();
        String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"")
                .body(resource);
    }
}
