// popup.js
// 채팅 팝업 내부에서 동작하는 스크립트
// 역할: 채팅 히스토리 로딩 + WebSocket 연결 + 메시지 송/수신 처리

// ====== 초기 데이터 ======
const root = document.body;
const roomId = Number(root.dataset.roomId);
const senderId = Number(root.dataset.senderId);
const role = root.dataset.role;

console.log("현재 roomId:", roomId, "현재 senderId:", senderId);

const $ = (s) => document.querySelector(s);
const list = $("#list");
const input = $("#text");
const sendBtn = $("#send");
const fileInp = $("#file");
const exitBtn = $("#exitBtn"); // 👉 나가기 버튼
const endBtn = $("#endBtn"); // 👉 종료 요청 버튼

// 👉 구매자면 종료 요청 버튼 숨기기
if (role !== "SELLER" && endBtn) {
  endBtn.style.display = "none";
}

let stomp = null;

// ====== 유틸 ======
const pad = (n) => String(n).padStart(2, "0");
function fmtTime(v) {
  try {
    const d = v ? new Date(v) : new Date();
    if (isNaN(d.getTime())) throw new Error("Invalid Date");
    return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
  } catch {
    const now = new Date();
    return `${pad(now.getHours())}:${pad(now.getMinutes())}`;
  }
}
function scrollToBottom() {
  requestAnimationFrame(() => {
    list.scrollTop = list.scrollHeight;
  });
}

// ====== 메시지 렌더링 ======
function renderMessage(msg) {
  console.log("🖼️ renderMessage 호출:", msg);
  const mine = msg.senderId == senderId;
  const wrap = document.createElement("div");
  wrap.className = `item ${mine ? "me" : "them"}`;
  wrap.dataset.id = msg.msgId;

  if (!mine) {
    const profile = document.createElement("img");
    profile.className = "profile";

    if (role === "BUYER") {
      // 내가 구매자 → 상대는 판매자
      profile.src = msg.seller_profileImage
        ? `data:image/jpeg;base64,${msg.seller_profileImage}`
        : "/img/default-profile.png";
    } else if (role === "SELLER") {
      // 내가 판매자 → 상대는 구매자
      profile.src = msg.buyer_profileImage
        ? `data:image/jpeg;base64,${msg.buyer_profileImage}`
        : "/img/default-profile.png";
    }

    wrap.appendChild(profile);
  }

  const msgBox = document.createElement("div");
  msgBox.className = "msg-box";

  if (!mine) {
    const nick = document.createElement("div");
    nick.className = "nickname";

    if (role === "BUYER") {
      nick.textContent = msg.seller_nickname || "상대방";
    } else if (role === "SELLER") {
      nick.textContent = msg.buyer_nickname || "상대방";
    }

    msgBox.appendChild(nick);
  }

  //말풍선
  const bubble = document.createElement("div");
  bubble.className = "bubble";

  if ((msg.type || "TEXT") === "IMAGE") {
    const im = document.createElement("img");
    im.className = "msg-img";
    im.src = msg.content;
    im.alt = "image";
    bubble.appendChild(im);
  } else {
    bubble.textContent = msg.content;
  }

  msgBox.appendChild(bubble);

  //시간
  const time = document.createElement("div");
  time.className = "time";
  time.textContent = fmtTime(msg.createdAt);
  msgBox.appendChild(time);

  wrap.appendChild(msgBox);
  list.appendChild(wrap);
  scrollToBottom();
}

// ====== 시스템 메시지 렌더링 ======
function renderSystemMessage(text) {
  const wrap = document.createElement("div");
  wrap.className = "system-msg";

  const msgBox = document.createElement("div");
  msgBox.className = "sys-msg-box";

  const bubble = document.createElement("div");
  bubble.className = "bubble system";
  bubble.textContent = text;

  msgBox.appendChild(bubble);
  wrap.appendChild(msgBox);
  list.appendChild(wrap);

  scrollToBottom();
}

// ====== WebSocket 연결 ======
function connect() {
  const socket = new SockJS("/ws");
  stomp = Stomp.over(socket);

  stomp.connect(
    {},
    () => {
      console.log("✅ STOMP connected");

      // 1) 메시지 구독
      stomp.subscribe(`/topic/chat/rooms/${roomId}`, (frame) => {
        try {
          const body = JSON.parse(frame.body);
          console.log("📩 구독으로 받은 메시지:", body, "type:", body.type);

          // 👉 종료 요청 이벤트 처리
          if (body.type === "END_REQUEST") {
            console.log("📩 END_REQUEST 이벤트 수신:", body);

            // 내가 보낸 종료요청이면 무시
            if (body.senderId === senderId) return;

            // role이 BUYER인 경우에만 다이얼로그 표시
            if (role === "BUYER") {
              showEndConfirmDialog();
            }
            return;
          }

          // 👉 종료 확정 이벤트 처리
          if (body.type === "END_CONFIRM") {
            console.log("✅ 채팅 종료 확정 이벤트 수신");

            if (role === "BUYER") {
              // 구매자: 팝업 닫기
              disconnect();
              window.close();
            } else if (role === "SELLER") {
              // 판매자: 닫지 않고 시스템 메시지 출력
              renderSystemMessage("-- 채팅이 종료되었습니다 --");
            }
            return;
          }

          // 👉 종료 거절 이벤트 처리
          if (body.type === "END_CANCEL") {
            console.log("❌ 상대방이 종료를 거절했습니다.");
            return;
          }

          // 기본 메시지 처리
          renderMessage(body);

          if (body.senderId != senderId) {
            console.log("📤 읽음 이벤트 전송:", {
              roomId,
              readerId: senderId,
              lastReadMessageId: body.msgId,
            });
            stomp.send(
              `/app/chat/rooms/${roomId}/read`,
              {},
              JSON.stringify({
                roomId,
                readerId: senderId,
                lastReadMessageId: body.msgId,
              })
            );
          }
        } catch (err) {
          console.error("메시지 파싱 오류:", err);
        }
      });

      // 2) 읽음 이벤트 구독
      stomp.subscribe(`/topic/chat/rooms/${roomId}/read`, (frame) => {
        try {
          const { lastReadMessageId, readerId } = JSON.parse(frame.body);
          console.log("👀 읽음 이벤트 수신:", lastReadMessageId, readerId);
          if (readerId == senderId) return;

          setTimeout(() => {
            document
              .querySelectorAll(".item.me .read")
              .forEach((el) => el.remove());
            const msgEl = document.querySelector(
              `.item.me[data-id="${lastReadMessageId}"]`
            );
            if (msgEl) {
              const msgBox = msgEl.querySelector(".msg-box");
              const readEl = document.createElement("div");
              readEl.className = "read";
              readEl.textContent = "읽음";
              msgBox.appendChild(readEl);
            }
          }, 100);
        } catch (err) {
          console.error("읽음 이벤트 처리 오류:", err);
        }
      });

      // 3) 최근 메시지 불러오기 + 방 입장 시 읽음 처리
      fetch(`/api/chat/rooms/${roomId}/messages?limit=30`)
        .then((r) => {
          console.log("📡 fetch 응답 상태:", r.status);
          return r.ok ? r.json() : [];
        })
        .then((arr) => {
          console.log("📜 fetch로 받은 메시지 배열:", arr);
          if (Array.isArray(arr)) {
            arr.forEach(renderMessage);

            const last = arr[arr.length - 1];
            if (last && last.senderId != senderId) {
              console.log("📡 방 입장: 마지막 메시지 읽음 처리", last.msgId);
              stomp.send(
                `/app/chat/rooms/${roomId}/read`,
                {},
                JSON.stringify({
                  roomId,
                  readerId: senderId,
                  lastReadMessageId: last.msgId,
                })
              );
            }
          }
        })
        .catch((err) => {
          console.error("❌ fetch 중 오류:", err);
        });
    },
    (err) => {
      console.error("[stomp error]", err);
      alert("채팅 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
  );
}

// ====== 전송 함수 ======
function sendText() {
  const content = input.value.trim();
  if (!content || !stomp || !stomp.connected) return;

  const payload = { roomId, senderId, content, type: "TEXT" };
  console.log("✉️ 텍스트 전송:", payload);
  stomp.send(`/app/chat/rooms/${roomId}`, {}, JSON.stringify(payload));
  input.value = "";
}

async function sendImage(file) {
  if (!file) return;
  const fd = new FormData();
  fd.append("file", file);
  fd.append("roomId", roomId);

  try {
    const res = await fetch("/api/chat/upload", { method: "POST", body: fd });
    console.log("📡 이미지 업로드 응답:", res.status);
    if (!res.ok) {
      const t = await res.text().catch(() => "이미지 업로드 실패");
      alert(t);
      return;
    }
    const { imageUrl } = await res.json();
    const payload = { roomId, senderId, content: imageUrl, type: "IMAGE" };
    console.log("🖼️ 이미지 전송:", payload);
    stomp.send(`/app/chat/rooms/${roomId}`, {}, JSON.stringify(payload));
  } catch (err) {
    console.error("이미지 전송 실패:", err);
    alert("이미지 전송 중 오류가 발생했습니다.");
  }
}

// ====== disconnect & closeRoom ======
function disconnect() {
  if (stomp && stomp.connected) {
    stomp.disconnect(() => {
      console.log("❌ STOMP disconnected");
    });
  }
}

async function closeRoom() {
  try {
    const res = await fetch(`/api/chat/rooms/${roomId}/close`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    });

    if (res.ok) {
      console.log("✅ 방 상태 CLOSED 처리 완료");
    } else {
      console.error("❌ 방 상태 변경 실패:", res.status);
    }
  } catch (err) {
    console.error("❌ 방 상태 변경 중 오류:", err);
  }
}

// ====== 종료 확인 모달 ======
function showEndConfirmDialog() {
  const dialog = document.createElement("div");
  dialog.className = "end-confirm";

  dialog.innerHTML = `
    <div class="end-dialog-box">
      <p>채팅을 종료하시겠습니까?</p>
      <div class="end-btn-box">
        <button class="endYes" id="endYes">예</button>
        <button class="endNo" id="endNo">아니오</button>
      </div>
    </div>
  `;

  document.body.appendChild(dialog);

  document.getElementById("endYes").addEventListener("click", () => {
    stomp.send(
      `/app/chat/rooms/${roomId}`,
      {},
      JSON.stringify({
        roomId,
        senderId,
        type: "END_CONFIRM",
      })
    );
    setTimeout(() => {
      disconnect(); //소켓 끊기
      window.close(); //팝업 닫기
    }, 200);
    // dialog.remove();
  });

  document.getElementById("endNo").addEventListener("click", () => {
    stomp.send(
      `/app/chat/rooms/${roomId}`,
      {},
      JSON.stringify({
        roomId,
        senderId,
        type: "END_CANCEL",
      })
    );
    dialog.remove();
  });
}

// ====== 이벤트 바인딩 ======
sendBtn.addEventListener("click", sendText);
input.addEventListener("keydown", (e) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    sendText();
  }
});
fileInp.addEventListener("change", (e) => {
  const f = e.target.files?.[0];
  if (f) sendImage(f);
  e.target.value = "";
});

// '나가기' 버튼 만들면 사용
// if (exitBtn) {
//   exitBtn.addEventListener("click", async () => {
//     await closeRoom();
//     disconnect();
//     window.close();
//   });
// }

if (endBtn) {
  endBtn.addEventListener("click", () => {
    if (!stomp || !stomp.connected) return;
    const payload = { roomId, senderId, type: "END_REQUEST" };
    console.log("📤 종료 요청 전송:", payload);
    stomp.send(`/app/chat/rooms/${roomId}`, {}, JSON.stringify(payload));
  });
}

// ====== 실행 시작 ======
connect();
