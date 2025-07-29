package com.dev.BionLifeScienceWeb.service.program.brand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
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

@Service
public class BrandCheckService {

	@Autowired
	BrandRepository brandRepository;

	@Autowired
	BrandProductRepository productRepository;

	@Autowired
	BrandBigSortRepository bigSortRepository;

	@Autowired
	BrandMiddleSortRepository middleSortRepository;

	@Autowired
	BrandSmallSortRepository smallSortRepository;

	@Autowired
	BrandProductSpecRepository productSpecRepository;

	@Autowired
	BrandProductInfoRepository productInfoRepository;

	@Autowired
	BrandProductImageRepository productImageRepository;

	@Autowired
	BrandProductFileRepository productFileRepository;

	@Value("${spring.upload.path}")
	private String commonPath;

	public List<String> resetExcelCheck(MultipartFile file) throws IOException {
		List<String> result = new ArrayList<String>();

		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		try {
			// 브랜드 ID 가 DB와 일치하는지 체크
			// 브랜드에 빈 ROW가 있는지 체크
			Sheet brandSheet = workbook.getSheetAt(0);
			List<Long> brandSheetList = new ArrayList<Long>();
			List<Long> brandDBList = new ArrayList<Long>();
			List<Brand> brands = brandRepository.findAll();

			for (Brand b : brands) {
				brandDBList.add(b.getId());
			}

			for (int i = 1; i < brandSheet.getPhysicalNumberOfRows(); i++) {
				Row row = brandSheet.getRow(i);
				Cell brandId = row.getCell(0);
				Long brandIdValue = (long) brandId.getNumericCellValue();
				brandSheetList.add(brandIdValue);
			}

			for (int x = 0; x < brandSheetList.size(); x++) {
				if (brandSheetList.get(x) == 0l || !brandDBList.contains(brandSheetList.get(x))) {
					result.add("브랜드의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 대분류 ID가 DB와 일치하는지 체크
			// 대분류에 빈 ROW가 있는지 체크
			Sheet bigSortSheet = workbook.getSheetAt(1);
			List<Long> bigSortSheetList = new ArrayList<Long>();
			List<Long> bigSortDBList = new ArrayList<Long>();
			List<BrandBigSort> bigSorts = bigSortRepository.findAll();

			for (BrandBigSort big : bigSorts) {
				bigSortDBList.add(big.getId());
			}

			for (int i = 1; i < bigSortSheet.getPhysicalNumberOfRows(); i++) {
				Row row = bigSortSheet.getRow(i);
				Cell bigSortId = row.getCell(0);
				Long bigValue = (long) bigSortId.getNumericCellValue();
				bigSortSheetList.add(bigValue);
			}

			for (int x = 0; x < bigSortSheetList.size(); x++) {
				if (bigSortSheetList.get(x) == 0l || !bigSortDBList.contains(bigSortSheetList.get(x))) {
					result.add("대분류의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 중분류 ID가 DB와 일치하는지 체크
			// 중분류에 빈 ROW가 있는지 체크
			Sheet middleSortSheet = workbook.getSheetAt(2);
			List<Long> middleSortSheetList = new ArrayList<Long>();
			List<Long> middleSortDBList = new ArrayList<Long>();
			List<BrandMiddleSort> middleSorts = middleSortRepository.findAll();

			for (BrandMiddleSort middle : middleSorts) {
				middleSortDBList.add(middle.getId());
			}

			for (int i = 1; i < middleSortSheet.getPhysicalNumberOfRows(); i++) {
				Row row = middleSortSheet.getRow(i);
				Cell middleSortId = row.getCell(0);
				Long middleValue = (long) middleSortId.getNumericCellValue();
				middleSortSheetList.add(middleValue);
			}

			for (int x = 0; x < middleSortSheetList.size(); x++) {
				if (middleSortSheetList.get(x) == 0l || !middleSortDBList.contains(middleSortSheetList.get(x))) {
					result.add("중분류의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 소분류 ID가 DB와 일치하는지 체크
			// 소분류에 빈 ROW가 있는지 체크
			Sheet smallSortSheet = workbook.getSheetAt(3);
			List<Long> smallSortSheetList = new ArrayList<Long>();
			List<Long> smallSortDBList = new ArrayList<Long>();
			List<BrandSmallSort> smallSorts = smallSortRepository.findAll();

			for (BrandSmallSort small : smallSorts) {
				smallSortDBList.add(small.getId());
			}

			for (int i = 1; i < smallSortSheet.getPhysicalNumberOfRows(); i++) {
				Row row = smallSortSheet.getRow(i);
				Cell smallSortId = row.getCell(0);
				Long smallValue = (long) smallSortId.getNumericCellValue();
				smallSortSheetList.add(smallValue);
			}

			for (int x = 0; x < smallSortSheetList.size(); x++) {
				if (smallSortSheetList.get(x) == 0l || !smallSortDBList.contains(smallSortSheetList.get(x))) {
					result.add("소분류의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 제품 SHEET에서 존재하지 않는 대분류 ID 체크
			// 제품 SHEET에서 존재하지 않는 중분류 ID 체크
			// 제품 SHEET에서 존재하지 않는 소분류 ID 체크
			Sheet productSheet = workbook.getSheetAt(4);
			List<Long> productSheetBrandList = new ArrayList<Long>();
			List<Long> productSheetBigSortList = new ArrayList<Long>();
			List<String> productSheetSubjectList = new ArrayList<String>();
			List<String> productSheetContentList = new ArrayList<String>();
			List<String> productSheetSignList = new ArrayList<String>();

			List<Long> productSheetMiddleSortList = new ArrayList<Long>();
			List<Long> productSheetSmallSortList = new ArrayList<Long>();
			List<String> productSheetProductCodeList = new ArrayList<String>();

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell productCode = row.getCell(0);
				String productCodeValue = productCode + "";
				productSheetProductCodeList.add(productCodeValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell productSubject = row.getCell(1);
				String productSubjectValue = productSubject + "";
				productSheetSubjectList.add(productSubjectValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell productContent = row.getCell(2);
				String productContentValue = productContent + "";
				productSheetContentList.add(productContentValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell smallSortId = row.getCell(3);
				Long smallValue = (long) smallSortId.getNumericCellValue();
				productSheetSmallSortList.add(smallValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell middleSortId = row.getCell(4);
				Long middleSortIdValue = (long) middleSortId.getNumericCellValue();
				productSheetMiddleSortList.add(middleSortIdValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell bigSortId = row.getCell(5);
				Long bigSortIdValue = (long) bigSortId.getNumericCellValue();
				productSheetBigSortList.add(bigSortIdValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell brandId = row.getCell(6);
				Long brandIdValue = (long) brandId.getNumericCellValue();
				productSheetBrandList.add(brandIdValue);
			}

			for (int x = 0; x < productSheetSmallSortList.size(); x++) {
				if (productSheetSmallSortList.get(x) == 0l
						|| !smallSortDBList.contains(productSheetSmallSortList.get(x))) {
					result.add("제품(PRODUCT) 시트 소분류 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			for (int x = 0; x < productSheetMiddleSortList.size(); x++) {
				if (productSheetMiddleSortList.get(x) == 0l
						|| !middleSortDBList.contains(productSheetMiddleSortList.get(x))) {
					result.add("제품(PRODUCT) 시트 중분류 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			for (int x = 0; x < productSheetBigSortList.size(); x++) {
				if (productSheetBigSortList.get(x) == 0l || !bigSortDBList.contains(productSheetBigSortList.get(x))) {
					result.add("제품(PRODUCT) 시트 대분류 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			for (int x = 0; x < productSheetBrandList.size(); x++) {
				if (productSheetBrandList.get(x) == 0l || !brandDBList.contains(productSheetBrandList.get(x))) {
					result.add("제품(PRODUCT) 시트 브랜드 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// PRODUCT SHEET PRODUCT CODE 중복 체크
			for (int x = 0; x < productSheetProductCodeList.size(); x++) {
				for (int y = 0; y < x; y++) {
					if (productSheetProductCodeList.get(x).equals(productSheetProductCodeList.get(y))
							&& !productSheetProductCodeList.get(x).equals("")) {
						result.add((x + 2) + "행과 " + (y + 2) + "행이 중복되었습니다. 제품 코드를 중복되지 않게 입력 해 주세요.");
					}
				}
			}

			// PRODUCT SHEET PRODUCT CODE NULL 체크
			for (int x = 0; x < productSheetProductCodeList.size(); x++) {
				if (productSheetProductCodeList.get(x).equals("")) {
					result.add("제품(PRODUCT) 시트의 PRODUCT CODE의 " + (x + 2) + "행이 빈 값입니다.");
				}
			}

			// PRODUCT SHEET SUBJECT NULL 체크
			for (int x = 0; x < productSheetSubjectList.size(); x++) {
				if (productSheetSubjectList.get(x).equals("")) {
					result.add("제품(PRODUCT) 시트의 PRODUCT SUBJECT의 " + (x + 2) + "행이 빈 값입니다.");
				}
			}

			// PRODUCT SHEET CONTENT NULL 체크
			for (int x = 0; x < productSheetContentList.size(); x++) {
				if (productSheetContentList.get(x).equals("")) {
					result.add("제품(PRODUCT) 시트의 PRODUCT CONTENT의 " + (x + 2) + "행이 빈 값입니다.");
				}
			}

			// SPEC SHEET에서 존재하지 않는 PRODUCT CODE 체크
			Sheet specSheet = workbook.getSheetAt(5);
			List<String> specSheetProductCodeList = new ArrayList<String>();
			for (int i = 1; i < specSheet.getPhysicalNumberOfRows(); i++) {
				Row row = specSheet.getRow(i);
				Cell productCode = row.getCell(0);
				String productCodeValue = productCode + "";
				specSheetProductCodeList.add(productCodeValue);
			}

			for (int x = 0; x < specSheetProductCodeList.size(); x++) {
				if (!productSheetProductCodeList.contains(specSheetProductCodeList.get(x))
						|| specSheetProductCodeList.get(x).equals("")) {
					result.add("제품 스펙 SHEET의 " + (x + 2) + "번째 행의 제품 코드가 빈 값 이거나 제품 SHEET에 존재하지 않습니다. 다시 확인해 주세요.");
				}
			}

			// INFO SHEET에서 존재하지 않는 PRODUCT CODE 체크
			Sheet infoSheet = workbook.getSheetAt(6);
			List<String> infoSheetProductCodeList = new ArrayList<String>();
			for (int i = 1; i < infoSheet.getPhysicalNumberOfRows(); i++) {
				Row row = infoSheet.getRow(i);
				Cell productCode = row.getCell(0);
				String productCodeValue = productCode + "";
				infoSheetProductCodeList.add(productCodeValue);
			}

			for (int x = 0; x < infoSheetProductCodeList.size(); x++) {
				if (!productSheetProductCodeList.contains(infoSheetProductCodeList.get(x))
						|| infoSheetProductCodeList.get(x).equals("")) {
					result.add("제품 인포 SHEET의 " + (x + 2) + "번째 행의 제품 코드가 빈 값 이거나 제품 SHEET에 존재하지 않습니다. 다시 확인해 주세요.");
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		if (result.size() > 0) {
			return result;
		} else {
			result.add("success");
			return result;
		}
	}

	public List<String> addExcelCheck(MultipartFile file) throws IOException {
		List<String> result = new ArrayList<String>();

		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		try {
			// 브랜드 ID 가 DB와 일치하는지 체크
			// 브랜드에 빈 ROW가 있는지 체크
			Sheet brandSheet = workbook.getSheetAt(0);
			List<Long> brandSheetList = new ArrayList<Long>();
			List<Long> brandDBList = new ArrayList<Long>();
			List<Brand> brands = brandRepository.findAll();

			for (Brand b : brands) {
				brandDBList.add(b.getId());
			}

			for (int i = 1; i < brandSheet.getPhysicalNumberOfRows(); i++) {
				Row row = brandSheet.getRow(i);
				Cell brandId = row.getCell(0);
				Long brandIdValue = (long) brandId.getNumericCellValue();
				brandSheetList.add(brandIdValue);
			}

			for (int x = 0; x < brandSheetList.size(); x++) {
				if (brandSheetList.get(x) == 0l || !brandDBList.contains(brandSheetList.get(x))) {
					result.add("브랜드의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 대분류 ID가 DB와 일치하는지 체크
			// 대분류에 빈 ROW가 있는지 체크
			Sheet bigSortSheet = workbook.getSheetAt(0);
			List<Long> bigSortSheetList = new ArrayList<Long>();
			List<Long> bigSortDBList = new ArrayList<Long>();
			List<BrandBigSort> bigSorts = bigSortRepository.findAll();

			for (BrandBigSort big : bigSorts) {
				bigSortDBList.add(big.getId());
			}

			for (int i = 1; i < bigSortSheet.getPhysicalNumberOfRows(); i++) {
				Row row = bigSortSheet.getRow(i);
				Cell bigSortId = row.getCell(1);
				Long bigValue = (long) bigSortId.getNumericCellValue();
				bigSortSheetList.add(bigValue);
			}

			for (int x = 0; x < bigSortSheetList.size(); x++) {
				if (bigSortSheetList.get(x) == 0l || !bigSortDBList.contains(bigSortSheetList.get(x))) {
					result.add("대분류의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 중분류 ID가 DB와 일치하는지 체크
			// 중분류에 빈 ROW가 있는지 체크
			Sheet middleSortSheet = workbook.getSheetAt(2);
			List<Long> middleSortSheetList = new ArrayList<Long>();
			List<Long> middleSortDBList = new ArrayList<Long>();
			List<BrandMiddleSort> middleSorts = middleSortRepository.findAll();

			for (BrandMiddleSort middle : middleSorts) {
				middleSortDBList.add(middle.getId());
			}

			for (int i = 1; i < middleSortSheet.getPhysicalNumberOfRows(); i++) {
				Row row = middleSortSheet.getRow(i);
				Cell middleSortId = row.getCell(0);
				Long middleValue = (long) middleSortId.getNumericCellValue();
				middleSortSheetList.add(middleValue);
			}

			for (int x = 0; x < middleSortSheetList.size(); x++) {
				if (middleSortSheetList.get(x) == 0l || !middleSortDBList.contains(middleSortSheetList.get(x))) {
					result.add("중분류의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 소분류 ID가 DB와 일치하는지 체크
			// 소분류에 빈 ROW가 있는지 체크
			Sheet smallSortSheet = workbook.getSheetAt(3);
			List<Long> smallSortSheetList = new ArrayList<Long>();
			List<Long> smallSortDBList = new ArrayList<Long>();
			List<BrandSmallSort> smallSorts = smallSortRepository.findAll();

			for (BrandSmallSort small : smallSorts) {
				smallSortDBList.add(small.getId());
			}

			for (int i = 1; i < smallSortSheet.getPhysicalNumberOfRows(); i++) {
				Row row = smallSortSheet.getRow(i);
				Cell smallSortId = row.getCell(0);
				Long smallValue = (long) smallSortId.getNumericCellValue();
				smallSortSheetList.add(smallValue);
			}

			for (int x = 0; x < smallSortSheetList.size(); x++) {
				if (smallSortSheetList.get(x) == 0l || !smallSortDBList.contains(smallSortSheetList.get(x))) {
					result.add("소분류의 " + (x + 2) + "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// 제품 SHEET에서 존재하지 않는 대분류 ID 체크
			// 제품 SHEET에서 존재하지 않는 중분류 ID 체크
			// 제품 SHEET에서 존재하지 않는 소분류 ID 체크
			Sheet productSheet = workbook.getSheetAt(4);
			List<Long> productSheetBrandList = new ArrayList<Long>();
			List<Long> productSheetBigSortList = new ArrayList<Long>();
			List<String> productSheetSubjectList = new ArrayList<String>();
			List<String> productSheetContentList = new ArrayList<String>();

			List<Long> productSheetMiddleSortList = new ArrayList<Long>();
			List<Long> productSheetSmallSortList = new ArrayList<Long>();
			List<String> productSheetProductCodeList = new ArrayList<String>();

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell productCode = row.getCell(0);
				String productCodeValue = productCode + "";
				productSheetProductCodeList.add(productCodeValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell productSubject = row.getCell(1);
				String productSubjectValue = productSubject + "";
				productSheetSubjectList.add(productSubjectValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell productContent = row.getCell(2);
				String productContentValue = productContent + "";
				productSheetContentList.add(productContentValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell smallSortId = row.getCell(3);
				Long smallValue = (long) smallSortId.getNumericCellValue();
				productSheetSmallSortList.add(smallValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell middleSortId = row.getCell(4);
				Long middleSortIdValue = (long) middleSortId.getNumericCellValue();
				productSheetMiddleSortList.add(middleSortIdValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell bigSortId = row.getCell(5);
				Long bigSortIdValue = (long) bigSortId.getNumericCellValue();
				productSheetBigSortList.add(bigSortIdValue);
			}

			for (int i = 1; i < productSheet.getPhysicalNumberOfRows(); i++) {
				Row row = productSheet.getRow(i);
				Cell brandId = row.getCell(6);
				Long brandIdValue = (long) brandId.getNumericCellValue();
				productSheetBrandList.add(brandIdValue);
			}

			for (int x = 0; x < productSheetSmallSortList.size(); x++) {
				if (productSheetSmallSortList.get(x) == 0l
						|| !smallSortDBList.contains(productSheetSmallSortList.get(x))) {
					result.add("제품(PRODUCT) 시트 소분류 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			for (int x = 0; x < productSheetMiddleSortList.size(); x++) {
				if (productSheetMiddleSortList.get(x) == 0l
						|| !middleSortDBList.contains(productSheetMiddleSortList.get(x))) {
					result.add("제품(PRODUCT) 시트 중분류 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			for (int x = 0; x < productSheetBigSortList.size(); x++) {
				if (productSheetBigSortList.get(x) == 0l || !bigSortDBList.contains(productSheetBigSortList.get(x))) {
					result.add("제품(PRODUCT) 시트 대분류 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			for (int x = 0; x < productSheetBrandList.size(); x++) {
				if (productSheetBrandList.get(x) == 0l || !brandDBList.contains(productSheetBrandList.get(x))) {
					result.add("제품(PRODUCT) 시트 브랜드 ID의 " + (x + 2)
							+ "번째 행이 빈 값 혹은 잘못된 숫자 입니다. 새로 RESET용 EXCEL을 다운로드 하여 사용 해 주시기 바랍니다.");
				}
			}

			// PRODUCT SHEET PRODUCT CODE 중복 체크
			for (int x = 0; x < productSheetProductCodeList.size(); x++) {
				for (int y = 0; y < x; y++) {
					if (productSheetProductCodeList.get(x).equals(productSheetProductCodeList.get(y))
							&& !productSheetProductCodeList.get(x).equals("")) {
						result.add((x + 2) + "행과 " + (y + 2) + "행이 중복되었습니다. 제품 코드를 중복되지 않게 입력 해 주세요.");
					}
				}
			}

			// PRODUCT SHEET PRODUCT CODE NULL 체크
			for (int x = 0; x < productSheetProductCodeList.size(); x++) {
				if (productSheetProductCodeList.get(x).equals("")) {
					result.add("제품(PRODUCT) 시트의 PRODUCT CODE의 " + (x + 2) + "행이 빈 값입니다.");
				}
			}

			// PRODUCT SHEET SUBJECT NULL 체크
			for (int x = 0; x < productSheetSubjectList.size(); x++) {
				if (productSheetSubjectList.get(x).equals("")) {
					result.add("제품(PRODUCT) 시트의 PRODUCT SUBJECT의 " + (x + 2) + "행이 빈 값입니다.");
				}
			}

			// PRODUCT SHEET CONTENT NULL 체크
			for (int x = 0; x < productSheetContentList.size(); x++) {
				if (productSheetContentList.get(x).equals("")) {
					result.add("제품(PRODUCT) 시트의 PRODUCT CONTENT의 " + (x + 2) + "행이 빈 값입니다.");
				}
			}

			// SPEC SHEET에서 존재하지 않는 PRODUCT CODE 체크
			Sheet specSheet = workbook.getSheetAt(5);
			List<String> specSheetProductCodeList = new ArrayList<String>();
			for (int i = 1; i < specSheet.getPhysicalNumberOfRows(); i++) {
				Row row = specSheet.getRow(i);
				Cell productCode = row.getCell(0);
				String productCodeValue = productCode + "";
				specSheetProductCodeList.add(productCodeValue);
			}

			for (int x = 0; x < specSheetProductCodeList.size(); x++) {
				if (!productSheetProductCodeList.contains(specSheetProductCodeList.get(x))
						|| specSheetProductCodeList.get(x).equals("")) {
					result.add("제품 스펙 SHEET의 " + (x + 2) + "번째 행의 제품 코드가 빈 값 이거나 제품 SHEET에 존재하지 않습니다. 다시 확인해 주세요.");
				}
			}

			// INFO SHEET에서 존재하지 않는 PRODUCT CODE 체크
			Sheet infoSheet = workbook.getSheetAt(6);
			List<String> infoSheetProductCodeList = new ArrayList<String>();
			for (int i = 1; i < infoSheet.getPhysicalNumberOfRows(); i++) {
				Row row = infoSheet.getRow(i);
				Cell productCode = row.getCell(0);
				String productCodeValue = productCode + "";
				infoSheetProductCodeList.add(productCodeValue);
			}

			for (int x = 0; x < infoSheetProductCodeList.size(); x++) {
				if (!productSheetProductCodeList.contains(infoSheetProductCodeList.get(x))
						|| infoSheetProductCodeList.get(x).equals("")) {
					result.add("제품 인포 SHEET의 " + (x + 2) + "번째 행의 제품 코드가 빈 값 이거나 제품 SHEET에 존재하지 않습니다. 다시 확인해 주세요.");
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		if (result.size() > 0) {
			return result;
		} else {
			result.add("success");
			return result;
		}
	}

	public synchronized List<String> resetZipCheck(MultipartFile file) throws IOException {
		List<String> result = new ArrayList<String>();
		// 폴더명이 제대로 되었는지(COMPANY) 체크
		// 제품 코드의 숫자와 DB에 있는 제품 코드의 숫자를 비교하여 없는 코드 체크
		// 내부 폴더명이 제대로 되어있는지 체크
		// 내부 폴더의 파일 수 체크

		List<BrandProduct> products = productRepository.findAll();
		List<String> dbProrductCodes = new ArrayList<String>();
		for (BrandProduct p : products) {
			dbProrductCodes.add(p.getBrandProductCode());
		}
		List<String> folderProductCodes = new ArrayList<String>();

		File exFile = new File(commonPath + "/tempfile");
		if (exFile.exists() && exFile.isDirectory()) {
			try {
				FileUtils.cleanDirectory(exFile);
				exFile.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		File tempFile = new File(commonPath + "/tempfile");
		try {
			tempFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.write(file.getBytes());
			fos.close();
			ZipUtil.explode(tempFile);

			Thread.sleep(3000);

			for (File product : tempFile.listFiles()) {
				if (product.isDirectory()) {
					String productCode = product.getName();
					folderProductCodes.add(productCode);

					if (product.listFiles().length > 0) {
						for (int a = 0; a < product.listFiles().length; a++) {
							if (product.listFiles()[a].isDirectory()) {
								String fileType = product.listFiles()[a].getName();
								if (!fileType.equals("slide") && !fileType.equals("files") && !fileType.equals("spec")
										&& !fileType.equals("overview")) {
									result.add("제품코드 " + productCode
											+ "의 폴더 내에 세부 폴더 명이 정확하지 않습니다. files / slide / overview / spec 중 하나로 입력 해 주세요.");
								} else if (product.listFiles()[a].getName().equals("spec")
										&& product.listFiles()[a].listFiles().length > 1) {
									result.add("제품코드 " + productCode + "의 spec 폴더 내에는 하나의 파일만 존재할 수 있습니다.");
								} else if (product.listFiles()[a].getName().equals("overview")
										&& product.listFiles()[a].listFiles().length > 1) {
									result.add("제품코드 " + productCode + "의 overview 폴더 내에는 하나의 파일만 존재할 수 있습니다.");
								}

							} else {
								result.add("제품코드 " + productCode + "의 폴더 내의 " + product.listFiles()[a].getName()
										+ "파일은 폴더가 아닙니다.");
							}
						}
					}
				} else {
					result.add("제품코드 영역의 " + product.getName() + "파일은 폴더가 아닙니다.");
				}
			}

			for (int a = 0; a < folderProductCodes.size(); a++) {
				if (!dbProrductCodes.contains(folderProductCodes.get(a))) {
					result.add("제품코드 " + folderProductCodes.get(a) + "는 존재하지 않는 제품 코드 입니다. 다시 확인 해 주세요.");
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		if (result.size() > 0) {
			return result;
		} else {
			result.add("success");
			return result;
		}
	}

	public synchronized List<String> addZipCheck(MultipartFile file) throws IOException {

		List<String> result = new ArrayList<String>();
		// 폴더명이 제대로 되었는지(COMPANY) 체크
		// 제품 코드의 숫자와 DB에 있는 제품 코드의 숫자를 비교하여 없는 코드 체크
		// 내부 폴더명이 제대로 되어있는지 체크
		// 내부 폴더의 파일 수 체크

		List<BrandProduct> products = productRepository.findAll();
		List<String> dbProrductCodes = new ArrayList<String>();
		for (BrandProduct p : products) {
			dbProrductCodes.add(p.getBrandProductCode());
		}
		List<String> folderProductCodes = new ArrayList<String>();

		File exFile = new File(commonPath + "/tempfile");
		if (exFile.exists() && exFile.isDirectory()) {
			try {
				FileUtils.cleanDirectory(exFile);
				exFile.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		File tempFile = new File(commonPath + "/tempfile");
		try {
			tempFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.write(file.getBytes());
			fos.close();
			ZipUtil.explode(tempFile);

			Thread.sleep(3000);

			for (File product : tempFile.listFiles()) {
				if (product.isDirectory()) {
					String productCode = product.getName();
					System.out.println("first loop");
					folderProductCodes.add(productCode);

					if (product.listFiles().length > 0) {
						for (int a = 0; a < product.listFiles().length; a++) {
							System.out.println("second loop");
							if (product.listFiles()[a].isDirectory()) {
								String fileType = product.listFiles()[a].getName();
								if (!fileType.equals("slide") && !fileType.equals("files") && !fileType.equals("spec")
										&& !fileType.equals("overview")) {
									result.add("제품코드 " + productCode
											+ "의 폴더 내에 세부 폴더 명이 정확하지 않습니다. files / slide / overview / spec 중 하나로 입력 해 주세요.");
								} else if (product.listFiles()[a].getName().equals("spec")
										&& product.listFiles()[a].listFiles().length > 1) {
									result.add("제품코드 " + productCode + "의 spec 폴더 내에는 하나의 파일만 존재할 수 있습니다.");
								} else if (product.listFiles()[a].getName().equals("overview")
										&& product.listFiles()[a].listFiles().length > 1) {
									result.add("제품코드 " + productCode + "의 overview 폴더 내에는 하나의 파일만 존재할 수 있습니다.");
								}

							} else {
								result.add("제품코드 " + productCode + "의 폴더 내의 " + product.listFiles()[a].getName()
										+ "파일은 폴더가 아닙니다.");
							}
						}
					}
				} else {
					result.add("제품코드 영역의 " + product.getName() + "파일은 폴더가 아닙니다.");
				}
			}

			for (int a = 0; a < folderProductCodes.size(); a++) {
				if (!dbProrductCodes.contains(folderProductCodes.get(a))) {
					result.add("제품코드 " + folderProductCodes.get(a) + "는 존재하지 않는 제품 코드 입니다. 다시 확인 해 주세요.");
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		if (result.size() > 0) {
			return result;
		} else {
			result.add("success");
			return result;
		}
	}
}
