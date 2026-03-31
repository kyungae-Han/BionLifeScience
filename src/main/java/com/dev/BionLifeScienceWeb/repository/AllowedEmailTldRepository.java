package com.dev.BionLifeScienceWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.AllowedEmailTld;

@Repository
public interface AllowedEmailTldRepository extends JpaRepository<AllowedEmailTld, Long> {
}
