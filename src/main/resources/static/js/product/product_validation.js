document.addEventListener('DOMContentLoaded', function () {
    const productForm = document.getElementById('productForm');

    // 필수 입력 필드들
    const titleInput = document.querySelector('input[name="title"]');
    const categorySelect = document.querySelector('select[name="category"]');
    const guideYnRadios = document.querySelectorAll('input[name="guideYn"]');
    const normalPriceInput = document.querySelector('input[name="normalPrice"]');
    const salesPriceInput = document.querySelector('input[name="salesPrice"]');
    const guidePriceInput = document.querySelector('input[name="guidePrice"]');
    const salesGuidePriceInput = document.querySelector('input[name="salesGuidePrice"]');
    const totalDayInput = document.querySelector('input[name="totalDay"]');
    const totalTimeInput = document.querySelector('input[name="totalTime"]');
    const reqMoneyInput = document.querySelector('input[name="reqMoney"]');
    const descriptionTextarea = document.querySelector('textarea[name="description"]');
    const detailTextarea = document.querySelector('textarea[name="detail"]');
    const priceDetailTextarea = document.querySelector('textarea[name="priceDetail"]');
    const gpriceDetailTextarea = document.querySelector('textarea[name="gpriceDetail"]');
    const productImageInput = document.querySelector('#productImage');
    const documentFileInput = document.querySelector('input[name="documentFile"]');

    // 선택 입력 필드들
    const transportInfoInput = document.querySelector('input[name="transportInfo"]');
    const stucksInput = document.querySelector('input[name="stucks"]');
    const reqPeopleInput = document.querySelector('input[name="reqPeople"]');
    const targetInput = document.querySelector('input[name="target"]');

    // 상수 정의
    const MAX_PRICE_VALUE = 10000000;
    const MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20MB
    const allowedImageExtensions = ['jpg', 'jpeg', 'png', 'gif'];
    const allowedDocumentExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'csv', 'txt', 'rtf', 'html'];

    // 유효성 검사 메시지 표시/숨김 함수
    function showValidationMessage(element, message) {
        const parent = element.closest('.input-container, .image-upload-container');
        if (!parent) return;

        let messageElement = parent.querySelector('.validation-message');
        if (!messageElement) {
            messageElement = document.createElement('div');
            messageElement.className = 'validation-message';
            parent.appendChild(messageElement);
        }
        messageElement.textContent = message;
        messageElement.style.display = 'block';
    }

    function hideValidationMessage(element) {
        const parent = element.closest('.input-container, .image-upload-container');
        if (!parent) return;

        const messageElement = parent.querySelector('.validation-message');
        if (messageElement) {
            messageElement.style.display = 'none';
        }
    }

    // 글자수 카운트 업데이트 함수
    function updateLengthMessage(inputElement, messageElement, maxLength) {
        if (!inputElement || !messageElement) return;
        const currentLength = inputElement.value.length;
        messageElement.textContent = `${currentLength}/${maxLength}자`;
        messageElement.style.color = currentLength > maxLength ? 'red' : 'gray';

        if (currentLength > maxLength) {
            showValidationMessage(inputElement, `글자수를 ${maxLength}자 이하로 줄여주세요.`);
        } else {
            hideValidationMessage(inputElement);
        }
    }

    // 파일 확장자 검사 함수
    function validateFileExtension(file, allowedExtensions) {
        const fileExtension = file.name.split('.').pop().toLowerCase();
        return allowedExtensions.includes(fileExtension);
    }

    // 실시간 글자수 유효성 검사 이벤트 리스너
    titleInput.addEventListener('input', () => updateLengthMessage(titleInput, titleInput.nextElementSibling, 60));
    transportInfoInput.addEventListener('input', () => updateLengthMessage(transportInfoInput, transportInfoInput.nextElementSibling, 15));
    reqPeopleInput.addEventListener('input', () => updateLengthMessage(reqPeopleInput, reqPeopleInput.nextElementSibling, 15));
    targetInput.addEventListener('input', () => updateLengthMessage(targetInput, targetInput.nextElementSibling, 15));
    stucksInput.addEventListener('input', () => updateLengthMessage(stucksInput, stucksInput.nextElementSibling, 30)); // 30자로 수정
    descriptionTextarea.addEventListener('input', () => updateLengthMessage(descriptionTextarea, descriptionTextarea.nextElementSibling, 500));
    detailTextarea.addEventListener('input', () => updateLengthMessage(detailTextarea, detailTextarea.nextElementSibling, 1000));
    priceDetailTextarea.addEventListener('input', () => updateLengthMessage(priceDetailTextarea, priceDetailTextarea.nextElementSibling, 150));
    gpriceDetailTextarea.addEventListener('input', () => updateLengthMessage(gpriceDetailTextarea, gpriceDetailTextarea.nextElementSibling, 150));

    // 문서 파일 첨부 유효성 검사
    documentFileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        hideValidationMessage(documentFileInput);
        if (file) {
            if (!validateFileExtension(file, allowedDocumentExtensions)) {
                showValidationMessage(documentFileInput, `허용되지 않는 문서 파일 형식입니다: ${file.name}`);
                this.value = '';
            } else if (file.size > MAX_DOCUMENT_SIZE) {
                showValidationMessage(documentFileInput, `문서 파일은 ${MAX_DOCUMENT_SIZE / (1024 * 1024)}MB를 초과할 수 없습니다.`);
                this.value = '';
            }
        }
    });

    // 가이드 동반 여부에 따른 가격 입력 필드 활성화/비활성화
    function toggleGuidePriceFields() {
        const isGuideIncluded = document.querySelector('input[name="guideYn"][value="Y"]').checked;
        const fields = [guidePriceInput, salesGuidePriceInput, gpriceDetailTextarea];

        fields.forEach(field => {
            field.disabled = !isGuideIncluded;
            if (!isGuideIncluded) {
                field.value = '';
                hideValidationMessage(field);
                if(field === gpriceDetailTextarea) {
                    const messageElement = field.closest('.input-container').querySelector('.word-count-message');
                    if(messageElement) messageElement.textContent = `0/150자`;
                }
            }
        });
    }
    guideYnRadios.forEach(radio => radio.addEventListener('change', toggleGuidePriceFields));
    toggleGuidePriceFields();

    // 폼 제출 시 최종 유효성 검사
    productForm.addEventListener('submit', function (event) {
        if (!validateAllFields()) {
            event.preventDefault();
            alert('필수 입력 항목을 확인해주세요.');
        }
    });

    // 모든 필드에 대한 최종 유효성 검사 로직
    function validateAllFields() {
        let allValid = true;
        const validate = (condition, element, message) => {
            if (condition) {
                showValidationMessage(element, message);
                allValid = false;
            } else {
                hideValidationMessage(element);
            }
        };

        // 1. 상품명
        validate(titleInput.value.trim() === '' || titleInput.value.length > 60, titleInput, '상품명을 1~60자 이내로 입력해주세요.');

        // 2. 카테고리
        validate(categorySelect.value === 'select', categorySelect, '카테고리를 선택해주세요.');

        // 3. 가이드 동반 여부
        const isGuideYnSelected = Array.from(guideYnRadios).some(radio => radio.checked);
        validate(!isGuideYnSelected, guideYnRadios[0], '가이드 동반 여부를 선택해주세요.');

        // 4. 가격 정보
        const normalPrice = Number(normalPriceInput.value) || 0;
        const salesPrice = Number(salesPriceInput.value) || 0;
        validate(normalPrice <= 0 || normalPrice > MAX_PRICE_VALUE, normalPriceInput, '올바른 정상 가격을 입력해주세요 (1원 이상, 1천만 원 이하).');
        validate(salesPrice > normalPrice || salesPrice <= 0 || salesPrice > MAX_PRICE_VALUE, salesPriceInput, '판매 가격은 정상 가격보다 작아야 하며, 올바른 값을 입력해주세요.');

        // 5. 가이드 동반 가격
        const isGuideIncluded = document.querySelector('input[name="guideYn"][value="Y"]').checked;
        if (isGuideIncluded) {
            const guidePrice = Number(guidePriceInput.value) || 0;
            const salesGuidePrice = Number(salesGuidePriceInput.value) || 0;
            validate(guidePrice <= 0 || guidePrice > MAX_PRICE_VALUE, guidePriceInput, '가이드 동반 가격을 입력해주세요 (1원 이상, 1천만 원 이하).');
            validate(salesGuidePrice > guidePrice || salesGuidePrice <= 0 || salesGuidePrice > MAX_PRICE_VALUE, salesGuidePriceInput, '가이드 동반 판매 가격은 동반 가격보다 작아야 하며, 올바른 값을 입력해주세요.');
        } else {
            hideValidationMessage(guidePriceInput);
            hideValidationMessage(salesGuidePriceInput);
        }

        // 6. 소요 기간 or 시간
        const totalDay = Number(totalDayInput.value) || 0;
        const totalTime = Number(totalTimeInput.value) || 0;
        validate((totalDay === 0 && totalTime === 0) || totalDay < 0 || totalTime < 0, totalDayInput, '소요 기간 또는 시간을 올바르게 입력해주세요.');

        // 7. 최소 여행 경비
        const reqMoney = Number(reqMoneyInput.value) || 0;
        validate(reqMoney < 0 || reqMoney > MAX_PRICE_VALUE, reqMoneyInput, '올바른 최소 여행 경비를 입력해주세요.');

        // 8. 상품 기본 설명
        validate(descriptionTextarea.value.trim() === '' || descriptionTextarea.value.length > 500, descriptionTextarea, '상품 기본 설명을 1~500자 이내로 입력해주세요.');

        // 9. 상품 상세 설명
        validate(detailTextarea.value.trim() === '' || detailTextarea.value.length > 1000, detailTextarea, '상품 상세 설명을 1~1000자 이내로 입력해주세요.');

        // 10. 가격 정보 상세 기입 (기본)
        validate(priceDetailTextarea.value.trim() === '' || priceDetailTextarea.value.length > 150, priceDetailTextarea, '가격 정보 상세 내용을 1~150자 이내로 입력해주세요.');

        // 11. 가격 정보 상세 기입 (가이드 동반)
        if(isGuideIncluded){
            validate(gpriceDetailTextarea.value.trim() === '' || gpriceDetailTextarea.value.length > 150, gpriceDetailTextarea, '가이드 동반 가격 정보 상세 내용을 1~150자 이내로 입력해주세요.');
        } else {
            hideValidationMessage(gpriceDetailTextarea);
        }

        // 12. 상품 이미지 등록
        validate(productImageInput.files.length === 0, productImageInput, '상품 이미지를 1장 이상 등록해주세요.');

        // 13. 판매 파일 첨부
        validate(!documentFileInput.files[0], documentFileInput, '판매 파일을 첨부해주세요.');

        return allValid;
    }
});