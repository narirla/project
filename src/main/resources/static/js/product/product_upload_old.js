// 상품 업로드 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function () {
  initializeFormValidation();
  const uploadBox = document.querySelector('.image-upload-single'); // 기존 업로드 박스
  const input = uploadBox.querySelector('input[type="file"]');
  const preview = uploadBox.querySelector('.image-preview');
  const icon = uploadBox.querySelector('i');
  const text = uploadBox.querySelector('p');
  const nameList = document.getElementById('imageNameList');
  const maxCount = 10;

  let filesArr = [];

  // 업로드 박스 클릭 시 파일 선택창 열기
  uploadBox.addEventListener('click', function (e) {
    if (e.target === input) return;
    input.click();
  });

  // 파일 선택 시 처리
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
    if (filesArr.length > 0) showPreview(filesArr[0]);
    else hidePreview();
  });

  // 드래그 앤 드롭 지원
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
      if (filesArr.length > 0) showPreview(filesArr[0]);
      else hidePreview();
    }
  });

  // 파일명 리스트 렌더링 (+삭제 버튼 포함)
  function renderNameList() {
    nameList.innerHTML = '';
    filesArr.forEach((file, idx) => {
      const item = document.createElement('div');
      item.className = 'image-name-item';
      item.textContent = file.name;

      const removeBtn = document.createElement('button');
      removeBtn.type = 'button';
      removeBtn.className = 'remove-image';
      removeBtn.innerHTML = '<i class="fas fa-times"></i>';
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

  // 첫 파일 미리보기
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

  // 미리보기 숨기기
  function hidePreview() {
    preview.style.display = 'none';
    preview.src = '';
    icon.style.display = 'block';
    text.style.display = 'block';
  }

  // input.files 동기화 (폼 제출용)
  function updateInputFiles() {
    const dt = new DataTransfer();
    filesArr.forEach(f => dt.items.add(f));
    input.files = dt.files;
  }
});

// 폼 검증 초기화
function initializeFormValidation() {
    const form = document.getElementById('productForm');
    
    form.addEventListener('submit', function(e) {
        if (!validateForm()) {
            e.preventDefault();
        }
    });
}

// 폼 검증
function validateForm() {
    let isValid = true;
    
    // 상품명 검증
    const productName = document.getElementById('productName');
    // 상품명을 적지 않았을때 && 공백만 입력 했을 때 에러 메시지 출력
    if (!productName.value.trim()) {
        showError(productName, '상품명을 입력해주세요.');
        isValid = false;
    } else {
        hideError(productName);
    }
    
    // 가격 검증
    const productPrice = document.getElementById('productPrice');
    // 가격을 입력하지 않아 벨류값이 null값일때, 혹은 입력한 값이 음수일때
    if (!productPrice.value || productPrice.value < 0) {
        showError(productPrice, '올바른 가격을 입력해주세요.');
        isValid = false;
    } else {
        hideError(productPrice);
    }
    
    // 카테고리 검증
    const productCategory = document.getElementById('productCategory');
    // 카테고리 값을 받아오지 못했을 때, 혹은 카테고리를 선택하지 않았을 때
    if (!productCategory || !productCategory.value.trim()) {
        showError(productCategory, '카테고리를 선택해주세요.');
        isValid = false;
    } else {
        hideError(productCategory);
    }
    
    // 여행 기간 검증
    const productTotalDate = document.getElementById('productTotalDate');
    if (!productTotalDate.value || productTotalDate.value == "0") {
        showError(productTotalDate, '여행 일수를 입력해주세요.');
        isValid = false;
    } else {
        hideError(productTotalDate);
    }
    
    // 상품 설명 검증
    const productDescription = document.getElementById('productDescription');
    if (!productDescription.value.trim()) {
        showError(productDescription, '상품 설명을 입력해주세요.');
        isValid = false;
    } else {
        hideError(productDescription);
    }
    
    // 최대 참가 인원 검증
    const maxParticipants = document.getElementById('maxParticipants');
    if (!maxParticipants.value || maxParticipants.value <= 0) {
        showError(maxParticipants, '올바른 최대 참가 인원을 입력해주세요.');
        isValid = false;
    } else {
        hideError(maxParticipants);
    }

    // 필요 금액 검증
    const reqMoney = document.getElementById('reqMoney');
    if (reqMoney && reqMoney.value && reqMoney.value < 0) {
        showError(reqMoney, '인당 소비 예상 금액은 필수입력사항 입니다.');
        isValid = false;
    } else if (reqMoney) {
        hideError(reqMoney);
    }

    // 나이 검증
    const age = document.getElementById('age');
    if (age && age.value && age.value <= 0) {
        showError(age, '올바른 나이를 입력해주세요.');
        isValid = false;
    } else if (age) {
        hideError(age);
    }

    // 대표 이미지 업로드 검증
    const productImage = document.getElementById('productImage');
    if (!productImage.files || productImage.files.length === 0) {
        showError('대표 이미지를 업로드해주세요.');
        isValid = false;
    }

    return isValid;
}

// 에러 메시지 표시
function showError(element, message) {
    const errorDiv = element.parentElement.querySelector('.error-message');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
    element.style.borderColor = '#e74c3c';
}

// 에러 메시지 숨기기
function hideError(element) {
    const errorDiv = element.parentElement.querySelector('.error-message');
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
    element.style.borderColor = '#e0e0e0';
}

// 가격 포맷팅
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}