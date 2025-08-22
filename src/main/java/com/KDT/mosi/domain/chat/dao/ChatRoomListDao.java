package com.KDT.mosi.domain.chat.dao;

import com.KDT.mosi.domain.dto.chat.ChatRoomListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomListDao {

  //  final NamedParameterJdbcTemplate template; //Entity ChatRoomList 객체 필드와 SQL 내 파라미터 연결도구
  final JdbcTemplate jdbcTemplate;



  public List<ChatRoomListDto> findBySellerId(Long sellerId) {
    String sql = """
            SELECT r.room_id,
                   r.buyer_id,
                   r.seller_id,
                   r.product_id,
                   r.status,
                   r.created_at,
                   b.nickname   AS buyer_nickname,
                   p.title      AS product_title,
                   pi2.image_data  AS product_image,
                   m.content    AS last_message,
                   CASE WHEN EXISTS (
                       SELECT 1
                         FROM chat_message cm2
                        WHERE cm2.room_id = r.room_id
                          AND cm2.sender_id <> ?
                          AND cm2.read_yn = 'N'
                   ) THEN 1 ELSE 0 END AS has_new
            FROM chat_room r
            JOIN member b ON r.buyer_id = b.member_id
            JOIN member s ON r.seller_id = s.member_id
            JOIN product p ON r.product_id = p.product_id
            JOIN (
                SELECT product_id, image_data
                FROM (
                    SELECT product_id,
                           image_data,
                           ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY image_id) AS rn
                    FROM product_image
                )
                WHERE rn = 1
            ) pi2 ON r.product_id = pi2.product_id
            LEFT JOIN (
                SELECT cm.room_id, cm.content
                FROM chat_message cm
                WHERE cm.created_at = (
                    SELECT MAX(created_at)
                    FROM chat_message
                    WHERE room_id = cm.room_id
                )
            ) m ON r.room_id = m.room_id
            WHERE r.seller_id = ?
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      ChatRoomListDto dto = new ChatRoomListDto();
      dto.setRoomId(rs.getLong("room_id"));
      dto.setBuyerId(rs.getLong("buyer_id"));
      dto.setSellerId(rs.getLong("seller_id"));
      dto.setProductId(rs.getLong("product_id"));
      dto.setStatus(rs.getString("status"));
      dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

      // 확장된 필드 매핑
      dto.setBuyerNickname(rs.getString("buyer_nickname"));
      dto.setProductTitle(rs.getString("product_title"));
      dto.setProductImage(rs.getBytes("product_image"));
      dto.setLastMessage(rs.getString("last_message"));

      // 새 메시지 여부
      dto.setHasNew(rs.getInt("has_new") == 1);

      return dto;
    }, sellerId, sellerId);
  }

}
