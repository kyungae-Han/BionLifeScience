package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.CompanyInfo;
import com.dev.BionLifeScienceWeb.repository.CompanyInfoRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyInfoService {

	private final CompanyInfoRepository companyInfoRepository;

	/** 프로필마다 다르다. 로컬은 static/administration, 운영은 톰캣 webapps */
	@Value("${spring.upload.path}")
	private String uploadPath;

	/** 저장한 파일을 내보내는 주소. WebConfig 의 /upload/** 리소스 핸들러와 짝이다 */
	private static final String UPLOAD_URL_PREFIX = "/upload/company/about";

	public void changeCompanyInfo(CompanyInfo companyInfo) {
		if (companyInfoRepository.findById(1L).isPresent()) {

			companyInfoRepository.findById(1L).ifPresent(s -> {
				s.setCompanyAddress(companyInfo.getCompanyAddress());
				s.setCompanyName(companyInfo.getCompanyName());
				s.setCompanyNumber(companyInfo.getCompanyNumber());
				s.setCompanyTelephone(companyInfo.getCompanyTelephone());
				s.setCompanyEmailCheck(companyInfo.getCompanyEmailCheck());
				companyInfoRepository.save(s);
			});
		} else {
			CompanyInfo s = new CompanyInfo();
			s.setCompanyAddress(companyInfo.getCompanyAddress());
			s.setCompanyName(companyInfo.getCompanyName());
			s.setCompanyNumber(companyInfo.getCompanyNumber());
			s.setCompanyTelephone(companyInfo.getCompanyTelephone());
			s.setCompanyEmailCheck(companyInfo.getCompanyEmailCheck());
			companyInfoRepository.save(s);
		}
	}

	public void changeCompanyMail(Boolean companyEmail) {
		companyInfoRepository.findById(1L).ifPresent(s -> {
			s.setCompanyEmailCheck(companyEmail);
			companyInfoRepository.save(s);
		});
	}

	/**
	 * 회사소개 상단 배너를 올린다. 한글·영문 각각이고 둘 다 선택이다.
	 * 안 고른 쪽은 그대로 둔다 (실수로 비우는 것을 막는다).
	 *
	 * ⚠️ 지우기는 {@link #clearAboutBanner(boolean)} 로 한다.
	 *    "안 고름" 과 "지움" 을 같은 요청으로 섞으면 어느 쪽인지 알 수 없다.
	 */
	public void saveAboutBanner(MultipartFile koFile, MultipartFile enFile) {
		CompanyInfo info = companyInfoRepository.findById(1L).orElseGet(CompanyInfo::new);

		if (koFile != null && !koFile.isEmpty()) {
			info.setAboutBanner(store(koFile));
		}
		if (enFile != null && !enFile.isEmpty()) {
			info.setAboutBannerEn(store(enFile));
		}
		companyInfoRepository.save(info);
	}

	/** 올린 배너를 치운다. 기본 그림으로 돌아간다. 파일 자체는 남겨 둔다 */
	public void clearAboutBanner(boolean english) {
		companyInfoRepository.findById(1L).ifPresent(s -> {
			if (english) {
				s.setAboutBannerEn(null);
			} else {
				s.setAboutBanner(null);
			}
			companyInfoRepository.save(s);
		});
	}

	/**
	 * 파일을 업로드 폴더에 저장하고 화면에서 쓸 주소를 돌려준다.
	 * 저장 위치는 PageContentAdminService 의 비주얼 이미지와 같은 방식이다
	 * (업로드 폴더 / 갈래 / 날짜 / 임의이름.확장자).
	 */
	private String store(MultipartFile file) {
		String originalName = file.getOriginalFilename();
		if (originalName == null || originalName.isBlank()) {
			throw new IllegalArgumentException("파일 이름이 올바르지 않습니다.");
		}

		String lower = originalName.toLowerCase();
		if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg")
				|| lower.endsWith(".png") || lower.endsWith(".webp"))) {
			throw new IllegalArgumentException("배너는 jpg, jpeg, png, webp 만 올릴 수 있습니다.");
		}

		String datePath = LocalDate.now().toString();
		Path dirPath = Paths.get(uploadPath).toAbsolutePath().normalize()
				.resolve("company").resolve("about").resolve(datePath);

		File dir = dirPath.toFile();
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("배너 업로드 폴더를 만들지 못했습니다. path=" + dir.getAbsolutePath());
		}

		String ext = originalName.substring(originalName.lastIndexOf("."));
		String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
		File dest = dirPath.resolve(savedName).toFile();

		try {
			file.transferTo(dest);
		} catch (IOException e) {
			throw new IllegalStateException("배너를 저장하지 못했습니다. path=" + dest.getAbsolutePath(), e);
		}

		return UPLOAD_URL_PREFIX + "/" + datePath + "/" + savedName;
	}
}
