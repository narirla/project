import { ajax } from '/js/common.js';
const Quill = window.Quill;
const parentIdAttr = document.body.dataset.parentId;
const parentId     = parentIdAttr ? Number(parentIdAttr) : null;
let parentCategory = null;
const btnTempSaveToggle = document.getElementById('tempSaveButton');
const tempSaveListEl    = document.getElementById('tempSaveList');
const tempSaveCountEl   = document.getElementById('tempSaveCount');
const tempSaveItemsEl   = document.getElementById('tempSaveListItems');


//Quill 에디터 초기화
const quill = new Quill('#editor', {
    theme: 'snow',
    modules: {
        toolbar: '#toolbar',
        imageDrop:true,
        imageResize: {}
    }
});

// 부모 게시글 카테고리 로드
if (parentId) {
    try {
      const res = await ajax.get(`/api/bbs/${parentId}`);
      if (res.header.rtcd === 'S00') {
        parentCategory = res.body.bcategory;
      }
    } catch (e) {
      parentCategory = null;
    }
}

// 게시글 저장
async function addBbs(data) {
    data.status = 'B0201';
    const res = await ajax.post('/api/bbs', data);
    if (res.header.rtcd === 'S00') {
      window.location.href = parentId
        ? `/csr/bbs/${parentId}`
        : '/csr/bbs';
    } else {
      alert("저장에 실패했습니다.");
    }
}

// 임시저장 호출
async function saveDraft(data) {
    data.status = 'B0203';
    const res = await ajax.post('/api/bbs', data);
    if (res.header.rtcd === 'S00') {
      window.location.href = '/csr/bbs';
    } else {
      alert("임시 저장에 실패했습니다.");
    }
}

// 폼 요소 참조
const wrap           = document.querySelector('.content-area');
const frm            = wrap.querySelector('#write-form');
const categorySelect = wrap.querySelector('#bcategory');
const btnDraft       = wrap.querySelector('#temp-save-btn');


// 카테고리 로드
try {
  const resCat = await ajax.get('/api/bbs/categories');
  if (resCat.header.rtcd === 'S00' && Array.isArray(resCat.body)) {
    resCat.body.forEach(code => {
      const opt = document.createElement('option');
      opt.value       = code.codeId;
      opt.textContent = code.decode;
      categorySelect.appendChild(opt);
    });
  }
  if (parentCategory) {
    categorySelect.value = parentCategory;
    categorySelect.setAttribute('disabled', '');
  }
} catch (e) {
  console.error('카테고리 로드 실패', e);
}

// 초기 임시저장 확인 및 로드/삭제 (새글 vs 답글 구분)
try {
  const checkUrl = parentId
    ? `/api/bbs/temp/check?pbbsId=${parentId}`
    : '/api/bbs/temp/check';
  const checkRes = await ajax.get(checkUrl);
  if (checkRes.header.rtcd === 'S00' && checkRes.body) {
    const message = parentId
      ? '이전 답글 초안을 불러오시겠습니까?'
      : '이전 임시저장을 불러오시겠습니까?';
    if (confirm(message)) {
      const loadUrl = parentId
        ? `/api/bbs/temp/load?pbbsId=${parentId}`
        : '/api/bbs/temp/load';
      const loadRes = await ajax.get(loadUrl);
      if (loadRes.header.rtcd === 'S00') {
        frm.querySelector('[name="title"]').value      = loadRes.body.title;
        quill.root.innerHTML = loadRes.body.bcontent || '';
        // 드래프트 카테고리 선택
        categorySelect.value = loadRes.body.bcategory || '';
      }
    } else {
      const deleteUrl = parentId
        ? `/api/bbs/temp?pbbsId=${parentId}`
        : '/api/bbs/temp';
      await ajax.delete(deleteUrl);
    }
  }
} catch (e) {
  console.error('임시저장 확인 실패', e);
}

// 등록 핸들러
frm.addEventListener('submit', e => {
  e.preventDefault();
  document.getElementById('editorContent').value = quill.root.innerHTML;
  const data = Object.fromEntries(new FormData(frm).entries());
  if (parentId) data.pbbsId = parentId;
  if (!data.title.trim())      return alert('제목은 필수입니다.');
  if (!parentId && (!data.bcategory || !data.bcategory.trim())) {
    return alert('카테고리를 선택하세요.');
  }
  if (!data.bcontent.trim())   return alert('내용은 필수입니다.');
  addBbs(data);
});

// 임시 저장 핸들러
btnDraft.addEventListener('click', () => {
  document.getElementById('editorContent').value = quill.root.innerHTML;
  const data = Object.fromEntries(new FormData(frm).entries());
  if (!data.title.trim() && !data.bcontent.trim()) {
    return alert('제목 또는 내용을 입력해야 임시 저장할 수 있습니다.');
  }
  if (parentId) data.pbbsId = parentId;
  saveDraft(data);
});




  // 4) 파일 선택시 파일명 표시
  document.getElementById('file-input')
    .addEventListener('change', function() {
      const display = document.getElementById('file-name-display');
      display.textContent = this.files.length
        ? Array.from(this.files).map(f => f.name).join(', ')
        : '선택된 파일 없음';
    });