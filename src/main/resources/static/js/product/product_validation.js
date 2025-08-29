// product_validation.js (최종 수정된 전체 코드)

document.addEventListener('DOMContentLoaded', function () {
    const productForm = document.getElementById('productForm');
    const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

    function showValidationMessage(element, message) {
        const validationMessageEl = element.closest('.input-container, .form-group').querySelector('.validation-message');
        if (validationMessageEl) {
            validationMessageEl.textContent = message;
        }
    }

    function hideValidationMessage(element) {
        const validationMessageEl = element.closest('.input-container, .form-group').querySelector('.validation-message');
        if (validationMessageEl) {
            validationMessageEl.textContent = '';
        }
    }

    // ⭐ 기존의 모든 유효성 검사 필드 정의
    const titleInput = document.querySelector('input[name="title"]');
    const categorySelect = document.querySelector('select[name="category"]');
    const normalPriceInput = document.querySelector('input[name="normalPrice"]');
    const salesPriceInput = document.querySelector('input[name="salesPrice"]');
    const guideYnRadios = document.querySelectorAll('input[name="guideYn"]');
    const guidePriceInput = document.querySelector('input[name="guidePrice"]');
    const totalDayInput = document.querySelector('input[name="totalDay"]');
    const totalTimeInput = document.querySelector('input[name="totalTime"]');
    const reqMoneyInput = document.querySelector('input[name="reqMoney"]');
    const descriptionTextarea = document.querySelector('textarea[name="description"]');
    const detailTextarea = document.querySelector('textarea[name="detail"]');
    const priceDetailTextarea = document.querySelector('textarea[name="priceDetail"]');
    const gpriceDetailTextarea = document.querySelector('textarea[name="gpriceDetail"]');
    const reqPeopleInput = document.querySelector('input[name="reqPeople"]');
    const guidePriceBox = document.querySelector('.price-info-guided');

    // 개별 필드 유효성 검사 함수
    function validateField(field) {
        const value = field.value.trim();
        const fieldName = field.name;

        // 필수 필드 검사
        if (field.required && !value) {
            showValidationMessage(field, '필수 입력 항목입니다.');
            return false;
        }

        // 특정 필드에 대한 추가 유효성 검사
        if (fieldName === 'title' && value.length > 50) {
            showValidationMessage(field, '상품명은 50자 이내로 입력해주세요.');
            return false;
        }

        // 가격 검증
        if ((fieldName === 'normalPrice' || fieldName === 'salesPrice' || fieldName === 'guidePrice') && value) {
            if (isNaN(value) || parseInt(value, 10) <= 0) {
                showValidationMessage(field, '유효한 가격을 입력해주세요.');
                return false;
            }
        }

        // 가이드 여부 라디오 버튼 검사
        if (fieldName === 'guideYn') {
          const isGuideSelected = Array.from(guideYnRadios).some(radio => radio.checked);
          if (!isGuideSelected) {
              // 라디오 버튼 그룹 전체에 대한 메시지 표시
              showValidationMessage(guideYnRadios[0], '필수 선택 항목입니다.');
              return false;
          }
        }

        // 모든 검사 통과 시 메시지 숨기기
        hideValidationMessage(field);
        return true;
    }

    // 각 필드에 focusout 이벤트 리스너 추가 (실시간 유효성 검사)
    const fields = [
        titleInput, categorySelect, normalPriceInput, salesPriceInput, totalDayInput, totalTimeInput,
        reqMoneyInput, descriptionTextarea, detailTextarea, priceDetailTextarea, gpriceDetailTextarea,
        reqPeopleInput, guidePriceInput
    ].filter(el => el); // null인 요소 제거

    fields.forEach(field => {
        field.addEventListener('focusout', () => {
            validateField(field);
        });
    });

    // 라디오 버튼에도 이벤트 리스너 추가
    if (guideYnRadios.length > 0) {
        guideYnRadios.forEach(radio => {
            radio.addEventListener('change', () => {
                validateField(radio);
                // 가이드 동반에 따른 가격 입력 필드 가시성 제어
                const guideYesRadio = document.querySelector('input[name="guideYn"][value="yes"]');
                if (guideYesRadio.checked) {
                    guidePriceBox.classList.remove('hidden');
                } else {
                    guidePriceBox.classList.add('hidden');
                }
            });
        });
    }

    // ⭐ 최종 등록/수정 버튼 클릭 시 모든 유효성 검사 수행
    const registerBtn = document.getElementById('registerBtn');
    if (registerBtn) {
        registerBtn.addEventListener('click', async function(event) {
            event.preventDefault(); // 기본 폼 제출 동작 방지

            let isFormValid = true;

            // 1. 클라이언트 측 개별 필드 유효성 검사
            fields.forEach(field => {
                if (!validateField(field)) {
                    isFormValid = false;
                }
            });

            // 2. 파일 필드 유효성 검사 (상품 등록/수정 페이지에 따라 달라짐)
            const productImageInput = document.querySelector('#productImage');
            const hasExistingImages = document.querySelectorAll('.existing-image').length > 0;
            const isUpdatePage = document.querySelector('.product-registration h2')?.textContent.trim() === '상품 수정';

            if (!isUpdatePage && productImageInput.files.length === 0) {
                // 등록 페이지이고 이미지가 없을 경우
                showValidationMessage(productImageInput, '이미지는 최소 1개 이상 업로드해야 합니다.');
                isFormValid = false;
            } else if (isUpdatePage && productImageInput.files.length === 0 && !hasExistingImages) {
                // 수정 페이지이고 기존 이미지도 없고 새 이미지도 없을 경우
                showValidationMessage(productImageInput, '이미지는 최소 1개 이상 존재해야 합니다.');
                isFormValid = false;
            } else {
                hideValidationMessage(productImageInput);
            }

            if (!isFormValid) {
                alert('입력된 항목을 다시 확인해주세요. 화면에 표시된 오류 메시지를 수정해야 합니다.');
                return;
            }

            // 3. 서버 측 유효성 검사 (API 호출)
            const validationFormData = new FormData(productForm);

            try {
                const response = await fetch('/api/product/validate', {
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: validationFormData
                });

                const result = await response.json();

                if (result.errors && Object.keys(result.errors).length > 0) {
                    document.querySelectorAll('.validation-message').forEach(el => el.textContent = '');

                    let errorMessage = "필수 입력 항목을 확인해주세요.\n\n";
                    const errorMessages = [];

                    for (const fieldName in result.errors) {
                        const message = result.errors[fieldName];
                        errorMessages.push(`- ${message}`);

                        const fieldElement = document.querySelector(`[name="${fieldName}"]`);
                        if (fieldElement) {
                            showValidationMessage(fieldElement, message);
                        }
                    }
                    alert(errorMessage + errorMessages.join('\n'));
                } else {
                    // 유효성 검사 통과 시 폼 제출
                    productForm.submit();
                }

            } catch (error) {
                console.error('유효성 검사 오류:', error);
                alert('유효성 검사 중 오류가 발생했습니다. 다시 시도해주세요.');
            }
        });
    }

    // 페이지 로드 시 서버에서 받은 에러 메시지를 alert으로 표시 (Thymeleaf 연동)
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