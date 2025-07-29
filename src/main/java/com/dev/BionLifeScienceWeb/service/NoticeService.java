package com.dev.BionLifeScienceWeb.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.model.Notice;
import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import com.dev.BionLifeScienceWeb.repository.NoticeSubjectRepository;

@Service
public class NoticeService {
	
	@Autowired
	NoticeRepository noticeRepository;
	
	@Autowired
	NoticeSubjectRepository noticeSubjectRepository;
	
	public String noticeUpdate(Notice notice) {
		
		Optional<Notice> n = noticeRepository.findById(notice.getId());
		n.ifPresent(no->{
			no.setContent(notice.getContent());
			no.setSign(notice.getSign());
			no.setSubject(notice.getSubject());
			no.setNoticeSubject(noticeSubjectRepository.findById(notice.getSubjectId()).get());
			
			noticeRepository.save(no);
		});
		
		return "success";
	}
}
