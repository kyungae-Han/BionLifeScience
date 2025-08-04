package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductFileService;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductImageService;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductService;
import com.dev.BionLifeScienceWeb.service.program.brand.BrandCheckService;
import com.dev.BionLifeScienceWeb.service.program.brand.BrandExcelDownloadService;
import com.dev.BionLifeScienceWeb.service.program.brand.BrandExcelUploadService;
import com.dev.BionLifeScienceWeb.service.program.brand.BrandZipService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/brandCenter")
@RequiredArgsConstructor
public class BrandManageController {

	private final BrandBigSortRepository brandBigSortRepository;
	private final BrandMiddleSortRepository brandMiddleSortRepository;
	private final BrandSmallSortRepository brandSmallSortRepository;
	private final BrandProductRepository brandProductRepository;
	private final BrandRepository brandRepository;
	private final BrandProductImageRepository brandProductImageRepository;
	private final BrandProductFileRepository brandProductFileRepository;
	private final BrandProductFileService brandProductFileService;
	private final BrandProductService brandProductService;
	private final BrandExcelDownloadService brandExcelDownloadService;
	private final BrandExcelUploadService brandExcelUploadService;
	private final BrandZipService brandZipService;
	private final BrandCheckService brandExcelCheckService;
	private final BrandProductImageService brandProductImageService;
	
	@GetMapping("/addExcelDownload")
	@ResponseBody
	public void addExcelDownload(
			HttpServletResponse res
			) throws IOException {
		brandExcelDownloadService.productAddSheetDownload(res);
	}
	
	
	@GetMapping("/resetExcelDownload")
	@ResponseBody
	public void resetExcelDownload(
			HttpServletResponse res
			) throws IOException {
		brandExcelDownloadService.bigSortDownload(res);
	}

	@GetMapping("/resetZipDownload")
	@ResponseBody
	public ResponseEntity<Object> resetZipDownload() {
		return brandZipService.downZip();
		
	}
	
	@PostMapping("/addExcelUpload")
	@ResponseBody
	public List<String> addExcelUpload(
			MultipartFile file,
			Model model
			) throws IOException {
		
		List<String> result = brandExcelCheckService.addExcelCheck(file);
		
		if(result.get(0).equals("success")){
			brandExcelUploadService.uploadAddExcel(file);
			
			return result;
		}else {
			return result;
		}
	}
	
	@PostMapping("/resetExcelUpload")
	@ResponseBody
	public List<String> resetExcelUpload(
			MultipartFile file,
			Model model
			) throws IOException {
		List<String> result = brandExcelCheckService.resetExcelCheck(file);
		
		if(result.get(0).equals("success")){
			
			brandExcelUploadService.uploadExcel(file);
			return result;
		}else {
			return result;
		}
	}
	
	@PostMapping("/addZipUpload")
	@ResponseBody
	public List<String> addZipUpload(
			MultipartFile file,
			Model model
			) throws IOException {
		
		List<String> result = brandExcelCheckService.addZipCheck(file);
		if(result.get(0).equals("success")){
			
			brandProductService.zipAddProductInsert(file);
			return result;
		}else {
			return result;
		}
	}
	
	@PostMapping("/resetZipUpload")
	@ResponseBody
	public List<String> resetZipUpload(
			MultipartFile file,
			Model model
			) throws IOException {
		
		
		List<String> result = brandExcelCheckService.resetZipCheck(file);
		if(result.get(0).equals("success")){
			brandProductService.zipProductInsert(file);
			return result;
		}else {
			return result;
		}
	}
	
	@GetMapping("/productManager")
	public String productManager(
			Model model, 
			@RequestParam(required = false) Long smallId,
			@RequestParam(required = false) Long middleId,
			@RequestParam(required = false) Long bigId,
			@RequestParam(required = false) Long brandId,
			@RequestParam(required = false, defaultValue = "") String searchWord,
			@PageableDefault(size = 10) Pageable pageable
			) {
		Page<BrandProduct> products = brandProductRepository.findAllBySubjectContainsOrderByIdDesc(pageable, searchWord);
		if (smallId != null) {
			products = brandProductRepository.findAllBySmallSortAndSubjectContainsOrderByIdDesc(pageable,
					brandSmallSortRepository.findById(smallId).get(), searchWord);
		
			model.addAttribute("smallsorts", brandSmallSortRepository.findAllByMiddleSort(brandMiddleSortRepository.findById(middleId).get()));
			model.addAttribute("middlesorts", brandMiddleSortRepository.findAll());
			model.addAttribute("bigsorts", brandBigSortRepository.findAll());
		
		}else if(smallId == null && middleId != null){
			Optional<BrandMiddleSort> m = brandMiddleSortRepository.findById(middleId);
			products = brandProductRepository.findAllByMiddleSortAndSubjectContainsOrderByIdDesc(pageable, m.get(), searchWord);
			
			model.addAttribute("middlesorts", brandMiddleSortRepository.findAllByBigSort(brandBigSortRepository.findById(bigId).get()));
			model.addAttribute("smallsorts", brandSmallSortRepository.findAllByMiddleSort(m.get()));
			model.addAttribute("bigsorts", brandBigSortRepository.findAll());
			
		}else if(smallId == null && middleId == null && bigId != null) {
			Optional<BrandBigSort> b = brandBigSortRepository.findById(bigId);
			products = brandProductRepository.findAllByBigSortAndSubjectContainsOrderByIdDesc(pageable, b.get(), searchWord);
			
			model.addAttribute("bigsorts", brandBigSortRepository.findAllByBrand(brandRepository.findById(brandId).get()));
			model.addAttribute("middlesorts", brandMiddleSortRepository.findAllByBigSort(b.get()));
			
			
		}else if(smallId == null && middleId == null && bigId == null && brandId != null){
			Optional<Brand> b = brandRepository.findById(brandId);
			products = brandProductRepository.findAllByBrandAndSubjectContainsOrderByIdDesc(pageable, b.get(), searchWord);
			model.addAttribute("bigsorts", brandBigSortRepository.findAllByBrand(brandRepository.findById(brandId).get()));
			
		}else {
			products = brandProductRepository.findAllBySubjectContainsOrderByIdDesc(pageable, searchWord);
		}
		int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
		int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
		
		model.addAttribute("brands", brandRepository.findAll());
		model.addAttribute("products", products);
		model.addAttribute("middleId", middleId);
		model.addAttribute("smallId", smallId);
		model.addAttribute("bigId", bigId);
		model.addAttribute("brandId", brandId);
		model.addAttribute("searchWord", searchWord);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		return "program/brand/brandProductManager";
	}
	
	@GetMapping("/productOverall")
	public String productOverall(	
			Model model, 
			@RequestParam(required = false) Long brandId,
			@RequestParam(required = false) Long smallId,
			@RequestParam(required = false) Long middleId,
			@RequestParam(required = false) Long bigId,
			@PageableDefault(size = 10) Pageable pageable
			) {
		int startPage = 0;
		int endPage = 0;
		if (smallId != null) {
			Page<BrandProduct> products = brandProductRepository.findAllBySmallSortOrderByBrandProductIndexAsc(
					pageable,
					brandSmallSortRepository.findById(smallId).get()
					);
			model.addAttribute("sortable", products);
			startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
			endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
			model.addAttribute("smallsorts", brandSmallSortRepository.findAllByMiddleSort(brandMiddleSortRepository.findById(middleId).get()));
			model.addAttribute("middlesorts", brandMiddleSortRepository.findAll());
			model.addAttribute("bigsorts", brandBigSortRepository.findAll());
			model.addAttribute("code", "product");
			
		}else if(smallId == null && middleId != null){
			Optional<BrandMiddleSort> m = brandMiddleSortRepository.findById(middleId);
			Page<BrandSmallSort> smallSorts = brandSmallSortRepository.findAllByMiddleSortOrderByBrandSmallSortIndexAsc(pageable, m.get());
			model.addAttribute("sortable", smallSorts);
			startPage = Math.max(1, smallSorts.getPageable().getPageNumber() - 4);
			endPage = Math.min(smallSorts.getTotalPages(), smallSorts.getPageable().getPageNumber() + 4);
			model.addAttribute("middlesorts", brandMiddleSortRepository.findAllByBigSort(brandBigSortRepository.findById(bigId).get()));
			model.addAttribute("smallsorts", brandSmallSortRepository.findAllByMiddleSort(m.get()));
			model.addAttribute("bigsorts", brandBigSortRepository.findAll());
			model.addAttribute("code", "smallsort");
			
		}else if(smallId == null && middleId == null && bigId != null) {
			
			Optional<BrandBigSort> b = brandBigSortRepository.findById(bigId);
			Page<BrandMiddleSort> middleSorts = brandMiddleSortRepository.findAllByBigSortOrderByBrandMiddleSortIndexAsc(pageable, b.get());
			model.addAttribute("sortable", middleSorts);
			startPage = Math.max(1, middleSorts.getPageable().getPageNumber() - 4);
			endPage = Math.min(middleSorts.getTotalPages(), middleSorts.getPageable().getPageNumber() + 4);
			model.addAttribute("bigsorts", brandBigSortRepository.findAllByBrand(brandRepository.findById(brandId).get()));
			model.addAttribute("middlesorts", brandMiddleSortRepository.findAllByBigSort(b.get()));
			model.addAttribute("code", "middlesort");
			
		}else if(smallId == null && middleId == null && bigId == null && brandId != null){
			
			Optional<Brand> b = brandRepository.findById(brandId);
			Page<BrandBigSort> bigSorts = brandBigSortRepository.findAllByBrandOrderByBrandBigSortIndexAsc(pageable, b.get());
			model.addAttribute("sortable", bigSorts);
			startPage = Math.max(1, bigSorts.getPageable().getPageNumber() - 4);
			endPage = Math.min(bigSorts.getTotalPages(), bigSorts.getPageable().getPageNumber() + 4);
			model.addAttribute("bigsorts", brandBigSortRepository.findAllByBrand(brandRepository.findById(brandId).get()));
			model.addAttribute("code", "bigsort");
		}else {
			Page<Brand> brands =  brandRepository.findAllByOrderByBrandIndexAsc(pageable);
			startPage = Math.max(1, brands.getPageable().getPageNumber() - 4);
			endPage = Math.min(brands.getTotalPages(), brands.getPageable().getPageNumber() + 4);
			model.addAttribute("sortable", brands);
			model.addAttribute("code", "brand");
		
		}
		
		model.addAttribute("middleId", middleId);
		model.addAttribute("smallId", smallId);
		model.addAttribute("bigId", bigId);
		model.addAttribute("brandId", brandId);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("brands", brandRepository.findAll());
		return "program/brand/brandProductOverall";
	}
	
	@GetMapping("/productAddManager")
	public String productAddManager() {
		return "program/brand/brandProductAddManager";
	}
	
	@PostMapping("/productFileInsert")
	public String productFileInsert(
			Long productIdInput,
			String productCodeInput,
			List<MultipartFile> file
			
			) throws IllegalStateException, IOException {
		if(productCodeInput.equals("slide")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				brandProductImageRepository.deleteAllByProductId(productIdInput);
				brandProductImageService.fileUpload(
						file, 
						productIdInput,
						brandProductRepository.findById(productIdInput).get().getBrandProductCode());
			}
		}else if(productCodeInput.equals("files")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				brandProductFileRepository.deleteAllByProductId(productIdInput);
				brandProductFileService.fileUpload(
						file, 
						productIdInput, 
						brandProductRepository.findById(productIdInput).get().getBrandProductCode());
			}
		}else if(productCodeInput.equals("spec")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				brandProductService.productSpec(file.get(0),brandProductRepository.findById(productIdInput).get());
			}
			
		}else if(productCodeInput.equals("overview")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				brandProductService.productOverview(file.get(0),brandProductRepository.findById(productIdInput).get());
			}
		}
		return "redirect:/admin/brandCenter/productManager";
	}
	
	@GetMapping("/productResetManager")
	public String productResetManager() {
		return "program/brand/brandProductResetManager";
	}
	
	@PostMapping("/changeIndex")
	public String changeIndex(
			@RequestParam(value="exIndex[]") Long[] exIndex,
			@RequestParam(value="sortableIndex[]") Long[] sortableIndex,
			String code
			) {
		if(code.equals("brand")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = brandRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getBrandIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				brandRepository.findById(exIndex[b]).ifPresent(n->{
					n.setBrandIndex(afterValue);
					brandRepository.save(n);
				});
			}
			
			
		}else if(code.equals("bigsort")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = brandBigSortRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getBrandBigSortIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				brandBigSortRepository.findById(exIndex[b]).ifPresent(n->{
					n.setBrandBigSortIndex(afterValue);
					brandBigSortRepository.save(n);
				});
			}
		}else if(code.equals("middlesort")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = brandMiddleSortRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getBrandMiddleSortIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				brandMiddleSortRepository.findById(exIndex[b]).ifPresent(n->{
					n.setBrandMiddleSortIndex(afterValue);
					brandMiddleSortRepository.save(n);
				});
			}
			
		}else if(code.equals("smallsort")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = brandSmallSortRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getBrandSmallSortIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				brandSmallSortRepository.findById(exIndex[b]).ifPresent(n->{
					n.setBrandSmallSortIndex(afterValue);
					brandSmallSortRepository.save(n);
				});
			}
			
		}else if(code.equals("product")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = brandProductRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getBrandProductIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				brandProductRepository.findById(exIndex[b]).ifPresent(n->{
					n.setBrandProductIndex(afterValue);
					brandProductRepository.save(n);
				});
			}
			
		}
		return "redirect:/admin/brandCenter/productOverall";
	}
}
