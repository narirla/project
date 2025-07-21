document.addEventListener('DOMContentLoaded', function () {
  const uploadBox = document.querySelector('.upload-box');
  const input = document.querySelector('#productImage');   // id로 직접 선택하여 명확성 증가
  const preview = uploadBox.querySelector('.image-preview');
  const icon = uploadBox.querySelector('i');
  const text = uploadBox.querySelector('p');
  const nameList = document.getElementById('imageNameList');

  const maxCount = 10;
  let filesArr = [];

  uploadBox.addEventListener('click', function (e) {
    if(e.target === input) return;
    input.click();
  });

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
    renderNameList();

    if (filesArr.length > 0) {
        showPreview(filesArr[0]);
        renderNameList(filesArr[0]); // 활성화 파일명 표시
      } else {
        hidePreview();
        renderNameList(null);
      }
  });

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
      renderNameList();

      if (filesArr.length > 0) {
          showPreview(filesArr[0]);
          renderNameList(filesArr[0]); // 활성화 파일명 표시
        } else {
          hidePreview();
          renderNameList(null);
        }
    }
  });

  function renderNameList(activeFile) {
    nameList.innerHTML = '';
    filesArr.forEach((file, idx) => {
      const item = document.createElement('div');
      item.className = 'image-name-item';
      item.textContent = file.name;

      // 현재 활성화된 파일에 active 클래스 추가
      if (activeFile && activeFile.name === file.name && activeFile.size === file.size) {
        item.classList.add('active');
      }

      item.addEventListener('click', (e) => {
        e.stopPropagation();
        showPreview(file);
        // 미리보기 이미지 불러올 때 기본으로 띄워지는 이미지 파일명에 활성화 내용 적용하기
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

        // 이미지 삭제시 활성화된 이미지 갱신
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

  function hidePreview() {
    preview.style.display = 'none';
    preview.src = '';
    icon.style.display = 'block';
    text.style.display = 'block';
  }

  function updateInputFiles() {
    const dt = new DataTransfer();
    filesArr.forEach(f => dt.items.add(f));
    input.files = dt.files;
  }

  initializeFormValidation();

  function initializeFormValidation() {
    const form = document.getElementById('productForm');
    if (!form) return;
    form.addEventListener('submit', function (e) {
      if (!validateForm()) e.preventDefault();
    });
  }

  function validateForm() {
    // TODO: 필요한 검증 로직 추가
    return true;
  }
});