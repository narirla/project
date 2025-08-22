// product_validation.js

document.addEventListener('DOMContentLoaded', function () {
    const productForm = document.getElementById('productForm');
    const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

    // 필수 입력 필드들
    const titleInput = document.querySelector('input[name="title"]');
    const categorySelect = document.querySelector('select[name="category"]');
    const guideYnRadios = document.querySelectorAll('input[name="guideYn"]');
    const normalPriceInput = document.querySelector('input[name="normalPrice"]');
    const salesPriceInput = document.querySelector('input[name="salesPrice"]');
    const guidePriceInput = document.querySelector('input[name="guidePrice"]');
    const totalDayInput = document.querySelector('input[name="totalDay"]');
    const totalTimeInput = document.querySelector('input[name="totalTime"]');
    const reqMoneyInput = document.querySelector('input[name="reqMoney"]');
    const descriptionTextarea = document.querySelector('textarea[name="description"]');
    const detailTextarea = document.querySelector('textarea[name="detail"]');
    const priceDetailTextarea = document.querySelector('textarea[name="priceDetail"]');
    const gpriceDetailTextarea = document.querySelector('textarea[name="gpriceDetail"]');
    const productImageInput = document.querySelector('#productImage');
    const documentFileInput = document.querySelector('input[name="documentFile"]');
    const reqPeopleInput = document.querySelector('input[name="reqPeople"]');
    const targetInput = document.querySelector('input[name="target"]');
    const transportInfoInput = document.querySelector('input[name="transportInfo"]');
    const stucksInput = document.querySelector('input[name="stucks"]');
    const salesGuidePriceInput = document.querySelector('input[name="salesGuidePrice"]');

    // 검사 대상 필드 목록 (HTML 요소)
    const fieldsToValidate = [
        titleInput, categorySelect, normalPriceInput, salesPriceInput,
        guidePriceInput, totalDayInput, totalTimeInput, salesGuidePriceInput,
        reqMoneyInput, descriptionTextarea, detailTextarea, priceDetailTextarea,
        gpriceDetailTextarea, reqPeopleInput, targetInput, transportInfoInput, stucksInput
    ];

    const MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20MB
    const allowedDocumentExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'csv', 'txt', 'rtf', 'html'];

    // 유효성 검사 메시지 표시/숨김 함수
    function showValidationMessage(element, message) {
        if (!element) return;
        const parent = element.closest('.input-container, .image-upload-container, .input-with-tag, .guide-yn-radios');
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
        if (!element) return;
        const parent = element.closest('.input-container, .image-upload-container, .input-with-tag, .guide-yn-radios');
        if (!parent) return;

        const messageElement = parent.querySelector('.validation-message');
        if (messageElement) {
            messageElement.style.display = 'none';
        }
    }

    // 다른 스크립트에서 접근할 수 있도록 함수를 전역 객체에 노출
    window.showValidationMessage = showValidationMessage;
    window.hideValidationMessage = hideValidationMessage;

    // 글자수 카운트 업데이트 함수
    function updateLengthMessage(inputElement, maxLength) {
        const messageElement = inputElement.closest('.input-container').querySelector('.word-count-message');
        if (!inputElement || !messageElement) return;

        const currentLength = inputElement.value.length;
        messageElement.textContent = `${currentLength}/${maxLength}자`;
        messageElement.style.color = currentLength > maxLength ? 'red' : '#777';
    }

    // 파일 확장자 검사 함수
    function validateFileExtension(file, allowedExtensions) {
        const fileExtension = file.name.split('.').pop().toLowerCase();
        return allowedExtensions.includes(fileExtension);
    }

    // 서버에 유효성 검사 요청을 보내는 범용 함수
    async function sendAjaxValidation(data) {
        const response = await fetch('/api/product/validate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(data)
        });
        return response;
    }

    // 단일 필드에 대한 Ajax 검사 로직 (실시간 검사 목적)
    async function validateField(element) {
        if (!element || element.type === 'file' || element.name === 'category') return true;

        const fieldName = element.name;
        const value = element.value.trim(); // 공백 제거

        const data = { [fieldName]: value };

        // ⭐⭐⭐ 수정된 부분: 클라이언트 측에서 미리 글자수와 공백 검사를 수행합니다. ⭐⭐⭐
        const maxLength = {
            title: 60, transportInfo: 15, stucks: 30, reqPeople: 15, target: 15,
            description: 500, detail: 1000, priceDetail: 150, gpriceDetail: 150
        }[fieldName];

        // 입력 필드가 비어있을 때 에러 메시지를 표시
        if (value.length === 0) {
            showValidationMessage(element, `${element.placeholder}을(를) 입력하세요.`);
            return false;
        }

        // 글자수 초과 에러 메시지를 표시
        if (maxLength && value.length > maxLength) {
            showValidationMessage(element, `글자수를 ${maxLength}자 이하로 줄여주세요.`);
            return false;
        }

        // 클라이언트 측 검사를 통과하면 메시지 숨김
        hideValidationMessage(element);

        // 연관 필드가 있는 경우, 함께 전송
        if (fieldName === 'salesPrice') {
            data.normalPrice = normalPriceInput.value;
        } else if (fieldName === 'salesGuidePrice') {
            data.guidePrice = guidePriceInput.value;
        } else if (fieldName === 'totalDay') {
            data.totalTime = totalTimeInput.value;
        } else if (fieldName === 'totalTime') {
            data.totalDay = totalDayInput.value;
        }

        try {
            const response = await sendAjaxValidation(data);
            const errorData = await response.json();

            if (response.status === 400 && errorData[fieldName]) {
                showValidationMessage(element, errorData[fieldName]);
                return false;
            } else {
                hideValidationMessage(element);
                return true;
            }
        } catch (error) {
            console.error('Ajax 유효성 검사 실패:', error);
            return false;
        }
    }

    // 입력 필드에 blur 이벤트 리스너 추가 (실시간 검사)
    fieldsToValidate.forEach(field => {
        if (field && field.type !== 'file' && field.name !== 'category') {
            field.addEventListener('blur', function() {
                validateField(this);
            });
        }
    });

    // 글자수 카운트 이벤트 리스너
    const wordCountFields = [
        { el: titleInput, max: 60 },
        { el: transportInfoInput, max: 15 },
        { el: stucksInput, max: 30 },
        { el: reqPeopleInput, max: 15 },
        { el: targetInput, max: 15 },
        { el: descriptionTextarea, max: 500 },
        { el: detailTextarea, max: 1000 },
        { el: priceDetailTextarea, max: 150 },
        { el: gpriceDetailTextarea, max: 150 }
    ];
    wordCountFields.forEach(item => {
        if (item.el) {
            item.el.addEventListener('input', () => updateLengthMessage(item.el, item.max));
        }
    });

    // select, radio, file 등 change 이벤트 리스너
    categorySelect.addEventListener('change', () => validateField(categorySelect));
    guideYnRadios.forEach(radio => radio.addEventListener('change', () => {
        const selectedRadio = document.querySelector('input[name="guideYn"]:checked');
        if (selectedRadio) {
            hideValidationMessage(guideYnRadios[0]);
        }
        // 라디오 버튼 선택 시 가이드 가격 필드 활성화/비활성화
        toggleGuidePriceFields();
    }));

    // 문서 파일 첨부 유효성 검사 (클라이언트 측 검사)
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
            }
        } else {
            hideValidationMessage(this); // 파일 선택 안 했을 경우 메시지 숨김
        }
    });

    // ⭐⭐⭐ 수정된 부분: 모든 관련 필드를 함수 내에서 재선언합니다. ⭐⭐⭐
    function toggleGuidePriceFields() {
        const isGuideIncluded = document.querySelector('input[name="guideYn"][value="Y"]').checked;

        const salesGuidePriceInput = document.querySelector('input[name="salesGuidePrice"]');
        const guidePriceInput = document.querySelector('input[name="guidePrice"]');
        const gpriceDetailTextarea = document.querySelector('textarea[name="gpriceDetail"]');

        const fields = [guidePriceInput, salesGuidePriceInput, gpriceDetailTextarea];

        fields.forEach(field => {
            if (!field) return;

            field.disabled = !isGuideIncluded;
            if (!isGuideIncluded) {
                field.value = '';
                hideValidationMessage(field);
                const messageElement = field.closest('.input-container').querySelector('.word-count-message');
                if(messageElement) messageElement.textContent = `0/${field.maxLength || 150}자`;
            }
        });
    }
    toggleGuidePriceFields();

    // 폼 제출 시 모든 필드에 대한 최종 유효성 검사
    productForm.addEventListener('submit', async function (event) {
        event.preventDefault();

        // 1. 모든 클라이언트 측 유효성 검사를 비동기로 동시에 실행합니다.
        const validationPromises = fieldsToValidate.map(field => {
            if (field) {
                return validateField(field);
            }
            return Promise.resolve(true); // 필드가 없는 경우 true 반환
        });

        // 이미지 파일 및 문서 파일 클라이언트 측 검사 추가
        validationPromises.push(new Promise(resolve => {
            const isValid = productImageInput.files.length > 0;
            if (!isValid) {
                showValidationMessage(productImageInput.closest('.image-upload-container'), '상품 이미지를 1장 이상 등록해주세요.');
            } else {
                hideValidationMessage(productImageInput.closest('.image-upload-container'));
            }
            resolve(isValid);
        }));

        validationPromises.push(new Promise(resolve => {
            const isValid = documentFileInput.files.length > 0;
            if (!isValid) {
                showValidationMessage(documentFileInput, '판매 파일을 첨부해주세요.');
            } else {
                hideValidationMessage(documentFileInput);
            }
            resolve(isValid);
        }));

        // 라디오 버튼 검사 추가
        validationPromises.push(new Promise(resolve => {
            const isGuideYnSelected = document.querySelector('input[name="guideYn"]:checked');
            const isValid = !!isGuideYnSelected;
            if (!isValid) {
                showValidationMessage(guideYnRadios[0], '가이드 동반 여부를 선택해주세요.');
            } else {
                hideValidationMessage(guideYnRadios[0]);
            }
            resolve(isValid);
        }));

        // 2. 모든 비동기 검사 결과를 기다림
        const results = await Promise.all(validationPromises);
        const allValid = results.every(result => result === true);

        if (allValid) {
            // 3. 모든 클라이언트 측 검사를 통과하면 폼을 서버로 제출합니다.
            this.submit();
        } else {
            // 4. 클라이언트 측 검사 실패 시 알림창을 띄웁니다.
            alert('필수 입력 항목을 확인해주세요.');
        }
    });

    // 페이지 로드 시 서버에서 받은 에러 메시지를 alert으로 표시
    (function () {
        const serverErrorsDiv = document.getElementById('server-validation-errors');
        if (serverErrorsDiv) {
            let errorText = serverErrorsDiv.textContent.trim();
            if (errorText.startsWith('[') && errorText.endsWith(']')) {
                errorText = errorText.substring(1, errorText.length - 1);
                const errors = errorText.split(',').map(err => err.trim());
                let errorMessage = "필수 입력 항목을 확인해주세요.\n\n";

                errors.forEach(error => {
                    errorMessage += `- ${error}\n`;
                });
                alert(errorMessage);
            } else if (errorText) {
                alert("필수 입력 항목을 확인해주세요.\n\n" + errorText);
            }
        }
    })();
});