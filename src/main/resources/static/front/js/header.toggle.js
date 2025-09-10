/* =======================================================
 * Desktop only: Mega menu open/close + depth hover
 * ======================================================= */
document.addEventListener('DOMContentLoaded', () => {
  const mqDesktop = window.matchMedia('(min-width: 991px)');
  const isDesktop = () => mqDesktop.matches;

  /* 1) 상단 GNB에서 제품/브랜드 패널 열고 닫기 */
  (function initNav(){
    const nav = document.querySelector('nav.primary-menu');
    if (!nav) return;

    const itemProduct  = nav.querySelector('.menu-item.product');
    const itemBrand    = nav.querySelector('.menu-item.brand');
    const panelProduct = document.getElementById('product-items');
    const panelBrand   = document.getElementById('brands-items');

    let hideTimer = null;
    let handlers = [];

    const open = (panel) => {
      clearTimeout(hideTimer);
      [panelProduct, panelBrand].forEach(p=>{
        if(!p) return;
        const on = p === panel;
        p.style.opacity        = on ? '1'   : '0';
        p.style.visibility     = on ? 'visible' : 'hidden';
        p.style.pointerEvents  = on ? 'auto': 'none';
        if (on) p.style.transform = 'translateX(-50%)';
      });
    };

    const delayedClose = (delay=180) => {
      clearTimeout(hideTimer);
      hideTimer = setTimeout(()=>{
        [panelProduct, panelBrand].forEach(p=>{
          if(!p) return;
          p.style.opacity = '0';
          p.style.visibility = 'hidden';
          p.style.pointerEvents = 'none';
        });
      }, delay);
    };

    const add = (el, type, fn) => { el.addEventListener(type, fn); handlers.push({el, type, fn}); };
    const detach = () => { handlers.forEach(({el, type, fn}) => el.removeEventListener(type, fn)); handlers = []; };

    const attach = () => {
      if (!isDesktop()) return;

      if (itemProduct && panelProduct){
        add(itemProduct,  'pointerenter', ()=>open(panelProduct));
        add(panelProduct, 'pointerenter', ()=>clearTimeout(hideTimer));
        add(panelProduct, 'pointerleave', ()=>delayedClose());
        add(itemProduct,  'click', (e)=>{ e.preventDefault(); open(panelProduct); });
      }

      if (itemBrand && panelBrand){
        add(itemBrand,    'pointerenter', ()=>open(panelBrand));
        add(panelBrand,   'pointerenter', ()=>clearTimeout(hideTimer));
        add(panelBrand,   'pointerleave', ()=>delayedClose());
        add(itemBrand,    'click', (e)=>{ e.preventDefault(); open(panelBrand); });
      }

      // 다른 GNB 항목으로 가면 즉시 닫기
      const nonTriggers = nav.querySelectorAll('.is-desktop .menu-container > .menu-item:not(.product):not(.brand)');
      nonTriggers.forEach(li => {
        add(li, 'pointerenter', () => delayedClose(0));
        add(li, 'focusin',     () => delayedClose(0));
      });

      const topBar = nav.querySelector('.is-desktop .menu-container');
      if (topBar) add(topBar, 'pointerleave', () => delayedClose(0));

      // 바깥 클릭 시 닫기
      add(document, 'pointerdown', (e) => {
        if (!isDesktop()) return;
        const inProduct = itemProduct?.contains(e.target) || panelProduct?.contains(e.target);
        const inBrand   = itemBrand?.contains(e.target)   || panelBrand?.contains(e.target);
        if (!inProduct && !inBrand) delayedClose(0);
      });

      // nav 영역을 벗어나면 닫기
      add(nav, 'pointerleave', ()=>delayedClose());
    };

    const update = () => { detach(); if (isDesktop()) attach(); };
    update();
    window.addEventListener('resize', update);
    mqDesktop.addEventListener?.('change', update);
  })();

  /* 2) Mega menu 내부 다단(depthed) 호버 연동 */
  (function initDepth(){
    const PANEL_IDS = ['product-items', 'brands-items'];

    function initDepthPanel(root){
      if (!root) return;
      const cols = [];
      for (let d = 1; d <= 5; d++){
        const ul = root.querySelector(`.col.depth-${d}`);
        if (ul) cols.push(ul);
      }
      if (cols.length < 2) return;

      const setActive = (ul, id) => {
        if (!ul) return;
        [...ul.children].forEach(li => li.querySelector('.menu-link')?.classList.remove('is-active'));
        ul.querySelector(`li[data-id="${id}"] .menu-link`)?.classList.add('is-active');
      };

      const clearFrom = (startIdx) => {
        for (let i = startIdx; i < cols.length; i++)
          [...cols[i].children].forEach(li => li.classList.remove('is-visible'));
      };

      const showNext = (nextIdx, parentId) => {
        if (!cols[nextIdx]) return;
        [...cols[nextIdx].children].forEach(li => {
          li.classList.toggle('is-visible', li.dataset.parent === String(parentId));
        });
        setActive(cols[nextIdx - 1], parentId);
        clearFrom(nextIdx + 1);

        const first = cols[nextIdx].querySelector('li.is-visible');
        if (first && cols[nextIdx + 1]) showNext(nextIdx + 1, first.dataset.id);
      };

      const disposers = [];
      const delegate = (ul, nextIdx) => (e) => {
        if (!isDesktop()) return;
        const li = e.target.closest('li');
        if (!li || !ul.contains(li)) return;
        showNext(nextIdx, li.dataset.id);
      };

      const attach = () => {
        if (!isDesktop()) return;
        for (let i = 0; i < cols.length - 1; i++){
          const ul = cols[i];
          const fn = delegate(ul, i + 1);
          ul.addEventListener('mouseover', fn);
          ul.addEventListener('focusin',  fn);
          ul.addEventListener('click',    fn);
          disposers.push(()=>{ ul.removeEventListener('mouseover', fn); ul.removeEventListener('focusin', fn); ul.removeEventListener('click', fn); });
        }
        const first = cols[0].querySelector('li');
        if (first) showNext(1, first.dataset.id);
      };

      const detach = () => { disposers.splice(0).forEach(fn => fn()); };
      const update = () => { detach(); if (isDesktop()) attach(); };

      update();
      window.addEventListener('resize', update);
      mqDesktop.addEventListener?.('change', update);
    }

    PANEL_IDS.forEach(id => initDepthPanel(document.getElementById(id)));
  })();
});


// ==== Mobile-only: Hamburger toggle + Accordion ====
document.addEventListener('DOMContentLoaded', () => {
  const mqDesktop = window.matchMedia('(min-width: 991px)');
  const isDesktop = () => mqDesktop.matches;

  const hamburger = document.getElementById('hamburger-menu-trigger');
  const primaryMenu =
    document.querySelector('nav.primary-menu .nav.is-mobile .mobile-primary-menu') ||
    document.querySelector('nav.primary-menu .nav.is-mobile .menu-container');

  if (!hamburger || !primaryMenu) return;

  // 데스크톱 복귀 시 초기화
  function resetForDesktop() {
    if (!isDesktop()) return;
    hamburger.classList.remove('active');
    hamburger.setAttribute('aria-expanded', 'false');
    primaryMenu.classList.remove('open');
    primaryMenu.style.removeProperty('display');
    primaryMenu.style.removeProperty('visibility');
    primaryMenu.style.removeProperty('pointer-events');
    primaryMenu.querySelectorAll('.menu-link[aria-expanded="true"]')
      .forEach(a => a.setAttribute('aria-expanded', 'false'));
  }

  // 햄버거 버튼 토글 (모바일 전용)
  hamburger.addEventListener('click', () => {
    if (isDesktop()) return;
    const willOpen = hamburger.getAttribute('aria-expanded') !== 'true';
    hamburger.classList.toggle('active', willOpen);
    hamburger.setAttribute('aria-expanded', String(willOpen));
    primaryMenu.classList.toggle('open', willOpen);
    if (willOpen) primaryMenu.style.display = 'block';
    else          primaryMenu.style.removeProperty('display');
  });

  // 아코디언: 자식 UL/패널이 있는 항목만 펼침/접힘 (모바일 전용)
  primaryMenu.addEventListener('click', (e) => {
    if (isDesktop()) return;

    // 트리거: 링크/아이콘/버튼 등
    const trigger = e.target.closest('.menu-link, .sub-menu-trigger, .toggle, .chevron, button');
    if (!trigger || !primaryMenu.contains(trigger)) return;

    const li = trigger.closest('li');
    if (!li) return;

    // 직계 하위 컨테이너(테마별 클래스 대응)
    const childList = li.querySelector(':scope > ul, :scope > .mega-menu-content, :scope > .sub-menu-container');
    if (!childList) return; // 자식 없으면 기본 링크 이동

    // 자식 있으면 아코디언 토글
    e.preventDefault();
    const willOpen = trigger.getAttribute('aria-expanded') !== 'true';

    // 같은 레벨 형제 닫기(원치 않으면 이 블록을 주석 처리)
    [...li.parentElement.children].forEach(sib => {
      const a = sib.querySelector(':scope > .menu-link[aria-expanded="true"]');
      a && a.setAttribute('aria-expanded', 'false');
    });

    trigger.setAttribute('aria-expanded', String(willOpen));
  });

  // 바깥 클릭 시 닫기(모바일 전용, 선택사항)
  document.addEventListener('pointerdown', (e) => {
    if (isDesktop()) return;
    if (!primaryMenu.contains(e.target) && e.target !== hamburger && !hamburger.contains(e.target)) {
      hamburger.classList.remove('active');
      hamburger.setAttribute('aria-expanded', 'false');
      primaryMenu.classList.remove('open');
      primaryMenu.style.removeProperty('display');
    }
  });

  // 브레이크포인트 전환 시 초기화
  resetForDesktop();
  mqDesktop.addEventListener?.('change', resetForDesktop);
  window.addEventListener('resize', resetForDesktop);
});

// ==== Mobile-only sticky class toggles (no spacer touches) ====
document.addEventListener('DOMContentLoaded', () => {
  const MOBILE_MAX = 991;              // 모바일 기준 폭
  const SHRINK_AT  = 1;                // 몇 px 이상 스크롤 시 shrink 적용
  const header = document.getElementById('header');
  if (!header) return;

  const isMobile = () => window.innerWidth <= MOBILE_MAX;
  const getY = () => (document.scrollingElement || document.documentElement).scrollTop || 0;

  let ticking = false;
  function apply() {
    if (!isMobile()) {
      // 데스크톱: 항상 해제
      header.classList.remove('sticky-header', 'sticky-header-shrink');
      document.body.classList.remove('header-shrink');
      return;
    }

    const y = getY();
    const onSticky = y > 0; 
    const onShrink = y > SHRINK_AT; 

    header.classList.toggle('sticky-header', onSticky);
    header.classList.toggle('sticky-header-shrink', onShrink && onSticky);
    document.body.classList.toggle('header-shrink', onShrink && onSticky);
  }

  function onScroll() {
    if (ticking) return;
    ticking = true;
    requestAnimationFrame(() => { apply(); ticking = false; });
  }

  // 초기 상태 확정 + 리스너
  apply();
  document.addEventListener('scroll', onScroll, { passive: true });
  window.addEventListener('resize', apply);
});

