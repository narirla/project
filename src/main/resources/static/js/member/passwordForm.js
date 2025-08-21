/* /js/member/passwordForm.js */

(() => {
  const $ = (sel, p=document) => p.querySelector(sel);
  const $$ = (sel, p=document) => [...p.querySelectorAll(sel)];

  const form = $('form[th\\:object], form.verify-form') || document.querySelector('form');
  const current   = $('#currentPassword');
  const npw       = $('#newPassword');
  const cpw       = $('#confirmPassword');
  const submitBtn = $('#submitBtn');
  const strength  = $('.strength');

  const criteria = {
    length:  $('#c-length'),
    upper:   $('#c-upper'),
    lower:   $('#c-lower'),
    digit:   $('#c-digit'),
    special: $('#c-special'),
    space:   $('#c-space')
  };
  const currentMsg = $('#currentPwMsg');
  const matchMsg   = $('#matchMsg');

  // 재인증(TTL 내) 여부: hidden 또는 data-attr로 전달되었다면 반영
  const verifiedRecently =
    ($('#pwdVerifiedRecently')?.value === 'true') ||
    (form?.dataset?.verified === 'true');

  // ===== 비밀번호 보기/숨기기 토글 =====
  document.addEventListener('DOMContentLoaded', () => {
    $$('.input-wrap .toggle-btn').forEach((btn) => {
      btn.addEventListener('click', () => {
        const wrap  = btn.closest('.input-wrap');
        const input = wrap?.querySelector('input[type="password"], input[type="text"]');
        if (!input) return;

        const icon = btn.querySelector('i');

        const willShow = input.type === 'password';
        input.type = willShow ? 'text' : 'password';
        const visible = input.type === 'text';

        if (icon) {
          icon.classList.remove('fa-regular','fa-solid','fa-eye','fa-eye-slash');
          // ✅ 상태 기준: 보임=eye, 숨김=eye-slash
          // 같은 스타일로 가려면 둘 다 fa-regular를 쓰는 게 깔끔합니다.
          icon.classList.add('fa-regular', visible ? 'fa-eye' : 'fa-eye-slash');
          // (원하면 보임일 때만 굵게) icon.classList.add(visible ? 'fa-solid' : 'fa-regular', ...);
        }
        btn.setAttribute('aria-pressed', String(visible));
        btn.setAttribute('aria-label',  visible ? '비밀번호 숨기기' : '비밀번호 표시');
      });
    });
  });

  // ===== 규칙 검사 =====
  const tests = (v='') => ({
    length : v.length >= 8 && v.length <= 12,
    upper  : /[A-Z]/.test(v),
    lower  : /[a-z]/.test(v),
    digit  : /\d/.test(v),
    special: /[!@#$%^&*()\-=\+\[\]{};:'",.<>/?\\|`~]/.test(v),
    space  : !/\s/.test(v),
  });

  const setClass = (el, ok) => { if (el) el.classList.toggle('ok', !!ok); };

  // 강도 갱신
  const updateStrength = () => {
    const t = tests(npw?.value || '');

    setClass(criteria.length,  t.length);
    setClass(criteria.upper,   t.upper);
    setClass(criteria.lower,   t.lower);
    setClass(criteria.digit,   t.digit);
    setClass(criteria.special, t.special);
    setClass(criteria.space,   t.space);

    const satisfied = Object.values(t).filter(Boolean).length;
    const level = Math.min(4, Math.max(0, Math.floor((satisfied - 2))));
    if (strength) strength.className = 'strength' + (level ? ` level-${level}` : '');

    // 메시지
    if (matchMsg) {
      if (npw?.value && current?.value && npw.value === current.value) {
        matchMsg.textContent = '새 비밀번호가 현재 비밀번호와 동일합니다.';
        matchMsg.className = 'helper bad';
      } else if (cpw?.value && npw?.value !== cpw.value) {
        matchMsg.textContent = '새 비밀번호가 일치하지 않습니다.';
        matchMsg.className = 'helper bad';
      } else if (cpw?.value && npw?.value === cpw.value) {
        matchMsg.textContent = '새 비밀번호가 일치합니다.';
        matchMsg.className = 'helper ok';
      } else {
        matchMsg.textContent = '';
        matchMsg.className = 'helper';
      }
    }

    gate();
  };

  // ===== 현재 비밀번호 원격검증 =====
  // [변경] 단일 엔드포인트: GET /members/password/check?currentPassword=...
  let currentOk = false;

  const verifyCurrent = async () => {
    if (verifiedRecently) { // 재인증 상태면 스킵
      currentOk = true;
      if (currentMsg) { currentMsg.textContent = '최근 재인증 상태입니다.'; currentMsg.className = 'helper ok'; }
      gate();
      return;
    }

    const val = current?.value || '';
    currentOk = false;
    if (currentMsg) { currentMsg.textContent = ''; currentMsg.className = 'helper'; }
    if (!val) { gate(); return; }

    try {
      const res = await fetch(`/members/password/check?currentPassword=${encodeURIComponent(val)}`, {
        method: 'GET',
        cache: 'no-store'
      });
      if (!res.ok) throw new Error(`GET check failed: ${res.status}`);

      currentOk = await parseBoolResponse(res);
      if (currentMsg) {
        currentMsg.textContent = currentOk ? '현재 비밀번호 확인됨.' : '현재 비밀번호가 올바르지 않습니다.';
        currentMsg.className = 'helper ' + (currentOk ? 'ok' : 'bad');
      }
    } catch (e) {
      if (currentMsg) {
        currentMsg.textContent = '현재 비밀번호 확인 중 오류가 발생했습니다.';
        currentMsg.className = 'helper bad';
      }
      currentOk = false;
    } finally {
      gate();
    }
  };

  async function parseBoolResponse(res) {
    const ct = (res.headers.get('content-type') || '').toLowerCase();
    if (ct.includes('application/json')) {
      const data = await res.json();
      if (typeof data === 'boolean') return data;
      if (data && typeof data === 'object') return !!(data.result ?? data.valid ?? data.ok);
      return false;
    } else {
      const txt = (await res.text()).trim();
      return txt === 'true' || txt === 'ok' || txt === '1';
    }
  }

  // ===== 제출 가능 조건 =====
  const gate = () => {
    if (!submitBtn) return;

    const t = tests(npw?.value || '');
    const allRules = t.length && t.upper && t.lower && t.digit && t.special && t.space;
    const match = !!(npw?.value && cpw?.value && (npw.value === cpw.value));
    const notSameAsCurrent = !(npw?.value && current?.value && npw.value === current.value);

    // verifiedRecently면 현재 비밀번호 확인 없이 제출 허용
    const ready = (verifiedRecently ? true : currentOk) && allRules && match && notSameAsCurrent;
    submitBtn.disabled = !ready;
  };

  // ===== 이벤트 바인딩 =====
  if (npw)  npw.addEventListener('input', updateStrength);
  if (cpw)  cpw.addEventListener('input', updateStrength);

  if (current) {
    current.addEventListener('input', () => { if (!verifiedRecently) { currentOk = false; } gate(); });
    // blur에서만 원격검증(입력 중 과도요청 방지). 재인증 상태면 스킵.
    current.addEventListener('blur', verifyCurrent);
  }

  // ===== 초기화 =====
  updateStrength();
  gate();
})();
