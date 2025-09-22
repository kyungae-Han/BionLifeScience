package com.dev.BionLifeScienceWeb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/noticeDetail")
@RequiredArgsConstructor
public class NoticeDetailController {

    private final NoticeRepository noticeRepository;

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var n = noticeRepository.findOneWithSubject(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("notice", n);

        // (선택) 이전/다음
        model.addAttribute("prev", noticeRepository.findTop1ByIdLessThanOrderByIdDesc(id).orElse(null));
        model.addAttribute("next", noticeRepository.findTop1ByIdGreaterThanOrderByIdAsc(id).orElse(null));

        return "front/notice/noticeDetail"; // 새 템플릿
    }
}
