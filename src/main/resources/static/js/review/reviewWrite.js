import { ajax } from '/js/community/common.js';

//카테고리,태그 관련
const product_category = document.querySelector('.summary')?.getAttribute('data-category') ?? null;
const tagListEl = document.querySelector('#reviewTags .tag-list');
const selectedTagsInput = document.getElementById('selectedTags');

//전송 관련
const saveBtn   = document.getElementById('saveBtn');
const scoreEl   = document.getElementById('score');     // hidden (별점)
const contentEl = document.getElementById('content');   // textarea
const cancelBtn  = document.getElementById('btnCancel');

try {
  if (product_category) {
    const resCat = await ajax.get(`/api/review/tag/${product_category}`);
    if (resCat.header?.rtcd === 'S00' && Array.isArray(resCat.body)) {
      tagListEl.innerHTML = resCat.body
        .map(tag => `<button type="button" class="tag" data-id="${tag.tagId}">${tag.label}</button>`)
        .join('');
    }
  }
} catch (e) {
  console.error('태그 로드 실패', e);
}

// 클릭 이벤트: 여러개 선택 가능
let selectedOrder = [];

// 선택 순서 배지 갱신
const updateOrderBadges = () => {
  // 일단 모두 비우고
  tagListEl?.querySelectorAll('.tag').forEach(el => el.removeAttribute('data-order'));
  // selectedOrder 순서대로 번호 부여(1부터)
  selectedOrder.forEach((id, idx) => {
    const el = tagListEl?.querySelector(`.tag[data-id="${id}"]`);
    if (el) el.setAttribute('data-order', String(idx + 1));
  });
  // hidden input 동기화
  selectedTagsInput.value = selectedOrder.join(',');
};

tagListEl?.addEventListener('click', e => {
  if (!e.target.classList.contains('tag')) return;
  const id = e.target.dataset.id;

  if (e.target.classList.toggle('selected')) {
    // 새 선택 → 순서 배열에 추가
    selectedOrder.push(id);
  } else {
    // 해제 → 배열에서 제거 & 이 버튼의 배지 제거
    selectedOrder = selectedOrder.filter(item => item !== id);
    e.target.removeAttribute('data-order');
  }

  // 번호 다시 매기기
  updateOrderBadges();

  console.log('클릭 순서대로:', selectedOrder);
});

const parseOrderItemId = () => {
  const seg = location.pathname.split('/').filter(Boolean);
  const id = Number(seg.at(-1));
  return Number.isFinite(id) ? id : null;
};

// 3) 저장 버튼 클릭 → 서버 전송 ===
saveBtn?.addEventListener('click', async () => {
  const orderItemId = parseOrderItemId();
  const score   = Number(scoreEl?.value || 0);
  const content = (contentEl?.value || '').trim();
  const tagIds  = selectedOrder.map(n => Number(n)).filter(Number.isFinite); // 선택 순서 그대로
  const category = document.querySelector('.summary')?.getAttribute('data-category') ?? null;

  // 최소 검증
  if (!orderItemId) { alert('주문 항목 식별자가 없습니다.'); return; }
  if (score <= 0 || score > 5) { alert('별점을 선택해 주세요.'); return; }
  if (!category) { alert('카테고리 정보를 확인할 수 없습니다.'); return; }

  // 서버 규격에 맞게 키 매핑
  const payload = { orderItemId, score, content, tagIds, category };

  try {
    saveBtn.disabled = true;
    const res = await ajax.post('/api/review', payload);
    if (res?.header?.rtcd === 'S00') {
      alert('리뷰가 저장되었습니다.');
      // 필요 시 이동
      // location.href = '/review/list';
    } else {
      alert(res?.header?.rtmsg || '저장에 실패했습니다.');
    }
  } catch (err) {
    console.error('리뷰 저장 실패:', err);
    alert('서버 통신 중 오류가 발생했습니다.');
  } finally {
    saveBtn.disabled = false;
  }
});
  document.addEventListener('DOMContentLoaded', () => {
  const textarea = document.getElementById('content');
  if (!textarea) return;   // 안전장치

  const handleResizeHeight = () => {
    textarea.style.height = 'auto';
    textarea.style.height = textarea.scrollHeight + 'px';
  };

  // 로드시, 입력시 자동 리사이즈
  handleResizeHeight();
  textarea.addEventListener('input', handleResizeHeight);
});