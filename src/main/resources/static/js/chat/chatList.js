document.addEventListener("DOMContentLoaded", () => {
//    const sellerId = document.body.dataset.sellerId; // body 태그에 sellerId를 주입해둔다
    const listDiv = document.getElementById("room-list"); // 채팅방 목록 출력 영역

    // ✅ 1. WebSocket 연결
    const socket = new SockJS("/ws");  // WebSocket 엔드포인트
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        console.log("✅ WebSocket 연결됨");

        // ✅ 2. 판매자 전용 채널 구독
        stompClient.subscribe(`/chat/rooms`, (message) => {
            const roomDto = JSON.parse(message.body);
            console.log("📩 새 채팅방 알림:", roomDto);

            // 알림이 오면 Ajax로 최신 목록 다시 가져오기
            refreshRoomList();
        });

        // 페이지 처음 열 때 목록 로딩
        refreshRoomList();
    });

    // ✅ 3. Ajax로 채팅방 목록 불러오기
    function refreshRoomList() {
        fetch(`/chat/rooms/api`)
            .then(res => res.json())
            .then(data => {
                console.log("📋 현재 채팅방 목록:", data);
                renderRoomList(data);
            })
            .catch(err => {
            console.error("❌ 채팅방 목록 조회 실패", err)
            console.error(err.stack);
            });
    }

    // ✅ 4. 화면에 목록 그려주기
    function renderRoomList(data) {
        listDiv.innerHTML = ""; // 기존 목록 지우기
        if (data.length === 0) {
            listDiv.innerHTML = "<li>받은 문의가 없습니다.</li>";
            return;
        }

        data.forEach(room => {
            const li = document.createElement("li");
//            div.classList.add("room-item");
            li.innerHTML = `
                <b>상품ID:</b> ${room.productId}<br/>
                <b>구매자:</b> ${room.buyerId}<br/>
                <b>채팅방ID:</b> ${room.roomId}
            `;
            // 클릭하면 채팅창 팝업 띄우기
            li.addEventListener("click", () => {
                window.open(`/chat/popup?roomId=${room.roomId}`,
                    "chatPopup",
                    "width=400,height=600");
            });
            listDiv.appendChild(li);
        });
    }

    // 페이지 로스 시 Ajax 호출
//    refreshRoomList();


});
