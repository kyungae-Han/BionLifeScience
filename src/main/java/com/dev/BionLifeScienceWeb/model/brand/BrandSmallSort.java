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
@Table(name="brand_small_sort")
public class BrandSmallSort {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BRAND_SMALLSORT_ID")
	private Long id;
	
	@Column(name="BRAND_SMALLSORT_NAME")
	private String name;
	
	/** 영문 화면용. 비어 있으면 한글이 그대로 나간다 */
	@Column(name="BRAND_SMALLSORT_NAME_EN")
	private String nameEn;
	
	@Transient
	private Long brandMiddleSortId;
	
	@Column(name="BRAND_SMALLSORT_INDEX")
	private int brandSmallSortIndex;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(
			name="BRAND_SMALLSORT_REFER_ID", referencedColumnName = "BRAND_MIDDLESORT_ID"
			)
	private BrandMiddleSort middleSort;

}
