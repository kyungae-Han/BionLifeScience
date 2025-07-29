package com.dev.BionLifeScienceWeb.model.brand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="brand")
public class Brand {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BRAND_ID")
	private Long id;
	
	@Column(name="BRAND_NAME")
	private String name;
	
	@Column(name="BRAND_CONTENT")
	private String content;
	
	@Column(name="BRAND_IMAGE_PATH")
	private String imagePath;
	
	@Column(name="BRAND_IMAGE_ROAD")
	private String imageRoad;
	
	@Column(name="BRAND_IMAGE_NAME")
	private String imageName;
	
	@Column(name="BRAND_INDEX")
	private int brandIndex;

}
