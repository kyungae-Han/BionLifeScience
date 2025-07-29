package com.dev.BionLifeScienceWeb.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.product.ProductInfo;

@Repository
public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long>{
	
	@Transactional
	int deleteAllByProductId(Long id);
}
