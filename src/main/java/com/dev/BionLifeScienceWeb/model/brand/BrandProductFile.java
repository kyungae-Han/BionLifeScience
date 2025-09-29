package com.dev.BionLifeScienceWeb.model.brand;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="brand_product_file")
@Data
public class BrandProductFile {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="BRAND_PRODUCT_FILE_ID")
	private Long id;
	
	@Column(name="BRAND_PRODUCT_FILE_NAME")
	private String productFileName;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="BRAND_PRODUCT_ID", nullable = false)
    private BrandProduct product;
	
	@Column(name="BRAND_PRODUCT_FILE_PATH")
	private String productFilePath;
	
	@Column(name="BRAND_PRODUCT_FILE_ROAD")
	private String productFileRoad;
	
	@Column(name="BRAND_PRODUCT_FILE_DATE")
	private Date productFileDate;

}
