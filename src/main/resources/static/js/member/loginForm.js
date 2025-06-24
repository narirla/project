/*member/loginForm.js*/
document.addEventListener("DOMContentLoaded", () => {
  const pwInput = document.getElementById("password");
  const eyeBtn = document.querySelector(".btn-eye");
  const clearBtn = document.querySelector(".btn-clear");
  const eyeIcon = eyeBtn.querySelector("i");
  const capsWarning = document.getElementById("caps-warning"); // ✅ Caps Lock 경고 요소

  // ✅ 기본 상태 설정
  eyeIcon.classList.remove("fa-eye");
  eyeIcon.classList.add("fa-eye-slash");
  clearBtn.style.display = "none";
  if (capsWarning) capsWarning.style.display = "none";

  // 👁 비밀번호 보기 토글 버튼
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

  // ❌ 입력 삭제 버튼
  clearBtn.addEventListener("click", () => {
    pwInput.value = "";
    pwInput.focus();
    clearBtn.style.display = "none";
    eyeBtn.classList.remove("shift-left"); // 👁 아이콘 위치 원복
    if (capsWarning) capsWarning.style.display = "none";
  });

  // ✅ 입력 시 X 버튼 보이기 + 👁 위치 이동 처리
  pwInput.addEventListener("input", () => {
    if (pwInput.value) {
      clearBtn.style.display = "block";
      eyeBtn.classList.add("shift-left");   // 👁 왼쪽 이동
    } else {
      clearBtn.style.display = "none";
      eyeBtn.classList.remove("shift-left"); // 👁 오른쪽 복귀
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
});

