package com.KDT.mosi.domain.chat.svc;


import com.KDT.mosi.domain.chat.dao.ChatMessageDao;
import com.KDT.mosi.domain.chat.dao.ChatRoomDao;
import com.KDT.mosi.domain.chat.dao.ChatRoomListDao;
import com.KDT.mosi.domain.dto.ChatMessageDto;
import com.KDT.mosi.domain.dto.ChatRoomDto;
import com.KDT.mosi.domain.dto.ChatRoomListDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomDao dao;
  private final ChatRoomListDao listDao;
  private final ChatMessageDao messageDao;
  private final SimpMessagingTemplate messagingTemplate; // 🚀 WebSocket push용


  /**
   * 채팅방 보장 (없으면 생성)
   * - 구매자가 "문의하기" 버튼 눌렀을 때 실행됨
   * - 생성/보장 후 판매자에게 WebSocket으로 알림
   */
  @Transactional
  public long ensure(long productId, long buyerId, long sellerId) {
    long roomId = dao.ensureActiveRoom(productId, buyerId, sellerId);

    log.info("✅ ensure() called: roomId={}, buyerId={}, sellerId={}, productId={}",
        roomId, buyerId, sellerId, productId);

    // 🚀 채팅방이 새로 생성된 경우 → 판매자에게 알림 push
    ChatRoomDto roomDto = new ChatRoomDto(roomId, buyerId, sellerId, productId);
    messagingTemplate.convertAndSend(
        "/topic/chat/rooms/" + sellerId,  // 판매자 전용 채널
        roomDto                           // 클라이언트로 보낼 DTO
    );

    return roomId;
  }

  /**
   * 메시지 조회
   * @param roomId
   * @param limit
   * @return
   */
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public List<ChatMessageDto> findRecent(Long roomId, int limit) {
    // beforeId 파라미터는 지금 Dao에 없으니, 일단 offset 방식으로 간단히 처리
    int offset = 0; // 가장 최근부터
    return messageDao.findPageByRoom(roomId, offset, limit);
  }

//  public List<ChatMessageDto> findRecent(Long roomId, int limit, Long beforeId) {
//    int offset = 0; // 가장 최근부터
//    List<ChatMessageDto> list = messageDao.findPageByRoom(roomId, offset, limit);
//    if (list == null) {
//      return Collections.emptyList();
//    }
//    return list;
//  }

  /**
   * 판매자 기준 채팅방 목록 조회
   * @param sellerId
   * @return
   */
  @Transactional
  public List<ChatRoomListDto> findBySellerId(long sellerId) {
    return listDao.findBySellerId(sellerId);
  }




}