package com.dev.BionLifeScienceWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.Certification;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long>{

}
