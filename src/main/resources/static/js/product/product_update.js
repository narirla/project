document.addEventListener('DOMContentLoaded', function () {
  const uploadBox = document.querySelector('.upload-box');
  const input = document.querySelector('#productImage');
  const preview = uploadBox.querySelector('.image-preview');
  const icon = uploadBox.querySelector('i');
  const text = uploadBox.querySelector('p');
  const nameList = document.getElementById('imageNameList');

  const maxCount = 10;
  let filesArr = [];

  initializeExistingImages();

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
    
    if (filesArr.length > 0) {
      showPreview(filesArr[0]);
      renderNameList(filesArr[0]);
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
      
      if (filesArr.length > 0) {
        showPreview(filesArr[0]);
        renderNameList(filesArr[0]);
      } else {
        hidePreview();
        renderNameList(null);
      }
    }
  });

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

  function initializeExistingImages() {
    const existingImages = document.querySelectorAll('.existing-image');
    
    if (existingImages.length > 0) {
      // 첫 번째 이미지를 자동으로 미리보기로 설정
      const firstImage = existingImages[0];
      const firstImageId = firstImage.getAttribute('data-image-id');
      showPreview(`/product-images/${firstImageId}/data`);
      firstImage.classList.add('active');
    }

    existingImages.forEach((imageItem) => {
      const fileName = imageItem.getAttribute('data-image-name');
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
          // 모달 확인 제거하고 즉시 삭제
          imageItem.remove();
        });
      }
    });
  }

  const documentFileInput = document.querySelector('input[name="documentFile"]');
  let currentBlobUrl = null;
  
  if (documentFileInput) {
    documentFileInput.addEventListener('change', function(e) {
      const currentFileInfo = document.querySelector('.current-file-info');
      if (e.target.files.length > 0 && currentFileInfo) {
        const file = e.target.files[0];
        const fileName = file.name;
        const fileSize = Math.round(file.size / 1024);
        
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