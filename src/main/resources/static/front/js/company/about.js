/* =========================================================
   Certificates Slider (axis-lock + 가로 드래그/휠/키보드)
   ========================================================= */
   (function () {
     const sec = document.getElementById('certificates');
     if (!sec || sec.dataset.sliderInit) return; // guard: run once
     sec.dataset.sliderInit = '1';

     const viewport = sec.querySelector('#certViewport');
     const track = sec.querySelector('#certTrack');
     const prevBtn = sec.querySelector('#certPrev');
     const nextBtn = sec.querySelector('#certNext');

     const getStep = () => {
       const first = track.querySelector('.cert');
       const rect = first ? first.getBoundingClientRect() : { width: viewport.clientWidth * 0.8 };
       const cs = getComputedStyle(track);
       const gap = parseFloat(cs.columnGap || cs.gap || '16');
       return rect.width + gap; // 카드 1장 + 간격
     };

     const scrollByStep = (dir) => {
       viewport.scrollBy({ left: dir * getStep(), behavior: 'smooth' });
     };

     const clamp = (n, min, max) => Math.max(min, Math.min(max, n));
     const maxScrollX = (el) => Math.max(0, el.scrollWidth - el.clientWidth);

     const updateButtons = () => {
       const max = viewport.scrollWidth - viewport.clientWidth - 1;
       prevBtn.disabled = viewport.scrollLeft <= 1;
       nextBtn.disabled = viewport.scrollLeft >= max;
     };

     prevBtn.addEventListener('click', () => scrollByStep(-1));
     nextBtn.addEventListener('click', () => scrollByStep(1));

     const initDragScroll = () => {
       let dragging = false;
       let startX = 0, startLeft = 0;
       let pointerId = null;
       const THRESH = 8;

       viewport.addEventListener('pointerdown', (e) => {
         if (e.isPrimary === false || e.button === 2) return;
         dragging = true;
         pointerId = e.pointerId;
         startX = e.clientX;
         startLeft = viewport.scrollLeft;
       });

       viewport.addEventListener('pointermove', (e) => {
         if (!dragging || e.pointerId !== pointerId) return;
         const dx = e.clientX - startX;
         const max = maxScrollX(viewport);
         viewport.scrollLeft = clamp(startLeft - dx, 0, max);
       });

       const endDrag = () => {
         dragging = false;
         pointerId = null;
       };

       viewport.addEventListener('pointerup', endDrag);
       viewport.addEventListener('pointercancel', endDrag);
       viewport.addEventListener('pointerleave', endDrag);
     };

     const initWheelScroll = () => {
       viewport.addEventListener('wheel', (e) => {
         if (Math.abs(e.deltaY) <= Math.abs(e.deltaX)) return;
         const max = maxScrollX(viewport);
         viewport.scrollLeft = clamp(viewport.scrollLeft + e.deltaY, 0, max);
         e.preventDefault();
       }, { passive: false });
     };

     // Initialize Drag Scroll and Wheel Scroll
     initDragScroll();
     initWheelScroll();

     viewport.addEventListener('scroll', updateButtons, { passive: true });
     window.addEventListener('resize', updateButtons, { passive: true });
     updateButtons();
   })();

   (function () {
     const Tabs = () => {
       let root, container, contents, tabs, idSet;

       const cache = () => {
         root = document.querySelector('.bio-history-tabs');
         if (!root) return false;

         container = root.querySelector('.bio-history-tab-container');
         contents = Array.from(root.querySelectorAll('.bio-history-tab-content'));
         tabs = Array.from(root.querySelectorAll('.bio-history-tab-nav a'));
         idSet = new Set(contents.map(c => c.id));

         return container && contents.length && tabs.length;
       };

       const lockContainerHeight = () => {
         if (!container) return;
         const prev = contents.map(c => c.classList.contains("is-active"));
         contents.forEach(c => { c.style.visibility = 'hidden'; c.style.opacity = '1'; c.style.pointerEvents = 'none'; c.style.transform = 'none'; });
         let maxH = 0;
         contents.forEach(c => { maxH = Math.max(maxH, c.scrollHeight); });
         contents.forEach((c, i) => {
           c.style.visibility = ''; c.style.opacity = ''; c.style.pointerEvents = ''; c.style.transform = '';
           c.classList.toggle('is-active', prev[i]);
         });
         container.style.height = maxH + "px";
       };

       const activeContent = () => {
         const hashEl = location.hash && document.querySelector(location.hash);
         return hashEl ? hashEl : contents[0];
       };

       const activateById = (id) => {
         const target = (id && idSet.has(id)) ? root.querySelector(`#${id}`) : activeContent();
         if (!target) return;

         contents.forEach(c => c.classList.toggle("is-active", c === target));
         tabs.forEach(a => {
           const isActive = a.getAttribute('href') === `#${target.id}`;
           a.setAttribute('aria-selected', isActive);
           a.classList.toggle('is-active', isActive);
         });

         container?._recalcProgress?.();
       };

       const init = () => {
         if (!cache()) return;

         const initial = location.hash.slice(1);
         activateById(initial || contents[0].id);

         const relock = () => requestAnimationFrame(lockContainerHeight);
         window.addEventListener("load", relock, { passive: true });
         window.addEventListener("resize", relock, { passive: true });
         setTimeout(relock, 0);

         tabs.forEach(a => a.addEventListener("click", (e) => {
           e.preventDefault();
           const id = a.getAttribute("href").slice(1);
           activateById(id);
           history.replaceState(null, '', a.hash);
         }));

         window.addEventListener("hashchange", () => {
           const id = location.hash.slice(1);
           activateById(id);
         });
       };

       return { init };
     };

     document.addEventListener("DOMContentLoaded", () => {
       Tabs().init();
     });
   })();

   (function () {
     const observeElements = (selector, callback, options = { threshold: 0.5 }) => {
       const observer = new IntersectionObserver(callback, options);
       document.querySelectorAll(selector).forEach(element => observer.observe(element));
     };

     document.addEventListener('DOMContentLoaded', () => {
       // 카드 리빌
       observeElements('.il-image-card__content', (entries, observer) => {
         entries.forEach(entry => {
           if (entry.isIntersecting) {
             setTimeout(() => entry.target.classList.add('visible'), 150);
           } else {
             entry.target.classList.remove('visible');
           }
         });
       });

       // Hero Video Effect
       const hero = document.getElementById('hero');
       const bg = document.getElementById('heroVideo');
       let tickingHero = false;

       const getProgress = () => {
         if (!hero) return 0;
         const rect = hero.getBoundingClientRect();
         const h = rect.height || 1;
         const p = 1 - Math.max(0, Math.min(1, rect.bottom / h));
         return Math.max(0, Math.min(1, p * 1.7));
       };

       const updateHero = () => {
         tickingHero = false;
         const p = getProgress();
         const scale = 0.85 + 1.75 * p;
         const translateX = 700 * p;
         const ty = 1000 * p;
         const blur = 2 * (1 - p);
         if (bg) {
           bg.style.transform = `translate3d(${translateX}px, ${ty}px, 0)`;
           bg.style.transform += ` scale(${scale})`;
           bg.style.filter = `blur(${blur}px)`;
         }
       };

       window.addEventListener('scroll', () => {
         if (!tickingHero) { requestAnimationFrame(updateHero); tickingHero = true; }
       });
       window.addEventListener('resize', updateHero);
     });
   })();

/* ===============================
   Bion History UI (single module)
   =============================== */
(function(){
  "use strict";

  /* ========== Utilities ========== */
  const $  = (sel, root=document) => root.querySelector(sel);
  const $$ = (sel, root=document) => Array.from(root.querySelectorAll(sel));
  const rafThrottle = (fn) => {
    let ticking = false;
    return (...args) => {
      if (ticking) return;
      requestAnimationFrame(() => { fn(...args); ticking = false; });
      ticking = true;
    };
  };
  function freezeScrollWhile(run){
    const sx = window.scrollX, sy = window.scrollY;
    document.documentElement.classList.add('is-switching-tab');
    document.body.classList.add('is-switching-tab');
    run();
    requestAnimationFrame(() => {
      window.scrollTo(sx, sy);
      requestAnimationFrame(() => {
        window.scrollTo(sx, sy);
        document.documentElement.classList.remove('is-switching-tab');
        document.body.classList.remove('is-switching-tab');
      });
    });
  }

  /* ========== 1) History Cards: 내용 파싱(따옴표 → li) ========== */
  function parseHistoryCards(){
    $$("#history .bio-history-card").forEach(card => {
      const yearSpan = $(".bio-history-time__day", card);
      if (yearSpan) {
        const t = yearSpan.textContent.trim();
        if (t.length >= 4) {
          yearSpan.textContent = t.substring(0,4);
          yearSpan.classList.add("hist-year");
        }
      }
      const p = $(".bio-history-card__content p", card);
      if (!p) return;
      let raw = (p.textContent || "").trim();
      if (!raw) return;
      raw = raw.replace(/&quot;/g, '"');
      const quoted = [];
      let m, re = /"([^"]+)"/g;
      while ((m = re.exec(raw)) !== null) quoted.push(m[1].trim());
      const items = quoted.length ? quoted : [raw];
      const ul = document.createElement("ul");
      ul.className = "hist-items";
      items.forEach(it => { const li = document.createElement("li"); li.textContent = it; ul.appendChild(li); });
      p.replaceWith(ul);
    });
  }

  /* ========== 2) Cert Filter (선택) ========== */
  function initCertFilter(){
    const links = $$(".grid-filter a");
    if (!links.length) return;
    const getItems = () => $$("#certTrack > .cert, #certTrack > .cert-viewport");
    const show = (el, on) => { el.style.display = on ? "" : "none"; el.classList.toggle("is-hidden", !on); el.setAttribute("aria-hidden", String(!on)); };
    const apply = (sel) => {
      const isAll = !sel || sel === "*" || sel === "all";
      getItems().forEach(box => {
        const card = box.classList.contains("cert") ? box : $(".cert", box);
        const match = isAll ? true : box.matches(sel) || (card && card.matches(sel));
        show(box, match);
      });
    };
    links.forEach(a => a.addEventListener("click", e => {
      e.preventDefault();
      links.forEach(l => l.parentElement.classList.remove("activeFilter"));
      a.parentElement.classList.add("activeFilter");
      apply(a.dataset.filter || "*");
    }));
    apply("*");
  }

  /* ========== 3) Breadcrumb bottom pin ========== */
  function initBreadcrumbPin(){
    const breadcrumb = $(".breadcrumb-wrap");
    const sentinel   = $("#page-title");
    if (!breadcrumb || !sentinel) return;
    const io = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        breadcrumb.classList.toggle("fixed-bottom", !entry.isIntersecting);
      });
    });
    io.observe(sentinel);
  }

  /* ========== 4) Drag-to-scroll (가로 드래그: 축 잠금) ========== */
  function initDragScroll() {
    $$(".bio-history-timeline").forEach(scroller => {
      let dragging = false;
      let startX = 0, startY = 0, startLeft = 0;
      let lock = null; // 'x' | 'y' | null
      let pointerId = null;
      const THRESH = 8;

      scroller.addEventListener("pointerdown", (e) => {
        if (e.isPrimary === false || e.button === 2) return;
        dragging   = true;
        pointerId  = e.pointerId;
        startX     = e.clientX;
        startY     = e.clientY;
        startLeft  = scroller.scrollLeft;
        lock       = null;
      }, { passive: true });

      scroller.addEventListener("pointermove", (e) => {
        if (!dragging || e.pointerId !== pointerId) return;

        const dx  = e.clientX - startX;
        const dy  = e.clientY - startY;
        const adx = Math.abs(dx);
        const ady = Math.abs(dy);

        if (lock == null) {
          if (adx < THRESH && ady < THRESH) return;
          lock = adx > ady ? 'x' : 'y';
          if (lock === 'x') scroller.setPointerCapture?.(e.pointerId);
        }

        if (lock === 'x') {
          e.preventDefault(); // 세로 스크롤 차단
          scroller.scrollLeft = startLeft - dx;
        } else {
          scroller.releasePointerCapture?.(e.pointerId); // 세로는 브라우저에게
        }
      }, { passive: false });

      const endDrag = (e) => {
        if (!dragging || (pointerId && e.pointerId !== pointerId)) return;
        dragging  = false;
        lock      = null;
        pointerId = null;
        try { scroller.releasePointerCapture?.(e.pointerId); } catch {}
      };

      scroller.addEventListener("pointerup",     endDrag, { passive: true });
      scroller.addEventListener("pointercancel", endDrag, { passive: true });
      scroller.addEventListener("pointerleave",  endDrag, { passive: true });
    });
  }

  /* ========== 5) 진행바 + 활성카드 이동(점/테두리 싱크) ========== */
  function bindProgressFor(container, root){
    const bar = container.querySelector('.hist-progress__bar');
    if (!bar) return;

    const track = bar.parentElement;
    if(!track) return;

    const getActiveContent = () =>
      root.querySelector('.bio-history-tab-content.is-active') ||
      root.querySelector('.bio-history-tab-content');

    const getScroller = () => getActiveContent()?.querySelector('.bio-history-timeline');
    const getPage     = () => getActiveContent()?.querySelector('.bio-history-page');
    const getCards    = () => Array.from(getActiveContent()?.querySelectorAll('.bio-history-card') || []);

    const updateBar = rafThrottle(() => {
      const s = getScroller();
      if (!s) return;
      const max = s.scrollWidth - s.clientWidth;
      bar.style.width = (max > 0 ? (s.scrollLeft / max) * 100 : 0) + "%";
    });

    const clamp01 = (v) => Math.max(0, Math.min(1, v));
    function gotoByRatio(r, smooth = true){
      const s = getScroller();
      if(!s) return;
      const max  = s.scrollWidth - s.clientWidth;
      const left = clamp01(r) * (max <= 0 ? 0 : max);
      s.scrollTo({ left, behavior: smooth ? 'smooth' : 'auto' });
    }

    const updateActiveCard = (() => {
      let ticking = false;
      return () => {
        if (ticking) return;
        requestAnimationFrame(() => {
          const s = getScroller();
          const cards = getCards();
          if (!s || !cards.length) { ticking = false; return; }

          const left   = s.scrollLeft;
          const vw     = s.clientWidth;
          const max    = s.scrollWidth - vw;
          const center = left + vw / 2;
          const EPS    = 2;

          if (left <= EPS) {
            cards.forEach((el, i) => el.classList.toggle('is-active', i === 0));
            ticking = false; return;
          }
          if (left >= max - EPS) {
            cards.forEach((el, i) => el.classList.toggle('is-active', i === cards.length - 1));
            ticking = false; return;
          }

          let best = cards[0], bestDist = Infinity;
          for (const el of cards){
            const mid = el.offsetLeft + el.offsetWidth / 2;
            const d   = Math.abs(mid - center);
            if (d < bestDist){ bestDist = d; best = el; }
          }
          cards.forEach(el => el.classList.toggle('is-active', el === best));
          ticking = false;
        });
        ticking = true;
      };
    })();

    const setVisibility = () => {
      const page = getPage();
      const s = getScroller();
      if (!page || !s) return;
      const over = s.scrollWidth > s.clientWidth + 1;
      page.dataset.progress = over ? "show" : "hidden";
      if (!over) bar.style.width = "0%";
    };

    let userSelecting = false;

    function rebind(){
      container._unbindProgress?.();

      const s = getScroller();
      if (!s) return;

      const onScroll = () => {
        updateBar();
        if (!userSelecting) updateActiveCard();
      };

      const onResize = () => { setVisibility(); updateBar(); updateActiveCard(); };

      s.addEventListener('scroll', onScroll, { passive: true });
      window.addEventListener('resize', onResize, { passive: true });

      container._unbindProgress = () => {
        s.removeEventListener('scroll', onScroll);
        window.removeEventListener('resize', onResize);
      };

      setVisibility();
      updateBar();
      updateActiveCard();

      // 진행바 스크럽
      const onDown = (e) => {
        const rect = track.getBoundingClientRect();
        userSelecting = true;
        const r0 = (e.clientX - rect.left) / rect.width;
        gotoByRatio(r0);
        track.setPointerCapture?.(e.pointerId);

        const onMove = (ev) => {
          const r = (ev.clientX - rect.left) / rect.width;
          gotoByRatio(r, false);
        };
        const onUp = (ev) => {
          userSelecting = false;
          try { track.releasePointerCapture?.(ev.pointerId); } catch {}
          window.removeEventListener('pointermove', onMove);
          window.removeEventListener('pointerup',   onUp);
          window.removeEventListener('pointercancel', onUp);
        };

        window.addEventListener('pointermove',   onMove, { passive: true });
        window.addEventListener('pointerup',     onUp,   { once: true, passive: true });
        window.addEventListener('pointercancel', onUp,   { once: true, passive: true });
      };

      track.addEventListener('pointerdown', onDown, { passive: true });

      // 카드 클릭 → 중앙 정렬
      getCards().forEach(card => {
        if (card._clickedBound) return;
        card._clickedBound = true;
        card.addEventListener('click', () => {
          const sc = getScroller();
          if (!sc) return;
          userSelecting = true;
          const leftForCenter = card.offsetLeft - (sc.clientWidth - card.offsetWidth) / 2;
          const max = sc.scrollWidth - sc.clientWidth;
          const clamped = Math.max(0, Math.min(leftForCenter, max));
          sc.scrollTo({ left: clamped, behavior: 'smooth' });

          getCards().forEach(el => el.classList.toggle('is-active', el === card));
          setTimeout(() => { userSelecting = false; }, 800);

          card.classList.add('just-activated');
          setTimeout(() => card.classList.remove('just-activated'), 400);
        });
      });
    }

    container._recalcProgress = rebind;
    rebind();
  }

  /* ========== 6) Tabs: 내용만 교체 + 진행바 리바인드 ========== */
  const Tabs = (function(){
    let root, container, contents, tabs, idSet;

    function cache(){
      root = document.querySelector('#history .bio-history-tabs') ||
             document.querySelector('.bio-history-wrap .bio-history-tabs');
      if (!root) return false;

      container = root.querySelector('.bio-history-tab-container');
      contents  = Array.from(root.querySelectorAll('.bio-history-tab-content'));
      tabs      = Array.from(root.querySelectorAll('.bio-history-tab-nav a'));
      idSet     = new Set(contents.map(c => c.id));

      return container && contents.length && tabs.length;
    }

    function lockContainerHeight(){
      if (!container) return;
      const prev = contents.map(c => c.classList.contains("is-active"));
      contents.forEach(c => { c.style.visibility='hidden'; c.style.opacity='1'; c.style.pointerEvents='none'; c.style.transform='none'; });
      let maxH = 0;
      contents.forEach(c => { maxH = Math.max(maxH, c.scrollHeight); });
      contents.forEach((c,i) => {
        c.style.visibility=''; c.style.opacity=''; c.style.pointerEvents=''; c.style.transform='';
        c.classList.toggle('is-active', prev[i]);
      });
      container.style.height = maxH + "px";
    }

    function activeContent(){
      const hashEl = location.hash && $(location.hash);
      if (hashEl && hashEl.classList.contains("bio-history-tab-content")) return hashEl;
      return contents[0];
    }

    function activateById(id){
      const target = (id && idSet.has(id)) ? root.querySelector('#'+id) : activeContent();
      if (!target) return;

      contents.forEach(c => c.classList.toggle("is-active", c === target));

      tabs.forEach(a => {
        const isActive = a.getAttribute('href') === '#' + target.id;
        a.setAttribute('aria-selected', isActive);
        a.classList.toggle('is-active', isActive);
      });

      container?._recalcProgress?.();
    }

    function init(){
      if (!cache()) return;

      bindProgressFor(container, root);

      const initial = location.hash && location.hash.slice(1);
      activateById(idSet.has(initial) ? initial : null);

      const relock = () => requestAnimationFrame(lockContainerHeight);
      window.addEventListener("load",   relock, { passive: true });
      window.addEventListener("resize", relock, { passive: true });
      setTimeout(relock, 0);

      tabs.forEach(a => a.addEventListener("click", (e) => {
        e.preventDefault();
        const id = a.getAttribute("href").slice(1);
        freezeScrollWhile(() => {
          activateById(id);
          history.replaceState(null, '', a.hash);
        });
      }));

      window.addEventListener("hashchange", () => {
        const id = location.hash.slice(1);
        freezeScrollWhile(() => { activateById(id); });
      });
    }

    return { init };
  })();

  /* ========== Boot ========== */
  document.addEventListener("DOMContentLoaded", () => {
    parseHistoryCards();
    initCertFilter();
    initBreadcrumbPin();
    initDragScroll();
    Tabs.init();
  });
})();

//  nav top body 스크롤 이벤트 (변경 없음)
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
});

document.addEventListener('DOMContentLoaded', () => {
  const headerEl = document.getElementById('header');
  if (headerEl) {
    document.documentElement.style.setProperty('--header-bottom', headerEl.offsetHeight + 'px');
  }
});

// 카드 리빌(IntersectionObserver)
document.addEventListener('DOMContentLoaded', () => {
  const observer = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        setTimeout(() => {
          entry.target.classList.add('visible');
          observer.unobserve(entry.target);
        }, 150);
      } else {
        entry.target.classList.remove('visible');
      }
    });
  }, { threshold: 0.5 });

  document.querySelectorAll('.il-image-card__content').forEach(card => observer.observe(card));
});

(() => {
  /* =========================
     IntersectionObserver 안전 가드
     ========================= */
  (function () {
    if (!('IntersectionObserver' in window)) return;
    const _observe = IntersectionObserver.prototype.observe;
    IntersectionObserver.prototype.observe = function (target) {
      if (!target || !(target instanceof Element)) return;
      try { return _observe.call(this, target); } catch (e) {}
    };
  })();

  /* =========================
     Hero video scale / translate / blur
     ========================= */
  const hero = document.getElementById('hero');
  const bg   = document.getElementById('heroVideo');
  let tickingHero = false;

  function getProgress() {
    if (!hero) return 0;
    const rect = hero.getBoundingClientRect();
    const h = rect.height || 1;
    const p = 1 - Math.max(0, Math.min(1, rect.bottom / h));
    const speed = 1.7;
    return Math.max(0, Math.min(1, p * speed));
  }

  function updateHero() {
    tickingHero = false;
    const p = getProgress();
    const scale = 0.85 + 1.75 * p;
    const translateX = 700 * p;
    const ty = 1000 * p;
    const blur = 2 * (1 - p);
    if (bg) {
      bg.style.transform = `translate3d(${translateX}px, ${ty}px, 0)`;
      bg.style.transform += ` scale(${scale})`;
      bg.style.filter = `blur(${blur}px)`;
    }
  }
  
  function onScrollHero() {
    if (!tickingHero) { requestAnimationFrame(updateHero); tickingHero = true; }
  }
  updateHero();
  window.addEventListener('scroll', onScrollHero, { passive: true });
  window.addEventListener('resize', onScrollHero);

  /* =========================
     페이지 진입 시 LNB 애니메이션
     ========================= */
  window.addEventListener('DOMContentLoaded', () => {
    document.querySelector('.breadcrumb-wrap')?.classList.add('show');
  });

  /* =========================
     .p-item 패럴랙스 & 리빌
     ========================= */
  const items = Array.from(document.querySelectorAll('.p-item'));
  let lastY = window.scrollY;

  const onScrollItems = () => {
    const y = window.scrollY;
    if (Math.abs(y - lastY) < 2) return;
    lastY = y;

    const vh = window.innerHeight;
    items.forEach((el) => {
      const rect = el.getBoundingClientRect();
      const speed = parseFloat(el.dataset.speed || 0.2);

      const visible = rect.top < vh * 0.86 && rect.bottom > vh * 0.14;
      if (visible) {
        if (!el.classList.contains('show')) {
          void el.offsetWidth;
          el.classList.add('show');
        }
      } else {
        el.classList.remove('show');
      }

      if (rect.bottom > 0 && rect.top < vh) {
        const offset = (rect.top - vh / 2) * speed;
        el.style.setProperty('--py', `${offset}px`);
      }
    });
  };

  let tickingItems = false;
  const rafItems = () => { onScrollItems(); tickingItems = false; };
  window.addEventListener('scroll', () => {
    if (!tickingItems) { tickingItems = true; window.requestAnimationFrame(rafItems); }
  }, { passive: true });
  window.addEventListener('load', onScrollItems);

  /* =========================
     LNB Active State Sync
     ========================= */
  const sections = document.querySelectorAll('main .section[id]');
  const links = document.querySelectorAll('.breadcrumb-wrap a');
  if (sections.length && links.length) {
    const io = new IntersectionObserver((entries) => {
      entries.forEach(e => {
        if (e.isIntersecting) {
          const id = '#' + e.target.id;
          links.forEach(a => a.classList.toggle('is-active', a.getAttribute('href') === id));
        }
      });
    }, { rootMargin: '-40% 0px -50% 0px', threshold: 0 });
    sections.forEach(sec => io.observe(sec));
  }

  /* =========================
     CountUp & helpers
     ========================= */
  function resetCounts(scope){
    scope.querySelectorAll('.countup').forEach(el => { el.dataset.counted = ''; });
    scope.querySelectorAll('.progress__bar').forEach(el => {
      el.dataset.filled = '';
      el.style.transition = 'none';
      el.style.width = '0%';
      void el.offsetWidth;
      el.style.transition = '';
    });
  }
  function triggerCounts(nodes) {
    nodes.forEach((s) => {
      const el = s.querySelector('.countup');
      if (el && !el.dataset.counted) { startCount(el); el.dataset.counted = '1'; }
    });
  }
  function startCount(el) {
    const target = Number(el.getAttribute('data-target')) || 0;
    const suffix = el.getAttribute('data-suffix') || '';
    const dur = 900;
    const t0 = performance.now();
    const start = 0;
    function step(t) {
      const p = Math.min(1, (t - t0) / dur);
      const eased = 1 - Math.pow(1 - p, 3);
      const val = Math.floor(start + (target - start) * eased);
      el.textContent = val.toLocaleString();
      if (p < 1) requestAnimationFrame(step);
      else if (suffix) el.textContent += suffix;
    }
    requestAnimationFrame(step);
  }

  /* =========================
     Custom Indicator (dots + bar)
     ========================= */
  function setupIndicator(sw){
    const indicator = document.querySelector('.glance-indicator');
    if (!indicator) return;
    const dotsWrap = indicator.querySelector('.gi-dots');
    dotsWrap.innerHTML = '';
    const total = sw.slides.length;
    for (let i = 0; i < total; i++){
      const dot = document.createElement('span');
      dot.className = 'dot';
      dot.addEventListener('click', () => sw.slideTo(i));
      dotsWrap.appendChild(dot);
    }
  }
  function renderIndicator(sw){
    const indicator = document.querySelector('.glance-indicator');
    if (!indicator) return;
    const dots = indicator.querySelectorAll('.gi-dots .dot');
    const fill = indicator.querySelector('.gi-fill');
    const total = sw.slides.length;
    const current = sw.activeIndex;
    dots.forEach((d, i) => d.classList.toggle('is-active', i === current));
    const denom = Math.max(1, total - 1);
    fill.style.width = (current / denom * 100) + '%';
  }

  /* =========================
     Glance Swiper (autoplay + toggle)
     ========================= */
  function initGlanceSwiper() {
    if (!window.Swiper) return;

    const sw = new Swiper('.glance-swiper', {
      speed: 600,
      slidesPerView:'auto',
      autoHeight: 'auto',
      loop: false,
      breakpoints: {
        0:   { slidesPerView: 1, centeredSlides: true,  centeredSlidesBounds:true },
        762: { slidesPerView: 1, centeredSlides: false },
        991: { slidesPerView: 1, centeredSlides: true  },
      },
      grabCursor: true,
      pagination: { el: '.glance-swiper .swiper-pagination', clickable: true },
      navigation: { nextEl: '.glance-swiper .swiper-button-next', prevEl: '.glance-swiper .swiper-button-prev' },
      autoplay: { delay: 6000, disableOnInteraction: false, stopOnLastSlide: false },
      on: {
        init() {
          try {
            const slide = this.slides[this.activeIndex];
            if (typeof resetCounts === 'function') resetCounts(slide);
            if (typeof triggerCounts === 'function') triggerCounts([slide]);
            if (typeof setupIndicator === 'function') setupIndicator(this);
            if (typeof renderIndicator === 'function') renderIndicator(this);
          } catch (e) {}

          const btn = document.querySelector('.gi-toggle');
          if (!btn) return;
          const setUI = (running) => {
            btn.setAttribute('aria-pressed', running ? 'false' : 'true');
            btn.classList.toggle("bi-stop-circle", running);
            btn.classList.toggle("bi-play-circle", !running);
          };
          setUI(this.autoplay && this.autoplay.running === true);
          btn.addEventListener('click', () => {
            if (this.autoplay && this.autoplay.running) { this.autoplay.stop(); setUI(false); }
            else { this.autoplay.start(); setUI(true); }
          });
        },
        slideChangeTransitionEnd() {
          try {
            const slide = this.slides[this.activeIndex];
            if (typeof resetCounts === 'function') resetCounts(slide);
            if (typeof triggerCounts === 'function') triggerCounts([slide]);
            if (typeof renderIndicator === 'function') renderIndicator(this);

            this.slides.forEach(s => s.querySelector('.stat-card')?.classList.remove('active'));
            this.slides[this.activeIndex]?.querySelector('.stat-card')?.classList.add('active');
          } catch (e) {}
        }
      }
    });

    const glanceSection = document.querySelector('.glance-swiper');
    if (glanceSection) {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (!sw.autoplay) return;
          if (entry.isIntersecting) {
            sw.autoplay.start();
            try {
              const slide = sw.slides[sw.activeIndex];
              if (typeof resetCounts === 'function') resetCounts(slide);
              if (typeof triggerCounts === 'function') triggerCounts([slide]);
              sw.slides.forEach(s => s.querySelector('.stat-card')?.classList.remove('active'));
              slide.querySelector('.stat-card')?.classList.add('active');
            } catch(e){}
          } else {
            sw.autoplay.stop();
          }
        });
      }, { threshold: 0.5 });
      observer.observe(glanceSection);
    }

    return sw;
  }

  if (!window.Swiper) {
    const s = document.createElement('script');
    s.src = 'https://unpkg.com/swiper@9/swiper-bundle.min.js';
    s.onload = initGlanceSwiper;
    document.head.appendChild(s);
  } else {
    initGlanceSwiper();
  }

  // (선택) 버튼 위치 보정 예시 함수 - 필요 시 호출
 /* function setNavPosition(swiper) {
    const activeSlide = swiper.slides[swiper.activeIndex];
    if (!activeSlide) return;
    const slideWidth = activeSlide.offsetWidth;
    const prevBtn = swiper.el.querySelector('.swiper-button-prev');
    const nextBtn = swiper.el.querySelector('.swiper-button-next');
    if (prevBtn && nextBtn) {
      prevBtn.style.top = slideWidth + 'px';
      nextBtn.style.top = slideWidth + 'px';
    }
  }*/
})();
