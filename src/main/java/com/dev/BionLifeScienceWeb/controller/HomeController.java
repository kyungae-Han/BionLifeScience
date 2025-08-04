package com.dev.BionLifeScienceWeb.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

		List<Banner> b = bannerRepository.findAll();
		if(b.size()<1) {
			Banner ba = new Banner();
			ba.setMobileroad("/front/images/slider/swiper/1.jpg");
			ba.setWebroad("/front/images/slider/swiper/1.jpg");
			ba.setSubject("WELCOME to Bion Life Science");
			ba.setContent("You'll be surprised to see the Final\r\n" + 
					"										Results of your Creation &amp; would crave for more.");
			b.add(ba);
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
		List<Product> pr = productRepository.findAllBySignOrderByProductIndexAsc(true);
		for(Product p : pr) {
			if(p.getImages().size() > 0) {
				
				p.setFirstImageRoad(p.getImages().get(0).getProductImageRoad());
			}else {
				p.setFirstImageRoad("null");
			}
		}
		model.addAttribute("fi", fi);
		model.addAttribute("notice", notice);
		model.addAttribute("product", pr);
		model.addAttribute("ev", ev.get(0));
		model.addAttribute("ba", b);
		
		return "front/index";
	}
	
	@GetMapping("/about")
	public String about(
			Model model
			) {
		return "front/company/about";
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
	public String contact(
			Model model
			) {
		return "front/customer/contact";
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
		Optional<BrandProduct> product = brandProductRepository.findById(id);
		if(product.isPresent()) {
			
			if(product.get().getImages().size() > 0) {
				product.get().setFirstImageRoad(product.get().getImages().get(0).getProductImageRoad());
			}else {
				product.get().setFirstImageRoad("null");
			}
			model.addAttribute("pr", product.get());
			return "front/brand/brandProductDetail";
		}else {
			throw new UrlNotFoundException();
		}
	}

	@GetMapping("/notice")
	public String notice(
			Model model
			) {
		
		List<Notice> notice = noticeRepository.findTop5ByOrderBySignDescDateDesc();
		model.addAttribute("notice", notice);
		return "front/notice/notice";
	}

	@GetMapping("/references")
	public String reference(
			Model model
			) {
		
		model.addAttribute("file",referenceFileRepository.findAll());
		return "front/references";
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
}






















