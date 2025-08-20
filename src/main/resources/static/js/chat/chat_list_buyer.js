document.addEventListener("DOMContentLoaded", () => {
  const tabs = document.querySelectorAll(".tab-button");
  const contents = document.querySelectorAll(".tab-content");

  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      // 모든 버튼 active 제거
      tabs.forEach(t => t.classList.remove("active"));
      // 모든 컨텐츠 active 제거
      contents.forEach(c => c.classList.remove("active"));

      // 클릭한 버튼 활성화
      tab.classList.add("active");

      // 클릭한 버튼의 data-tab 속성값과 같은 id를 가진 content 활성화
      const target = document.getElementById(tab.dataset.tab);
      if (target) {
        target.classList.add("active");
      }
    });
  });
});
