package com.dev.BionLifeScienceWeb.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.product.BigSort;

@Repository
public interface BigSortRepository extends JpaRepository<BigSort, Long>{

	List<BigSort> findAllByOrderByBigSortIndexAsc();
	
	Page<BigSort> findAllByOrderByBigSortIndexAsc(Pageable pageable);
	
	@Query("SELECT MAX(bigSortIndex) FROM BigSort")
	Optional<Integer> findFirstIndex();
}
