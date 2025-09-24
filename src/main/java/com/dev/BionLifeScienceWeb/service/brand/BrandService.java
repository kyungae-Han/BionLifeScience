package com.dev.BionLifeScienceWeb.service.brand;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {

	private final BrandRepository brandRepository;
	
	@Value("${spring.upload.env}")
	private String env;

	@Value("${spring.upload.path}")
	private String commonPath;
	
	
	public String uploadEditorImage(MultipartFile file) throws IOException {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dirPath = commonPath + "/brand/editor/" + today;
        String webPath = "/administration/brand/editor/" + today;

        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        return webPath + "/" + fileName;
    }
	
	public void applyLogo(Brand brand, MultipartFile logo) throws IOException {
		if (logo == null || logo.isEmpty()) return;
		
		FileMeta m = storeFile("brand", logo);
		brand.setImagePath(m.path);
        brand.setImageRoad(m.road);
        brand.setImageName(m.name);
		
	}
	
	 public void applyVisual(Brand brand, MultipartFile visualImage) throws IOException {
        if (visualImage == null || visualImage.isEmpty()) return;

        FileMeta m = storeFile("brand", visualImage);
        brand.setVisualPath(m.path);
        brand.setVisualRoad(m.road);
        brand.setVisualName(m.name);
    }
	 
 	@lombok.AllArgsConstructor
    @lombok.Getter
    private static class FileMeta {
        private String path;  // 물리 경로(DB)
        private String road;  // 웹 경로
        private String name;  // 파일명
    }

    private FileMeta storeFile(String subdir, MultipartFile file) throws IOException {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String dirPath = commonPath + "/" + subdir + "/" + today;       // 물리
        String webRoad = "/administration/" + subdir + "/" + today;     // 웹

        String fileName = randomKey(10) + file.getOriginalFilename();

        java.io.File dir = env.equals("local")
                ? new java.io.File(new java.io.File("").getAbsolutePath() + java.io.File.separator + dirPath)
                : new java.io.File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        java.io.File dest = new java.io.File(dir, fileName);
        file.transferTo(dest);

        return new FileMeta(dirPath + "/" + fileName, webRoad + "/" + fileName, fileName);
    }

    private String randomKey(int n) {
        java.util.Random r = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < n) {
            int c = r.nextInt(75) + 48; // 48~122
            if ((c <= 57 || c >= 65) && (c <= 90 || c >= 97)) sb.append((char) c);
        }
        return sb.toString();
    }
	
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



















