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

  // 진행중인 채팅방 조회 (ACTIVE)
  public Long findActiveRoomId(long productId, long buyerId, long sellerId) {
    String sql = """
      SELECT ROOM_ID FROM CHAT_ROOM
      WHERE PRODUCT_ID=:pid AND BUYER_ID=:bid AND SELLER_ID=:sid AND STATUS='ACTIVE'
    """;
    var p = new MapSqlParameterSource()
        .addValue("pid", productId)
        .addValue("bid", buyerId)
        .addValue("sid", sellerId);
    var list = jdbc.queryForList(sql, p, Long.class);
    return list.isEmpty() ? null : list.get(0);
  }

  // 종료된 채팅방 조회 (CLOSED)
  public Long findClosedRoomId(long productId, long buyerId, long sellerId) {
    String sql = """
      SELECT ROOM_ID FROM CHAT_ROOM
      WHERE PRODUCT_ID=:pid AND BUYER_ID=:bid AND SELLER_ID=:sid AND STATUS='CLOSED'
      ORDER BY CREATED_AT DESC
    """;
    var p = new MapSqlParameterSource()
        .addValue("pid", productId)
        .addValue("bid", buyerId)
        .addValue("sid", sellerId);
    var list = jdbc.queryForList(sql, p, Long.class);
    return list.isEmpty() ? null : list.get(0);
  }

  // 채팅방 생성
  public long createRoom(long productId, long buyerId, long sellerId) {
    Long roomId = jdbc.getJdbcTemplate()
        .queryForObject("SELECT CHAT_ROOM_SEQ.NEXTVAL FROM DUAL", Long.class);

    String sql = """
      INSERT INTO CHAT_ROOM (ROOM_ID, BUYER_ID, SELLER_ID, PRODUCT_ID, STATUS, CREATED_AT)
      VALUES (:rid, :bid, :sid, :pid, 'ACTIVE', SYSTIMESTAMP)
    """;
    var p = new MapSqlParameterSource()
        .addValue("rid", roomId)
        .addValue("bid", buyerId)
        .addValue("sid", sellerId)
        .addValue("pid", productId);
    jdbc.update(sql, p);
    return roomId;
  }

  /**
   * 방 보장 로직:
   * 1) ACTIVE 방이 있으면 반환
   * 2) 없으면 CLOSED 방을 ACTIVE로 변경 후 반환
   * 3) 그래도 없으면 새로 생성
   */
  @Transactional
  public long ensureActiveRoom(long productId, long buyerId, long sellerId) {
    // 1) ACTIVE 찾기
    Long active = findActiveRoomId(productId, buyerId, sellerId);
    if (active != null) return active;

    // 2) CLOSED 찾기 → 있으면 ACTIVE로 업데이트
    Long closed = findClosedRoomId(productId, buyerId, sellerId);
    if (closed != null) {
      updateStatus(closed, "ACTIVE");
      return closed;
    }

    // 3) 새로 생성 (동시성 대비)
    try {
      return createRoom(productId, buyerId, sellerId);
    } catch (org.springframework.dao.DuplicateKeyException e) {
      Long now = findActiveRoomId(productId, buyerId, sellerId);
      if (now != null) return now;
      throw e;
    }
  }

  // 팝업 채팅창 정보 조회
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
      JOIN BUYER_PAGE b ON r.buyer_id = b.member_id
      JOIN SELLER_PAGE s ON r.seller_id = s.member_id
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

  // ✅ 특정 roomId가 CLOSED 상태인지 확인
  public boolean isClosed(long roomId) {
    String sql = "SELECT CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END FROM chat_room WHERE room_id = :rid";
    var p = new MapSqlParameterSource().addValue("rid", roomId);
    Integer result = jdbc.queryForObject(sql, p, Integer.class);
    return result != null && result == 1;
  }

  // 채팅방 상태 변경 (ACTIVE ↔ CLOSED)
  public int updateStatus(long roomId, String status) {
    String sql = """
      UPDATE CHAT_ROOM
      SET STATUS = :status
      WHERE ROOM_ID = :rid
    """;
    var p = new MapSqlParameterSource()
        .addValue("status", status)
        .addValue("rid", roomId);
    return jdbc.update(sql, p);
  }

  // 특정 roomId의 판매자 ID 조회
  public Long findSellerIdByRoomId(long roomId) {
    String sql = "SELECT seller_id FROM chat_room WHERE room_id = :rid";
    var p = new MapSqlParameterSource().addValue("rid", roomId);
    return jdbc.queryForObject(sql, p, Long.class);
  }

  public Long findBuyerIdByRoomId(long roomId) {
    String sql = "SELECT buyer_id FROM chat_room WHERE room_id = :rid";
    var p = new MapSqlParameterSource().addValue("rid", roomId);
    return jdbc.queryForObject(sql, p, Long.class);
  }



}
