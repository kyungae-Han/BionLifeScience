package com.dev.BionLifeScienceWeb.service.brand;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	private static final String DATE_FMT = "yyyy-MM-dd";
	private String today() {
	    return new SimpleDateFormat(DATE_FMT).format(new Date());
	}
	
	public String uploadEditorImage(MultipartFile file) throws IOException {
        
		String today = today();
        String dirPath = commonPath + "/brand/editor/" + today;
        String webPath = "/administration/brand/editor/" + today;

        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        return webPath + "/" + fileName;
    }
	
	public void applyLogo(Brand brand, MultipartFile brandImage) throws IOException {
		if (brandImage == null || brandImage.isEmpty()) return;
		
		FileMeta m = storeFile("brand", brandImage);
		brand.setImagePath(m.path);
        brand.setImageRoad(m.road);
        brand.setImageName(m.name);
		
	}
	
	public void applyVisual(Brand brand, MultipartFile visualImage) throws IOException {
	    if (visualImage == null || visualImage.isEmpty()) return;
	    
	    
	    System.out.println("업데이트 visualPath = " + brand.getVisualPath());
	    
	    FileMeta m = storeFileVisual(visualImage);
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
        String today = today();
        String dirPath = commonPath + "/" + subdir + "/" + today;       // 물리
        String webRoad = "/upload/" + subdir + "/" + today;     // 웹

        String fileName = randomKey(10) + file.getOriginalFilename();

        java.io.File dir = env.equals("local")
                ? new java.io.File(new java.io.File("").getAbsolutePath() + java.io.File.separator + dirPath)
                : new java.io.File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        java.io.File dest = new java.io.File(dir, fileName);
        file.transferTo(dest);

        return new FileMeta(dirPath + "/" + fileName, webRoad + "/" + fileName, fileName);
    }
    
    
    private FileMeta storeFileVisual(MultipartFile file) throws IOException {
        String today = today();
        String dirPath = commonPath + "/brand/" + today + "/visual";   // 물리
        String webRoad = "/upload/brand/" + today + "/visual"; // 웹

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
            MultipartFile brandImage,
            MultipartFile visualImage,
            Brand brand
    ) throws IOException {

        // brandIndex 세팅 (필요 없으면 제거 가능)
        int index = 1;
        if (brandRepository.findFirstIndex().isPresent()) {
            index = brandRepository.findFirstIndex().get() + 1;
        }
        brand.setBrandIndex(index);

        // 로고 파일 처리
        applyLogo(brand, brandImage);

        // 비주얼 파일 처리
        applyVisual(brand, visualImage);

        // 최종 저장
        brandRepository.save(brand);
    }
	
    
	public void brandUpdate(
			  MultipartFile brandImage,
	          MultipartFile visualImage,
	          Brand brand
			) throws IllegalStateException, IOException {
		
		
		brandRepository.findById(brand.getId()).ifPresent(b -> {
	        b.setName(brand.getName());
	        b.setContent(brand.getContent());
	        b.setDesc(brand.getDesc());
	        b.setType(brand.getType());

	        try {
	            // 로고 파일 있으면 교체
	            if (brandImage != null && !brandImage.isEmpty()) {
	                applyLogo(b, brandImage);
	            }

	            // 비주얼 파일 있으면 교체
	            if (visualImage != null && !visualImage.isEmpty()) {
	                applyVisual(b, visualImage);
	            }
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }

	        brandRepository.save(b);
	    });
		
	}
}



















