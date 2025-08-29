// product_update.js (상품 수정 전용)

document.addEventListener('DOMContentLoaded', function () {
  // 기본 설정
  const uploadBox = document.querySelector('.upload-box');
  const input = document.querySelector('#productImage');
  const preview = uploadBox.querySelector('.image-preview');
  const icon = uploadBox.querySelector('i');
  const text = uploadBox.querySelector('p');
  const nameList = document.getElementById('imageNameList');
  const tempSaveBtn = document.getElementById('tempSaveBtn');
  const registerBtn = document.getElementById('registerBtn'); // ⭐ 이 부분을 수정했습니다.
  const productForm = document.getElementById('productForm');
  const documentFileInput = document.querySelector('input[name="documentFile"]');

  const maxCount = 10;
  const MAX_DOCUMENT_SIZE = 20 * 1024 * 1024;
  const allowedDocumentExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'csv', 'txt', 'rtf', 'html'];
  let filesArr = [];

  // 유효성 검사 메시지 표시/숨김 함수 (product_validation.js에서 가져옴)
  const showValidationMessage = window.showValidationMessage;
  const hideValidationMessage = window.hideValidationMessage;
  const validateForm = window.validateForm;

  // 파일 확장자 검사 함수
  function validateFileExtension(file, allowedExtensions) {
      const fileExtension = file.name.split('.').pop().toLowerCase();
      return allowedExtensions.includes(fileExtension);
  }

  // 기존 이미지 초기화
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

      if (activeFile && activeFile.name === file.name && activeFile.size === file.size) {
        item.classList.add('active');
      }

      item.addEventListener('click', (e) => {
        e.stopPropagation();
        showPreview(file);
        renderNameList(file);
      });

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
      const imageId = imageItem.getAttribute('data-image-id');
      imageItem.addEventListener('click', function(e) {
        if (e.target.tagName === 'BUTTON') return;
        e.stopPropagation();
        showPreview(`/product-images/${imageId}/data`);
        document.querySelectorAll('.image-name-item.active').forEach(el => el.classList.remove('active'));
        this.classList.add('active');
      });

      const removeBtn = imageItem.querySelector('.remove-image');
      if (removeBtn) {
        removeBtn.addEventListener('click', function(e) {
          e.stopPropagation();
          const imageId = imageItem.getAttribute('data-image-id');
          if (imageId) {
            addDeleteImageIdToForm(imageId);
          }
          imageItem.remove();
          updatePreviewAfterDelete();
        });
      }
    });
  }

  // 삭제할 이미지 ID를 폼에 추가하는 함수
  function addDeleteImageIdToForm(imageId) {
    const form = document.getElementById('productForm');
    let hiddenInput = form.querySelector('input[name="deleteImageIds"]');
    if (!hiddenInput) {
        hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'deleteImageIds';
        hiddenInput.value = '';
        form.appendChild(hiddenInput);
    }
    const currentIds = hiddenInput.value.split(',').filter(id => id.trim() !== '');
    currentIds.push(imageId);
    hiddenInput.value = currentIds.join(',');
  }

  // 이미지 삭제 후 미리보기 업데이트 함수
  function updatePreviewAfterDelete() {
    const remainingImages = document.querySelectorAll('.existing-image, .new-file-item');
    if (remainingImages.length > 0) {
      const firstImage = remainingImages[0];
      if (firstImage.classList.contains('existing-image')) {
          const firstImageId = firstImage.getAttribute('data-image-id');
          showPreview(`/product-images/${firstImageId}/data`);
      } else {
          const fileIndex = filesArr.findIndex(f => f.name === firstImage.textContent.trim());
          if (fileIndex !== -1) {
              showPreview(filesArr[fileIndex]);
          }
      }
      document.querySelectorAll('.image-name-item.active').forEach(el => el.classList.remove('active'));
      firstImage.classList.add('active');
    } else {
      hidePreview();
    }
  }

  // 문서 파일 첨부 유효성 검사 (클라이언트 측 검사)
  const fileNameDisplay = document.querySelector('.file-name');
  if (documentFileInput && fileNameDisplay) {
    documentFileInput.addEventListener('change', function(e) {
      const file = e.target.files[0];
      if (file) {
          if (!validateFileExtension(file, allowedDocumentExtensions)) {
              showValidationMessage(this, `허용되지 않는 문서 파일 형식입니다.`);
              this.value = '';
          } else if (file.size > MAX_DOCUMENT_SIZE) {
              showValidationMessage(this, `문서 파일은 ${MAX_DOCUMENT_SIZE / (1024 * 1024)}MB를 초과할 수 없습니다.`);
              this.value = '';
          } else {
              hideValidationMessage(this);
              fileNameDisplay.textContent = file.name;
          }
      } else {
          hideValidationMessage(this);
          const hasExistingDocument = document.querySelector('.existing-document');
          if (!hasExistingDocument) {
              fileNameDisplay.textContent = '';
          }
      }
    });
  }

  // 임시저장 버튼 이벤트
  if (tempSaveBtn) {
    tempSaveBtn.addEventListener('click', async function(event) {
        event.preventDefault();

        // 폼 필드 유효성 검사
        const isFormValid = await validateForm(productForm);
        if (!isFormValid) {
            return;
        }

        const hasExistingImages = document.querySelectorAll('.existing-image').length > 0;
        const hasNewImages = filesArr.length > 0;
        if (!hasExistingImages && !hasNewImages) {
            showValidationMessage(input.closest('.image-upload-container'), '상품 이미지를 1장 이상 등록해주세요.');
            return;
        } else {
            hideValidationMessage(input.closest('.image-upload-container'));
        }

        const hasExistingDocument = document.querySelector('.file-name')?.textContent.trim() !== '';
        const hasNewDocument = documentFileInput?.files?.length > 0;
        if (!hasExistingDocument && !hasNewDocument) {
             showValidationMessage(documentFileInput.closest('.file, .input-container'), '판매 파일을 첨부해주세요.');
             return;
        } else {
            hideValidationMessage(documentFileInput.closest('.file, .input-container'));
        }

        // 모든 검사 통과 후 임시 저장 처리
        handleTempSave();
    });
  }

  // 임시저장 버튼 클릭 핸들러
  async function handleTempSave() {
    const formData = new FormData(productForm);
    formData.append('status', '임시저장');

    filesArr.forEach(file => {
      formData.append('uploadImages', file);
    });

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

  // 폼 제출 이벤트
  if (registerBtn) {
      registerBtn.addEventListener('click', async function(event) {
          event.preventDefault();

          const isFormValid = await validateForm(productForm);
          if (!isFormValid) {
              return;
          }

          const hasExistingImages = document.querySelectorAll('.existing-image').length > 0;
          const hasNewImages = filesArr.length > 0;
          if (!hasExistingImages && !hasNewImages) {
              showValidationMessage(input.closest('.image-upload-container'), '상품 이미지를 1장 이상 등록해주세요.');
              alert('상품 이미지를 1장 이상 등록해주세요.');
              return;
          } else {
              hideValidationMessage(input.closest('.image-upload-container'));
          }

          const hasExistingDocument = document.querySelector('.file-name')?.textContent.trim() !== '';
          const hasNewDocument = documentFileInput?.files?.length > 0;
          if (!hasExistingDocument && !hasNewDocument) {
               showValidationMessage(documentFileInput.closest('.file, .input-container'), '판매 파일을 첨부해주세요.');
               alert('판매 파일을 첨부해주세요.');
               return;
          } else {
              hideValidationMessage(documentFileInput.closest('.file, .input-container'));
          }

          // ⭐ 수정된 부분: FormData를 생성하고 status 값을 설정, 파일들을 추가
          const formData = new FormData(productForm);
          formData.set('status', '판매중');
          filesArr.forEach(file => {
              formData.append('uploadImages', file);
          });
          const documentFile = document.querySelector('input[name="documentFile"]').files[0];
          if (documentFile) {
              formData.append('documentFile', documentFile);
          }
          const deleteImageIds = document.getElementById('deleteImageIds').value;
          if (deleteImageIds) {
              formData.append('deleteImageIds', deleteImageIds);
          }


          const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
          try {
              const response = await fetch(productForm.action, {
                  method: 'POST',
                  headers: {
                      'X-CSRF-TOKEN': csrfToken
                  },
                  body: formData
              });

              if (response.ok) {
                  // 성공적으로 수정되었을 경우
                  alert('상품 정보가 성공적으로 수정되었습니다.');
                  window.location.href = '/product/manage?status=판매중';
              } else {
                  // 서버에서 에러 응답을 보낼 경우
                  const result = await response.json();
                  if (result.error) {
                      alert(result.error);
                  } else {
                      alert('상품 수정에 실패했습니다. 다시 시도해주세요.');
                  }
              }
          } catch (error) {
              console.error('상품 수정 중 오류 발생:', error);
              alert('상품 수정 중 오류가 발생했습니다. 다시 시도해주세요.');
          }
      });
  }
});