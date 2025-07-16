/* ──────────────────────────────────────────────
   editBuyerPage.js  (파일선택 미리보기·닉네임·주소·검증 통합)
   ────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {

  /* ───── 요소 캐싱 ───── */
  const form          = document.querySelector('#buyerUpdateForm');
  const nicknameInput = document.querySelector('#nickname');
  const nicknameBtn   = document.querySelector('#nicknameToggleBtn');
  const resultSpan    = document.querySelector('#nicknameCheckMsg');
  const introTextarea = document.querySelector('#intro');
  const imageInput    = document.querySelector('#imageFile');
  const previewImg    = document.querySelector('#sectionProfilePreview'); // section의 미리보기 이미지 ID
  const asideProfileImage = document.querySelector('#asideProfileImage'); // aside의 이미지 ID

  /* ───── 공통 상수 ───── */
  const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
  const allowExt      = ['jpg', 'jpeg', 'png', 'gif'];
  const maxSize       = 2 * 1024 * 1024; // 2MB

  /* ──────────────────────────────────
     1) 닉네임 중복 확인
     ────────────────────────────────── */
  nicknameBtn?.addEventListener('click', async () => {
    const nickname = nicknameInput.value.trim();

    // 1‑1) 1차 화면 검증
    if (!nickname)            return showError('닉네임을 입력해주세요.');
    if (nickname.length < 2)  return showError('닉네임은 2자 이상이어야 합니다.');
    if (nickname.length > 30) return showError('닉네임은 30자 이하여야 합니다.');
    if (!nicknameRegex.test(nickname))
      return showError('닉네임은 한글·영문·숫자만 사용할 수 있습니다.');

    // 1‑2) 서버 중복 체크
    try {
      const res        = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(nickname)}`);
      const duplicated = await res.json();              // true → 이미 존재

      if (duplicated) {
        showError('이미 사용 중인 닉네임입니다.');
      } else {
        resultSpan.textContent = '사용 가능한 닉네임입니다.';
        resultSpan.className   = 'form-text ms-2 text-success';
      }
    } catch (err) {
      console.error(err);
      showError('중복 확인 중 오류가 발생했습니다.');
    }
  });

  nicknameInput?.addEventListener('input', resetNicknameMsg);

  function showError(msg) {
    resultSpan.textContent = msg;
    resultSpan.className   = 'form-text ms-2 text-danger';
    nicknameInput.focus();
  }
  function resetNicknameMsg() {
    resultSpan.textContent = '';
    resultSpan.className   = 'form-text ms-2';
  }

  /* ──────────────────────────────────
     2) 주소 검색 (다음 우편번호)
     ────────────────────────────────── */
  document.getElementById('btnSearchAddr')?.addEventListener('click', () => {
    new daum.Postcode({
      oncomplete: data => {
        document.getElementById('zonecode').value = data.zonecode;
        document.getElementById('address').value  = data.roadAddress || data.jibunAddress;
        document.getElementById('detailAddress').focus();
      }
    }).open();
  });

  /* ──────────────────────────────────
     3) 프로필 사진 선택 시 즉시 미리보기 (및 aside 동기화)
     ────────────────────────────────── */
  imageInput?.addEventListener('change', e => {
    const file = e.target.files[0];
    if (!file) {
      // 파일 선택 취소 시 기존 이미지 유지
      // (서버에서 받은 초기 이미지로 되돌리려면, 해당 이미지의 URL을 어딘가에 저장해두고 사용해야 합니다)
      return;
    }

    const ext = file.name.split('.').pop().toLowerCase();
    if (!allowExt.includes(ext)) {
      alert('이미지는 jpg · jpeg · png · gif 형식만 가능합니다.');
      imageInput.value = '';
      // 파일 형식 오류 시 미리보기/aside 이미지도 기본 이미지로 되돌릴지 결정
      // 예: previewImg.src = "기본 이미지 경로";
      // 예: asideProfileImage.src = "기본 이미지 경로";
      return;
    }
    if (file.size > maxSize) {
      alert('이미지 파일은 2MB 이하만 업로드 가능합니다.');
      imageInput.value = '';
      // 파일 크기 오류 시 미리보기/aside 이미지도 기본 이미지로 되돌릴지 결정
      return;
    }

    const reader = new FileReader();
    reader.onload = ev => {
      // section의 미리보기 이미지 업데이트
      previewImg.src = ev.target.result;

      // aside의 프로필 이미지 업데이트
      if (asideProfileImage) { // asideProfileImage 요소가 존재하는지 확인
        asideProfileImage.src = ev.target.result;
        // 만약 aside에 기본 프로필 이미지가 표시되고 있었다면, 숨깁니다.
        const asideDefaultProfileImage = document.querySelector('aside .profile-image img[th:if="${member == null or member.pic == null}"]');
        if (asideDefaultProfileImage) {
            asideDefaultProfileImage.style.display = 'none';
        }
        // 새로운 이미지를 표시합니다.
        asideProfileImage.style.display = 'block'; // 혹시 display: none 되어있을 경우를 대비
      }
    };
    reader.readAsDataURL(file);
  });

  /* ──────────────────────────────────
     4) 클라이언트 최종 검증 후 submit
     ────────────────────────────────── */
  form?.addEventListener('submit', e => {
    const nick = nicknameInput.value.trim();

    if (!nick)               return block('닉네임을 입력해주세요.');
    if (nick.length < 2)     return block('닉네임은 2자 이상이어야 합니다.');
    if (nick.length > 30)    return block('닉네임은 30자 이하여야 합니다.');
    if (!nicknameRegex.test(nick))
      return block('닉네임은 한글·영문·숫자만 사용할 수 있습니다.');

    if (introTextarea.value.length > 60) // HTML maxlength="60"과 일치시켰습니다.
      return block('자기소개는 최대 60자까지 입력할 수 있습니다.');

    // 이미지 유효성은 change 이벤트에서 검증했으므로 재검사 불필요
    /* 통과 ⇒ 브라우저 기본 submit */
  });

  function block(msg) {
    alert(msg);
    event.preventDefault(); // 기본 submit 동작 방지
  }
});