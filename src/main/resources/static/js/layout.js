// 레이아웃 초기화
(() => {
  if (window.__LAYOUT_INIT__) return;
  window.__LAYOUT_INIT__ = true;

  const ready = () => {
    const wrappers = document.querySelectorAll('.dropdown-wrapper');
    let openedMenu = null;
    let openedBtn  = null;

    const closeAll = () => {
      document.querySelectorAll('.dropdown-menu.open')
        .forEach(m => m.classList.remove('open'));
      document.querySelectorAll('.menuButton[aria-expanded="true"]')
        .forEach(b => b.setAttribute('aria-expanded', 'false'));
      openedMenu = null;
      openedBtn  = null;
    };

    const openMenu = (btn, menu) => {
      if (openedMenu === menu) return;
      closeAll();
      menu.classList.add('open');
      btn.setAttribute('aria-expanded', 'true');
      openedMenu = menu;
      openedBtn  = btn;
    };

    // 드롭다운 초기화
    wrappers.forEach((wrap) => {
      const btns = wrap.querySelectorAll('.menuButton');
      const menu = wrap.querySelector('.dropdown-menu');
      if (!btns.length || !menu) return;

      // 버튼 설정
      btns.forEach((btn) => {
        btn.setAttribute('aria-haspopup', 'true');
        btn.setAttribute('aria-expanded', 'false');
        btn.addEventListener('click', (e) => {
          e.preventDefault();
          e.stopPropagation();
          menu.classList.contains('open') ? closeAll() : openMenu(btn, menu);
        });
      });

      // 메뉴 내부 클릭은 닫힘 방지
      menu.addEventListener('click', (e) => e.stopPropagation());
    });   // ←←← **이 줄(반복문 닫기)이 누락되어 있었음**

    // 바깥 영역 클릭 시 닫기
    document.addEventListener('click', (e) => {
      if (!openedMenu) return;
      const inWrapper = e.target.closest('.dropdown-wrapper');
      const inMenu    = e.target.closest('.dropdown-menu');
      if (!inWrapper && !inMenu) closeAll();
    });

    // ESC로 닫기(포커스 복원)
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && openedMenu) {
        closeAll();
        if (openedBtn) openedBtn.focus();
      }
    });

    // 스크롤/리사이즈 시 닫기
    window.addEventListener('scroll', closeAll, { passive: true });
    window.addEventListener('resize', closeAll);

    // 내부 스크롤 컨테이너 대응
    const pageLayout = document.querySelector('.page-layout');
    if (pageLayout) pageLayout.addEventListener('scroll', closeAll, { passive: true });

    // 디버깅용
    window.__closeAllDropdowns = closeAll;
    
    // 장바구니 개수 설정
    initCartCount();
  };

  (document.readyState === 'loading')
    ? document.addEventListener('DOMContentLoaded', ready)
    : ready();
})();

// 장바구니 개수 초기화
async function initCartCount() {
  try {
    const response = await fetch('/cart/count', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (response.ok) {
      const data = await response.json();
      updateCartBadge(data.count || 0);
    }
  } catch (error) {
    // 오류 시 조용히 무시 (로그인하지 않은 경우 등)
  }
}

// 장바구니 개수 업데이트
async function updateCartCount() {
  try {
    const response = await fetch('/cart/count', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (response.ok) {
      const data = await response.json();
      updateCartBadge(data.count || 0);
    }
  } catch (error) {
    console.error('장바구니 개수 업데이트 실패:', error);
  }
}

// 장바구니 배지 업데이트
function updateCartBadge(count) {
  const badge = document.getElementById('cart-count');
  if (badge) {
    if (count > 0) {
      badge.textContent = count;
      badge.style.display = 'flex';
    } else {
      badge.style.display = 'none';
    }
  }
}

// 전역에서 접근 가능하도록
window.updateCartCount = updateCartCount;
window.initCartCount = initCartCount;
