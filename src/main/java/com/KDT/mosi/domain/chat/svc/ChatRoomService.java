package com.KDT.mosi.domain.chat.svc;

import com.KDT.mosi.domain.chat.dao.ChatMessageDao;
import com.KDT.mosi.domain.chat.dao.ChatRoomDao;
import com.KDT.mosi.domain.chat.dao.ChatRoomListDao;
import com.KDT.mosi.domain.dto.chat.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomDao chatRoomDao;
  private final ChatRoomListDao listDao;
  private final ChatMessageDao messageDao;
  private final SimpMessagingTemplate messagingTemplate; // 🚀 WebSocket push용


  // ============================ 판매자 ============================
  /**
   * 채팅방 보장 (없으면 생성 / CLOSED면 재활성화)
   * - 구매자가 "문의하기" 버튼 눌렀을 때 실행됨
   * - 생성/보장 후 판매자에게 WebSocket으로 알림
   */
  @Transactional
  public long ensure(long productId, long buyerId, long sellerId) {
    Long roomId = chatRoomDao.findActiveRoomId(productId, buyerId, sellerId);

    if (roomId != null) {
      log.info("✅ ensure(): 기존 ACTIVE 방 사용 roomId={}", roomId);
    } else {
      // ACTIVE 없음 → CLOSED 방 찾기
      Long closed = chatRoomDao.findClosedRoomId(productId, buyerId, sellerId);
      if (closed != null) {
        chatRoomDao.updateStatus(closed, "ACTIVE");
        roomId = closed;
        log.info("✅ ensure(): CLOSED 방 재활성화 roomId={}", roomId);
      } else {
        // CLOSED도 없음 → 새로 생성
        roomId = chatRoomDao.createRoom(productId, buyerId, sellerId);
        log.info("✅ ensure(): 신규 방 생성 roomId={}", roomId);

        // 🚀 새로 만든 경우에만 판매자에게 알림 push
        ChatRoomDto roomDto = new ChatRoomDto(roomId, buyerId, sellerId, productId);

        // 판매자 채널 알림
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + sellerId, roomDto);

        // 구매자 채널 알림
        messagingTemplate.convertAndSend("/topic/chat/rooms/buyer/" + buyerId, roomDto);

      }
    }

    return roomId;
  }

  /**
   * 특정 방의 최근 메시지 목록 조회
   */
  @Transactional(readOnly = true)
  public List<ChatMessageResponse> findRecent(Long roomId) {
    return messageDao.findAllByRoomWithMember(roomId);
  }

  /**
   * 판매자 기준 모든 채팅방 목록 조회 (ACTIVE + CLOSED 전체)
   */
  @Transactional(readOnly = true)
  public List<ChatRoomListDto> findBySellerId(long sellerId) {
    return listDao.findBySellerId(sellerId);
  }

  /**
   * 판매자 기준 진행중(Active) 채팅방 목록 조회
   */
  @Transactional(readOnly = true)
  public List<ChatRoomListDto> getActiveRooms(long sellerId) {
    return listDao.findActiveBySellerId(sellerId);
  }

  /**
   * 판매자 기준 종료된(Closed) 채팅방 목록 조회
   */
  @Transactional(readOnly = true)
  public List<ChatRoomListDto> getClosedRooms(long sellerId) {
    return listDao.findClosedBySellerId(sellerId);
  }

  /**
   * 채팅 팝업에 필요한 정보 조회
   */
  @Transactional(readOnly = true)
  public ChatPopupDto getPopupInfo(long roomId) {
    return chatRoomDao.findPopupInfo(roomId);
  }

  /**
   * ✅ 특정 방이 CLOSED 상태인지 확인
   */
  @Transactional(readOnly = true)
  public boolean isClosed(Long roomId) {
    return chatRoomDao.isClosed(roomId);
  }

  /**
   * ✅ CLOSED 방을 ACTIVE로 재오픈
   */
  @Transactional
  public void reopenRoom(Long roomId) {
    int updated = chatRoomDao.updateStatus(roomId, "ACTIVE");
    log.info("♻️ roomId={} CLOSED→ACTIVE 재활성화 ({}건 갱신)", roomId, updated);
  }


  /**
   * 방 상태 변경 (ACTIVE → CLOSED)
   */
  @Transactional
  public void closeRoom(long roomId) {
    chatRoomDao.updateStatus(roomId, "CLOSED");
  }

  /**
   * 주어진 senderId가 해당 roomId의 판매자인지 여부 확인
   */
  public boolean isSeller(Long roomId, Long senderId) {
    Long sellerId = chatRoomDao.findSellerIdByRoomId(roomId);
    return sellerId != null && sellerId.equals(senderId);
  }

  // ============================ 구매자 ============================
  /**
   * 구매자 기준 모든 채팅방 목록 조회 (ACTIVE + CLOSED 전체)
   */
  @Transactional(readOnly = true)
  public List<ChatRoomListDto> findByBuyerId(long buyerId) {
    return listDao.findByBuyerId(buyerId);
  }

  /**
   * 구매자 기준 진행중(Active) 채팅방 목록 조회
   */
  @Transactional(readOnly = true)
  public List<ChatRoomListDto> getActiveRoomsByBuyer(long buyerId) {
    return listDao.findActiveByBuyerId(buyerId);
  }

  /**
   * 구매자 기준 종료된(Closed) 채팅방 목록 조회
   */
  @Transactional(readOnly = true)
  public List<ChatRoomListDto> getClosedRoomsByBuyer(long buyerId) {
    return listDao.findClosedByBuyerId(buyerId);
  }

  @Transactional(readOnly = true)
  public Long getSellerId(Long roomId) {
    return chatRoomDao.findSellerIdByRoomId(roomId);
  }

  @Transactional(readOnly = true)
  public Long getBuyerId(Long roomId) {
    return chatRoomDao.findBuyerIdByRoomId(roomId);
  }



}
