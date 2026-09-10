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
import lombok.EqualsAndHashCode;

@Entity
@Table(name="brand_product_spec")
@Data
@EqualsAndHashCode(exclude = {"id", "product"}) 
public class BrandProductSpec {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="BRAND_PRODUCT_SPEC_ID")
	private Long id;
	
	@Column(name="BRAND_PRODUCT_SPEC_SUBJECT")
	private String productSpecSubject;
	
	/** 영문 항목명. 비우면 화면에서 한글이 나간다 */
	@Column(name="BRAND_PRODUCT_SPEC_SUBJECT_EN", length = 1000)
	private String productSpecSubjectEn;
	
	@Column(name="BRAND_PRODUCT_SPEC_CONTENT")
	private String productSpecContent;
	
	/** 영문 값. 비우면 화면에서 한글이 나간다 */
	@Column(name="BRAND_PRODUCT_SPEC_CONTENT_EN", length = 1000)
	private String productSpecContentEn;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="BRAND_PRODUCT_ID", nullable = false)
    private BrandProduct product;
	
	
	@Column(name = "SPEC_ORDER")
	private Integer specOrder;
	
}
