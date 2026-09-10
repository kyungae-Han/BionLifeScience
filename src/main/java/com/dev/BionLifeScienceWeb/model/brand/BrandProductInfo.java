package com.dev.BionLifeScienceWeb.model.brand;

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
@Table(name="brand_product_info")
@Data
public class BrandProductInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="BRAND_PRODUCT_INFO_ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="BRAND_PRODUCT_ID", nullable = false)
    private BrandProduct product;
	
	@Column(name="BRAND_PRODUCT_INFO_TEXT")
	private String productInfoText;
	
	/** 영문 제품 정보. 비우면 화면에서 한글이 나간다 */
	@Column(name="BRAND_PRODUCT_INFO_TEXT_EN", length = 1000)
	private String productInfoTextEn;
	
}
