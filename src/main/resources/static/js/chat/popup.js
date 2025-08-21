// popup.js
// 채팅 팝업 내부에서 동작하는 스크립트
// 역할: 채팅 히스토리 로딩 + WebSocket 연결 + 메시지 송/수신 처리
/* popup.js */

// ====== 초기 데이터 ======
const root = document.body;
const roomId   = Number(root.dataset.roomId);
const senderId = Number(root.dataset.senderId);

const $  = (s) => document.querySelector(s);
const list    = $('#list');
const input   = $('#text');
const sendBtn = $('#send');
const fileInp = $('#file');

let stomp = null;

// ====== 유틸 ======
const pad = (n) => String(n).padStart(2, '0');
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
  // msg: {id, roomId, senderId, nickname, profileImage, content, type, createdAt}
  const mine = msg.senderId == senderId;

  const wrap = document.createElement('div');
  wrap.className = `item ${mine ? 'me' : 'them'}`;
  wrap.dataset.id = msg.id; // 메시지 ID 저장

  // 🔹 상대방 프로필 이미지
  if (!mine) {
    const profile = document.createElement('img');
    profile.className = 'profile';
    profile.src = msg.profileImage || '/img/default-profile.png';
    profile.alt = msg.nickname || '상대방';
    wrap.appendChild(profile);
  }

  const msgBox = document.createElement('div');
  msgBox.className = 'msg-box';

  // 🔹 상대방 닉네임
  if (!mine) {
    const nick = document.createElement('div');
    nick.className = 'nickname';
    nick.textContent = msg.nickname || '상대방';
    msgBox.appendChild(nick);
  }

  // 🔹 말풍선
  const bubble = document.createElement('div');
  bubble.className = 'bubble';

  if ((msg.type || 'TEXT') === 'IMAGE') {
    const im = document.createElement('img');
    im.className = 'msg-img';
    im.src = msg.content;
    im.alt = 'image';
    bubble.appendChild(im);
  } else {
    bubble.textContent = msg.content;
  }
  msgBox.appendChild(bubble);

  // 🔹 시간
  const time = document.createElement('div');
  time.className = 'time';
  time.textContent = fmtTime(msg.createdAt);
  msgBox.appendChild(time);

  wrap.appendChild(msgBox);
  list.appendChild(wrap);
  scrollToBottom();
}

// ====== WebSocket 연결 ======
function connect() {
  const socket = new SockJS('/ws');
  stomp = Stomp.over(socket);

  stomp.connect({}, () => {
    console.log("✅ STOMP connected");

    // 1) 메시지 구독
    stomp.subscribe(`/topic/chat/rooms/${roomId}`, (frame) => {
      try {
        const body = JSON.parse(frame.body);
        renderMessage(body);

        // 상대방 메시지 받으면 → 읽음 이벤트 보냄
        if (body.senderId != senderId) {
          stomp.send(`/app/chat/rooms/${roomId}/read`,
            {}, JSON.stringify({ roomId, readerId: senderId, lastReadMessageId: body.id }));
        }
      } catch (err) {
        console.error("메시지 파싱 오류:", err);
      }
    });

    // 2) 읽음 이벤트 구독
    stomp.subscribe(`/topic/chat/rooms/${roomId}/read`, (frame) => {
      try {
        const { lastReadMessageId, readerId } = JSON.parse(frame.body);
        if (readerId == senderId) return; // 내가 보낸 건 무시

        // 기존 읽음 표시 제거
        document.querySelectorAll('.item.me .read').forEach(el => el.remove());

        // 마지막 읽은 메시지에만 표시
        const msgEl = document.querySelector(`.item.me[data-id="${lastReadMessageId}"]`);
        if (msgEl) {
          const msgBox = msgEl.querySelector('.msg-box');

          const readEl = document.createElement('div');
          readEl.className = 'read';
          readEl.textContent = '읽음';
          msgBox.appendChild(readEl);
        }
      } catch (err) {
        console.error("읽음 이벤트 처리 오류:", err);
      }
    });

    // 3) 최근 메시지 불러오기
    fetch(`/api/chat/rooms/${roomId}/messages?limit=30`)
      .then(r => r.ok ? r.json() : [])
      .then(arr => Array.isArray(arr) && arr.forEach(renderMessage))
      .catch(() => { /* 무시 */ });
  }, (err) => {
    console.error('[stomp error]', err);
    alert('채팅 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.');
  });
}

// ====== 전송 함수 ======
function sendText() {
  const content = input.value.trim();
  if (!content || !stomp || !stomp.connected) return;

  const payload = { roomId, senderId, content, type: 'TEXT' };
  stomp.send(`/app/chat/rooms/${roomId}`, {}, JSON.stringify(payload));
  input.value = '';
}

async function sendImage(file) {
  if (!file) return;

  const fd = new FormData();
  fd.append('file', file);
  fd.append('roomId', roomId);

  try {
    const res = await fetch('/api/chat/upload', { method: 'POST', body: fd });
    if (!res.ok) {
      const t = await res.text().catch(() => '이미지 업로드 실패');
      alert(t);
      return;
    }
    const { imageUrl } = await res.json();
    const payload = { roomId, senderId, content: imageUrl, type: 'IMAGE' };
    stomp.send(`/app/chat/rooms/${roomId}`, {}, JSON.stringify(payload));
  } catch (err) {
    console.error('이미지 전송 실패:', err);
    alert('이미지 전송 중 오류가 발생했습니다.');
  }
}

// ====== 이벤트 바인딩 ======
sendBtn.addEventListener('click', sendText);
input.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendText();
  }
});
fileInp.addEventListener('change', (e) => {
  const f = e.target.files?.[0];
  if (f) sendImage(f);
  e.target.value = ''; // 같은 파일 재선택 허용
});

// ====== 실행 시작 ======
connect();
