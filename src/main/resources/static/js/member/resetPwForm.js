// js/member/resetPwForm.js
function validateResetPwForm() {
  const pw = document.getElementById("newPw").value.trim();
  const pwConfirm = document.getElementById("newPwConfirm").value.trim();

  if (!pw || !pwConfirm) {
    alert("모든 항목을 입력해주세요.");
    return false;
  }

  if (pw !== pwConfirm) {
    alert("비밀번호가 일치하지 않습니다.");
    return false;
  }

  return true;
}
