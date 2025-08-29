import { ajax,PaginationUI } from '/js/community/common.js';

// ====== 설정값 ======
const LIST_URL     = '/api/review/product';             // ← 베이스 URL
const TOTAL_URL    = '/api/review/product/reviewCnt';   // ← 슬래시 추가 + 이름 통일
const PROFILE_URL  = '/api/review/product/profile-images';
const PAGE_SIZE    = 5;
let   currentPage  = 1;
let   totalCount   = 0;

// ====== 유틸 ======
const reviewBox     = document.getElementById('review');

const reviewTotalEl = reviewBox.querySelector('#review-total');
const reviewSection = reviewBox.querySelector('.review_list');


const section = document.querySelector('.product-body-right');

const pid     = section.dataset.productId;

// 페이징용
const PAGING_ID = 'review-paging';
let pager = null;

function setupPager() {
  pager = new PaginationUI(PAGING_ID, (page) => {
    getReview(page, PAGE_SIZE);
  });
  pager.setRecordsPerPage(PAGE_SIZE);
  pager.setPagesPerPage(10);
  pager.setTotalRecords(totalCount);
  pager.render();
  togglePagerVisibility();
}

function togglePagerVisibility() {
  const el = document.getElementById(PAGING_ID);
  if (el) el.style.display = totalCount > PAGE_SIZE ? '' : 'none';
}

// ====== 총개수 ======
async function fetchTotalCount() {
  if (!pid) {  return; }
  try {
    const url = `${TOTAL_URL}/${pid}`;       // ← 슬래시 추가 반영
    const res = await ajax.get(url);
    if (res?.header?.rtcd === 'S00') {
      totalCount = Number(res.body ?? 0) || 0;
    } else {
      totalCount = 0;
    }
  } catch (e) {
    console.error('총개수 조회 실패', e);
    totalCount = 0;
  }
  if (reviewTotalEl) reviewTotalEl.textContent = `등록 리뷰 수 ${totalCount}건`;

  if (pager) {                      // ← 추가
    pager.setTotalRecords(totalCount);
    pager.render();
    togglePagerVisibility();
  }
}

// ====== 목록 조회 ======
const getReview = async (reqPage, reqRec) => {
  try {
    const url = `${LIST_URL}/${pid}?pageNo=${reqPage}&numOfRows=${reqRec}`;
    const result = await ajax.get(url);

    if (result?.header?.rtcd === 'S00') {
      currentPage = reqPage;
      const rows = result.body ?? [];

      renderList(rows);
      if (pager) {                      // ← 추가
        pager.state.currentPage = reqPage;
        pager.render();
      }
    } else {
      alert(result?.header?.rtmsg ?? '목록 조회 실패');
    }
  } catch (err) {
    console.error(err);
    renderEmpty('목록을 불러오지 못했습니다.');
  }
};

async function getProfileImg(authorId) {
  try {
    const url = `${PROFILE_URL}/${authorId}`;
    const result = await ajax.get(url);

    if (result?.header?.rtcd === 'S00') {
      return result.body;
    } else {
      return;
    }
  } catch (err) {
    return;
  }
}

// base64 시그니처로 MIME 추정 (JPEG/PNG/GIF/WEBP/BMP 일부)
function guessMimeFromBase64(b64 = '') {
  if (!b64) return null;
  const head = b64.slice(0, 10);
  if (head.startsWith('/9j/'))   return 'image/jpeg';
  if (head.startsWith('iVBOR'))  return 'image/png';
  if (head.startsWith('R0lGOD')) return 'image/gif';
  if (head.startsWith('UklGR'))  return 'image/webp';
  if (head.startsWith('Qk'))     return 'image/bmp';
  return null;
}

// data URL 생성 헬퍼
function makeImgSrc(b64) {
  if (!b64) return '/img/bbs/bbs_detail/profile-pic.png';
  if (typeof b64 === 'string' && b64.startsWith('data:')) return b64;
  const mime = guessMimeFromBase64(b64) || 'image/jpeg';
  return `data:${mime};base64,${b64}`;
}

// ====== 개별 아이템 템플릿 ======
async function renderOneItem(item = {}) {
  let img = {};
  if (item.hasPic == 1) {
    img = (await getProfileImg(item.buyerId)) || {};
  }
  const dateText = formatYyMMddHm(item.rcreate);

  const html = `
    <div class="review" data-review-id="${item.reviewId ?? ''}">
      <div class="review_item">
        <div class="review_profile">
          <img
            class="profile-pic"
            src="${makeImgSrc(img?.imageData)}"
            alt="프로필사진"
            loading="lazy"
            decoding="async"
          >
        </div>
        <div class="review_meta">
          <div class="review_left">
            <div class="star-rating" data-score="${Number(item.score ?? 0)}">
              <div class="star-fill"></div>
            </div>
            <span class="rating_score">${Number(item.score ?? 0).toFixed(1)}</span>
            <span class="review_date">${dateText}</span>
          </div>
          <div class="profile_name">${item.nickname ?? ''}</div>
        </div>
      </div>
      <div class="review_tag">${stripPipes(item.tagLabels)}</div>
      <div class="review_content">${item.content ?? ''}</div>
      <div class="review_option">${item.optionType ?? ''}</div>
    </div>
  `;
  return html;
}

// ====== 리스트 렌더 ======
async function renderList(items = []) {
  if (!reviewSection) return;
  if (!items.length) {
    renderEmpty('등록된 리뷰가 없습니다.');
    return;
  }

  // 모든 renderOneItem 결과를 기다림
  const htmlArr = await Promise.all(items.map(renderOneItem));
  reviewSection.innerHTML = htmlArr.join('');
  window.initRatingDisplays?.(reviewSection);
}

function renderEmpty(msg = '데이터가 없습니다.') {
  if (!reviewSection) return;
  reviewSection.innerHTML = `<div class="empty">${msg}</div>`;
}

// ====== 초기 구동 ======
document.addEventListener('DOMContentLoaded', async () => {
  await fetchTotalCount();
  setupPager();
  await getReview(1, PAGE_SIZE);
});


window.initRatingDisplays = function initRatingDisplays(root = document) {
  root.querySelectorAll('.star-rating').forEach(el => {
    const raw = Number(el.dataset.score || 0);
    const score = Math.max(0, Math.min(5, raw));
    const fill = el.querySelector('.star-fill');
    if (fill) fill.style.width = (score / 5) * 100 + '%';
  });
};


// ================== 시간 포맷: "yy.MM.dd HH:mm" ==================
function formatYyMMddHm(input) {
  const d = (function toDate(x) {
    if (!x) return null;
    if (x instanceof Date) return isNaN(x) ? null : x;
    if (typeof x === 'number') {
      // 초 단위로 들어오면 ms로 변환
      const ms = x < 1e12 ? x * 1000 : x;
      const dd = new Date(ms);
      return isNaN(dd) ? null : dd;
    }
    if (typeof x === 'string') {
      let s = x.trim();
      // "YYYY-MM-DD HH:mm:ss" → "YYYY-MM-DDTHH:mm:ss"
      if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}/.test(s)) {
        s = s.replace(' ', 'T');
      }
      // 마이크로초(6자리 이상) → 밀리초(3자리)로 자르기
      s = s.replace(/(\.\d{3})\d+/, '$1');
      // 날짜만 있으면 자정으로 보정
      if (/^\d{4}-\d{2}-\d{2}$/.test(s)) s += 'T00:00:00';
      const dd = new Date(s);
      return isNaN(dd) ? null : dd;
    }
    return null;
  })(input);

  if (!d) return '';

  const yy = String(d.getFullYear()).slice(-2);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const mi = String(d.getMinutes()).padStart(2, '0');

  return `${yy}.${mm}.${dd} ${hh}:${mi}`;
}


function stripPipes(s) {
  return (s ?? '').replace(/\s*\|\s*/g, ' ').trim();
}