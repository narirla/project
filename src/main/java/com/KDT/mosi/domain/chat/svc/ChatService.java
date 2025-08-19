package com.KDT.mosi.domain.chat.svc;


import com.KDT.mosi.domain.chat.dao.ChatMessageDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
