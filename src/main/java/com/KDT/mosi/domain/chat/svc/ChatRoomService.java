package com.KDT.mosi.domain.chat.svc;


import com.KDT.mosi.domain.chat.dao.ChatRoomDao;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
  private final ChatRoomDao dao;
  @Transactional
  public long ensure(long productId, long buyerId, long sellerId) {
    return dao.ensureActiveRoom(productId, buyerId, sellerId);
  }
}