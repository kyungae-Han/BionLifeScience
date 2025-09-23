package com.dev.BionLifeScienceWeb.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dev.BionLifeScienceWeb.model.Notice;
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
        
        Notice cur = noticeRepository.findOneWithSubject(id).orElseThrow();
        model.addAttribute("notice", cur);

        Long subjectId = (cur.getNoticeSubject() != null) ? cur.getNoticeSubject().getId() : null;

        Optional<Notice> prev = Optional.empty();
        Optional<Notice> next = Optional.empty();

        if (subjectId != null) {
            prev = noticeRepository
                  .findTopByNoticeSubject_IdAndIdLessThanOrderByIdDesc(subjectId, id);
            next = noticeRepository
                  .findTopByNoticeSubject_IdAndIdGreaterThanOrderByIdAsc(subjectId, id);
        }

        model.addAttribute("prevNotice", prev.orElse(null));
        model.addAttribute("nextNotice", next.orElse(null));

        return "front/notice/noticeDetail";
    }
}
