/*layout.js*/
document.addEventListener('DOMContentLoaded', () => {
  console.log('✅ layout.js 실행됨');

  const menuButtons = document.querySelectorAll('.menuButton');
  const dropdownMenu = document.querySelector('.dropdown-menu'); // 단일 메뉴

  // 초기화
  dropdownMenu.style.display = 'none';

  menuButtons.forEach((menuButton, index) => {
    menuButton.addEventListener('click', (e) => {
      e.preventDefault();
      console.log(`✅ ${index + 1}번째 menuButton 클릭됨`);

      const isVisible = dropdownMenu.style.display === 'flex';
      dropdownMenu.style.display = isVisible ? 'none' : 'flex';

      console.log('👉 드롭다운 상태:', dropdownMenu.style.display);
    });
  });

  // 외부 클릭 시 닫기
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.dropdown-wrapper')) {
      dropdownMenu.style.display = 'none';
      console.log('🧼 외부 클릭: 드롭다운 닫힘');
    }
  });
});
