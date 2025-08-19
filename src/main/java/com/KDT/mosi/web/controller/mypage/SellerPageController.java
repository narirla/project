package com.KDT.mosi.web.controller.mypage;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.mypage.seller.dao.SellerPageDAO;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.web.form.mypage.sellerpage.SellerPageCreateForm;
import com.KDT.mosi.web.form.mypage.sellerpage.SellerPageUpdateForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
  private final MemberSVC memberSVC;

  @Autowired
  private PasswordEncoder passwordEncoder;

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

    // 판매자 페이지로 이동할 때 session에 저장된 loginMember 객체의 닉네임을 판매자용 닉네임으로 업데이트
    loginMember.setNickname(sellerPage.getNickname());
    session.setAttribute("loginMember", loginMember);

    // ✅ 사이드바/템플릿 보조 속성
    model.addAttribute("activePath", "/mypage/seller/home");
    model.addAttribute("hasSellerImg", sellerPage.getImage() != null);

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
  public String viewSellerPage(HttpServletRequest request,
                               HttpSession session, Model model) {
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

    model.addAttribute("activePath", request.getRequestURI());

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

    SellerPageCreateForm form = new SellerPageCreateForm();
    form.setMemberId(memberId);  // ★ 반드시 설정

    model.addAttribute("form", form);  // ★ 추가
    return "mypage/sellerpage/createSellerPage";
  }

  /**
   * ✅ 판매자 마이페이지 생성 처리
   */
  @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String create(@ModelAttribute("form") SellerPageCreateForm form,
                       BindingResult bindingResult,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {

    // 1) 로그인 체크
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }
    Long memberId = loginMember.getMemberId();

    // 2) 중복 생성 방지 (GET 우회 방지)  ➜ [추가]
    if (sellerPageSVC.existByMemberId(memberId)) {
      redirectAttributes.addFlashAttribute("error", "이미 판매자 페이지가 존재합니다.");
      return "redirect:/mypage/seller/home";
    }

    // 3) 엔티티에 서버에서 memberId 주입 (폼 값 무시)  ➜ [핵심 변경]
    SellerPage sellerPage = new SellerPage();
    sellerPage.setMemberId(memberId);
    sellerPage.setIntro(form.getIntro());
    sellerPage.setNickname(form.getNickname());

    try {
      MultipartFile imageFile = form.getImageFile();
      if (imageFile != null && !imageFile.isEmpty()) {
        sellerPage.setImage(imageFile.getBytes());
      }
    } catch (Exception e) {
      log.error("이미지 업로드 실패", e);
      redirectAttributes.addFlashAttribute("error", "이미지 업로드에 실패했습니다.");
    }

    // 4) 디폴트 값 보정(컬럼이 NOT NULL이면 필수)  ➜ [추가 권장]
    if (sellerPage.getSalesCount() == null) sellerPage.setSalesCount(0);
    if (sellerPage.getReviewAvg() == null)  sellerPage.setReviewAvg(0.0);

    sellerPageSVC.save(sellerPage);

    redirectAttributes.addFlashAttribute("msg", "판매자 마이페이지가 생성되었습니다.");
    return "redirect:/mypage/seller/home";
  }



  /**
   * ✅ 판매자 마이페이지 수정 폼
   */
  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable("id") Long id, HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) return "redirect:/login";

    if (!id.equals(loginMember.getMemberId())) {
      throw new AccessDeniedException("본인의 마이페이지만 수정할 수 있습니다.");
    }

    Optional<SellerPage> optional = sellerPageSVC.findByMemberId(id);
    if (optional.isEmpty()) throw new AccessDeniedException("판매자 페이지가 존재하지 않습니다.");

    SellerPage sellerPage = optional.get();

    SellerPageUpdateForm form = new SellerPageUpdateForm();
    form.setPageId(sellerPage.getPageId());
    form.setMemberId(sellerPage.getMemberId());
    form.setNickname(sellerPage.getNickname());
    form.setIntro(sellerPage.getIntro());
    form.setTel(loginMember.getTel());
    form.setPasswd("");
    form.setZonecode(sellerPage.getZonecode());
    form.setAddress(sellerPage.getAddress());
    form.setDetailAddress(sellerPage.getDetailAddress());
    form.setNotification("Y");
    form.setImage(sellerPage.getImage());

    model.addAttribute("sellerPage", sellerPage);
    model.addAttribute("form", form);
    model.addAttribute("member", loginMember);
    model.addAttribute("timestamp", System.currentTimeMillis());

    return "mypage/sellerpage/editSellerPage";
  }



  /**
   * ✅ 판매자 마이페이지 수정 처리 (Form 객체 기반)
   */
  @PostMapping("/{id}/edit")
  public String edit(@PathVariable("id") Long id,
                     @ModelAttribute("form") SellerPageUpdateForm form,
                     BindingResult bindingResult,
                     HttpSession session,
                     RedirectAttributes redirectAttributes) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    if (!id.equals(loginMember.getMemberId())) {
      throw new AccessDeniedException("본인의 마이페이지만 수정할 수 있습니다.");
    }

    Optional<SellerPage> optional = sellerPageSVC.findByMemberId(id);
    if (optional.isEmpty()) {
      throw new AccessDeniedException("판매자 마이페이지 정보가 존재하지 않습니다.");
    }

    SellerPage sellerPage = optional.get();

    // 🔄 업데이트 대상 필드 복사
    sellerPage.setNickname(form.getNickname());
    sellerPage.setTel(form.getTel());
    sellerPage.setIntro(form.getIntro());
    sellerPage.setZonecode(form.getZonecode());
    sellerPage.setAddress(form.getAddress());
    sellerPage.setDetailAddress(form.getDetailAddress());

    // 📷 이미지 처리
    if (Boolean.TRUE.equals(form.getDeleteImage())) {
      sellerPage.setImage(null);
    } else {
      MultipartFile imageFile = form.getImageFile();
      if (imageFile != null && !imageFile.isEmpty()) {
        try {
          sellerPage.setImage(imageFile.getBytes());
        } catch (Exception e) {
          log.error("프로필 이미지 처리 오류", e);
          redirectAttributes.addFlashAttribute("error", "이미지 업로드에 실패했습니다.");
          return "redirect:/mypage/seller/" + id + "/edit";
        }
      } else {
        sellerPage.setImage(optional.get().getImage());
      }
    }

    sellerPageSVC.updateById(sellerPage.getPageId(), sellerPage);

    StringBuilder msg = new StringBuilder("마이페이지 정보가 수정되었습니다.");

    // 🛠️ Member 테이블의 전화번호, 비밀번호도 수정
    if (form.getTel() != null && !form.getTel().isBlank()) {
      memberSVC.updateTel(id, form.getTel());
      loginMember.setTel(form.getTel());
    }

    if (form.getPasswd() != null && !form.getPasswd().isBlank()) {
      String encodedPw = passwordEncoder.encode(form.getPasswd());
      memberSVC.updatePasswd(id, encodedPw);
      msg.append(" 비밀번호가 수정되었습니다.");
    }

    // 세션 nickname 업데이트
    loginMember.setNickname(form.getNickname());
    session.setAttribute("loginMember", loginMember);

    redirectAttributes.addFlashAttribute("msg", msg.toString());

    return "redirect:/mypage/seller/" + id + "/edit";
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

  /**
   * ✅ 닉네임 중복 확인 API
   */
  @GetMapping("/nickname-check")
  @ResponseBody
  public Map<String, Boolean> checkNickname(@RequestParam("nickname") String nickname) {
    boolean available = !sellerPageSVC.existByNickname(nickname);
    return Map.of("available", available);
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

