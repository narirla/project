import { ajax } from '/js/community/common.js';

//카테고리,태그 관련
const product_category = document.querySelector('.summary')?.getAttribute('data-category') ?? null;
const tagListEl = document.querySelector('#reviewTags .tag-list');
const selectedTagsInput = document.getElementById('selectedTags');

//전송 관련
const saveBtn   = document.getElementById('saveBtn');
const scoreEl   = document.getElementById('score');     // hidden (별점)
const contentEl = document.getElementById('content');   // textarea
const cancelBtn  = document.getElementById('cancelBtn');
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

const initSelectedTags = () => {
  const preset = (selectedTagsInput?.value || '')
    .split(',')
    .map(s => s.trim())
    .filter(Boolean);

  if (!preset.length || !tagListEl) return;

  // 선택 순서 유지
  selectedOrder = [...preset]; // 문자열로 유지 (dataset 비교용)

  // 버튼에 selected, data-order 부여
  preset.forEach((id, idx) => {
    const el = tagListEl.querySelector(`.tag[data-id="${id}"]`);
    if (el) el.classList.add('selected');
  });

  updateOrderBadges(); // 번호/hidden 동기화
};

try {
  if (product_category) {
    const resCat = await ajax.get(`/api/review/tag/${product_category}`);
    if (resCat.header?.rtcd === 'S00' && Array.isArray(resCat.body)) {
      tagListEl.innerHTML = resCat.body
        .map(tag => `<button type="button" class="tag" data-id="${tag.tagId}">${tag.label}</button>`)
        .join('');
        initSelectedTags();
    }
  }
} catch (e) {
  console.error('태그 로드 실패', e);
}

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
const root = document.querySelector('.content.review');
const mode = (root?.dataset.mode === 'edit') ? 'edit' : 'add';
const reviewId = Number(root?.dataset.reviewId || 0);

const toNum = v => {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
};

saveBtn?.addEventListener('click', async () => {
  const score   = toNum(scoreEl?.value);
  const content = (contentEl?.value || '').trim();

  // 선택된 태그: 클릭 순서 유지. 비어있으면 hidden 값 복원
  const tagIds =
    (selectedOrder.length ? selectedOrder : (selectedTagsInput?.value || '').split(','))
      .map(Number)
      .filter(Number.isFinite);

  if (score <= 0 || score > 5) { alert('별점을 선택해 주세요.'); return; }

  let url, payload;

  if (mode === 'edit') {
    // ===== 수정 모드 =====
    if (!reviewId) { alert('리뷰 식별자가 없습니다.'); return; }
    url = '/api/review/update';
    payload = { reviewId, score, content, tagIds }; // productId 불필요
  } else {
    // ===== 작성 모드 =====
    const orderItemIdHidden = toNum(document.getElementById('orderItemId')?.value);
    const orderItemId = orderItemIdHidden || parseOrderItemId();
    if (!orderItemId) { alert('주문 항목 식별자가 없습니다.'); return; }

    const category = document.querySelector('.summary')?.dataset.category
                  || document.querySelector('.summary')?.getAttribute('data-category');
    if (!category) { alert('카테고리 정보를 확인할 수 없습니다.'); return; }

    url = '/api/review';
    payload = { orderItemId, score, content, tagIds, category };
  }

  try {
    saveBtn.disabled = true;
    const res = await ajax.post(url, payload);
    if (res?.header?.rtcd === 'S00') {
       location.href = '/review/buyer/list';
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


const textarea = document.getElementById('content');

const autoResize = () => {
  if (!textarea) return;
  // 레이아웃 적용 직후에 계산하도록 rAF로 한 프레임 미룸
  requestAnimationFrame(() => {
    textarea.style.height = 'auto';
    textarea.style.height = textarea.scrollHeight + 'px';
  });
};

// ① 즉시 1회 (서버가 채워준 초기 내용 반영)
autoResize();

// ② 입력/붙여넣기 때마다
textarea?.addEventListener('input', autoResize);
textarea?.addEventListener('change', autoResize);
textarea?.addEventListener('paste', () => setTimeout(autoResize, 0));

// ③ 모든 리소스/이미지/폰트 로드 후에도 한 번 더
window.addEventListener('load', autoResize);
document.fonts?.ready?.then(autoResize);