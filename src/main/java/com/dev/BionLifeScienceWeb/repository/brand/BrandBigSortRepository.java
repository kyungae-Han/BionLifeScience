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

@Repository
public interface BrandBigSortRepository extends JpaRepository<BrandBigSort, Long>{

	List<BrandBigSort> findAllByBrand(Brand brand);
	
	List<BrandBigSort> findAllByOrderByBrandBigSortIndexAsc();
	
	Page<BrandBigSort> findAllByOrderByBrandBigSortIndexAsc(Pageable pageable);
	
	List<BrandBigSort> findAllByBrandOrderByBrandBigSortIndexAsc(Brand brand);
	
	Page<BrandBigSort> findAllByBrandOrderByBrandBigSortIndexAsc(Pageable pageable, Brand brand);
	
	@Query("SELECT MAX(brandBigSortIndex) FROM BrandBigSort")
	Optional<Integer> findFirstIndex();
}
