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
  const pwInput = document.querySelector('#passwd');
  const pwConfirmInput = document.querySelector('#confirmPasswd');
  const pwMatchMsg = document.querySelector('#pwMatchMsg');
  const pwStrengthBox = document.getElementById("pwStrength");
  const pwHintBox = document.getElementById("pwHint");
  const imageMsg = document.getElementById("imageMsg");

  const currentPwInput = document.getElementById('currentPasswd');
  const currentPwMsg = document.getElementById('currentPwMsg'); // 오류 메시지 출력용 span 요소

  const telInput = document.querySelector('#tel');
  const telMsg = document.querySelector('#telMsg');

  // ─── 상수 ───
  const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
  const allowExt = ['jpg', 'jpeg', 'png', 'gif'];
  const maxSize = 2 * 1024 * 1024; // 2MB
  const defaultImageSrc = '/img/default-profile.png';

  // ─── 닉네임 중복 확인 ───
  nicknameBtn?.addEventListener('click', async () => {
    const nickname = nicknameInput.value.trim();

    if (!nickname) return showError('닉네임을 입력해주세요.');
    if (nickname.length < 2) return showError('닉네임은 2자 이상이어야 합니다.');
    if (nickname.length > 30) return showError('닉네임은 30자 이하여야 합니다.');
    if (!nicknameRegex.test(nickname)) return showError('닉네임은 한글·영문·숫자만 사용할 수 있습니다.');

    try {
      const res = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(nickname)}`);
      const duplicated = await res.json();

      if (duplicated) {
        showError('이미 사용 중인 닉네임입니다.');
      } else {
        resultSpan.textContent = '사용 가능한 닉네임입니다.';
        resultSpan.className = 'form-text ms-2 text-success';
      }
    } catch (err) {
      console.error(err);
      showError('중복 확인 중 오류가 발생했습니다.');
    }
  });

  nicknameInput?.addEventListener('input', resetNicknameMsg);

  function showError(msg) {
    resultSpan.textContent = msg;
    resultSpan.className = 'form-text ms-2 text-danger';
    nicknameInput.focus();
  }
  function resetNicknameMsg() {
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

  // ─── 이미지 업로드 미리보기 + 용량 검사 ───
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
      imageInput.value = '';
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

  // ─── 기존 비밀번호 일치 확인 ───
  currentPwInput?.addEventListener('blur', async () => {
    const currentPw = currentPwInput.value.trim();
    if (!currentPw) {
      currentPwMsg.textContent = '현재 비밀번호를 입력해주세요.';
      currentPwMsg.className = 'form-text ms-2 text-danger';
      return;
    }

    try {
      const res = await fetch(`/members/passwordCheck?passwd=${encodeURIComponent(currentPw)}`);
      const result = await res.json();

      if (result) {
        currentPwMsg.textContent = '기존 비밀번호와 일치합니다.';
        currentPwMsg.className = 'form-text ms-2 text-success';
      } else {
        currentPwMsg.textContent = '기존 비밀번호와 일치하지 않습니다.';
        currentPwMsg.className = 'form-text ms-2 text-danger';
      }
    } catch (err) {
      console.error(err);
      currentPwMsg.textContent = '비밀번호 확인 중 오류가 발생했습니다.';
      currentPwMsg.className = 'form-text ms-2 text-danger';
    }
  });



  // ─── 비밀번호 강도 평가 ───
  pwInput?.addEventListener('input', () => {
    evaluatePasswordStrength(pwInput.value);
    checkPasswordMatch(pwInput.value, pwConfirmInput.value);
  });

  function evaluatePasswordStrength(password) {
    if (!pwStrengthBox || !pwHintBox) return;

    const hasDigit = /\d/.test(password);
    const hasAlpha = /[a-zA-Z]/.test(password);
    const hasSymbol = /[^a-zA-Z0-9]/.test(password);
    const hasRepeat = /(.)\1{2,}/.test(password);

    // 기존 클래스 제거
    pwStrengthBox.classList.remove("text-success", "text-warning", "text-danger");

    if (password.length < 8 || hasRepeat) {
      pwStrengthBox.textContent = "약함";
      pwStrengthBox.className = "pw-strength weak text-danger";  // ✅ 빨간색
      pwHintBox.textContent = "비밀번호는 8자 ~ 12자, 대소문자, 숫자, 특수문자를 포함하고 동일 문자를 3회 이상 반복할 수 없습니다.";
      pwHintBox.className = "form-text ms-2 text-danger"; // ✅ 빨간색 안내
      return;
    }

    if (hasDigit && hasAlpha && hasSymbol) {
      pwStrengthBox.textContent = "강함";
      pwStrengthBox.className = "pw-strength strong text-success";  // ✅ 초록색
      pwHintBox.textContent = "";
      pwHintBox.className = "form-text ms-2"; // 초기화
    } else if ((hasDigit && hasAlpha) || (hasAlpha && hasSymbol)) {
      pwStrengthBox.textContent = "보통";
      pwStrengthBox.className = "pw-strength medium text-warning";  // ✅ 노란색
      pwHintBox.textContent = "영문, 숫자, 특수문자를 조합하세요.";
      pwHintBox.className = "form-text ms-2 text-warning";
    } else {
      pwStrengthBox.textContent = "약함";
      pwStrengthBox.className = "pw-strength weak text-danger";
      pwHintBox.textContent = "비밀번호가 너무 단순합니다.";
      pwHintBox.className = "form-text ms-2 text-danger";
    }
  }


  // ─── 비밀번호 일치 확인 ───
  pwConfirmInput?.addEventListener('input', () => {
    checkPasswordMatch(pwInput.value, pwConfirmInput.value);
  });

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

  // ─── 비밀번호 보기 토글 ───
  window.togglePassword = function(id) {
    const pwInput = document.getElementById(id);
    const wrapper = pwInput.parentElement;
    const toggleBtn = wrapper.querySelector('.toggle-password');
    const icon = toggleBtn.querySelector('i');

    if (pwInput.type === "text") {
      pwInput.type = "password";
      icon.classList.remove("fa-eye");
      icon.classList.add("fa-eye-slash");
    } else {
      pwInput.type = "text";
      icon.classList.remove("fa-eye-slash");
      icon.classList.add("fa-eye");
    }
  };

  // ─── 최종 폼 유효성 검사 후 제출 ───
  form?.addEventListener('submit', e => {
    const nick = nicknameInput.value.trim();
    if (!nick) return block('닉네임을 입력해주세요.');
    if (nick.length < 2) return block('닉네임은 2자 이상이어야 합니다.');
    if (nick.length > 30) return block('닉네임은 30자 이하여야 합니다.');
    if (!nicknameRegex.test(nick)) return block('닉네임은 한글·영문·숫자만 사용할 수 있습니다.');
    if (introTextarea.value.length > 150)
      return block('자기소개는 최대 500자까지 입력할 수 있습니다.');
  });

  function block(msg) {
    alert(msg);
    event.preventDefault();
  }

  // 플래시 메시지 확인 후 모달 띄우기
  const flashMsg = document.querySelector('[name="flashMsg"]');
  const successModal = document.getElementById('successModal');
  const confirmBtn = document.getElementById('confirmBtn');

  if (flashMsg && flashMsg.value) {
    successModal.style.display = 'flex';

    confirmBtn.addEventListener('click', () => {
      window.location.href = `/mypage/buyer/${flashMsg.dataset.memberId}`;  // 또는 '/mypage/buyer/view'
    });
  }

  // ─── 기본 이미지로 변경 버튼 동작 ───
  const deleteImageBtn = document.getElementById('deleteImageBtn');
  const deleteImageInput = document.getElementById('deleteImage');

  deleteImageBtn?.addEventListener('click', () => {
    // 미리보기 이미지 기본 이미지로 변경
    previewImg.src = defaultImageSrc;
    if (asideProfileImage) asideProfileImage.src = defaultImageSrc;

    // 기존 업로드 input 초기화
    if (imageInput) imageInput.value = '';

    // 삭제 플래그 true 설정
    deleteImageInput.value = true;

    // 안내 메시지 초기화
    imageMsg.textContent = '';
  });


  const flashMsgFadeOut = document.querySelector('#flashMsg');
  if (flashMsgFadeOut) {
    setTimeout(() => {
      flashMsgFadeOut.style.transition = 'opacity 1s ease-out';
      flashMsgFadeOut.style.opacity = '0';
      setTimeout(() => flashMsgFadeOut.remove(), 1000);
    }, 3000);
  }

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



});
