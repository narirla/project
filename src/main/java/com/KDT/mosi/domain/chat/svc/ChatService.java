package com.KDT.mosi.domain.chat.svc;


import com.KDT.mosi.domain.chat.dao.ChatMessageDao;
import com.KDT.mosi.domain.dto.chat.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatMessageDao messageDao;
  // (선택) private final ChatRoomDao roomDao;  // last_msg_id 업데이트 등에 사용

  /** 메시지 저장 → (선택) 채팅방 최근 메시지 업데이트 */
  @Transactional
  public long saveMessage(long roomId, long senderId, String content, String clientMsgId){
    long msgId = messageDao.insert(roomId, senderId, content, clientMsgId);
    // roomDao.updateLastMsgId(roomId, msgId); // 필요 시 활성화
    return msgId;
  }


  /** 특정 채팅방의 전체 메시지 (닉네임, 프로필 포함) */
  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getMessagesWithMember(Long roomId) {
    return messageDao.findAllByRoomWithMember(roomId);
  }

  /** 특정 채팅방의 메시지 1개) */
  @Transactional(readOnly = true)
  public ChatMessageResponse findMessageWithMember(Long msgId){
    return messageDao.findByIdWithMember(msgId);
  }


}
