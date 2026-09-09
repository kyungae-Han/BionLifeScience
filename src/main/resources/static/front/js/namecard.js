(function () {
  var root = document.documentElement;
  var save = document.getElementById('save');
  var map = document.getElementById('map');
  // 사람마다 다른 값(지도 검색어, 연락처 파일 주소, 복사할 문구)은 서버가 HTML 에 넣어 둔다.
  var ADDR = { ko: map.getAttribute('data-ko'), en: map.getAttribute('data-en') };
  var VCF = save.getAttribute('data-vcf');

  function setLang(lang) {
    root.setAttribute('data-lang', lang);
    root.setAttribute('lang', lang);
    save.setAttribute('href', VCF + (lang === 'en' ? '?lang=en' : '?lang=ko'));
    map.setAttribute('href', ADDR[lang]);
    document.querySelectorAll('.langtoggle button').forEach(function (b) {
      b.setAttribute('aria-pressed', String(b.dataset.set === lang));
    });
    // 링크를 그대로 복사해 전달해도 같은 언어로 열리게 주소만 갱신한다.
    // file:// 로 열어 보거나 주소 조작이 막힌 환경에서는 예외가 난다. 여기서
    // 막지 않으면 그 아래 인앱 브라우저 안내까지 통째로 실행되지 않는다.
    try {
      history.replaceState(null, '', lang === 'en' ? '?lang=en' : location.pathname);
    } catch (e) { /* 주소만 안 바뀔 뿐 명함은 정상 동작한다 */ }
  }

  document.querySelectorAll('.langtoggle button').forEach(function (b) {
    b.addEventListener('click', function () { setLang(b.dataset.set); });
  });

  // 첫 언어. 주소의 ?lang 이 우선이고, 없으면 브라우저 언어가 한국어인지로 정한다.
  var q = new URLSearchParams(location.search).get('lang');
  if (q === 'en' || q === 'ko') setLang(q);
  else if (!/^ko/i.test(navigator.language || '')) setLang('en');

  // 인앱 브라우저는 파일 저장을 막는 경우가 있다. 카톡은 외부 브라우저로 넘길 수 있다.
  var ua = navigator.userAgent || '';
  var kakao = /KAKAOTALK/i.test(ua);
  if (kakao || /NAVER\(inapp|Instagram|FBAN|FBAV|Line\//i.test(ua)) {
    var box = document.getElementById('inapp');
    box.style.display = 'block';
    document.getElementById('inapp-open').addEventListener('click', function () {
      if (kakao) location.href = 'kakaotalk://web/openExternal?url=' + encodeURIComponent(location.href);
      else copyText(location.href, '주소를 복사했습니다. 브라우저에 붙여넣어 주세요.', 'Link copied. Paste it in your browser.');
    });
  }

  function copyText(text, msgKo, msgEn) {
    var done = function () { alert(root.getAttribute('data-lang') === 'en' ? msgEn : msgKo); };
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(text).then(done, function () { legacy(text, done); });
    } else legacy(text, done);
  }
  function legacy(text, done) {
    var ta = document.createElement('textarea');
    ta.value = text; ta.style.position = 'fixed'; ta.style.opacity = '0';
    document.body.appendChild(ta); ta.select();
    try { document.execCommand('copy'); done(); } catch (e) { prompt('', text); }
    document.body.removeChild(ta);
  }

  document.getElementById('copy').addEventListener('click', function () {
    var box = document.getElementById('copy');
    var en = root.getAttribute('data-lang') === 'en';
    var text = en ? box.getAttribute('data-en') : box.getAttribute('data-ko');
    copyText(text, '연락처를 복사했습니다.', 'Contact details copied.');
  });
})();
