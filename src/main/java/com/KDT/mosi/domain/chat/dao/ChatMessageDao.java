package com.KDT.mosi.domain.chat.dao;

import com.KDT.mosi.domain.dto.chat.ChatMessageDto;
import com.KDT.mosi.domain.dto.chat.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageDao {

  private final NamedParameterJdbcTemplate jdbc;

  //================ 매퍼 ====================

  /** 테이블 그대로 → ChatMessageDto 매핑 */
  private static final RowMapper<ChatMessageDto> ROW_MAPPER = (rs, rowNum) ->
      new ChatMessageDto(
          rs.getLong("MSG_ID"),
          rs.getLong("ROOM_ID"),
          rs.getLong("SENDER_ID"),
          rs.getString("CONTENT"),
          rs.getTimestamp("CREATED_AT").toLocalDateTime(),
          "Y".equals(rs.getString("READ_YN"))
      );

  /** member JOIN 결과 → ChatMessageResponse 매핑 */
  private static final RowMapper<ChatMessageResponse> RESPONSE_MAPPER = (rs, rowNum) ->
      new ChatMessageResponse(
          rs.getLong("MSG_ID"),
          rs.getLong("ROOM_ID"),
          rs.getLong("SENDER_ID"),
          rs.getString("CONTENT"),
          rs.getTimestamp("CREATED_AT").toLocalDateTime(),
          "Y".equals(rs.getString("READ_YN")),
          rs.getString("SELLER_NICKNAME"),
          rs.getString("BUYER_NICKNAME"),
          rs.getBytes("SELLER_IMAGE"),   // DB 컬럼명 PIC
          rs.getBytes("BUYER_IMAGE")   // DB 컬럼명 PIC
      );

  //================ CRUD ====================

  /** 메시지 저장 */
  public long insert(long roomId, long senderId, String content, String clientMsgId){
    Long msgId = jdbc.getJdbcTemplate()
        .queryForObject("SELECT CHAT_MESSAGE_SEQ.NEXTVAL FROM DUAL", Long.class);

    String sql = """
          INSERT INTO CHAT_MESSAGE (MSG_ID, ROOM_ID, SENDER_ID, CONTENT, CREATED_AT, READ_YN, CLIENT_MSG_ID)
          VALUES (:msgId, :roomId, :senderId, :content, SYSTIMESTAMP, 'N', :clientMsgId)
        """;
    var p = new MapSqlParameterSource()
        .addValue("msgId", msgId)
        .addValue("roomId", roomId)
        .addValue("senderId", senderId)
        .addValue("content", content)
        .addValue("clientMsgId", clientMsgId);

    jdbc.update(sql, p);
    return msgId;
  }

  /** 특정 방 전체 메시지 (닉네임+프로필 포함) */
  public List<ChatMessageResponse> findAllByRoomWithMember(Long roomId) {
    String sql = """
                  SELECT m.MSG_ID,
                 m.ROOM_ID,
                 m.SENDER_ID,
                 m.CONTENT,
                 m.CREATED_AT,
                 m.READ_YN,
                 sp.NICKNAME AS SELLER_NICKNAME,
                 bp.nickname AS BUYER_NICKNAME,
                 sp.image AS SELLER_IMAGE,
                 bp.IMAGE AS BUYER_IMAGE
          FROM CHAT_MESSAGE m
          JOIN SELLER_PAGE sp  ON m.SENDER_ID = sp.MEMBER_ID
          JOIN BUYER_PAGE bp ON m.SENDER_ID = bp.MEMBER_ID
          WHERE m.ROOM_ID = :roomId
          ORDER BY m.CREATED_AT ASC
        """;

    var p = new MapSqlParameterSource()
        .addValue("roomId", roomId);

    return jdbc.query(sql, p, RESPONSE_MAPPER);
  }

  /** 메시지 들고오기 */
  public ChatMessageResponse findByIdWithMember(Long msgId) {
    String sql = """
        SELECT m.MSG_ID,
               m.ROOM_ID,
               m.SENDER_ID,
               m.CONTENT,
               m.CREATED_AT,
               m.READ_YN,
               sp.NICKNAME AS seller_nickname,
               bp.NICKNAME AS buyer_nickname,
               sp.IMAGE AS SELLER_IMAGE,
               bp.IMAGE AS BUYER_IMAGE
        FROM CHAT_MESSAGE m
        JOIN MEMBER mem ON m.SENDER_ID = mem.MEMBER_ID
        JOIN SELLER_PAGE sp ON sp.MEMBER_ID = mem.MEMBER_ID
        JOIN BUYER_PAGE bp ON bp.MEMBER_ID = mem.MEMBER_ID
        WHERE m.MSG_ID = :msgId
    """;


    var p = new MapSqlParameterSource()
        .addValue("msgId", msgId);

    return jdbc.queryForObject(sql, p, RESPONSE_MAPPER);
  }





  /** (옵션) 페이징 조회 */
//  public List<ChatMessageResponse> findPageByRoomWithMember(long roomId, int offset, int size) {
//    String sql = """
//          SELECT m.MSG_ID,
//                 m.ROOM_ID,
//                 m.SENDER_ID,
//                 m.CONTENT,
//                 m.CREATED_AT,
//                 m.READ_YN,
//                 mem.NICKNAME,
//                 mem.PIC
//          FROM CHAT_MESSAGE m
//          JOIN MEMBER mem
//            ON m.SENDER_ID = mem.MEMBER_ID
//          WHERE m.ROOM_ID = :roomId
//          ORDER BY m.CREATED_AT ASC
//          OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
//        """;
//
//    var p = new MapSqlParameterSource()
//        .addValue("roomId", roomId)
//        .addValue("offset", offset)
//        .addValue("size", size);
//
//    return jdbc.query(sql, p, RESPONSE_MAPPER);
//  }


  /**
   //   * 읽음 처리
   //   * @param roomId
   //   * @param memberId
   //   */
  public int markAsRead(Long roomId, Long readerId, Long lastReadMessageId) {
    String sql = """
        UPDATE chat_message
           SET read_yn = 'Y'
         WHERE room_id = :roomId
           AND sender_id <> :readerId
           AND msg_id <= :lastReadMessageId
           AND read_yn = 'N'
    """;

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("roomId", roomId)
        .addValue("readerId", readerId)
        .addValue("lastReadMessageId", lastReadMessageId);

    return jdbc.update(sql, params);
  }



}

//




