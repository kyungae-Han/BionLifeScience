package com.dev.BionLifeScienceWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.CompanyEmail;

@Repository
public interface CompanyEmailRepository extends JpaRepository<CompanyEmail, Long>{

}
