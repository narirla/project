// popup.js
// 채팅 팝업 내부에서 동작하는 스크립트
// 역할: 채팅 히스토리 로딩 + WebSocket 연결 + 메시지 송/수신 처리

document.addEventListener("DOMContentLoaded", () => {
  // -------------------------------
  // 0) 전역 변수 및 DOM 요소
  // -------------------------------
  const roomId   = document.body.dataset.roomId;     // 방 ID (서버에서 Thymeleaf로 주입)
  const senderId = document.body.dataset.senderId;   // 내 ID (로그인한 회원)
  const msgsDiv  = document.getElementById("list");  // 메시지 출력 영역
  const input    = document.getElementById("text");  // 입력창
  const sendBtn  = document.getElementById("send");  // 전송 버튼

  let stomp = null; // STOMP 클라이언트 인스턴스

  // -------------------------------
  // 1) 메시지 출력 함수
  // -------------------------------
  function renderMessage(msg) {
    const div = document.createElement("div");
    // 내가 보낸 메시지(mine)와 상대방(theirs)을 구분
    div.className = "msg " + (msg.senderId == senderId ? "mine" : "theirs");

    // 메시지 버블 형태 HTML
    div.innerHTML = `
      <div class="bubble">
        ${msg.content}
        <span class="time">${msg.createdAt || ""}</span>
      </div>
    `;

    msgsDiv.appendChild(div);
    // 항상 최신 메시지가 보이도록 스크롤 아래로
    msgsDiv.scrollTop = msgsDiv.scrollHeight;
  }

  // -------------------------------
  // 2) 히스토리 불러오기 (REST API 호출)
  // -------------------------------
// 최근 메시지 불러오기
async function loadHistory() {
  try {
    const res = await fetch(`/api/chat/rooms/${roomId}/messages?limit=30`);
     console.log("roomId from Thymeleaf:", document.body.dataset.roomId);
    if (!res.ok) {
      // 서버에서 에러코드 내려왔을 때만 경고
      console.warn("이전 메시지 불러오기 실패 (status: " + res.status + ")");
      return;
    }

    const list = await res.json();

    if (!Array.isArray(list) || list.length === 0) {
      console.log("불러올 메시지가 없습니다.");
      return;
    }

    list.forEach(renderMessage);

    // 스크롤 맨 아래로 이동
    msgsDiv.scrollTop = msgsDiv.scrollHeight;

  } catch (err) {
    console.error("메시지 불러오기 중 오류:", err);
  }
}


  // -------------------------------
  // 3) WebSocket 연결
  // -------------------------------
  function connect() {
    // /ws 엔드포인트는 서버(Spring WebSocketConfig)에 맞춰야 함
    const socket = new SockJS("/ws");
    stomp = Stomp.over(socket);

    // STOMP 연결 시작
    stomp.connect({}, () => {
      console.log("✅ STOMP connected");

      // 해당 방 구독 (서버에서 /topic/chat/rooms/{roomId}로 메시지 브로드캐스트 가정)
      stomp.subscribe(`/topic/chat/rooms/${roomId}`, (frame) => {
        const msg = JSON.parse(frame.body);
        console.log("📩 새 메시지 수신:", msg);
        renderMessage(msg); // 수신 메시지 출력
      });
    });
  }

  // -------------------------------
  // 4) 메시지 전송
  // -------------------------------
  function sendMessage() {
    const text = input.value.trim();
    if (!text || !stomp || !stomp.connected) return;

    // 보낼 메시지 DTO 구조 (서버 ChatMessageDto 참고)
    const payload = {
      roomId: roomId,
      senderId: senderId,
      content: text,
      clientMsgId: Date.now() // 임시 클라이언트 메시지 ID
    };

    // 서버의 @MessageMapping("/app/chat/rooms/" + roomId)로 전송
    stomp.send("/app/chat/rooms/" + roomId, {}, JSON.stringify(payload));
//    consol.log(payload);

    // 입력창 비우기
    input.value = "";
  }

  // -------------------------------
  // 5) 이벤트 바인딩
  // -------------------------------
  // 전송 버튼 클릭 시 메시지 전송
  sendBtn.addEventListener("click", (e) => {
    e.preventDefault();
    sendMessage();
  });

  // Enter 키 입력 시 메시지 전송
  input.addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      sendMessage();
    }
  });

  // -------------------------------
  // 6) 실행
  // -------------------------------
  loadHistory(); // 페이지 로드 시 이전 대화 불러오기
  connect();     // WebSocket 연결
});
