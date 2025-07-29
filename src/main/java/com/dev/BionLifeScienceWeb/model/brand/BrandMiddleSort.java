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

@Data
@Entity
@Table(name="brand_middle_sort")
public class BrandMiddleSort {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BRAND_MIDDLESORT_ID")
	private Long id;
	
	@Column(name="BRAND_MIDDLESORT_NAME")
	private String name;
	
	@Transient
	private Long brandBigSortId;
	
	@Column(name="BRAND_MIDDLESORT_INDEX")
	private int brandMiddleSortIndex;
		
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(
			name="BRAND_MIDDLESORT_REFER_ID", referencedColumnName = "BRAND_BIGSORT_ID"
			)
	private BrandBigSort bigSort;
}
