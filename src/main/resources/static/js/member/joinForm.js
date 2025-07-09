// ✅ joinForm.js 최종 전체 코드

// 이메일 중복 확인 상태 저장 객체
let emailCheckStatus = {
  checked: false,
  value: ''
};

// ✅ 닉네임 중복 확인 상태 객체
let nicknameCheckStatus = {
  checked: false,
  value: ''
};

// ✅ 이메일 형식 유효성 검사 함수
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// ✅ 이메일 중복 확인 (기존 함수 내부 수정)
async function checkEmail() {
  const email = document.getElementById("email").value.trim();
  const resultBox = document.getElementById("emailCheckResult");

  if (!email) {
    resultBox.textContent = "이메일을 입력해주세요.";
    resultBox.style.color = "red";
    return;
  }

  if (!isValidEmail(email)) {
    resultBox.textContent = "올바른 이메일 형식이 아닙니다.";
    resultBox.style.color = "red";
    return;
  }

  try {
    const response = await fetch(`/members/emailCheck?email=${encodeURIComponent(email)}`);
    const result = await response.json();

    if (result.exists === true) {
      resultBox.textContent = "이미 사용 중인 이메일입니다.";
      resultBox.style.color = "red";
      emailCheckStatus.checked = false;
    } else {
      resultBox.textContent = "사용 가능한 이메일입니다.";
      resultBox.style.color = "green";
      emailCheckStatus.checked = true;
      emailCheckStatus.value = email;
    }
  } catch (error) {
    console.error("이메일 중복 확인 실패:", error);
    resultBox.textContent = "오류가 발생했습니다.";
    resultBox.style.color = "red";
    emailCheckStatus.checked = false;
  }
}


// ✅ 닉네임 중복 확인
async function checkNickname() {
  const nickname = document.getElementById("nickname").value.trim();
  const nicknameCheckResult = document.getElementById("nicknameCheckResult");

  if (!nickname) {
    nicknameCheckResult.textContent = "닉네임을 입력해주세요.";
    nicknameCheckResult.style.color = "red";
    return;
  }

  try {
    const response = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(nickname)}`);
    const exists = await response.json();

    if (exists) {
      nicknameCheckResult.textContent = "이미 사용 중인 닉네임입니다.";
      nicknameCheckResult.style.color = "red";
      nicknameCheckStatus.checked = false;
    } else {
      nicknameCheckResult.textContent = "사용 가능한 닉네임입니다.";
      nicknameCheckResult.style.color = "green";
        nicknameCheckStatus.checked = true;
        nicknameCheckStatus.value = nickname;
    }
  } catch (error) {
    console.error("닉네임 중복 확인 실패:", error);
  }
}

// ✅ 토스트 메시지 출력 함수
function showToast(message) {
  const toast = document.getElementById("toast");
  toast.textContent = message;
  toast.style.display = "block";

  toast.classList.remove("toast-message");
  void toast.offsetWidth;
  toast.classList.add("toast-message");

  setTimeout(() => {
    toast.style.display = "none";
  }, 2500);
}

// ✅ 다음 우편번호 API 실행 함수
function execDaumPostcode() {
  new daum.Postcode({
    oncomplete: function(data) {
      document.getElementById("zonecode").value = data.zonecode;
      document.getElementById("address").value = data.roadAddress;
      document.getElementById("detailAddress").focus();
    }
  }).open();
}

// ✅ 비밀번호 보기 토글
function togglePassword(id) {
  const pwInput = document.getElementById(id);
  const wrapper = pwInput.parentElement;
  const toggleBtn = wrapper.querySelector('.toggle-password');
  const icon = toggleBtn.querySelector('i');

  // 현재 비밀번호가 텍스트 → 보이고 있으므로, 클릭 시 가려야 함
  if (pwInput.type === "text") {
    pwInput.type = "password";
    icon.classList.remove("fa-eye");
    icon.classList.add("fa-eye-slash");
  } else {
    pwInput.type = "text";
    icon.classList.remove("fa-eye-slash");
    icon.classList.add("fa-eye");
  }
}


// ✅ 비밀번호 강도 평가
function evaluatePasswordStrength(password) {
  const strengthBox = document.getElementById("pwStrength");
  const hintBox = document.getElementById("pwHint");

  const hasDigit = /\d/.test(password);
  const hasAlpha = /[a-zA-Z]/.test(password);
  const hasSymbol = /[^a-zA-Z0-9]/.test(password);
  const hasRepeat = /(.)\1{2,}/.test(password); // 같은 문자 3번 이상 반복

  if (password.length < 8 || hasRepeat) {
    strengthBox.textContent = "약함";
    strengthBox.className = "pw-strength weak";
    hintBox.textContent = "비밀번호는 8자 ~ 12자\n,대소문자, 숫자, 특수문자를 포함 \n 동일 문자를 3회 이상 반복할 수 없습니다.";
    return;
  }

  if (hasDigit && hasAlpha && hasSymbol) {
    strengthBox.textContent = "강함";
    strengthBox.className = "pw-strength strong";
    hintBox.textContent = "";
  } else if ((hasDigit && hasAlpha) || (hasAlpha && hasSymbol)) {
    strengthBox.textContent = "보통";
    strengthBox.className = "pw-strength medium";
    hintBox.textContent = "영문, 숫자, 특수문자를 조합하세요.";
  } else {
    strengthBox.textContent = "약함";
    strengthBox.className = "pw-strength weak";
    hintBox.textContent = "비밀번호가 너무 단순합니다.";
  }
}

// ✅ 중복 확인 상태 플래그
let isEmailChecked = false;
let isNicknameChecked = false;

// ✅ 비밀번호 입력 시 실시간 강도 평가
document.addEventListener("DOMContentLoaded", () => {
  const pwInput = document.getElementById("passwd");
  pwInput.addEventListener("input", () => {
    evaluatePasswordStrength(pwInput.value);
  });

  // 닉네임 중복 확인 (blur)
  const nicknameInput = document.getElementById("nickname");
  if (nicknameInput) {
    nicknameInput.addEventListener("blur", checkNickname);
  }

  // 이메일 입력 후 Enter 키로 중복 확인
  const emailInput = document.getElementById("email");
  emailInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      checkEmail();
    }
  });

  // 비밀번호 확인 입력 시 실시간 일치 검사
  const confirmPwInput = document.getElementById("confirmPasswd");
  confirmPwInput.addEventListener("input", () => {
    const pw = document.getElementById("passwd").value.trim();
    const pwCheck = confirmPwInput.value.trim();
    const confirmPwErrorBox = document.getElementById("confirmPwError");

      if (pw && pwCheck && pw !== pwCheck) {
        confirmPwErrorBox.textContent = "비밀번호가 일치하지 않습니다.";
        confirmPwErrorBox.style.color = "red";
      } else if (pw && pwCheck && pw === pwCheck) {
        confirmPwErrorBox.textContent = "비밀번호가 일치합니다.";
        confirmPwErrorBox.style.color = "green";
      } else {
        confirmPwErrorBox.textContent = "";
      }
  });
});

// ✅ 회원가입 폼 유효성 검사 함수
function validateForm() {
  const requiredFields = [
    { id: "email", name: "이메일" },
    { id: "passwd", name: "비밀번호" },
    { id: "confirmPasswd", name: "비밀번호 확인" },
    { id: "nickname", name: "닉네임" },
    { id: "name", name: "이름" },
    { id: "tel", name: "전화번호" },
  ];

  for (const field of requiredFields) {
    const input = document.getElementById(field.id);
    if (!input || !input.value.trim()) {
      alert(`${field.name}은(는) 필수 입력 항목입니다.`);
      input.focus();
      return false;
    }
  }

  // 전화번호 형식 검사
  const tel = document.getElementById("tel").value.trim();
  const telPattern = /^01[016789]-\d{3,4}-\d{4}$/;
  if (!telPattern.test(tel)) {
    alert("전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678");
    document.getElementById("tel").focus();
    return false;
  }

  // 비밀번호 일치 검사
  const pw = document.getElementById("passwd").value.trim();
  const pwCheck = document.getElementById("confirmPasswd").value.trim();
  const confirmPwErrorBox = document.getElementById("confirmPwError");

  if (pw !== pwCheck) {
    confirmPwErrorBox.textContent = "비밀번호가 일치하지 않습니다.";
    confirmPwErrorBox.style.color = "red";
    document.getElementById("confirmPasswd").focus();
    return false;
  } else {
    confirmPwErrorBox.textContent = ""; // 일치하면 메시지 제거
  }

  // 이메일 중복 확인 여부 검사
  const currentEmail = document.getElementById("email").value.trim();
  if (!emailCheckStatus.checked || emailCheckStatus.value !== currentEmail) {
    alert("이메일 중복 확인을 완료해주세요.");
    document.getElementById("email").focus();
    return false;
  }

  // 닉네임 중복 확인 여부 검사
  const currentNickname = document.getElementById("nickname").value.trim();
  if (!nicknameCheckStatus.checked || nicknameCheckStatus.value !== currentNickname) {
    alert("닉네임 중복 확인을 완료해주세요.");
    document.getElementById("nickname").focus();
    return false;
  }

  return true;
}
