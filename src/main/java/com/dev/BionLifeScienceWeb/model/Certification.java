package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="certification")
@Data
public class Certification {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="CERTIFICATION_ID")
	private Long id;

	@Column(name="CERTIFICATION_SUBJECT")
	private String subject;
	
	@Column(name="CERTIFICATION_CONTENT")
	private String content;
	
	@Column(name="CERTIFICATION_SORT")
	private String sort;
	
	@Column(name="CERTIFICATION_IMAGE_ROAD")
	private String road;
	
	@Column(name="CERTIFICATION_IMAGE_PATH")
	private String path;
}
