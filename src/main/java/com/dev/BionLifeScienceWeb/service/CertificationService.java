package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Certification;
import com.dev.BionLifeScienceWeb.repository.CertificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificationService {

	private final CertificationRepository certificationRepository;

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
		if (!fileFolder.exists()) {
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
			// 확장자 명이 없으면 이 파일은 잘 못 된 것이다
			if (ObjectUtils.isEmpty(contentType)) {
				return "NONE";
			}
			String new_file_name = generatedString + "_" + file.getOriginalFilename();
			if (env.equals("local")) {
				fileFolder = new File(absolutePath + path + "/" + new_file_name);
				certification.setPath(absolutePath + path + "/" + new_file_name);

			} else if (env.equals("prod")) {
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
