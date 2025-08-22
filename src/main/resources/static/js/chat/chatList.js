document.addEventListener("DOMContentLoaded", () => {
  const sellerId = document.body.dataset.sellerId; // body 태그에 sellerId를 주입해둔다
  const listDiv = document.getElementById("inquiry-list"); // 채팅방 목록 출력 영역

  // ✅ 1. WebSocket 연결
  const socket = new SockJS("/ws"); // WebSocket 엔드포인트
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log("✅ WebSocket 연결됨");

    // ✅ 2. 판매자 전용 채널 구독 (새 메시지 도착 알림)
    stompClient.subscribe(`/topic/chat/rooms/${sellerId}`, (message) => {
      const roomDto = JSON.parse(message.body);
      console.log("📩 알림:", roomDto);
      refreshRoomList();
    });

    // ✅ 3. 목록 초기 로딩 + 각 방별 읽음 이벤트 구독
    refreshRoomList(() => {
      // 목록 다 가져온 뒤 각 roomId별로 읽음 이벤트 구독
      fetch(`/chat/rooms/api?sellerId=${sellerId}`)
        .then(res => res.json())
        .then(data => {
          data.forEach(room => {
            stompClient.subscribe(`/topic/chat/rooms/${room.roomId}/read`, (frame) => {
              const readEvent = JSON.parse(frame.body);
              console.log("👀 읽음 이벤트 수신:", readEvent);

              // 해당 roomId DOM 찾아서 NEW 라벨 제거
              const row = document.querySelector(
                `.inquiry-row[data-room-id="${room.roomId}"]`
              );
              if (row) {
                const newLabel = row.querySelector(".new-label");
                if (newLabel) newLabel.remove();
              }
            });
          });
        });
    });
  });

  // ✅ 4. Ajax로 채팅방 목록 불러오기
  function refreshRoomList(callback) {
    fetch(`/chat/rooms/api?sellerId=${sellerId}`)
      .then((res) => res.json())
      .then((data) => {
        console.log("📋 현재 채팅방 목록:", data);
        renderRoomList(data);
        if (callback) callback(); // 목록 로딩 후 콜백 실행
      })
      .catch((err) => {
        console.error("❌ 채팅방 목록 조회 실패", err);
        console.error(err.stack);
      });
  }

  // ✅ 5. 화면에 목록 그려주기
  function renderRoomList(data) {
    listDiv.innerHTML = ""; // 기존 목록 지우기

    if (!data || data.length === 0) {
      listDiv.innerHTML = "<div>받은 문의가 없습니다.</div>";
      return;
    }

    data.forEach((room) => {
      const row = document.createElement("div");
      row.classList.add("inquiry-row");
      row.dataset.roomId = room.roomId; // DOM에 roomId 저장

      row.innerHTML = `
        <div class="inquiry-list-product_number">${room.roomId}</div>
        <div class="inquiry-list-product_img">
          ${room.productImage
            ? `<img src="data:image/jpeg;base64,${room.productImage}" alt="썸네일" width="120"/>`
            : `<span>이미지 없음</span>`}
        </div>
        <div class="inquiry-list-product_title">${room.productTitle}</div>
        <div class="inquiry-list-msgs">
            <div class="inquiry-list-buyer_nickname">
              ${room.buyerNickname}
              ${room.hasNew ? `<span class="new-label">NEW</span>` : ""}
            </div>
            <div class="inquiry-list-last_msg">
                ${room.lastMessage ?? ""}
            </div>
        </div>
      `;

      // ✅ 이벤트 바인딩
      const buyerEl = row.querySelector(".inquiry-list-buyer_nickname");
      const lastMsgEl = row.querySelector(".inquiry-list-last_msg");

      const openChat = () => {
        window.open(
          `/api/chat/popup?roomId=${room.roomId}`,
          `chat_${room.roomId}`,
          "width=400,height=600"
        );
      };

      if (buyerEl) buyerEl.addEventListener("click", openChat);
      if (lastMsgEl) lastMsgEl.addEventListener("click", openChat);

      listDiv.appendChild(row);
    });
  }
});
