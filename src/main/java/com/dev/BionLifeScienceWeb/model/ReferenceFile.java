package com.dev.BionLifeScienceWeb.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="reference_file")
public class ReferenceFile {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="REFERENCE_FILE_ID")
	private Long id;
	
	@Column(name="REFERENCE_FILE_SUBJECT")
	private String filesubject;
	
	@Column(name="REFERENCE_FILE_DATE")
	private Date filedate;
	
	@Column(name="REFERENCE_FILE_PATH")
	private String filepath;
	
	@Column(name="REFERENCE_FILE_NAME")
	private String filename;
	
	@Column(name="REFERENCE_FILE_ROAD")
	private String fileroad;
	
	@Column(name="REFERENCE_FILE_EXTENSION")
	private String fileextension;
}
