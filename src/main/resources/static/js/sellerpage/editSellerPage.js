document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form');
  const nicknameInput = document.querySelector('#nickname');
  const nicknameBtn = document.querySelector('#nicknameToggleBtn');
  const resultSpan = document.querySelector('#nicknameCheckMsg');
  const introTextarea = document.querySelector('#intro');
  const imageInput = document.querySelector('#imageFile');
  const previewImg = document.querySelector('#sectionProfilePreview');
  const asideProfileImage = document.querySelector('#asideProfileImage');
  const passwdInput = document.querySelector('#passwd');
  const confirmPasswdInput = document.querySelector('#confirmPasswd');
  const pwStrengthBox = document.querySelector('#pwStrength');
  const pwHintBox = document.querySelector('#pwHint');
  const pwMatchMsg = document.querySelector('#pwMatchMsg');
  const imageMsg = document.querySelector('#imageMsg');
  const saveBtn = document.querySelector('#saveBtn');
  const currentPasswdInput = document.querySelector('#currentPasswd');
  const currentPwMsg = document.querySelector('#currentPwMsg');
  const telInput = document.querySelector('#tel');
  const telMsg = document.querySelector('#telMsg');

  const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
  const allowExt = ['jpg', 'jpeg', 'png', 'gif'];
  const maxSize = 2 * 1024 * 1024;
  const defaultImageSrc = '/img/default-profile.png';

  let isNicknameChecked = false;
  let checkedNickname = '';
  const originalNickname = nicknameInput.value.trim();
  let isEditing = false;

  // 닉네임 중복 확인
  nicknameBtn?.addEventListener('click', async () => {
    const current = nicknameInput.value.trim();
    if (!isEditing) {
      nicknameInput.readOnly = false;
      nicknameInput.focus();
      nicknameBtn.textContent = '중복 확인';
      resultSpan.textContent = '';
      resultSpan.className = 'form-text ms-2';
      isEditing = true;
      isNicknameChecked = false;
    } else {
      if (!current) return showError('닉네임을 입력해주세요.');
      if (current.length < 2) return showError('닉네임은 2자 이상이어야 합니다.');
      if (current.length > 30) return showError('닉네임은 30자 이하여야 합니다.');
      if (!nicknameRegex.test(current)) return showError('닉네임은 한글·영문·숫자만 사용 가능합니다.');

      try {
        const res = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(current)}`);
        const duplicated = await res.json();
        if (duplicated) {
          showError('이미 사용 중인 닉네임입니다.');
        } else {
          resultSpan.textContent = '사용 가능한 닉네임입니다.';
          resultSpan.className = 'form-text ms-2 text-success';
          isNicknameChecked = true;
          checkedNickname = current;
          nicknameInput.readOnly = true;
          nicknameBtn.textContent = '변경하기';
          isEditing = false;
        }
      } catch (err) {
        showError('중복 확인 중 오류 발생');
      }
    }
  });

  nicknameInput?.addEventListener('input', () => {
    isNicknameChecked = false;
    resultSpan.textContent = '';
    resultSpan.className = 'form-text ms-2';
  });

  function showError(msg) {
    resultSpan.textContent = msg;
    resultSpan.className = 'form-text ms-2 text-danger';
    nicknameInput.focus();
  }

  // 주소 검색
  document.getElementById('btnSearchAddr')?.addEventListener('click', () => {
    new daum.Postcode({
      oncomplete: data => {
        document.getElementById('zonecode').value = data.zonecode;
        document.getElementById('address').value = data.roadAddress || data.jibunAddress;
        document.getElementById('detailAddress').focus();
      }
    }).open();
  });

  // 이미지 업로드
  imageInput?.addEventListener('change', e => {
    const file = e.target.files[0];
    if (!file) {
      previewImg.src = defaultImageSrc;
      if (asideProfileImage) asideProfileImage.src = defaultImageSrc;
      imageMsg.textContent = '';
      return;
    }

    const ext = file.name.split('.').pop().toLowerCase();
    if (!allowExt.includes(ext)) {
      imageMsg.textContent = '이미지는 jpg · jpeg · png · gif 형식만 가능합니다.';
      imageMsg.className = 'form-text ms-2 text-danger';
      imageInput.value = '';
      previewImg.src = defaultImageSrc;
      return;
    }

    if (file.size > maxSize) {
      imageMsg.textContent = '이미지 용량은 2MB 이하만 가능합니다.';
      imageMsg.className = 'form-text ms-2 text-danger';
      imageInput.value = null;
      previewImg.src = defaultImageSrc;
      return;
    }

    const reader = new FileReader();
    reader.onload = ev => {
      previewImg.src = ev.target.result;
      if (asideProfileImage) asideProfileImage.src = ev.target.result;
      imageMsg.textContent = '';
    };
    reader.readAsDataURL(file);
  });

  // 비밀번호 강도 평가 및 일치 검사
  passwdInput?.addEventListener('input', () => {
    evaluatePasswordStrength(passwdInput.value);
    checkPasswordMatch(passwdInput.value, confirmPasswdInput.value);
  });

  confirmPasswdInput?.addEventListener('input', () => {
    checkPasswordMatch(passwdInput.value, confirmPasswdInput.value);
  });

  function evaluatePasswordStrength(password) {
    if (!pwStrengthBox || !pwHintBox) return;
    const hasDigit = /\d/.test(password);
    const hasAlpha = /[a-zA-Z]/.test(password);
    const hasSymbol = /[^a-zA-Z0-9]/.test(password);
    const hasRepeat = /(.)\1{2,}/.test(password);

    if (password.length < 8 || hasRepeat) {
      pwStrengthBox.textContent = "약함";
      pwStrengthBox.className = "pw-strength weak";
      pwHintBox.textContent = "8~12자, 대소문자/숫자/특수문자 포함, 동일 문자 3회 이상 불가";
      return;
    }

    if (hasDigit && hasAlpha && hasSymbol) {
      pwStrengthBox.textContent = "강함";
      pwStrengthBox.className = "pw-strength strong";
      pwHintBox.textContent = "";
    } else if ((hasDigit && hasAlpha) || (hasAlpha && hasSymbol)) {
      pwStrengthBox.textContent = "보통";
      pwStrengthBox.className = "pw-strength medium";
      pwHintBox.textContent = "영문, 숫자, 특수문자를 조합하세요.";
    } else {
      pwStrengthBox.textContent = "약함";
      pwStrengthBox.className = "pw-strength weak";
      pwHintBox.textContent = "비밀번호가 너무 단순합니다.";
    }
  }

  function checkPasswordMatch(password, confirmPassword) {
    if (!password || !confirmPassword) {
      pwMatchMsg.textContent = '';
      pwMatchMsg.className = 'form-text ms-2';
      return;
    }

    if (password === confirmPassword) {
      pwMatchMsg.textContent = '비밀번호가 일치합니다.';
      pwMatchMsg.className = 'form-text ms-2 text-success';
    } else {
      pwMatchMsg.textContent = '비밀번호가 일치하지 않습니다.';
      pwMatchMsg.className = 'form-text ms-2 text-danger';
    }
  }

  // 기존 비밀번호 확인
  currentPasswdInput?.addEventListener('blur', async () => {
    const currentPasswd = currentPasswdInput.value.trim();
    if (!currentPasswd) {
      currentPwMsg.textContent = '';
      currentPwMsg.className = 'form-text ms-2';
      return;
    }

    try {
      const res = await fetch('/members/passwordCheck', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ passwd: currentPasswd })
      });

      const isMatch = await res.json();
      currentPwMsg.textContent = isMatch ? '기존 비밀번호와 일치합니다.' : '기존 비밀번호와 일치하지 않습니다.';
      currentPwMsg.className = isMatch ? 'form-text ms-2 text-success' : 'form-text ms-2 text-danger';
    } catch (err) {
      currentPwMsg.textContent = '비밀번호 확인 중 오류가 발생했습니다.';
      currentPwMsg.className = 'form-text ms-2 text-danger';
    }
  });

  // 전화번호 유효성 검사
  telInput?.addEventListener('blur', () => {
    const telValue = telInput.value.trim();
    const telRegex = /^010-?\d{4}-?\d{4}$/;

    if (telValue && !telRegex.test(telValue)) {
      telMsg.textContent = '전화번호 형식은 010-0000-0000입니다.';
      telMsg.className = 'form-text ms-2 text-danger';
    } else {
      telMsg.textContent = '';
      telMsg.className = 'form-text ms-2';
    }
  });

  // ✅ 전화번호 입력 시 실시간 오류 제거
  telInput?.addEventListener('input', () => {
    telMsg.textContent = '';
    telMsg.className = 'form-text ms-2';
  });

  // 비밀번호 보기 토글
  window.togglePassword = function (id) {
    const pwInput = document.getElementById(id);
    const icon = pwInput.parentElement.querySelector('.toggle-password i');
    if (pwInput.type === "text") {
      pwInput.type = "password";
      icon.classList.replace("fa-eye", "fa-eye-slash");
    } else {
      pwInput.type = "text";
      icon.classList.replace("fa-eye-slash", "fa-eye");
    }
  };

  // 저장 버튼 클릭 시 유효성 검사
  saveBtn?.addEventListener('click', function (event) {
    const nickname = nicknameInput.value.trim();
    const telValue = telInput.value.trim();
    const telRegex = /^010-?\d{4}-?\d{4}$/;

    if (!nickname) return block(event, '닉네임을 입력해주세요.');
    if (nickname.length < 2 || nickname.length > 30) return block(event, '닉네임은 2자 이상 30자 이하입니다.');
    if (!nicknameRegex.test(nickname)) return block(event, '닉네임은 한글, 영문, 숫자만 사용 가능합니다.');
    if (nickname !== originalNickname && (!isNicknameChecked || checkedNickname !== nickname))
      return block(event, '닉네임 중복 확인을 해주세요.');
    if (telValue && !telRegex.test(telValue)) return block(event, '전화번호 형식은 010-0000-0000입니다.');
    if (introTextarea.value.length > 150) return block(event, '자기소개는 최대 150자까지 입력할 수 있습니다.');

    form.submit();
  });

  function block(event, msg) {
    alert(msg);
    event.preventDefault();
  }

  // flash 메시지 자동 제거
  const flashMsg = document.querySelector('#flashMsg');
  if (flashMsg) {
    setTimeout(() => {
      flashMsg.style.transition = 'opacity 1s ease-out';
      flashMsg.style.opacity = '0';
      setTimeout(() => flashMsg.remove(), 1000);
    }, 3000);
  }

  // 저장 성공 모달 확인
  const modal = document.querySelector('#successModal');
  const confirmBtn = document.querySelector('#confirmBtn');
  if (modal && confirmBtn) {
    confirmBtn.addEventListener('click', function () {
      window.location.href = '/mypage/seller/view';
    });
  }

  // 이미지 기본값 복원
  const deleteImageBtn = document.getElementById('deleteImageBtn');
  const deleteImageInput = document.getElementById('deleteImage');
  deleteImageBtn?.addEventListener('click', () => {
    previewImg.src = defaultImageSrc;
    if (asideProfileImage) asideProfileImage.src = defaultImageSrc;
    imageInput.value = null;
    deleteImageInput.value = 'true';
    imageMsg.textContent = '기본 이미지로 변경됩니다.';
    imageMsg.className = 'form-text ms-2 text-warning';
  });

});
