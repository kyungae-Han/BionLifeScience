package com.dev.BionLifeScienceWeb.service.brand;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductFile;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandProductFileService {

	private final BrandProductFileRepository brandProductFileRepository;
	private final BrandProductRepository brandProductRepository; 
	
	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public String fileUpload(
			List<MultipartFile> productFiles,
			Long id,
			String productCode
			) throws IllegalStateException, IOException {
		
		String absolutePath = new File("").getAbsolutePath() + "\\";
        String path = commonPath +  "/brandproduct/" + productCode + "/files";
        String road = "/upload/brandproduct/" + productCode + "/files";
        
        // 올린 파일은 기존 파일 뒤에 붙는다. 폴더를 비우지 않는다.
        // 예전에는 여기서 FileUtils.cleanDirectory 로 폴더를 통째로 비워서,
        // 새 파일을 하나 올리면 먼저 있던 것이 전부 사라졌다. 그래서 파일을
        // 여러 개 모아 둘 수가 없었고, 한 개만 떼어내는 길도 없었다.
        // 지금은 떼어낼 파일을 화면의 × 로 한 건씩 지운다 (deleteOne).
        // 이름 앞에 10자리 무작위 문자를 붙이므로 같은 이름을 올려도 덮이지 않는다.
        File fileFolder = new File(path);

        int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
		
        if(!fileFolder.exists()) {
        	fileFolder.mkdirs();
        }
        
        for(MultipartFile file : productFiles) {
        	if(!file.isEmpty()) {
        		String generatedString = random.ints(leftLimit,rightLimit + 1)
      				  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
      				  .limit(targetStringLength)
      				  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      				  .toString();
        		BrandProductFile f = new BrandProductFile();
        		
        		BrandProduct product = brandProductRepository.findById(id)
        		        .orElseThrow(() -> new RuntimeException("상품 없음"));
        		
        		 f.setProduct(product);
        		 
            	String contentType = file.getContentType();
            	// 확장자 명 NULL 검증
        		if (ObjectUtils.isEmpty(contentType)) {
        			return "NONE";
        		} 
        		
        		//String encodedFileName = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        		 String new_file_name = generatedString +  "_" + file.getOriginalFilename();
        		 
        		 
                if(env.equals("local")) {
                	fileFolder = new File(absolutePath + path + "/" + new_file_name.replaceAll("\\s+", ""));
                	f.setProductFilePath(absolutePath + path + "/" + new_file_name.replaceAll("\\s+", ""));
				}else if(env.equals("prod")) {
				    fileFolder = new File(new File(commonPath).getAbsolutePath() + "/brandproduct/" + productCode + "/files/" + new_file_name.replaceAll("\\s+", ""));
				    f.setProductFilePath(new File(commonPath).getAbsolutePath() + "/brandproduct/" + productCode + "/files/" + new_file_name.replaceAll("\\s+", ""));
				}
                
                file.transferTo(fileFolder);
                f.setProductFileRoad(road + "/" + new_file_name.replaceAll("\\s+", ""));
                f.setProductFileName(file.getOriginalFilename());
                f.setProductFileDate(new Date());
                brandProductFileRepository.save(f);
            }
        }

        return "success";
	}

	/**
	 * 파일 한 건을 지운다. DB 행과 디스크 파일을 함께 지운다.
	 * 화면의 × 가 이것을 부른다. 수정 저장과 무관하게 바로 지워진다.
	 *
	 * 디스크 파일을 못 지워도 DB 행은 지운다. 로컬에서 만든 오래된 행은
	 * 저장된 경로가 운영과 달라 파일을 찾지 못하는데, 그때 행까지 남겨 두면
	 * 화면에는 계속 보이면서 내려받을 수는 없는 줄이 된다.
	 *
	 * @return 지웠으면 true, 그 번호의 행이 없으면 false
	 */
	@Transactional
	public boolean deleteOne(Long id) {
		BrandProductFile file = brandProductFileRepository.findById(id).orElse(null);
		if (file == null) {
			return false;
		}
		deleteFromDisk(file.getProductFilePath());
		brandProductFileRepository.delete(file);
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
			// 위 주석 참고. 디스크에서 못 지워도 DB 행은 지운다
		}
	}
}
