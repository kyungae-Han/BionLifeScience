package com.dev.BionLifeScienceWeb.service.member;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.dev.BionLifeScienceWeb.model.AdminMenu;
import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.model.MemberAccount;
import com.dev.BionLifeScienceWeb.model.MemberMenu;
import com.dev.BionLifeScienceWeb.repository.MemberMenuRepository;
import com.dev.BionLifeScienceWeb.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * 지금 로그인한 계정이 어느 대카테고리를 쓸 수 있는지 답한다.
 *
 * 화면에서는 이렇게 쓴다.
 * <pre>th:if="${@adminMenu.can('site')}"</pre>
 *
 * 권한과 부여 목록은 로그인할 때 담아 둔 값이 아니라 요청마다 DB 에서 읽는다.
 * 관리자가 권한을 바꾸거나 계정을 끄면 상대가 다시 로그인하지 않아도 바로 따라간다.
 * 한 요청 안에서는 한 번만 읽고 요청 속성에 담아 둔다.
 */
@Service("adminMenu")
@RequiredArgsConstructor
public class AdminMenuAccess {

	private static final Logger log = LoggerFactory.getLogger(AdminMenuAccess.class);

	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_USER = "ROLE_USER";

	private static final String ATTR_MEMBER = AdminMenuAccess.class.getName() + ".member";
	private static final String ATTR_MENUS = AdminMenuAccess.class.getName() + ".menus";

	private final MemberRepository memberRepository;
	private final MemberMenuRepository memberMenuRepository;

	/** 로그인한 계정의 아이디. 로그인 안 했으면 null */
	public Long currentMemberId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}
		Object principal = auth.getPrincipal();
		if (principal instanceof Member) {
			return ((Member) principal).getId();
		}
		if (principal instanceof MemberAccount) {
			return ((MemberAccount) principal).getMember().getId();
		}
		return null;
	}

	/** DB 에서 읽은 지금 계정. 없거나 로그인 안 했으면 null */
	public Member currentMember() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs != null) {
			Object cached = attrs.getAttribute(ATTR_MEMBER, RequestAttributes.SCOPE_REQUEST);
			if (cached instanceof Member) {
				return (Member) cached;
			}
		}
		Long id = currentMemberId();
		if (id == null) {
			return null;
		}
		Member member = memberRepository.findById(id).orElse(null);
		if (attrs != null && member != null) {
			attrs.setAttribute(ATTR_MEMBER, member, RequestAttributes.SCOPE_REQUEST);
		}
		return member;
	}

	public boolean isAdmin() {
		Member member = currentMember();
		return member != null && ROLE_ADMIN.equals(member.getRole());
	}

	/** 지금 계정이 받은 대카테고리. 관리자는 전부다 */
	public Set<AdminMenu> grantedMenus() {
		Member member = currentMember();
		if (member == null) {
			return Collections.emptySet();
		}
		if (ROLE_ADMIN.equals(member.getRole())) {
			return EnumSet.allOf(AdminMenu.class);
		}

		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs != null) {
			@SuppressWarnings("unchecked")
			Set<AdminMenu> cached = (Set<AdminMenu>) attrs.getAttribute(ATTR_MENUS, RequestAttributes.SCOPE_REQUEST);
			if (cached != null) {
				return cached;
			}
		}

		Set<AdminMenu> menus = menusOf(member.getId());
		if (attrs != null) {
			attrs.setAttribute(ATTR_MENUS, menus, RequestAttributes.SCOPE_REQUEST);
		}
		return menus;
	}

	/**
	 * 한 계정이 받은 대카테고리. 표에서 사라진 옛 코드는 버린다.
	 * member_menu 테이블이 아직 없으면 빈 목록이다. 일반회원은 아무 데도 못 들어가고
	 * 관리자는 영향을 받지 않는다.
	 */
	public Set<AdminMenu> menusOf(Long memberId) {
		Set<AdminMenu> menus = EnumSet.noneOf(AdminMenu.class);
		try {
			for (MemberMenu row : memberMenuRepository.findByMemberId(memberId)) {
				AdminMenu menu = AdminMenu.fromCode(row.getMenuCode());
				if (menu != null) {
					menus.add(menu);
				}
			}
		} catch (DataAccessException e) {
			log.warn("member_menu 를 읽지 못했다. certification 처럼 SQL 을 먼저 돌렸는지 확인한다: {}", e.getMessage());
		}
		return menus;
	}

	/** 화면용. 코드 문자열로 묻는다 */
	public boolean can(String code) {
		AdminMenu menu = AdminMenu.fromCode(code);
		return menu != null && grantedMenus().contains(menu);
	}

	/**
	 * 로그인 직후나 권한 없는 곳에 왔을 때 보낼 첫 화면.
	 * 받은 카테고리 중 차림표에서 가장 위에 있는 것이고, 하나도 없으면 안내 화면이다.
	 */
	public String landing() {
		for (AdminMenu menu : AdminMenu.values()) {
			if (grantedMenus().contains(menu)) {
				return menu.getLanding();
			}
		}
		return "/admin/noPermission";
	}
}
