package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev.BionLifeScienceWeb.controller.api.SummernoteImageProcessor;
import com.dev.BionLifeScienceWeb.exception.DeleteViolationException;
import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductInfo;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductSpec;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductInfoRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductSpecRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductFileService;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductImageService;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductService;
import com.dev.BionLifeScienceWeb.service.brand.BrandService;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BrandController {

	private final SummernoteImageProcessor imageProcessor;
	private final BrandRepository brandRepository;
	private final BrandBigSortRepository brandBigSortRepository;
	private final BrandMiddleSortRepository brandMiddleSortRepository;
	private final BrandSmallSortRepository brandSmallSortRepository;
	private final BrandProductRepository brandProductRepository;
	private final BrandProductInfoRepository brandProductInfoRepository;
	private final BrandProductSpecRepository brandProductSpecRepository;
	private final BrandProductFileRepository brandProductFileRepository;
	private final BrandProductImageRepository brandProductImageRepository;
	private final BrandService brandService;	
	private final BrandProductService brandProductService;
	private final BrandProductImageService brandProductImageService;
	private final BrandProductFileService brandProductFileService;
	
	@RequestMapping(value = "/brandManager",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String brandManager(
			Model model, 
			@PageableDefault(size = 10) Pageable pageable,
			@RequestParam(required = false) String searchWord
			) {
		if (searchWord != null) {
			Page<Brand> brands = brandRepository.findAllByName(pageable,searchWord);
			model.addAttribute("brands", brands);
			int startPage = Math.max(1, brands.getPageable().getPageNumber() - 4);
			int endPage = Math.min(brands.getTotalPages(), brands.getPageable().getPageNumber() + 4);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
			model.addAttribute("searchWord", searchWord);
			
		}else {
			Page<Brand> brands = brandRepository.findAll(pageable);
			int startPage = Math.max(1, brands.getPageable().getPageNumber() - 4);
			int endPage = Math.min(brands.getTotalPages(), brands.getPageable().getPageNumber() + 4);
			model.addAttribute("brands", brands);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
			model.addAttribute("searchWord", searchWord);
		}
		model.addAttribute("brand",brandRepository.findAll());
		return "admin/brand/brandManager";
	}
	
	@PostMapping("/brandInsert")
	@ResponseBody
	public String brandInsert(
	        @ModelAttribute Brand brand,
	        @RequestParam(value = "brandImage", required = false) MultipartFile brandImage,
	        @RequestParam(value = "visualImage", required = false) MultipartFile visualImage,
	        @RequestParam(value = "brandFooterImage", required = false) MultipartFile brandFooterImage
	) throws IOException {

	    if (brand.getType() == null) {
	        return "<script>alert('브랜드 타입을 선택하세요.');history.back();</script>";
	    }
	    
	    if (brandImage == null || brandImage.isEmpty()) {
	        return "<script>alert('로고 파일은 필수입니다.');history.back();</script>";
	    }
	    
	    String desc = imageProcessor.processEditorImages(brand.getDesc(), "brand");
		
		if (desc != null) {
		    desc = desc.replaceAll("(?i)<p>(\\s|&nbsp;|<br>|)*</p>", "");
		}

	    brand.setDesc(desc);

	    brandService.brandInsert(brandImage, visualImage, brandFooterImage, brand); // ✅ 세 개 넘김

	    return "<script>alert('브랜드가 등록 되었습니다.');"
	         + "location.href='/admin/brandManager';</script>";
	}
	
	
	
	@RequestMapping(value = "/brandDelete",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	
	@ResponseBody
	public String brandDelete(
			Long text,
			Model model
			) {
		
		try {
			brandRepository.deleteById(text);
		}catch(DeleteViolationException e) {
			throw new DeleteViolationException();
		}	
			
		StringBuffer sb = new StringBuffer();
		String msg = "브랜드가 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@GetMapping("/brandSortManager")
	public String brandSortManager(
			Model model
			) {
		
		model.addAttribute("brand",brandRepository.findAll());
		return "admin/brand/brandSortManager";
	}
	
	@RequestMapping(value = "/brandBigSortInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandBigSortInsert(
			BrandBigSort brandBigSort,
			Long brandId
			) {
		int index = 1;
		if(brandBigSortRepository.findFirstIndex().isPresent()) {
			index = brandBigSortRepository.findFirstIndex().get() + 1;
		}
		brandBigSort.setBrandBigSortIndex(index);
		brandBigSort.setBrand(brandRepository.findById(brandId).get());
		brandBigSortRepository.save(brandBigSort);
		StringBuffer sb = new StringBuffer();
		String msg = "브랜드의 대분류가 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandSortManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping(value = "/brandBigSortSearch",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public List<BrandBigSort> searchBrandBigSort(
			Model model,
			Long brandId
			){
		return brandBigSortRepository.findAllByBrand(brandRepository.findById(brandId).get());
	}
	
	@RequestMapping(value = "/brandBigSortDelete",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandBigSortDelete(
			Long text,
			Long brandId,
			Model model
			) {
		
		try {
			brandBigSortRepository.deleteById(text);
		}catch(DeleteViolationException e) {
			throw new DeleteViolationException();
		}	
			
		StringBuffer sb = new StringBuffer();
		String msg = "브랜드의 대분류가 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandSortManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping(value = "/brandMiddleSortInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandMiddleSortInsert(
			BrandMiddleSort brandMiddleSort,
			Long brandBigSortId
			) {
		int index = 1;
		if(brandMiddleSortRepository.findFirstIndex().isPresent()) {
			index = brandMiddleSortRepository.findFirstIndex().get() + 1;
		}
		brandMiddleSort.setBrandMiddleSortIndex(index);
		brandMiddleSort.setBigSort(brandBigSortRepository.findById(brandBigSortId).get());
		brandMiddleSortRepository.save(brandMiddleSort);
		StringBuffer sb = new StringBuffer();
		String msg = "브랜드의 중분류가 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandSortManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping(value = "/brandMiddleSortSearch",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public List<BrandMiddleSort> brandMiddleSortSearch(
			Model model,
			Long brandBigSortId
			){
		return brandMiddleSortRepository.findAllByBigSort(brandBigSortRepository.findById(brandBigSortId).get());
	}
	
	@RequestMapping(value = "/brandMiddleSortDelete",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandMiddleSortDelete(
			Long brandMiddleSortId,
			Model model
			) {
		
		try {
			brandMiddleSortRepository.deleteById(brandMiddleSortId);
		}catch(DeleteViolationException e) {
			throw new DeleteViolationException();
		}	
			
		StringBuffer sb = new StringBuffer();
		String msg = "브랜드의 중분류가 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandSortManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping(value = "/brandSmallSortInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandSmallSortInsert(
			BrandSmallSort brandSmallSort,
			Long brandMiddleSortId
			) {
		int index = 1;
		if(brandSmallSortRepository.findFirstIndex().isPresent()) {
			index = brandSmallSortRepository.findFirstIndex().get() + 1;
		}
		brandSmallSort.setBrandSmallSortIndex(index);
		brandSmallSort.setMiddleSort(brandMiddleSortRepository.findById(brandMiddleSortId).get());
		brandSmallSortRepository.save(brandSmallSort);
		StringBuffer sb = new StringBuffer();
		String msg = "브랜드의 소분류가 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandSortManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	
	@RequestMapping(value = "/brandSmallSortSearch",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public List<BrandSmallSort> brandSmallSortSearch(
			Model model,
			Long brandMiddleSortId
			){
		return brandSmallSortRepository.findAllByMiddleSort(brandMiddleSortRepository.findById(brandMiddleSortId).get());
	}
	
	@RequestMapping(value = "/brandSmallSortDelete",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandSmallSortDelete(
			Long brandSmallSortId,
			Model model
			) {
		
		try {
			brandSmallSortRepository.deleteById(brandSmallSortId);
		}catch(DeleteViolationException e) {
			throw new DeleteViolationException();
		}	
			
		StringBuffer sb = new StringBuffer();
		String msg = "브랜드의 소분류가 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandSortManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping(value = "/brandProductManager",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String brandProductManager(
			Model model, 
			@RequestParam(required = false) Long brandSmallSortId,
			@RequestParam(required = false) Long brandMiddleSortId,
			@RequestParam(required = false) Long brandBigSortId,
			@RequestParam(required = false) Long brandId,
			@PageableDefault(size = 10) Pageable pageable
			) {
		
		  Page<BrandProduct> products;

		    if (brandSmallSortId != null) {
		        products = brandProductRepository.findAllBySmallSort(
		                pageable,
		                brandSmallSortRepository.findById(brandSmallSortId).orElse(null)
		        );
		    } else if (brandMiddleSortId != null) {
		        products = brandProductRepository.findAllByMiddleSort(
		                pageable,
		                brandMiddleSortRepository.findById(brandMiddleSortId).orElse(null)
		        );
		    } else if (brandBigSortId != null) {
		        products = brandProductRepository.findAllByBigSort(
		                pageable,
		                brandBigSortRepository.findById(brandBigSortId).orElse(null)
		        );
		    } else if (brandId != null) {
		        products = brandProductRepository.findAllByBrand(
		                pageable,
		                brandRepository.findById(brandId).orElse(null)
		        );
		    } else {
		        products = brandProductRepository.findAll(pageable);
		    }

		    int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
		    int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);

		    model.addAttribute("products", products);
		    model.addAttribute("startPage", startPage);
		    model.addAttribute("endPage", endPage);

		    // 선택값 유지용
		    model.addAttribute("brandSmallSortId", brandSmallSortId);
		    model.addAttribute("brandMiddleSortId", brandMiddleSortId);
		    model.addAttribute("brandBigSortId", brandBigSortId);
		    model.addAttribute("brandId", brandId);

		    // 분류 셀렉트박스 채우기
		    model.addAttribute("smallsorts", brandSmallSortRepository.findAll());
		    model.addAttribute("middlesorts", brandMiddleSortRepository.findAll());
		    model.addAttribute("bigsorts", brandBigSortRepository.findAll());
		    model.addAttribute("brand", brandRepository.findAll());

		    return "admin/brand/brandProductManager";
	}
	
	@GetMapping("/brandProductInsertForm")
	public String brandProductInsertForm(Model model) {

		model.addAttribute("brand",brandRepository.findAll());
		
	    BrandProduct product = new BrandProduct();
	    product.setInfos(new ArrayList<>());
	    product.setSpecs(new ArrayList<>());

	    model.addAttribute("product", product);

		return "admin/brand/brandProductInsertForm";
	}

	@PostMapping("/brandProductInsert")
	@ResponseBody
	public String brandProductInsert(
			BrandProduct product, 
			String[] spec, 
			String[] infoQ, 
			String[] infoA,
			MultipartFile productOverviewImage, 
			MultipartFile productSpecImage, 
			List<MultipartFile> slides,
			List<MultipartFile> productFile

	) throws IllegalStateException, IOException {
		
		if (product.getBrandId() == null || product.getBrandBigSortId() == null) {
	        throw new IllegalArgumentException("브랜드와 대분류는 반드시 선택해야 합니다.");
	    }

		
	   if (product.getBrandSmallSortId() != null) {
	        product.setSmallSort(
	            brandSmallSortRepository.findById(product.getBrandSmallSortId())
	                .orElse(null)
	        );
	    } else {
	        product.setSmallSort(null);
	    }

	    // ✅ 중분류도 선택일 경우만 세팅
	    if (product.getBrandMiddleSortId() != null) {
	        product.setMiddleSort(
	            brandMiddleSortRepository.findById(product.getBrandMiddleSortId())
	                .orElse(null)
	        );
	    } else {
	        product.setMiddleSort(null);
	    }
   
	   
	   product.setBigSort(
		        brandBigSortRepository.findById(product.getBrandBigSortId())
		            .orElseThrow(() -> new IllegalArgumentException("대분류는 필수입니다."))
		    );
	   
	   
	   product.setBrand(
		        brandRepository.findById(product.getBrandId())
		            .orElseThrow(() -> new IllegalArgumentException("브랜드는 필수입니다."))
		    );
	   
	   
		BrandProduct p = brandProductService.productInsert(productOverviewImage, productSpecImage, product);

		if (spec != null && spec.length > 0) {
		    for (String s : spec) {
		        if (s != null && !s.trim().isEmpty()) { // 빈 값 필터링
		            BrandProductInfo in = new BrandProductInfo();
		            in.setProduct(product);
		            in.setProductInfoText(s.trim());
		            brandProductInfoRepository.save(in);
		        }
		    }
		}
		
		if (infoQ != null && infoQ.length > 0) {
		    for (int a = 0; a < infoQ.length; a++) {
		        if ((infoQ[a] != null && !infoQ[a].trim().isEmpty()) ||
		            (infoA[a] != null && !infoA[a].trim().isEmpty())) {
		            BrandProductSpec sp = new BrandProductSpec();
		            sp.setProductSpecSubject(infoQ[a] != null ? infoQ[a].trim() : "");
		            sp.setProductSpecContent(infoA[a] != null ? infoA[a].trim() : "");
		            sp.setProduct(product);
		            brandProductSpecRepository.save(sp);
		        }
		    }
		}
		
		
		if (productFile != null && !productFile.isEmpty() && productFile.stream().anyMatch(f -> !f.isEmpty())) {
		    brandProductFileRepository.deleteAllByProductId(product.getId());
		    brandProductFileService.fileUpload(productFile, product.getId(), product.getBrandProductCode());
		}

		if (slides != null && !slides.isEmpty() && slides.stream().anyMatch(f -> !f.isEmpty())) {
		    brandProductImageRepository.deleteAllByProductId(product.getId());
		    brandProductImageService.fileUpload(slides, product.getId(), product.getBrandProductCode());
		}
		StringBuffer sb = new StringBuffer();
		String msg = "제품이 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandProductManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@GetMapping("/brandProductInsert")
	public String redirectInsertToForm() {
	    return "redirect:/admin/brandProductInsertForm";
	}

	@RequestMapping(value = "/brandProductDetail/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String brandProductDetail(
			@PathVariable Long id,
			Model model
			) {
		
		   BrandProduct product = brandProductRepository.findById(id)
			        .orElseThrow(() -> new IllegalArgumentException("해당 제품이 존재하지 않습니다. id=" + id));

		model.addAttribute("brand",brandRepository.findAll());
		
		// ✅ 브랜드 기준으로 대분류 목록 조회
	    if (product.getBrand() != null) {
	        model.addAttribute("bigsorts", brandBigSortRepository.findAllByBrand(product.getBrand()));
	    }

	    // ✅ 대분류 기준으로 중분류 목록 조회 (있을 때만)
	    if (product.getBigSort() != null) {
	        model.addAttribute("middlesorts", brandMiddleSortRepository.findAllByBigSort(product.getBigSort()));
	    }

	    // ✅ 중분류 기준으로 소분류 목록 조회 (있을 때만)
	    if (product.getMiddleSort() != null) {
	        model.addAttribute("smallsorts", brandSmallSortRepository.findAllByMiddleSort(product.getMiddleSort()));
	    }
	    
	    model.addAttribute("product", product);
	    
		return "admin/brand/brandProductDetail";
	}

	
	@GetMapping("/brandProductUpdate/{id}")
	public String brandProductUpdateForm(@PathVariable Long id, Model model) {
	    BrandProduct product = brandProductRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("없는 제품입니다."));

	    model.addAttribute("product", product);
	    model.addAttribute("brand", brandRepository.findAll());
	    model.addAttribute("bigsorts", brandBigSortRepository.findAll());
	    model.addAttribute("middlesorts", brandMiddleSortRepository.findAll());
	    model.addAttribute("smallsorts", brandSmallSortRepository.findAll()); 

	    return "admin/brand/brandProductDetail"; 
	}
	
	@PostMapping("/brandProductUpdate")
	@ResponseBody
	public String brandProductUpdate(
			@ModelAttribute BrandProduct product, 
			RedirectAttributes redirectAttributes,
	        @RequestParam(required = false) MultipartFile productOverviewImage,
	        @RequestParam(required = false) MultipartFile productSpecImage,
	        @RequestParam(required = false) List<MultipartFile> slides,
	        @RequestParam(required = false) List<MultipartFile> productFile,
	        @RequestParam(required = false) String[] spec,
	        @RequestParam(required = false) String[] infoQ,
	        @RequestParam(required = false) String[] infoA,
	        @RequestParam(required = false) Integer[] specOrder,
	        Model model,
	        @PageableDefault(size = 10) Pageable pageable
			) throws IllegalStateException, IOException {
		
		
		
		if(product.getSign() == null) {
			product.setSign(false);
		}
		
		product.setBrand(
			    brandRepository.findById(product.getBrandId())
			        .orElseThrow(() -> new IllegalArgumentException("브랜드는 필수입니다."))
		);
		
		product.setBigSort(
			    brandBigSortRepository.findById(product.getBrandBigSortId())
			        .orElseThrow(() -> new IllegalArgumentException("대분류는 필수입니다."))
		);
		
		if (product.getBrandMiddleSortId() != null) {
		    product.setMiddleSort(
		        brandMiddleSortRepository.findById(product.getBrandMiddleSortId())
		            .orElse(null)
		    );
		} else {
		    product.setMiddleSort(null);
		}
		
		
		if (product.getBrandSmallSortId() != null) {
		    product.setSmallSort(
		        brandSmallSortRepository.findById(product.getBrandSmallSortId())
		            .orElse(null)
		    );
		} else {
		    product.setSmallSort(null);
		}
		
		
		brandProductService.productUpdate(productOverviewImage, productSpecImage, product);
		
		if (spec != null && spec.length > 0) {
		    for (String s : spec) {
		        if (s != null && !s.trim().isEmpty()) { // 빈 값 필터링
		            BrandProductInfo in = new BrandProductInfo();
		            in.setProduct(product);
		            in.setProductInfoText(s.trim());
		            brandProductInfoRepository.save(in);
		        }
		    }
		}
		
		if (infoQ != null && infoQ.length > 0) {
		    List<BrandProductSpec> specList = new ArrayList<>();

		    for (int i = 0; i < infoQ.length; i++) {
		        String q = (infoQ[i] != null) ? infoQ[i].trim() : "";
		        String a = (infoA != null && infoA.length > i && infoA[i] != null) ? infoA[i].trim() : "";

		        // ✅ 완전 빈 항목은 skip
		        if (q.isEmpty() && a.isEmpty()) continue;

		        BrandProductSpec sp = new BrandProductSpec();
		        sp.setProductSpecSubject(q);
		        sp.setProductSpecContent(a);

		        // ✅ specOrder 배열이 있으면 그대로, 없으면 순차
		        if (specOrder != null && specOrder.length > i) {
		            sp.setSpecOrder(specOrder[i]);
		        } else {
		            sp.setSpecOrder(i + 1);
		        }

		        specList.add(sp);
		    }

		    // ✅ 필터 후 남은 항목이 있을 때만 저장
		    if (!specList.isEmpty()) {
		        product.setSpecs(specList);
		        brandProductService.saveSpecsWithOrder(product.getId(), specList);
		    }
		}
		
		
		if(productFile.size()>0 && !productFile.get(0).isEmpty()) {
			brandProductFileRepository.deleteAllByProductId(product.getId());
			brandProductFileService.fileUpload(
				productFile, 
				product.getId(), 
				brandProductRepository.findById(product.getId()).get().getBrandProductCode()
				);
		}
		if(slides.size()>0 && !slides.get(0).isEmpty()) {
			brandProductImageRepository.deleteAllByProductId(product.getId());
			brandProductImageService.fileUpload(
				slides, 
				product.getId(), 
				brandProductRepository.findById(product.getId()).get().getBrandProductCode()
				);
		}

		Page<BrandProduct> products = brandProductRepository.findAll(pageable);
		model.addAttribute("products", products);
		int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
		int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
		
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("smallId", product.getBrandSmallSortId());
		model.addAttribute("bigsorts", brandBigSortRepository.findAll());
		
		 StringBuffer sb = new StringBuffer();
		    String msg = "제품이 수정 되었습니다.";
		    sb.append("<script>")
		      .append("alert('" + msg + "');")
		      .append("location.href='/admin/brandProductManager';")
		      .append("</script>");
		    return sb.toString();
		
	}
	
	@RequestMapping(value = "/brandProductDelete/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandProductDelete(
			@PathVariable Long id
			) {
		
		StringBuffer sb = new StringBuffer();
		try {
			 if (brandProductRepository.existsById(id)) {
		            // 관련 파일 삭제 로직
		            brandProductService.deleteFiles(id);

		            // 제품 삭제
		            brandProductRepository.deleteById(id);

		            String msg = "제품이 삭제 되었습니다.";
		            sb.append("alert('" + msg + "');");
		        } else {
		            String msg = "이미 삭제되었거나 존재하지 않는 제품입니다.";
		            sb.append("alert('" + msg + "');");
		        }
		} catch (Exception e) {
			System.out.println(e);
		}
		
		sb.append("location.href='/admin/brandProductManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping(value = "/brandDetail/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String brandDetail(
			@PathVariable Long id,
			Model model
			) {
		
		model.addAttribute("brand", brandRepository.findById(id).get());
		return "admin/brand/brandDetail";
	}
	
	@RequestMapping(value = "/brandUpdate",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	
	
	@ResponseBody
	public String brandUpdate(
			@ModelAttribute Brand brand,
	        @RequestParam(value = "brandImage", required = false) MultipartFile brandImage,
	        @RequestParam(value = "visualImage", required = false) MultipartFile visualImage,
	        @RequestParam(value = "brandFooterImage", required = false) MultipartFile brandFooterImage
			) throws IOException {
		
		
		Brand saved = brandRepository.findById(brand.getId()).orElse(null);
		if (saved == null) {
			
		    // 원하는 처리: 리다이렉트/메시지/로그 등
		    return "redirect:/admin/brandForm"; // 컨트롤러라면
		}
		
		
		String desc = imageProcessor.processEditorImages(brand.getDesc(), "brand");
		
		if (desc != null) {
		    desc = desc.replaceAll("(?i)<p>(\\s|&nbsp;|<br>|)*</p>", "");
		}
		
		saved.setType(brand.getType());
	    saved.setName(brand.getName());
	    saved.setContent(brand.getContent());
	    saved.setDesc(desc);
	    
	    brandService.applyLogo(saved, brandImage);
	    brandService.applyVisual(saved, visualImage);
	    brandService.applyFooter(saved, brandFooterImage);
		
	    brandRepository.save(saved);

	    StringBuffer sb = new StringBuffer();
	    sb.append("<script>")
	      .append("alert('수정이 완료 되었습니다.');")
	      .append("location.href='/admin/brandManager'")
	      .append("</script>");
	    return sb.toString();
	}
	
	
	@RequestMapping(
		    value = "/brandProductSpecDelete/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE}
		)
		@ResponseBody
		public String brandProductSpecDelete(@PathVariable Long id) {
		    StringBuffer sb = new StringBuffer();
		    try {
		        if (brandProductSpecRepository.existsById(id)) {
		            brandProductService.deleteSpecById(id);
		            String msg = "선택한 스펙이 삭제되었습니다.";
		            sb.append("alert('" + msg + "');");
		        } else {
		            String msg = "이미 삭제되었거나 존재하지 않는 스펙입니다.";
		            sb.append("alert('" + msg + "');");
		        }
		    } catch (Exception e) {
		        System.out.println("❌ 스펙 삭제 오류: " + e.getMessage());
		        sb.append("alert('삭제 중 오류가 발생했습니다.');");
		    }

		    sb.append("</script>");
		    sb.insert(0, "<script>");
		    return sb.toString();
		}
	
}
















