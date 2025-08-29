/* createSellerPage.js */
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
  const dropzone = document.querySelector('.profile-preview-box');

  const maxLength = 150;
  let isNicknameAvailable = false;

  // ▼▼ 추가: 드롭 플래그 방식
  let isDrop = false;
  // ▲▲

  // ---------- 닉네임 입력 관련 ----------
  if (nicknameInput && msg) {
    nicknameInput.addEventListener('input', () => {
      isNicknameAvailable = false;
      msg.textContent = '';
      msg.classList.remove('success', 'error');
      nicknameInput.setCustomValidity('');
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
            nicknameInput.setCustomValidity('');
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

  // ---------- 제출 검증 ----------
  if (form && nicknameInput && msg) {
    form.addEventListener('submit', function (e) {
      const nickname = nicknameInput.value.trim();

      if (!nickname) {
        e.preventDefault();
        msg.textContent = '닉네임을 입력해주세요.';
        msg.classList.remove('success');
        msg.classList.add('form-text', 'error');
        nicknameInput.setCustomValidity('닉네임을 입력해주세요.');
        nicknameInput.reportValidity();
        nicknameInput.focus();
        return;
      } else {
        nicknameInput.setCustomValidity('');
      }

      if (!isNicknameAvailable) {
        e.preventDefault();
        msg.textContent = '닉네임 중복 확인을 먼저 해주세요.';
        msg.classList.remove('success');
        msg.classList.add('form-text', 'error');
        nicknameInput.focus();
        return;
      }
    });
  }

  // ---------- 소개글 글자 수 ----------
  if (introTextarea && charCountNum) {
    introTextarea.addEventListener('input', function () {
      const currentLength = introTextarea.value.length;
      charCountNum.textContent = currentLength;
      charCountNum.style.color = currentLength > maxLength ? 'red' : '';
    });
  }

  // ---------- 이미지 업로드/삭제 ----------
  if (imageInput && previewImg && removeBtn && placeholderText) {

    function applyFile(file) {
      if (!file) return;

      const maxSize = 2 * 1024 * 1024; // 2MB
      const allowedExt = ['jpg', 'jpeg', 'png', 'gif'];

      const name = (file.name || '').toLowerCase();
      const ext = name.includes('.') ? name.split('.').pop() : '';
      const isValidExt = allowedExt.includes(ext);
      const isValidSize = file.size <= maxSize;

      if (!isValidExt) { alert('jpg, jpeg, png, gif 형식만 허용됩니다.'); resetImage(); return; }
      if (!isValidSize) { alert('이미지 파일은 2MB 이하만 허용됩니다.'); resetImage(); return; }

      const dt = new DataTransfer();
      dt.items.add(file);
      imageInput.files = dt.files;

      const reader = new FileReader();
      reader.onload = (e) => {
        previewImg.src = e.target.result;
        previewImg.style.display = 'block';
        placeholderText.style.display = 'none';
        removeBtn.style.display = 'flex';
      };
      reader.readAsDataURL(file);
    }

    imageInput.addEventListener('change', function () {
      applyFile(imageInput.files?.[0]);
    });

    removeBtn.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      resetImage();
    });

    function resetImage() {
      imageInput.value = '';
      previewImg.src = '';
      previewImg.style.display = 'none';
      placeholderText.style.display = 'block';
      removeBtn.style.display = 'none';
    }

    // === 드래그 앤 드롭만 처리 ===
    if (dropzone) {
      ['dragenter', 'dragover', 'dragleave'].forEach(evt => {
        dropzone.addEventListener(evt, (e) => {
          e.preventDefault();
          e.stopPropagation();
        });
      });

      dropzone.addEventListener('dragenter', () => dropzone.classList.add('drag-over'));
      dropzone.addEventListener('dragover',  () => dropzone.classList.add('drag-over'));
      dropzone.addEventListener('dragleave', () => dropzone.classList.remove('drag-over'));
      dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        e.stopPropagation();
        dropzone.classList.remove('drag-over');

        const file = e.dataTransfer?.files?.[0];
        if (!file) return;
        applyFile(file);
      });
    }


    // 클립보드 붙여넣기 지원
    document.addEventListener('paste', (e) => {
      const item = [...(e.clipboardData?.items || [])].find(i => i.type.startsWith('image/'));
      if (!item) return;
      const file = item.getAsFile();
      applyFile(file);
    });
  }
});
