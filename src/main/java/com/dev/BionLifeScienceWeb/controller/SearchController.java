package com.dev.BionLifeScienceWeb.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.data.domain.PageRequest;

import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import com.dev.BionLifeScienceWeb.repository.ReferenceFileRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.util.EnText;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SearchController {

	private final ProductRepository productRepository;
	private final BrandProductRepository brandProductRepository;
	private final NoticeRepository noticeRepository;
	private final ReferenceFileRepository referenceRepository;
	private final EnText enText;

	/** 자동완성 목록에 보여 줄 최대 줄 수. 너무 길면 고르기 어렵다 */
	private static final int SUGGEST_LIMIT = 8;


	/**
	 * 검색칸 자동완성. 한 글자부터 제품명과 품목코드로 찾아 준다.
	 * 칠 때마다 도는 조회라 개수를 끊고 필요한 값만 돌려준다.
	 * 돌려주는 값: id(제품 상세로 가는 번호), name(제품명), code(품목코드)
	 */
	@GetMapping("/searchSuggest")
	@ResponseBody
	public List<Map<String, Object>> searchSuggest(@RequestParam(name = "q", required = false) String q) {
		List<Map<String, Object>> result = new ArrayList<>();
		if (q == null || q.trim().isEmpty()) {
			return result;
		}

		// 글자가 들어 있기만 하면 찾는다. 제품명, 검색키워드, 품목코드를 본다
		List<BrandProduct> found = brandProductRepository
				.suggestByKeyword(q.trim(), PageRequest.of(0, SUGGEST_LIMIT));

		for (BrandProduct p : found) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", p.getId());
			row.put("name", enText.pick(p.getSubject(), p.getSubjectEn()));
			row.put("code", firstItemCode(p));
			result.add(row);
		}
		return result;
	}

	/** 제품 상세 정보 중 주제가 '품목코드' 인 줄의 값. 없으면 빈 글자 */
	private String firstItemCode(BrandProduct p) {
		if (p.getSpecs() == null) {
			return "";
		}
		return p.getSpecs().stream()
				.filter(sp -> "품목코드".equals(sp.getProductSpecSubject()))
				.map(sp -> sp.getProductSpecContent())
				.filter(v -> v != null && !v.isBlank())
				.findFirst()
				.orElse("");
	}

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
		
		List<BrandProduct> brandProducts = brandProductRepository.searchAllByKeyword(searchWord);
		for (BrandProduct p : brandProducts) {
		    if (p.getImages().size() > 0) {
		        p.setFirstImageRoad(p.getImages().get(0).getProductImageRoad());
		    } else {
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
