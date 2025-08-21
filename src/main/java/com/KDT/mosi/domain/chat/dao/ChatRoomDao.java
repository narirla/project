package com.KDT.mosi.domain.chat.dao;

import com.KDT.mosi.domain.dto.chat.ChatPopupDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomDao {
  private final NamedParameterJdbcTemplate jdbc;

  //진행중인 채팅방 조회
  public Long findActiveRoomId(long productId, long buyerId, long sellerId) {
    String sql = """
      SELECT ROOM_ID FROM CHAT_ROOM
      WHERE PRODUCT_ID=:pid AND BUYER_ID=:bid AND SELLER_ID=:sid AND STATUS='ACTIVE'
    """;
    var p = new MapSqlParameterSource()
        .addValue("pid", productId).addValue("bid", buyerId).addValue("sid", sellerId);
    var list = jdbc.queryForList(sql, p, Long.class);
    return list.isEmpty() ? null : list.get(0);
  }


  //채팅방 생성
  public long createRoom(long productId, long buyerId, long sellerId) {
    Long roomId = jdbc.getJdbcTemplate()
        .queryForObject("SELECT CHAT_ROOM_SEQ.NEXTVAL FROM DUAL", Long.class);

    String sql = """
      INSERT INTO CHAT_ROOM (ROOM_ID, BUYER_ID, SELLER_ID, PRODUCT_ID, STATUS, CREATED_AT)
      VALUES (:rid, :bid, :sid, :pid, 'ACTIVE', SYSTIMESTAMP)
    """;
    var p = new MapSqlParameterSource()
        .addValue("rid", roomId).addValue("bid", buyerId).addValue("sid", sellerId).addValue("pid", productId);
    jdbc.update(sql, p);
    return roomId;
  }


  /** 동시성 대비: 먼저 조회하고, 없으면 시도→유니크 충돌 시 다시 조회 */
  @Transactional
  public long ensureActiveRoom(long productId, long buyerId, long sellerId) {
    Long existed = findActiveRoomId(productId, buyerId, sellerId);
    if (existed != null) return existed;
    try {
      return createRoom(productId, buyerId, sellerId);
    } catch (org.springframework.dao.DuplicateKeyException e) {
      // 유니크 충돌 → 동시에 누가 만들었음 → 다시 조회해서 반환
      Long now = findActiveRoomId(productId, buyerId, sellerId);
      if (now != null) return now;
      throw e;
    }
  }

  //팝업 채팅창 정보 조회
  public ChatPopupDto findPopupInfo(long roomId) {
    String sql = """
        SELECT r.room_id,
               r.buyer_id,
               r.seller_id,
               b.nickname   AS buyer_nickname,
               s.nickname   AS seller_nickname,
               p.title      AS product_title,
               p.sales_price      AS product_price,
               pi2.image_data AS product_image
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
        WHERE r.room_id = :roomId
    """;

    var params = new MapSqlParameterSource().addValue("roomId", roomId);
    return jdbc.queryForObject(sql, params,
        (rs, rowNum) -> {
          ChatPopupDto dto = new ChatPopupDto();
          dto.setRoomId(rs.getLong("room_id"));
          dto.setProductTitle(rs.getString("product_title"));
          dto.setProductPrice(rs.getLong("product_price"));
          dto.setBuyerId(rs.getLong("buyer_id"));
          dto.setSellerId(rs.getLong("seller_id"));
          dto.setBuyerNickname(rs.getString("buyer_nickname"));
          dto.setSellerNickname(rs.getString("seller_nickname"));
          dto.setProductImage(rs.getBytes("product_image")); // BLOB → Byte[]
          return dto;
        }
    );
  }





}
