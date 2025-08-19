package com.KDT.mosi.domain.chat.dao;

import com.KDT.mosi.domain.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageDao {

  private final NamedParameterJdbcTemplate jdbc;

  private static final RowMapper<ChatMessageDto> ROW_MAPPER = new RowMapper<>() {
    @Override public ChatMessageDto mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new ChatMessageDto(
          rs.getLong("MSG_ID"),
          rs.getLong("ROOM_ID"),
          rs.getLong("SENDER_ID"),
          rs.getString("CONTENT"),
          rs.getTimestamp("CREATED_AT").toLocalDateTime(),
          "Y".equals(rs.getString("READ_YN"))
      );
    }
  };

  /** 메시지 저장 (시퀀스 직접 사용) */
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

  /** 특정 방의 메시지 갯수 */
  public long countByRoom(long roomId){
    String sql = "SELECT COUNT(*) FROM CHAT_MESSAGE WHERE ROOM_ID = :roomId";
    return jdbc.queryForObject(sql, new MapSqlParameterSource("roomId", roomId), Long.class);
  }

  /** 특정 방의 메시지 페이지 조회 (최신순/오래된순은 UI 정책에 맞게) */
  public List<ChatMessageDto> findPageByRoom(long roomId, int offset, int size){
    String sql = """
      SELECT MSG_ID, ROOM_ID, SENDER_ID, CONTENT, CREATED_AT, READ_YN
      FROM CHAT_MESSAGE
      WHERE ROOM_ID = :roomId
      ORDER BY CREATED_AT ASC
      OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
    """;
    var p = new MapSqlParameterSource()
        .addValue("roomId", roomId)
        .addValue("offset", offset)
        .addValue("size", size);
    return jdbc.query(sql, p, ROW_MAPPER);
  }
}
