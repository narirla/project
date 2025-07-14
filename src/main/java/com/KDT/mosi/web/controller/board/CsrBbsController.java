package com.KDT.mosi.web.controller.board;

import com.KDT.mosi.domain.board.bbs.svc.BbsSVC;
import com.KDT.mosi.domain.entity.Member;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bbs")
@RequiredArgsConstructor
public class CsrBbsController {
  final private BbsSVC bbsSVC;

  @GetMapping
  public String bbs() {
    return "csr/bbs/allForm_v2";
  }

  @GetMapping("/community")
  public String community() {
    return "csr/bbs/bbs_list";
  }

  //게시글조회
  @GetMapping("/{id}")
  public String findById(
      @PathVariable("id") Long id,
      Model model
  ){
    model.addAttribute("bbsId", id);

    return "csr/bbs/detailForm";
  }

  @GetMapping("/add")
  public String bbsAdd(
      HttpSession session
      ,Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    model.addAttribute("user", loginMember);
    return "postBoards/write_quill";
  }

  // 게시글 답글
  @GetMapping("/add/{id}")
  public String updateForm(@PathVariable("id") Long id, Model model) {
    model.addAttribute("bbsId", id);
    return "postBoards/write_quill";
  }

}
