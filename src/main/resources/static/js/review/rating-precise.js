// /js/review/rating-precise.js
(() => {
  const root = document.getElementById('ratingPrecise');
  if (!root) return;

  const wrap   = root.querySelector('.stars-wrap');
  const stars  = root.querySelector('.stars');
  const input  = root.querySelector('#score');
  const output = root.querySelector('#scoreText');

  const MAX  = 5;
  const STEP = 0.1;
  const RIGHT_OVERSHOOT_PX = 24; // CSS의 확장폭과 동일하게

  let selected = Number.parseFloat(input?.value) || 0;

  // 범위 제한
  const clamp = (v, min, max) => Math.min(max, Math.max(min, v));

  // 0.1 단위 반올림 스냅 (간결화)
  const snap = (raw) => Number(clamp(Math.round(raw / STEP) * STEP, 0, MAX).toFixed(1));

  // 시각 반영
  const setVisual = (val) => {
    stars.style.setProperty('--fill', `${clamp((val / MAX) * 100, 0, 100)}%`);
    output.value = val.toFixed(1);
  };

  // wrap 내부 비율 → 값
  const posToValueInside = (clientX) => {
    const rect = wrap.getBoundingClientRect();
    const x = clamp(clientX - rect.left, 0, rect.width);
    return snap((x / (rect.width || 1)) * MAX);
  };

  // 오른쪽 바깥(오버슈트) 클릭 시 5점
  const posToValueWithOvershoot = (clientX) => {
    const rect = wrap.getBoundingClientRect();
    if (clientX > rect.right && clientX <= rect.right + RIGHT_OVERSHOOT_PX) return MAX;
    return posToValueInside(clientX);
  };

  setVisual(selected);

  wrap.addEventListener('mousemove', (e) => setVisual(posToValueInside(e.clientX)));
  wrap.addEventListener('mouseleave', () => setVisual(selected));
  wrap.addEventListener('click', (e) => {
    selected = posToValueWithOvershoot(e.clientX);
    input.value = selected.toFixed(1);
    setVisual(selected);
  });

})();
