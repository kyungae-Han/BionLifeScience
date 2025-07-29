package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.dev.BionLifeScienceWeb.model.product.BigSort;
import com.dev.BionLifeScienceWeb.model.product.MiddleSort;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;
import com.dev.BionLifeScienceWeb.service.ZipService;
import com.dev.BionLifeScienceWeb.service.product.ProductFileService;
import com.dev.BionLifeScienceWeb.service.product.ProductImageService;
import com.dev.BionLifeScienceWeb.service.product.ProductService;
import com.dev.BionLifeScienceWeb.service.program.company.CheckService;
import com.dev.BionLifeScienceWeb.service.program.company.ExcelDownloadService;
import com.dev.BionLifeScienceWeb.service.program.company.ExcelUploadService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin/productCenter")
public class ProductManageController {

	@Autowired
	BigSortRepository bigSortRepository;

	@Autowired
	MiddleSortRepository middleSortRepository;

	@Autowired
	SmallSortRepository smallSortRepository;

	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	ProductFileService productFileService;

	@Autowired
	ProductImageService productImageService;
	
	@Autowired
	ProductImageRepository productImageRepository;
	
	@Autowired
	ProductFileRepository productFileRepository;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	ExcelDownloadService excelDownloadService;
	
	@Autowired
	ExcelUploadService excelUploadService;
	
	@Autowired
	ZipService zipService;
	
	@Autowired
	CheckService excelCheckService;
	
	@GetMapping("/addExcelDownload")
	@ResponseBody
	public void addExcelDownload(
			HttpServletResponse res
			) throws IOException {
		excelDownloadService.productAddSheetDownload(res);
		
	}
	

	@GetMapping("/resetExcelDownload")
	@ResponseBody
	public void resetExcelDownload(
			HttpServletResponse res
			) throws IOException {
		excelDownloadService.bigSortDownload(res);
		
	}
	
	@PostMapping("/addExcelUpload")
	@ResponseBody
	public List<String> addExcelUpload(
			MultipartFile file,
			Model model
			) throws IOException {
		List<String> result = excelCheckService.addExcelCheck(file);
		
		if(result.get(0).equals("success")){
			excelUploadService.uploadAddExcel(file);
			
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
		List<String> result = excelCheckService.resetExcelCheck(file);
		if(result.get(0).equals("success")){
			excelUploadService.uploadExcel(file);
			
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
		List<String> result = excelCheckService.resetZipCheck(file);
		if(result.get(0).equals("success")){
			productService.zipProductInsert(file);
			return result;
		}else {
			return result;
		}
	}
	
	@PostMapping("/addZipUpload")
	@ResponseBody
	public List<String> addZipUpload(
			MultipartFile file
			) throws IOException {
		List<String> result = excelCheckService.addZipCheck(file);
		if(result.get(0).equals("success")){
			productService.zipAddProductInsert(file);
			return result;
		}else {
			return result;
		}
	}
	
	
	@GetMapping("/resetZipDownload")
	@ResponseBody
	public ResponseEntity<Object> resetZipDownload() {
		return zipService.downZip();
		
	}
	
	
	
	
	@GetMapping("/productManager")
	public String productManager(
			Model model, 
			@RequestParam(required = false) Long smallId,
			@RequestParam(required = false) Long middleId,
			@RequestParam(required = false) Long bigId,
			@RequestParam(required = false, defaultValue = "") String searchWord,
			@PageableDefault(size = 10) Pageable pageable
			) {
		Page<Product> products = productRepository.findAllBySubjectContainsOrderByIdDesc(pageable, searchWord);
		if (smallId != null) {
			products = productRepository.findAllBySmallSortAndSubjectContainsOrderByIdDesc(pageable,
					smallSortRepository.findById(smallId).get(), searchWord);
			model.addAttribute("smallsorts", smallSortRepository.findAll());
			model.addAttribute("middlesorts", middleSortRepository.findAll());
		
		}else if(smallId == null && middleId != null){
			Optional<MiddleSort> m = middleSortRepository.findById(middleId);
			products = productRepository.findAllByMiddleSortAndSubjectContainsOrderByIdDesc(pageable, m.get(), searchWord);
			model.addAttribute("middlesorts", middleSortRepository.findAll());
			model.addAttribute("smallsorts", smallSortRepository.findAllByMiddleSort(m.get()));
			
		}else if(smallId == null && middleId == null && bigId != null) {
			Optional<BigSort> b = bigSortRepository.findById(bigId);
			products = productRepository.findAllByBigSortAndSubjectContainsOrderByIdDesc(pageable, b.get(), searchWord);
			model.addAttribute("middlesorts", middleSortRepository.findAllByBigSort(b.get()));
		}else {
			products = productRepository.findAllBySubjectContainsOrderByIdDesc(pageable, searchWord);
		}
		int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
		int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
		model.addAttribute("products", products);
		model.addAttribute("middleId", middleId);
		model.addAttribute("smallId", smallId);
		model.addAttribute("bigId", bigId);
		model.addAttribute("searchWord", searchWord);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("bigsorts", bigSortRepository.findAll());
		return "program/company/productManager";
	}
	
	@GetMapping("/productOverall")
	public String productOverall(	
			Model model, 
			@RequestParam(required = false) Long smallId,
			@RequestParam(required = false) Long middleId,
			@RequestParam(required = false) Long bigId,
			@PageableDefault(size = 10) Pageable pageable
			) {
		int startPage = 0;
		int endPage = 0;
		if (smallId != null) {
			Page<Product> products = productRepository.findAllBySmallSortOrderByProductIndexAsc(
					pageable,
					smallSortRepository.findById(smallId).get()
					);
			model.addAttribute("sortable", products);
			startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
			endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
			model.addAttribute("smallsorts", smallSortRepository.findAll());
			model.addAttribute("middlesorts", middleSortRepository.findAll());
			model.addAttribute("code", "product");
			
		}else if(smallId == null && middleId != null){
			Optional<MiddleSort> m = middleSortRepository.findById(middleId);
			Page<SmallSort> smallSorts = smallSortRepository.findAllByMiddleSortOrderBySmallSortIndexAsc(pageable, m.get());
			model.addAttribute("sortable", smallSorts);
			startPage = Math.max(1, smallSorts.getPageable().getPageNumber() - 4);
			endPage = Math.min(smallSorts.getTotalPages(), smallSorts.getPageable().getPageNumber() + 4);
			model.addAttribute("middlesorts", middleSortRepository.findAll());
			model.addAttribute("smallsorts", smallSortRepository.findAllByMiddleSort(m.get()));
			model.addAttribute("code", "smallsort");
			
		}else if(smallId == null && middleId == null && bigId != null) {
			
			Optional<BigSort> b = bigSortRepository.findById(bigId);
			Page<MiddleSort> middleSorts = middleSortRepository.findAllByBigSortOrderByMiddleSortIndexAsc(pageable, b.get());
			model.addAttribute("sortable", middleSorts);
			startPage = Math.max(1, middleSorts.getPageable().getPageNumber() - 4);
			endPage = Math.min(middleSorts.getTotalPages(), middleSorts.getPageable().getPageNumber() + 4);
			model.addAttribute("middlesorts", middleSortRepository.findAllByBigSort(b.get()));
			model.addAttribute("code", "middlesort");
			
		}else {
			Page<BigSort> bigSorts =  bigSortRepository.findAllByOrderByBigSortIndexAsc(pageable);
			startPage = Math.max(1, bigSorts.getPageable().getPageNumber() - 4);
			endPage = Math.min(bigSorts.getTotalPages(), bigSorts.getPageable().getPageNumber() + 4);
			model.addAttribute("sortable", bigSorts);
			model.addAttribute("code", "bigsort");
			
		
		}
		
		model.addAttribute("middleId", middleId);
		model.addAttribute("smallId", smallId);
		model.addAttribute("bigId", bigId);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("bigsorts", bigSortRepository.findAll());
		return "program/company/productOverall";
	}
	
	@GetMapping("/productAddManager")
	public String productAddManager() {
		
		return "program/company/productAddManager";
	}
	
	@PostMapping("/productFileInsert")
	public String productFileInsert(
			Long productIdInput,
			String productCodeInput,
			List<MultipartFile> file
			
			) throws IllegalStateException, IOException {
		if(productCodeInput.equals("slide")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				productImageRepository.deleteAllByProductId(productIdInput);
				productImageService.fileUpload(
						file, 
						productIdInput,
						productRepository.findById(productIdInput).get().getProductCode());
			}
		}else if(productCodeInput.equals("files")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				productFileRepository.deleteAllByProductId(productIdInput);
				productFileService.fileUpload(
						file, 
						productIdInput, 
						productRepository.findById(productIdInput).get().getProductCode());
			}
		}else if(productCodeInput.equals("spec")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				productService.productSpec(file.get(0),productRepository.findById(productIdInput).get());
			}
			
		}else if(productCodeInput.equals("overview")) {
			if(file.size()>0 && !file.get(0).isEmpty()) {
				productService.productOverview(file.get(0),productRepository.findById(productIdInput).get());
			}
		}
		
		return "redirect:productManager";
	}
	
	@GetMapping("/productResetManager")
	public String productResetManager() {
		
		return "program/company/productResetManager";
	}
	
	
	@PostMapping("/changeIndex")
	public String changeIndex(
			@RequestParam(value="exIndex[]") Long[] exIndex,
			@RequestParam(value="sortableIndex[]") Long[] sortableIndex,
			String code
			) {
		if(code.equals("bigsort")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = bigSortRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getBigSortIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				bigSortRepository.findById(exIndex[b]).ifPresent(n->{
					n.setBigSortIndex(afterValue);
					bigSortRepository.save(n);
				});
			}
		}else if(code.equals("middlesort")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = middleSortRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getMiddleSortIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				middleSortRepository.findById(exIndex[b]).ifPresent(n->{
					n.setMiddleSortIndex(afterValue);
					middleSortRepository.save(n);
				});
			}
			
		}else if(code.equals("smallsort")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = smallSortRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getSmallSortIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				smallSortRepository.findById(exIndex[b]).ifPresent(n->{
					n.setSmallSortIndex(afterValue);
					smallSortRepository.save(n);
				});
			}
			
		}else if(code.equals("product")) {
			int[] finalIndex = new int[sortableIndex.length];
			for(int a=0; a<sortableIndex.length; a++) {
				
				int after = productRepository.findById(exIndex[Arrays.asList(sortableIndex).indexOf(exIndex[a])]).get().getProductIndex();
				finalIndex[a] = after;
			}
			for(int b=0; b<finalIndex.length; b++) {
				int afterValue = finalIndex[b];
				productRepository.findById(exIndex[b]).ifPresent(n->{
					n.setProductIndex(afterValue);
					productRepository.save(n);
				});
			}
			
		}
		return "redirect:productOverall";
	}
}
