package com.dev.BionLifeScienceWeb.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{
	
	Optional<Client> findByPhone(String phone);
	
	Page<Client> findAllByOrderByJoindateDesc(Pageable pageable);
	
	Page<Client> findAllByNameOrderByJoindateDesc(Pageable pageable, String name);
	
	Page<Client> findAllByPhoneOrderByJoindateDesc(Pageable pageable, String phone);
	
	Page<Client> findAllByEmailOrderByJoindateDesc(Pageable pageable, String email);
	
	Page<Client> findAllByCompanyOrderByJoindateDesc(Pageable pageable, String business);
	
	Page<Client> findAllByJoindateLessThan(Pageable pageable,Date date);
	
	Page<Client> findAllByJoindateGreaterThan(Pageable pageable,Date date);
	
	Page<Client> findAllByJoindateBetween(Pageable pageable, Date startDate, Date endDate);
}
