package com.dev.BionLifeScienceWeb.service.brand;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;

@Service
public class BrandService {

	@Autowired
	BrandRepository brandRepository;
	
	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public void brandInsert(
			MultipartFile image,
			Brand brand
			) throws IllegalStateException, IOException {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String current_date = simpleDateFormat.format(new Date());
		String absolutePath = new File("").getAbsolutePath() + "\\";
        String brandPath = commonPath + "/brand/" + current_date;
        String brandRoad = "/administration/brand/"+current_date;
        File brandFileFolder = new File(brandPath);
        
        int index = 1;
        if(brandRepository.findFirstIndex().isPresent()) {
        	index = brandRepository.findFirstIndex().get() + 1;
        }
        brand.setBrandIndex(index);
        int leftLimit = 48; 
 		int rightLimit = 122;
 		int targetStringLength = 10;
 		Random random = new Random();
 		
 		String generatedString = random.ints(leftLimit,rightLimit + 1)
			  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
			  .limit(targetStringLength)
			  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			  .toString();
 		
 		if(!brandFileFolder.exists()) {
 			brandFileFolder.mkdirs();
 		}
 		
 		String brandFileName = generatedString + image.getOriginalFilename();
 		
 		if(env.equals("local")) {
 			brandFileFolder = new File(absolutePath + brandPath + "/" + brandFileName);
		}else if(env.equals("prod")) {
			brandFileFolder = new File(brandPath + "/" + brandFileName);
		}
 		image.transferTo(brandFileFolder);
 		brand.setImagePath(brandPath + "/" + brandFileName);
 		brand.setImageRoad(brandRoad + "/" + brandFileName);
 		brand.setImageName(brandFileName);
 		
 		brandRepository.save(brand);
 		
	}
	
	public void brandUpdate(
			MultipartFile image,
			Brand brand
			) throws IllegalStateException, IOException {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String current_date = simpleDateFormat.format(new Date());
		String absolutePath = new File("").getAbsolutePath() + "\\";
      
        String brandPath = commonPath + "/brand/" + current_date;
        String brandRoad = "/administration/brand/"+current_date;
        File brandFileFolder = new File(brandPath);
        
        int leftLimit = 48; // numeral '0'
 		int rightLimit = 122; // letter 'z'
 		int targetStringLength = 10;
 		Random random = new Random();
 		
 		if(image!=null && !image.isEmpty()) {
 			String generatedString = random.ints(leftLimit,rightLimit + 1)
				  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				  .limit(targetStringLength)
				  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				  .toString();
	 		
	 		if(!brandFileFolder.exists()) {
	 			brandFileFolder.mkdirs();
	 		}
	 		
	 		String brandFileName = generatedString + image.getOriginalFilename();
	 		
	 		if(env.equals("local")) {
	 			brandFileFolder = new File(absolutePath + brandPath + "/" + brandFileName);
			}else if(env.equals("prod")) {
				brandFileFolder = new File(brandPath + "/" + brandFileName);
			}
	 		image.transferTo(brandFileFolder);
	 		brandRepository.findById(brand.getId()).ifPresent(b->{
	 			b.setImagePath(brandPath + "/" + brandFileName);
		 		b.setImageRoad(brandRoad + "/" + brandFileName);
		 		b.setImageName(brandFileName);
		 		brandRepository.save(b);
	 		});
	 		
 		}
 		brandRepository.findById(brand.getId()).ifPresent(b->{
 			b.setName(brand.getName());
	 		b.setContent(brand.getContent());
	 		brandRepository.save(b);
 		});
 		
 		
	}
}



















