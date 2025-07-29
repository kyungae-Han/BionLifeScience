package com.dev.BionLifeScienceWeb.service.program.brand;

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

import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductInfo;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductSpec;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductFileRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductInfoRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductSpecRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductService;

@Service
public class BrandExcelUploadService {

	@Autowired
	BrandRepository brandRepository;

	@Autowired
	BrandProductRepository brandProductRepository;

	@Autowired
	BrandBigSortRepository brandBigSortRepository;

	@Autowired
	BrandMiddleSortRepository brandMiddleSortRepository;

	@Autowired
	BrandSmallSortRepository brandSmallSortRepository;

	@Autowired
	BrandProductSpecRepository brandProductSpecRepository;

	@Autowired
	BrandProductInfoRepository brandProductInfoRepository;

	@Autowired
	BrandProductService brandProductService;

	@Autowired
	BrandProductImageRepository brandProductImageRepository;

	@Autowired
	BrandProductFileRepository brandProductFileRepository;

	public void uploadExcel(MultipartFile file) throws IOException {

		String brandHeaderNames[] = new String[] { "브랜드 ID", "브랜드 이름" };
		String bigSortHeaderNames[] = new String[] { "대분류 ID", "대분류 이름" };
		String middleSortHeaderNames[] = new String[] { "중분류 ID", "중분류 이름" };
		String smallSortHeaderNames[] = new String[] { "소분류 ID", "소분류 이름" };
		String productHeaderNames[] = new String[] { "PRODUCT_CODE", "PRODUCT_SUBJECT", "PRODUCT_CONTENT",
				"PRODUCT_SUB_CONTENT", "소분류 ID", "중분류 ID", "대분류 ID", "브랜드 ID" };
		String productInfoHeaderNames[] = new String[] { "PRODUCT_CODE", "PRODUCT_INFO_TEXT" };
		String productSpecHeaderNames[] = new String[] { "PRODUCT_CODE", "PRODUCT_SPEC_SUBJECT",
				"PRODUCT_SPEC_CONTENT" };
		List<String[]> headers = new ArrayList<String[]>();

		headers.add(brandHeaderNames);
		headers.add(bigSortHeaderNames);
		headers.add(middleSortHeaderNames);
		headers.add(smallSortHeaderNames);
		headers.add(productHeaderNames);
		headers.add(productInfoHeaderNames);
		headers.add(productSpecHeaderNames);

		List<String> productCodes = new ArrayList<String>();
		List<String> infoCodes = new ArrayList<String>();
		List<String> specCodes = new ArrayList<String>();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		executorService.submit(() -> {
			try {
				brandProductInfoRepository.deleteAll();
				brandProductSpecRepository.deleteAll();
				brandProductRepository.deleteAll();
				brandProductImageRepository.deleteAll();
				brandProductFileRepository.deleteAll();
			} catch (Exception e) {
				System.out.println("DELETE ALL");
			}
		});

		executorService.submit(() -> {
			try {

				// 엑셀 97 - 2003 까지는 HSSF(xls), 엑셀 2007 이상은 XSSF(xlsx)

				Sheet productSheet = workbook.getSheetAt(4);
				for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSheet.getRow(i);

					if (row != null) {
						BrandProduct product = new BrandProduct();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						Cell subContent = row.getCell(3);
						Cell smallSort = row.getCell(4);
						Cell middleSort = row.getCell(5);
						Cell bigSort = row.getCell(6);
						Cell brandId = row.getCell(7);
						Long brandValue = (long) brandId.getNumericCellValue();
						Long smallValue = (long) smallSort.getNumericCellValue();
						Long middleValue = (long) middleSort.getNumericCellValue();
						Long bigValue = (long) bigSort.getNumericCellValue();
						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						String subContentValue = subContent + "";
						Boolean signValue = false;
						product.setBrandProductCode(codeValue);
						product.setSubject(subjectValue);
						product.setContent(contentValue);
						product.setProductSubContent(subContentValue);
						product.setSign(signValue);
						product.setSmallSort(brandSmallSortRepository.findById(smallValue).get());
						product.setMiddleSort(brandMiddleSortRepository.findById(middleValue).get());
						product.setBigSort(brandBigSortRepository.findById(bigValue).get());
						product.setBrand(brandRepository.findById(brandValue).get());
						brandProductService.excelInsert(product);

					}
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		});
		executorService.submit(() -> {

			try {

				Sheet productInfoSheet = workbook.getSheetAt(6);
				for (int i = 1; i < productInfoSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productInfoSheet.getRow(i);

					if (row != null) {
						BrandProductInfo info = new BrandProductInfo();
						Cell code = row.getCell(0);
						Cell text = row.getCell(1);
						String codeValue = code + "";
						String textValue = text + "";
						info.setProductId(brandProductRepository.findByBrandProductCode(codeValue).get().getId());
						info.setProductInfoText(textValue);
						brandProductInfoRepository.save(info);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}

			try {

				Sheet productSpecSheet = workbook.getSheetAt(5);
				for (int i = 1; i < productSpecSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSpecSheet.getRow(i);

					if (row != null) {
						BrandProductSpec spec = new BrandProductSpec();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						spec.setProductSpecSubject(subjectValue);
						spec.setProductSpecContent(contentValue);
						spec.setProductId(brandProductRepository.findByBrandProductCode(codeValue).get().getId());
						brandProductSpecRepository.save(spec);

					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}

			try {

				for (String code : productCodes) {
					if (!infoCodes.contains(code)) {
						BrandProductInfo info = new BrandProductInfo();
						info.setProductId(brandProductRepository.findByBrandProductCode(code).get().getId());
						info.setProductInfoText("-");
						brandProductInfoRepository.save(info);
					}

					if (!specCodes.contains(code)) {
						BrandProductSpec spec = new BrandProductSpec();
						spec.setProductSpecSubject("-");
						spec.setProductSpecContent("-");
						spec.setProductId(brandProductRepository.findByBrandProductCode(code).get().getId());
						brandProductSpecRepository.save(spec);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
		});
		executorService.shutdown();

	}
	public void uploadAddExcel(MultipartFile file) throws IOException {

		String brandHeaderNames[] = new String[] { "브랜드 ID", "브랜드 이름" };
		String bigSortHeaderNames[] = new String[] { "대분류 ID", "대분류 이름" };
		String middleSortHeaderNames[] = new String[] { "중분류 ID", "중분류 이름" };
		String smallSortHeaderNames[] = new String[] { "소분류 ID", "소분류 이름" };
		String productHeaderNames[] = new String[] { "PRODUCT_CODE", "PRODUCT_SUBJECT", "PRODUCT_CONTENT",
				"PRODUCT_SUB_CONTENT", "소분류 ID", "중분류 ID", "대분류 ID", "브랜드 ID" };
		String productInfoHeaderNames[] = new String[] { "PRODUCT_CODE", "PRODUCT_INFO_TEXT" };
		String productSpecHeaderNames[] = new String[] { "PRODUCT_CODE", "PRODUCT_SPEC_SUBJECT",
				"PRODUCT_SPEC_CONTENT" };
		List<String[]> headers = new ArrayList<String[]>();
		headers.add(brandHeaderNames);
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
				Sheet productSheet = workbook.getSheetAt(4);
				for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSheet.getRow(i);
					if (row != null) {
						BrandProduct product = new BrandProduct();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						Cell subContent = row.getCell(3);
						Cell smallSort = row.getCell(4);
						Cell middleSort = row.getCell(5);
						Cell bigSort = row.getCell(6);
						Cell brandId = row.getCell(7);

						Long brandValue = (long) brandId.getNumericCellValue();
						Long smallValue = (long) smallSort.getNumericCellValue();
						Long middleValue = (long) middleSort.getNumericCellValue();
						Long bigValue = (long) bigSort.getNumericCellValue();

						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						String subContentValue = subContent + "";
						Boolean signValue = false;
						productCodes.add(codeValue);
						product.setBrandProductCode(codeValue);
						product.setSubject(subjectValue);
						product.setContent(contentValue);
						product.setProductSubContent(subContentValue);
						product.setSign(signValue);
						product.setBrand(brandRepository.findById(brandValue).get());
						product.setSmallSort(brandSmallSortRepository.findById(smallValue).get());
						product.setMiddleSort(brandMiddleSortRepository.findById(middleValue).get());
						product.setBigSort(brandBigSortRepository.findById(bigValue).get());
						brandProductService.excelInsert(product);

					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
		});
		System.out.println("22222222222222222");
		executorService.submit(() -> {

			try {

				Sheet productInfoSheet = workbook.getSheetAt(6);
				for (int i = 1; i < productInfoSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productInfoSheet.getRow(i);

					if (row != null) {

						BrandProductInfo info = new BrandProductInfo();
						Cell code = row.getCell(0);
						Cell text = row.getCell(1);
						String codeValue = code + "";
						String textValue = text + "";
						info.setProductId(brandProductRepository.findByBrandProductCode(codeValue).get().getId());
						info.setProductInfoText(textValue);
						infoCodes.add(codeValue);
						brandProductInfoRepository.save(info);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}

			try {

				Sheet productSpecSheet = workbook.getSheetAt(5);
				for (int i = 1; i < productSpecSheet.getPhysicalNumberOfRows(); i++) {
					Row row = productSpecSheet.getRow(i);

					if (row != null) {
						BrandProductSpec spec = new BrandProductSpec();
						Cell code = row.getCell(0);
						Cell subject = row.getCell(1);
						Cell content = row.getCell(2);
						String codeValue = code + "";
						String subjectValue = subject + "";
						String contentValue = content + "";
						specCodes.add(codeValue);
						spec.setProductSpecSubject(subjectValue);
						spec.setProductSpecContent(contentValue);
						spec.setProductId(brandProductRepository.findByBrandProductCode(codeValue).get().getId());
						brandProductSpecRepository.save(spec);

					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}

			try {

				for (String code : productCodes) {
					if (!infoCodes.contains(code)) {
						BrandProductInfo info = new BrandProductInfo();
						info.setProductId(brandProductRepository.findByBrandProductCode(code).get().getId());
						info.setProductInfoText("-");
						brandProductInfoRepository.save(info);
					}

					if (!specCodes.contains(code)) {
						BrandProductSpec spec = new BrandProductSpec();
						spec.setProductSpecSubject("-");
						spec.setProductSpecContent("-");
						spec.setProductId(brandProductRepository.findByBrandProductCode(code).get().getId());
						brandProductSpecRepository.save(spec);
					}
				}

			} catch (Exception e) {
				System.out.println(e.fillInStackTrace());
			}
		});
		executorService.shutdown();

	}
}
