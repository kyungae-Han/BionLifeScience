package com.dev.BionLifeScienceWeb.repository.brand;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.brand.BrandProductInfo;

@Repository
public interface BrandProductInfoRepository extends JpaRepository<BrandProductInfo, Long>{
	
	@Transactional
	int deleteAllByProductId(Long id);
	
	boolean existsByProductAndProductInfoText(BrandProduct product, String productInfoText);
	
	List<BrandProductInfo> findAllByProduct(BrandProduct product);
}
