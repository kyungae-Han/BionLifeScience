package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dev.BionLifeScienceWeb.model.Banner;
import com.dev.BionLifeScienceWeb.model.Event;
import com.dev.BionLifeScienceWeb.model.HistorySubject;
import com.dev.BionLifeScienceWeb.model.Notice;
import com.dev.BionLifeScienceWeb.model.ReferenceFile;
import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;
import com.dev.BionLifeScienceWeb.model.product.BigSort;
import com.dev.BionLifeScienceWeb.model.product.MiddleSort;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;
import com.dev.BionLifeScienceWeb.repository.BannerRepository;
import com.dev.BionLifeScienceWeb.repository.CertificationRepository;
import com.dev.BionLifeScienceWeb.repository.CompanyEmailRepository;
import com.dev.BionLifeScienceWeb.repository.EventRepository;
import com.dev.BionLifeScienceWeb.repository.HistoryContentRepository;
import com.dev.BionLifeScienceWeb.repository.HistorySubjectRepository;
import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import com.dev.BionLifeScienceWeb.repository.ReferenceFileRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;

import jakarta.servlet.http.HttpServletResponse;

import com.dev.BionLifeScienceWeb.model.Certification;
import com.dev.BionLifeScienceWeb.model.CompanyEmail;

import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
	
	private final HistorySubjectRepository historySubjectRepository;
	private final HistoryContentRepository historyContentRepository;
	private final ProductRepository productRepository;
	private final BigSortRepository bigSortRepository;
	private final MiddleSortRepository middleSortRepository;
	private final SmallSortRepository smallSortRepository;
	private final BannerRepository bannerRepository;
	private final EventRepository eventRepository;
	private final NoticeRepository noticeRepository;
	private final ReferenceFileRepository referenceFileRepository;
	private final BrandRepository brandRepository;
	private final BrandBigSortRepository brandBigSortRepository;
	private final BrandMiddleSortRepository brandMiddleSortRepository;
	private final BrandSmallSortRepository brandSmallSortRepository;
	private final BrandProductRepository brandProductRepository;
	private final CertificationRepository certificationRepository;
	
	
	@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="잘못된 접근입니다.")
    public class UrlNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L; 
	}
	
	@GetMapping({"/", "/index"})
	public String index(
		 Model model
			) {
		
		List<Banner> banners = bannerRepository.findAll();
		
		for (Banner b : banners) {
	        if (b.getWebroad() != null) {
	            b.setWebroad(b.getWebroad().replace("/administration/", "/upload/"));
	        }
	        if (b.getMobileroad() != null) {
	            b.setMobileroad(b.getMobileroad().replace("/administration/", "/upload/"));
	        }
	    }
		
		List<Event> ev = eventRepository.findAll();
		if(ev.size()<1) {
			Event e = new Event();
			e.setContent("이벤트 배너 내용 입니다");
			e.setSubject("이벤트 배너 제목 입니다");
			e.setLink("https://www.naver.com");
			ev.add(e);
		}
		List<Notice> notice = noticeRepository.findTop5ByOrderBySignDescDateDesc();
		List<ReferenceFile> fi = referenceFileRepository.findAll();
		List<BrandProduct> pr =
				brandProductRepository.findAllByBrand_TypeOrderByBrandProductIndexAsc(Brand.BrandType.OWN);
		
		
		for (BrandProduct p : pr) {
	        p.setFirstImageRoad(p.addFirstImage());
	    }
		
		
		List<Brand> partnerBrands = brandRepository.findAllByTypeOrderByNameAsc(Brand.BrandType.PARTNER)
		.stream()
		.filter(b -> b.getBrandFooterImageRoad() != null && !b.getBrandFooterImageRoad().isBlank())
	    .collect(Collectors.toList());
		
		
	    for (Brand b : partnerBrands) {
	        if (b.getImageRoad() != null)
	            b.setImageRoad(b.getImageRoad().replace("/administration/", "/upload/"));
	        
	        if (b.getBrandFooterImageRoad() != null)
	            b.setBrandFooterImageRoad(b.getBrandFooterImageRoad().replace("/administration/", "/upload/"));
	    }
		
		model.addAttribute("fi", fi);
		model.addAttribute("notice", notice);
		model.addAttribute("products", pr);
		model.addAttribute("ev", ev.get(0));
		model.addAttribute("ba", banners);
		model.addAttribute("brands",partnerBrands);
		
		return "front/index";
	}
	
	@GetMapping("/about")
    public String about(Model model) {
        var subject = historySubjectRepository.findAllByOrderByStartDesc();
        for (var s : subject) {
            s.setContents(historyContentRepository.findAllBySubjectIdOrderByDateDesc(s.getId()));
        }
        
        
        List<Certification> certs = certificationRepository.findAll();
        model.addAttribute("certification", certs);
        
        model.addAttribute("list", subject);

        return "front/company/about";
    }
	
	@GetMapping("/rnd")
    public String overview(Model model) {

        return "front/business/rnd";
    }
	
	@GetMapping("/history")
	public String history(
			Model model
			) {
		
		List<HistorySubject> subject = historySubjectRepository.findAllByOrderByStartDesc();
		
		
		for(HistorySubject s : subject) {
			s.setContents(historyContentRepository.findAllBySubjectIdOrderByDateDesc(s.getId()));
		}
		
		model.addAttribute("list",subject);
		
		return "front/company/history";
	}
	
	@GetMapping("/certifications")
	public String certifications(
			Model model
			) {
		
		model.addAttribute("certification", certificationRepository.findAll());
		return "front/company/certifications";
	}
	
	@GetMapping("/address")
	public String address(
			Model model
			) {
		return "front/company/address";
	}
	
	
    @GetMapping("/contact")
    public void contactRedirect(HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write("""
            <script>
                alert('잘못된 접근입니다');
                window.location.href = '/';
            </script>
        """);
    }

    // ✅ 모달에서 불러올 실제 HTML
    @GetMapping("/contact-body")
    public String contactBody() {
        return "front/customer/contact";  // 기존 contact.html 템플릿 경로
    }
	
	
	
	@GetMapping("/productOverall")
	public String productOverall(
			Model model
			) {
		return "front/product/productOverall";
	}
	
	@GetMapping("/productDetail/{id}")
	public String productDetail(
			Model model,
			@PathVariable Long id
			) {
		
		Optional<Product> product = productRepository.findById(id);
		if(product.isPresent()) {
			if(product.get().getImages().size() > 0) {
				
				product.get().setFirstImageRoad(product.get().getImages().get(0).getProductImageRoad());
			}else {
				product.get().setFirstImageRoad("null");
			}
			model.addAttribute("product", product.get());

			return "front/product/productDetail";
		}else {
			throw new UrlNotFoundException();
		}
	}
	
	
	@GetMapping("/brandProductDetail/{id}")
	public String brandProductDetail(
	        Model model,
	        @PathVariable Long id
	) {
	    BrandProduct product = brandProductRepository.findById(id)
	            .orElseThrow(UrlNotFoundException::new);

	    // 이미지 첫 번째 세팅
	    if (product.getImages() != null && !product.getImages().isEmpty()) {
	        product.setFirstImageRoad(product.getImages().get(0).getProductImageRoad());
	    } else {
	        product.setFirstImageRoad("null");
	    }

	    // ✅ middleSort, smallSort null 체크 후 모델에 담기
	    if (product.getMiddleSort() != null) {
	        model.addAttribute("middleSort", product.getMiddleSort());
	    }
	    if (product.getSmallSort() != null) {
	        model.addAttribute("smallSort", product.getSmallSort());
	    }

	    // ✅ brand 도 같이 내려주면 뷰에서 브랜드명도 활용 가능
	    if (product.getBrand() != null) {
	        model.addAttribute("brand", product.getBrand());
	    }

	    model.addAttribute("product", product);
	    
	    List<BrandProduct> relatedProducts = new ArrayList<>();
	    
	    if (product != null && product.getBrand() != null) {
	        relatedProducts = brandProductRepository.findTop8ByBrandAndIdNotOrderByBrandProductIndexAsc(product.getBrand(), id);
	    }

	    model.addAttribute("relatedProducts", relatedProducts);

	    return "front/brand/brandProductDetail";
	}
	
	
	
	
	@GetMapping("/brandProductSorted/brand/{id}")
	public String brandEntry(@PathVariable Long id) {
	    Brand brand = brandRepository.findById(id)
	            .orElseThrow(UrlNotFoundException::new);

	    if (brand.getType() == Brand.BrandType.OWN) {
	        return "redirect:/brand/own/" + id;
	    }
	    
	    if (brand.getType() == Brand.BrandType.GENERIC) {
	        // OTHER는 바로 리스트 페이지로 이동
	        return "redirect:/brandProductSorted/list/" + id;
	    }
	    
	    // PARTNER: 해당 브랜드의 첫 상품으로 이동 (없으면 카테고리로)
	    Page<BrandProduct> firstPage =
	            brandProductRepository
	                .findAllByBrand(PageRequest.of(0, 1), brand);

	    if (!firstPage.isEmpty()) {
	        Long productId = firstPage.getContent().get(0).getId();
	        return "redirect:/brandProductDetail/" + productId;
	    }

	    return "redirect:/brandProductSorted/list/" + id;
	}
	
	
	@GetMapping("/brandProductSorted/list/{id}")
	public String partnerBrandList(
	        @PathVariable Long id,
	        @PageableDefault(size = 12) Pageable pageable,
	        Model model) {

	    // 이미 있는 범용 핸들러로 위임: sort="brand"
	    return brandProductSorted(model, "brand", id, pageable);
	}

	
	@GetMapping("/brandList")
	public String brandOverview(Model model) {
		
	    model.addAttribute("partnerBrands",
	        brandRepository.findAllByTypeOrderByNameAsc(Brand.BrandType.PARTNER));

	    return "front/brand/brandOverview";
	}
	
	
	@GetMapping("/brandList/{id}/products")
	@ResponseBody
	public Map<String, Object> brandProductsAjax(@PathVariable Long id) {
	    Brand brand = brandRepository.findById(id)
	            .orElseThrow(UrlNotFoundException::new);

	    Map<String, Object> result = new HashMap<>();
	    result.put("brandName", brand.getName());
	    result.put("brandDesc", brand.getContent());
	    result.put("brandLogo", brand.getImageRoad());

	    // ✅ 1️⃣ 브랜드의 전체 대분류 가져오기
	    List<BrandBigSort> bigSortList =
	    	    brandBigSortRepository.findAllByBrandOrderByBrandBigSortIndexAsc(brand);

	    // ✅ 2️⃣ 브랜드의 전체 상품
	    List<BrandProduct> products = brandProductRepository
	            .findByBrandOrderByBrandProductIndexAsc(brand);

	    // ✅ 3️⃣ 대분류별 상품 매핑
	    List<Map<String, Object>> bigSortResult = new ArrayList<>();

	    for (BrandBigSort bigSort : bigSortList) {
	        Map<String, Object> categoryMap = new HashMap<>();
	        categoryMap.put("bigSortName", bigSort.getName());

	        List<Map<String, Object>> productList = products.stream()
	                .filter(p -> p.getBigSort() != null &&
	                        Objects.equals(p.getBigSort().getId(), bigSort.getId()))
	                .map(p -> {
	                    Map<String, Object> m = new HashMap<>();
	                    m.put("id", p.getId());
	                    m.put("subject", p.getSubject());
	                    m.put("content", p.getContent());
	                    m.put("firstImageRoad", (p.getImages() != null && !p.getImages().isEmpty())
	                            ? p.getImages().get(0).getProductImageRoad()
	                            : "/front/images/no-image.png");
	                    return m;
	                })
	                .collect(Collectors.toList());

	        categoryMap.put("products", productList);
	        bigSortResult.add(categoryMap);
	    }

	    result.put("categories", bigSortResult);
	    return result;
	}
	
	
	
	@GetMapping("/brandList/own/{id}")
	public String ownBrandDetail(@PathVariable Long id, Model model) {
	    Brand brand = brandRepository.findById(id)
	            .orElseThrow(UrlNotFoundException::new);
	    
	    for (BrandProduct p : brand.getProducts()) {
	        if (p.getImages() != null && !p.getImages().isEmpty()) {
	            p.setFirstImageRoad(p.getImages().get(0).getProductImageRoad());
	        } else {
	            p.setFirstImageRoad("/front/images/common/no-image.png");
	        }
	    }
	    
	    System.out.println("Products size = " + brand.getProducts().size());
	    System.out.println("Products class = " + brand.getProducts().getClass());

	    List<BrandProduct> products = brand.getProducts();

	    // ✅ 이 부분 추가
	    if (products == null || products.isEmpty()) {
	        model.addAttribute("products", null);
	    } else {
	        model.addAttribute("products", products);
	    }

	    List<BrandBigSort> bigSorts = brandBigSortRepository.findAllByBrand_IdOrderByBrandBigSortIndexAsc(id);
	    List<BrandMiddleSort> middleSorts = brandMiddleSortRepository.findAllByBigSort_Brand_IdOrderByBrandMiddleSortIndexAsc(id);
	    List<BrandSmallSort> smallSorts = brandSmallSortRepository.findAllByMiddleSort_BigSort_Brand_IdOrderByBrandSmallSortIndexAsc(id);
	    
	    model.addAttribute("brand", brand);
	    model.addAttribute("bigSorts", bigSorts);
	    model.addAttribute("middleSorts", middleSorts);
	    model.addAttribute("smallSorts", smallSorts);
	    model.addAttribute("sort", "brand");
	    model.addAttribute("id", id);

	    return "front/brand/ownBrandDetail";
	}


	@GetMapping("/notice")
	public String notice(@RequestParam(defaultValue = "1") int page,
	                     @RequestParam(required = false) String searchText,
	                     Model model) {

	    int size = 10; // 한 페이지 10개
	    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

	    Page<Notice> p;

	    if (searchText != null && !searchText.isBlank()) {
	    	
	    	
	        p = noticeRepository
	                .findBySubjectContainingIgnoreCaseOrContentContainingIgnoreCase(
	                        searchText, searchText, pageable);
	        model.addAttribute("pinned", List.of()); 
	    } else {
	        // 📌 중요공지(고정) 따로 뽑기
	        List<Notice> pinned = noticeRepository.findTop5BySignOrderByDateDesc(true);
	        model.addAttribute("pinned", pinned);

	        // 일반 목록(고정 제외)만 페이징
	        p = noticeRepository.findBySignFalse(pageable);
	    }
	    

	    model.addAttribute("page", p);             
	    model.addAttribute("notices", p.getContent()); 
	    model.addAttribute("nowPage", page);         
	    model.addAttribute("size", size);
	    model.addAttribute("searchText", searchText);

	    return "front/notice/notice";
	}



	@GetMapping("/references")
	public String reference(
			Model model
			) {
		
		model.addAttribute("file",referenceFileRepository.findAll());
		return "front/customer/references";
	}
	
	@GetMapping("/productSorted/{sort}/{id}")
	public String productSorted(
			Model model,
			@PathVariable String sort,
			@PathVariable Long id,
			@PageableDefault(size = 12) Pageable pageable
			
			) {
			Page<Product> products = null;
			String name = "";
			if(sort.equals("main")) {
				Optional<BigSort> b = bigSortRepository.findById(id);
				if(b.isPresent()) {
					products = productRepository.findAllByBigSort(pageable, b.get());
					if(products.getNumberOfElements()>0) {
						name = b.get().getName();
					}else {
						name="해당 분류에 속하는 제품이 존재하지 않습니다.";
					}
				}else {
					products = null;
					throw new UrlNotFoundException();
					
				}
			}else if(sort.equals("middle")) {
				Optional<MiddleSort> m = middleSortRepository.findById(id);
				if(m.isPresent()) {
					products = productRepository.findAllByMiddleSort(pageable, m.get());
					if(products.getNumberOfElements()>0) {
						name = m.get().getName();
					}else {
						name="해당 분류에 속하는 제품이 존재하지 않습니다.";
					}
				}else {
					products = null;
					throw new UrlNotFoundException();
				}
			}else if(sort.equals("sub")) {
				Optional<SmallSort> s = smallSortRepository.findById(id);
				if(s.isPresent()) {
					products = productRepository.findAllBySmallSortOrderByIdDesc(pageable, s.get());
					if(products.getNumberOfElements()>0) {
						name = smallSortRepository.findById(id).get().getName();
					}else {
						name="해당 분류에 속하는 제품이 존재하지 않습니다.";
					}
					
				}else {
					products = null;
					throw new UrlNotFoundException();
				}
			}
			if(products.getNumberOfElements()>0) {
				for(Product p : products) {
					if(!p.getImages().isEmpty()) {
						p.setFirstImageRoad(p.getImages().get(0).getProductImageRoad());
					}else {
						p.setFirstImageRoad("-");
					}
				}
			}
			model.addAttribute("products", products);
			model.addAttribute("name", name);
			int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
			int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
			model.addAttribute("sort", sort);
			model.addAttribute("id",id);
			return "front/product/productSorted";
		
	}
	
	@GetMapping("/brandProductSorted/{sort}/{id}")
	public String brandProductSorted(
			Model model,
			@PathVariable String sort,
			@PathVariable Long id,
			@PageableDefault(size = 12) Pageable pageable
			
			) {
		
		Page<BrandProduct> products = null;
		Brand brand = null;	
		String name = "";
		

		if(sort.equals("brand")) {
			Optional<Brand> b = brandRepository.findById(id);
			
			if(b.isPresent()) {
				products = brandProductRepository.findAllByBrand(pageable, b.get());
				brand = b.get();
				if(products.getNumberOfElements()>0) {
					
					name = b.get().getName() + "브랜드의 제품들 입니다.";
				}else {
					name="해당 분류에 속하는 제품이 존재하지 않습니다.";
				}
				
			}else {
				brand = null;
				products = null;
				throw new UrlNotFoundException();
			}
		}else if(sort.equals("main")) {
			
			Optional<BrandBigSort> b = brandBigSortRepository.findById(id);
			
			if(b.isPresent()) {
				products = brandProductRepository.findAllByBigSort(pageable, b.get());
				brand = b.get().getBrand();
				if(products.getNumberOfElements()>0) {
					
					name = brand.getName() + "브랜드의 " + b.get().getName() + "분류에 속한 제품들 입니다.";
				}else {
					name="해당 분류에 속하는 제품이 존재하지 않습니다.";
				}
			}else {
				products = null;
				brand = null;
				throw new UrlNotFoundException();
			}
		}else if(sort.equals("middle")) {
			Optional<BrandMiddleSort> b = brandMiddleSortRepository.findById(id);
			if(b.isPresent()) {
				products = brandProductRepository.findAllByMiddleSort(pageable, b.get());
				brand = b.get().getBigSort().getBrand();
				if(products.getNumberOfElements()>0) {
					name = brand.getName() + "브랜드의 " + b.get().getName() + "분류에 속한 제품들 입니다.";
				}else {
					name="해당 분류에 속하는 제품이 존재하지 않습니다.";
				}
				
			}else {
				products = null;
				brand = null;
				throw new UrlNotFoundException();
			}
			
		}else if(sort.equals("sub")) {
			
			Optional<BrandSmallSort> b = brandSmallSortRepository.findById(id);
			if(b.isPresent()) {
				products = brandProductRepository.findAllBySmallSort(pageable, b.get());
				brand = b.get().getMiddleSort().getBigSort().getBrand();
				if(products.getNumberOfElements()>0) {
					
					name = brand.getName() + "브랜드의 " + b.get().getName() + "분류에 속한 제품들 입니다.";
				}else {
					name="해당 분류에 속하는 제품이 존재하지 않습니다.";
				}
				
			}else {
				products = null;
				brand = null;
				throw new UrlNotFoundException();
			}
		}
		
		if(products.getNumberOfElements()>0) {
			for(BrandProduct p : products) {
				if(!p.getImages().isEmpty()) {
					p.setFirstImageRoad(p.getImages().get(0).getProductImageRoad());
				}else {
					p.setFirstImageRoad("-");
				}
			}
		}
		
		model.addAttribute("brandInfo", brand);
		model.addAttribute("products", products);
		model.addAttribute("name", name);
		int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
		int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("sort", sort);
		model.addAttribute("id",id);
		
		return "front/brand/brandProductSorted";
	}
	
	@ControllerAdvice
	@RequiredArgsConstructor
	public class NavAdvice {
	    private final BrandRepository brandRepository;
	    private final BrandBigSortRepository bigSortRepository;
	    private final BrandMiddleSortRepository middleSortRepository;
	    private final BrandSmallSortRepository smallSortRepository;

	    @ModelAttribute("partnerBrands")
	    public List<Brand> partnerBrands() {
	        return brandRepository
	            .findAllByTypeOrderByNameAsc(Brand.BrandType.PARTNER);
	    }

	    @ModelAttribute("ownBrands")
	    public List<Brand> ownBrands() {
	        return brandRepository
	            .findAllByTypeOrderByNameAsc(Brand.BrandType.OWN);
	    }
	    
	    @ModelAttribute("genericBrands")
	    public List<Brand> genericBrands() {
	        return brandRepository
	        		.findAllByTypeOrderByNameAsc(Brand.BrandType.GENERIC);
	    }
	    
	    // 3depth → BigSort (브랜드별)
	    @ModelAttribute("brandBigSortList")
	    public List<BrandBigSort> brandBigSortList() {
	        return bigSortRepository.findAllByOrderByBrandBigSortIndexAsc();
	    }

	    // 4depth → MiddleSort (BigSort별)
	    @ModelAttribute("brandMiddleSortList")
	    public List<BrandMiddleSort> brandMiddleSortList() {
	        return middleSortRepository.findAllByOrderByBrandMiddleSortIndexAsc();
	    }

	    // 5depth → SmallSort (MiddleSort별)
	    @ModelAttribute("brandSmallSortList")
	    public List<BrandSmallSort> brandSmallSortList() {
	        return smallSortRepository.findAllByOrderByBrandSmallSortIndexAsc();
	    }
	}
	
	
	@ControllerAdvice
	@RequiredArgsConstructor
	public class GlobalAdvice {

	    private final CompanyEmailRepository companyEmailRepository;

	    @ModelAttribute("companyEmail")
	    public String companyEmail() {
	        return companyEmailRepository.findAll()
	                .stream()
	                .findFirst()
	                .map(CompanyEmail::getEmail)
	                .orElse("info@bionlifescience.com");
	    }
	}
}






















