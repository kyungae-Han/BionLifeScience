package com.dev.BionLifeScienceWeb.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev.BionLifeScienceWeb.model.AdminMenu;
import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.service.MemberService;
import com.dev.BionLifeScienceWeb.service.member.AdminMenuAccess;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberService memberService;
	private final AdminMenuAccess adminMenuAccess;
	
	/*로그인폼*/
	@GetMapping("/memberLoginForm")
	public String memberLoginForm() {
		return "admin/login";
	}
	
    // 등록 폼
    @GetMapping("/admin/memberInsert")
    public String insertForm(Model model) {
        if (!model.containsAttribute("member")) {
            Member member = new Member();
            member.setRole(AdminMenuAccess.ROLE_USER);
            model.addAttribute("member", member);
        }
        model.addAttribute("menus", AdminMenu.values());
        model.addAttribute("grantedCodes", Collections.emptySet());
        return "admin/memberInsert";
    }

    // 등록 처리. 고른 권한을 따르고, 일반회원이면 체크한 메뉴를 같이 저장한다
    @PostMapping("/admin/memberInsert")
    public String insertSubmit(@ModelAttribute Member member,
                               @RequestParam(value = "menus", required = false) List<String> menus,
                               RedirectAttributes ra) {
        try {
            memberService.insertMember(member, menus);
            ra.addFlashAttribute("msg", "계정이 등록되었습니다.");
            return "redirect:/admin/memberList";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/admin/memberInsert";
        }
    }
    
    @GetMapping("/admin/memberList")
    public String memberList(Model model) {
        List<Member> members = memberService.getAllMembers();
        model.addAttribute("members", members);
        model.addAttribute("memberMenus", memberService.menusByMember(members));
        return "admin/memberList";
    }
    
    @GetMapping("/admin/memberDelete/{id}")
    public String deleteMember(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            memberService.deleteMember(id, adminMenuAccess.currentMemberId());
            ra.addFlashAttribute("msg", "멤버가 삭제되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/admin/memberList";
    }

    /** 일반회원인데 받은 메뉴가 하나도 없을 때 로그인하면 여기로 온다 */
    @GetMapping("/admin/noPermission")
    public String noPermission() {
        return "admin/noPermission";
    }
	
}
