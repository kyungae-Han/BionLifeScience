package com.dev.BionLifeScienceWeb.dto;

import java.util.List;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;
import com.dev.BionLifeScienceWeb.model.product.BigSort;
import com.dev.BionLifeScienceWeb.model.product.MiddleSort;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuDTO {


	private List<Product> productList;
	private List<BigSort> bigSortList;
	private List<MiddleSort> middleSortList;
	private List<SmallSort> smallSortList;
	
	private List<Brand> brandList;
	private List<BrandBigSort> brandBigSortList;
	private List<BrandMiddleSort> brandMiddleSortList;
	private List<BrandSmallSort> brandSmallSortList;
	private List<BrandProduct> brandProductList;
}
















