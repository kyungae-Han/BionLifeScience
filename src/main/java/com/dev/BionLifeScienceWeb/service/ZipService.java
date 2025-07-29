package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

@Service
public class ZipService {

	@Value("${spring.upload.env}")
	private String env;

	@Value("${spring.upload.path}")
	private String commonPath;

	public ResponseEntity<Object> downZip() {
		String absolutePath = new File("").getAbsolutePath() + "/";
		String existFilePath = "";
		String zipFilePath = "";
		if(env.equals("local")) {
			existFilePath = absolutePath + commonPath + "/company";
			zipFilePath = absolutePath + "company.zip";
		}else if(env.equals("prod")) {
			existFilePath = commonPath + "/company";
			zipFilePath = commonPath + "company.zip";
		}
		ZipUtil.pack(new File(existFilePath), new File(zipFilePath));
		

		
		try {
			Path filePath = Paths.get(zipFilePath);
			Resource resource = new InputStreamResource(Files.newInputStream(filePath)); // 파일 resource 얻기
			
			File file = new File(zipFilePath);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getName()).build());  // 다운로드 되거나 로컬에 저장되는 용도로 쓰이는지를 알려주는 헤더
			
			return new ResponseEntity<Object>(resource, headers, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>(null, HttpStatus.CONFLICT);
		}
		
	}
}
