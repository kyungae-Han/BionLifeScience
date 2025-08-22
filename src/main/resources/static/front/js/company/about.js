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
    if (!hero) return 0;                           // ✅ null 가드
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
	      bg.style.transform = `translate3d(${translateX}px, ${ty}px, 0) /*scale(${scale})*/`;
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
          void el.offsetWidth;            // 강제 리플로우
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
     Certificates swipe buttons + snap helper
     ========================= */
  const viewport = document.getElementById('certViewport');
  const prev = document.getElementById('certPrev');
  const next = document.getElementById('certNext');
  if (viewport && prev && next) {
    const cardWidth = () => viewport.querySelector('.cert')?.offsetWidth || 280;
    const updateBtns = () => {
      const maxScroll = viewport.scrollWidth - viewport.clientWidth - 2;
      prev.disabled = viewport.scrollLeft <= 2;
      next.disabled = viewport.scrollLeft >= maxScroll;
    };
    prev.addEventListener('click', () => {
      viewport.scrollBy({ left: -cardWidth() - 16, behavior: 'smooth' });
      setTimeout(updateBtns, 350);
    });
    next.addEventListener('click', () => {
      viewport.scrollBy({ left:  cardWidth() + 16, behavior: 'smooth' });
      setTimeout(updateBtns, 350);
    });
    viewport.addEventListener('scroll', updateBtns, { passive: true });
    window.addEventListener('load', updateBtns);
  }

  /* =========================
     CountUp & helpers
     ========================= */
  function resetCounts(scope){
    scope.querySelectorAll('.countup').forEach(el => { el.dataset.counted = ''; });
    scope.querySelectorAll('.progress__bar').forEach(el => {
      el.dataset.filled = '';
      el.style.transition = 'none';
      el.style.width = '0%';              // ✅ 단위 %
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
    const current = sw.activeIndex; // 0-base
    dots.forEach((d, i) => d.classList.toggle('is-active', i === current));
    const denom = Math.max(1, total - 1);
    fill.style.width = (current / denom * 100) + '%';
  }

  /* =========================
  Glance Swiper (autoplay + toggle pause/resume)
  ========================= */
function initGlanceSwiper() {
 if (!window.Swiper) return;

 const sw = new Swiper('.glance-swiper', {
   speed: 600,
   spaceBetween: 24,
   centeredSlides: true,
   slidesPerView:'auto',
   loop: false,
   slidesOffsetBefore: 24,
   slidesOffsetAfter: 24,
   grabCursor: true,
   pagination: { el: '.glance-swiper .swiper-pagination', clickable: true },
   navigation: {
     nextEl: '.glance-swiper .swiper-button-next',
     prevEl: '.glance-swiper .swiper-button-prev'
   },
   autoplay: {
     delay: 6000,
     disableOnInteraction: false, // 조작 후에도 계속
     stopOnLastSlide: false
   },
   on: {
     init() {
       try {
         const slide = this.slides[this.activeIndex];
         if (typeof resetCounts === 'function') resetCounts(slide);
         if (typeof triggerCounts === 'function') triggerCounts([slide]);
         if (typeof setupIndicator === 'function') setupIndicator(this);
         if (typeof renderIndicator === 'function') renderIndicator(this);
       } catch (e) {}

       // ▶/⏸ 토글: autoplay만 stop/start
       const btn = document.querySelector('.gi-toggle');
       if (!btn) return;

       const setUI = (running) => {
         btn.setAttribute('aria-pressed', running ? 'false' : 'true');
         btn.classList.toggle("pause-circle", running)
         btn.classList.toggle("play-circle", !running);
       };

       setUI(this.autoplay && this.autoplay.running === true); // 초기 상태 반영

       btn.addEventListener('click', () => {
         if (this.autoplay && this.autoplay.running) {
           this.autoplay.stop();
           setUI(false);
         } else {
           this.autoplay.start();
           setUI(true);
         }
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
 
		if(glanceSection){
			const observer = new IntersectionObserver((entries) => {
				entries.forEach(entry => {
					if(!sw.autoplay) return;
					
					if(entry.isIntersecting){
						sw.autoplay.start();
						
						try {
							const slide = sw.slides[sw.activeIndex];
							if(typeof resetCounts === 'function') resetCounts(slide);
							if(typeof triggerCounts === 'function') triggerCounts([slide]);
							
							 sw.slides.forEach(s => s.querySelector('.stat-card')?.classList.remove('active'));
					          slide.querySelector('.stat-card')?.classList.add('active');
							
						} catch(e){
							console.error('GlanceSwiper error:', e);
						}
						
					}else{
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
  
//버튼 위치 업데이트 함수
  function setNavPosition(swiper) {
    // 현재 중앙 슬라이드 찾기
    const activeSlide = swiper.slides[swiper.activeIndex];
    if (!activeSlide) return;

    const slideWidth = activeSlide.offsetWidth;

    // 버튼 엘리먼트 가져오기
    const prevBtn = swiper.el.querySelector('.swiper-button-prev');
    const nextBtn = swiper.el.querySelector('.swiper-button-next');

    if (prevBtn && nextBtn) {
      // 예시: 버튼을 슬라이드 width 기준으로 세팅
      prevBtn.style.top = slideWidth + 'px'; 
      nextBtn.style.top = slideWidth + 'px';
    }
  }
})();