package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Certification;
import com.dev.BionLifeScienceWeb.repository.CertificationRepository;

@Service
public class CertificationService {

	@Autowired
	CertificationRepository certificationRepository;

	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public String certificationInsert(MultipartFile file, Certification certification)
			throws IllegalStateException, IOException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String current_date = simpleDateFormat.format(new Date());
		String absolutePath = new File("").getAbsolutePath() + "\\";
		String path = commonPath + "/certification/" + current_date;
		String road = "/administration/certification/" + current_date;
		File fileFolder = new File(path);
	    if(!fileFolder.exists()) {
        	fileFolder.mkdirs();
        }
		
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

		if (!file.isEmpty()) {

			String contentType = file.getContentType();
			String originalFileExtension = "";
			// 확장자 명이 없으면 이 파일은 잘 못 된 것이다
			if (ObjectUtils.isEmpty(contentType)) {
				return "NONE";
			} else {
				if (contentType.contains("image/jpeg")) {
					originalFileExtension = ".jpg";
				} else if (contentType.contains("image/png")) {
					originalFileExtension = ".png";
				} else if (contentType.contains("image/gif")) {
					originalFileExtension = ".gif";
				} else if (contentType.contains("application/pdf")) {
					originalFileExtension = ".pdf";
				} else if (contentType.contains("application/x-zip-compressed")) {
					originalFileExtension = ".zip";
				} else if (contentType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
					originalFileExtension = ".xlsx";
				} else if (contentType
						.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
					originalFileExtension = ".docx";
				} else if (contentType.contains("text/plain")) {
					originalFileExtension = ".txt";
				} else if (contentType.contains("image/x-icon")) {
					originalFileExtension = ".ico";
				} else if (contentType.contains("application/haansofthwp")) {
					originalFileExtension = ".hwp";
				}
			}
			String new_file_name = generatedString + "_" + file.getOriginalFilename();
			if(env.equals("local")) {
				fileFolder = new File(absolutePath + path + "/" + new_file_name);
				certification.setPath(absolutePath + path + "/" + new_file_name);
				
			}else if(env.equals("prod")) {
				fileFolder = new File(path + "/" + new_file_name);
				certification.setPath(path + "/" + new_file_name);
			}

			file.transferTo(fileFolder);
			certification.setRoad(road + "/" + new_file_name);
			certificationRepository.save(certification);
		}
		return "success";
	}
}

