package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.exception.DeleteViolationException;
import com.dev.BionLifeScienceWeb.model.product.BigSort;
import com.dev.BionLifeScienceWeb.model.product.MiddleSort;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.ProductInfo;
import com.dev.BionLifeScienceWeb.model.product.ProductSpec;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductInfoRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductSpecRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;
import com.dev.BionLifeScienceWeb.service.product.ProductFileService;
import com.dev.BionLifeScienceWeb.service.product.ProductImageService;
import com.dev.BionLifeScienceWeb.service.product.ProductService;

@RequestMapping("/admin")
@Controller
public class ProductController {

	@Autowired
	BigSortRepository bigSortRepository;

	@Autowired
	MiddleSortRepository middleSortRepository;

	@Autowired
	SmallSortRepository smallSortRepository;

	@Autowired
	ProductService productService;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	ProductImageRepository productImageRepository;

	@Autowired
	ProductFileRepository productFileRepository;

	@Autowired
	ProductSpecRepository productSpecRepository;

	@Autowired
	ProductInfoRepository productInfoRepository;

	@Autowired
	ProductFileService productFileService;

	@Autowired
	ProductImageService productImageService;

	@Value("${spring.upload.path}")
	private String commonPath;
	
	@RequestMapping("/sortManager")
	public String sortManager(Model model) {
		List<BigSort> b = bigSortRepository.findAll();
//		System.out.println(b.size());
		if(b.size()<1) {
			BigSort bs = new BigSort();
			bs.setName("분류를 등록 해 주세요");
			bs.setId(0L);
			b.add(bs);
		}
		model.addAttribute("bigsorts", b);
		
		return "admin/product/sortManager";
	}

	@RequestMapping("/bigsortInsert")
	public String bigsortInsert(BigSort bigSort, Model model) {
		int index = 1;
		if(bigSortRepository.findFirstIndex().isPresent()) {
			index = bigSortRepository.findFirstIndex().get() + 1;
		}
		bigSort.setBigSortIndex(index);
		bigSortRepository.save(bigSort);

		return "redirect:/admin/sortManager";
	}

	@RequestMapping("/bigSortDelete")
	public String bigSortDelete(
			@RequestParam(value = "text[]") Long[] text, 
			Model model) {
		try {
			for (Long id : text) {
				bigSortRepository.deleteById(id);
			}
		}catch(DeleteViolationException e) {
			throw new DeleteViolationException();
		}	
			
		
		return "redirect:/admin/sortManager";
	}

	@RequestMapping("/middlesortInsert")
	public String middlesortInsert(
			MiddleSort middleSort, 
			Model model, 
			Long bigId
			) {

		int index = 1;
		if(middleSortRepository.findFirstIndex().isPresent()) {
			index = middleSortRepository.findFirstIndex().get() + 1;
		}
		middleSort.setMiddleSortIndex(index);
		
		middleSort.setBigSort(bigSortRepository.findById(bigId).get());
		middleSortRepository.save(middleSort);
		return "redirect:/admin/sortManager";
	}

	@RequestMapping("/searchMiddleSort")
	@ResponseBody
	public List<MiddleSort> searchMiddleSort(Model model, Long bigId) {

		return middleSortRepository.findAllByBigSort(bigSortRepository.findById(bigId).get());
	}

	@RequestMapping("/middleSortDelete")
	public String middleSortDelete(@RequestParam(value = "text[]") Long[] text, Model model, Long bigId) {
		
		try {
			for (Long id : text) {
				middleSortRepository.deleteById(id);
			}
		}catch(DeleteViolationException e) {
			throw new DeleteViolationException();
		}	
		return "redirect:/admin/sortManager";
	}

	@RequestMapping("/smallsortInsert")
	public String smallsortInsert(
			SmallSort smallSort, 
			Model model, 
			Long middleId
			) {
		
		int index = 1;
		if(smallSortRepository.findFirstIndex().isPresent()) {
			index = smallSortRepository.findFirstIndex().get() + 1;
		}
		smallSort.setSmallSortIndex(index);
		
		smallSort.setMiddleSort(middleSortRepository.findById(middleId).get());
		smallSortRepository.save(smallSort);
		return "redirect:/admin/sortManager";
	}

	@RequestMapping("/searchSmallSort")
	@ResponseBody
	public List<SmallSort> searchSmallSort(Model model, Long middleId) {
		return smallSortRepository.findAllByMiddleSort(middleSortRepository.findById(middleId).get());
	}

	@RequestMapping("/smallSortDelete")
	public String smallSortDelete(@RequestParam(value = "text[]") Long[] text, Model model) {
		
		try {
			for (Long id : text) {
				smallSortRepository.deleteById(id);
			}
		}catch(DeleteViolationException e) {
			throw new DeleteViolationException();
		}	
		return "redirect:/admin/sortManager";
	}

	@RequestMapping("/productManager")
	public String productManager(
			Model model, 
			@RequestParam(required = false) Long smallId,
			@RequestParam(required = false) Long middleId,
			@RequestParam(required = false) Long bigId,
			@PageableDefault(size = 10) Pageable pageable
			) {
		if (smallId != null) {
			Page<Product> products = productRepository.findAllBySmallSortOrderByIdDesc(pageable,
					smallSortRepository.findById(smallId).get());
			model.addAttribute("products", products);
			int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
			int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
			model.addAttribute("smallsorts", smallSortRepository.findAll());
			model.addAttribute("middlesorts", middleSortRepository.findAll());
			model.addAttribute("bigsorts", bigSortRepository.findAll());
			model.addAttribute("smallId", smallId);
			model.addAttribute("middleId", middleId);
			model.addAttribute("bigId", bigId);
		}else {
			Page<Product> products = productRepository.findAllByOrderByIdDesc(pageable);
			model.addAttribute("products", products);
			int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
			int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
		}
		model.addAttribute("bigsorts", bigSortRepository.findAll());

		return "admin/product/productManager";
	}

	@RequestMapping("/productInsertForm")
	public String productInsertForm(Model model) {

		model.addAttribute("bigsorts", bigSortRepository.findAll());

		return "admin/product/productInsertForm";
	}

	@RequestMapping("/productInsert")
	@ResponseBody
	public String productInsert(
			Product product, 
			@RequestParam(required = false, defaultValue ="") String[] spec, 
			@RequestParam(required = false, defaultValue ="") String[] infoQ, 
			@RequestParam(required = false, defaultValue ="") String[] infoA,
			MultipartFile productOverviewImage, 
			MultipartFile productSpecImage, 
			List<MultipartFile> slides,
			List<MultipartFile> productFile

	) throws IllegalStateException, IOException {
		
		product.setSmallSort(smallSortRepository.findById(product.getSmallId()).get());
		product.setBigSort(bigSortRepository.findById(product.getBigId()).get());
		product.setMiddleSort(middleSortRepository.findById(product.getMiddleId()).get());
		
		Product p = productService.productInsert(productOverviewImage, productSpecImage, product);
		
		if(spec.length > 0  && spec != null) {
			for (String s : spec) {
				ProductInfo in = new ProductInfo();
				in.setProductId(p.getId());
				in.setProductInfoText(s);
				productInfoRepository.save(in);
			}
		}else {
			ProductInfo in = new ProductInfo();
			in.setProductId(p.getId());
			in.setProductInfoText("-");
			productInfoRepository.save(in);
		}
		
		if(infoQ.length > 0  && infoQ != null) {
			for (int a = 0; a < infoQ.length; a++) {
				ProductSpec sp = new ProductSpec();
				sp.setProductSpecSubject(infoQ[a]);
				sp.setProductSpecContent(infoA[a]);
				sp.setProductId(p.getId());
				productSpecRepository.save(sp);
			}
		}else {
			ProductSpec sp = new ProductSpec();
			sp.setProductSpecSubject("-");
			sp.setProductSpecContent("-");
			sp.setProductId(p.getId());
			productSpecRepository.save(sp);
		}
		if(!productFile.isEmpty() && !productFile.get(0).isEmpty()) {
			System.out.println("123123123123123123");
			System.out.println(productFile.get(0).getBytes());
			System.out.println(productFile.get(0).getSize());
			productFileService.fileUpload(
				productFile, 
				p.getId(), 
				p.getProductCode()
				);
		}
		if(!slides.isEmpty() && !slides.get(0).isEmpty()) {
			productImageService.fileUpload(
				slides, 
				p.getId(), 
				p.getProductCode()
				);
		}

		StringBuffer sb = new StringBuffer();
		String msg = "제품이 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/productManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}

	@RequestMapping("/productDetail/{id}")
	public String productDetail(
			@PathVariable Long id,
			Model model
			) {
		model.addAttribute("bigsorts", bigSortRepository.findAll());
		model.addAttribute("middlesorts", middleSortRepository.findAllByBigSort(productRepository.findById(id).get().getSmallSort().getMiddleSort().getBigSort()));
		model.addAttribute("smallsorts", smallSortRepository.findAllByMiddleSort(productRepository.findById(id).get().getSmallSort().getMiddleSort()));
		model.addAttribute("product",productRepository.findById(id).get());
		return "admin/product/productDetail";
	}

	@RequestMapping("/productUpdate")
	public String productUpdate(
			Model model, 
			@PageableDefault(size = 10) Pageable pageable,
			Product product, 
			@RequestParam(required = false, defaultValue ="") String[] spec, 
			@RequestParam(required = false, defaultValue ="") String[] infoQ, 
			@RequestParam(required = false, defaultValue ="") String[] infoA,
			MultipartFile productOverviewImage, 
			MultipartFile productSpecImage, 
			List<MultipartFile> slides,
			List<MultipartFile> productFile
			) throws IllegalStateException, IOException {
		if(product.getSign() == null) {
			product.setSign(false);
		}
		product.setSmallSort(smallSortRepository.findById(product.getSmallId()).get());
		product.setBigSort(bigSortRepository.findById(product.getBigId()).get());
		product.setMiddleSort(middleSortRepository.findById(product.getMiddleId()).get());
		productService.productUpdate(productOverviewImage, productSpecImage, product);
		productInfoRepository.deleteAllByProductId(product.getId());
		productSpecRepository.deleteAllByProductId(product.getId());
		
		if(spec.length > 0 && spec != null) {
			for (String s : spec) {
				ProductInfo in = new ProductInfo();
				in.setProductId(product.getId());
				in.setProductInfoText(s);
				productInfoRepository.save(in);
			}
		}else {
			ProductInfo in = new ProductInfo();
			in.setProductId(product.getId());
			in.setProductInfoText("-");
			productInfoRepository.save(in);
		}
		if(infoQ.length > 0 && infoQ != null) {
			for (int a = 0; a < infoQ.length; a++) {
				ProductSpec sp = new ProductSpec();
				sp.setProductSpecSubject(infoQ[a]);
				sp.setProductSpecContent(infoA[a]);
				sp.setProductId(product.getId());
				productSpecRepository.save(sp);
			}
		}else {
			ProductSpec sp = new ProductSpec();
			sp.setProductSpecSubject("-");
			sp.setProductSpecContent("-");
			sp.setProductId(product.getId());
			productSpecRepository.save(sp);
		}
		
		if(slides.size()>0 && !slides.get(0).isEmpty()) {
			productImageRepository.deleteAllByProductId(product.getId());
			productImageService.fileUpload(
					slides, 
					product.getId(),
					productRepository.findById(product.getId()).get().getProductCode());
		}
		
		if(productFile.size()>0 && !productFile.get(0).isEmpty()) {
			productFileRepository.deleteAllByProductId(product.getId());
			productFileService.fileUpload(
					productFile, 
					product.getId(), 
					productRepository.findById(product.getId()).get().getProductCode());
		}
		Page<Product> products = productRepository.findAll(pageable);
		int startPage = Math.max(1, products.getPageable().getPageNumber() - 4);
		int endPage = Math.min(products.getTotalPages(), products.getPageable().getPageNumber() + 4);
		
		model.addAttribute("products", products);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("smallId", product.getSmallId());
		model.addAttribute("bigsorts", bigSortRepository.findAll());

		return "redirect:/admin/productManager";
	}
	
	@RequestMapping("/productDelete/{id}")
	@ResponseBody
	public String productDelete(
			@PathVariable Long id
			) {
		
		try {
			productService.deleteFiles(id);
			productRepository.deleteById(id);
		} catch (Exception e) {
			System.out.println(e);
		}
		StringBuffer sb = new StringBuffer();
		String msg = "제품이 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/productManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
}
