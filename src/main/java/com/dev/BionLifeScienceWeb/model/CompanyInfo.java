package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="company_info")
@Data
public class CompanyInfo {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="COMPANY_ID")
	private Long id;
	
	@Column(name="COMPANY_NAME")
	private String companyName;
	
	@Column(name="COMPANY_ADDRESS")
	private String companyAddress;
	
	@Column(name="COMPANY_NUMBER")
	private String companyNumber;
	
	@Column(name="COMPANY_TELEPHONE")
	private String companyTelephone;
	
	@Column(name="COMPANY_EMAIL")
	private Boolean companyEmailCheck;
	
}
