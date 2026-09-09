package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.dev.BionLifeScienceWeb.model.namecard.NameCard;
import com.dev.BionLifeScienceWeb.model.namecard.NameCardDocument;
import com.dev.BionLifeScienceWeb.service.namecard.NameCardQrService;
import com.dev.BionLifeScienceWeb.service.namecard.NameCardService;

import lombok.RequiredArgsConstructor;

/**
 * 디지털 명함이 쓰는 주소들.
 *
 * 명함 본체는 /{메일아이디} 로 열린다 ({@link DynamicPageController} 가 넘겨준다).
 * 여기에는 그 명함이 끌어다 쓰는 것들 — 연락처 파일, QR, 거래 서류 — 이 모여 있다.
 */
@Controller
@RequestMapping("/namecard")
@RequiredArgsConstructor
public class NameCardController {

	public static final String CARD_VIEW = "front/namecard/card";

	private final NameCardService nameCardService;
	private final NameCardQrService nameCardQrService;

	/** 명함 본체. /{메일아이디} 와 같은 화면이며 관리자 미리보기에 쓴다 */
	@GetMapping("/{slug}")
	public String card(@PathVariable String slug, Model model) {
		model.addAttribute("card", findVisible(slug));
		return CARD_VIEW;
	}

	/**
	 * 연락처 파일. 아이폰 사파리가 "새로운 연락처" 화면을 바로 띄우려면
	 * Content-Type 이 text/vcard 여야 한다. 파일로 두지 않고 매번 만들어 내려준다.
	 */
	@GetMapping("/{slug}/vcard.vcf")
	public ResponseEntity<byte[]> vcard(@PathVariable String slug,
			@RequestParam(name = "lang", required = false, defaultValue = "ko") String lang) {

		NameCard card = findVisible(slug);
		boolean english = "en".equalsIgnoreCase(lang);
		byte[] body = nameCardService.toVCard(card, english).getBytes(StandardCharsets.UTF_8);

		String fileName = nameCardService.vCardFileName(card);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/vcard; charset=utf-8"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.cacheControl(CacheControl.noCache())
				.body(body);
	}

	/** QR. 관리자에 입력된 주소로 그때그때 그린다 */
	@GetMapping("/{slug}/qr.svg")
	public ResponseEntity<byte[]> qr(@PathVariable String slug) {
		NameCard card = findVisible(slug);
		if (!card.isQrVisible()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		byte[] body = nameCardQrService.toSvg(card.getQrUrl()).getBytes(StandardCharsets.UTF_8);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("image/svg+xml; charset=utf-8"))
				// QR 주소를 고치면 바로 반영돼야 한다. 오래 물고 있지 않는다.
				.cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
				.body(body);
	}

	/**
	 * 거래 서류. 파일은 웹으로 바로 열리지 않는 곳에 있고 이 경로로만 나간다.
	 * PDF 에는 HTML 의 noindex 가 걸리지 않으므로 헤더로 막는다.
	 */
	@GetMapping("/{slug}/doc/{documentId}")
	public ResponseEntity<FileSystemResource> document(@PathVariable String slug, @PathVariable Long documentId) {
		NameCard card = findVisible(slug);
		if (!card.isDocsVisible()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		NameCardDocument document = card.getDocuments().stream()
				.filter(doc -> doc.getNameCardDocumentId().equals(documentId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		Path path = new File(document.getFilePath()).toPath();
		if (!Files.isReadable(path)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		long length;
		try {
			length = Files.size(path);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "서류를 읽을 수 없습니다.", e);
		}

		String downloadName = document.getDocType().name().toLowerCase() + ".pdf";
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.contentLength(length)
				.header("X-Robots-Tag", "noindex, nofollow")
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + downloadName + "\"")
				.cacheControl(CacheControl.noCache())
				.body(new FileSystemResource(path));
	}

	private NameCard findVisible(String slug) {
		return nameCardService.findVisible(slug)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
}
