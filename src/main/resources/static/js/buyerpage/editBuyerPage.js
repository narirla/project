/*editBuyerPage.js*/
document.addEventListener('DOMContentLoaded', () => {
  // ─── 요소 캐싱 ───
  const form = document.querySelector('#buyerUpdateForm');
  const nicknameInput = document.querySelector('#nickname');
  const nicknameBtn = document.querySelector('#nicknameToggleBtn');
  const resultSpan = document.querySelector('#nicknameCheckMsg');
  const introTextarea = document.querySelector('#intro');
  const imageInput = document.querySelector('#imageFile');
  const previewImg = document.querySelector('#sectionProfilePreview');
  const asideProfileImage = document.querySelector('#asideProfileImage');
  const imageMsg = document.getElementById('imageMsg');
  const telInput = document.querySelector('#tel');
  const telMsg = document.querySelector('#telMsg');

  // ─── 상태 값 ───
  let isNicknameChecked = false;
  let checkedNickname = '';
  const originalNickname = (nicknameInput?.value || '').trim();

  // ─── 상수 ───
  const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
  const allowExt = ['jpg', 'jpeg', 'png', 'gif'];
  const maxSize = 2 * 1024 * 1024; // 2MB
  const defaultImageSrc = '/img/default-profile.png';

  // ─── 닉네임 중복 확인 ───
  nicknameBtn?.addEventListener('click', async () => {
    const nickname = (nicknameInput?.value || '').trim();

    if (!nickname) return showError('닉네임을 입력해주세요.');
    if (nickname.length < 2) return showError('닉네임은 2자 이상이어야 합니다.');
    if (nickname.length > 30) return showError('닉네임은 30자 이하여야 합니다.');
    if (!nicknameRegex.test(nickname)) return showError('닉네임은 한글·영문·숫자만 사용할 수 있습니다.');

    try {
      const res = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(nickname)}`);
      const duplicated = await res.json();

      if (duplicated) {
        isNicknameChecked = false;
        checkedNickname = '';
        showError('이미 사용 중인 닉네임입니다.');
      } else {
        isNicknameChecked = true;
        checkedNickname = nickname;
        resultSpan.textContent = '사용 가능한 닉네임입니다.';
        resultSpan.className = 'form-text ms-2 text-success';
      }
    } catch (err) {
      console.error(err);
      isNicknameChecked = false;
      checkedNickname = '';
      showError('중복 확인 중 오류가 발생했습니다.');
    }
  });

  nicknameInput?.addEventListener('input', () => {
    resetNicknameMsg();
    isNicknameChecked = false;
    checkedNickname = '';
  });

  function showError(msg) {
    if (!resultSpan) return;
    resultSpan.textContent = msg;
    resultSpan.className = 'form-text ms-2 text-danger';
    nicknameInput?.focus();
  }
  function resetNicknameMsg() {
    if (!resultSpan) return;
    resultSpan.textContent = '';
    resultSpan.className = 'form-text ms-2';
  }

  // ─── 주소 검색 ───
  document.getElementById('btnSearchAddr')?.addEventListener('click', () => {
    new daum.Postcode({
      oncomplete: data => {
        document.getElementById('zonecode').value = data.zonecode;
        document.getElementById('address').value = data.roadAddress || data.jibunAddress;
        document.getElementById('detailAddress').focus();
      }
    }).open();
  });

  // ─── 이미지 업로드 미리보기 + 용량/확장자 검사 ───
  imageInput?.addEventListener('change', e => {
    const file = e.target.files?.[0];
    if (!file) {
      if (previewImg) previewImg.src = defaultImageSrc;
      if (asideProfileImage) asideProfileImage.src = defaultImageSrc;
      if (imageMsg) imageMsg.textContent = '';
      return;
    }

    const ext = file.name.split('.').pop().toLowerCase();
    if (!allowExt.includes(ext)) {
      if (imageMsg) {
        imageMsg.textContent = '이미지는 jpg · jpeg · png · gif 형식만 가능합니다.';
        imageMsg.className = 'form-text ms-2 text-danger';
      }
      imageInput.value = '';
      if (previewImg) previewImg.src = defaultImageSrc;
      return;
    }

    if (file.size > maxSize) {
      if (imageMsg) {
        imageMsg.textContent = '이미지 용량은 2MB 이하만 가능합니다.';
        imageMsg.className = 'form-text ms-2 text-danger';
      }
      imageInput.value = '';
      if (previewImg) previewImg.src = defaultImageSrc;
      return;
    }

    const reader = new FileReader();
    reader.onload = ev => {
      if (previewImg) previewImg.src = ev.target.result;
      if (asideProfileImage) asideProfileImage.src = ev.target.result;
      if (imageMsg) imageMsg.textContent = '';
    };
    reader.readAsDataURL(file);
  });

  // ─── 최종 폼 유효성 검사 후 제출 ───
  form?.addEventListener('submit', e => {
    const nick = (nicknameInput?.value || '').trim();
    const telValue = (telInput?.value || '').trim();
    const telRegex = /^010-?\d{4}-?\d{4}$/;

    if (!nick) return block(e, '닉네임을 입력해주세요.');
    if (nick.length < 2) return block(e, '닉네임은 2자 이상이어야 합니다.');
    if (nick.length > 30) return block(e, '닉네임은 30자 이하여야 합니다.');
    if (!nicknameRegex.test(nick)) return block(e, '닉네임은 한글·영문·숫자만 사용할 수 있습니다.');
    if (nick !== originalNickname && (!isNicknameChecked || checkedNickname !== nick))
      return block(e, '닉네임 중복 확인을 해주세요.');

    if (telValue && !telRegex.test(telValue)) {
      if (telMsg) {
        telMsg.textContent = '전화번호 형식은 010-0000-0000입니다.';
        telMsg.className = 'form-text ms-2 text-danger';
      }
      return e.preventDefault();
    }

    if ((introTextarea?.value || '').length > 150)
      return block(e, '자기소개는 최대 150자까지 입력할 수 있습니다.');
  });

  function block(e, msg) {
    alert(msg);
    e.preventDefault();
  }

  // ─── 저장 성공 모달 처리 ───
  const flashMsg = document.querySelector('[name="flashMsg"]');
  const successModal = document.getElementById('successModal');
  const confirmBtn = document.getElementById('confirmBtn');

  if (flashMsg && flashMsg.value && successModal && confirmBtn) {
    successModal.style.display = 'flex';
    confirmBtn.addEventListener('click', () => {
      const memberId = flashMsg.dataset.memberId;
      window.location.href = `/mypage/buyer/${memberId}`;
    });
  }

  // ─── 이미지 기본값 복원 ───
  const deleteImageBtn = document.getElementById('deleteImageBtn');
  const deleteImageInput = document.getElementById('deleteImage');

  deleteImageBtn?.addEventListener('click', () => {
    if (previewImg) previewImg.src = defaultImageSrc;
    if (asideProfileImage) asideProfileImage.src = defaultImageSrc;
    if (imageInput) imageInput.value = '';
    if (deleteImageInput) deleteImageInput.value = 'true';
    if (imageMsg) {
      imageMsg.textContent = '';
      imageMsg.className = 'form-text ms-2';
    }
  });

  // ─── 플래시 메시지 페이드아웃 ───
  const flashMsgFadeOut = document.querySelector('#flashMsg');
  if (flashMsgFadeOut) {
    setTimeout(() => {
      flashMsgFadeOut.style.transition = 'opacity 1s ease-out';
      flashMsgFadeOut.style.opacity = '0';
      setTimeout(() => flashMsgFadeOut.remove(), 1000);
    }, 3000);
  }

  // ─── 전화번호 유효성 검사 및 실시간 메시지 제거 ───
  telInput?.addEventListener('blur', () => {
    const telValue = (telInput?.value || '').trim();
    const telRegex = /^010-?\d{4}-?\d{4}$/;

    if (telValue && !telRegex.test(telValue)) {
      if (telMsg) {
        telMsg.textContent = '전화번호 형식은 010-0000-0000입니다.';
        telMsg.className = 'form-text ms-2 text-danger';
      }
    } else if (telMsg) {
      telMsg.textContent = '';
      telMsg.className = 'form-text ms-2';
    }
  });

  telInput?.addEventListener('input', () => {
    if (telMsg) {
      telMsg.textContent = '';
      telMsg.className = 'form-text ms-2';
    }
  });
});
