package com.KDT.mosi.web.controller.chat;

import com.KDT.mosi.domain.chat.svc.ChatRoomService;
import com.KDT.mosi.domain.dto.chat.ChatRoomListDto;
import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.mypage.buyer.dao.BuyerPageDAO;
import com.KDT.mosi.domain.mypage.seller.dao.SellerPageDAO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatRoomListController {

  private final ChatRoomService chatRoomService;
  private final MemberDAO memberDAO;
  private final SellerPageDAO sellerPageDAO;
  private final BuyerPageDAO buyerPageDAO;


  //=============================== 판매자 ===============================

  /**
   * 판매자 채팅방 목록 페이지 (HTML 렌더링)
   * - 세션에서 로그인된 판매자(memberId) 확인
   * - 전체 채팅방 목록을 조회하여 model에 담아 뷰로 반환
   */
  @GetMapping("/seller")
  public String roomListPage(HttpSession session, Model model) {
    log.info("session.memberId값={}", session.getAttribute("loginMemberId"));

    // 세션에서 로그인 회원 ID 꺼내기
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      return "redirect:/login"; // 로그인 안 됐으면 로그인 페이지로
    }

    // 전체 채팅방 목록 (ACTIVE + CLOSED)
    List<ChatRoomListDto> rooms = chatRoomService.findBySellerId(memberId);

    // 뷰로 전달할 데이터
    model.addAttribute("rooms", rooms);
    model.addAttribute("memberId", memberId); // → HTML에서 data-member-id 로 내려줌

    SellerPage sp = sellerPageDAO.findByMemberId(memberId).orElse(null);
    model.addAttribute("sellerPage", sp);

    return "chat/chatList_seller"; // 📄 templates/chat/chatList_seller.html
  }

  /**
   * Ajax/REST 요청: 판매자 전체 채팅방 목록 (ACTIVE + CLOSED)
   */
  @GetMapping("/seller/api")
  @ResponseBody
  public List<ChatRoomListDto> roomListApi(HttpSession session) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      throw new IllegalStateException("로그인 필요");
    }

    List<ChatRoomListDto> rooms = chatRoomService.findBySellerId(memberId);

    // 로그는 필요한 정보만 추려서 찍기
    rooms.forEach(room ->
        log.info("📋 [판매자:{}] 채팅방ID={}, 상품ID={}, 구매자={}, 마지막메시지={}, 이미지크기={}",
            memberId,
            room.getRoomId(),
            room.getProductId(),
            room.getBuyerId(),
            room.getLastMessage(),
            room.getProductImage() != null ? room.getProductImage().length : 0)
    );

    return rooms;
  }

  /**
   * Ajax/REST 요청: 진행중(Active) 채팅방 목록
   */
  @GetMapping("/seller/api/active")
  @ResponseBody
  public List<ChatRoomListDto> activeRooms(HttpSession session) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      throw new IllegalStateException("로그인 필요");
    }

    List<ChatRoomListDto> rooms = chatRoomService.getActiveRooms(memberId);
    log.info("📡 [판매자:{}] 진행중 채팅방 {}건 조회", memberId, rooms.size());
    return rooms;
  }

  /**
   * Ajax/REST 요청: 종료된(Closed) 채팅방 목록
   */
  @GetMapping("/seller/api/closed")
  @ResponseBody
  public List<ChatRoomListDto> closedRooms(HttpSession session) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      throw new IllegalStateException("로그인 필요");
    }

    List<ChatRoomListDto> rooms = chatRoomService.getClosedRooms(memberId);
    log.info("📡 [판매자:{}] 종료된 채팅방 {}건 조회", memberId, rooms.size());
    return rooms;
  }


  //=============================== 구매자 ===============================
  /**
   * 구매자 채팅방 목록 페이지 (HTML 렌더링)
   */
  @GetMapping("/buyer")
  public String buyerRoomListPage(HttpSession session, Model model) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      return "redirect:/login"; // 로그인 안 됐으면 로그인 페이지로
    }

    // 전체 채팅방 목록 (ACTIVE + CLOSED)
    List<ChatRoomListDto> rooms = chatRoomService.findByBuyerId(memberId);

    // 뷰로 전달할 데이터
    model.addAttribute("rooms", rooms);
    model.addAttribute("memberId", memberId);

    // 👇 추가: 사이드바용 buyerPage
    BuyerPage bp = buyerPageDAO.findByMemberId(memberId).orElse(null);
    model.addAttribute("buyerPage", bp);

    Member loginMember = memberDAO.findById(memberId).orElse(null);
    model.addAttribute("member", loginMember);

    return "chat/chatList_buyer"; // 📄 templates/chat/chatList_buyer.html
  }

  /**
   * Ajax/REST 요청: 구매자 전체 채팅방 목록 (ACTIVE + CLOSED)
   */
  @GetMapping("/buyer/api")
  @ResponseBody
  public List<ChatRoomListDto> buyerRoomListApi(HttpSession session) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      throw new IllegalStateException("로그인 필요");
    }

    List<ChatRoomListDto> rooms = chatRoomService.findByBuyerId(memberId);

    rooms.forEach(room ->
        log.info("📋 [구매자:{}] 채팅방ID={}, 상품ID={}, 판매자={}, 마지막메시지={}",
            memberId,
            room.getRoomId(),
            room.getProductId(),
            room.getSellerId(),
            room.getLastMessage())
    );

    return rooms;
  }

  /**
   * Ajax/REST 요청: 진행중(Active) 채팅방 목록
   */
  @GetMapping("/buyer/api/active")
  @ResponseBody
  public List<ChatRoomListDto> activeBuyerRooms(HttpSession session) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      throw new IllegalStateException("로그인 필요");
    }

    List<ChatRoomListDto> rooms = chatRoomService.getActiveRoomsByBuyer(memberId);
    log.info("📡 [구매자:{}] 진행중 채팅방 {}건 조회", memberId, rooms.size());
    return rooms;
  }

  /**
   * Ajax/REST 요청: 종료된(Closed) 채팅방 목록
   */
  @GetMapping("/buyer/api/closed")
  @ResponseBody
  public List<ChatRoomListDto> closedBuyerRooms(HttpSession session) {
    Long memberId = (Long) session.getAttribute("loginMemberId");
    if (memberId == null) {
      throw new IllegalStateException("로그인 필요");
    }

    List<ChatRoomListDto> rooms = chatRoomService.getClosedRoomsByBuyer(memberId);
    log.info("📡 [구매자:{}] 종료된 채팅방 {}건 조회", memberId, rooms.size());
    return rooms;
  }

}
