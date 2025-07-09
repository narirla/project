//js/buyerpage/add.js

document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form');
  const nicknameInput = document.querySelector('input[name="nickname"]');
  const introTextarea = document.querySelector('textarea[name="intro"]');
  const imageInput = document.querySelector('input[type="file"]');

  form.addEventListener('submit', function (e) {
    // 닉네임 유효성 검사
    if (!nicknameInput.value.trim()) {
      alert('닉네임을 입력해주세요.');
      nicknameInput.focus();
      e.preventDefault();
      return;
    }

    // 자기소개 글자 수 제한 (예: 300자)
    if (introTextarea.value.length > 300) {
      alert('자기소개는 최대 300자까지 입력할 수 있습니다.');
      introTextarea.focus();
      e.preventDefault();
      return;
    }

    // 이미지 파일 확장자 검사
    if (imageInput.files.length > 0) {
      const file = imageInput.files[0];
      const allowedExtensions = ['jpg', 'jpeg', 'png', 'gif'];
      const fileExtension = file.name.split('.').pop().toLowerCase();

      if (!allowedExtensions.includes(fileExtension)) {
        alert('이미지 파일은 jpg, jpeg, png, gif 형식만 가능합니다.');
        imageInput.value = ''; // 파일 초기화
        e.preventDefault();
        return;
      }

      // 파일 사이즈 제한 (예: 2MB)
      const maxSize = 2 * 1024 * 1024;
      if (file.size > maxSize) {
        alert('이미지 파일 크기는 최대 2MB까지 가능합니다.');
        imageInput.value = '';
        e.preventDefault();
        return;
      }
    }
  });
});
