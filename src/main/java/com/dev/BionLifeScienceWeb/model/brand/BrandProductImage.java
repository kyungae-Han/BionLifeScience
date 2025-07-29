package com.dev.BionLifeScienceWeb.model.brand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="brand_product_image")
public class BrandProductImage {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BRAND_PRODUCT_IMAGE_ID")
	private Long id;
	
	@Column(name="BRAND_PRODUCT_ID")
	private Long productId;
	
	@Column(name="BRAND_PRODUCT_IMAGE_PATH")
	private String productImagePath;
	
	@Column(name="BRAND_PRODUCT_IMAGE_NAME")
	private String productImageName;
	
	@Column(name="BRAND_PRODUCT_IMAGE_ROAD")
	private String productImageRoad;
}
