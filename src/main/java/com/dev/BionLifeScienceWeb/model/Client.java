package com.dev.BionLifeScienceWeb.model;

import java.util.Date;

import org.springframework.lang.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="client")
@Data
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="CLIENT_ID")
	private Long id;
	
	@Column(name="CLIENT_NAME")
	private String name;
	
	@Column(name="CLIENT_COMPANY")
	@Nullable
	private String company;
	
	@Column(name="CLIENT_EMAIL")
	private String email;
	
	@Column(name="CLIENT_PHONE")
	private String phone;
	
	@Column(name="CLIENT_DATE")
	private Date joindate;
	
	@Column(name="CLIENT_COUNTRY")
	@Nullable
	private String country;
	
	@Column(name="CLIENT_TOPIC")
	@Nullable
	private String topic;
	
	@Column(name="CLIENT_SUBJECT")
	private String subject;
	
	@Column(name="CLIENT_CONTACT")
	private Boolean contact;
	
	@Column(name="CLIENT_COMMENT")
	private String comment;
	
	@Column(name="CLIENT_FILE_DATE")
	private String filedate;
	
	@Column(name="CLIENT_FILE_PATH")
	private String filepath;
	
	@Column(name="CLIENT_FILE_NAME")
	private String filename;
	
	@Column(name="CLIENT_FILE_ROAD")
	private String fileroad;
	
		
	
}





















