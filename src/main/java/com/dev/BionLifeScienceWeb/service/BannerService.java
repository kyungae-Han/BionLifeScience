package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Banner;
import com.dev.BionLifeScienceWeb.repository.BannerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerService {

	private final BannerRepository bannerRepository;

	@Value("${spring.upload.env}")
	private String env;

	@Value("${spring.upload.path}")
	private String commonPath;

	public String bannerInsert(List<MultipartFile> files, Banner banner) throws IllegalStateException, IOException {
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    String current_date = simpleDateFormat.format(new Date());

	    // 실제 저장 경로 및 URL 경로
	    String path = commonPath + "/banner/" + current_date;   // 상대 or 절대
	    String road = "/upload/banner/" + current_date; // 접근 URL

	    // env-분기
	    String absolutePath = "";
	    if ("local".equals(env)) {
	        absolutePath = new File("").getAbsolutePath() + "/";
	    }

	    // 폴더 생성
	    File dir;
	    if ("local".equals(env)) {
	        dir = new File(absolutePath + path);
	    } else {
	        dir = new File(path);
	    }
	    if (!dir.exists()) dir.mkdirs();

	    int leftLimit = 48, rightLimit = 122, targetStringLength = 10;
	    Random random = new Random();
	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	            .limit(targetStringLength)
	            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	            .toString();

	    String webFileName = null, mobileFileName = null;
	    String webSavedName = null, mobileSavedName = null;

	    if (files.size() == 1) {
	        MultipartFile file = files.get(0);
	        webFileName = file.getOriginalFilename();
	        webSavedName = generatedString + "_web_" + webFileName;

	        String fileSavePath = ("local".equals(env) ? absolutePath + path : path) + "/" + webSavedName;

	        banner.setWebname(webFileName);
	        banner.setWebpath(fileSavePath);
	        banner.setWebroad(road + "/" + webSavedName);

	        banner.setMobilename(webFileName);
	        banner.setMobilepath(fileSavePath);
	        banner.setMobileroad(road + "/" + webSavedName);

	        // 실제 저장
	        file.transferTo(new File(fileSavePath));
	    } else if (files.size() == 2) {
	        MultipartFile webFile = files.get(0);
	        MultipartFile mobileFile = files.get(1);

	        webFileName = webFile.getOriginalFilename();
	        webSavedName = generatedString + "_web_" + webFileName;
	        mobileFileName = mobileFile.getOriginalFilename();
	        mobileSavedName = generatedString + "_mobile_" + mobileFileName;

	        String webSavePath = ("local".equals(env) ? absolutePath + path : path) + "/" + webSavedName;
	        String mobileSavePath = ("local".equals(env) ? absolutePath + path : path) + "/" + mobileSavedName;

	        // 웹파일
	        banner.setWebname(webSavedName);
	        banner.setWebpath(webSavePath);
	        banner.setWebroad(road + "/" + webSavedName);

	        // 모바일파일
	        banner.setMobilename(mobileSavedName);
	        banner.setMobilepath(mobileSavePath);
	        banner.setMobileroad(road + "/" + mobileSavedName);

	        // 실제 저장
	        webFile.transferTo(new File(webSavePath));
	        mobileFile.transferTo(new File(mobileSavePath));
	    }
	    // 정렬순서를 비우고 등록하면 맨 뒤 번호를 자동으로 붙인다.
	    //
	    // 폼의 숫자 칸을 비우면 "" 가 넘어오고, 스프링이 Integer 로 바꾸면서 null 이 된다.
	    // 그 null 이 엔티티의 기본값 0 을 덮어써서 NOT NULL 위반으로 등록이 통째로 실패했다.
	    // 2026-11-06 에 로컬에서 배너를 올리다 발견했다. 순서를 안 적는 것이 잘못은 아니므로
	    // 오류를 내는 대신 번호를 채워 준다.
	    if (banner.getBannerIndex() == null) {
	        banner.setBannerIndex(nextBannerIndex());
	    }
	    bannerRepository.save(banner);
	    return "success";
	}

	/** 비어 있을 때 쓸 다음 정렬순서. 가장 큰 값 + 1, 하나도 없으면 1 */
	private int nextBannerIndex() {
		return bannerRepository.findAll().stream()
				.map(Banner::getBannerIndex)
				.filter(java.util.Objects::nonNull)
				.max(Integer::compareTo)
				.orElse(0) + 1;
	}
	
	
	public String bannerUpdate(Long id, MultipartFile webFile, MultipartFile mobileFile, Banner banner) throws IOException {
	    Banner existing = bannerRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));

	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    String current_date = sdf.format(new Date());
	    String path = commonPath + "/banner/" + current_date;
	    String road = "/upload/banner/" + current_date;

	    String absolutePath = "";
	    if ("local".equals(env)) absolutePath = new File("").getAbsolutePath() + "/";

	    File dir = ("local".equals(env)) ? new File(absolutePath + path) : new File(path);
	    if (!dir.exists()) dir.mkdirs();

	    Random random = new Random();
	    String randomStr = random.ints(48, 122)
	            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	            .limit(10)
	            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	            .toString();

	    // WEB
	    if (webFile != null && !webFile.isEmpty()) {
	        String fileName = webFile.getOriginalFilename();
	        String savedName = randomStr + "_web_" + fileName;
	        String savePath = ("local".equals(env) ? absolutePath + path : path) + "/" + savedName;

	        webFile.transferTo(new File(savePath));
	        existing.setWebname(fileName);
	        existing.setWebpath(savePath);
	        existing.setWebroad(road + "/" + savedName);
	    }

	    // MOBILE
	    if (mobileFile != null && !mobileFile.isEmpty()) {
	        String fileName = mobileFile.getOriginalFilename();
	        String savedName = randomStr + "_mobile_" + fileName;
	        String savePath = ("local".equals(env) ? absolutePath + path : path) + "/" + savedName;

	        mobileFile.transferTo(new File(savePath));
	        existing.setMobilename(fileName);
	        existing.setMobilepath(savePath);
	        existing.setMobileroad(road + "/" + savedName);
	    }

	    // 텍스트 수정
	    existing.setSubject(banner.getSubject());
	    existing.setContent(banner.getContent());
	    existing.setSubjectEn(banner.getSubjectEn());
	    existing.setContentEn(banner.getContentEn());
	    existing.setLinkUrl(banner.getLinkUrl());

	    // 수정 화면에서 정렬순서를 비우면 원래 번호를 그대로 둔다.
	    // 여기서 null 을 넣으면 등록 때와 같은 이유로 저장이 깨진다.
	    if (banner.getBannerIndex() != null) {
	        existing.setBannerIndex(banner.getBannerIndex());
	    }

	    bannerRepository.save(existing);
	    return "updated";
	}


}
