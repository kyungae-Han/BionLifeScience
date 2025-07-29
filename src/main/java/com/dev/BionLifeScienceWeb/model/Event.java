package com.dev.BionLifeScienceWeb.model;

import org.springframework.lang.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="event")
public class Event {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="EVENT_ID")
	private Long id;
	
	@Column(name="EVENT_SUBJECT")
	@Nullable
	private String subject;
	
	@Column(name="EVENT_CONTENT")
	@Nullable
	private String content;
	
	@Column(name="EVENT_LINK")
	@Nullable
	private String link;

}
