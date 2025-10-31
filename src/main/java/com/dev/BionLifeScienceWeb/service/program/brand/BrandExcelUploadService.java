package com.dev.BionLifeScienceWeb.service.program.brand;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductImage;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductInfo;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductSpec;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductImageRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductInfoRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductSpecRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;
import com.dev.BionLifeScienceWeb.service.brand.BrandProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandExcelUploadService {

	private final BrandRepository brandRepository;
	private final BrandProductRepository brandProductRepository;
	private final BrandBigSortRepository brandBigSortRepository;
	private final BrandMiddleSortRepository brandMiddleSortRepository;
	private final BrandSmallSortRepository brandSmallSortRepository;
	private final BrandProductSpecRepository brandProductSpecRepository;
	private final BrandProductInfoRepository brandProductInfoRepository;
	private final BrandProductService brandProductService;
	private final BrandProductImageRepository brandProductImageRepository;


@Transactional
public void uploadExcel(MultipartFile file) throws IOException {

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

        // 1️⃣ Product Sheet에서 상품 데이터 읽기
        Sheet productSheet = workbook.getSheetAt(4);
        for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
            Row row = productSheet.getRow(i);
            if (row == null) continue;

            String codeValue = getCellString(row.getCell(6));
            if (codeValue == null || codeValue.isBlank()) {
                continue;
            }


            BrandProduct product = new BrandProduct();
            product.setBrandProductCode(codeValue);
            Long brandIdValue = getCellLong(row.getCell(0));
            product.setSubject(getCellString(row.getCell(1)));
            product.setContent(getCellString(row.getCell(2)));
            product.setProductSubContent(getCellString(row.getCell(3)));
            product.setTableImagePath(getCellString(row.getCell(4)));
            product.setTableImageRoad(getCellString(row.getCell(5)));
            product.setTableImageName(getCellString(row.getCell(6)));
            product.setSpecImagePath(getCellString(row.getCell(7)));
            product.setSpecImageRoad(getCellString(row.getCell(8)));
            product.setSpecImageName(getCellString(row.getCell(9)));
            product.setSign(false);

            Long smallValue   = getCellLong(row.getCell(11));
            Long middleValue  = getCellLong(row.getCell(12));
            Long bigValue     = getCellLong(row.getCell(13));
            
            if (brandIdValue != null) {
                brandRepository.findById(brandIdValue).ifPresent(product::setBrand);
            } else {
                System.out.println("⚠️ [" + i + "행] 브랜드 ID가 비어있어 건너뜀");
                continue; // 또는 product.setBrand(null); 로 처리 가능
            }

            if (smallValue != null) {
                brandSmallSortRepository.findById(smallValue).ifPresent(product::setSmallSort);
            }
            if (middleValue != null) {
                brandMiddleSortRepository.findById(middleValue).ifPresent(product::setMiddleSort);
            }
            if (bigValue != null) {
                brandBigSortRepository.findById(bigValue).ifPresent(product::setBigSort);
            }
            
            brandProductService.excelInsert(product);
            
            
            if (product.getTableImageRoad() != null && !product.getTableImageRoad().equals("-")) {
                BrandProductImage img = new BrandProductImage();
                img.setProduct(product);
                img.setProductImagePath(product.getTableImagePath());
                img.setProductImageRoad(product.getTableImageRoad());
                img.setProductImageName(product.getTableImageName());
                brandProductImageRepository.save(img);
            }
            
            System.out.println("✅ 신규 상품 등록 완료: " + codeValue);
        }

        
        Sheet productInfoSheet = workbook.getSheetAt(6);
        for (int i = 1; i < productInfoSheet.getPhysicalNumberOfRows(); i++) {
            Row row = productInfoSheet.getRow(i);
            if (row == null) continue;

            String codeValue = getCellString(row.getCell(0));
            String textValue = getCellString(row.getCell(1));
            if (codeValue == null || codeValue.isBlank()) continue;

            // 이미 없는 상품이면 무시
            BrandProduct product = brandProductRepository.findByBrandProductCode(codeValue).orElse(null);
            if (product == null) continue;

            // 이미 등록된 정보면 skip
            if (brandProductInfoRepository.existsByProductAndProductInfoText(product, textValue)) {
                continue;
            }

            BrandProductInfo info = new BrandProductInfo();
            info.setProduct(product);
            info.setProductInfoText(textValue != null ? textValue : "-");
            brandProductInfoRepository.save(info);
        }

        Sheet productSpecSheet = workbook.getSheetAt(5);
        for (int i = 1; i < productSpecSheet.getPhysicalNumberOfRows(); i++) {
            Row row = productSpecSheet.getRow(i);
            if (row == null) continue;

            String codeValue = getCellString(row.getCell(0));
            if (codeValue == null || codeValue.isBlank()) continue;

            BrandProduct product = brandProductRepository.findByBrandProductCode(codeValue).orElse(null);
            if (product == null) continue;

            String subjectValue = getCellString(row.getCell(1));
            String contentValue = getCellString(row.getCell(2));

            // 이미 같은 스펙 내용이 있으면 skip
            if (brandProductSpecRepository.existsByProductAndProductSpecSubjectAndProductSpecContent(
                    product, subjectValue, contentValue)) {
                continue;
            }

            BrandProductSpec spec = new BrandProductSpec();
            spec.setProduct(product);
            spec.setProductSpecSubject(subjectValue);
            spec.setProductSpecContent(contentValue);
            brandProductSpecRepository.save(spec);
        }
    }
}


@Transactional
public void uploadAddExcel(MultipartFile file) throws IOException {

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

        Sheet productSheet = workbook.getSheetAt(4);
        for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
            Row row = productSheet.getRow(i);
            if (row == null) continue;

            String codeValue = getCellString(row.getCell(6));
            if (codeValue == null || codeValue.isBlank()) {
                continue;
            }


            BrandProduct product = new BrandProduct();
            product.setBrandProductCode(codeValue);
            Long brandIdValue = getCellLong(row.getCell(0)); //BRAND_ID
            product.setSubject(getCellString(row.getCell(1))); //간략설명
            product.setContent(getCellString(row.getCell(2))); //설명
            product.setProductSubContent(getCellString(row.getCell(3)));
            product.setTableImagePath(getCellString(row.getCell(4)));
            product.setTableImageRoad(getCellString(row.getCell(5)));
            product.setTableImageName(getCellString(row.getCell(6)));
            product.setSpecImagePath(getCellString(row.getCell(7)));
            product.setSpecImageRoad(getCellString(row.getCell(8)));
            product.setSpecImageName(getCellString(row.getCell(9)));
            product.setSign(false);

            Long smallValue   = getCellLong(row.getCell(11));
            Long middleValue  = getCellLong(row.getCell(12));
            Long bigValue     = getCellLong(row.getCell(13));
            

            if (brandIdValue != null) {
                brandRepository.findById(brandIdValue).ifPresent(product::setBrand);
            } else {
                continue;
            }

            if (smallValue != null) {
                brandSmallSortRepository.findById(smallValue).ifPresent(product::setSmallSort);
            }
            if (middleValue != null) {
                brandMiddleSortRepository.findById(middleValue).ifPresent(product::setMiddleSort);
            }
            if (bigValue != null) {
                brandBigSortRepository.findById(bigValue).ifPresent(product::setBigSort);
            }

            brandProductService.excelInsert(product);
            
            if (product.getTableImageRoad() != null && !product.getTableImageRoad().equals("-")) {
                BrandProductImage img = new BrandProductImage();
                img.setProduct(product);
                img.setProductImagePath(product.getTableImagePath());
                img.setProductImageRoad(product.getTableImageRoad());
                img.setProductImageName(product.getTableImageName());
                brandProductImageRepository.save(img);
            }
            
            System.out.println("✅ 신규 상품 추가 완료: " + codeValue);
        }

        // 2️⃣ ProductInfo Sheet에서 상품 상세정보 추가
        Sheet productInfoSheet = workbook.getSheetAt(6);
        for (int i = 1; i < productInfoSheet.getPhysicalNumberOfRows(); i++) {
            Row row = productInfoSheet.getRow(i);
            if (row == null) continue;

            String codeValue = getCellString(row.getCell(0));
            String textValue = getCellString(row.getCell(1));
            if (codeValue == null || codeValue.isBlank()) continue;

            BrandProduct product = brandProductRepository.findByBrandProductCode(codeValue).orElse(null);
            if (product == null) continue;

            // ✅ 이미 동일 ProductInfo 존재하면 skip
            if (brandProductInfoRepository.existsByProductAndProductInfoText(product, textValue)) {
                continue;
            }

            BrandProductInfo info = new BrandProductInfo();
            info.setProduct(product);
            info.setProductInfoText(textValue != null ? textValue : "-");
            brandProductInfoRepository.save(info);
            System.out.println("✅ ProductInfo 추가 완료: " + codeValue);
        }

        // 3️⃣ ProductSpec Sheet에서 스펙정보 추가
        Sheet productSpecSheet = workbook.getSheetAt(5);
        for (int i = 1; i < productSpecSheet.getPhysicalNumberOfRows(); i++) {
            Row row = productSpecSheet.getRow(i);
            if (row == null) continue;

            String codeValue = getCellString(row.getCell(0));
            if (codeValue == null || codeValue.isBlank()) continue;

            BrandProduct product = brandProductRepository.findByBrandProductCode(codeValue).orElse(null);
            if (product == null) continue;

            String subjectValue = getCellString(row.getCell(1));
            String contentValue = getCellString(row.getCell(2));

            // ✅ 동일한 Spec 이미 존재하면 skip
            if (brandProductSpecRepository.existsByProductAndProductSpecSubjectAndProductSpecContent(
                    product, subjectValue, contentValue)) {
                System.out.println("⚠️ [" + codeValue + "] 동일한 Spec 존재 → 건너뜀");
                continue;
            }

            BrandProductSpec spec = new BrandProductSpec();
            spec.setProduct(product);
            spec.setProductSpecSubject(subjectValue);
            spec.setProductSpecContent(contentValue);
            brandProductSpecRepository.save(spec);
            System.out.println("✅ ProductSpec 추가 완료: " + codeValue);
        }
    }
}

	
	
	// ✅ 셀을 안전하게 문자열로 변환
	private String getCellString(Cell cell) {
	    if (cell == null) return null;
	    try {
	        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
	        String value = cell.getStringCellValue();
	        return (value != null) ? value.trim() : null;
	    } catch (Exception e) {
	        System.out.println("⚠️ getCellString 변환 오류: " + e.getMessage());
	        return null;
	    }
	}

	// ✅ 셀을 안전하게 Long 값으로 변환
	private Long getCellLong(Cell cell) {
	    if (cell == null) return null;

	    switch (cell.getCellType()) {
	        case STRING:
	            String str = cell.getStringCellValue().trim();
	            if (str.isEmpty()) return null;
	            try {
	                return Long.parseLong(str);
	            } catch (NumberFormatException e) {
	                return null;
	            }

	        case NUMERIC:
	            return (long) cell.getNumericCellValue();

	        default:
	            return null;
	    }
	}

}
