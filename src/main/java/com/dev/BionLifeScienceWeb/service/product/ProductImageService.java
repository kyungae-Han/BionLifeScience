package com.dev.BionLifeScienceWeb.service.product;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.product.ProductImage;
import com.dev.BionLifeScienceWeb.repository.product.ProductImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageService {

	private final ProductImageRepository productImageRepository;
	
	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public Boolean fileDelete(
			Long id
			) {
		List<ProductImage> files = productImageRepository.findAllByProductId(id);
		for(ProductImage p : files) {
			File pFile = new File(p.getProductImagePath());
			if(!pFile.delete()) {
				return false;
			}
		}
		
		return true;
	}
	
	public String fileUpload(
			List<MultipartFile> productImages,
			Long id,
			String productCode
			) throws IllegalStateException, IOException {
		

		String absolutePath = new File("").getAbsolutePath() + "\\";
        String path = commonPath + "/company/" + productCode + "/slide";
        String road = "/administration/company/" + productCode + "/slide";
        File fileFolder = new File(path);
        if(fileFolder.exists() && fileFolder.isDirectory()) {
        	FileUtils.cleanDirectory(fileFolder);
        }
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
        		ProductImage f = new ProductImage();
        		f.setProductId(id);
            	String contentType = file.getContentType();
                
            	if (ObjectUtils.isEmpty(contentType)){
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
                productImageRepository.save(f);
            }
        }
        
        return "success";
	}
	
}
