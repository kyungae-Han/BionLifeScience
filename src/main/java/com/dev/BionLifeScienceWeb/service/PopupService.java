package com.dev.BionLifeScienceWeb.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Popup;
import com.dev.BionLifeScienceWeb.model.PopupImage;
import com.dev.BionLifeScienceWeb.repository.PopupImageRepository;
import com.dev.BionLifeScienceWeb.repository.PopupRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메인 팝업.
 *
 * 창은 하나고 그 안에서 이미지가 롤링된다. 이미지 한 줄에 한글용과 영문용이 같이 있다.
 * 켜져 있는 팝업은 한 번에 하나로 유지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopupService {

	private final PopupRepository popupRepository;
	private final PopupImageRepository popupImageRepository;

	@Value("${spring.upload.env}")
	private String env;

	@Value("${spring.upload.path}")
	private String commonPath;

	// ── 화면에 뿌릴 것 ──────────────────────────────────────────────

	/**
	 * 오늘 메인에 띄울 팝업. 없으면 비어 있다.
	 *
	 * popup 테이블이 아직 없는 DB 에서도 메인이 죽지 않도록 실패를 삼킨다.
	 * SQL 을 돌리기 전에 WAR 가 먼저 올라가는 경우가 있다.
	 */
	@Transactional(readOnly = true)
	public Optional<Popup> activePopup() {
		try {
			return popupRepository.findFirstByUseYnTrueOrderByIdDesc()
					.filter(p -> p.isVisibleOn(LocalDate.now()));
		} catch (RuntimeException e) {
			log.warn("popup 을 읽지 못했다. 팝업 없이 화면을 그린다", e);
			return Optional.empty();
		}
	}

	// ── 관리 화면 ──────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public List<Popup> list() {
		return popupRepository.findAllByOrderByIdDesc();
	}

	@Transactional(readOnly = true)
	public Optional<Popup> find(Long id) {
		return popupRepository.findById(id);
	}

	/**
	 * 등록 또는 수정.
	 *
	 * @param koFiles 한글 이미지. 새로 올린 것만 들어온다
	 * @param enFiles 영문 이미지. 줄 수는 한글과 맞출 필요 없다
	 */
	@Transactional
	public Long save(Popup form, List<MultipartFile> koFiles, List<MultipartFile> enFiles)
			throws IllegalStateException, IOException {

		Popup popup = (form.getId() == null)
				? new Popup()
				: popupRepository.findById(form.getId())
						.orElseThrow(() -> new IllegalArgumentException("없는 팝업이다: " + form.getId()));

		popup.setSubject(form.getSubject());
		popup.setLinkUrl(blankToNull(form.getLinkUrl()));
		popup.setLinkNewWindow(Boolean.TRUE.equals(form.getLinkNewWindow()));
		popup.setStartDate(form.getStartDate());
		popup.setEndDate(form.getEndDate());
		popup.setUseYn(Boolean.TRUE.equals(form.getUseYn()));
		popup.setAnchor(form.getAnchor());
		popup.setOffsetX(form.getOffsetX());
		popup.setOffsetY(form.getOffsetY());
		popup.setOffsetUnit(form.getOffsetUnit());
		popup.setWidth(form.getWidth());
		popup.setRollInterval(form.getRollInterval());
		popup.setAnimSpeed(form.getAnimSpeed());

		popupRepository.save(popup);

		addImages(popup, koFiles, enFiles);

		// 켜져 있는 팝업은 한 번에 하나다
		if (Boolean.TRUE.equals(popup.getUseYn())) {
			turnOffOthers(popup.getId());
		}

		return popup.getId();
	}

	/** 이 팝업만 켜고 나머지는 끈다 */
	@Transactional
	public void turnOn(Long id) {
		Popup popup = popupRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("없는 팝업이다: " + id));
		popup.setUseYn(Boolean.TRUE);
		popupRepository.save(popup);
		turnOffOthers(id);
	}

	@Transactional
	public void turnOff(Long id) {
		popupRepository.findById(id).ifPresent(p -> {
			p.setUseYn(Boolean.FALSE);
			popupRepository.save(p);
		});
	}

	@Transactional
	public void delete(Long id) {
		popupRepository.findById(id).ifPresent(popupRepository::delete);
	}

	/** 이미지 한 줄만 지운다. 파일은 남겨 둔다 (되돌릴 일이 있다) */
	@Transactional
	public void deleteImage(Long imageId) {
		popupImageRepository.findById(imageId).ifPresent(image -> {
			image.getPopup().getImages().remove(image);
			popupImageRepository.delete(image);
		});
	}

	private void turnOffOthers(Long keepId) {
		for (Popup other : popupRepository.findAllByUseYnTrue()) {
			if (!other.getId().equals(keepId)) {
				other.setUseYn(Boolean.FALSE);
				popupRepository.save(other);
			}
		}
	}

	// ── 파일 저장 ──────────────────────────────────────────────────

	/**
	 * 올라온 이미지를 줄로 만들어 붙인다.
	 * 같은 자리의 한글·영문을 한 줄로 묶는다. 한쪽만 올려도 된다.
	 */
	private void addImages(Popup popup, List<MultipartFile> koFiles, List<MultipartFile> enFiles)
			throws IllegalStateException, IOException {

		int koCount = countReal(koFiles);
		int enCount = countReal(enFiles);
		int rows = Math.max(koCount, enCount);
		if (rows == 0) {
			return;
		}

		String dir = ensureDir();
		// 업로드한 파일은 /upload/** 로만 나간다 (WebConfig 의 리소스 핸들러).
		// /administration/ 으로 적으면 로컬에서는 target/classes 를 뒤지고,
		// 운영에서는 WAR 안쪽을 뒤져서 둘 다 파일을 못 찾는다.
		String road = "/upload/popup/" + today();
		int nextIndex = popup.getImages().size();

		for (int i = 0; i < rows; i++) {
			MultipartFile ko = pick(koFiles, i);
			MultipartFile en = pick(enFiles, i);
			if (isEmpty(ko) && isEmpty(en)) {
				continue;
			}

			PopupImage image = new PopupImage();
			image.setPopup(popup);
			image.setSortIndex(nextIndex++);

			if (!isEmpty(ko)) {
				String saved = randomPrefix() + "_ko_" + ko.getOriginalFilename();
				String savePath = dir + "/" + saved;
				ko.transferTo(new File(savePath));
				image.setName(ko.getOriginalFilename());
				image.setPath(savePath);
				image.setRoad(road + "/" + saved);
			}

			if (!isEmpty(en)) {
				String saved = randomPrefix() + "_en_" + en.getOriginalFilename();
				String savePath = dir + "/" + saved;
				en.transferTo(new File(savePath));
				image.setNameEn(en.getOriginalFilename());
				image.setPathEn(savePath);
				image.setRoadEn(road + "/" + saved);
			}

			popup.getImages().add(image);
			popupImageRepository.save(image);
		}
	}

	/** 오늘 날짜 폴더를 만들고 실제 저장 경로를 돌려준다. 배너와 같은 규칙이다 */
	private String ensureDir() {
		String path = commonPath + "/popup/" + today();
		String absolute = "local".equals(env) ? new File("").getAbsolutePath() + "/" : "";
		File dir = new File(absolute + path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return absolute + path;
	}

	private String today() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}

	private String randomPrefix() {
		Random random = new Random();
		return random.ints(48, 123)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
				.limit(10)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	private static MultipartFile pick(List<MultipartFile> files, int index) {
		return (files == null || files.size() <= index) ? null : files.get(index);
	}

	private static boolean isEmpty(MultipartFile file) {
		return file == null || file.isEmpty();
	}

	private static int countReal(List<MultipartFile> files) {
		if (files == null) {
			return 0;
		}
		int last = 0;
		for (int i = 0; i < files.size(); i++) {
			if (!isEmpty(files.get(i))) {
				last = i + 1;
			}
		}
		return last;
	}

	private static String blankToNull(String value) {
		return (value == null || value.isBlank()) ? null : value.trim();
	}
}
