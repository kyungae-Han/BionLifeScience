package com.dev.BionLifeScienceWeb.repository.brand;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.brand.BrandProductFile;

@Repository
public interface BrandProductFileRepository extends JpaRepository<BrandProductFile, Long>{
	
	@Transactional
	int deleteAllByProductId(Long id);
}
