package com.dev.BionLifeScienceWeb.model.brand;

import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.Nullable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name="brand_product")
@Data
public class BrandProduct {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BRAND_PRODUCT_ID")
	private Long id;
	
	@Column(name="BRAND_PRODUCT_SUBJECT")
	private String subject;
	
	/** 영문 화면용. 비어 있으면 한글이 그대로 나간다 */
	@Column(name="BRAND_PRODUCT_SUBJECT_EN")
	private String subjectEn;
	
	@Column(name="BRAND_PRODUCT_CODE")
	@Nullable
	private String brandProductCode;
	
	@Column(name="BRAND_PRODUCT_CONTENT")
	private String content;
	
	@Column(name="BRAND_PRODUCT_CONTENT_EN")
	private String contentEn;
	
	@Column(name="BRAND_PRODUCT_SUB_CONTENT")
	private String productSubContent;
	
	@Column(name="BRAND_PRODUCT_SUB_CONTENT_EN")
	private String productSubContentEn;
	
	/** 상세페이지 본문. 관리자 편집기(summernote)로 넣는다. 이미지가 들어가니 LONGTEXT 다 */
	@Lob
	@Column(name="BRAND_PRODUCT_DETAIL_CONTENT", columnDefinition="LONGTEXT")
	private String detailContent;
	
	@Lob
	@Column(name="BRAND_PRODUCT_DETAIL_CONTENT_EN", columnDefinition="LONGTEXT")
	private String detailContentEn;
	
	/** 검색용 키워드. 쉼표로 여러 개. 화면에는 안 나오고 사이트 검색과 meta keywords 에만 쓴다 */
	@Column(name="BRAND_PRODUCT_SEARCH_KEYWORD")
	private String searchKeyword;
	
	@Column(name="BRAND_TABLE_IMAGE_PATH")
	private String tableImagePath;
	
	@Column(name="BRAND_TABLE_IMAGE_NAME")
	private String tableImageName;
	
	@Column(name="BRAND_TABLE_IMAGE_ROAD")
	private String tableImageRoad;
	
	@Column(name="BRAND_SPEC_IMAGE_PATH")
	private String specImagePath;
	
	@Column(name="BRAND_SPEC_IMAGE_NAME")
	private String specImageName;
	
	@Column(name="BRAND_SPEC_IMAGE_ROAD")
	private String specImageRoad;
	
	@Column(name="BRAND_PRODUCT_SIGN")
	private Boolean sign;
	
	@Column(name="BRAND_PRODUCT_INDEX")
	private int brandProductIndex;
	
	@Transient
	private Long brandSmallSortId;
	
	@Transient
	private Long brandMiddleSortId;
	
	@Transient
	private Long brandBigSortId;
	
	@Transient
	private Long brandId;
	
	@Transient
	private String firstImageRoad;
	
	public String addFirstImage() {
		
		return (images != null && !images.isEmpty())
	            ? images.get(0).getProductImageRoad()
	            : null;
	}
	
	@OneToMany(
			fetch = FetchType.LAZY, 
			cascade = CascadeType.ALL,
			orphanRemoval = true,
			mappedBy = "product"
			)
	private List<BrandProductImage> images = new ArrayList<>();
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<BrandProductInfo> infos = new ArrayList<>();
	
	@OneToMany(
			fetch = FetchType.LAZY, 
			cascade = CascadeType.ALL,
			orphanRemoval = true,
			mappedBy = "product"
			)
	private List<BrandProductFile> files = new ArrayList<>();
	
	@OneToMany(
			fetch = FetchType.LAZY, 
			cascade = CascadeType.ALL,
			orphanRemoval = true,
			mappedBy = "product"
			)
	@OrderBy("specOrder ASC")
	private List<BrandProductSpec> specs = new ArrayList<>();
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(
	    name="BRAND_PRODUCT_REFER_ID", 
	    referencedColumnName="BRAND_SMALLSORT_ID", 
	    nullable = true
	)
	private BrandSmallSort smallSort;

	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(
	    name="BRAND_PRODUCT_MIDDLE_REFER_ID", 
	    referencedColumnName="BRAND_MIDDLESORT_ID", 
	    nullable = true
	)
	private BrandMiddleSort middleSort;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(
	    name="BRAND_PRODUCT_BIG_REFER_ID", 
	    referencedColumnName="BRAND_BIGSORT_ID", 
	    nullable = false
	)
	private BrandBigSort bigSort;
	
	
	@ManyToOne
	@JoinColumn(name="BRAND_PRODUCT_BRAND_REFER_ID")
	private Brand brand;
}
