package com.dev.BionLifeScienceWeb.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.dev.BionLifeScienceWeb.dto.MenuDTO;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;

import lombok.RequiredArgsConstructor;


/**
 * 모든 VIEW 에서 메뉴리스트 사용을 위한 도움 클래스
 * ${menuList} 통해 접근
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

	private final ProductRepository productRepository;
	private final BigSortRepository bigSortRepository;
	private final MiddleSortRepository middleSortRepository;
	private final SmallSortRepository smallSortRepository;
	private final BrandRepository brandRepository;
	private final BrandBigSortRepository brandBigSortRepository;
	private final BrandMiddleSortRepository brandMiddleSortRepository;
	private final BrandSmallSortRepository brandSmallSortRepository;
	private final BrandProductRepository brandProductRepository;
	
	@ModelAttribute("menuList")
	public MenuDTO menuList(MenuDTO menuDto) {
		
		menuDto.setProductList(productRepository.findAllByOrderByProductIndexAsc());
		menuDto.setBigSortList(bigSortRepository.findAllByOrderByBigSortIndexAsc());
		menuDto.setMiddleSortList(middleSortRepository.findAllByOrderByMiddleSortIndexAsc());
		menuDto.setSmallSortList(smallSortRepository.findAllByOrderBySmallSortIndexAsc());
		
		menuDto.setBrandList(brandRepository.findAllByOrderByBrandIndexAsc());
		menuDto.setBrandBigSortList(brandBigSortRepository.findAllByOrderByBrandBigSortIndexAsc());
		menuDto.setBrandMiddleSortList(brandMiddleSortRepository.findAllByOrderByBrandMiddleSortIndexAsc());
		menuDto.setBrandSmallSortList(brandSmallSortRepository.findAllByOrderByBrandSmallSortIndexAsc());
		menuDto.setBrandProductList(brandProductRepository.findAllByOrderByBrandProductIndexAsc());
		return menuDto;
	}
}