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

		if (file == null || file.isEmpty()) {
			return "NONE";
		}
		if (!storeImage(file, certification)) {
			return "NONE";
		}

		// 새 인증서는 목록 맨 뒤에 붙인다. 끌어서 올리는 것은 사용자가 한다
		certification.setCertificationIndex(nextIndex());
		certificationRepository.save(certification);
		return "success";
	}

	/**
	 * 기존 인증서를 고친다. 이미지를 새로 고르지 않으면 원래 이미지를 그대로 둔다.
	 * 순서도 건드리지 않는다. 순서는 목록에서 끌어서만 바뀐다.
	 */
	public String certificationUpdate(MultipartFile file, Certification form)
			throws IllegalStateException, IOException {

		Certification saved = certificationRepository.findById(form.getId()).orElse(null);
		if (saved == null) {
			return "NONE";
		}

		saved.setSubject(form.getSubject());
		saved.setContent(form.getContent());
		saved.setSort(form.getSort());

		if (file != null && !file.isEmpty()) {
			// 새 파일을 받았을 때만 경로를 갈아 끼운다.
			// 옛 파일은 서버에 남긴다. 삭제도 파일은 남기므로 동작을 맞춘 것이다
			if (!storeImage(file, saved)) {
				return "NONE";
			}
		}

		certificationRepository.save(saved);
		return "success";
	}

	/** 목록 맨 뒤 번호. 한 건도 없으면 1 이다 */
	private int nextIndex() {
		return certificationRepository.findAll().stream()
				.map(Certification::getCertificationIndex)
				.filter(i -> i != null)
				.mapToInt(Integer::intValue)
				.max().orElse(0) + 1;
	}

	/**
	 * 파일을 저장하고 대상 인증서의 road/path 를 채운다.
	 *
	 * road 는 화면이 부르는 주소라 반드시 /upload 로 시작해야 한다.
	 * 이 주소만 WebConfig 의 리소스 핸들러를 타고 업로드 폴더를 가리킨다.
	 * 예전에는 /administration 으로 저장해서, 운영에서는 파일이 webapps 에 있는데
	 * 화면은 WAR 안의 정적 폴더를 뒤지느라 새로 올린 이미지가 전부 깨졌다.
	 */
	private boolean storeImage(MultipartFile file, Certification certification)
			throws IllegalStateException, IOException {

		String contentType = file.getContentType();
		// 확장자 명이 없으면 이 파일은 잘 못 된 것이다
		if (ObjectUtils.isEmpty(contentType)) {
			return false;
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String current_date = simpleDateFormat.format(new Date());
		String absolutePath = new File("").getAbsolutePath() + "\\";
		String path = commonPath + "/certification/" + current_date;
		String road = "/upload/certification/" + current_date;

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
		return true;
	}
}
