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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelUploadService {

	private final ProductRepository productRepository;
	private final BigSortRepository bigSortRepository;
	private final MiddleSortRepository middleSortRepository;
	private final SmallSortRepository smallSortRepository;
	private final ProductSpecRepository productSpecRepository;
	private final ProductInfoRepository productInfoRepository;
	private final ProductImageRepository productImageRepository;
	private final ProductFileRepository productFileRepository;
	private final ProductService productService;

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
				    if (row == null) continue; // 빈 행 무시

				    try {
				        Product product = new Product();

				        // ✅ 셀 가져오기
				        Cell codeCell        = row.getCell(0);
				        Cell subjectCell     = row.getCell(1);
				        Cell contentCell     = row.getCell(2);
				        Cell subContentCell  = row.getCell(3);
				        Cell signCell        = row.getCell(4);
				        Cell smallSortCell   = row.getCell(5);
				        Cell middleSortCell  = row.getCell(6);
				        Cell bigSortCell     = row.getCell(7);

				        // ✅ 셀 값 안전하게 파싱
				        String codeValue        = getCellString(codeCell);
				        String subjectValue     = getCellString(subjectCell);
				        String contentValue     = getCellString(contentCell);
				        String subContentValue  = getCellString(subContentCell);
				        String signStr          = getCellString(signCell);

				        Long smallValue  = getCellLong(smallSortCell);
				        Long middleValue = getCellLong(middleSortCell);
				        Long bigValue    = getCellLong(bigSortCell);

				        // ✅ 최소한 bigSort는 존재해야 함
				        if (bigValue == null) {
				            System.out.println("⚠️ [" + i + "행] 대분류 ID 없음 → 건너뜀");
				            continue;
				        }

				        // ✅ sign 처리 (TRUE/1/true 모두 허용)
				        Boolean signValue = signStr != null && 
				                            (signStr.equalsIgnoreCase("TRUE") || signStr.equals("1"));

				        // ✅ 기본정보 세팅
				        product.setProductCode(codeValue);
				        product.setSubject(subjectValue);
				        product.setContent(contentValue);
				        product.setProductSubContent(subContentValue);
				        product.setSign(signValue);

				        // ✅ 분류 연결 — 존재 확인 후만 set
				        bigSortRepository.findById(bigValue).ifPresent(product::setBigSort);

				        if (middleValue != null) {
				            middleSortRepository.findById(middleValue).ifPresent(product::setMiddleSort);
				        }
				        if (smallValue != null) {
				            smallSortRepository.findById(smallValue).ifPresent(product::setSmallSort);
				        }

				        // ✅ 코드 등록
				        productCodes.add(codeValue);
				        productService.excelInsert(product);

				    } catch (Exception e) {
				        System.out.println("❌ [Row " + i + "] 엑셀 데이터 파싱 실패: " + e.getMessage());
				    }
				} // ← ✅ for 문 닫힘


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
				    if (row == null) continue; // 빈 행 무시

				    try {
				        Product product = new Product();

				        // ✅ 셀 가져오기
				        Cell codeCell        = row.getCell(0);
				        Cell subjectCell     = row.getCell(1);
				        Cell contentCell     = row.getCell(2);
				        Cell subContentCell  = row.getCell(3);
				        Cell signCell        = row.getCell(4);
				        Cell smallSortCell   = row.getCell(5);
				        Cell middleSortCell  = row.getCell(6);
				        Cell bigSortCell     = row.getCell(7);

				        // ✅ 셀 값 안전하게 파싱
				        String codeValue        = getCellString(codeCell);
				        String subjectValue     = getCellString(subjectCell);
				        String contentValue     = getCellString(contentCell);
				        String subContentValue  = getCellString(subContentCell);
				        String signStr          = getCellString(signCell);

				        Long smallValue  = getCellLong(smallSortCell);
				        Long middleValue = getCellLong(middleSortCell);
				        Long bigValue    = getCellLong(bigSortCell);

				        // ✅ 최소한 bigSort는 존재해야 함
				        if (bigValue == null) {
				            System.out.println("⚠️ [" + i + "행] 대분류 ID 없음 → 건너뜀");
				            continue;
				        }

				        // ✅ sign 처리 (TRUE/1/true 모두 허용)
				        Boolean signValue = signStr != null && 
				                            (signStr.equalsIgnoreCase("TRUE") || signStr.equals("1"));

				        // ✅ 기본정보 세팅
				        product.setProductCode(codeValue);
				        product.setSubject(subjectValue);
				        product.setContent(contentValue);
				        product.setProductSubContent(subContentValue);
				        product.setSign(signValue);

				        // ✅ 분류 연결 — 존재 확인 후만 set
				        bigSortRepository.findById(bigValue).ifPresent(product::setBigSort);

				        if (middleValue != null) {
				            middleSortRepository.findById(middleValue).ifPresent(product::setMiddleSort);
				        }
				        if (smallValue != null) {
				            smallSortRepository.findById(smallValue).ifPresent(product::setSmallSort);
				        }

				        // ✅ 코드 등록
				        productCodes.add(codeValue);
				        productService.excelInsert(product);

				    } catch (Exception e) {
				        System.out.println("❌ [Row " + i + "] 엑셀 데이터 파싱 실패: " + e.getMessage());
				    }
				} // ← ✅ for 문 닫힘


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
	
	// ✅ 문자열 변환 (모든 셀 타입 안전하게 처리)
	private String getCellString(Cell cell) {
	    if (cell == null) return null;

	    try {
	        // 셀 타입에 상관없이 문자열 값으로 강제 변환
	        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
	        String value = cell.getStringCellValue();
	        return (value != null) ? value.trim() : null;

	    } catch (Exception e) {
	        System.out.println("⚠️ getCellString 변환 오류: " + e.getMessage());
	        return null;
	    }
	}



	// ✅ Long 변환 (문자열·숫자 혼합, null 안전)
	private Long getCellLong(Cell cell) {
	    if (cell == null) return null;

	    try {
	        // 강제로 문자열로 변환 후 숫자 파싱 시도
	        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
	        String value = cell.getStringCellValue().trim();

	        if (value.isEmpty()) return null;

	        // "3.0" → "3"
	        if (value.endsWith(".0")) {
	            value = value.substring(0, value.length() - 2);
	        }

	        return Long.parseLong(value);

	    } catch (Exception e) {
	        System.out.println("⚠️ getCellLong 변환 오류: " + e.getMessage());
	        return null;
	    }
	}



}
