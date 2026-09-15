package com.dev.BionLifeScienceWeb.filter;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.dev.BionLifeScienceWeb.model.AdminMenu;
import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.service.member.AdminMenuAccess;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 주소마다 대카테고리 권한을 확인한다.
 *
 * 스프링 시큐리티는 로그인했는지와 관리자·일반회원인지만 본다.
 * 어느 카테고리를 쓸 수 있는지는 여기서 본다. 주소가 카테고리별 앞 경로로
 * 나뉘어 있지 않아서 시큐리티 설정의 경로 규칙으로는 가를 수 없기 때문이다.
 *
 * 원칙은 막는 쪽이다. AdminMenu 표에 없는 주소는 관리자만 들어간다.
 */
@Component
@RequiredArgsConstructor
public class AdminMenuInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(AdminMenuInterceptor.class);

	/** 어느 계정이든 로그인만 했으면 쓰는 주소. 자기 비밀번호 바꾸기와 안내 화면이다 */
	private static final Set<String> COMMON = Set.of(
			"/admin/passwordForm",
			"/admin/passwordUpdate",
			"/admin/noPermission",
			"/admin/memberLoginForm");

	/** 관리자 첫 화면. 문의사항 관리와 같은 화면이다 */
	private static final Set<String> HOME = Set.of("/admin", "/admin/", "/admin/index");

	private final AdminMenuAccess access;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String path = request.getRequestURI().substring(request.getContextPath().length());

		// 로그인 안 한 요청은 시큐리티가 이미 로그인 화면으로 돌렸다. 여기까지 오면 로그인한 것이다
		if (access.currentMemberId() == null) {
			return true;
		}

		Member member = access.currentMember();
		if (member == null || !Boolean.TRUE.equals(member.getEnabled())) {
			// 로그인한 뒤에 계정이 지워졌거나 꺼졌다. 세션을 끊고 로그인 화면으로 보낸다
			SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			response.sendRedirect(request.getContextPath() + "/memberLoginForm");
			return false;
		}

		if (AdminMenuAccess.ROLE_ADMIN.equals(member.getRole())) {
			return true;
		}
		if (!AdminMenuAccess.ROLE_USER.equals(member.getRole())) {
			log.warn("알 수 없는 권한 값이라 막았다. 계정={}, 권한=[{}]", member.getUsername(), member.getRole());
			return deny(request, response);
		}

		if (COMMON.contains(path)) {
			return true;
		}

		// 로그인하면 누구나 /admin/index 로 온다. 문의사항을 못 보는 사람은 자기 첫 화면으로 보낸다
		if (HOME.contains(path)) {
			if (access.grantedMenus().contains(AdminMenu.CLIENT)) {
				return true;
			}
			response.sendRedirect(request.getContextPath() + access.landing());
			return false;
		}

		AdminMenu menu = AdminMenu.fromPath(path);
		if (menu == null) {
			log.info("카테고리 표에 없는 관리자 주소라 일반회원을 막았다: {} (계정={})", path, member.getUsername());
			return deny(request, response);
		}
		if (access.grantedMenus().contains(menu)) {
			return true;
		}
		return deny(request, response);
	}

	/** 화면 요청은 403 화면으로, 자바스크립트 요청은 403 상태만 돌려준다 */
	private boolean deny(HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean ajax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
				|| String.valueOf(request.getHeader("Accept")).contains("application/json");
		if (ajax) {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType("text/plain;charset=UTF-8");
			response.getWriter().write("권한이 없는 메뉴입니다.");
		} else {
			response.sendError(HttpStatus.FORBIDDEN.value());
		}
		return false;
	}
}
