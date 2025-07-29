package com.dev.BionLifeScienceWeb.model.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="product_spec")
@Data
public class ProductSpec {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="PRODUCT_SPEC_ID")
	private Long id;
	
	@Column(name="PRODUCT_SPEC_SUBJECT")
	private String productSpecSubject;
	
	@Column(name="PRODUCT_ID")
	private Long productId;
	
	@Column(name="PRODUCT_SPEC_CONTENT")
	private String productSpecContent;
}
