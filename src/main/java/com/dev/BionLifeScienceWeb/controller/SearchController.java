package com.dev.BionLifeScienceWeb.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import com.dev.BionLifeScienceWeb.repository.ReferenceFileRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SearchController {

	private final ProductRepository productRepository;
	private final BrandProductRepository brandProductRepository;
	private final NoticeRepository noticeRepository;
	private final ReferenceFileRepository referenceRepository;

	@RequestMapping(value = "/searchAll",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String searchAll(
			String searchWord,
			Model model
			) {
		
		List<Product> products = productRepository.findBySubjectContains(searchWord);
		for(Product p : products) {
			if(p.getImages().size() > 0) {
				
				p.setFirstImageRoad(p.getImages().get(0).getProductImageRoad());
			}else {
				p.setFirstImageRoad("null");
			}
		}
		model.addAttribute("products", products);
		
		List<BrandProduct> brandProducts = brandProductRepository.findBySubjectContains(searchWord);
		for(BrandProduct p : brandProducts) {
			if(p.getImages().size() > 0) {
				
				p.setFirstImageRoad(p.getImages().get(0).getProductImageRoad());
			}else {
				p.setFirstImageRoad("null");
			}
		}
		model.addAttribute("brandProducts", brandProducts);
		
		model.addAttribute("notices", noticeRepository.findAllBySubjectContainsOrderBySignDescDateDesc(searchWord));
		model.addAttribute("references", referenceRepository.findByFilesubjectContains(searchWord));
		
		return "front/search/searchAll";
	}
	
	@GetMapping("/searchSorted")
	public String searchSorted(
			
			) {
		return "front/search/searchSorted";
	}
}
