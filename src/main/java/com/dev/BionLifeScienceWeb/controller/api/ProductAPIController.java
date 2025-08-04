package com.dev.BionLifeScienceWeb.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dev.BionLifeScienceWeb.model.product.BigSort;
import com.dev.BionLifeScienceWeb.model.product.MiddleSort;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductAPIController {

	private final BigSortRepository bigSortRepository;
    private final MiddleSortRepository middleSortRepository;
    private final SmallSortRepository smallSortRepository;
    private final ProductRepository productRepository;
    
    @GetMapping("/bigSortList")
    public List<BigSort> getBigSortList() {
        return bigSortRepository.findAllByOrderByBigSortIndexAsc();
    }
    
    // 1. 대분류 ID → 중분류 리스트
    @GetMapping("/middleSortList")
    public List<MiddleSort> getMiddleSortList(@RequestParam Long bigSortId) {
        return middleSortRepository.findAllByBigSortOrderByMiddleSortIndexAsc(bigSortRepository.findById(bigSortId).get());
    }

    // 2. 중분류 ID → 소분류 리스트
    @GetMapping("/smallSortList")
    public List<SmallSort> getSmallSortList(@RequestParam Long middleSortId) {
        return smallSortRepository.findAllByMiddleSortOrderBySmallSortIndexAsc(middleSortRepository.findById(middleSortId).get());
    }

    // 3. 소분류 ID → 제품 리스트
    @GetMapping("/productList")
    public List<Product> getProductList(@RequestParam Long smallSortId) {
        return productRepository.findBySmallSortOrderByProductIndexAsc(smallSortRepository.findById(smallSortId).get());
    }
}
