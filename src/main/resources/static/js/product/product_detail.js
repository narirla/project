document.addEventListener("DOMContentLoaded", function() {
    // 1. 상태 텍스트에 색상 적용
    document.querySelectorAll('.status-text').forEach(function(el) {
        var status = el.innerText.trim();
        console.log("상태값:", status);
        if (status === '판매중') {
            el.classList.add('status-green');
        } else if (status === '판매중단') {
            el.classList.add('status-red');
        }
    });

    // 2. 문장 분리: 첫 문장은 헤드라인, 나머지는 본문
    const descEl = document.getElementById('desc-origin');
    if (descEl) {
        const desc = descEl.textContent.trim();
        // 마침표, 물음표, 느낌표([.?!]) 뒤에 공백이 있어도 분리
        const splitRegex = /([.?!])\s*/;
        const arr = desc.split(splitRegex);

        let headline = '';
        let description = '';

        if (arr.length >= 2) {
            headline = arr[0] + arr[1];
            description = arr.slice(2).join('').trim();
        } else {
            headline = desc;
            description = '';
        }

        const headlineEl = document.getElementById('headline');
        const mainEl = document.getElementById('desc-main');
        if (headlineEl) headlineEl.innerText = headline;
        if (mainEl) mainEl.innerText = description;
    }
});
