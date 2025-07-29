package com.dev.BionLifeScienceWeb.model.brand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="brand_product_spec")
@Data
public class BrandProductSpec {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="BRAND_PRODUCT_SPEC_ID")
	private Long id;
	
	@Column(name="BRAND_PRODUCT_SPEC_SUBJECT")
	private String productSpecSubject;
	
	@Column(name="BRAND_PRODUCT_ID")
	private Long productId;
	
	@Column(name="BRAND_PRODUCT_SPEC_CONTENT")
	private String productSpecContent;
}
