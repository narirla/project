package com.KDT.mosi.domain.chat.dao;

import com.KDT.mosi.domain.dto.chat.ChatRoomListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomListDao {

  private final JdbcTemplate jdbcTemplate;

  // ---- 판매자 공통 SQL ----
  private static final String BASE_SQL_SELLER = """
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
      JOIN buyer_page b ON r.buyer_id = b.member_id
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

  // ---- 구매자 공통 SQL ----
  private static final String BASE_SQL_BUYER = """
      SELECT r.room_id,
                       r.buyer_id,
                       r.seller_id,
                       r.product_id,
                       r.status,
                       r.created_at,
                       s.nickname   AS seller_nickname,
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
                JOIN seller_page s ON r.seller_id = s.member_id
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
                WHERE r.buyer_id = ?
  """;

  // ---- Seller 전용 ----
  public List<ChatRoomListDto> findBySellerId(Long sellerId) {
    String sql = BASE_SQL_SELLER + " ORDER BY r.created_at DESC";
    return queryList(sql, sellerId);
  }

  public List<ChatRoomListDto> findActiveBySellerId(Long sellerId) {
    String sql = BASE_SQL_SELLER + " AND r.status = 'ACTIVE' ORDER BY r.created_at DESC";
    return queryList(sql, sellerId);
  }

  public List<ChatRoomListDto> findClosedBySellerId(Long sellerId) {
    String sql = BASE_SQL_SELLER + " AND r.status = 'CLOSED' ORDER BY r.closed_at DESC NULLS LAST";
    return queryList(sql, sellerId);
  }

  // ---- Buyer 전용 ----
  public List<ChatRoomListDto> findByBuyerId(Long buyerId) {
    String sql = BASE_SQL_BUYER + " ORDER BY r.created_at DESC";
    return queryList(sql, buyerId);
  }

  public List<ChatRoomListDto> findActiveByBuyerId(Long buyerId) {
    String sql = BASE_SQL_BUYER + " AND r.status = 'ACTIVE' ORDER BY r.created_at DESC";
    return queryList(sql, buyerId);
  }

  public List<ChatRoomListDto> findClosedByBuyerId(Long buyerId) {
    String sql = BASE_SQL_BUYER + " AND r.status = 'CLOSED' ORDER BY r.closed_at DESC NULLS LAST";
    return queryList(sql, buyerId);
  }

  // ---- 공통 RowMapper ----
  private List<ChatRoomListDto> queryList(String sql, Long memberId) {
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      ChatRoomListDto dto = new ChatRoomListDto();
      dto.setRoomId(rs.getLong("room_id"));
      dto.setBuyerId(rs.getLong("buyer_id"));
      dto.setSellerId(rs.getLong("seller_id"));
      dto.setProductId(rs.getLong("product_id"));
      dto.setStatus(rs.getString("status"));
      dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
      // Buyer 전용 / Seller 전용 컬럼이 다름
      try { dto.setBuyerNickname(rs.getString("buyer_nickname")); } catch (Exception ignored) {}
      try { dto.setSellerNickname(rs.getString("seller_nickname")); } catch (Exception ignored) {}
      dto.setProductTitle(rs.getString("product_title"));
      dto.setProductImage(rs.getBytes("product_image"));
      dto.setLastMessage(rs.getString("last_message"));
      dto.setHasNew(rs.getInt("has_new") == 1);
      return dto;
    }, memberId, memberId);
  }
}


