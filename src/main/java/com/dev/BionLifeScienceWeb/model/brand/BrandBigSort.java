package com.dev.BionLifeScienceWeb.model.brand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name="brand_big_sort")
public class BrandBigSort {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BRAND_BIGSORT_ID")
	private Long id;
	
	@Column(name="BRAND_BIGSORT_NAME")
	private String name;
	
	@Transient
	private Long brandId;
	
	@Column(name="BRAND_BIGSORT_INDEX")
	private int brandBigSortIndex;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(
			name="BRAND_BIGSORT_REFER_ID", referencedColumnName="BRAND_ID"
			)
	private Brand brand;

}
