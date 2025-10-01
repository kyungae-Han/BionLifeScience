package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.ReferenceFile;
import com.dev.BionLifeScienceWeb.repository.ReferenceFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReferenceFileService {

	private final ReferenceFileRepository referenceFileRepository;
	
	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public String referenceFileInsert(
			MultipartFile file,
			ReferenceFile f
			) throws IllegalStateException, IOException {
		
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    String currentDate = sdf.format(new Date());
		    
		    String absolutePath = new File("").getAbsolutePath() + File.separator;

		    String path = absolutePath + commonPath + "/reference/" + currentDate;
		    String road = "/upload/reference/" + currentDate;

		    File fileFolder = new File(path);
		    if (!fileFolder.exists()) {
		        fileFolder.mkdirs();
		    }
		    
		    String originName = file.getOriginalFilename();

		    // 확장자 추출
		    String ext = "";
		    if (originName != null && originName.contains(".")) {
		        ext = originName.substring(originName.lastIndexOf(".")).toLowerCase();
		    }

		    // 랜덤 문자열 + 확장자
		    String newFileName = generateRandomString(10) + ext;
		    
		    File dest = new File(fileFolder, newFileName);
		    file.transferTo(dest);

		    // DB 세팅
		    if (f.getFilesubject() == null || f.getFilesubject().isEmpty()) {
		        f.setFilesubject(originName);
		    }
		    f.setFiledate(new Date());
		    f.setFilename(originName);
		    f.setFileextension(originName.substring(originName.lastIndexOf(".")).toLowerCase());
		    f.setFilepath(dest.getAbsolutePath());  // 서버 절대 경로
		    f.setFileroad("/upload/reference/" + currentDate + "/" + newFileName); // 웹 접근 경로

		    referenceFileRepository.save(f);

		    return "success";
		
	}
	
	
	private String generateRandomString(int length) {
	    int leftLimit = 48; // '0'
	    int rightLimit = 122; // 'z'
	    Random random = new Random();

	    return random.ints(leftLimit, rightLimit + 1)
	            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	            .limit(length)
	            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	            .toString();
	}
}













