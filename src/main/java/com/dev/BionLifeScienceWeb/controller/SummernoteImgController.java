package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SummernoteImgController{
	
		@Value("{spring.upload.path}")
		private String uploadPath;
		
	
	 	@GetMapping("/upload/notice/{fileName:.+}")
		public ResponseEntity<Resource> getNoticeImage(@PathVariable String fileName) throws IOException {
		    Path baseDir = Paths.get(System.getProperty("user.dir"), "upload", "front", "images", "notice")
		            .toAbsolutePath().normalize();

		    Path filePath = baseDir.resolve(fileName).normalize();

		    // 경로 조작 방지 + 존재 확인
		    if (!filePath.startsWith(baseDir) || !Files.exists(filePath)) {
		        return ResponseEntity.notFound().build();
		    }

		    Resource resource = new UrlResource(filePath.toUri());
		    if (!resource.exists() || !resource.isReadable()) {
		        return ResponseEntity.notFound().build();
		    }

		    String contentType = Files.probeContentType(filePath);
		    MediaType mediaType = (contentType != null)
		            ? MediaType.parseMediaType(contentType)
		            : MediaType.APPLICATION_OCTET_STREAM;

		    return ResponseEntity.ok()
		            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
		            .contentType(mediaType)
		            .body(resource);
		}
		
		
		@PostMapping("/admin/notice/image")
		public Map<String, String> uploadNoticeImage(@RequestParam("file") MultipartFile file) throws IOException {
			
			String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
			original = Paths.get(original).getFileName().toString();
			
			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

		    // 저장 경로 (서버 내부)
		    Path saveDir  = Paths.get(System.getProperty("user.dir"), "upload", "front", "images", "notice");
		    Files.createDirectories(saveDir);

		    Path savePath = saveDir.resolve(fileName);
		    file.transferTo(savePath.toFile());

		    // 상품등록처럼 상대경로만 주기
		    String url = "/upload/notice/" + fileName;

		    return Map.of("url", url);
		}
}