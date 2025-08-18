document.querySelectorAll(".tab-button").forEach(button => {
  button.addEventListener("click", () => {
    // 버튼 active 변경
    document.querySelectorAll(".tab-button").forEach(btn => btn.classList.remove("active"));
    button.classList.add("active");

    // 콘텐츠 active 변경
    document.querySelectorAll(".tab-content").forEach(content => content.classList.remove("active"));
    document.getElementById(button.dataset.tab).classList.add("active");
  });
});