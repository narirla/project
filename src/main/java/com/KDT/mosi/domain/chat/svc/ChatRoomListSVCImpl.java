//package com.KDT.mosi.domain.chat.svc;
//
//import com.KDT.mosi.domain.chat.dao.ChatRoomListDao;
//import com.KDT.mosi.domain.entity.ChatRoomList;
//import lombok.RequiredArgsConstructor;
//
//import java.util.List;
//
//
//@RequiredArgsConstructor
//public class ChatRoomListSVCImpl implements ChatRoomListSVC {
//  private final ChatRoomListDao chatRoomListDao;
//
//
//  /**
//   * 채팅목록 조회
//   * @param sellerId
//   * @return 목록 리스트
//   */
//  @Override
//  public List<ChatRoomList> findAll(Long sellerId) {
//    return chatRoomListDao.findAll(sellerId);
//  }
//}
