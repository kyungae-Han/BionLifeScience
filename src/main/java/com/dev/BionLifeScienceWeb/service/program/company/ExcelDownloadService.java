package com.dev.BionLifeScienceWeb.service.program.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.product.BigSort;
import com.dev.BionLifeScienceWeb.model.product.MiddleSort;
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.ProductInfo;
import com.dev.BionLifeScienceWeb.model.product.ProductSpec;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;
import com.dev.BionLifeScienceWeb.repository.product.BigSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.MiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductInfoRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductRepository;
import com.dev.BionLifeScienceWeb.repository.product.ProductSpecRepository;
import com.dev.BionLifeScienceWeb.repository.product.SmallSortRepository;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ExcelDownloadService {

	private final ProductRepository productRepository;
	private final BigSortRepository bigSortRepository;
	private final MiddleSortRepository middleSortRepository;
	private final SmallSortRepository smallSortRepository;
	private final ProductSpecRepository productSpecRepository;
	private final ProductInfoRepository productInfoRepository;
	
	public void bigSortDownload(
			HttpServletResponse res
			) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet bigSort = workbook.createSheet("대분류");
		Sheet middleSort = workbook.createSheet("중분류");
		Sheet smallSort = workbook.createSheet("소분류");
		Sheet product = workbook.createSheet("제품정보");
		Sheet productSpec = workbook.createSheet("제품스펙");
		Sheet productInfo = workbook.createSheet("제품인포");
		
		bigSort.setDefaultColumnWidth(20); // 디폴트 너비 설정
		middleSort.setDefaultColumnWidth(30);
		smallSort.setDefaultColumnWidth(30);
		product.setDefaultColumnWidth(40);
		productInfo.setDefaultColumnWidth(40);
		productSpec.setDefaultColumnWidth(40);

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
		
		
		
		XSSFFont headerXSSFFont = (XSSFFont) workbook.createFont();
		headerXSSFFont.setColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }));

		/**
		 * header cell style
		 */
		XSSFCellStyle headerXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

		// 테두리 설정
		headerXssfCellStyle.setBorderLeft(BorderStyle.THIN);
		headerXssfCellStyle.setBorderRight(BorderStyle.THIN);
		headerXssfCellStyle.setBorderTop(BorderStyle.THIN);
		headerXssfCellStyle.setBorderBottom(BorderStyle.THIN);

		// 배경 설정
		headerXssfCellStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 34, (byte) 37, (byte) 41 }));
		headerXssfCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerXssfCellStyle.setFont(headerXSSFFont);

		/**
		 * body cell style
		 */
		XSSFCellStyle bodyXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

		// 테두리 설정
		bodyXssfCellStyle.setBorderLeft(BorderStyle.THIN);
		bodyXssfCellStyle.setBorderRight(BorderStyle.THIN);
		bodyXssfCellStyle.setBorderTop(BorderStyle.THIN);
		bodyXssfCellStyle.setBorderBottom(BorderStyle.THIN);


		Row headerRow = null;
		Cell headerCell = null;
		Sheet[] sheets = {bigSort, middleSort, smallSort, product, productInfo, productSpec};
		List<String[]> headers = new ArrayList<String[]>();
		headers.add(bigSortHeaderNames);
		headers.add(middleSortHeaderNames);
		headers.add(smallSortHeaderNames);
		headers.add(productHeaderNames);
		headers.add(productInfoHeaderNames);
		headers.add(productSpecHeaderNames);
		
		
		
		for(int x = 0; x < sheets.length; x++) {
			int rowCount = 0; // 데이터가 저장될 행
			headerRow = sheets[x].createRow(rowCount++);
			for(int y=0; y< headers.get(x).length; y++) {
				headerCell = headerRow.createCell(y);
				headerCell.setCellValue(headers.get(x)[y]); // 데이터 추가
				headerCell.setCellStyle(headerXssfCellStyle); // 스타일 추가
			}
			
		}
		
		
		List<BigSort> bigSorts = bigSortRepository.findAll();
		List<MiddleSort> middleSorts = middleSortRepository.findAll();
		List<SmallSort> smallSorts = smallSortRepository.findAll();
		List<Product> products = productRepository.findAll();
		List<ProductInfo> productInfos = productInfoRepository.findAll();
		List<ProductSpec> productSpecs = productSpecRepository.findAll();

		/**
		 * body data
		 */
		String bigBodyDatass[][] = new String[bigSorts.size()][headers.get(0).length];
		String middleBodyDatass[][] = new String[middleSorts.size()][headers.get(1).length];
		String smallBodyDatass[][] = new String[smallSorts.size()][headers.get(2).length];
		String productBodyDatass[][] = new String[products.size()][headers.get(3).length];
		String productInfoBodyDatass[][] = new String[productInfos.size()][headers.get(4).length];
		String productSpecBodyDatass[][] = new String[productSpecs.size()][headers.get(5).length];
		int bigRowCount = 1; // 데이터가 저장될 행
		int middleRowCount = 1;
		int smallRowCount = 1;
		int productRowCount = 1;
		int productInfoRowCount = 1;
		int productSpecRowCount = 1;
		Row bigBodyRow = null;
		Row middleBodyRow = null;
		Row smallBodyRow = null;
		Row productBodyRow = null;
		Row productInfoBodyRow = null;
		Row productSpecBodyRow = null;
		Cell bigBodyCell = null;
		Cell middleBodyCell = null;
		Cell smallBodyCell = null;
		Cell productBodyCell = null;
		Cell productInfoBodyCell = null;
		Cell productSpecBodyCell = null;
		int cellNumbber = 0;
		
		for (int x=0; x<bigBodyDatass.length; x++) {
			bigBodyRow = bigSort.createRow(bigRowCount++);
			
			for (int i = 0; i < bigBodyDatass[x].length; i++) {
				bigBodyCell = bigBodyRow.createCell(cellNumbber);
				bigBodyCell.setCellValue(bigSorts.get(x).getId());
				bigBodyCell.setCellStyle(bodyXssfCellStyle);
				
				bigBodyCell = bigBodyRow.createCell(cellNumbber+1);
				bigBodyCell.setCellValue(bigSorts.get(x).getName());
				bigBodyCell.setCellStyle(bodyXssfCellStyle);
			}
		}
		
		for (int x=0; x<middleBodyDatass.length; x++) {
			middleBodyRow = middleSort.createRow(middleRowCount++);
			for (int i = 0; i < middleBodyDatass[x].length; i++) {
				middleBodyCell = middleBodyRow.createCell(cellNumbber);
				middleBodyCell.setCellValue(middleSorts.get(x).getId());
				middleBodyCell.setCellStyle(bodyXssfCellStyle);
				
				middleBodyCell = middleBodyRow.createCell(cellNumbber+1);
				middleBodyCell.setCellValue(middleSorts.get(x).getName());
				middleBodyCell.setCellStyle(bodyXssfCellStyle);
			}
		}
		
		for (int x=0; x<smallBodyDatass.length; x++) {
			smallBodyRow = smallSort.createRow(smallRowCount++);
			for (int i = 0; i < smallBodyDatass[x].length; i++) {
				smallBodyCell = smallBodyRow.createCell(cellNumbber);
				smallBodyCell.setCellValue(smallSorts.get(x).getId());
				smallBodyCell.setCellStyle(bodyXssfCellStyle);
				
				smallBodyCell = smallBodyRow.createCell(cellNumbber+1);
				smallBodyCell.setCellValue(smallSorts.get(x).getName());
				smallBodyCell.setCellStyle(bodyXssfCellStyle);
			}
		}
		
		for (int x=0; x<productBodyDatass.length; x++) {
			productBodyRow = product.createRow(productRowCount++);
			for (int i = 0; i < productBodyDatass[x].length; i++) {
				
				productBodyCell = productBodyRow.createCell(cellNumbber);
				productBodyCell.setCellValue(products.get(x).getProductCode()); 
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productBodyCell = productBodyRow.createCell(cellNumbber+1);
				productBodyCell.setCellValue(products.get(x).getSubject()); 
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productBodyCell = productBodyRow.createCell(cellNumbber+2);
				productBodyCell.setCellValue(products.get(x).getContent());
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productBodyCell = productBodyRow.createCell(cellNumbber+3);
				productBodyCell.setCellValue(products.get(x).getProductSubContent());
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productBodyCell = productBodyRow.createCell(cellNumbber+4);
				productBodyCell.setCellValue(products.get(x).getSign());
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productBodyCell = productBodyRow.createCell(cellNumbber+5);
				productBodyCell.setCellValue(products.get(x).getSmallSort().getId());
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productBodyCell = productBodyRow.createCell(cellNumbber+6);
				productBodyCell.setCellValue(products.get(x).getMiddleSort().getId());
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productBodyCell = productBodyRow.createCell(cellNumbber+7);
				productBodyCell.setCellValue(products.get(x).getBigSort().getId());
				productBodyCell.setCellStyle(bodyXssfCellStyle);
				
			}
		}
		
		for (int x=0; x<productInfoBodyDatass.length; x++) {
			productInfoBodyRow = productInfo.createRow(productInfoRowCount++);
			for (int i = 0; i < productInfoBodyDatass[x].length; i++) {
				productInfoBodyCell = productInfoBodyRow.createCell(cellNumbber);
				productInfoBodyCell.setCellValue(productRepository.findById(productInfos.get(x).getProductId()).get().getProductCode());
				productInfoBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productInfoBodyCell = productInfoBodyRow.createCell(cellNumbber+1);
				productInfoBodyCell.setCellValue(productInfos.get(x).getProductInfoText());// 데이터 추가
				productInfoBodyCell.setCellStyle(bodyXssfCellStyle); // 스타일 추가
			}
		}
		
		for (int x=0; x<productSpecBodyDatass.length; x++) {
			productSpecBodyRow = productSpec.createRow(productSpecRowCount++);

			for (int i = 0; i < productSpecBodyDatass[x].length; i++) {
				productSpecBodyCell = productSpecBodyRow.createCell(cellNumbber);
				productSpecBodyCell.setCellValue(productRepository.findById(productSpecs.get(x).getProductId()).get().getProductCode());
				productSpecBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productSpecBodyCell = productSpecBodyRow.createCell(cellNumbber+1);
				productSpecBodyCell.setCellValue(productSpecs.get(x).getProductSpecSubject());
				productSpecBodyCell.setCellStyle(bodyXssfCellStyle);
				
				productSpecBodyCell = productSpecBodyRow.createCell(cellNumbber+2);
				productSpecBodyCell.setCellValue(productSpecs.get(x).getProductSpecContent());// 데이터 추가
				productSpecBodyCell.setCellStyle(bodyXssfCellStyle); // 스타일 추가
			}
		}
			
			

		/**
		 * download
		 */
		String fileName = "BIONLIFESCIENCE_PRODUCT_RESET_SHEET";

		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		res.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
		ServletOutputStream servletOutputStream = res.getOutputStream();

		workbook.write(servletOutputStream);
		workbook.close();
		servletOutputStream.flush();
		servletOutputStream.close();
	}
	
	
	public void productAddSheetDownload(
			HttpServletResponse res
			) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet bigSort = workbook.createSheet("대분류");
		Sheet middleSort = workbook.createSheet("중분류");
		Sheet smallSort = workbook.createSheet("소분류");
		Sheet product = workbook.createSheet("제품정보");
		Sheet productSpec = workbook.createSheet("제품스펙");
		Sheet productInfo = workbook.createSheet("제품인포");
		
		bigSort.setDefaultColumnWidth(20); // 디폴트 너비 설정
		middleSort.setDefaultColumnWidth(30);
		smallSort.setDefaultColumnWidth(30);
		product.setDefaultColumnWidth(40);
		productInfo.setDefaultColumnWidth(40);
		productSpec.setDefaultColumnWidth(40);

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
		
		
		
		XSSFFont headerXSSFFont = (XSSFFont) workbook.createFont();
		headerXSSFFont.setColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }));

		/**
		 * header cell style
		 */
		XSSFCellStyle headerXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

		// 테두리 설정
		headerXssfCellStyle.setBorderLeft(BorderStyle.THIN);
		headerXssfCellStyle.setBorderRight(BorderStyle.THIN);
		headerXssfCellStyle.setBorderTop(BorderStyle.THIN);
		headerXssfCellStyle.setBorderBottom(BorderStyle.THIN);

		// 배경 설정
		headerXssfCellStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 34, (byte) 37, (byte) 41 }));
		headerXssfCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerXssfCellStyle.setFont(headerXSSFFont);

		/**
		 * body cell style
		 */
		XSSFCellStyle bodyXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

		// 테두리 설정
		bodyXssfCellStyle.setBorderLeft(BorderStyle.THIN);
		bodyXssfCellStyle.setBorderRight(BorderStyle.THIN);
		bodyXssfCellStyle.setBorderTop(BorderStyle.THIN);
		bodyXssfCellStyle.setBorderBottom(BorderStyle.THIN);


		Row headerRow = null;
		Cell headerCell = null;
		Sheet[] sheets = {bigSort, middleSort, smallSort, product, productInfo, productSpec};
		List<String[]> headers = new ArrayList<String[]>();
		headers.add(bigSortHeaderNames);
		headers.add(middleSortHeaderNames);
		headers.add(smallSortHeaderNames);
		headers.add(productHeaderNames);
		headers.add(productInfoHeaderNames);
		headers.add(productSpecHeaderNames);
		
		
		
		for(int x = 0; x < sheets.length; x++) {
			int rowCount = 0; // 데이터가 저장될 행
			headerRow = sheets[x].createRow(rowCount++);
			for(int y=0; y< headers.get(x).length; y++) {
				headerCell = headerRow.createCell(y);
				headerCell.setCellValue(headers.get(x)[y]); // 데이터 추가
				headerCell.setCellStyle(headerXssfCellStyle); // 스타일 추가
			}
			
		}
		
		
		List<BigSort> bigSorts = bigSortRepository.findAll();
		List<MiddleSort> middleSorts = middleSortRepository.findAll();
		List<SmallSort> smallSorts = smallSortRepository.findAll();

		/**
		 * body data
		 */
		String bigBodyDatass[][] = new String[bigSorts.size()][headers.get(0).length];
		String middleBodyDatass[][] = new String[middleSorts.size()][headers.get(1).length];
		String smallBodyDatass[][] = new String[smallSorts.size()][headers.get(2).length];
		int bigRowCount = 1; // 데이터가 저장될 행
		int middleRowCount = 1;
		int smallRowCount = 1;
		Row bigBodyRow = null;
		Row middleBodyRow = null;
		Row smallBodyRow = null;
		Cell bigBodyCell = null;
		Cell middleBodyCell = null;
		Cell smallBodyCell = null;
		int cellNumbber = 0;
		
		for (int x=0; x<bigBodyDatass.length; x++) {
			bigBodyRow = bigSort.createRow(bigRowCount++);
			
			for (int i = 0; i < bigBodyDatass[x].length; i++) {
				bigBodyCell = bigBodyRow.createCell(cellNumbber);
				bigBodyCell.setCellValue(bigSorts.get(x).getId());
				bigBodyCell.setCellStyle(bodyXssfCellStyle);
				
				bigBodyCell = bigBodyRow.createCell(cellNumbber+1);
				bigBodyCell.setCellValue(bigSorts.get(x).getName());
				bigBodyCell.setCellStyle(bodyXssfCellStyle);
			}
		}
		
		for (int x=0; x<middleBodyDatass.length; x++) {
			middleBodyRow = middleSort.createRow(middleRowCount++);
			for (int i = 0; i < middleBodyDatass[x].length; i++) {
				middleBodyCell = middleBodyRow.createCell(cellNumbber);
				middleBodyCell.setCellValue(middleSorts.get(x).getId());
				middleBodyCell.setCellStyle(bodyXssfCellStyle);
				
				middleBodyCell = middleBodyRow.createCell(cellNumbber+1);
				middleBodyCell.setCellValue(middleSorts.get(x).getName());
				middleBodyCell.setCellStyle(bodyXssfCellStyle);
			}
		}
		
		for (int x=0; x<smallBodyDatass.length; x++) {
			smallBodyRow = smallSort.createRow(smallRowCount++);
			for (int i = 0; i < smallBodyDatass[x].length; i++) {
				smallBodyCell = smallBodyRow.createCell(cellNumbber);
				smallBodyCell.setCellValue(smallSorts.get(x).getId());
				smallBodyCell.setCellStyle(bodyXssfCellStyle);
				
				smallBodyCell = smallBodyRow.createCell(cellNumbber+1);
				smallBodyCell.setCellValue(smallSorts.get(x).getName());
				smallBodyCell.setCellStyle(bodyXssfCellStyle);
			}
		}
		
			

		/**
		 * download
		 */
		String fileName = "BIONLIFESCIENCE_PRODUCT_ADD_SHEET";

		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		res.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
		ServletOutputStream servletOutputStream = res.getOutputStream();

		workbook.write(servletOutputStream);
		workbook.close();
		servletOutputStream.flush();
		servletOutputStream.close();
	}
	
	
}
