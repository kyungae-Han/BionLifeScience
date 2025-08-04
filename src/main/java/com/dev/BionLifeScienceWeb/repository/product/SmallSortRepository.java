package com.dev.BionLifeScienceWeb.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.product.MiddleSort;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;

@Repository
public interface SmallSortRepository extends JpaRepository<SmallSort, Long>{

	List<SmallSort> findAllByMiddleSort(MiddleSort middleSort);
	
	List<SmallSort> findAllByMiddleSortOrderBySmallSortIndexAsc(MiddleSort middleSort);
	
	List<SmallSort> findAllByOrderBySmallSortIndexAsc();
	
	Page<SmallSort> findAllByOrderBySmallSortIndexAsc(Pageable pageable);
	
	Page<SmallSort> findAllByMiddleSortOrderBySmallSortIndexAsc(Pageable pageable, MiddleSort middleSort);
	
	@Query("SELECT MAX(smallSortIndex) FROM SmallSort")
	Optional<Integer> findFirstIndex();
	
}
