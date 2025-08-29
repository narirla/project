/*index.js*/
document.addEventListener("DOMContentLoaded", () => {
  new Swiper(".mySwiper", {
    loop: true,
    autoplay: {
      delay: 5000, // 0.6초마다 변경 → 3초에 5장
      disableOnInteraction: false,
    },
    speed: 600,
  });
});
