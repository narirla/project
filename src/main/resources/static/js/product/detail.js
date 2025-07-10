  document.addEventListener("DOMContentLoaded", function() {
    const slides = document.querySelectorAll(".slide");
    const prevBtn = document.getElementById("prevBtn");
    const nextBtn = document.getElementById("nextBtn");
    let currentIndex = 0;

    function showSlide(index) {
      slides.forEach((slide, i) => {
        slide.classList.toggle("active", i === index);
      });
    }

    prevBtn.addEventListener("click", () => {
      currentIndex = (currentIndex === 0) ? slides.length - 1 : currentIndex - 1;
      showSlide(currentIndex);
    });

    nextBtn.addEventListener("click", () => {
      currentIndex = (currentIndex === slides.length - 1) ? 0 : currentIndex + 1;
      showSlide(currentIndex);
    });

    // 처음 페이지 로드시 첫번째 슬라이드 보여주기
    showSlide(currentIndex);

    // 숫자 포매팅
    const priceElems = document.querySelectorAll(".allPrice");

    priceElems.forEach(priceElem => {
      const originalText = priceElem.textContent;

      // 숫자만 뽑아내기 (점(.)과 마이너스(-)도 포함)
      const onlyNumber = originalText.match(/[\d.-]+/g);

      if (onlyNumber) {
        const numberValue = Number(onlyNumber.join("")); // 배열 합쳐서 숫자 변환
        if (!isNaN(numberValue)) {
          // 숫자만 포매팅 하고 다시 기존 텍스트에서 숫자를 교체
          const formattedNumber = numberValue.toLocaleString();

          // 숫자 부분만 바꿔서 텍스트 갱신
          const newText = originalText.replace(/[\d.,-]+/, formattedNumber);

          priceElem.textContent = newText;
        }
      }
    });
  });