/*layout.js*/
document.addEventListener('DOMContentLoaded', () => {
  console.log('✅ layout.js 실행됨');

  const menuButtons = document.querySelectorAll('.menuButton');
  console.log('✅ menuButtons:', menuButtons);

  menuButtons.forEach((menuButton, index) => {
    const dropdownMenu = menuButton.nextElementSibling;
    console.log(`✅ ${index+1}번째 dropdownMenu 연결됨:`, dropdownMenu);

    menuButton.addEventListener('click', (e) => {
      e.preventDefault();
      console.log(`✅ ${index+1}번째 menuButton 클릭됨`);

      const isVisible = dropdownMenu.style.display === 'flex';
      dropdownMenu.style.display = isVisible ? 'none' : 'flex';

      console.log('👉 변경 후 dropdownMenu 상태:', dropdownMenu.style.display);
    });
  });
});
