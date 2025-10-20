package com.dev.BionLifeScienceWeb.repository.brand;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.brand.BrandProductSpec;

@Repository
public interface BrandProductSpecRepository extends JpaRepository<BrandProductSpec, Long>{
	
	List<BrandProductSpec> findAllByProductId(Long productId);
	
	@Transactional
	int deleteAllByProductId(Long id);
	
	@Query("SELECT s FROM BrandProductSpec s WHERE s.product.id = :productId ORDER BY s.specOrder ASC")
	List<BrandProductSpec> findAllByProductIdOrderBySpecOrderAsc(@Param("productId") Long productId);
}
