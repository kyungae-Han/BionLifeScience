package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="company_mail")
@Data
public class CompanyEmail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="COMPANY_MAIL_ID")
	private Long id;
	
	@Column(name="COMPANY_MAIL_ADDRESS")
	private String email;
}
