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
	    String road = "/administration/banner/" + current_date; // 접근 URL

	    // env 분기
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
	        banner.setWebname(webFileName);
	        banner.setWebpath(webSavePath);
	        banner.setWebroad(road + "/" + webSavedName);

	        // 모바일파일
	        banner.setMobilename(mobileFileName);
	        banner.setMobilepath(mobileSavePath);
	        banner.setMobileroad(road + "/" + mobileSavedName);

	        // 실제 저장
	        webFile.transferTo(new File(webSavePath));
	        mobileFile.transferTo(new File(mobileSavePath));
	    }
	    bannerRepository.save(banner);
	    return "success";
	}

}
