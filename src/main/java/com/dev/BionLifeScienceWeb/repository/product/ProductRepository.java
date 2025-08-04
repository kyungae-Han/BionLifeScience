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
import com.dev.BionLifeScienceWeb.model.product.Product;
import com.dev.BionLifeScienceWeb.model.product.SmallSort;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

	Page<Product> findAllBySmallSortOrderByIdDesc(Pageable pageable, SmallSort smallSort);
	
	Page<Product> findAllByOrderByIdDesc(Pageable pageble);
	
	Page<Product> findAllByBigSort(Pageable pageable, BigSort bigSort);
	
	Page<Product> findAllByMiddleSort(Pageable pageable, MiddleSort bigSort);
	
	Page<Product> findAllBySmallSortAndSubjectContains(Pageable pageable, SmallSort smallSort, String subject);
	
	Page<Product> findAllBySmallSortAndSubjectContainsOrderByIdDesc(Pageable pageable, SmallSort smallSort, String subject);
	
	Page<Product> findAllBySubjectContains(Pageable pageble, String subject);
	
	Page<Product> findAllBySubjectContainsOrderByIdDesc(Pageable pageble, String subject);
	
	Page<Product> findAllByBigSortAndSubjectContains(Pageable pageable, BigSort bigSort, String subject);
	
	Page<Product> findAllByMiddleSortAndSubjectContains(Pageable pageable, MiddleSort bigSort, String subject);
	
	Page<Product> findAllByBigSortAndSubjectContainsOrderByIdDesc(Pageable pageable, BigSort bigSort, String subject);
	
	Page<Product> findAllByMiddleSortAndSubjectContainsOrderByIdDesc(Pageable pageable, MiddleSort bigSort, String subject);

	List<Product> findAllBySign(Boolean sign);
	
	Page<Product> findAllBySmallSortOrderByProductIndexAsc(Pageable pageable, SmallSort smallSort);
	
	Page<Product> findAllByOrderByProductIndexAsc(Pageable pageble);
	
	Page<Product> findAllByBigSortOrderByProductIndexAsc(Pageable pageable, BigSort bigSort);
	
	Page<Product> findAllByMiddleSortOrderByProductIndexAsc(Pageable pageable, MiddleSort bigSort);
	
	Page<Product> findAllBySmallSortAndSubjectContainsOrderByProductIndexAsc(Pageable pageable, SmallSort smallSort, String subject);
	
	Page<Product> findAllBySubjectContainsOrderByProductIndexAsc(Pageable pageble, String subject);
	
	Page<Product> findAllByBigSortAndSubjectContainsOrderByProductIndexAsc(Pageable pageable, BigSort bigSort, String subject);
	
	Page<Product> findAllByMiddleSortAndSubjectContainsOrderByProductIndexAsc(Pageable pageable, MiddleSort bigSort, String subject);

	List<Product> findAllBySignOrderByProductIndexAsc(Boolean sign);
	
	List<Product> findAllByOrderByProductIndexAsc();
	
	List<Product> findBySubjectContains(String subject);
	
	Optional<Product> findByProductCode(String code);
	
	@Query("SELECT MAX(productIndex) FROM Product")
	Optional<Integer> findFirstIndex();
	
	List<Product> findBySmallSortOrderByProductIndexAsc(SmallSort smallSort);
	
}
