package com.dev.BionLifeScienceWeb.repository.namecard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.namecard.NameCardDocument;

@Repository
public interface NameCardDocumentRepository extends JpaRepository<NameCardDocument, Long> {
}
