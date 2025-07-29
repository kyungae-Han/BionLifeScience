package com.dev.BionLifeScienceWeb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.model.Event;
import com.dev.BionLifeScienceWeb.repository.EventRepository;

@Service
public class EventService {

	@Autowired
	EventRepository eventRepository;
	
	public void insertEvent(Event event) {
		if (eventRepository.findById(1L).isPresent()) {

			eventRepository.findById(1L).ifPresent(s -> {
				s.setSubject(event.getSubject());
				s.setContent(event.getContent());
				s.setLink(event.getLink());

				eventRepository.save(s);
			});
		} else {
			Event s = new Event();
			s.setSubject(event.getSubject());
			s.setContent(event.getContent());
			s.setLink(event.getLink());

			eventRepository.save(s);
		}
	}
}
