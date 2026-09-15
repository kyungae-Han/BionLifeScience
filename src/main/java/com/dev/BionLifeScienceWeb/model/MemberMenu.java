package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일반회원에게 준 대카테고리 한 줄. 코드는 AdminMenu 의 code 다.
 *
 * Member 엔티티에 컬렉션으로 붙이지 않고 따로 둔다. 붙이면 로그인할 때마다
 * 이 테이블을 읽어서, SQL 을 돌리기 전에 WAR 가 뜨면 관리자까지 로그인이 막힌다.
 * 따로 두면 관리자는 이 테이블을 한 번도 읽지 않는다.
 */
@Entity
@Table(name = "member_menu")
@Getter
@Setter
@NoArgsConstructor
public class MemberMenu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MEMBER_MENU_ID")
	private Long id;

	@Column(name = "MEMBER_ID")
	private Long memberId;

	@Column(name = "MENU_CODE")
	private String menuCode;

	public MemberMenu(Long memberId, String menuCode) {
		this.memberId = memberId;
		this.menuCode = menuCode;
	}
}
