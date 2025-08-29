// /js/review/seller_review_list.js
// type="module" 가정
import { ajax } from '/js/community/common.js';

// ====== 설정값 ======
const LIST_URL     = '/api/review/paging/buyer';
const TOT_CNT_URL  = '/api/review/buyer/totCnt';
const PAGE_SIZE    = 10;              // 서버에 명시 전달(원하면 숫자만 바꿔)
let   currentPage  = 1;
let   totalCount   = 0;

// ====== 유틸 ======
const qs  = (sel, el=document) => el.querySelector(sel);
const qsa = (sel, el=document) => [...el.querySelectorAll(sel)];
const escapeHTML = (s='') => { const d=document.createElement('div'); d.textContent = s ?? ''; return d.textContent; };
const ymd = (iso) => {
  const d = parseDateSafe(iso);          // 안전 파서 사용
  if (!d) return escapeHTML(String(iso));
  const p = (n)=>String(n).padStart(2,'0');
  return `${d.getFullYear()}.${p(d.getMonth()+1)}.${p(d.getDate())}`;
};
const splitTagLabels = (labels) =>
  String(labels ?? '')
    .split('|')
    .map(s => s.trim())
    .filter(Boolean);


// 마이크로초가 붙은 ISO도 안전하게 파싱 (…SSS#### → …SSS 로 잘라냄)
const parseDateSafe = (iso) => {
  if (!iso) return null;
  const trimmed = String(iso).replace(/(\.\d{3})\d+$/, '$1');
  const d = new Date(trimmed);
  return isNaN(d) ? null : d;
};

const isNewWithinDays = (iso, days = 3) => {
  const d = parseDateSafe(iso);
  if (!d) return false;
  const diff = Date.now() - d.getTime();
  return diff >= 0 && diff <= days * 24 * 60 * 60 * 1000;
};

// ====== 버튼 라벨 토글(네 코드 유지) ======
function setToggleLabel(btn, isOpen){
  const arrow = btn.querySelector('.arrow');
  btn.textContent = (isOpen ? '접기' : '펼치기') + ' ';
  if (arrow) btn.appendChild(arrow);
}

document.addEventListener('click', (e)=>{
  const btn = e.target.closest('.btnToggle');
  if(!btn) return;

  const item = btn.closest('.review_lists')
            || btn.closest('.review_item')
            || btn.closest('.review_item_summary')?.parentElement;
  if(!item) return;

  const panel = item.querySelector('.review-collapsible');
  if(!panel) return;

  const willOpen = panel.style.maxHeight === '' || panel.style.maxHeight === '0px';

  if(willOpen){
    panel.style.maxHeight = panel.scrollHeight + 'px';
    btn.classList.add('is-open');
    setToggleLabel(btn, true);   // 접기
  }else{
    panel.style.maxHeight = panel.scrollHeight + 'px';
    panel.offsetHeight; // reflow
    panel.style.maxHeight = '0px';
    btn.classList.remove('is-open');
    setToggleLabel(btn, false);  // 펼치기
  }
});

// 열린 패널 리사이즈 대응
window.addEventListener('resize', ()=>{
  document.querySelectorAll('.review-collapsible').forEach(p=>{
    if(p.style.maxHeight && p.style.maxHeight !== '0px'){
      p.style.maxHeight = p.scrollHeight + 'px';
    }
  });
});

// ====== DOM 앵커 ======
const mainArea   = qs('.review_list_main');
const counterEl  = qs('.overall-info > div', mainArea);

// 렌더 전에 기존 아이템(.review_lists)과 페이지네이션 제거
function clearListArea() {
  qsa('.review_list_main .review_lists', mainArea).forEach(el => el.remove());
  qs('.review_list_main .pagination', mainArea)?.remove();
}

// 페이지네이션 컨테이너 보장
function ensurePager() {
  let pager = qs('.review_list_main .pagination', mainArea);
  if (!pager) {
    pager = document.createElement('div');
    pager.className = 'pagination';
    mainArea.appendChild(pager);
  }
  return pager;
}

// ====== 총개수 ======
async function fetchTotalCount() {
  try {
    const res = await ajax.get(TOT_CNT_URL);
    if (res?.header?.rtcd === 'S00') {
      totalCount = Number(res.body ?? 0) || 0;
    } else {
      totalCount = 0;
    }
  } catch(e) {
    console.error('총개수 조회 실패', e);
    totalCount = 0;
  }
  if (counterEl) counterEl.textContent = `총 ${totalCount}개`;
}

// ====== 목록 API(네 형식 유지) ======
const getBbs = async (reqPage, reqRec) => {
  try {
    // ?pageNo= 필수 + numOfRows 전달(서버 기본과 다르면 여기서만 조정)
    const url = `${LIST_URL}?pageNo=${reqPage}&numOfRows=${reqRec}`;
    const result = await ajax.get(url);
    if (result.header.rtcd === 'S00') {
      currentPage = reqPage;
      await displayBbsList(result.body ?? []);
    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err);
    renderEmpty('목록을 불러오지 못했습니다.');
  }
};

// ====== 렌더링 ======
function renderEmpty(message='표시할 리뷰가 없습니다.') {
  clearListArea();
  const empty = document.createElement('div');
  empty.className = 'review_lists';
  empty.innerHTML = `<div style="padding:20px; color:#666;">${escapeHTML(message)}</div><hr>`;
  mainArea.appendChild(empty);
  renderPagination(0, 0, currentPage);
}

function renderOneItem(item, listNumber) {
  // 명세 매핑
  const title     = item.title ?? '';
  const dateStr   = ymd(item.rcreate);
  const score     = Number(item.score ?? 0);
  const tagsAll   = splitTagLabels(item.tagLabels);
  const tags3     = tagsAll.slice(0,3);
  const tags3Line = tags3.join('  ');
  const detailTag = tagsAll.join('  ');
  const content   = item.content ?? '';
  const option    = item.optionType ?? '';
  const imgUrl = item.productImageId
    ? `/api/review/product-images/${item.productImageId}`
    : '/img/no-img.png';

  // 이미지 스킵(빈 이미지 박스만 유지)
  const html = `
    <div class="review_lists" data-id="${item.reviewId ?? ''}">
      <div class="review_item_summary">
        <div class="listNumber">${item.reviewId}</div>
        <div class="listImg">
          <div class="image">
            <img src="${imgUrl}" alt="${escapeHTML(title)}" loading="lazy" decoding="async">
          </div>
        </div>
        <div class="review-info">
          <div class="reviewFirst">
            <div class="reviewTitle">${escapeHTML(title)}</div>
            <div class="reviewDate">${escapeHTML(dateStr)}</div>
          </div>
          <div class="reviewSecond">
            <span class="star-label"></span>
            <div class="star-rating" data-score="${Number.isFinite(score) ? score : 0}">
              <div class="star-fill"></div>

            </div>
          </div>
          <div class="reviewThird">
            <div>${escapeHTML(tags3Line)}</div>
          </div>
          <div class="reviewFourth">
            <div class="fourthText">${escapeHTML(content)}</div>
          </div>
        </div>
        <div class="review-button">
          <div class="btnSelect">
            <div>
              <button type="button" class="btnEdit">수정</button>
              <span> | </span>
              <button type="button" class="btnDel">삭제</button>
            </div>
            <div class="toggle">
              <button type="button" class="btnToggle">
                펼치기 <span class="arrow">﹀</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="review-collapsible" style="max-height:0; overflow:hidden;">
        <div class="collapsible-inner">
          <div class="detailOption">
            <div class="option">옵션</div>
            <div class="optionText">${escapeHTML(option)}</div>
          </div>
          <div class="detailTag">${escapeHTML(detailTag)}</div>
          <div class="detailText">
            ${escapeHTML(content)}
          </div>
        </div>
      </div>
    </div>
  `;
  return html;
}

async function displayBbsList(items) {
  clearListArea();

  // 번호 계산(전체 개수 기반 내림차순)
  const startNo = totalCount
    ? (totalCount - (currentPage - 1) * PAGE_SIZE)
    : ((currentPage - 1) * PAGE_SIZE + 1);

  if (!Array.isArray(items) || items.length === 0) {
    renderEmpty();
    return;
  }

  // 아이템들 렌더
  const frag = document.createDocumentFragment();
  items.forEach((it, idx) => {
    const wrap = document.createElement('div');
    wrap.innerHTML = renderOneItem(it, totalCount ? (startNo - idx) : (startNo + idx));
    // innerHTML로 만들어진 최상위 .review_lists만 옮김
    frag.appendChild(wrap.firstElementChild);
  });
  mainArea.appendChild(frag);

  // 별점 스크립트가 있다면 재초기화
  if (typeof window.initRatingDisplays === 'function') {
    window.initRatingDisplays();
  }

  // 페이지네이션
  renderPagination(totalCount, PAGE_SIZE, currentPage);
}

// ====== 페이지네이션 ======
function renderPagination(total, size, page) {
  const pager = ensurePager();
  pager.innerHTML = '';

  if (!total || !size) {
    // 총개수/사이즈 없으면 페이저 숨김
    pager.style.display = 'none';
    return;
  }
  pager.style.display = '';

  const totalPages = Math.max(1, Math.ceil(total / size));
  const windowSize = 5;
  const start = Math.max(1, page - Math.floor(windowSize/2));
  const end   = Math.min(totalPages, start + windowSize - 1);
  const realStart = Math.max(1, end - windowSize + 1);

  const mkBtn = (label, target, disabled=false, active=false) => {
    const el = document.createElement('span');
    el.className = `page-link${active ? ' active' : ''}${disabled ? ' disabled' : ''}`;
    el.dataset.page = String(target);
    el.textContent = label;
    return el;
  };

  pager.appendChild(mkBtn('«', 1, page === 1));
  pager.appendChild(mkBtn('‹', Math.max(1, page - 1), page === 1));

  for (let p = realStart; p <= end; p++) {
    pager.appendChild(mkBtn(String(p), p, false, p === page));
  }

  pager.appendChild(mkBtn('›', Math.min(totalPages, page + 1), page === totalPages));
  pager.appendChild(mkBtn('»', totalPages, page === totalPages));

  pager.onclick = (e) => {
    const btn = e.target.closest('.page-link');
    if (!btn || btn.classList.contains('disabled')) return;
    const nextPage = parseInt(btn.dataset.page, 10);
    if (Number.isFinite(nextPage) && nextPage !== currentPage) {
      goPage(nextPage);
    }
  };
}

// ====== 흐름 ======
async function goPage(pageNo=1) {
  // 총개수 먼저 보장(처음 1회만)
  if (totalCount === 0) {
    await fetchTotalCount();
  }
  await getBbs(pageNo, PAGE_SIZE);
}

document.addEventListener('DOMContentLoaded', async () => {
  markBuyerSidebarActive();
  // 초기 진입: 페이지 1
  await fetchTotalCount();
  await getBbs(1, PAGE_SIZE);
});


window.initRatingDisplays = function() {
  const labels = {
    1: "매우 불만",
    2: "불만",
    3: "보통",
    4: "만족",
    5: "매우 만족"
  };

  const MAX = 5;
  document.querySelectorAll('.reviewSecond').forEach(root => {
    const starBox = root.querySelector('.star-rating');
    const fillBox = starBox.querySelector('.star-fill');
    const labelEl = root.querySelector('.star-label');

    const raw = parseFloat(starBox.dataset.score ?? "0");
    const score = Math.round(Math.max(1, Math.min(MAX, raw)));
    const percent = (raw / MAX) * 100;

    fillBox.style.width = `${percent}%`;
    labelEl.textContent = labels[score];
  });
};

document.addEventListener('click', (e) => {
  const btn = e.target.closest('.btnEdit');
  if (!btn) return;

  const item = btn.closest('.review_lists');
  const reviewId = item?.dataset?.id;
  if (!reviewId) {
    alert('리뷰 ID를 찾을 수 없습니다.');
    return;
  }
  // 컨트롤러가 @RequestMapping("/review") 아래라면 다음 경로가 매핑됩니다.
  location.href = `/review/edit/${reviewId}`;
});

// ====== 삭제 처리 ======
async function deleteReviewById(reviewId){
  try {
    const res = await ajax.delete(`/api/review/${reviewId}`);

    if (res?.header?.rtcd === 'S00') {
      // 총 개수 감소 반영
      totalCount = Math.max(0, (totalCount || 0) - 1);
      fetchTotalCount();
      // 현재 페이지에서 마지막 1개를 지웠다면 이전 페이지로 이동
      const rendered = document.querySelectorAll('.review_list_main .review_lists').length;
      const willBeEmpty = rendered <= 1; // 지금 보이는 게 1개면, 이걸 지우면 빈 페이지

      if (willBeEmpty && currentPage > 1) {
        await goPage(currentPage - 1);
      } else {
        await goPage(currentPage);
      }
    } else {
      alert(res?.header?.rtmsg || '삭제에 실패했습니다.');
    }
  } catch (e) {
    console.error('삭제 실패', e);
    alert('삭제 중 오류가 발생했습니다.');
  }
}

// 삭제 버튼 델리게이션
document.addEventListener('click', async (e) => {
  const delBtn = e.target.closest('.btnDel');
  if (!delBtn) return;

  const item = delBtn.closest('.review_lists');
  const reviewId = item?.dataset?.id;
  if (!reviewId) {
    alert('리뷰 ID를 찾을 수 없습니다.');
    return;
  }

  if (!confirm('이 리뷰를 삭제할까요?')) return;

  await deleteReviewById(reviewId);
})

// ====== Buyer Sidebar 활성화 ======
function markBuyerSidebarActive() {
  const sidebar = document.querySelector('#BUYER_SIDEBAR');
  if (!sidebar) return;

  // 사이드바 안의 모든 메뉴에서 is-active 제거
  sidebar.querySelectorAll('a.is-active').forEach(el => {
    el.classList.remove('is-active');
  });

  // 리뷰 메뉴에만 is-active 추가
  const reviewLink = sidebar.querySelector('a[href="/review/buyer/list"]');
  if (reviewLink) {
    reviewLink.classList.add('is-active');
  }
}
