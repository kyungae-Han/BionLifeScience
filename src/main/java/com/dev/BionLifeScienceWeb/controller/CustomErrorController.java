package com.dev.BionLifeScienceWeb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController{

	private String VIEW_PATH = "/error/";
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	BigSortRepository bigSortRepository;
	
	@Autowired
	MiddleSortRepository middleSortRepository;
	
	@Autowired
	SmallSortRepository smallSortRepository;
	
	@Autowired
	BrandRepository brandRepository;
	
	@Autowired
	BrandBigSortRepository brandBigSortRepository;
	
	@Autowired
	BrandMiddleSortRepository brandMiddleSortRepository;
	
	@Autowired
	BrandSmallSortRepository brandSmallSortRepository;
	
	@Autowired
	BrandProductRepository brandProductRepository;
	
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
	
	@GetMapping("/error")
	public String handleError(
			HttpServletRequest request,
			Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if(status != null) {
			int statusCode = Integer.valueOf(status.toString());
			
			if(statusCode == HttpStatus.NOT_FOUND.value()) {
				return VIEW_PATH+"404";
			}else if(statusCode == 500){
				return VIEW_PATH+"500";
			}else {
				return VIEW_PATH+"403";
			}
		}
		return "error";
	}
	
}
