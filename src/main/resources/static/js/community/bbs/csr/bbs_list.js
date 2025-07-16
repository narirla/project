import { ajax, PaginationUI } from '/js/community/common.js';
const $list = document.getElementById('post-tbody')
let currentPage = 1;
const recordsPerPage = 10;
const pagesPerPage   = 10;


// 게시글 목록 조회
const getBbs = async (reqPage, reqRec) => {
  try {
    const url = `/api/bbs/paging?pageNo=${reqPage}&numOfRows=${reqRec}`;
    const result = await ajax.get(url);
    console.log('목록 가져오기');
    if (result.header.rtcd === 'S00') {
      currentPage = reqPage;
      displayBbsList(result.body);
      console.log('목록 가져오기 성공');
    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err);
  }
};

// 페이지네이션 설정
async function configPagination() {
  try {
    console.log('페이지네이션 시작');
    const result = await ajax.get('/api/bbs/totCnt');
    const totalRecords = result.body;

    const handlePageChange = page => getBbs(page, recordsPerPage);
    const pagination = new PaginationUI('reply_pagenation', handlePageChange);

    pagination.setTotalRecords(totalRecords);
    pagination.setRecordsPerPage(recordsPerPage);
    pagination.setPagesPerPage(pagesPerPage);

    pagination.handleFirstClick();
  } catch (err) {
    console.error(err);
  }
}

// 목록 그리기 (bindent 만큼 제목에 들여쓰기)
function displayBbsList(bbs) {
  console.log('목록 그리기');
  if (bbs.length === 0) {
    $list.innerHTML = '<tr><td colspan="7">게시글이 없습니다.</td></tr>';
    return;
  }
const rows = bbs.map(b => `
  <tr data-pid="${b.bbsId}">
    <td>${b.bbsId}</td>
    <td>${b.bcategory}</td>
    <td>${'&nbsp;'.repeat((b.bindent||0)*4)}${b.title}</td>
    <td>${b.nickname}</td>
    <td>${b.hit}</td>
    <td>${b.createDate}</td>
    <td>${b.updateDate}</td>
  </tr>
`).join('');
  $list.innerHTML = rows;
}

document.addEventListener('DOMContentLoaded', () => {
  configPagination();          // 페이지네이션 세팅-&-첫 페이지 로드
});