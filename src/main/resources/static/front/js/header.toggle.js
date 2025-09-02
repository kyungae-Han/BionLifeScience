document.addEventListener('DOMContentLoaded', () => {
	
  const mqDesktopWidth = window.matchMedia('(min-width: 998px)');  
  const isDesktop = () => mqDesktopWidth.matches;                  

  (function initNav(){
    const nav = document.querySelector('nav.primary-menu');
    if (!nav) return;

    const itemProduct  = nav.querySelector('.menu-item.product');
    const itemBrand    = nav.querySelector('.menu-item.brand');
    const panelProduct = document.getElementById('product-items');
    const panelBrand   = document.getElementById('brands-items');

    let hideTimer = null;
    let handlers = [];

    document.body.classList.add('js-nav');

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
      add(nav, 'pointerleave', ()=>delayedClose());
    };
    const update = () => { detach(); if (isDesktop()) attach(); };

    update();
    window.addEventListener('resize', update);
    mqDesktopWidth.addEventListener?.('change', update);
  })();

  
  (function initDepth(){
    const colBig     = document.getElementById('col-big');
    const colMiddle  = document.getElementById('col-middle');
    const colSmall   = document.getElementById('col-small');
    const colProduct = document.getElementById('col-product');
    if (!colBig || !colMiddle || !colSmall) return;

    let middles = [];
    let smalls = [];
    let products = [];

    const refreshLists = () => {
      middles  = [...colMiddle.querySelectorAll('li')];
      smalls   = [...colSmall.querySelectorAll('li')];
      products = colProduct ? [...colProduct.querySelectorAll('li')] : [];
    };
    refreshLists();

    const setActive = (elList, el) => {
      elList.forEach(li => li.querySelector('.menu-link')?.classList.remove('is-active'));
      el?.querySelector('.menu-link')?.classList.add('is-active');
    };

    const showMiddle = (bigId) => {
      middles.forEach(li => li.classList.toggle('is-visible', li.dataset.parent === String(bigId)));
      const first = middles.find(li => li.dataset.parent === String(bigId));
      setActive([...colBig.querySelectorAll('li')], colBig.querySelector(`[data-id="${bigId}"]`));
      if (first) showSmall(first.dataset.id); else clearSmall();
    };

    const clearSmall = () => {
      smalls.forEach(li => li.classList.remove('is-visible'));
      clearProduct();
    };

    const showSmall = (midId) => {
      smalls.forEach(li => li.classList.toggle('is-visible', li.dataset.parent === String(midId)));
      const first = smalls.find(li => li.dataset.parent === String(midId));
      setActive([...colMiddle.querySelectorAll('li.is-visible')], colMiddle.querySelector(`li[data-id="${midId}"]`));
      if (first) showProduct(first.dataset.id); else clearProduct();
    };

    const clearProduct = () => {
      products.forEach(li => li.classList.remove('is-visible'));
    };

    const showProduct = (smId) => {
      products.forEach(li => li.classList.toggle('is-visible', li.dataset.parent === String(smId)));
      setActive([...colSmall.querySelectorAll('li.is-visible')], colSmall.querySelector(`li[data-id="${smId}"]`));
    };

    const makeDelegates = (container, handler) => {
      const over = (e) => {
        if (!isDesktop()) return;
        const li = e.target.closest('li');
        if(li && container.contains(li)) handler(li.dataset.id);
      };
      const focus = (e) => {
        if (!isDesktop()) return;
        const li = e.target.closest('li');
        if(li && container.contains(li)) handler(li.dataset.id);
      };
      const click = (e) => {
        if (!isDesktop()) return;
        const li = e.target.closest('li');
        if(li && container.contains(li)) handler(li.dataset.id);
      };
      return { over, focus, click };
    };

    let disposers = [];
    const attach = () => {
      if (!isDesktop()) return;

      refreshLists();

      const d1 = makeDelegates(colBig, showMiddle);
      colBig.addEventListener('mouseover', d1.over);
      colBig.addEventListener('focusin',  d1.focus);
      colBig.addEventListener('click',    d1.click);
      disposers.push(()=>{ colBig.removeEventListener('mouseover', d1.over); colBig.removeEventListener('focusin', d1.focus); colBig.removeEventListener('click', d1.click); });

      const d2 = makeDelegates(colMiddle, showSmall);
      colMiddle.addEventListener('mouseover', d2.over);
      colMiddle.addEventListener('focusin',  d2.focus);
      colMiddle.addEventListener('click',    d2.click);
      disposers.push(()=>{ colMiddle.removeEventListener('mouseover', d2.over); colMiddle.removeEventListener('focusin', d2.focus); colMiddle.removeEventListener('click', d2.click); });

      const d3 = makeDelegates(colSmall, showProduct);
      colSmall.addEventListener('mouseover', d3.over);
      colSmall.addEventListener('focusin',  d3.focus);
      colSmall.addEventListener('click',    d3.click);
      disposers.push(()=>{ colSmall.removeEventListener('mouseover', d3.over); colSmall.removeEventListener('focusin', d3.focus); colSmall.removeEventListener('click', d3.click); });

      const firstBig = colBig.querySelector('li');
      if(firstBig) showMiddle(firstBig.dataset.id);
    };
    const detach = () => { disposers.forEach(fn=>fn()); disposers = []; };
    const update = () => { detach(); if (isDesktop()) attach(); };

    update();
    window.addEventListener('resize', update);
    mqDesktopWidth.addEventListener?.('change', update);

    const cols = [colMiddle, colSmall, colProduct].filter(Boolean);
    const mo = new MutationObserver(()=>{ refreshLists(); });
    cols.forEach(col => mo.observe(col, {childList:true, subtree:true}));
  })();
});


(function () {
  const MOBILE_MAX = 998;
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


document.addEventListener('DOMContentLoaded', () => {                  
  const hamburger  = document.getElementById('hamburger-menu-trigger');
  const primaryMenu = document.querySelector('nav.primary-menu .nav.is-mobile .mobile-primary-menu');
  const mqDesktop  = window.matchMedia('(min-width: 998px)');        

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