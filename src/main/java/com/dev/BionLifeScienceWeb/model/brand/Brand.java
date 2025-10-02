package com.dev.BionLifeScienceWeb.model.brand;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import lombok.Data;

@Data
@Entity
@Table(name="brand")
public class Brand {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BRAND_ID")
	private Long id;
	
	@Column(name="BRAND_NAME")
	private String name;
	
	@Column(name="BRAND_CONTENT")
	private String content;
	
	@Column(name="BRAND_IMAGE_PATH", nullable=false, length=255)
	private String imagePath;
	
	@Column(name="BRAND_IMAGE_ROAD", nullable=false, length=255)
	private String imageRoad;
	
	@Column(name="BRAND_IMAGE_NAME", nullable=false, length=255)
	private String imageName;
	
	@Column(name="BRAND_INDEX")
	private int brandIndex;
	
	@Enumerated(EnumType.STRING)
	@Column(name="BRAND_TYPE", nullable=false, length=16)
	private BrandType type;
	
	public enum BrandType {
		 OWN("자사브랜드"),
		 PARTNER("타사브랜드"),
		 GENERIC("GENERIC");
	
		 private final String label;
	
		 BrandType(String label) {
		     this.label = label;
		  }
	
		 public String getLabel() {
		     return label;
		  }
	}
	
	
	@Column(name = "BRAND_VISUAL_PATH", length = 255)
	private String visualPath;

	@Column(name = "BRAND_VISUAL_ROAD", length = 255)
	private String visualRoad;

	@Column(name = "BRAND_VISUAL_NAME", length = 255)
	private String visualName;

	@Lob
	@Column(name = "BRAND_DESC", columnDefinition="LONGTEXT")
	private String desc;
	
	@OneToMany(mappedBy = "brand", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@com.fasterxml.jackson.annotation.JsonIgnore
    private List<BrandProduct> products = new ArrayList<>();
	
	public void addProduct(BrandProduct product) {
	    products.add(product);
	    product.setBrand(this);
	}

	public void removeProduct(BrandProduct product) {
	    products.remove(product);
	    product.setBrand(null);
	}
}
