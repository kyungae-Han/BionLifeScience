/*
 * 페이지 내 내비게이션(.breadcrumb-wrap) 스크롤 동작.
 *
 * 회사소개(about.js)에만 있던 것을 R&D 화면과 같이 쓰려고 따로 뺐다.
 * 같은 뜻을 가진 화면 요소는 화면마다 다르게 움직이면 안 된다.
 *
 * 규칙
 *  - 맨 위에서는 보인다
 *    (마크업에 data-hide-at-top="true" 가 붙어 있으면 맨 위에서는 숨긴다)
 *  - 스크롤하는 동안 보인다
 *  - 스크롤이 멈추고 1.8초가 지나면 사라진다
 *  - 바닥(푸터가 보이면)에서는 사라진다
 *
 * 보이고 사라지는 모양은 company.css 의 .hide / .gone / .fixed-bottom 이 맡는다.
 */
document.addEventListener("DOMContentLoaded", () => {
  const breadcrumb = document.querySelector(".breadcrumb-wrap");
  const footer     = document.querySelector("footer");
  const header     = document.querySelector("#header") || document.querySelector(".header-wrap");
  if (!breadcrumb) return;

  let footerVisible = false;
  // 처음 상태는 마크업을 보고 정한다. 맨 위에서 숨기는 화면은 숨은 채로 그려진다.
  let isHidden      = breadcrumb.classList.contains('hide');
  let idleTimer     = null;

  const AT_TOP_EPS = 1;
  const IDLE_MS    = 1800;

  // 화면에 따라 맨 위에서 숨기고 싶을 때가 있다 (R&D 는 상단이 큰 그림이라 겹친다).
  // 표시가 없으면 지금까지와 똑같이 맨 위에서 보인다.
  const hideAtTop = breadcrumb.dataset.hideAtTop === 'true';

  const atTop = () => window.scrollY <= AT_TOP_EPS;
  const headerH = () => (header ? header.offsetHeight || 0 : 0);
  const inHeaderZone = () => window.scrollY <= Math.max(0, headerH() - AT_TOP_EPS);

  function setFixedBottom(toBottom) {
    breadcrumb.classList.toggle("fixed-bottom", !!toBottom);
  }

  function setHidden(nextHidden) {
    if (isHidden === nextHidden) return;
    isHidden = nextHidden;
    if (!nextHidden) {
      breadcrumb.classList.remove('gone');
      requestAnimationFrame(() => breadcrumb.classList.remove("hide"));
    } else {
      breadcrumb.classList.add("hide");
    }
  }

  breadcrumb.addEventListener('transitionend', (e) => {
    if (isHidden && (e.propertyName === 'opacity' || e.propertyName === 'transform')) {
      breadcrumb.classList.add('gone');
      // 아래에 붙여 두던 자리는 다 사라진 뒤에 되돌린다.
      // 먼저 떼면 원래 자리로 한 번 튄 다음 사라져서 눈에 거슬린다.
      if (hideAtTop && (atTop() || inHeaderZone())) setFixedBottom(false);
    }
  });

  if (footer) {
    const io = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        footerVisible = entry.isIntersecting;
        if (footerVisible) setHidden(true);
      }
    }, { threshold: 0 });
    io.observe(footer);
  }

  function onScroll() {
    if (footerVisible) { setHidden(true); return; }

    const top = atTop() || inHeaderZone();

    if (top && hideAtTop) {
      // 숨기기만 하고 자리는 그대로 둔다. 되돌리는 일은 transitionend 가 맡는다.
      setHidden(true);
      if (breadcrumb.classList.contains('gone')) setFixedBottom(false);
    } else {
      setFixedBottom(!top);
      setHidden(false);
    }

    clearTimeout(idleTimer);
    idleTimer = setTimeout(() => {
      if (footerVisible) { setHidden(true); return; }
      if (atTop() || inHeaderZone()) setHidden(hideAtTop);
      else setHidden(true);
    }, IDLE_MS);
  }
  window.addEventListener("scroll", onScroll, { passive: true });

  function sync() {
    const top = atTop() || inHeaderZone();
    if (top && hideAtTop) {
      setHidden(true);
      if (breadcrumb.classList.contains('gone')) setFixedBottom(false);
    } else {
      setFixedBottom(!top);
      setHidden(false);
    }
  }
  window.addEventListener("load",   sync, { passive: true });
  window.addEventListener("resize", sync, { passive: true });

  // 들어올 때 한 번 떠오르는 효과. 회사소개에서 하던 것과 같다
  breadcrumb.classList.add('show');
});
