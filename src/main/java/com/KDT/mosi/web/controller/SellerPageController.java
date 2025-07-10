package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.mypage.seller.dao.SellerPageDAO;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/seller")
public class SellerPageController {

  private final SellerPageSVC sellerPageSVC;
  private final SellerPageDAO sellerPageDAO;

  // ✅ 기본 진입 시 /home으로 리디렉션
  @GetMapping
  public String defaultRedirect() {
    return "redirect:/mypage/seller/home";
  }

  /**
   * ✅ 판매자 마이페이지 홈
   */
  @GetMapping("/home")
  public String sellerMypageHome(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    Optional<SellerPage> optional = sellerPageSVC.findByMemberId(loginMember.getMemberId());
    if (optional.isEmpty()) {
      return "redirect:/mypage/seller/create";
    }

    SellerPage sellerPage = optional.get();

    // 🔐 Null-safe 기본값 설정
    if (sellerPage.getTotalSales() == null) sellerPage.setTotalSales(0);
    if (sellerPage.getFollowerCount() == null) sellerPage.setFollowerCount(0);
    if (sellerPage.getReviewCount() == null) sellerPage.setReviewCount(0);
    if (sellerPage.getRecentQnaCnt() == null) sellerPage.setRecentQnaCnt(0);

    // 🔍 로그 추가
    log.info("🟢 member: {}", loginMember.getName());
    log.info("🟢 sellerPage: {}", sellerPage);
    log.info("🟢 loginMember.getNickname: {}", loginMember.getNickname());
    log.info("🟢 totalSales: {}", sellerPage.getTotalSales());
    log.info("🟢 followerCount: {}", sellerPage.getFollowerCount());
    log.info("🟢 reviewCount: {}", sellerPage.getReviewCount());
    log.info("🟢 recentQnaCnt: {}", sellerPage.getRecentQnaCnt());
    log.info("🟢 optional.get(): {}", optional.get());


    model.addAttribute("member", loginMember);
    model.addAttribute("sellerPage", optional.get());
    model.addAttribute("orders", mockOrders());     // 개발용 모의 데이터
    model.addAttribute("products", mockProducts()); // 개발용 모의 데이터

    return "mypage/sellerpage/sellerMypageHome";
  }

  /**
   * ✅ 판매자 상세 페이지 보기
   */
  @GetMapping("/view")
  public String viewSellerPage(HttpSession session, Model model) {
    log.info("판매자 상세 페이지 진입 확인");

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    Optional<SellerPage> optional = sellerPageSVC.findByMemberId(loginMember.getMemberId());
    if (optional.isEmpty()) {
      return "redirect:/mypage/seller/create";
    }

    model.addAttribute("member", loginMember);
    model.addAttribute("sellerPage", optional.get());

    return "mypage/sellerpage/viewSellerPage";
  }

  /**
   * ✅ 판매자 마이페이지 생성 폼
   */
  @GetMapping("/create")
  public String createForm(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    Long memberId = loginMember.getMemberId();
    if (sellerPageSVC.existByMemberId(memberId)) {
      throw new AccessDeniedException("이미 판매자 페이지가 존재합니다.");
    }

    model.addAttribute("memberId", memberId);
    return "mypage/sellerpage/createSellerPage";
  }

  /**
   * ✅ 판매자 마이페이지 생성 처리
   */
  @PostMapping("/create")
  public String create(@RequestParam("memberId") Long memberId,
                       @RequestParam("intro") String intro,
                       @RequestParam("image") MultipartFile image,
                       RedirectAttributes redirectAttributes) {

    SellerPage sellerPage = new SellerPage();
    sellerPage.setMemberId(memberId);
    sellerPage.setIntro(intro);

    try {
      if (image != null && !image.isEmpty()) {
        sellerPage.setImage(image.getBytes());
      }
    } catch (Exception e) {
      log.error("이미지 업로드 실패", e);
      redirectAttributes.addFlashAttribute("error", "이미지 업로드에 실패했습니다.");
    }

    sellerPageSVC.save(sellerPage);
    redirectAttributes.addFlashAttribute("msg", "판매자 마이페이지가 생성되었습니다.");
    return "redirect:/mypage/seller";
  }

  /**
   * ✅ 판매자 마이페이지 수정 폼
   */
  @GetMapping("/edit")
  public String editForm(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    Long memberId = loginMember.getMemberId();
    Optional<SellerPage> optional = sellerPageSVC.findByMemberId(memberId);
    if (optional.isEmpty()) {
      throw new AccessDeniedException("판매자 페이지가 존재하지 않습니다.");
    }

    model.addAttribute("sellerPage", optional.get());
    model.addAttribute("member", loginMember);
    return "mypage/sellerpage/editSellerPage";
  }

  /**
   * ✅ 판매자 마이페이지 수정 처리
   */
  @PostMapping("/edit")
  public String edit(@ModelAttribute SellerPage sellerPage,
                     @RequestParam("image") MultipartFile image,
                     HttpSession session,
                     RedirectAttributes redirectAttributes) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    Long memberId = loginMember.getMemberId();
    Optional<SellerPage> optional = sellerPageSVC.findByMemberId(memberId);

    if (optional.isEmpty()) {
      throw new AccessDeniedException("판매자 마이페이지 정보가 존재하지 않습니다.");
    }

    try {
      if (image != null && !image.isEmpty()) {
        sellerPage.setImage(image.getBytes());
      }
    } catch (Exception e) {
      log.error("프로필 이미지 처리 오류", e);
      redirectAttributes.addFlashAttribute("error", "이미지 업로드에 실패했습니다.");
    }

    Long pageId = optional.get().getPageId();
    sellerPageSVC.updateById(pageId, sellerPage);

    redirectAttributes.addFlashAttribute("msg", "마이페이지 정보가 수정되었습니다.");
    return "redirect:/mypage/seller";
  }

  /**
   * ✅ 판매자 프로필 이미지 조회
   */
  @GetMapping("/images/profile/{id}")
  @ResponseBody
  public ResponseEntity<byte[]> getProfileImage(@PathVariable("id") Long pageId) {
    Optional<SellerPage> optional = sellerPageSVC.findById(pageId);
    if (optional.isPresent() && optional.get().getImage() != null) {
      return ResponseEntity
          .ok()
          .contentType(MediaType.IMAGE_JPEG) // 실제 서비스에선 이미지 MIME 타입을 판별하거나 고정
          .body(optional.get().getImage());
    }
    return ResponseEntity.notFound().build();
  }

  // ✅ 개발용 모의 주문 데이터
  private List<Map<String, Object>> mockOrders() {
    return List.of(
        Map.of("date", "2025.07.01", "title", "[MO:SI Pick] 황령산 투어", "orderNo", "ORD20250701-1234567", "amount", 5000)
    );
  }

  // ✅ 개발용 모의 상품 데이터
  private List<Map<String, Object>> mockProducts() {
    return List.of(
        Map.of("name", "[MO:SI Pick] 황령산 투어", "price", 5000, "discountPrice", 4500, "imageUrl", "/img/sample-product.png")
    );
  }
}

