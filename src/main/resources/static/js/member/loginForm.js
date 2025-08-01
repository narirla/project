/*member/loginForm.js*/
document.addEventListener("DOMContentLoaded", () => {
  const pwInput = document.getElementById("password");
  const emailInput = document.getElementById("email");  // ✅ 이메일 input
  const eyeBtn = document.querySelector(".btn-eye");
  const pwClearBtn = document.querySelector(".pw-clear"); // ✅ 비밀번호용 X 버튼
  const emailClearBtn = document.querySelector(".email-clear"); // ✅ 이메일용 X 버튼
  const eyeIcon = eyeBtn.querySelector("i");
  const capsWarning = document.getElementById("caps-warning");
  const rememberChk = document.getElementById("remember");

  // ✅ 초기 상태
  eyeIcon.classList.remove("fa-eye");
  eyeIcon.classList.add("fa-eye-slash");
  pwClearBtn.style.display = "none";
  emailClearBtn.style.display = "none";
  if (capsWarning) capsWarning.style.display = "none";

  // 👁 비밀번호 보기 토글
  eyeBtn.addEventListener("click", () => {
    const isHidden = pwInput.type === "password";
    pwInput.type = isHidden ? "text" : "password";

    if (isHidden) {
      eyeIcon.classList.remove("fa-eye-slash");
      eyeIcon.classList.add("fa-eye");
    } else {
      eyeIcon.classList.remove("fa-eye");
      eyeIcon.classList.add("fa-eye-slash");
    }
  });

  // ❌ 비밀번호 X 버튼
  pwClearBtn.addEventListener("click", () => {
    pwInput.value = "";
    pwInput.focus();
    pwClearBtn.style.display = "none";
    eyeBtn.classList.remove("shift-left");
    if (capsWarning) capsWarning.style.display = "none";
  });

  // ✅ 비밀번호 입력 시 X 버튼 표시
  pwInput.addEventListener("input", () => {
    if (pwInput.value) {
      pwClearBtn.style.display = "block";
      eyeBtn.classList.add("shift-left");
    } else {
      pwClearBtn.style.display = "none";
      eyeBtn.classList.remove("shift-left");
    }
  });

  // ✅ 이메일 X 버튼
  emailClearBtn.addEventListener("click", () => {
    emailInput.value = "";
    emailInput.focus();
    emailClearBtn.style.display = "none";
  });

  // ✅ 이메일 입력 시 X 버튼 표시
  emailInput.addEventListener("input", () => {
    if (emailInput.value) {
      emailClearBtn.style.display = "block";
    } else {
      emailClearBtn.style.display = "none";
    }
  });

  // ✅ Caps Lock 감지
  pwInput.addEventListener("keyup", (e) => {
    if (e.getModifierState && e.getModifierState("CapsLock")) {
      if (capsWarning) capsWarning.style.display = "block";
    } else {
      if (capsWarning) capsWarning.style.display = "none";
    }
  });

  // ✅ 이메일 기억하기(localStorage)
  const savedEmail = localStorage.getItem("savedEmail");
  if (savedEmail) {
    emailInput.value = savedEmail;
    rememberChk.checked = true;
    emailClearBtn.style.display = "block"; // 저장된 이메일 있을 때 X 버튼 표시
  }

  const loginForm = document.querySelector("form");
  loginForm.addEventListener("submit", () => {
    if (rememberChk.checked) {
      localStorage.setItem("savedEmail", emailInput.value);
    } else {
      localStorage.removeItem("savedEmail");
    }
  });
});


