package com.dev.BionLifeScienceWeb.repository.brand;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.brand.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long>{

	Page<Brand> findAllByName(Pageable pageable, String name);
	
	Page<Brand> findAll(Pageable pageable);
	
	List<Brand> findAllByOrderByBrandIndexAsc();
	
	Page<Brand> findAllByOrderByBrandIndexAsc(Pageable pageable);
	
	@Query("SELECT MAX(brandIndex) FROM Brand")
	Optional<Integer> findFirstIndex();
}
