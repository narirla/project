// js/member/resetPwRequestForm.js
function validateEmailForm() {
  const email = document.getElementById("email").value.trim();
  if (!email) {
    alert("이메일을 입력해주세요.");
    return false;
  }
  return true;
}
