// /js/editMember.js

document.addEventListener("DOMContentLoaded", function () {
  const fileInput = document.getElementById("picFile");
  const previewImg = document.getElementById("preview");

  // 프로필 이미지 미리보기
  if (fileInput && previewImg) {
    fileInput.addEventListener("change", function () {
      const file = fileInput.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
          previewImg.src = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    });
  }

  // 비밀번호 일치 여부 확인
  const passwd = document.getElementById("passwd");
  const confirmPasswd = document.getElementById("confirmPasswd");
  const submitBtn = document.querySelector("form button[type='submit']");

  function checkPasswordsMatch() {
    if (
      passwd.value &&
      confirmPasswd.value &&
      passwd.value === confirmPasswd.value
    ) {
      submitBtn.disabled = false;
    } else {
      submitBtn.disabled = true;
    }
  }

  if (passwd && confirmPasswd && submitBtn) {
    passwd.addEventListener("input", checkPasswordsMatch);
    confirmPasswd.addEventListener("input", checkPasswordsMatch);
  }

  // 주소 검색 함수 (다음 우편번호 API)
  window.execDaumPostcode = function () {
    new daum.Postcode({
      oncomplete: function (data) {
        document.getElementById('zonecode').value = data.zonecode;
        document.getElementById('address').value = data.roadAddress;
        document.getElementById('detailAddress').focus();
      }
    }).open();
  };
});
