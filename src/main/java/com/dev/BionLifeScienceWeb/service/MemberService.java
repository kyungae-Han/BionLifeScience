package com.dev.BionLifeScienceWeb.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.model.AdminMenu;
import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.model.MemberAccount;
import com.dev.BionLifeScienceWeb.model.MemberMenu;
import com.dev.BionLifeScienceWeb.repository.MemberMenuRepository;
import com.dev.BionLifeScienceWeb.repository.MemberRepository;


@Service
@Configuration
public class MemberService implements UserDetailsService {

	@Bean(name = "saveBean")
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	  @Autowired
	    private MemberRepository memberRepository;

	    @Autowired
	    private MemberMenuRepository memberMenuRepository;

	    private static final String ROLE_ADMIN = "ROLE_ADMIN";
	    private static final String ROLE_USER = "ROLE_USER";

	    /** 로그인 시 DB 조회 */
	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        Optional<Member> member = memberRepository.findByUsername(username);
	        if (!member.isPresent()) {
	            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
	        }
	        return new MemberAccount(member.get());
	    }
	    
	    /**
	     * 계정 수정. 일반회원이면 체크한 대카테고리로 권한을 갈아 끼운다.
	     *
	     * 관리자가 한 명도 안 남는 일을 막는다. 자기 권한을 스스로 내리는 것도 막는다.
	     * 막을 때는 IllegalStateException 의 메시지를 화면에 그대로 보여 준다.
	     */
	    public Member updateMember(Member member, Collection<String> menuCodes, Long actorId) {
	        Member dbMember = memberRepository.findById(member.getId())
	            .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));

	        String newRole = normalizeRole(member.getRole());
	        boolean demote = ROLE_ADMIN.equals(dbMember.getRole()) && !ROLE_ADMIN.equals(newRole);
	        if (demote && dbMember.getId().equals(actorId)) {
	            throw new IllegalStateException("자기 계정의 관리자 권한은 내릴 수 없습니다. 다른 관리자에게 요청해 주세요.");
	        }
	        if (demote && memberRepository.countByRole(ROLE_ADMIN) <= 1) {
	            throw new IllegalStateException("마지막 관리자는 일반회원으로 바꿀 수 없습니다.");
	        }

	        // 비밀번호 입력이 있을 경우만 암호화 후 업데이트
	        if (member.getPassword() != null && !member.getPassword().isEmpty()) {
	            dbMember.setPassword(passwordEncoder().encode(member.getPassword()));
	        }

	        dbMember.setName(member.getName());
	        dbMember.setEmail(member.getEmail());
	        dbMember.setPhone(member.getPhone());
	        dbMember.setRole(newRole);
	        dbMember.setEnabled(true);

	        Member saved = memberRepository.save(dbMember);
	        saveMenus(saved, menuCodes);
	        return saved;
	    }

	    
	    /** ✅ 관리자 전용 등록 */
	    public Member insertAdmin(Member member) {
	        String encodedPassword = passwordEncoder().encode(member.getPassword());
	        member.setPassword(encodedPassword);
	        member.setEnabled(true);
	        member.setRole("ROLE_ADMIN");
	        return memberRepository.save(member);
	    }

	    /**
	     * 관리자 화면에서 계정을 만든다. 고른 권한을 따른다.
	     * 예전에는 무엇을 골라도 관리자로 저장됐다. 일반회원이면 체크한 대카테고리를 같이 저장한다.
	     */
	    public Member insertMember(Member member, Collection<String> menuCodes) {
	        if (member.getUsername() == null || member.getUsername().isBlank()) {
	            throw new IllegalStateException("아이디를 입력해 주세요.");
	        }
	        if (memberRepository.findByUsername(member.getUsername().trim()).isPresent()) {
	            throw new IllegalStateException("이미 있는 아이디입니다: " + member.getUsername());
	        }
	        if (member.getPassword() == null || member.getPassword().isEmpty()) {
	            throw new IllegalStateException("비밀번호를 입력해 주세요.");
	        }

	        member.setUsername(member.getUsername().trim());
	        member.setPassword(passwordEncoder().encode(member.getPassword()));
	        member.setEnabled(true);
	        member.setRole(normalizeRole(member.getRole()));

	        Member saved = memberRepository.save(member);
	        saveMenus(saved, menuCodes);
	        return saved;
	    }
	    
	    /** ✅ 전체 멤버 목록 조회 */
	    public List<Member> getAllMembers() {
	        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
	    }

	    /** ✅ 특정 멤버 삭제 */
	    public void deleteMember(Long id, Long actorId) {
	        Member target = memberRepository.findById(id).orElse(null);
	        if (target == null) {
	            return;
	        }
	        if (target.getId().equals(actorId)) {
	            throw new IllegalStateException("자기 계정은 지울 수 없습니다.");
	        }
	        if (ROLE_ADMIN.equals(target.getRole()) && memberRepository.countByRole(ROLE_ADMIN) <= 1) {
	            throw new IllegalStateException("마지막 관리자는 지울 수 없습니다.");
	        }
	        try {
	            memberMenuRepository.deleteByMemberId(id);
	        } catch (DataAccessException e) {
	            // member_menu 가 아직 없으면 지울 권한 줄도 없다
	        }
	        memberRepository.deleteById(id);
	    }
	    

	    /** 관리자 말고는 전부 일반회원이다. 알 수 없는 값이 저장되지 않게 한다 */
	    private String normalizeRole(String role) {
	        return ROLE_ADMIN.equals(role) ? ROLE_ADMIN : ROLE_USER;
	    }

	    /**
	     * 권한 줄을 갈아 끼운다. 관리자는 전부 쓰므로 줄을 남기지 않는다.
	     * 표에 없는 코드는 버린다. 화면을 조작해 이상한 값을 보내도 저장되지 않는다.
	     */
	    private void saveMenus(Member member, Collection<String> menuCodes) {
	        Set<String> codes = new LinkedHashSet<>();
	        if (ROLE_USER.equals(member.getRole()) && menuCodes != null) {
	            for (String code : menuCodes) {
	                if (AdminMenu.fromCode(code) != null) {
	                    codes.add(code);
	                }
	            }
	        }
	        try {
	            memberMenuRepository.deleteByMemberId(member.getId());
	            List<MemberMenu> rows = new ArrayList<>();
	            for (String code : codes) {
	                rows.add(new MemberMenu(member.getId(), code));
	            }
	            memberMenuRepository.saveAll(rows);
	        } catch (DataAccessException e) {
	            if (!codes.isEmpty()) {
	                throw new IllegalStateException(
	                    "계정은 저장했지만 메뉴 권한을 저장하지 못했습니다. memberMenu.sql 을 먼저 실행해 주세요.");
	            }
	        }
	    }

	    /** 한 계정이 받은 메뉴 코드. 수정 화면의 체크 상태에 쓴다 */
	    public Set<String> menuCodesOf(Long memberId) {
	        Set<String> codes = new LinkedHashSet<>();
	        try {
	            for (MemberMenu row : memberMenuRepository.findByMemberId(memberId)) {
	                codes.add(row.getMenuCode());
	            }
	        } catch (DataAccessException e) {
	            return Collections.emptySet();
	        }
	        return codes;
	    }

	    /** 계정 목록용. 계정마다 받은 메뉴를 차림표 순서로 */
	    public Map<Long, List<AdminMenu>> menusByMember(Collection<Member> members) {
	        Map<Long, Set<AdminMenu>> grouped = new HashMap<>();
	        List<Long> ids = new ArrayList<>();
	        for (Member m : members) {
	            ids.add(m.getId());
	            grouped.put(m.getId(), EnumSet.noneOf(AdminMenu.class));
	        }
	        try {
	            if (!ids.isEmpty()) {
	                for (MemberMenu row : memberMenuRepository.findByMemberIdIn(ids)) {
	                    AdminMenu menu = AdminMenu.fromCode(row.getMenuCode());
	                    if (menu != null && grouped.containsKey(row.getMemberId())) {
	                        grouped.get(row.getMemberId()).add(menu);
	                    }
	                }
	            }
	        } catch (DataAccessException e) {
	            // member_menu 가 아직 없으면 모두 빈 목록으로 보인다
	        }
	        Map<Long, List<AdminMenu>> result = new HashMap<>();
	        grouped.forEach((id, set) -> result.put(id, new ArrayList<>(set)));
	        return result;
	    }
}
