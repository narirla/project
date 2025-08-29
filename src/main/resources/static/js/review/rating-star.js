document.addEventListener("DOMContentLoaded", () => {
(() => {
  const labels = {
    1: "매우 불만",
    2: "불만",
    3: "보통",
    4: "만족",
    5: "매우 만족"
  };

  const MAX = 5;
  document.querySelectorAll('.reviewSecond').forEach(root => {
    const starBox = root.querySelector('.star-rating');
    const fillBox = starBox.querySelector('.star-fill');
    const labelEl = root.querySelector('.star-label');

    const raw = parseFloat(starBox.dataset.score ?? "0");
    const score = Math.round(Math.max(1, Math.min(MAX, raw))); // 1~5 정수
    const percent = (raw / MAX) * 100;

    // 별 채우기
    fillBox.style.width = `${percent}%`;

    // 라벨 붙이기
    labelEl.textContent = labels[score];
  });
})();
});