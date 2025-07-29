package com.dev.BionLifeScienceWeb.model;

import java.io.Serializable;

import org.springframework.lang.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name="member")
@ToString
public class Member implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="MEMBER_ID")
	private Long id;
	
	@Column(name="MEMBER_USERNAME")
	private String username;
	
	@Column(name="MEMBER_PASSWORD")
	private String password;
	
	@Column(name="MEMBER_NAME")
	private String name;
	
	@Column(name="MEMBER_EMAIL")
	@Nullable
	private String email;
	
	@Column(name="MEMBER_PHONE")
	private String phone;
	
	@Column(name="MEMBER_ENABLED")
	private Boolean enabled;
	
	@Column(name="MEMBER_ROLE")
	private String role;
	
}























