document.addEventListener('DOMContentLoaded', function () {
  // 업로드 영역, input[type=file], 미리보기 이미지, 아이콘, 텍스트, 파일명 리스트 요소 선택
  const uploadBox = document.querySelector('.image-upload-single');
  const input = uploadBox.querySelector('input[type="file"]');
  const preview = uploadBox.querySelector('.image-preview');
  const icon = uploadBox.querySelector('i');
  const text = uploadBox.querySelector('p');
  const nameList = document.getElementById('imageNameList');

  const maxCount = 10; // 최대 업로드 가능한 이미지 수 제한
  let filesArr = []; // 현재 업로드된 파일 배열

  // 업로드 박스 클릭하면 파일 선택창 열림 (단, input 직접 클릭은 무시)
  uploadBox.addEventListener('click', function (e) {
    if (e.target === input) return;
    input.click();
  });

  // 파일 선택시 처리
  input.addEventListener('change', function (e) {
    const newFiles = Array.from(input.files);
    // 기존에 없는 새 파일만 추가
    filesArr = filesArr.concat(newFiles.filter(newFile =>
      !filesArr.some(f => f.name === newFile.name && f.size === newFile.size)
    ));

    if (filesArr.length > maxCount) {
      alert(`이미지는 최대 ${maxCount}개까지 업로드할 수 있습니다.`);
      filesArr = filesArr.slice(0, maxCount);
    }

    updateInputFiles();
    renderNameList();
    if (filesArr.length > 0) showPreview(filesArr[0]);
    else hidePreview();
  });

  // 드래그 오버 시 스타일 변경 및 기본 동작 방지
  uploadBox.addEventListener('dragover', function (e) {
    e.preventDefault();
    this.style.borderColor = '#3498db';
    this.style.background = '#f0f7ff';
  });

  // 드래그 떠날 때 스타일 원상복귀
  uploadBox.addEventListener('dragleave', function (e) {
    e.preventDefault();
    this.style.borderColor = '#e0e0e0';
    this.style.background = '#f8f9fa';
  });

  // 드랍 시 파일 추가 처리
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
      if (filesArr.length > 0) showPreview(filesArr[0]);
      else hidePreview();
    }
  });

  // 파일명 리스트 렌더링 및 클릭, 삭제 버튼 이벤트 등록
  function renderNameList() {
    nameList.innerHTML = ''; // 초기화
    filesArr.forEach((file, idx) => {
      const item = document.createElement('div');
      item.className = 'image-name-item';
      item.textContent = file.name;
      item.style.cursor = 'pointer';

      // 파일명 클릭 시 해당 이미지 미리보기 표시, 이벤트 버블링 차단
      item.addEventListener('click', (e) => {
        e.stopPropagation();
        showPreview(file);
      });

      const removeBtn = document.createElement('button');
      removeBtn.type = 'button';
      removeBtn.className = 'remove-image';
      removeBtn.innerHTML = '<i class="fas fa-times"></i>';
      removeBtn.style.marginLeft = '10px';

      // 삭제 버튼 클릭시 파일 배열에서 삭제, 이벤트 버블링 차단
      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        filesArr.splice(idx, 1);
        updateInputFiles();
        renderNameList();
        if (filesArr.length > 0) showPreview(filesArr[0]);
        else hidePreview();
      });

      item.appendChild(removeBtn);
      nameList.appendChild(item);
    });
  }

  // File 객체를 이용해 미리보기 이미지 표시
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

  // 미리보기 이미지 숨기고 아이콘, 텍스트 다시 보이게 함
  function hidePreview() {
    preview.style.display = 'none';
    preview.src = '';
    icon.style.display = 'block';
    text.style.display = 'block';
  }

  // 실제 form 제출 시 input[type=file] 에 파일 리스트 동기화
  function updateInputFiles() {
    const dt = new DataTransfer();
    filesArr.forEach(f => dt.items.add(f));
    input.files = dt.files;
  }

  // 폼 검증 초기화 (필요시 확장 가능)
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