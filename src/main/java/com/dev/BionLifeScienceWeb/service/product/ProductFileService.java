package com.dev.BionLifeScienceWeb.service.product;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.product.ProductFile;
import com.dev.BionLifeScienceWeb.repository.product.ProductFileRepository;

@Service
public class ProductFileService {

	@Autowired
	ProductFileRepository productFileRepository;
	
	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public Boolean fileDelete(
			Long id
			) {
		List<ProductFile> files = productFileRepository.findAllByProductId(id);
		for(ProductFile p : files) {
			File pFile = new File(p.getProductFilePath());
			if(!pFile.delete()) {
				return false;
			}
		}
		
		return true;
	}
	
	public String fileUpload(
			List<MultipartFile> productFiles,
			Long id,
			String productCode
			) throws IllegalStateException, IOException {
		
        String absolutePath = new File("").getAbsolutePath() + "\\";

        String path = commonPath + "/company/" + productCode + "/files";
        String road = "/administration/company/" + productCode + "/files";
        
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
        
        for(MultipartFile file : productFiles) {
        	if(!file.isEmpty()) {
        		String generatedString = random.ints(leftLimit,rightLimit + 1)
      				  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
      				  .limit(targetStringLength)
      				  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      				  .toString();
        		ProductFile f = new ProductFile();
        		f.setProductId(id);
            	String contentType = file.getContentType();
                String originalFileExtension = "";
                    // 확장자 명이 없으면 이 파일은 잘 못 된 것이다
                if (ObjectUtils.isEmpty(contentType)){
                    return "NONE";
                }else{
                    if(contentType.contains("image/jpeg")){
                        originalFileExtension = ".jpg";
                    }
                    else if(contentType.contains("image/png")){
                        originalFileExtension = ".png";
                    }
                    else if(contentType.contains("image/gif")){
                        originalFileExtension = ".gif";
                    }
                    else if(contentType.contains("application/pdf")) {
                    	originalFileExtension = ".pdf";
                    }
                    else if(contentType.contains("application/x-zip-compressed")) {
                    	originalFileExtension = ".zip";
                    }
                    else if(contentType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    	originalFileExtension = ".xlsx";
                    }
                    else if(contentType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    	originalFileExtension = ".docx";
                    }
                    else if(contentType.contains("text/plain")) {
                    	originalFileExtension = ".txt";
                    }
                    else if(contentType.contains("image/x-icon")) {
                    	originalFileExtension = ".ico";
                    }
                    else if(contentType.contains("application/haansofthwp")) {
                    	originalFileExtension = ".hwp";
                    }
                }
                String new_file_name = generatedString +  "_" + file.getOriginalFilename();
                if(env.equals("local")) {
                	fileFolder = new File(absolutePath + path + "/" + new_file_name);
                	f.setProductFilePath(absolutePath + path + "/" + new_file_name);
				}else if(env.equals("prod")) {
					fileFolder = new File(path + "/" + new_file_name);
					f.setProductFilePath(path + "/" + new_file_name);
				}
                
                file.transferTo(fileFolder);
                f.setProductFileRoad(road + "/" + new_file_name );
                f.setProductFileName(file.getOriginalFilename());
                f.setProductFileDate(new Date());
                productFileRepository.save(f);
            }
        }
        
        return "success";
	}
}
