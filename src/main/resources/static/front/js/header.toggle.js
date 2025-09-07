document.addEventListener('DOMContentLoaded', () => {
	
  const mqDesktopWidth = window.matchMedia('(min-width: 991px)');  
  const isDesktop = () => mqDesktopWidth.matches;                  

  (function initNav(){
    const nav = document.querySelector('nav.primary-menu');
    if (!nav) return;
	

    const itemProduct  = nav.querySelector('.menu-item.product');
    const itemBrand    = nav.querySelector('.menu-item.brand');
    const panelProduct = document.getElementById('product-items');
    const panelBrand   = document.getElementById('brands-items');
	const body = document.get

    let hideTimer = null;
    let handlers = [];

    document.body.classList.add('js-nav');
	
	document.addEventListener('pointerdown', (e) => {
			  if (!isDesktop()) return; // 데스크탑 전용
			  const inProduct = itemProduct?.contains(e.target) || panelProduct?.contains(e.target);
			  const inBrand   = itemBrand?.contains(e.target)   || panelBrand?.contains(e.target);

			  if (!inProduct && !inBrand) {
			    [panelProduct, panelBrand].forEach(p => {
			      if (!p) return;
			      p.style.opacity = '0';
			      p.style.visibility = 'hidden';
			      p.style.pointerEvents = 'none';
			    });
			  }
		});

    const open = (panel) => {
      clearTimeout(hideTimer);
      [panelProduct, panelBrand].forEach(p=>{
        if(!p) return;
        if(p === panel){
          p.style.opacity = '1';
          p.style.visibility = 'visible';
          p.style.pointerEvents = 'auto';
          p.style.transform = 'translateX(-50%)';
        }else{
          p.style.opacity = '0';
          p.style.visibility = 'hidden';
          p.style.pointerEvents = 'none';
        }
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

    const add = (el, type, fn) => {
      el.addEventListener(type, fn);
      handlers.push({el, type, fn});
    };
    const detach = () => {
      handlers.forEach(({el, type, fn}) => el.removeEventListener(type, fn));
      handlers = [];
    };
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
	  
	  const nonTriggers = nav.querySelectorAll(
	     '.is-desktop .menu-container > .menu-item:not(.product):not(.brand)'
	   );
	   nonTriggers.forEach(li => {
	     add(li, 'pointerenter', () => delayedClose(0));
	     add(li, 'focusin',     () => delayedClose(0)); // 키보드 접근 시도
	   });

	   const topBar = nav.querySelector('.is-desktop .menu-container');
	   if (topBar) add(topBar, 'pointerleave', () => delayedClose(0));
	  
      add(nav, 'pointerleave', ()=>delayedClose());
    };
    const update = () => { detach(); if (isDesktop()) attach(); };

    update();
    window.addEventListener('resize', update);
    mqDesktopWidth.addEventListener?.('change', update);
  })();

  
  (function initDepth(){
	const PANEL_IDS = ['product-items', 'brands-items']; // 루트 패널 id만 유지

	 // root 패널 하나를 바인딩
	 function initDepthPanel(root){
	   if (!root) return;

	   // 존재하는 depth만 순서대로 수집 (1~5)
	   const cols = [];
	   for (let d = 1; d <= 5; d++){
	     const ul = root.querySelector(`.col.depth-${d}`);
	     if (ul) cols.push(ul);
	   }
	   if (cols.length < 2) return; // 최소 2열 이상일 때만

	   const setActive = (ul, id) => {
	     if (!ul) return;
	     [...ul.children].forEach(li => li.querySelector('.menu-link')?.classList.remove('is-active'));
	     ul.querySelector(`li[data-id="${id}"] .menu-link`)?.classList.add('is-active');
	   };

	   const clearFrom = (startIdx) => {
	     for (let i = startIdx; i < cols.length; i++) {
	       [...cols[i].children].forEach(li => li.classList.remove('is-visible'));
	     }
	   };

	   // 현재 depth(k)의 항목을 선택하면 다음 depth(k+1)를 필터링해 보여줌
	   const showNext = (nextIdx, parentId) => {
	     if (!cols[nextIdx]) return;
	     [...cols[nextIdx].children].forEach(li => {
	       li.classList.toggle('is-visible', li.dataset.parent === String(parentId));
	     });
	     // active 표시 (현재 depth는 nextIdx-1)
	     setActive(cols[nextIdx - 1], parentId);

	     // 그 아래 depth들은 일단 숨김
	     clearFrom(nextIdx + 1);

	     // 다음 depth에 보이는 첫 항목이 있으면 자동 진행
	     const first = cols[nextIdx].querySelector('li.is-visible');
	     if (first && cols[nextIdx + 1]) {
	       showNext(nextIdx + 1, first.dataset.id);
	     }
	   };

	   // 위임 핸들러: 특정 depth의 UL에서 li를 선택하면 다음 depth를 표시
	   const delegate = (ul, nextIdx) => (e) => {
	     if (!isDesktop()) return;
	     const li = e.target.closest('li');
	     if (!li || !ul.contains(li)) return;
	     showNext(nextIdx, li.dataset.id);
	   };

	   // 이벤트 바인딩: depth-1 → depth-2, depth-2 → depth-3, …
	   const disposers = [];
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
	     // 초기 표시: depth-1의 첫 항목 기준
	     const first = cols[0].querySelector('li');
	     if (first) showNext(1, first.dataset.id);
	   };

	   const detach = () => { disposers.splice(0).forEach(fn => fn()); };
	   const update = () => { detach(); if (isDesktop()) attach(); };

	   update();
	   window.addEventListener('resize', update);
	   mqDesktopWidth.addEventListener?.('change', update);

	   // 데이터 변경 대응 (필요 시)
	   const mo = new MutationObserver(() => { /* 데이터가 바뀌면 다시 초기화하고 싶으면 여기에서 update(); */ });
	   cols.forEach(col => mo.observe(col, { childList: true, subtree: true }));
	 }

	 // 두 패널 모두 적용
	 PANEL_IDS.forEach(id => initDepthPanel(document.getElementById(id)));
  })();
});


(function () {
  const MOBILE_MAX = 991;
  const THRESHOLD  = 0;
  const header     = document.getElementById('header');
  const headerWrap = document.getElementById('header-wrap');
  if (!header || !headerWrap) return;

  let spacer = document.querySelector('.header-wrap-clone');
  if (!spacer) {
    spacer = document.createElement('div');
    spacer.className = 'header-spacer';
    header.after(spacer);
  }

  let sentinel = document.getElementById('shrink-sentinel');
  if (!sentinel) {
    sentinel = document.createElement('div');
    sentinel.id = 'shrink-sentinel';
    sentinel.style.position = 'absolute';
    sentinel.style.top = '1px';
    sentinel.style.height = '1px';
    sentinel.style.width = '1px';
    header.after(sentinel);
  }

  const isMobile = () => window.innerWidth <= MOBILE_MAX;

  function syncSpacer() {
    const h = header.classList.contains('sticky-header') ? headerWrap.offsetHeight : 0;
    spacer.style.height = h + 'px';
  }

  function enterSticky(withShrink){
    if (!header.classList.contains('sticky-header')) {
      header.classList.add('sticky-header');
      header.classList.add('sticky-enter');
      header.offsetHeight;
      header.classList.remove('sticky-enter');
    }
    const on = !!withShrink;
    header.classList.toggle('sticky-header-shrink', on);
    document.body.classList.toggle('header-shrink', on);
    syncSpacer();
  }

  function leaveSticky(){
    header.classList.remove('sticky-header', 'sticky-header-shrink');
    document.body.classList.remove('header-shrink');
    syncSpacer();
  }

  function apply() {
    if (!isMobile()) { leaveSticky(); return; }
    const y = window.scrollY || document.documentElement.scrollTop || 0;
    const on = y > THRESHOLD;
    if (on) enterSticky(true); else leaveSticky();
  }

  if ('IntersectionObserver' in window) {
    const io = new IntersectionObserver(([e]) => {
      if (header.classList.contains('sticky-header')) {
        const on = !e.isIntersecting;
        header.classList.toggle('sticky-header-shrink', on);
        document.body.classList.toggle('header-shrink', on);
        syncSpacer();
      }
    }, { rootMargin: "-1px 0px 0px 0px", threshold: 0 });
    io.observe(sentinel);
  }

  let ticking = false;
  function onScroll() {
    if (ticking) return;
    ticking = true;
    requestAnimationFrame(() => { apply(); ticking = false; });
  }

  document.addEventListener('scroll', onScroll, { passive: true });
  window.addEventListener('resize', apply);
  document.addEventListener('DOMContentLoaded', apply);
  apply();
})();



/*
document.addEventListener('DOMContentLoaded', () => {                  
  const hamburger  = document.getElementById('hamburger-menu-trigger');
  const primaryMenu = document.querySelector('nav.primary-menu .nav.is-mobile .mobile-primary-menu');
  const mqDesktop  = window.matchMedia('(min-width: 991px)');        

  if (!hamburger || !primaryMenu) return;
 
  

  function resetForDesktop() {
    if (!mqDesktop.matches) return;
    hamburger.classList.remove('active');
    hamburger.setAttribute('aria-expanded', 'false');
    primaryMenu.classList.remove('open');
    primaryMenu.style.removeProperty('display');   
    primaryMenu.style.removeProperty('visibility');
    primaryMenu.style.removeProperty('pointer-events');
  }

  hamburger.addEventListener('click', () => {
    if (mqDesktop.matches) return;
    const expanded = hamburger.getAttribute('aria-expanded') === 'true';
    hamburger.classList.toggle('active');
    hamburger.setAttribute('aria-expanded', String(!expanded));

    const willOpen = !primaryMenu.classList.contains('open');
    primaryMenu.classList.toggle('open', willOpen);
    if (willOpen) primaryMenu.style.display = 'block';
    else          primaryMenu.style.removeProperty('display');
  });

  resetForDesktop();
  mqDesktop.addEventListener?.('change', resetForDesktop);
  window.addEventListener('resize', resetForDesktop);
});

*/


document.addEventListener('DOMContentLoaded', () => {                  
  const hamburger   = document.getElementById('hamburger-menu-trigger');
  // 구조에 따라 fallback 하나 더 둠
  const primaryMenu = document.querySelector('nav.primary-menu .nav.is-mobile .mobile-primary-menu')
                    || document.querySelector('nav.primary-menu .nav.is-mobile .menu-container');
  const mqDesktop   = window.matchMedia('(min-width: 991px)');

  if (!hamburger || !primaryMenu) return;

  // 데스크톱으로 돌아오면 초기 상태로
  function resetForDesktop() {
    if (!mqDesktop.matches) return;
    hamburger.classList.remove('active');
    hamburger.setAttribute('aria-expanded', 'false');
    primaryMenu.classList.remove('open');
    primaryMenu.style.removeProperty('display');
    primaryMenu.style.removeProperty('visibility');
    primaryMenu.style.removeProperty('pointer-events');
    // 펼친 표시도 정리(선택)
    primaryMenu.querySelectorAll('.menu-link[aria-expanded="true"]')
               .forEach(a => a.setAttribute('aria-expanded','false'));
  }

  // 햄버거 토글 (기존 로직 유지)
  hamburger.addEventListener('click', () => {
    if (mqDesktop.matches) return;
    const expanded = hamburger.getAttribute('aria-expanded') === 'true';
    const willOpen = !expanded;

    hamburger.classList.toggle('active', willOpen);
    hamburger.setAttribute('aria-expanded', String(willOpen));

    primaryMenu.classList.toggle('open', willOpen);
    if (willOpen) primaryMenu.style.display = 'block';
    else          primaryMenu.style.removeProperty('display');
  });

  // 아코디언: 하위 UL이 있는 항목만 링크 이동을 막고 펼침/접힘
  primaryMenu.addEventListener('click', (e) => {
    if (mqDesktop.matches) return;

    // 토글 트리거: 링크/아이콘/버튼 등
    const trigger = e.target.closest('.menu-link, .sub-menu-trigger, .toggle, .chevron, button');
    if (!trigger || !primaryMenu.contains(trigger)) return;

    const li = trigger.closest('li');
    if (!li) return;

    // 직계 하위 리스트(테마에 따라 클래스가 다를 수 있음)
    const childList = li.querySelector(':scope > ul, :scope > .mega-menu-content, :scope > .sub-menu-container');
    if (!childList) return; // 자식 없으면 기본 이동

    // 자식 있으면 아코디언 토글
    //e.preventDefault();
    const willOpen = trigger.getAttribute('aria-expanded') !== 'true';

    // 같은 레벨 형제 닫기(원하면 유지해도 됨)
    [...li.parentElement.children].forEach(sib => {
      const a = sib.querySelector(':scope > .menu-link[aria-expanded="true"]');
      a && a.setAttribute('aria-expanded', 'false');
    });

    trigger.setAttribute('aria-expanded', String(willOpen));
  });

  resetForDesktop();
  mqDesktop.addEventListener?.('change', resetForDesktop);
  window.addEventListener('resize', resetForDesktop);
});
