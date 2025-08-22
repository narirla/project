// product_detail.js
// 상품 상세 페이지에서 "문의하기" 버튼을 눌렀을 때 실행되는 스크립트
// 역할: 채팅방 보장(생성 또는 기존 방 찾기) + 채팅 팝업창 열기

// CSRF 토큰 정보 (Spring Security 사용 시 필요)
const CSRF = {
  token : document.querySelector('meta[name="_csrf"]')?.content,     // 토큰 값
  header: document.querySelector('meta[name="_csrf_header"]')?.content // 헤더 이름
};

// DOM 선택 간단 헬퍼
const $ = (s)=>document.querySelector(s);

/**
 * 채팅 팝업 열기 (문의하기 버튼 클릭 시 실행됨)
 */
async function openChatPopup(){
  // 상품 상세 페이지 최상단 DOM(#page)에 저장된 데이터 속성 활용
  const page = $('#page');
  const productId = Number(page.dataset.productId); // 상품 ID
  const sellerId  = Number(page.dataset.sellerId);  // 판매자 ID
  const buyerId   = Number(page.dataset.buyerId);   // 구매자(현재 로그인 회원) ID

  // 로그인 체크
  if(!buyerId){
    alert('로그인 후 이용해주세요.');
    location.href='/login';
    return;
  }
  // 상품/판매자 정보 유효성 체크
  if(!productId || !sellerId){
    alert('상품/판매자 정보가 잘못되었습니다.');
    return;
  }
  // 본인 상품일 경우 차단
  if(buyerId === sellerId){
    alert('본인에게는 문의를 보낼 수 없습니다.');
    return;
  }

  // -------------------------------
  // 1) 방 보장(ensure API 호출)
  //    - 이미 방이 있으면 해당 방 반환
  //    - 없으면 새로 생성 후 반환
  // -------------------------------
  const res = await fetch('/api/chat/rooms/ensure', {
    method: 'POST',
    headers: Object.assign(
      { 'Content-Type':'application/json' },
      CSRF.token ? { [CSRF.header]: CSRF.token } : {} // CSRF 헤더 추가
    ),
    body: JSON.stringify({ productId, buyerId, sellerId }) // 요청 바디
  });
  if(!res.ok){
    const text = await res.text();
    throw new Error(text || 'ensureRoom 실패');
  }
  const ensured = await res.json();
  const roomId = ensured.roomId; // 서버에서 반환한 채팅방 ID

  // -------------------------------
  // 2) 팝업 오픈
  // -------------------------------
  const w = 405, h = 630; // 팝업 크기
  const left = window.screenX + (window.outerWidth - w) + 20; // 화면 오른쪽에 위치
  const top  = window.screenY + 80;                          // 화면 위에서 80px

  const features = [
    `width=${w}`,
    `height=${h}`,
    `left=${left}`,
    `top=${top}`,
    'resizable=yes',
    'scrollbars=no',
    'noopener',   // 부모-자식 간 window 객체 접근 차단 (보안)
    'noreferrer'  // Referer 헤더 차단 (보안)
  ].join(',');

  // 서버 컨트롤러에서 popup_real.html을 렌더링하도록 매핑해두어야 함
  window.open(
    `/api/chat/popup?roomId=${encodeURIComponent(roomId)}`,
    `chat_${roomId}`,
    features
  );
}

// -------------------------------
// 이벤트 바인딩
// -------------------------------
// 문의 버튼(.inquiry-btn, .btn-question) 클릭 시 openChatPopup 실행
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.inquiry-btn, .btn-question')
    .forEach(btn => btn.addEventListener('click', openChatPopup));
});
