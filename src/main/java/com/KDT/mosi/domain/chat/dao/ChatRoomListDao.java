package com.KDT.mosi.domain.chat.dao;

import com.KDT.mosi.domain.dto.ChatRoomListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomListDao {

  //  final NamedParameterJdbcTemplate template; //Entity ChatRoomList 객체 필드와 SQL 내 파라미터 연결도구
  final JdbcTemplate jdbcTemplate;
//
//  /**
//   * 게시글 목록
//   * @return 게시글 목록
//   */
//  @Override
//  public List<ChatRoomList> findAll(Long sellerId) {
//    //sql
//    StringBuffer sql = new StringBuffer();
//
//    sql.append("SELECT room_id, buyer_id, seller_id, product_id, status, created_at ");
//    sql.append("FROM chat_room ");
//    sql.append("WHERE seller_id = :sellerId ");
//    sql.append("ORDER BY created_at DESC ");
//
//
//    SqlParameterSource param = new MapSqlParameterSource().addValue("sellerId", sellerId);
//
//    return template.query(sql.toString(), param, BeanPropertyRowMapper.newInstance(ChatRoomList.class));
//  }





  public List<ChatRoomListDto> findBySellerId(Long sellerId) {
    String sql = """
            SELECT room_id, buyer_id, seller_id, product_id, status, created_at
              FROM chat_room
             WHERE seller_id = ?
             ORDER BY created_at DESC
            """;

    return jdbcTemplate.query(sql,
        (rs, rowNum) -> new ChatRoomListDto(
            rs.getLong("room_id"),
            rs.getLong("buyer_id"),
            rs.getLong("seller_id"),
            rs.getLong("product_id"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime()
        ),
        sellerId
    );
  }
}
