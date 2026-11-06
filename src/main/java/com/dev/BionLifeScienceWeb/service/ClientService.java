package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Client;
import com.dev.BionLifeScienceWeb.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

	private final ClientRepository clientRepository;
	
	@Value("${spring.upload.env}")
	private String env;
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	public String clientInsert(
			Client client,
			MultipartFile file) throws IllegalStateException, IOException {

		// 첨부를 고르지 않아도 브라우저는 이름이 빈 파일 파트를 함께 보낸다.
		// 예전에는 이것을 걸러내지 않아서, 첨부 없는 문의마다 0바이트 파일이 하나씩 쌓이고
		// DB 의 파일 칸에도 그 경로가 박혔다. 관리자 상세에는 빈 첨부 링크가 떴다.
		boolean hasFile = file != null && !file.isEmpty()
				&& file.getOriginalFilename() != null
				&& !file.getOriginalFilename().isBlank();

		// 저장이 깨졌을 때 치우려고 방금 쓴 파일을 들고 있는다
		File written = null;

		if (hasFile) {

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        String current_date = simpleDateFormat.format(new Date());

	        String absolutePath = new File("").getAbsolutePath() + "\\";
	        String path = commonPath + "/clientfiles/" + current_date;
	        String road = "/upload/clientfiles/" + current_date;

	        File fileFolder = new File(path);

	        int leftLimit = 48; // numeral '0'
			int rightLimit = 122; // letter 'z'
			int targetStringLength = 10;
			Random random = new Random();

			String generatedString = random.ints(leftLimit,rightLimit + 1)
			  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
			  .limit(targetStringLength)
			  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			  .toString();

	        if(!fileFolder.exists()) {
	        	fileFolder.mkdirs();
	        }

	    	String contentType = file.getContentType();

	    	// 확장자 명 NULL 검증
			if (ObjectUtils.isEmpty(contentType)) {
				return "NONE";
			}
	        String new_file_name = generatedString +  "_" + file.getOriginalFilename();
	        if(env.equals("local")) {
	        	fileFolder = new File(absolutePath + path + "/" + new_file_name);
	        	client.setFilepath(absolutePath + path + "/" + new_file_name);
			}else if(env.equals("prod")) {
				fileFolder = new File(path + "/" + new_file_name);
				client.setFilepath(path + "/" + new_file_name);
			}
	        file.transferTo(fileFolder);
	        written = fileFolder;
	        client.setFiledate(current_date);
	        client.setFileroad(road + "/" + new_file_name);
	        client.setFilename(file.getOriginalFilename());

		} else {
			// 파일 칸은 NOT NULL 이다. 비워 두면 insert 가 깨지므로 빈 문자열을 넣는다.
			client.setFiledate("");
			client.setFilepath("");
			client.setFilename("");
			client.setFileroad("");
		}

        client.setJoindate(new Date());
        client.setContact(false);

        try {
        	clientRepository.save(client);
        } catch (RuntimeException e) {
        	// 저장이 깨지면 방금 쓴 파일을 치운다. 안 그러면 주인 없는 파일이 디스크에 남는다.
        	// 2026-11-05 에 이모지 때문에 저장이 깨지면서 그렇게 두 개가 남았다.
        	if (written != null && written.exists()) {
        		written.delete();
        	}
        	throw e;
        }

        return "success";
	}
	
	public Page<Client> findByDate(Pageable pageable, String startDate, String endDate) throws ParseException {

		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat bf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if ("".equals(startDate) && "".equals(startDate)) {

			Date today = new Date();
			String day = bf.format(today);

			String start = day.substring(0, 10) + " 00:00:00";
			String end = day.substring(0, 10) + " 23:59:00";

			Date first = bf.parse(start);
			Date second = bf.parse(end);
			return clientRepository.findAllByJoindateBetween(pageable, first, second);

		} else if (!"".equals(startDate) && !"".equals(startDate) && startDate.equals(endDate)) {
			String start = startDate.substring(0, 10) + " 00:00:00";
			Date first = f.parse(start);
			Date second = f.parse(start);

			Calendar c = Calendar.getInstance();
			c.setTime(second);
			c.add(Calendar.DATE, 1);
			second = c.getTime();

			return clientRepository.findAllByJoindateBetween(pageable, first, second);

		} else if ("".equals(startDate) && !"".equals(endDate)) {

			Date second = f.parse(endDate);
			return clientRepository.findAllByJoindateLessThan(pageable, second);

		} else if (!"".equals(startDate) && "".equals(endDate)) {
			Date first = f.parse(startDate);
			return clientRepository.findAllByJoindateGreaterThan(pageable, first);
		} else {
			Date first = f.parse(startDate);
			Date second = f.parse(endDate);

			Calendar c = Calendar.getInstance();
			c.setTime(second);
			c.add(Calendar.DATE, 1);
			second = c.getTime();

			return clientRepository.findAllByJoindateBetween(pageable, first, second);
		}
	}
	
	@Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // 파일 삭제 (물리 경로 기준)
        if (client.getFileroad() != null && !client.getFileroad().isEmpty()) {
            try {
                // 실제 static 디렉토리 경로 계산
                String projectRoot = new File("").getAbsolutePath();
                String realPath = projectRoot + "/src/main/resources/static" + client.getFileroad();
                File file = new File(realPath);
                if (file.exists()) file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        clientRepository.delete(client);
    }
	
}
