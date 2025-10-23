package com.dev.BionLifeScienceWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberService memberService;
	
	/*로그인폼*/
	@GetMapping("/memberLoginForm")
	public String memberLoginForm() {
		return "admin/login";
	}
	
	
    // 등록 폼
    @GetMapping("/admin/memberInsert")
    public String insertForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("members", memberService.getAllMembers());
        return "admin/memberInsert";
    }

    // 등록 처리
    @PostMapping("/admin/memberInsert")
    public String insertSubmit(@ModelAttribute Member member, RedirectAttributes ra) {
        memberService.insertAdmin(member);
        ra.addFlashAttribute("msg", "사용자 계정이 정상 등록되었습니다.");
        return "redirect:/admin/memberList";
    }
    
    @GetMapping("/admin/memberList")
    public String memberList(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "admin/memberList";
    }
    
    @GetMapping("/admin/memberDelete/{id}")
    public String deleteMember(@PathVariable("id") Long id, RedirectAttributes ra) {
        memberService.deleteMember(id);
        ra.addFlashAttribute("msg", "멤버가 삭제되었습니다.");
        return "redirect:/admin/memberList";
    }
	
}
