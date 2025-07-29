package com.dev.BionLifeScienceWeb.repository.brand;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandBigSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;

@Repository
public interface BrandProductRepository extends JpaRepository<BrandProduct, Long>{

	Page<BrandProduct> findAllBySmallSort(Pageable pageable, BrandSmallSort brandSmallSort);
	
	Page<BrandProduct> findAllByMiddleSort(Pageable pageable, BrandMiddleSort brandMiddleSort);
	
	Page<BrandProduct> findAllByBigSort(Pageable pageable, BrandBigSort brandBigSort);
	
	Page<BrandProduct> findAllByBrand(Pageable pageable, Brand brand);
	
	Page<BrandProduct> findAll(Pageable pageble);

	List<BrandProduct> findAllBySign(Boolean sign);
	
	List<BrandProduct> findBySubjectContains(String subject);
	
	Page<BrandProduct> findAllBySubjectContainsOrderByIdDesc(Pageable pageable, String searchWord);
	
	Page<BrandProduct> findAllBySmallSortAndSubjectContainsOrderByIdDesc(Pageable pageable, BrandSmallSort smallSort, String searchWord);
	
	Page<BrandProduct> findAllByMiddleSortAndSubjectContainsOrderByIdDesc(Pageable pageable, BrandMiddleSort middleSort, String searchWord);
	
	Page<BrandProduct> findAllByBigSortAndSubjectContainsOrderByIdDesc(Pageable pageable, BrandBigSort bigSort, String searchWord);
	
	Page<BrandProduct> findAllBySmallSortOrderByBrandProductIndexAsc(Pageable pageable, BrandSmallSort smallSort);
	
	Page<BrandProduct> findAllByOrderByBrandProductIndexAsc(Pageable pageble);
	
	Page<BrandProduct> findAllByBigSortOrderByBrandProductIndexAsc(Pageable pageable, BrandBigSort bigSort);
	
	Page<BrandProduct> findAllByMiddleSortOrderByBrandProductIndexAsc(Pageable pageable, BrandMiddleSort bigSort);
	
	Page<BrandProduct> findAllBySmallSortAndSubjectContainsOrderByBrandProductIndexAsc(Pageable pageable, BrandSmallSort smallSort, String subject);
	
	Page<BrandProduct> findAllBySubjectContainsOrderByBrandProductIndexAsc(Pageable pageble, String subject);
	
	Page<BrandProduct> findAllByBigSortAndSubjectContainsOrderByBrandProductIndexAsc(Pageable pageable, BrandBigSort bigSort, String subject);
	
	Page<BrandProduct> findAllByMiddleSortAndSubjectContainsOrderByBrandProductIndexAsc(Pageable pageable, BrandMiddleSort bigSort, String subject);

	List<BrandProduct> findAllBySignOrderByBrandProductIndexAsc(Boolean sign);
	
	List<BrandProduct> findAllByOrderByBrandProductIndexAsc();	
	
	Page<BrandProduct> findAllByBrandAndSubjectContainsOrderByIdDesc(Pageable pageable, Brand brand, String searchWord);
	
	Optional<BrandProduct> findByBrandProductCode(String code);
	
	@Query("SELECT MAX(brandProductIndex) FROM BrandProduct")
	Optional<Integer> findFirstIndex();
}
