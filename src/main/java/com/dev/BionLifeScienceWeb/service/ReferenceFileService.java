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
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String current_date = simpleDateFormat.format(new Date());
        String absolutePath = new File("").getAbsolutePath() + "\\";
        String path = commonPath + "/reference/" + current_date;
        String road = "/administration/reference/"+current_date;
        File fileFolder = new File(path);
        int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
        if(!fileFolder.exists()) {
        	fileFolder.mkdirs();
        }
        String generatedString = random.ints(leftLimit,rightLimit + 1)
				  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				  .limit(targetStringLength)
				  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				  .toString();
		
    	String contentType = file.getContentType();
        String originalFileExtension = "";
        // 확장자 명 NULL 검증
		if (ObjectUtils.isEmpty(contentType)) {
			return "NONE";
		} 
        String new_file_name = generatedString +  "_" + file.getOriginalFilename();
        if(env.equals("local")) {
        	fileFolder = new File(absolutePath + path + "/" + new_file_name);
        	f.setFilepath(absolutePath + path + "/" + new_file_name);
		}else if(env.equals("prod")) {
			fileFolder = new File(path + "/" + new_file_name);
			f.setFilepath(path + "/" + new_file_name);
		}
        
        file.transferTo(fileFolder);
        if(f.getFilesubject().isEmpty()) {
        	f.setFilesubject(file.getOriginalFilename());
        }
        f.setFiledate(new Date());
        f.setFileextension(originalFileExtension);
        f.setFilename(file.getOriginalFilename());
        f.setFileroad(road + "/" +  new_file_name);
        referenceFileRepository.save(f);
        
        return "success";
		
	}
}













