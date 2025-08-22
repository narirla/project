package com.KDT.mosi.web.controller.review;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.review.ReviewProduct;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.review.svc.ReviewSVC;
import com.KDT.mosi.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class CsrApiReviewController {

  private final ReviewSVC reviewSVC;
  private final MemberSVC memberSVC;

  private Long getLoginMemberId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Object principal = auth.getPrincipal();

    if (principal instanceof com.KDT.mosi.security.CustomUserDetails userDetails) {
      return userDetails.getMember().getMemberId();
    }

    throw new IllegalStateException("로그인 사용자 정보를 확인할 수 없습니다.");
  }

  @GetMapping("/add/{orderItemId}")
  public String bbs(@PathVariable("orderItemId") Long orderItemId,
                    @AuthenticationPrincipal CustomUserDetails user,
                    Model model) {

    Long memberId = user.getMember().getMemberId();
    Long loginMemberId = getLoginMemberId();
    Optional<Member> optionalMember = memberSVC.findById(loginMemberId);
    if (optionalMember.isEmpty()) {
      return "error/403"; // 로그인 정보 없음
    }

    Member member = optionalMember.get();
    model.addAttribute("memberId", loginMemberId);
    model.addAttribute("member", member);
    Optional<ReviewProduct> reviewProductOpt = reviewSVC.summaryFindById(orderItemId, memberId);

    ReviewProduct reviewProduct = reviewProductOpt.orElse(null);
    model.addAttribute("reviewProduct", reviewProduct);

    // byte[] -> Base64 data URI
    String imageSrc = null;
    if (reviewProduct != null) {
      byte[] data = reviewProduct.getImageData();
      if (data != null && data.length > 0) {
        String mime = (reviewProduct.getMimeType() != null && !reviewProduct.getMimeType().isBlank())
            ? reviewProduct.getMimeType()
            : "image/jpeg";
        String base64 = Base64.getEncoder().encodeToString(data);
        imageSrc = "data:" + mime + ";base64," + base64;
      }
    }
    model.addAttribute("imageSrc", imageSrc);
    return "review/review_writeForm";
//    return "review/write";
  }

  @GetMapping("/list")
  public String reviewListBuyer(
      @RequestParam(name = "pageNo", required = false) Integer pageNo,
      @RequestParam(name = "numOfRows", required = false) Integer numOfRows,
      @AuthenticationPrincipal CustomUserDetails user,
      Model model
  ) {
    if (user == null) return "redirect:/login";
    int p = (pageNo == null) ? 1 : pageNo;
    int r = (numOfRows == null) ? 5 : numOfRows;

    // 쿼리 없으면 주소창에 기본값 고정
    if (pageNo == null || numOfRows == null) {
      return "redirect:/review/list?pageNo=" + p + "&numOfRows=" + r;
    }

    model.addAttribute("mode", "buyer");     // 프런트에서 /api/review/paging/buyer 호출
    model.addAttribute("pageNo", p);
    model.addAttribute("numOfRows", r);
    return "review/review_list";             // 목록 뷰 (앞서 만든 HTML 템플릿)
  }
}
