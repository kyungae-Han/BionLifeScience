package com.dev.BionLifeScienceWeb.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.product.BigSort;
import com.dev.BionLifeScienceWeb.model.product.MiddleSort;

@Repository
public interface MiddleSortRepository extends JpaRepository<MiddleSort, Long>{

	List<MiddleSort> findAllByBigSort(BigSort bigsort);
	
	List<MiddleSort> findAllByOrderByMiddleSortIndexAsc();
	
	List<MiddleSort> findAllByBigSortOrderByMiddleSortIndexAsc(BigSort bigsort);
	
	Page<MiddleSort> findAllByOrderByMiddleSortIndexAsc(Pageable pageable);
	
	Page<MiddleSort> findAllByBigSortOrderByMiddleSortIndexAsc(Pageable pageable, BigSort bigsort);
	
	@Query("SELECT MAX(middleSortIndex) FROM MiddleSort")
	Optional<Integer> findFirstIndex();
	
}
