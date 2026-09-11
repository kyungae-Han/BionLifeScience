package com.dev.BionLifeScienceWeb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.BionLifeScienceWeb.model.Popup;

public interface PopupRepository extends JpaRepository<Popup, Long> {

	List<Popup> findAllByOrderByIdDesc();

	/** 켜져 있는 팝업. 한 번에 하나만 켜지므로 보통 0개 아니면 1개다 */
	Optional<Popup> findFirstByUseYnTrueOrderByIdDesc();

	List<Popup> findAllByUseYnTrue();
}
