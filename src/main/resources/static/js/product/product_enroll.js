// product_enroll.js (상품 등록 전용)

document.addEventListener('DOMContentLoaded', function () {
    const uploadBox = document.querySelector('.upload-box');
    const input = document.querySelector('#productImage');
    const preview = uploadBox.querySelector('.image-preview');
    const icon = uploadBox.querySelector('i');
    const text = uploadBox.querySelector('p');
    const nameList = document.getElementById('imageNameList');
    const documentFileInput = document.querySelector('input[name="documentFile"]');
    const documentFileNameDisplay = document.querySelector('.file-name');
    const tempSaveBtn = document.getElementById('tempSaveBtn');
    const productForm = document.getElementById('productForm');

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

    // 업로드 박스 클릭 시 파일 선택 창 열기
    uploadBox.addEventListener('click', function (e) {
        if(e.target === input || e.target.tagName === 'IMG') return;
        input.click();
    });

    // 파일 선택 시
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
            showPreview(filesArr[0]);
            renderNameList(filesArr[0]);
        } else {
            hidePreview();
            renderNameList(null);
        }
    });

    // 드래그 앤 드롭 기능
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
                showPreview(filesArr[0]);
                renderNameList(filesArr[0]);
            } else {
                hidePreview();
                renderNameList(null);
            }
        }
    });

    // 파일명 목록 렌더링
    function renderNameList(activeFile) {
        nameList.innerHTML = '';
        filesArr.forEach((file, idx) => {
            const item = document.createElement('div');
            item.className = 'image-name-item';
            item.textContent = file.name;

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
                let nextActiveFile = filesArr[0] || null;
                if(activeFile && filesArr.length > 0 && !(activeFile.name === file.name && activeFile.size === file.size)){
                    nextActiveFile = activeFile;
                }
                if (filesArr.length > 0) {
                    showPreview(nextActiveFile);
                    renderNameList(nextActiveFile);
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
    function showPreview(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            preview.src = e.target.result;
            preview.style.display = 'block';
            icon.style.display = 'none';
            text.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }

    // 이미지 미리보기 숨기기
    function hidePreview() {
        preview.style.display = 'none';
        preview.src = '';
        icon.style.display = 'block';
        text.style.display = 'block';
    }

    // 실제 <input> 필드의 파일 목록을 업데이트
    function updateInputFiles() {
        const dt = new DataTransfer();
        filesArr.forEach(f => dt.items.add(f));
        input.files = dt.files;
    }

    // 문서 파일 첨부 유효성 검사 (클라이언트 측 검사)
    if (documentFileInput) {
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
                if (documentFileNameDisplay) {
                    documentFileNameDisplay.textContent = file.name;
                }
            }
        } else {
            hideValidationMessage(this);
            if (documentFileNameDisplay) {
                documentFileNameDisplay.textContent = '';
            }
        }
      });
    }

    // 임시저장 버튼 이벤트 리스너 추가
    if (tempSaveBtn) {
        tempSaveBtn.addEventListener('click', async function(event) {
            event.preventDefault();

            const hasNewImages = filesArr.length > 0;
            const hasNewDocument = documentFileInput?.files?.length > 0;

            if (!hasNewImages) {
                showValidationMessage(input.closest('.image-upload-container'), '상품 이미지를 1장 이상 등록해주세요.');
                return;
            } else {
                hideValidationMessage(input.closest('.image-upload-container'));
            }

            if (!hasNewDocument) {
                 showValidationMessage(documentFileInput.closest('.file, .input-container'), '판매 파일을 첨부해주세요.');
                 return;
            } else {
                hideValidationMessage(documentFileInput.closest('.file, .input-container'));
            }

            // validateForm 함수를 호출하여 폼 필드 유효성 검사
            const isFormValid = await validateForm(productForm);
            if (!isFormValid) {
                return;
            }

            // 모든 검사 통과 후 임시 저장 처리
            handleTempSave();
        });
    }

    // 폼 제출 이벤트 리스너: 최종 검증 및 폼 제출
    if (productForm) {
      productForm.addEventListener('submit', async function(event) {
          event.preventDefault();

          const hasNewImages = filesArr.length > 0;
          const hasNewDocument = documentFileInput?.files?.length > 0;

          if (!hasNewImages) {
              showValidationMessage(input.closest('.image-upload-container'), '상품 이미지를 1장 이상 등록해주세요.');
              alert('상품 이미지를 1장 이상 등록해주세요.');
              return;
          } else {
              hideValidationMessage(input.closest('.image-upload-container'));
          }
          if (!hasNewDocument) {
               showValidationMessage(documentFileInput.closest('.file, .input-container'), '판매 파일을 첨부해주세요.');
               alert('판매 파일을 첨부해주세요.');
               return;
          } else {
              hideValidationMessage(documentFileInput.closest('.file, .input-container'));
          }

          const isFormValid = await validateForm(productForm);
          if (isFormValid) {
              productForm.submit();
          }
      });
    }

    // 임시저장 버튼 클릭 핸들러
    async function handleTempSave() {
        const formData = new FormData(productForm);
        formData.append('status', '임시저장');

        try {
            const response = await fetch('/product/temp-save', {
                method: 'POST',
                body: formData
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
});