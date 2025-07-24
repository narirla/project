
document.addEventListener('DOMContentLoaded', function () {
  // 기본 설정
  const uploadBox = document.querySelector('.upload-box');
  const input = document.querySelector('#productImage');
  const preview = uploadBox.querySelector('.image-preview');
  const icon = uploadBox.querySelector('i');
  const text = uploadBox.querySelector('p');
  const nameList = document.getElementById('imageNameList');
  const maxCount = 10;
  let filesArr = [];

  initializeExistingImages();

  // 업로드 박스 클릭 이벤트
  uploadBox.addEventListener('click', function (e) {
    if(e.target === input) return;
    input.click();
  });

  // 파일 선택 처리
  input.addEventListener('change', function (e) {
    const newFiles = Array.from(input.files);
    filesArr = filesArr.concat(newFiles.filter(newFile =>
      !filesArr.some(f => f.name === newFile.name && f.size === newFile.size)
    ));

    if (filesArr.length > maxCount) {
      alert(`이미지는 최대 ${maxCount}개까지 업로드할 수 있습니다.`);
      filesArr = filesArr.slice(0, maxCount);
    }

    updateInputFiles();

    if (filesArr.length > 0) {
      // 새 파일 업로드 시 모든 active 제거
      document.querySelectorAll('.image-name-item.active').forEach(el => el.classList.remove('active'));
      showPreview(filesArr[0]);
      renderNameList(filesArr[0]);
    } else {
      hidePreview();
      renderNameList(null);
    }
  });

  // 드래그 효과
  uploadBox.addEventListener('dragover', function (e) {
    e.preventDefault();
    this.style.borderColor = '#3498db';
    this.style.background = '#f0f7ff';
  });

  uploadBox.addEventListener('dragleave', function (e) {
    e.preventDefault();
    this.style.borderColor = '#e0e0e0';
    this.style.background = '#f8f9fa';
  });

  // 파일 드롭 처리
  uploadBox.addEventListener('drop', function (e) {
    e.preventDefault();
    this.style.borderColor = '#e0e0e0';
    this.style.background = '#f8f9fa';

    if (e.dataTransfer.files.length > 0) {
      filesArr = filesArr.concat(Array.from(e.dataTransfer.files).filter(f =>
        !filesArr.some(file => file.name === f.name && file.size === f.size)
      ));

      if (filesArr.length > maxCount) {
        alert(`이미지는 최대 ${maxCount}개까지 업로드할 수 있습니다.`);
        filesArr = filesArr.slice(0, maxCount);
      }

      updateInputFiles();

      if (filesArr.length > 0) {
        // 새 파일 드롭 시 모든 active 제거
        document.querySelectorAll('.image-name-item.active').forEach(el => el.classList.remove('active'));
        showPreview(filesArr[0]);
        renderNameList(filesArr[0]);
      } else {
        hidePreview();
        renderNameList(null);
      }
    }
  });

  // 새 파일 목록 렌더링
  function renderNameList(activeFile) {
    const newFileItems = nameList.querySelectorAll('.new-file-item');
    newFileItems.forEach(item => item.remove());

    filesArr.forEach((file, idx) => {
      const item = document.createElement('div');
      item.className = 'image-name-item new-file-item';

      const fileNameSpan = document.createElement('span');
      fileNameSpan.textContent = file.name;
      item.appendChild(fileNameSpan);

      // 현재 미리보기 파일이면 active 추가
      if (activeFile && activeFile.name === file.name && activeFile.size === file.size) {
        item.classList.add('active');
      }

      // 파일 클릭 시 미리보기 변경
      item.addEventListener('click', (e) => {
        e.stopPropagation();
        showPreview(file);
        renderNameList(file);
      });

      // 파일 삭제 버튼
      const removeBtn = document.createElement('button');
      removeBtn.type = 'button';
      removeBtn.className = 'remove-image';
      removeBtn.innerText = 'x';

      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        filesArr.splice(idx, 1);
        updateInputFiles();

        if (filesArr.length > 0){
          if (activeFile && activeFile.name === file.name && activeFile.size === file.size) {
            showPreview(filesArr[0]);
            renderNameList(filesArr[0]);
          } else {
            renderNameList(activeFile);
          }
        } else {
          hidePreview();
          renderNameList(null);
        }
      });

      item.appendChild(removeBtn);
      nameList.appendChild(item);
    });
  }

  // 이미지 미리보기 표시
  function showPreview(source) {
    if (source instanceof File) {
      const reader = new FileReader();
      reader.onload = (e) => {
        preview.src = e.target.result;
        preview.style.display = 'block';
        icon.style.display = 'none';
        text.style.display = 'none';
      };
      reader.readAsDataURL(source);
    } else if (typeof source === 'string') {
      preview.src = source;
      preview.style.display = 'block';
      icon.style.display = 'none';
      text.style.display = 'none';
    }
  }

  // 미리보기 숨기기
  function hidePreview() {
    preview.style.display = 'none';
    preview.src = '';
    icon.style.display = 'block';
    text.style.display = 'block';
  }

  // input 파일 속성 업데이트
  function updateInputFiles() {
    const dt = new DataTransfer();
    filesArr.forEach(f => dt.items.add(f));
    input.files = dt.files;
  }

  // 기존 이미지 초기화
  function initializeExistingImages() {
    const existingImages = document.querySelectorAll('.existing-image');

    if (existingImages.length > 0) {
      const firstImage = existingImages[0];
      const firstImageId = firstImage.getAttribute('data-image-id');
      showPreview(`/product-images/${firstImageId}/data`);
      firstImage.classList.add('active');
    }

    existingImages.forEach((imageItem) => {
      const fileName = imageItem.getAttribute('data-image-name');
      const imageId = imageItem.getAttribute('data-image-id');

      // 이미지 클릭 이벤트
      imageItem.addEventListener('click', function(e) {
        if (e.target.tagName === 'BUTTON') return;
        e.stopPropagation();

        showPreview(`/product-images/${imageId}/data`);

        // 모든 active 제거 후 현재만 활성화
        document.querySelectorAll('.image-name-item.active').forEach(el => el.classList.remove('active'));
        this.classList.add('active');
      });

      // 이미지 삭제 이벤트
      const removeBtn = imageItem.querySelector('.remove-image');
      if (removeBtn) {
        removeBtn.addEventListener('click', function(e) {
          e.stopPropagation();
          imageItem.remove();
        });
      }
    });
  }

  // 문서 파일 업로드
  const documentFileInput = document.querySelector('input[name="documentFile"]');
  let currentBlobUrl = null;

  if (documentFileInput) {
    documentFileInput.addEventListener('change', function(e) {
      const currentFileInfo = document.querySelector('.current-file-info');
      if (e.target.files.length > 0 && currentFileInfo) {
        const file = e.target.files[0];
        const fileName = file.name;
        const fileSize = Math.round(file.size / 1024);

        // 이전 Blob URL 메모리 해제
        if (currentBlobUrl) {
          URL.revokeObjectURL(currentBlobUrl);
        }

        currentBlobUrl = URL.createObjectURL(file);

        const fileLink = currentFileInfo.querySelector('.current-file-link');
        const fileSizeSpan = currentFileInfo.querySelector('.file-size');

        if (fileLink) {
          fileLink.textContent = fileName;
          fileLink.href = currentBlobUrl;
          fileLink.download = fileName;
        }
        if (fileSizeSpan) {
          fileSizeSpan.innerHTML = `(${fileSize} KB)`;
        }
      }
    });
  }
});