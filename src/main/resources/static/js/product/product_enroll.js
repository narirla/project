document.addEventListener('DOMContentLoaded', function () {
    const uploadBox = document.querySelector('.upload-box');
    const input = document.querySelector('#productImage');
    const preview = uploadBox.querySelector('.image-preview');
    const icon = uploadBox.querySelector('i');
    const text = uploadBox.querySelector('p');
    const nameList = document.getElementById('imageNameList');

    const maxCount = 10;
    let filesArr = []; // 업로드할 이미지 파일을 담을 배열

    // 업로드 박스 클릭 시 파일 선택 창 열기
    uploadBox.addEventListener('click', function (e) {
        // input 요소 자체가 클릭된 경우 이중 호출을 막음
        if(e.target === input || e.target.tagName === 'IMG') return;
        input.click();
    });

    // 파일 선택 시
    input.addEventListener('change', function (e) {
        const newFiles = Array.from(input.files);
        // 새로운 파일 중 중복되지 않는 파일만 추가
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

    // 임시저장 버튼 이벤트 리스너 추가
    const tempSaveBtn = document.getElementById('tempSaveBtn');
    if (tempSaveBtn) {
        tempSaveBtn.addEventListener('click', function(event) {
            handleTempSave();
        });
    }

    // 임시저장 버튼 클릭 핸들러
    async function handleTempSave() {
        const form = document.getElementById('productForm');
        const formData = new FormData(form);

        // DTO에 '임시저장' 상태 값 추가
        formData.append('status', '임시저장');

        // 이미지 및 파일 데이터 추가
        const productImages = document.getElementById('productImage').files;
        for (const file of productImages) {
            formData.append('productImages', file);
        }

        const documentFile = document.querySelector('input[name="documentFile"]').files[0];
        if (documentFile) {
            formData.append('documentFile', documentFile);
        }

//        // 코스 포인트 데이터 추가 (만약 있다면)
//        const coursePoints = getCoursePointsFromForm(); // ⭐ 이 함수는 기존에 구현되어 있어야 합니다.
//        if (coursePoints.length > 0) {
//            // FormData에 배열을 추가하는 방법
//            coursePoints.forEach((point, index) => {
//                formData.append(`coursePoints[${index}].latitude`, point.latitude);
//                formData.append(`coursePoints[${index}].longitude`, point.longitude);
//                formData.append(`coursePoints[${index}].description`, point.description);
//            });
//        }

        try {
            const response = await fetch('/product/temp-save', {
                method: 'POST',
                body: formData
            });

            if (response.redirected) {
                window.location.href = response.url;
            } else {
                const result = await response.json();
                // 에러 처리 또는 성공 메시지 표시
                if (result.error) {
                    alert(result.error);
                } else {
                    alert('상품이 임시저장되었습니다.');
                    // 성공적으로 임시저장되면 상품 관리 페이지로 리다이렉트
                    window.location.href = '/product/manage?status=임시저장';
                }
            }

        } catch (error) {
            console.error('임시저장 중 오류 발생:', error);
            alert('임시저장 중 오류가 발생했습니다. 다시 시도해주세요.');
        }
    }
});