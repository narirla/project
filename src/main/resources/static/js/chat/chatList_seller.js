document.addEventListener("DOMContentLoaded", () => {
  const sellerId = document.body.dataset.sellerId;
  const listProgress = document.querySelector("#progress .inquiry-list"); // 진행중
  const listClosed = document.querySelector("#closed .inquiry-list"); // 종료됨

  const socket = new SockJS("/ws");
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log("✅ WebSocket 연결됨");

    // 판매자 전용 채널 (새 메시지 알림)
    stompClient.subscribe(`/topic/chat/rooms/${sellerId}`, (message) => {
      const roomDto = JSON.parse(message.body);
      console.log("📩 새 방 알림:", roomDto);
      refreshRoomList("active"); // 진행중 목록 갱신
    });

    // 초기 로딩
    refreshRoomList("active");
  });

  // ✅ Ajax로 채팅방 목록 불러오기
  function refreshRoomList(type) {
    let url;
    if (type === "active") url = `/chat/rooms/seller/api/active`;
    else if (type === "closed") url = `/chat/rooms/seller/api/closed`;
    else url = `/chat/rooms/seller/api`;

    fetch(url)
      .then((res) => res.json())
      .then((data) => {
        console.log(`📋 ${type} 목록:`, data);
        if (type === "active") {
          renderRoomList(data, listProgress);
        } else if (type === "closed") {
          renderRoomList(data, listClosed);
        }

        // ✅ 각 roomId별 읽음 이벤트 구독
        data.forEach((room) => {
          stompClient.subscribe(
            `/topic/chat/rooms/${room.roomId}/read`,
            (frame) => {
              const readEvent = JSON.parse(frame.body);
              console.log("👀 읽음 이벤트 수신:", readEvent);

              const row = document.querySelector(
                `.inquiry-row[data-room-id="${room.roomId}"]`
              );
              if (row) {
                const newLabel = row.querySelector(".new-label");
                if (newLabel) newLabel.remove();
              }
            }
          );
        });
      })
      .catch((err) => console.error("❌ 목록 조회 실패", err));
  }

  // ✅ 화면에 목록 그리기
  function renderRoomList(data, container) {
    container.innerHTML = "";

    if (!data || data.length === 0) {
      container.innerHTML =
        "<div class='no-inquiry'>받은 문의가 없습니다.</div>";
      return;
    }

    data.forEach((room) => {
      const row = document.createElement("div");
      row.classList.add("inquiry-row");
      row.dataset.roomId = room.roomId;

      row.innerHTML = `
        <div class="inquiry-list-product_number">${room.roomId}</div>
        <div class="inquiry-list-product_img">
          ${
            room.productImage
              ? `<img src="data:image/jpeg;base64,${room.productImage}" alt="썸네일" width="120"/>`
              : `<span>이미지 없음</span>`
          }
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

      const openChat = () => {
        window.open(
          `/api/chat/popup?roomId=${room.roomId}`,
          `chat_${room.roomId}`,
          "width=400,height=600"
        );
      };

      row
        .querySelector(".inquiry-list-buyer_nickname")
        ?.addEventListener("click", openChat);
      row
        .querySelector(".inquiry-list-last_msg")
        ?.addEventListener("click", openChat);

      container.appendChild(row);
    });
  }

  // ✅ 탭 메뉴 클릭
  document.querySelectorAll(".tab-button").forEach((btn) => {
    btn.addEventListener("click", () => {
      document
        .querySelectorAll(".tab-button")
        .forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");

      const tab = btn.dataset.tab;
      document
        .querySelectorAll(".tab-content")
        .forEach((c) => c.classList.remove("active"));
      document.getElementById(tab).classList.add("active");

      if (tab === "progress") {
        refreshRoomList("active");
      } else if (tab === "closed") {
        refreshRoomList("closed");
      }
    });
  });
});
