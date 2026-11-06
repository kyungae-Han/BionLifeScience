package com.dev.BionLifeScienceWeb.service.brand;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductImage;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandProductImageService {

	private final BrandProductImageRepository brandProductImageRepository;
	private final BrandProductRepository brandProductRepository; 
	
	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public String fileUpload(
			List<MultipartFile> productImages,
			Long id,
			String productCode
			) throws IllegalStateException, IOException {
		
        String absolutePath = new File("").getAbsolutePath() + "\\";
        String path = commonPath + "/brandproduct/" + productCode + "/slide";
        String road = "/upload/brandproduct/" + productCode + "/slide";
        // 올린 썸네일은 기존 것 뒤에 붙는다. 폴더를 비우지 않는다.
        // 파일 쪽(BrandProductFileService)과 같은 이유다. 거기 주석을 참고한다.
        File fileFolder = new File(path);
        int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
        if(!fileFolder.exists()) {
        	fileFolder.mkdirs();
        }
        
        for(MultipartFile file : productImages) {
        	
        	if(!file.isEmpty()) {
        		String generatedString = random.ints(leftLimit,rightLimit + 1)
        				  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        				  .limit(targetStringLength)
        				  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        				  .toString();
        		BrandProductImage f = new BrandProductImage();
        		
        		BrandProduct product = brandProductRepository.findById(id)
        		        .orElseThrow(() -> new RuntimeException("상품 없음"));
        		
        		 f.setProduct(product);
        		 
        		 
            	String contentType = file.getContentType();
                // 확장자 명 NULL 검증
        		if (ObjectUtils.isEmpty(contentType)) {
        			return "NONE";
        		} 
                String new_file_name =generatedString +  "_" + file.getOriginalFilename();
                if(env.equals("local")) {
                	 fileFolder = new File(absolutePath + path + "/" + new_file_name);
                	 f.setProductImagePath(absolutePath + path + "/" + new_file_name);
				}else if(env.equals("prod")) {
					fileFolder = new File(path + "/" + new_file_name);
					f.setProductImagePath(path + "/" + new_file_name);
				}
                
                file.transferTo(fileFolder);
                f.setProductImageRoad(road + "/" + new_file_name );
                f.setProductImageName(file.getOriginalFilename());
                brandProductImageRepository.save(f);
            }
        }

        return "success";
	}

	/**
	 * 썸네일 한 장을 지운다. DB 행과 디스크 파일을 함께 지운다.
	 * 화면의 × 가 이것을 부른다. 수정 저장과 무관하게 바로 지워진다.
	 * 디스크 파일을 못 지워도 DB 행은 지운다 (BrandProductFileService 의 같은 메서드 주석 참고).
	 *
	 * @return 지웠으면 true, 그 번호의 행이 없으면 false
	 */
	@Transactional
	public boolean deleteOne(Long id) {
		BrandProductImage image = brandProductImageRepository.findById(id).orElse(null);
		if (image == null) {
			return false;
		}
		deleteFromDisk(image.getProductImagePath());
		brandProductImageRepository.delete(image);
		return true;
	}

	private void deleteFromDisk(String storedPath) {
		if (storedPath == null || storedPath.isBlank()) {
			return;
		}
		try {
			File target = new File(storedPath);
			if (target.exists() && target.isFile()) {
				target.delete();
			}
		} catch (Exception e) {
			// 디스크에서 못 지워도 DB 행은 지운다
		}
	}
}
