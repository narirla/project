/*createSellerPage.js*/
document.addEventListener('DOMContentLoaded', function () {
  const nicknameInput = document.getElementById('nickname');
  const checkBtn = document.getElementById('nicknameToggleBtn');
  const msg = document.getElementById('nicknameCheckMsg');
  const form = document.getElementById('sellerCreateForm');
  const introTextarea = document.getElementById('intro');
  const charCountNum = document.getElementById('charCountNum');
  const imageInput = document.getElementById('imageFile');
  const previewImg = document.getElementById('profilePreview');
  const removeBtn = document.getElementById('removeImageBtn');
  const placeholderText = document.getElementById('placeholderText');

  const maxLength = 150;
  let isNicknameAvailable = false;

  // 닉네임 입력 시 상태 초기화
  if (nicknameInput && msg) {
    nicknameInput.addEventListener('input', () => {
      isNicknameAvailable = false;
      msg.textContent = '';
      msg.classList.remove('success', 'error');
    });
  }

  // 닉네임 중복 확인
  if (nicknameInput && checkBtn && msg) {
    checkBtn.addEventListener('click', () => {
      const nickname = nicknameInput.value.trim();
      if (!nickname) {
        msg.textContent = '닉네임을 입력해주세요.';
        msg.classList.remove('success', 'error');
        msg.classList.add('form-text', 'error');
        nicknameInput.focus();
        isNicknameAvailable = false;
        return;
      }

      fetch(`/mypage/seller/nickname-check?nickname=${encodeURIComponent(nickname)}`)
        .then(res => res.json())
        .then(data => {
          msg.classList.remove('success', 'error');
          if (data.available) {
            msg.textContent = '사용 가능한 닉네임입니다.';
            msg.classList.add('form-text', 'success');
            isNicknameAvailable = true;
          } else {
            msg.textContent = '이미 사용 중인 닉네임입니다.';
            msg.classList.add('form-text', 'error');
            nicknameInput.focus();
            isNicknameAvailable = false;
          }
        })
        .catch(() => {
          msg.textContent = '중복 확인 중 오류가 발생했습니다.';
          msg.classList.remove('success', 'error');
          msg.classList.add('form-text', 'error');
          isNicknameAvailable = false;
        });
    });
  }

  // form 제출 시 닉네임 확인 검사
  if (form) {
    form.addEventListener('submit', function (e) {
      if (!isNicknameAvailable) {
        e.preventDefault();
        msg.textContent = '닉네임 중복 확인을 먼저 해주세요.';
        msg.classList.remove('success', 'error');
        msg.classList.add('form-text', 'error');
        nicknameInput.focus();
      }
    });
  }

  // 소개글 글자 수 실시간 표시
  if (introTextarea && charCountNum) {
    introTextarea.addEventListener('input', function () {
      const currentLength = introTextarea.value.length;
      charCountNum.textContent = currentLength;
      charCountNum.style.color = currentLength > maxLength ? 'red' : '';
    });
  }

  // 이미지 업로드 처리
  if (imageInput && previewImg && removeBtn && placeholderText) {
    imageInput.addEventListener('change', function () {
      const file = imageInput.files[0];
      if (!file) return;

      const maxSize = 2 * 1024 * 1024;
      const allowedExtensions = ['jpg', 'jpeg', 'png', 'gif'];
      const fileName = file.name.toLowerCase();
      const fileExt = fileName.substring(fileName.lastIndexOf('.') + 1);
      const isValidExt = allowedExtensions.includes(fileExt);
      const isValidSize = file.size <= maxSize;

      if (!isValidExt) {
        alert('jpg, jpeg, png, gif 형식의 이미지 파일만 업로드할 수 있습니다.');
        resetImage();
        return;
      }

      if (!isValidSize) {
        alert('이미지 파일 크기는 2MB 이하만 허용됩니다.');
        resetImage();
        return;
      }

      const reader = new FileReader();
      reader.onload = function (e) {
        previewImg.src = e.target.result;
        previewImg.style.display = 'block';
        placeholderText.style.display = 'none';
        removeBtn.style.display = 'flex';
      };
      reader.readAsDataURL(file);
    });

    // X 버튼 클릭 시 이미지 제거
    removeBtn.addEventListener('click', function () {
      resetImage();
    });

    function resetImage() {
      imageInput.value = '';
      previewImg.src = '';
      previewImg.style.display = 'none';
      placeholderText.style.display = 'block';
      removeBtn.style.display = 'none';
    }
  }
});

