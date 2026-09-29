/*
 * 페이지 내 내비게이션(.breadcrumb-wrap) 스크롤 동작.
 *
 * 회사소개(about.js)에만 있던 것을 R&D 화면과 같이 쓰려고 따로 뺐다.
 * 같은 뜻을 가진 화면 요소는 화면마다 다르게 움직이면 안 된다.
 *
 * 규칙
 *  - 맨 위에서는 보인다
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
  let isHidden      = false;
  let idleTimer     = null;

  const AT_TOP_EPS = 1;
  const IDLE_MS    = 1800;

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

    if (atTop() || inHeaderZone()) {
      setFixedBottom(false);
      setHidden(false);
    } else {
      setFixedBottom(true);
      setHidden(false);
    }

    clearTimeout(idleTimer);
    idleTimer = setTimeout(() => {
      if (footerVisible) { setHidden(true); return; }
      if (atTop() || inHeaderZone()) setHidden(false);
      else setHidden(true);
    }, IDLE_MS);
  }
  window.addEventListener("scroll", onScroll, { passive: true });

  function sync() {
    if (atTop() || inHeaderZone()) setFixedBottom(false);
    else setFixedBottom(true);
    setHidden(false);
  }
  window.addEventListener("load",   sync, { passive: true });
  window.addEventListener("resize", sync, { passive: true });

  // 들어올 때 한 번 떠오르는 효과. 회사소개에서 하던 것과 같다
  breadcrumb.classList.add('show');
});
