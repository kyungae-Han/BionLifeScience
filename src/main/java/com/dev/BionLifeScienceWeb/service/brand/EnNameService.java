package com.dev.BionLifeScienceWeb.service.brand;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;
import com.dev.BionLifeScienceWeb.repository.brand.BrandBigSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandMiddleSortRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandSmallSortRepository;

import lombok.RequiredArgsConstructor;

/**
 * 메뉴와 제품에 붙는 영문명을 한 화면에서 채워 넣기 위한 서비스.
 *
 * 브랜드와 분류는 등록 화면이 서로 흩어져 있고 분류는 수정 기능이 아예 없다.
 * 이미 등록된 것들의 영문명을 넣으려면 한 곳에 모아 두는 편이 빠르다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnNameService {

	private final BrandRepository brandRepository;
	private final BrandBigSortRepository brandBigSortRepository;
	private final BrandMiddleSortRepository brandMiddleSortRepository;
	private final BrandSmallSortRepository brandSmallSortRepository;
	private final BrandProductRepository brandProductRepository;

	public List<Brand> brands() {
		return brandRepository.findAllByOrderByBrandIndexAsc();
	}

	public List<BrandBigSort> bigSorts() {
		return brandBigSortRepository.findAllByOrderByBrandBigSortIndexAsc();
	}

	public List<BrandMiddleSort> middleSorts() {
		return brandMiddleSortRepository.findAllByOrderByBrandMiddleSortIndexAsc();
	}

	public List<BrandSmallSort> smallSorts() {
		return brandSmallSortRepository.findAllByOrderByBrandSmallSortIndexAsc();
	}

	public List<BrandProduct> products() {
		return brandProductRepository.findAllByOrderByBrandProductIndexAsc();
	}

	/**
	 * 화면에서 넘어온 값을 저장한다. 입력 이름은 "종류.아이디" 꼴이다 (brand.12, bigSort.3 ...).
	 * 값이 비어 있으면 영문명을 지운다. 지우면 그 자리에는 다시 한글이 나간다.
	 *
	 * @return 실제로 값이 바뀐 건수
	 */
	@Transactional
	public int saveEnglishNames(Map<String, String> params) {
		int changed = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String key = entry.getKey();
			int dot = key.indexOf('.');
			if (dot <= 0) {
				continue;
			}

			Long id;
			try {
				id = Long.valueOf(key.substring(dot + 1));
			} catch (NumberFormatException e) {
				continue;
			}

			String value = entry.getValue() == null ? "" : entry.getValue().trim();
			String newValue = value.isEmpty() ? null : value;

			switch (key.substring(0, dot)) {
				case "brand" -> changed += apply(brandRepository.findById(id).orElse(null), newValue);
				case "bigSort" -> changed += apply(brandBigSortRepository.findById(id).orElse(null), newValue);
				case "middleSort" -> changed += apply(brandMiddleSortRepository.findById(id).orElse(null), newValue);
				case "smallSort" -> changed += apply(brandSmallSortRepository.findById(id).orElse(null), newValue);
				case "product" -> changed += applyProduct(brandProductRepository.findById(id).orElse(null), newValue);
				default -> { /* 화면이 함께 보내는 다른 값(csrf 등)은 그냥 지나간다 */ }
			}
		}
		return changed;
	}

	private int apply(Object entity, String value) {
		if (entity instanceof Brand brand) {
			return set(brand.getNameEn(), value, brand::setNameEn);
		}
		if (entity instanceof BrandBigSort sort) {
			return set(sort.getNameEn(), value, sort::setNameEn);
		}
		if (entity instanceof BrandMiddleSort sort) {
			return set(sort.getNameEn(), value, sort::setNameEn);
		}
		if (entity instanceof BrandSmallSort sort) {
			return set(sort.getNameEn(), value, sort::setNameEn);
		}
		return 0;
	}

	private int applyProduct(BrandProduct product, String value) {
		return product == null ? 0 : set(product.getSubjectEn(), value, product::setSubjectEn);
	}

	private int set(String current, String value, java.util.function.Consumer<String> setter) {
		if (java.util.Objects.equals(current, value)) {
			return 0;
		}
		setter.accept(value);
		return 1;
	}
}
