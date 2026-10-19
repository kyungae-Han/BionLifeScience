/* 고객문의 폼 동작.
 *
 * ⚠️ 이 파일은 common.html 이 불러온다. contact.html 안에 두면 안 된다.
 *    고객문의 화면은 모달이 #openContact 안쪽만 잘라 innerHTML 로 꽂는데,
 *    그렇게 들어간 <script> 는 실행되지 않는다.
 *    예전에는 contact.js 가 contact.html 안에 있어서 모달에서 한 번도 안 돌았고,
 *    그 바람에 동의를 안 해도 전송 단추가 눌렸다. (2026-10-19 확인)
 *
 * 그래서 모든 처리를 document 에 위임해서 건다. 폼이 나중에 꽂혀도 걸린다.
 */
(function () {
  'use strict';

  var MAX_BYTES = 20 * 1024 * 1024;              // 20MB
  var OK_EXT = ['jpg', 'jpeg', 'png', 'pdf', 'zip'];

  function $(sel, root) { return (root || document).querySelector(sel); }

  /** 화면 문구. common.html 이 로케일에 맞춰 window.I18N 에 실어 준다.
   *  (여기 한글을 직접 적으면 영문으로 안 바뀐다. 뒤의 값은 I18N 이 없을 때의 대비용) */
  function msg(key, fallback) {
    return (window.I18N && window.I18N[key]) || fallback;
  }

  function prettySize(bytes) {
    if (bytes < 1024) { return bytes + 'B'; }
    if (bytes < 1024 * 1024) { return Math.round(bytes / 1024) + 'KB'; }
    return (bytes / (1024 * 1024)).toFixed(1) + 'MB';
  }

  function ext(name) {
    var i = name.lastIndexOf('.');
    return i < 0 ? '' : name.slice(i + 1).toLowerCase();
  }

  /** 고른 파일을 이름표로 보여 준다. 없으면 숨긴다 */
  function paintChip() {
    var input = $('#template-contactform-file');
    var chip = $('#cfChip');
    if (!input || !chip) { return; }

    var f = input.files && input.files[0];
    if (!f) {
      chip.hidden = true;
      return;
    }
    $('.cf-chip__name', chip).textContent = f.name;
    $('.cf-chip__size', chip).textContent = prettySize(f.size);
    chip.hidden = false;
  }

  /** 용량·확장자 검사. 맞지 않으면 비우고 false */
  function checkFile(input) {
    var f = input.files && input.files[0];
    if (!f) { return true; }

    if (OK_EXT.indexOf(ext(f.name)) < 0) {
      alert(msg('contactExt', 'jpg, jpeg, png, pdf, zip 만 올릴 수 있습니다.'));
      input.value = '';
      return false;
    }
    if (f.size > MAX_BYTES) {
      alert(msg('contactSize', '1회 업로드 가능 용량은 20MB입니다.'));
      input.value = '';
      return false;
    }
    return true;
  }

  // ── 동의 체크 → 전송 단추 ──────────────────────────────────────────
  /** 체크 상태에 맞춰 단추를 잠그거나 푼다 */
  function syncSendButton() {
    var chk = $('#policyCheck');
    var btn = $('#contactBtn');
    if (chk && btn) { btn.disabled = !chk.checked; }
  }

  // 폼은 모달이 열릴 때 뒤늦게 꽂힌다. 꽂히는 순간 한 번 맞춰 준다.
  // 마크업에는 disabled 를 안 박아 두었다 (JS 가 못 뜨면 문의를 아예 못 보내게 되므로).
  if (window.MutationObserver) {
    new MutationObserver(function () { syncSendButton(); })
      .observe(document.documentElement, { childList: true, subtree: true });
  }
  document.addEventListener('DOMContentLoaded', syncSendButton);

  // 보내기 직전에 한 번 더 막는다. 단추가 열려 있어도 동의 없이는 안 보낸다.
  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (!form || form.id !== 'contactForm') { return; }
    var chk = $('#policyCheck');
    if (chk && !chk.checked) {
      e.preventDefault();
      alert(msg('contactConsent', '개인정보의 수집 및 이용에 동의해 주세요.'));
      chk.focus();
    }
  });

  document.addEventListener('change', function (e) {
    if (e.target && e.target.id === 'policyCheck') {
      var btn = $('#contactBtn');
      if (btn) { btn.disabled = !e.target.checked; }
    }
    if (e.target && e.target.id === 'template-contactform-file') {
      if (checkFile(e.target)) { paintChip(); } else { paintChip(); }
    }
  });

  // ── 첨부 지우기 ───────────────────────────────────────────────────
  document.addEventListener('click', function (e) {
    var x = e.target.closest ? e.target.closest('.cf-chip__x') : null;
    if (x) {
      var input = $('#template-contactform-file');
      if (input) { input.value = ''; }
      paintChip();
      return;
    }

    // ── 약관 보기. 머리말에 이미 있는 모달을 그대로 띄운다 ──────────
    var policy = e.target.closest ? e.target.closest('#cfOpenPolicy') : null;
    if (policy) {
      var pb = document.getElementById('mainOpenPolicy');
      if (pb) { pb.click(); }
      else { window.open('/front/company/privacy-policy', '_blank'); }
      return;
    }
    var terms = e.target.closest ? e.target.closest('#cfOpenTerms') : null;
    if (terms) {
      var tb = document.querySelector('.mainOpenTerms');
      if (tb) { tb.click(); }
      else { window.open('/front/company/terms', '_blank'); }
    }
  });

  // ── 끌어다 놓기 ───────────────────────────────────────────────────
  // label 이라 누르면 파일 고르기가 열린다. 여기서는 드래그만 맡는다.
  function overDrop(e) {
    var drop = e.target.closest ? e.target.closest('#cfDrop') : null;
    if (!drop) { return null; }
    e.preventDefault();
    return drop;
  }

  document.addEventListener('dragover', function (e) {
    var drop = overDrop(e);
    if (drop) { drop.classList.add('is-over'); }
  });

  document.addEventListener('dragleave', function (e) {
    var drop = overDrop(e);
    if (drop) { drop.classList.remove('is-over'); }
  });

  document.addEventListener('drop', function (e) {
    var drop = overDrop(e);
    if (!drop) { return; }
    drop.classList.remove('is-over');

    var input = $('#template-contactform-file');
    var files = e.dataTransfer && e.dataTransfer.files;
    if (!input || !files || !files.length) { return; }

    // 서버는 파일 하나만 받는다. 여러 개를 떨어뜨리면 첫 개만 쓴다.
    if (files.length > 1) { alert(msg('contactOne', '첨부는 한 개만 됩니다. 첫 번째 파일만 넣었습니다.')); }

    var dt = new DataTransfer();
    dt.items.add(files[0]);
    input.files = dt.files;

    if (checkFile(input)) { paintChip(); } else { paintChip(); }
  });
})();
