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
});