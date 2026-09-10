package com.dev.BionLifeScienceWeb.service.page;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dev.BionLifeScienceWeb.model.page.PageText;
import com.dev.BionLifeScienceWeb.repository.page.PageTextRepository;
import com.dev.BionLifeScienceWeb.util.EnText;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 화면 고정 문구를 꺼내 준다.
 *
 * 한 화면에 문구가 수십 개다. 한 개씩 조회하면 페이지 한 장에 수십 번 DB 를 친다.
 * 그래서 화면 단위로 한 번에 읽어 두고 들고 있는다. 관리자에서 저장하면 비운다.
 */
@Slf4j
@Service("pageText")
@RequiredArgsConstructor
public class PageTextService {

	private final PageTextRepository pageTextRepository;
	private final EnText enText;

	/** 화면코드 → (문구이름 → 줄) */
	private final Map<String, Map<String, PageText>> cache = new ConcurrentHashMap<>();

	/**
	 * 보고 있는 언어에 맞는 문구를 돌려준다.
	 * 없거나 비어 있으면 null 이다. 화면에서 ?: _ 로 원래 글을 남긴다.
	 */
	public String get(String pageCode, String textKey) {
		PageText row = load(pageCode).get(textKey);
		if (row == null) {
			return null;
		}
		// 영문이 비면 한글로 떨어진다. 브랜드명과 같은 규칙이다
		return enText.pick(row.getTextKo(), row.getTextEn());
	}

	// 읽기에는 트랜잭션을 걸지 않는다. page_text 테이블이 없는 DB 에서
	// 조회가 깨지면 트랜잭션이 롤백 표시를 받아 화면 전체가 죽기 때문이다.
	private Map<String, PageText> load(String pageCode) {
		Map<String, PageText> cached = cache.get(pageCode);
		if (cached != null) {
			return cached;
		}

		Map<String, PageText> map = new LinkedHashMap<>();
		try {
			for (PageText row : pageTextRepository.findByPageCodeOrderBySortIndexAsc(pageCode)) {
				map.put(row.getTextKey(), row);
			}
		} catch (RuntimeException e) {
			// 아직 page_text 테이블이 없는 DB 일 수 있다 (SQL 실행 전에 배포한 경우).
			// 화면은 템플릿에 적힌 글을 그대로 쓴다.
			// 실패한 것은 들고 있지 않는다. 들고 있으면 나중에 SQL 을 돌려도
			// 톰캣을 다시 띄우기 전까지 계속 빈 것으로 보인다
			log.warn("page_text 를 읽지 못했다. 템플릿에 적힌 글을 그대로 쓴다. pageCode={}", pageCode, e);
			return Collections.emptyMap();
		}

		Map<String, PageText> loaded = Collections.unmodifiableMap(map);
		cache.put(pageCode, loaded);
		return loaded;
	}

	public List<PageText> list(String pageCode) {
		return pageTextRepository.findByPageCodeOrderBySortIndexAsc(pageCode);
	}

	public List<PageText> listAll() {
		return pageTextRepository.findAllByOrderByPageCodeAscSortIndexAsc();
	}

	/**
	 * 관리자 화면에서 넘어온 값을 저장한다.
	 * 입력 이름은 "ko.아이디", "en.아이디" 꼴이다.
	 *
	 * @return 실제로 바뀐 문구 수
	 */
	@Transactional
	public int save(Map<String, String> params) {
		int changed = 0;
		for (PageText row : pageTextRepository.findAll()) {
			String id = String.valueOf(row.getPageTextId());
			String ko = clean(params.get("ko." + id));
			String en = clean(params.get("en." + id));

			// 화면에 없던 줄은 건드리지 않는다
			if (!params.containsKey("ko." + id) && !params.containsKey("en." + id)) {
				continue;
			}

			boolean touched = false;
			if (params.containsKey("ko." + id) && !equals(row.getTextKo(), ko)) {
				row.setTextKo(ko);
				touched = true;
			}
			if (params.containsKey("en." + id) && !equals(row.getTextEn(), en)) {
				row.setTextEn(en);
				touched = true;
			}
			if (touched) {
				pageTextRepository.save(row);
				changed++;
			}
		}
		if (changed > 0) {
			clearCacheAfterCommit();
		}
		return changed;
	}

	/** 저장한 값이 바로 화면에 나오도록 들고 있던 것을 버린다 */
	public void clearCache() {
		cache.clear();
	}

	/**
	 * 커밋이 끝난 뒤에 버린다.
	 * 커밋 전에 버리면 그 사이에 들어온 요청이 옛 값을 다시 들고 앉는다.
	 */
	private void clearCacheAfterCommit() {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			clearCache();
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				clearCache();
			}
		});
	}

	private String clean(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean equals(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}
}
