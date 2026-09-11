/* =====================================================================
   메인 팝업. 2026-09-11

   - 이미지가 두 장 이상이면 정해진 간격으로 넘긴다
   - '오늘 하루 열지 않기' 를 누르면 자정까지 쿠키로 막는다
   - 쿠키 이름에 팝업 번호가 들어간다. 팝업을 바꾸면 다시 뜬다
   ===================================================================== */
(function () {
	'use strict';

	var box = document.querySelector('.bion-popup');
	if (!box) {
		return;
	}

	var id = box.getAttribute('data-popup-id');
	var cookieName = 'BION_POPUP_' + id;

	// 오늘 하루 닫아 둔 팝업이면 아예 그리지 않는다
	if (readCookie(cookieName)) {
		box.remove();
		return;
	}

	box.hidden = false;

	// 한 프레임 기다렸다가 제자리로 보낸다.
	// 바로 붙이면 브라우저가 시작 자리를 그리기 전에 끝나서 안 미끄러진다.
	requestAnimationFrame(function () {
		requestAnimationFrame(function () { box.classList.add('is-in'); });
	});

	// ── 이미지 넘기기 ──────────────────────────────────────────────
	var slides = Array.prototype.slice.call(box.querySelectorAll('.bion-popup-slide'));
	var dots = Array.prototype.slice.call(box.querySelectorAll('.bion-popup-dot'));
	var interval = parseInt(box.getAttribute('data-roll'), 10);
	var at = 0;
	var timer = null;

	function show(index) {
		at = (index + slides.length) % slides.length;
		slides.forEach(function (s, i) { s.classList.toggle('on', i === at); });
		dots.forEach(function (d, i) {
			d.classList.toggle('on', i === at);
			d.setAttribute('aria-current', i === at ? 'true' : 'false');
		});
	}

	function start() {
		if (slides.length < 2 || !interval || interval < 500) {
			return;
		}
		if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
			return;
		}
		stop();
		timer = window.setInterval(function () { show(at + 1); }, interval);
	}

	function stop() {
		if (timer) {
			window.clearInterval(timer);
			timer = null;
		}
	}

	dots.forEach(function (dot, i) {
		dot.addEventListener('click', function () { show(i); start(); });
	});

	// 마우스를 올리고 있는 동안에는 넘기지 않는다. 읽는 중에 바뀌면 곤란하다
	box.addEventListener('mouseenter', stop);
	box.addEventListener('mouseleave', start);

	show(0);
	start();

	// ── 닫기 ───────────────────────────────────────────────────────
	var today = box.querySelector('.bion-popup-today');
	var close = box.querySelector('.bion-popup-close');

	if (today) {
		today.addEventListener('click', function () {
			writeCookieUntilMidnight(cookieName, '1');
			dismiss();
		});
	}

	if (close) {
		close.addEventListener('click', dismiss);
	}

	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape') {
			dismiss();
		}
	});

	function dismiss() {
		stop();
		box.remove();
	}

	// ── 쿠키 ───────────────────────────────────────────────────────
	function readCookie(name) {
		return document.cookie.split('; ').some(function (row) {
			return row.indexOf(name + '=') === 0;
		});
	}

	/** 오늘 자정까지만 산다. 날이 바뀌면 다시 뜬다 */
	function writeCookieUntilMidnight(name, value) {
		var midnight = new Date();
		midnight.setHours(24, 0, 0, 0);
		document.cookie = name + '=' + value
			+ '; expires=' + midnight.toUTCString()
			+ '; path=/; SameSite=Lax';
	}
})();
