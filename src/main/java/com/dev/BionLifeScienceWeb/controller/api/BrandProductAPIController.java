package com.dev.BionLifeScienceWeb.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/brandProduct")
@RequiredArgsConstructor
public class BrandProductAPIController {

    private final BrandBigSortRepository brandBigSortRepository;
    private final BrandMiddleSortRepository brandMiddleSortRepository;
    private final BrandSmallSortRepository brandSmallSortRepository;
    private final BrandProductRepository brandProductRepository;
    private final BrandRepository brandRepository;
    
    @GetMapping("/brandList")
    public List<Brand> getBrandList() {
        return brandRepository.findAllByOrderByBrandIndexAsc();
    }
    
    // 1. 브랜드 ID → 대분류 리스트
    @GetMapping("/bigSortList")
    public List<BrandBigSort> getBigSortList(@RequestParam Long brandId) {
        return brandBigSortRepository.findAllByBrandOrderByBrandBigSortIndexAsc(brandRepository.findById(brandId).get());
    }

    // 2. 대분류 ID → 중분류 리스트
    @GetMapping("/middleSortList")
    public List<BrandMiddleSort> getMiddleSortList(@RequestParam Long bigSortId) {
        return brandMiddleSortRepository.findAllByBigSortOrderByBrandMiddleSortIndexAsc(brandBigSortRepository.findById(bigSortId).get());
    }

    // 3. 중분류 ID → 소분류 리스트
    @GetMapping("/smallSortList")
    public List<BrandSmallSort> getSmallSortList(@RequestParam Long middleSortId) {
        return brandSmallSortRepository.findAllByMiddleSortOrderByBrandSmallSortIndexAsc(brandMiddleSortRepository.findById(middleSortId).get());
    }

    // 4. 소분류 ID → 제품 리스트
    @GetMapping("/productList")
    public List<BrandProduct> getProductList(@RequestParam Long smallSortId) {
    	return brandProductRepository.findBySmallSortOrderByBrandProductIndexAsc(brandSmallSortRepository.findById(smallSortId).get());
    }
}
