/* joinForm.js */

// -------------------------------
// 0) 중복확인 상태
// -------------------------------
let emailCheckStatus = { checked: false, value: '' };
let nicknameCheckStatus = { checked: false, value: '' };

// -------------------------------
// 1) 유틸 함수
// -------------------------------

// 이메일 형식 검사
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// 전화번호 자동 하이픈(단일 입력)
function autoHyphenTel(el) {
  const digits = el.value.replace(/\D/g, '').slice(0, 11);
  let out = '';

  if (digits.startsWith('02')) { // 서울 국번
    if (digits.length < 3) out = digits;
    else if (digits.length < 6) out = `${digits.slice(0,2)}-${digits.slice(2)}`;
    else if (digits.length < 10) out = `${digits.slice(0,2)}-${digits.slice(2,5)}-${digits.slice(5)}`;
    else out = `${digits.slice(0,2)}-${digits.slice(2,6)}-${digits.slice(6,10)}`;
  } else { // 휴대폰
    if (digits.length < 4) out = digits;
    else if (digits.length < 8) out = `${digits.slice(0,3)}-${digits.slice(3)}`;
    else out = `${digits.slice(0,3)}-${digits.slice(3,7)}-${digits.slice(7,11)}`;
  }
  el.value = out;
}

// 이메일 분리 입력 → 합치기
function composeEmail() {
  const id = (document.getElementById('emailId')?.value || '').trim();
  const sel = document.getElementById('emailDomainSelect')?.value || '';
  const custom = (document.getElementById('emailDomainInput')?.value || '').trim();
  const domain = (sel === '직접입력') ? custom : sel;
  if (!id || !domain) return '';
  return `${id}@${domain}`;
}

// 합친 이메일을 hidden(th:field="*{email}")에 세팅
function setEmailHidden() {
  const full = composeEmail();
  const hidden = document.getElementById('emailFull');
  if (hidden) hidden.value = full;
  return full;
}

// 이메일 변경 시 중복확인 무효화
function invalidateEmailCheck() {
  emailCheckStatus.checked = false;
  emailCheckStatus.value = '';
  const box = document.getElementById('emailCheckResult');
  if (box) { box.textContent = ''; box.style.color = ''; }
}

// 생년월일 유틸
function toYMD(date){
  const y = date.getFullYear();
  const m = String(date.getMonth()+1).padStart(2,'0');
  const d = String(date.getDate()).padStart(2,'0');
  return `${y}-${m}-${d}`;
}
function getAge(ymd){
  const [y,m,d] = ymd.split('-').map(Number);
  const today = new Date();
  let age = today.getFullYear() - y;
  const mDiff = (today.getMonth()+1) - m;
  if (mDiff < 0 || (mDiff === 0 && today.getDate() < d)) age--;
  return age;
}
function setBirthDateBounds(minAge = 14, maxAge = 120){
  const el = document.getElementById('birthDate');
  if (!el) return;
  const today = new Date();
  const max = new Date(today.getFullYear() - minAge, today.getMonth(), today.getDate()); // 가장 늦은 생일
  const min = new Date(today.getFullYear() - maxAge, today.getMonth(), today.getDate()); // 너무 고령 방지
  el.max = toYMD(max);
  el.min = toYMD(min);
}

// -------------------------------
// 2) 서버 통신 (중복확인)
// -------------------------------

// 이메일 중복 확인 (분리 입력을 결합해 확인)
async function checkEmail() {
  const resultBox = document.getElementById("emailCheckResult");
  const email = setEmailHidden(); // 결합 및 hidden 적용

  if (!email) {
    resultBox.textContent = "이메일 아이디와 도메인을 입력하세요.";
    resultBox.style.color = "red";
    return;
  }
  if (!isValidEmail(email)) {
    resultBox.textContent = "올바른 이메일 형식이 아닙니다.";
    resultBox.style.color = "red";
    return;
  }

  try {
    const response = await fetch(`/members/emailCheck?email=${encodeURIComponent(email)}`);
    const result = await response.json();
    if (result.exists === true) {
      resultBox.textContent = "이미 사용 중인 이메일입니다.";
      resultBox.style.color = "red";
      emailCheckStatus.checked = false;
      emailCheckStatus.value = '';
    } else {
      resultBox.textContent = "사용 가능한 이메일입니다.";
      resultBox.style.color = "green";
      emailCheckStatus.checked = true;
      emailCheckStatus.value = email;
    }
  } catch (error) {
    console.error("이메일 중복 확인 실패:", error);
    resultBox.textContent = "오류가 발생했습니다.";
    resultBox.style.color = "red";
    emailCheckStatus.checked = false;
    emailCheckStatus.value = '';
  }
}

// 닉네임 중복 확인
async function checkNickname() {
  const nickname = document.getElementById("nickname").value.trim();
  const nicknameCheckResult = document.getElementById("nicknameCheckResult");

  if (!nickname) {
    nicknameCheckResult.textContent = "닉네임을 입력해주세요.";
    nicknameCheckResult.style.color = "red";
    return;
  }

  try {
    const response = await fetch(`/members/nicknameCheck?nickname=${encodeURIComponent(nickname)}`);
    const exists = await response.json();

    if (exists) {
      nicknameCheckResult.textContent = "이미 사용 중인 닉네임입니다.";
      nicknameCheckResult.style.color = "red";
      nicknameCheckStatus.checked = false;
      nicknameCheckStatus.value = '';
    } else {
      nicknameCheckResult.textContent = "사용 가능한 닉네임입니다.";
      nicknameCheckResult.style.color = "green";
      nicknameCheckStatus.checked = true;
      nicknameCheckStatus.value = nickname;
    }
  } catch (error) {
    console.error("닉네임 중복 확인 실패:", error);
  }
}

// -------------------------------
// 3) UI 보조
// -------------------------------
function showToast(message) {
  const toast = document.getElementById("toast");
  toast.textContent = message;
  toast.style.display = "block";
  toast.classList.remove("toast-message");
  void toast.offsetWidth; // reflow
  toast.classList.add("toast-message");
  setTimeout(() => { toast.style.display = "none"; }, 2500);
}

function execDaumPostcode() {
  new daum.Postcode({
    oncomplete: function(data) {
      document.getElementById("zonecode").value = data.zonecode;
      document.getElementById("address").value = data.roadAddress;
      document.getElementById("detailAddress").focus();
    }
  }).open();
}

function togglePassword(id) {
  const pwInput = document.getElementById(id);
  const wrapper = pwInput.parentElement;
  const toggleBtn = wrapper.querySelector('.toggle-password');
  const icon = toggleBtn.querySelector('i');

  if (pwInput.type === "text") {
    pwInput.type = "password";
    icon.classList.remove("fa-eye");
    icon.classList.add("fa-eye-slash");
  } else {
    pwInput.type = "text";
    icon.classList.remove("fa-eye-slash");
    icon.classList.add("fa-eye");
  }
}

// 비밀번호 강도 평가
function evaluatePasswordStrength(password) {
  const strengthBox = document.getElementById("pwStrength");
  const hintBox = document.getElementById("pwHint");

  const hasDigit = /\d/.test(password);
  const hasAlpha = /[a-zA-Z]/.test(password);
  const hasSymbol = /[^a-zA-Z0-9]/.test(password);
  const hasRepeat = /(.)\1{2,}/.test(password); // 같은 문자 3회 이상

  if (password.length < 8 || hasRepeat) {
    strengthBox.textContent = "약함";
    strengthBox.className = "pw-strength weak";
    hintBox.textContent = "비밀번호는 8~12자, 대소문자/숫자/특수문자 포함, 동일 문자 3회 이상 불가";
    return;
  }

  if (hasDigit && hasAlpha && hasSymbol) {
    strengthBox.textContent = "강함";
    strengthBox.className = "pw-strength strong";
    hintBox.textContent = "";
  } else if ((hasDigit && hasAlpha) || (hasAlpha && hasSymbol)) {
    strengthBox.textContent = "보통";
    strengthBox.className = "pw-strength medium";
    hintBox.textContent = "영문/숫자/특수문자를 조합하세요.";
  } else {
    strengthBox.textContent = "약함";
    strengthBox.className = "pw-strength weak";
    hintBox.textContent = "비밀번호가 너무 단순합니다.";
  }
}

// -------------------------------
// 4) 목록 복귀(ESC만 사용)
// -------------------------------
function switchDomainToSelect() {
  const domainSel   = document.getElementById('emailDomainSelect');
  const domainInput = document.getElementById('emailDomainInput');

  domainInput.style.display = 'none';
  domainInput.value = '';
  domainSel.style.display = '';
  if (domainSel.value === '직접입력') domainSel.value = '';

  invalidateEmailCheck();
  setEmailHidden();
  domainSel.focus();
}

// -------------------------------
// 5) DOMContentLoaded: 이벤트 바인딩
// -------------------------------
document.addEventListener("DOMContentLoaded", () => {
  // 비밀번호 강도
  const pwInput = document.getElementById("passwd");
  pwInput?.addEventListener("input", () => evaluatePasswordStrength(pwInput.value));

  // 닉네임 중복확인(blur)
  const nicknameInput = document.getElementById("nickname");
  nicknameInput?.addEventListener("blur", checkNickname);

  // 이메일 분리 입력
  const emailId     = document.getElementById('emailId');
  const domainSel   = document.getElementById('emailDomainSelect');
  const domainInput = document.getElementById('emailDomainInput');

  emailId?.addEventListener('input', () => { invalidateEmailCheck(); setEmailHidden(); });
  emailId?.addEventListener('keydown', (e) => {
    if (e.key === "Enter") { e.preventDefault(); checkEmail(); }
  });

  domainSel?.addEventListener('change', () => {
    if (domainSel.value === '직접입력') {
      // 직접입력 모드: select 숨기고 input 노출/포커스
      domainSel.style.display = 'none';
      domainInput.style.display = 'block';
      domainInput.style.flex = domainSel.style.flex || '2';
      domainInput.value = '';
      domainInput.focus();
    } else {
      // 선택 모드
      domainInput.style.display = 'none';
      domainInput.value = '';
      domainSel.style.display = '';
    }
    invalidateEmailCheck();
    setEmailHidden();
  });

  domainInput?.addEventListener('input', () => { invalidateEmailCheck(); setEmailHidden(); });
  domainInput?.addEventListener('keydown', (e) => {
    if (e.key === "Enter")  { e.preventDefault(); checkEmail(); }
    if (e.key === "Escape") { e.preventDefault(); switchDomainToSelect(); }
  });

  // 전화번호 blur 시 인라인 에러
  const tel = document.getElementById('tel');
  tel?.addEventListener('blur', () => {
    const msg = document.getElementById('telError');
    const v = (tel.value || '').trim();
    const telPattern = /^01[016789]-\d{3,4}-\d{4}$/;
    if (!v) msg.textContent = '전화번호를 입력해주세요.';
    else if (!telPattern.test(v)) msg.textContent = '형식: 010-1234-5678';
    else msg.textContent = '';
  });

  // 생년월일 범위 제약 및 인라인 검증
  setBirthDateBounds(14, 120); // 필요 시 minAge 변경
  const birthEl  = document.getElementById('birthDate');
  const birthErr = document.getElementById('birthError');
  birthEl?.addEventListener('blur', () => {
    if (!birthErr) return;
    birthErr.textContent = '';
    const v = birthEl.value;
    if (!v) return; // 선택 입력이라면 비우기 허용
    const age = getAge(v);
    if (age < 0)        birthErr.textContent = '미래 날짜는 입력할 수 없습니다.';
    else if (age < 14)  birthErr.textContent = '만 14세 이상만 가입할 수 있습니다.';
    else if (age > 120) birthErr.textContent = '생년월일을 다시 확인해주세요.';
  });

  // 비밀번호 일치 실시간 체크
  const confirmPwInput = document.getElementById("confirmPasswd");
  confirmPwInput?.addEventListener("input", () => {
    const pw = document.getElementById("passwd").value.trim();
    const pwCheck = confirmPwInput.value.trim();
    const confirmPwErrorBox = document.getElementById("confirmPwError");

    if (pw && pwCheck && pw !== pwCheck) {
      confirmPwErrorBox.textContent = "비밀번호가 일치하지 않습니다.";
      confirmPwErrorBox.style.color = "red";
    } else if (pw && pwCheck && pw === pwCheck) {
      confirmPwErrorBox.textContent = "비밀번호가 일치합니다.";
      confirmPwErrorBox.style.color = "green";
    } else {
      confirmPwErrorBox.textContent = "";
    }
  });
});

// -------------------------------
// 6) 제출 전 유효성 검사
// -------------------------------
function validateForm() {
  // 분리형 이메일 → hidden 세팅
  const emailCombined = setEmailHidden();

  // 필수값(이메일은 '아이디' 입력 기준, 도메인은 별도 체크)
  const requiredFields = [
    { id: "emailId",       name: "이메일 아이디" },
    { id: "passwd",        name: "비밀번호" },
    { id: "confirmPasswd", name: "비밀번호 확인" },
    { id: "nickname",      name: "닉네임" },
    { id: "name",          name: "이름" },
    { id: "tel",           name: "전화번호" }
  ];

  for (const field of requiredFields) {
    const input = document.getElementById(field.id);
    if (!input || !input.value.trim()) {
      alert(`${field.name}은(는) 필수 입력 항목입니다.`);
      input?.focus();
      return false;
    }
  }

  // 이메일 도메인 선택/직접입력 확인
  const domainSel = document.getElementById('emailDomainSelect');
  const domainInput = document.getElementById('emailDomainInput');
  const domainOk = (domainSel?.value && domainSel.value !== '직접입력') ||
                   (domainSel?.value === '직접입력' && domainInput?.value.trim());
  if (!domainOk) {
    alert('이메일 도메인을 선택하거나 직접 입력하세요.');
    (domainSel?.value === '직접입력' ? domainInput : domainSel)?.focus();
    return false;
  }

  // 이메일 형식 및 중복확인 체크 (합친 값 기준)
  if (!isValidEmail(emailCombined)) {
    alert('올바른 이메일 형식이 아닙니다.');
    document.getElementById('emailId')?.focus();
    return false;
  }
  if (!emailCheckStatus.checked || emailCheckStatus.value !== emailCombined) {
    alert("이메일 중복 확인을 완료해주세요.");
    document.getElementById("emailId")?.focus();
    return false;
  }

  // 전화번호 형식 검사
  const tel = document.getElementById("tel").value.trim();
  const telPattern = /^01[016789]-\d{3,4}-\d{4}$/;
  if (!telPattern.test(tel)) {
    alert("전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678");
    document.getElementById("tel").focus();
    return false;
  }

  // 비밀번호 일치 검사
  const pw = document.getElementById("passwd").value.trim();
  const pwCheck = document.getElementById("confirmPasswd").value.trim();
  const confirmPwErrorBox = document.getElementById("confirmPwError");
  if (pw !== pwCheck) {
    confirmPwErrorBox.textContent = "비밀번호가 일치하지 않습니다.";
    confirmPwErrorBox.style.color = "red";
    document.getElementById("confirmPasswd").focus();
    return false;
  } else {
    confirmPwErrorBox.textContent = "";
  }

  // 생년월일(선택 입력인 경우만 검사)
  const birthEl = document.getElementById('birthDate');
  if (birthEl && birthEl.value) {
    const age = getAge(birthEl.value);
    if (age < 0) {
      alert('미래 날짜는 입력할 수 없습니다.');
      birthEl.focus(); return false;
    }
    if (age < 14) {
      alert('만 14세 이상만 가입할 수 있습니다.');
      birthEl.focus(); return false;
    }
    if (age > 120) {
      alert('생년월일을 다시 확인해주세요.');
      birthEl.focus(); return false;
    }
  }

  return true; // 제출 진행
}
