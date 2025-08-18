/*popup.js*/

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
const pad = (n) => String(n).padStart(2,'0');
function fmtTime(v){
  const d = v ? new Date(v) : new Date();
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
function scrollToBottom(){ list.scrollTop = list.scrollHeight; }

// ====== 렌더 ======
function renderMessage(msg){
  // msg: {roomId, senderId, content, type:'TEXT'|'IMAGE', createdAt}
  const mine = msg.senderId === senderId;

  const wrap = document.createElement('div');
  wrap.className = `item ${mine ? 'me' : 'them'}`;

  const bubble = document.createElement('div');
  bubble.className = 'bubble';

  if ((msg.type || 'TEXT') === 'IMAGE') {
    const im = document.createElement('img');
    im.className = 'msg-img';
    im.src = msg.content; // 서버는 이미지 URL 또는 dataURL 반환
    im.alt = 'image';
    bubble.appendChild(im);
  } else {
    bubble.textContent = msg.content;
  }

  const time = document.createElement('div');
  time.className = 'time';
  time.textContent = fmtTime(msg.createdAt);

  wrap.appendChild(bubble);
  wrap.appendChild(time);
  list.appendChild(wrap);
  scrollToBottom();
}

// ====== WebSocket ======
function connect(){
  const socket = new SockJS('/ws');
  stomp = Stomp.over(socket);
  stomp.connect({}, ()=>{
    // 구독
    stomp.subscribe(`/topic/room/${roomId}`, (frame)=>{
      const body = JSON.parse(frame.body);
      renderMessage(body);
    });

    // 최근 메시지(선택)
    fetch(`/api/chat/messages?roomId=${roomId}&size=30`)
      .then(r => r.ok ? r.json() : [])
      .then(arr => arr.forEach(renderMessage))
      .catch(()=>{ /* 무시 */ });
  }, (err)=>{
    console.error('[stomp error]', err);
    alert('채팅 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.');
  });
}

// ====== 전송 ======
function sendText(){
  const content = input.value.trim();
  if (!content || !stomp || !stomp.connected) return;
  const payload = { roomId, senderId, content, type:'TEXT' };
  stomp.send('/app/chat.send', {}, JSON.stringify(payload));
  input.value = '';
}

async function sendImage(file){
  if (!file) return;
  const fd = new FormData();
  fd.append('file', file);
  fd.append('roomId', roomId);

  const res = await fetch('/api/chat/upload', { method:'POST', body: fd });
  if (!res.ok) {
    const t = await res.text().catch(()=> '이미지 업로드 실패');
    alert(t);
    return;
  }
  const { imageUrl } = await res.json();
  const payload = { roomId, senderId, content:imageUrl, type:'IMAGE' };
  stomp.send('/app/chat.send', {}, JSON.stringify(payload));
}

// ====== 이벤트 ======
sendBtn.addEventListener('click', sendText);
input.addEventListener('keydown', (e) => { if(e.key === 'Enter') sendText(); });
fileInp.addEventListener('change', (e)=>{
  const f = e.target.files?.[0];
  if (f) sendImage(f);
  e.target.value = ''; // 같은 파일 재선택 허용
});

// 시작
connect();
