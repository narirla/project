
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

  // 임시저장 버튼 이벤트
   const tempSaveBtn = document.getElementById('tempSaveBtn');
    const updateBtn = document.getElementById('updateBtn');

    if (tempSaveBtn) {
      tempSaveBtn.addEventListener('click', function(event) {
        handleTempSave();
      });
    }

    // 임시저장 버튼 클릭 핸들러 함수
    async function handleTempSave() {
      const form = document.getElementById('productForm');
      const formData = new FormData(form);

      // '임시저장' 상태 값 추가
      formData.append('status', '임시저장');

      // ⭐ 기존 로직을 활용하여 삭제할 이미지 ID 리스트를 FormData에 추가
      const deleteImageIds = document.getElementById('deleteImageIds').value;
      if (deleteImageIds) {
        formData.append('deleteImageIds', deleteImageIds);
      }

      // ⭐ 기존 로직을 활용하여 업로드할 새 이미지 파일을 FormData에 추가
      // filesArr는 현재 JS 코드에서 업로드할 새 이미지를 담는 배열입니다.
      filesArr.forEach(file => {
        formData.append('uploadImages', file); // HTML의 name="uploadImages"와 일치시킴
      });

      // ⭐ 기존 문서 파일 로직에서 선택한 새 문서 파일을 FormData에 추가
      const documentFile = document.querySelector('input[name="documentFile"]').files[0];
      if (documentFile) {
          formData.append('documentFile', documentFile);
      }

      try {
        const response = await fetch('/product/temp-save', {
          method: 'POST',
          body: formData,
        });

        if (response.redirected) {
          window.location.href = response.url;
        } else {
          const result = await response.json();
          if (result.error) {
            alert(result.error);
          } else {
            alert('상품이 임시저장되었습니다.');
            window.location.href = '/product/manage?status=임시저장';
          }
        }

      } catch (error) {
        console.error('임시저장 중 오류 발생:', error);
        alert('임시저장 중 오류가 발생했습니다. 다시 시도해주세요.');
      }
    }

    // ⭐ 기존 수정 버튼 이벤트
    if (updateBtn) {
      updateBtn.addEventListener('click', function(event) {
        // 폼의 기본 제출 동작을 따르므로, 별도 로직 없이 그대로 둡니다.
        // 폼의 action="/product/edit/{id}"와 method="post"가 동작하게 됩니다.
      });
    }
});