package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BrandController {

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
	        @RequestParam(value = "visualImage", required = false) MultipartFile visualImage
	) throws IOException {

	    if (brand.getType() == null) {
	        return "<script>alert('브랜드 타입을 선택하세요.');history.back();</script>";
	    }
	    
	    if (brandImage == null || brandImage.isEmpty()) {
	        return "<script>alert('로고 파일은 필수입니다.');history.back();</script>";
	    }

	    brandService.brandInsert(brandImage, visualImage, brand); // ✅ 세 개 넘김

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
		
		if (brandSmallSortId != null) {
			Page<BrandProduct> products = brandProductRepository.findAllBySmallSort(pageable,
					brandSmallSortRepository.findById(brandSmallSortId).get());
			int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
			int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
			model.addAttribute("products", products);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
			model.addAttribute("brandSmallSortId", brandSmallSortId);
			model.addAttribute("smallsorts", brandSmallSortRepository.findAll());
			model.addAttribute("middlesorts", brandMiddleSortRepository.findAll());
			model.addAttribute("bigsorts", brandBigSortRepository.findAll());
			model.addAttribute("smallId", brandSmallSortId);
			model.addAttribute("middleId", brandMiddleSortId);
			model.addAttribute("bigId", brandBigSortId);
			model.addAttribute("brandId", brandId);
		}else {
			Page<BrandProduct> products = brandProductRepository.findAll(pageable);
			int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
			int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
			model.addAttribute("products", products);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
		}
		
		model.addAttribute("brand",brandRepository.findAll());

		return "admin/brand/brandProductManager";
	}
	
	@GetMapping("/brandProductInsertForm")
	public String brandProductInsertForm(Model model) {

		model.addAttribute("brand",brandRepository.findAll());

		return "admin/brand/brandProductInsertForm";
	}

	@RequestMapping(value = "/brandProductInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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
		
		product.setSmallSort(brandSmallSortRepository.findById(product.getBrandSmallSortId()).get());
		product.setMiddleSort(brandMiddleSortRepository.findById(product.getBrandMiddleSortId()).get());
		product.setBigSort(brandBigSortRepository.findById(product.getBrandBigSortId()).get());
		product.setBrand(brandRepository.findById(product.getBrandId()).get());
		BrandProduct p = brandProductService.productInsert(productOverviewImage, productSpecImage, product);

		if(spec.length > 0 ) {
			for (String s : spec) {
				BrandProductInfo in = new BrandProductInfo();
				in.setProductId(p.getId());
				in.setProductInfoText(s);
				brandProductInfoRepository.save(in);
			}
		}else {
			BrandProductInfo in = new BrandProductInfo();
			in.setProductId(p.getId());
			in.setProductInfoText("-");
			brandProductInfoRepository.save(in);
		}
		
		if(infoQ.length > 0) {
			
			for (int a = 0; a < infoQ.length; a++) {
				BrandProductSpec sp = new BrandProductSpec();
				sp.setProductSpecSubject(infoQ[a]);
				sp.setProductSpecContent(infoA[a]);
				sp.setProductId(p.getId());
				brandProductSpecRepository.save(sp);
			}
		}else {
			BrandProductSpec sp = new BrandProductSpec();
			sp.setProductSpecSubject("-");
			sp.setProductSpecContent("-");
			sp.setProductId(p.getId());
			brandProductSpecRepository.save(sp);
		}
		if(!productFile.isEmpty()) {
			brandProductFileService.fileUpload(
				productFile, 
				p.getId(), 
				p.getBrandProductCode()
				);
		}
		if(!slides.isEmpty()) {
			brandProductImageService.fileUpload(
				slides, 
				p.getId(), 
				p.getBrandProductCode()
				);
		}

		StringBuffer sb = new StringBuffer();
		String msg = "제품이 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/brandProductManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}

	@RequestMapping(value = "/brandProductDetail/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String brandProductDetail(
			@PathVariable Long id,
			Model model
			) {
		model.addAttribute("brand",brandRepository.findAll());
		model.addAttribute("bigsorts", brandBigSortRepository.findAllByBrand(brandProductRepository.findById(id).get().getSmallSort().getMiddleSort().getBigSort().getBrand()));
		model.addAttribute("middlesorts", brandMiddleSortRepository.findAllByBigSort(brandProductRepository.findById(id).get().getSmallSort().getMiddleSort().getBigSort()));
		model.addAttribute("smallsorts", brandSmallSortRepository.findAllByMiddleSort(brandProductRepository.findById(id).get().getSmallSort().getMiddleSort()));
		model.addAttribute("product",brandProductRepository.findById(id).get());
		return "admin/brand/brandProductDetail";
	}

	@RequestMapping(value = "/brandProductUpdate",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String brandProductUpdate(
			Model model, 
			@PageableDefault(size = 10) Pageable pageable,
			BrandProduct product, 
			String[] spec, 
			String[] infoQ, 
			String[] infoA,
			MultipartFile productOverviewImage, 
			MultipartFile productSpecImage, 
			List<MultipartFile> slides,
			List<MultipartFile> productFile
			) throws IllegalStateException, IOException {
		if(product.getSign() == null) {
			product.setSign(false);
		}
		product.setSmallSort(brandSmallSortRepository.findById(product.getBrandSmallSortId()).get());
		product.setMiddleSort(brandMiddleSortRepository.findById(product.getBrandMiddleSortId()).get());
		product.setBigSort(brandBigSortRepository.findById(product.getBrandBigSortId()).get());
		product.setBrand(brandRepository.findById(product.getBrandId()).get());
		brandProductService.productUpdate(productOverviewImage, productSpecImage, product);
		
		brandProductInfoRepository.deleteAllByProductId(product.getId());
		brandProductSpecRepository.deleteAllByProductId(product.getId());
		
		if(spec.length > 0 && spec != null) {
			for (String s : spec) {
				BrandProductInfo in = new BrandProductInfo();
				in.setProductId(product.getId());
				in.setProductInfoText(s);
				brandProductInfoRepository.save(in);
			}
		}else {
			BrandProductInfo in = new BrandProductInfo();
			in.setProductId(product.getId());
			in.setProductInfoText("-");
			brandProductInfoRepository.save(in);
		}
		
		if(infoQ.length > 0 && infoQ != null) {
			
			for (int a = 0; a < infoQ.length; a++) {
				BrandProductSpec sp = new BrandProductSpec();
				sp.setProductSpecSubject(infoQ[a]);
				sp.setProductSpecContent(infoA[a]);
				sp.setProductId(product.getId());
				brandProductSpecRepository.save(sp);
			}
		}else {
			BrandProductSpec sp = new BrandProductSpec();
			sp.setProductSpecSubject("-");
			sp.setProductSpecContent("-");
			sp.setProductId(product.getId());
			brandProductSpecRepository.save(sp);
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

		return "admin/brand/brandProductManager";
	}
	
	@RequestMapping(value = "/brandProductDelete/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String brandProductDelete(
			@PathVariable Long id
			) {
		
		try {
			brandProductService.deleteFiles(id);
			brandProductRepository.deleteById(id);
		} catch (Exception e) {
			System.out.println(e);
		}
		StringBuffer sb = new StringBuffer();
		String msg = "제품이 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
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
	        @RequestParam(value = "visualImage", required = false) MultipartFile visualImage
			) throws IOException {
		
		Brand saved = brandRepository.findById(brand.getId()).orElse(null);
		if (saved == null) {
			
		    // 원하는 처리: 리다이렉트/메시지/로그 등
		    return "redirect:/admin/brandForm"; // 컨트롤러라면
		}
		
		
		saved.setType(brand.getType());
	    saved.setName(brand.getName());
	    saved.setContent(brand.getContent());
	    saved.setDesc(brand.getDesc());
	    
	    brandService.applyLogo(saved, brandImage);
	    brandService.applyVisual(saved, visualImage);
		
	    brandRepository.save(saved);

	    StringBuffer sb = new StringBuffer();
	    sb.append("<script>")
	      .append("alert('수정이 완료 되었습니다.');")
	      .append("location.href='/admin/brandManager'")
	      .append("</script>");
	    return sb.toString();
	}
	
	@Value("${spring.upload.path}")
	private String commonPath;

	@Value("${spring.upload.env}")
	private String env;
	
	@PostMapping("/admin/uploadEditorImage")
	@ResponseBody
	public String uploadEditorImage(@RequestParam("file") MultipartFile file) throws IOException {
	    // 저장 경로 (예: /upload/editor/yyyy-MM-dd)
	    String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	    String dirPath = commonPath + "/brand/editor/" + today;
	    String webPath = "/administration/brand/editor/" + today;

	    File dir = new File(dirPath);
	    if (!dir.exists()) dir.mkdirs();

	    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
	    File dest = new File(dir, fileName);
	    file.transferTo(dest);

	    return webPath + "/" + fileName;
	}

	
}
















