package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SummernoteImgController{
	
	 	@GetMapping("/upload/notice/editor/{dateDir}/{fileName:.+}")
		public ResponseEntity<Resource> getNoticeImage(
				@PathVariable String dateDir,
				@PathVariable String fileName) throws IOException {
		    Path baseDir = Paths.get(System.getProperty("user.dir"), "upload", "front", "images", "notice","editor")
		            .toAbsolutePath().normalize();

		    Path filePath = baseDir.resolve(dateDir).resolve(fileName).normalize();

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
		public Map<String, String> uploadBrandImagge(@RequestParam("file") MultipartFile file) throws IOException {
			
			String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			
			String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
			original = Paths.get(original).getFileName().toString();
			
			String fileName = System.currentTimeMillis() + "_" + original;

		    // 저장 경로 (서버 내부)
		    Path saveDir  = Paths.get(System.getProperty("user.dir"), "upload", "front", "images", "notice","editor" ,today);
		    Files.createDirectories(saveDir);

		    Path savePath = saveDir.resolve(fileName).normalize();
		    
		    if(!savePath.startsWith(saveDir.normalize())) {
		    	throw new IOException("Invalid file path");
		    }
		    
		    file.transferTo(savePath.toFile());

		    // 상품등록처럼 상대경로만 주기
		    String url = "/upload/notice/editor/"+ today + "/" + fileName;

		    return Map.of("url", url);
		}
	 	
	 	
	 	@GetMapping("/upload/page/editor/{dateDir}/{fileName:.+}")
	    public ResponseEntity<Resource> getPageImage(
	            @PathVariable String dateDir,
	            @PathVariable String fileName) throws IOException {

	        Path baseDir = Paths.get(System.getProperty("user.dir"),
	                "upload", "front", "images", "page", "editor")
	                .toAbsolutePath().normalize();

	        Path filePath = baseDir.resolve(dateDir).resolve(fileName).normalize();

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

	    @PostMapping("/admin/pageManager/image")
	    public Map<String, String> uploadPageImage(@RequestParam("file") MultipartFile file) throws IOException {

	        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
	        original = Paths.get(original).getFileName().toString();
	        String lower = original.toLowerCase();

	        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
	                || lower.endsWith(".gif") || lower.endsWith(".webp"))) {
	            throw new IOException("허용되지 않는 이미지 확장자입니다.");
	        }

	        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	        String fileName = System.currentTimeMillis() + "_" + original;

	        Path saveDir = Paths.get(System.getProperty("user.dir"),
	                "upload", "front", "images", "page", "editor", today);
	        Files.createDirectories(saveDir);

	        Path savePath = saveDir.resolve(fileName).normalize();

	        if (!savePath.startsWith(saveDir.normalize())) {
	            throw new IOException("Invalid file path");
	        }

	        file.transferTo(savePath.toFile());

	        String url = "/upload/page/editor/" + today + "/" + fileName;

	        return Map.of("url", url);
	    }
		
		
}