package com.dev.BionLifeScienceWeb.service.program.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.ProductInfo;
import com.dev.BionLifeScienceWeb.model.product.ProductSpec;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductInfoRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductSpecRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;
import com.dev.BionLifeScienceWeb.service.product.ProductService;

@Service
public class ExcelUploadService {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	BigSortRepository bigSortRepository;

	@Autowired
	MiddleSortRepository middleSortRepository;

	@Autowired
	SmallSortRepository smallSortRepository;

	@Autowired
	ProductSpecRepository productSpecRepository;

	@Autowired
	ProductInfoRepository productInfoRepository;

	@Autowired
	ProductImageRepository productImageRepository;
	
	@Autowired
	ProductFileRepository productFileRepository;

	@Autowired
	ProductService productService;

	public void uploadExcel(MultipartFile file) throws IOException {

		String bigSortHeaderNames[] = new String[] {
				"대분류 ID", "대분류 이름"
				};
		String middleSortHeaderNames[] = new String[] {
				"중분류 ID","중분류 이름"
				};
		String smallSortHeaderNames[] = new String[] {
				"소분류 ID", "소분류 이름"
				};
		String productHeaderNames[] = new String[] {
				"PRODUCT_CODE", "PRODUCT_SUBJECT", "PRODUCT_CONTENT", 
				"PRODUCT_SUB_CONTENT", "PRODUCT_SIGN","소분류 ID" ,"중분류 ID",
				"대분류 ID"
				};
		String productInfoHeaderNames[] = new String[] { 
				"PRODUCT_CODE", "PRODUCT_INFO_TEXT" 
				};
		String productSpecHeaderNames[] = new String[] { 
				"PRODUCT_CODE", "PRODUCT_SPEC_SUBJECT", "PRODUCT_SPEC_CONTENT" 
				};
		List<String[]> headers = new ArrayList<String[]>();
		headers.add(bigSortHeaderNames);
		headers.add(middleSortHeaderNames);
		headers.add(smallSortHeaderNames);
		headers.add(productHeaderNames);
		headers.add(productInfoHeaderNames);
		headers.add(productSpecHeaderNames);
		Workbook workbook = new XSSFWorkbook(file.getInputStream());

		List<String> productCodes = new ArrayList<String>();
		List<String> infoCodes = new ArrayList<String>();
		List<String> specCodes = new ArrayList<String>();
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		executorService.submit(() -> {
			try {
				productInfoRepository.deleteAll();
				productSpecRepository.deleteAll();
				productRepository.deleteAll();
				productFileRepository.deleteAll();
				productImageRepository.deleteAll();
			} catch (Exception e) {
				System.out.println(e);
			}
		});
		

		executorService.submit(() -> {
			try {
				Sheet productSheet = workbook.getSheetAt(3);
				for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSheet.getRow(i);

					if (row != null) {
						Product product = new Product();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						Cell subContent = row.getCell(3);
						Cell sign = row.getCell(4);
						Cell smallSort = row.getCell(5);
						Cell middleSort = row.getCell(6);
						Cell bigSort = row.getCell(7);
						Long smallValue = (long) smallSort.getNumericCellValue();
						Long middleValue = (long) middleSort.getNumericCellValue();
						Long bigValue = (long) bigSort.getNumericCellValue();
						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						String subContentValue = subContent + "";
						String signStr = sign + "";
						Boolean signValue = true;
						productCodes.add(codeValue);
						if (signStr.equals("TRUE")) {
							signValue = true;
						} else {
							signValue = false;
						}
						product.setProductCode(codeValue);
						product.setSubject(subjectValue);
						product.setContent(contentValue);
						product.setProductSubContent(subContentValue);
						product.setSign(signValue);
						product.setSmallSort(smallSortRepository.findById(smallValue).get());
						product.setMiddleSort(middleSortRepository.findById(middleValue).get());
						product.setBigSort(bigSortRepository.findById(bigValue).get());
						productService.excelInsert(product);

					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
		});
		
		executorService.submit(() -> {

			try {

				Sheet productInfoSheet = workbook.getSheetAt(5);
				for (int i = 1; i < productInfoSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productInfoSheet.getRow(i);

					if (row != null) {

						ProductInfo info = new ProductInfo();
						Cell code = row.getCell(0);
						Cell text = row.getCell(1);
						String codeValue = code + "";
						String textValue = text + "";
						info.setProductId(productRepository.findByProductCode(codeValue).get().getId());
						info.setProductInfoText(textValue);
						infoCodes.add(codeValue);
						productInfoRepository.save(info);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
			
			try {

				Sheet productSpecSheet = workbook.getSheetAt(4);
				for (int i = 1; i < productSpecSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSpecSheet.getRow(i);

					if (row != null) {
						ProductSpec spec = new ProductSpec();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						specCodes.add(codeValue);
						spec.setProductSpecSubject(subjectValue);
						spec.setProductSpecContent(contentValue);
						spec.setProductId(productRepository.findByProductCode(codeValue).get().getId());
						productSpecRepository.save(spec);

					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}

			
			try {

				for (String code : productCodes) {
					if (!infoCodes.contains(code)) {
						ProductInfo info = new ProductInfo();
						info.setProductId(productRepository.findByProductCode(code).get().getId());
						info.setProductInfoText("-");
						productInfoRepository.save(info);
					}

					if (!specCodes.contains(code)) {
						ProductSpec spec = new ProductSpec();
						spec.setProductSpecSubject("-");
						spec.setProductSpecContent("-");
						spec.setProductId(productRepository.findByProductCode(code).get().getId());
						productSpecRepository.save(spec);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
		});
		executorService.shutdown();
		
		
	}
	
	public void uploadAddExcel(MultipartFile file) throws IOException {

		String bigSortHeaderNames[] = new String[] {
				"대분류 ID", "대분류 이름"
				};
		String middleSortHeaderNames[] = new String[] {
				"중분류 ID","중분류 이름"
				};
		String smallSortHeaderNames[] = new String[] {
				"소분류 ID", "소분류 이름"
				};
		String productHeaderNames[] = new String[] {
				"PRODUCT_CODE", "PRODUCT_SUBJECT", "PRODUCT_CONTENT", 
				"PRODUCT_SUB_CONTENT", "PRODUCT_SIGN","소분류 ID" ,"중분류 ID",
				"대분류 ID"
				};
		String productInfoHeaderNames[] = new String[] { 
				"PRODUCT_CODE", "PRODUCT_INFO_TEXT" 
				};
		String productSpecHeaderNames[] = new String[] { 
				"PRODUCT_CODE", "PRODUCT_SPEC_SUBJECT", "PRODUCT_SPEC_CONTENT" 
				};
		List<String[]> headers = new ArrayList<String[]>();
		headers.add(bigSortHeaderNames);
		headers.add(middleSortHeaderNames);
		headers.add(smallSortHeaderNames);
		headers.add(productHeaderNames);
		headers.add(productInfoHeaderNames);
		headers.add(productSpecHeaderNames);
		Workbook workbook = new XSSFWorkbook(file.getInputStream());

		List<String> productCodes = new ArrayList<String>();
		List<String> infoCodes = new ArrayList<String>();
		List<String> specCodes = new ArrayList<String>();
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		executorService.submit(() -> {
			try {
				Sheet productSheet = workbook.getSheetAt(3);
				for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSheet.getRow(i);
					if (row != null) {
						Product product = new Product();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						Cell subContent = row.getCell(3);
						Cell sign = row.getCell(4);
						Cell smallSort = row.getCell(5);
						Cell middleSort = row.getCell(6);
						Cell bigSort = row.getCell(7);
						Long smallValue = (long) smallSort.getNumericCellValue();
						Long middleValue = (long) middleSort.getNumericCellValue();
						Long bigValue = (long) bigSort.getNumericCellValue();
						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						String subContentValue = subContent + "";
						String signStr = sign + "";
						Boolean signValue = true;
						productCodes.add(codeValue);
						if (signStr.equals("TRUE")) {
							signValue = true;
						} else {
							signValue = false;
						}
						product.setProductCode(codeValue);
						product.setSubject(subjectValue);
						product.setContent(contentValue);
						product.setProductSubContent(subContentValue);
						product.setSign(signValue);
						product.setSmallSort(smallSortRepository.findById(smallValue).get());
						product.setMiddleSort(middleSortRepository.findById(middleValue).get());
						product.setBigSort(bigSortRepository.findById(bigValue).get());
						productService.excelInsert(product);

					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
			
			
		});
		
		executorService.submit(() -> {

			try {

				Sheet productInfoSheet = workbook.getSheetAt(5);
				for (int i = 1; i < productInfoSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productInfoSheet.getRow(i);

					if (row != null) {

						ProductInfo info = new ProductInfo();
						Cell code = row.getCell(0);
						Cell text = row.getCell(1);
						String codeValue = code + "";
						String textValue = text + "";
						info.setProductId(productRepository.findByProductCode(codeValue).get().getId());
						info.setProductInfoText(textValue);
						infoCodes.add(codeValue);
						productInfoRepository.save(info);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
			
			try {

				Sheet productSpecSheet = workbook.getSheetAt(4);
				for (int i = 1; i < productSpecSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSpecSheet.getRow(i);

					if (row != null) {
						ProductSpec spec = new ProductSpec();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						specCodes.add(codeValue);
						spec.setProductSpecSubject(subjectValue);
						spec.setProductSpecContent(contentValue);
						spec.setProductId(productRepository.findByProductCode(codeValue).get().getId());
						productSpecRepository.save(spec);

					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}

			
			try {

				for (String code : productCodes) {
					if (!infoCodes.contains(code)) {
						ProductInfo info = new ProductInfo();
						info.setProductId(productRepository.findByProductCode(code).get().getId());
						info.setProductInfoText("-");
						productInfoRepository.save(info);
					}

					if (!specCodes.contains(code)) {
						ProductSpec spec = new ProductSpec();
						spec.setProductSpecSubject("-");
						spec.setProductSpecContent("-");
						spec.setProductId(productRepository.findByProductCode(code).get().getId());
						productSpecRepository.save(spec);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
		});
		executorService.shutdown();
		
	}
}
