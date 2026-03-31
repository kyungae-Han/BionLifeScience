package com.dev.BionLifeScienceWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.AllowedEmailAddress;

@Repository
public interface AllowedEmailAddressRepository extends JpaRepository<AllowedEmailAddress, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
