/*editSellerPage.js*/
document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form');
  const nicknameInput = document.querySelector('#nickname');
  const nicknameBtn = document.querySelector('#nicknameToggleBtn');
  const resultSpan = document.querySelector('#nicknameCheckMsg');
  const introTextarea = document.querySelector('#intro');
  const imageInput = document.querySelector('#imageFile');
  const previewImg = document.querySelector('#sectionProfilePreview');
  const asideProfileImage = document.querySelector('#asideProfileImage');
  const imageMsg = document.querySelector('#imageMsg');
  const saveBtn = document.querySelector('#saveBtn');
  const telInput = document.querySelector('#tel');
  const telMsg = document.querySelector('#telMsg');

  const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
  const allowExt = ['jpg', 'jpeg', 'png', 'gif'];
  const maxSize = 2 * 1024 * 1024;
  const defaultImageSrc = '/img/default-profile.png';

  let isNicknameChecked = false;
  let checkedNickname = '';
  const originalNickname = (nicknameInput?.value || '').trim();
  let isEditing = false;

  // ─── 닉네임 중복 확인(토글 방식) ───
  nicknameBtn?.addEventListener('click', async () => {
    const current = (nicknameInput?.value || '').trim();

    if (!isEditing) {
      if (!nicknameInput) return;
      nicknameInput.readOnly = false;
      nicknameInput.focus();
      nicknameBtn.textContent = '중복 확인';
      if (resultSpan) {
        resultSpan.textContent = '';
        resultSpan.className = 'form-text ms-2';
      }
      isEditing = true;
      isNicknameChecked = false;
      checkedNickname = '';
      return;
    }

    // 중복 확인 단계
    if (!current) return showError('닉네임을 입력해주세요.');
    if (current.length < 2) return showError('닉네임은 2자 이상이어야 합니다.');
    if (current.length > 30) return showError('닉네임은 30자 이하여야 합니다.');
    if (!nicknameRegex.test(current)) return showError('닉네임은 한글·영문·숫자만 사용 가능합니다.');

    try {
      const res = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(current)}`);
      const duplicated = await res.json();
      if (duplicated) {
        isNicknameChecked = false;
        checkedNickname = '';
        showError('이미 사용 중인 닉네임입니다.');
      } else {
        if (resultSpan) {
          resultSpan.textContent = '사용 가능한 닉네임입니다.';
          resultSpan.className = 'form-text ms-2 text-success';
        }
        isNicknameChecked = true;
        checkedNickname = current;
        if (nicknameInput) nicknameInput.readOnly = true;
        if (nicknameBtn) nicknameBtn.textContent = '변경하기';
        isEditing = false;
      }
    } catch (err) {
      isNicknameChecked = false;
      checkedNickname = '';
      showError('중복 확인 중 오류 발생');
    }
  });

  nicknameInput?.addEventListener('input', () => {
    isNicknameChecked = false;
    checkedNickname = '';
    if (resultSpan) {
      resultSpan.textContent = '';
      resultSpan.className = 'form-text ms-2';
    }
  });

  function showError(msg) {
    if (!resultSpan) return;
    resultSpan.textContent = msg;
    resultSpan.className = 'form-text ms-2 text-danger';
    nicknameInput?.focus();
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

  // ─── 이미지 업로드 ───
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

  // ─── 전화번호 유효성 검사 ───
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

  // ✅ 전화번호 입력 시 실시간 오류 제거
  telInput?.addEventListener('input', () => {
    if (telMsg) {
      telMsg.textContent = '';
      telMsg.className = 'form-text ms-2';
    }
  });

  // ─── 저장 버튼 클릭 시 유효성 검사 ───
  saveBtn?.addEventListener('click', function (event) {
    const nickname = (nicknameInput?.value || '').trim();
    const telValue = (telInput?.value || '').trim();
    const telRegex = /^010-?\d{4}-?\d{4}$/;

    if (!nickname) return block(event, '닉네임을 입력해주세요.');
    if (nickname.length < 2 || nickname.length > 30) return block(event, '닉네임은 2자 이상 30자 이하입니다.');
    if (!nicknameRegex.test(nickname)) return block(event, '닉네임은 한글, 영문, 숫자만 사용 가능합니다.');
    if (nickname !== originalNickname && (!isNicknameChecked || checkedNickname !== nickname))
      return block(event, '닉네임 중복 확인을 해주세요.');
    if (telValue && !telRegex.test(telValue)) return block(event, '전화번호 형식은 010-0000-0000입니다.');
    if ((introTextarea?.value || '').length > 150) return block(event, '자기소개는 최대 150자까지 입력할 수 있습니다.');

    form?.submit();
  });

  function block(event, msg) {
    alert(msg);
    event.preventDefault();
  }

  // ─── flash 메시지 자동 제거 ───
  const flashMsg = document.querySelector('#flashMsg');
  if (flashMsg) {
    setTimeout(() => {
      flashMsg.style.transition = 'opacity 1s ease-out';
      flashMsg.style.opacity = '0';
      setTimeout(() => flashMsg.remove(), 1000);
    }, 3000);
  }

  // ─── 저장 성공 모달 확인 ───
  const modal = document.querySelector('#successModal');
  const confirmBtn = document.querySelector('#confirmBtn');
  if (modal && confirmBtn) {
    confirmBtn.addEventListener('click', function () {
      window.location.href = '/mypage/seller/view';
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
      imageMsg.textContent = '기본 이미지로 변경됩니다.';
      imageMsg.className = 'form-text ms-2 text-warning';
    }
  });
});
