// findIdForm.js
function validateFindForm() {
  const tel = document.getElementById("tel").value.trim();
  const email = document.getElementById("email").value.trim();

  if (!tel && !email) {
    alert("전화번호 또는 이메일 중 하나를 입력하세요.");
    return false;
  }
  return true;
}
