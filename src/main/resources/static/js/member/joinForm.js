/*joinForm.js*/
// ✅ 이메일 중복 확인
async function checkEmail() {
  const email = document.getElementById("email").value.trim();
  const emailCheckResult = document.getElementById("emailCheckResult");

  if (!email) {
    emailCheckResult.textContent = "이메일을 입력해주세요.";
    emailCheckResult.style.color = "red";
    return;
  }

  try {
    const response = await fetch(`/members/emailCheck?email=${encodeURIComponent(email)}`);
    const result = await response.json();

    if (result === true) {
      emailCheckResult.textContent = "이미 사용 중인 이메일입니다.";
      emailCheckResult.style.color = "red";
    } else {
      emailCheckResult.textContent = "";
      showToast("사용 가능한 이메일입니다."); // ✅ 토스트 메시지 출력
    }
  } catch (error) {
    console.error("이메일 중복 확인 실패:", error);
  }
}

// ✅ 토스트 메시지 출력 함수
function showToast(message) {
  const toast = document.getElementById("toast");
  toast.textContent = message;
  toast.style.display = "block";

  // 애니메이션 리셋을 위한 강제 리플로우
  toast.classList.remove("toast-message");
  void toast.offsetWidth;
  toast.classList.add("toast-message");

  // 자동 숨김
  setTimeout(() => {
    toast.style.display = "none";
  }, 2500);
}

// ✅ 다음 우편번호 API 실행 함수 (주소 연동 시 사용)
function execDaumPostcode() {
  new daum.Postcode({
    oncomplete: function(data) {
      document.getElementById("address").value = data.roadAddress;
    }
  }).open();
}

// ✅ 회원가입 폼 유효성 검사
function validateForm() {
  const requiredFields = [
    { id: "email", name: "이메일" },
    { id: "passwd", name: "비밀번호" },
    { id: "confirmPasswd", name: "비밀번호 확인" },
    { id: "nickname", name: "닉네임" },
    { id: "name", name: "이름" },
    { id: "tel", name: "전화번호" }
  ];

  for (const field of requiredFields) {
    const input = document.getElementById(field.id);
    if (!input || !input.value.trim()) {
      alert(`${field.name}은(는) 필수 입력 항목입니다.`);
      input.focus();
      return false;
    }
  }

  // ✅ 전화번호 형식 검사
  const tel = document.getElementById("tel").value.trim();
  const telPattern = /^01[016789]-\d{3,4}-\d{4}$/;
  if (!telPattern.test(tel)) {
    alert("전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678");
    document.getElementById("tel").focus();
    return false;
  }

  // ✅ 비밀번호 일치 여부 검사
  const pw = document.getElementById("passwd").value.trim();
  const pwCheck = document.getElementById("confirmPasswd").value.trim();
  if (pw !== pwCheck) {
    alert("비밀번호가 일치하지 않습니다.");
    document.getElementById("confirmPasswd").focus();
    return false;
  }

  return true; // 모든 검증 통과
}
