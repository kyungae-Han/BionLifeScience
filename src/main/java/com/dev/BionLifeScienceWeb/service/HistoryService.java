package com.dev.BionLifeScienceWeb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.repository.HistoryContentRepository;
import com.dev.BionLifeScienceWeb.repository.HistorySubjectRepository;

@Service
public class HistoryService {
	
	@Autowired
	HistoryContentRepository historyContentRepository;
	
	@Autowired
	HistorySubjectRepository historySubjectRepository;
}
