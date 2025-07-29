package com.dev.BionLifeScienceWeb.repository.brand;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.brand.BrandMiddleSort;
import com.dev.BionLifeScienceWeb.model.brand.BrandSmallSort;

@Repository
public interface BrandSmallSortRepository extends JpaRepository<BrandSmallSort, Long>{

	List<BrandSmallSort> findAllByMiddleSort(BrandMiddleSort middleSort);
	
	List<BrandSmallSort> findAllByMiddleSortOrderByBrandSmallSortIndexAsc(BrandMiddleSort middleSort);
	
	List<BrandSmallSort> findAllByOrderByBrandSmallSortIndexAsc();
	
	Page<BrandSmallSort> findAllByOrderByBrandSmallSortIndexAsc(Pageable pageable);
	
	Page<BrandSmallSort> findAllByMiddleSortOrderByBrandSmallSortIndexAsc(Pageable pageable, BrandMiddleSort middleSort);
	
	@Query("SELECT MAX(brandSmallSortIndex) FROM BrandSmallSort")
	Optional<Integer> findFirstIndex();
}
