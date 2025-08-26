/*<![CDATA[*/
(function () {
  // ===== 0) 서버 템플릿에서 주입 (없으면 null)
  var menuList = /*[[${menuList}]]*/ null;

  // ===== 1) 유틸: 안전 로그
  const log = (...args) => console.log('[MENU]', ...args);

  // ===== 2) 인덱싱 & 트리빌드
  function indexById(list, pickId = x => x.id) {
    const map = new Map();
    (list || []).forEach(item => map.set(pickId(item), item));
    return map;
  }

  function buildTrees(raw) {
    if (!raw) return null;

    // --- 원본 리스트들
    const {
      // 브랜드 계열
      brandList = [],
      brandBigSortList = [],
      brandMiddleSortList = [],
      brandSmallSortList = [],
      brandProductList = [],
      // 일반 제품 계열
      bigSortList = [],
      middleSortList = [],
      smallSortList = [],
      productList = []
    } = raw;

    // --- 인덱스(참조용 맵)
    const idx = {
      brand: indexById(brandList),
      bBig: indexById(brandBigSortList),
      bMid: indexById(brandMiddleSortList),
      bSml: indexById(brandSmallSortList),
      bPrd: indexById(brandProductList),

      gBig: indexById(bigSortList),
      gMid: indexById(middleSortList),
      gSml: indexById(smallSortList),
      gPrd: indexById(productList)
    };

    // --- 브랜드 트리
    const brandTree = brandList.map(br => ({
      ...br,
      bigSorts: []
    }));

    const brandRef = indexById(brandTree);

    brandBigSortList.forEach(bbs => {
      const brId = bbs?.brand?.id;
      const brNode = brandRef.get(brId);
      if (!brNode) return;
      (brNode.bigSorts ||= []).push({ ...bbs, middleSorts: [] });
    });

    // 빠른 접근을 위해 bigSort 레벨도 인덱싱
    const brandBigRef = new Map();
    brandTree.forEach(br => {
      (br.bigSorts || []).forEach(bb => brandBigRef.set(bb.id, bb));
    });

    brandMiddleSortList.forEach(bms => {
      const bbId = bms?.bigSort?.id;
      const bbNode = brandBigRef.get(bbId);
      if (!bbNode) return;
      (bbNode.middleSorts ||= []).push({ ...bms, smallSorts: [] });
    });

    const brandMidRef = new Map();
    brandBigRef.forEach(bb => (bb.middleSorts || []).forEach(mm => brandMidRef.set(mm.id, mm)));

    brandSmallSortList.forEach(bss => {
      const midId = bss?.middleSort?.id;
      const midNode = brandMidRef.get(midId);
      if (!midNode) return;
      (midNode.smallSorts ||= []).push({ ...bss, products: [] });
    });

    const brandSmallRef = new Map();
    brandMidRef.forEach(mm => (mm.smallSorts || []).forEach(ss => brandSmallRef.set(ss.id, ss)));

    brandProductList.forEach(bp => {
      const smId = bp?.smallSort?.id;
      const smNode = brandSmallRef.get(smId);
      if (!smNode) return;
      (smNode.products ||= []).push(bp);
    });

    // --- 일반 트리
    const generalTree = bigSortList.map(big => ({
      ...big,
      middleSorts: []
    }));
    const gBigRef = indexById(generalTree);

    middleSortList.forEach(mid => {
      const bigId = mid?.bigSort?.id;
      const bigNode = gBigRef.get(bigId);
      if (!bigNode) return;
      (bigNode.middleSorts ||= []).push({ ...mid, smallSorts: [] });
    });

    const gMidRef = new Map();
    generalTree.forEach(bg => (bg.middleSorts || []).forEach(mm => gMidRef.set(mm.id, mm)));

    smallSortList.forEach(sm => {
      const midId = sm?.middleSort?.id;
      const midNode = gMidRef.get(midId);
      if (!midNode) return;
      (midNode.smallSorts ||= []).push({ ...sm, products: [] });
    });

    const gSmlRef = new Map();
    gMidRef.forEach(mm => (mm.smallSorts || []).forEach(ss => gSmlRef.set(ss.id, ss)));

    productList.forEach(p => {
      const smId = p?.smallSort?.id;
      const smNode = gSmlRef.get(smId);
      if (!smNode) return;
      (smNode.products ||= []).push(p);
    });

    return { brandTree, generalTree, index: idx };
  }

  // ===== 3) 빠른 조회 헬퍼 (View/이벤트에서 활용)
  const API = {
    getBrandById(tree, brandId) {
      return (tree?.brandTree || []).find(b => String(b.id) === String(brandId)) || null;
    },
    getBigSortsOfBrand(tree, brandId) {
      return this.getBrandById(tree, brandId)?.bigSorts || [];
    },
    getMiddleSorts(tree, bigSortId) {
      // 브랜드/일반 공용: bigSortId가 브랜드 big인지 일반 big인지 모두 탐색
      let node = null;
      (tree?.brandTree || []).some(br => {
        node = (br.bigSorts || []).find(bb => String(bb.id) === String(bigSortId));
        return !!node;
      });
      if (node) return node.middleSorts || [];
      (tree?.generalTree || []).some(gb => {
        node = (gb.middleSorts || []).find(mm => String(mm.id) === String(bigSortId));
        return !!node;
      });
      return node?.middleSorts || [];
    },
    getSmallSortsByMiddle(tree, middleId) {
      let node = null;
      (tree?.brandTree || []).some(br =>
        (br.bigSorts || []).some(bb =>
          (bb.middleSorts || []).some(mm => {
            if (String(mm.id) === String(middleId)) { node = mm; return true; }
            return false;
          })
        )
      );
      if (node) return node.smallSorts || [];
      (tree?.generalTree || []).some(gb =>
        (gb.middleSorts || []).some(mm => {
          if (String(mm.id) === String(middleId)) { node = mm; return true; }
          return false;
        })
      );
      return node?.smallSorts || [];
    },
    getProductsBySmall(tree, smallId) {
      let node = null;
      (tree?.brandTree || []).some(br =>
        (br.bigSorts || []).some(bb =>
          (bb.middleSorts || []).some(mm =>
            (mm.smallSorts || []).some(ss => {
              if (String(ss.id) === String(smallId)) { node = ss; return true; }
              return false;
            })
          )
        )
      );
      if (node) return node.products || [];
      (tree?.generalTree || []).some(gb =>
        (gb.middleSorts || []).some(mm =>
          (mm.smallSorts || []).some(ss => {
            if (String(ss.id) === String(smallId)) { node = ss; return true; }
            return false;
          })
        )
      );
      return node?.products || [];
    }
  };

  // ===== 4) 콘솔 출력(디버깅용)
  function printTrees(tree) {
    if (!tree) return;
    log('=== 브랜드 제품 계층 ===');
    (tree.brandTree || []).forEach(br => {
      log(`브랜드: ${br.name} (${br.id})`);
      (br.bigSorts || []).forEach(bb => {
        log(`  └ 대분류: ${bb.name} (${bb.id})`);
        (bb.middleSorts || []).forEach(mid => {
          log(`    └ 중분류: ${mid.name} (${mid.id})`);
          (mid.smallSorts || []).forEach(sm => {
            log(`      └ 소분류: ${sm.name} (${sm.id})`);
            (sm.products || []).forEach(p => {
              log(`        └ 제품: ${p.subject} (${p.id})`);
            });
          });
        });
      });
    });

    log('=== 일반 제품 계층 ===');
    (tree.generalTree || []).forEach(big => {
      log(`대분류: ${big.name} (${big.id})`);
      (big.middleSorts || []).forEach(mid => {
        log(`  └ 중분류: ${mid.name} (${mid.id})`);
        (mid.smallSorts || []).forEach(sm => {
          log(`    └ 소분류: ${sm.name} (${sm.id})`);
          (sm.products || []).forEach(p => {
            log(`      └ 제품: ${p.subject} (${p.id})`);
          });
        });
      });
    });
  }

  // ===== 5) AJAX: 기존 함수 유지 + Promise 버전 추가
  function getBrandList() {
    return $.ajax({ url: '/api/brandProduct/brandList', type: 'GET' });
  }
  function getBrandBigSortList(brandId) {
    return $.ajax({ url: '/api/brandProduct/bigSortList', data: { brandId } });
  }
  function getBrandMiddleSortList(bigSortId) {
    return $.ajax({ url: '/api/brandProduct/middleSortList', data: { bigSortId } });
  }
  function getBrandSmallSortList(middleSortId) {
    return $.ajax({ url: '/api/brandProduct/smallSortList', data: { middleSortId } });
  }
  function getBrandProductList(smallSortId) {
    return $.ajax({ url: '/api/brandProduct/productList', data: { smallSortId } });
  }

  function getBigSortList() {
    return $.ajax({ url: '/api/product/bigSortList', type: 'GET' });
  }
  function getProductMiddleSortList(bigSortId) {
    return $.ajax({ url: '/api/product/middleSortList', data: { bigSortId } });
  }
  function getProductSmallSortList(middleSortId) {
    return $.ajax({ url: '/api/product/smallSortList', data: { middleSortId } });
  }
  function getProductList(smallSortId) {
    return $.ajax({ url: '/api/product/productList', data: { smallSortId } });
  }

  // ===== 6) 초기화: menuList가 있으면 즉시 트리화, 없으면 개별 호출(옵션)
  let TREE = null;

  function hydrateFromServerListsIfNeeded() {
    // 서버에서 한 번에 menuList를 주지 않는 페이지라면
    // 필요한 조합을 여기서 모아 트리 구성 가능(예: Promise.all로 합치기)
    // 현재는 샘플로 대분류 2개만 미리 로드해두는 형태. 필요 시 확장하세요.
    return $.when(getBigSortList(), getBrandList())
      .then((gBigRes, brRes) => {
        log('[사전 로드] 일반 대분류/브랜드', gBigRes[0], brRes[0]);
        // 더 내려가려면 여기에 추가 호출/병합
        // 일괄 menuList 객체 형태로 만들어 buildTrees 호출
        const partialMenuList = {
          brandList: brRes[0] || [],
          brandBigSortList: [],
          brandMiddleSortList: [],
          brandSmallSortList: [],
          brandProductList: [],
          bigSortList: gBigRes[0] || [],
          middleSortList: [],
          smallSortList: [],
          productList: []
        };
        TREE = buildTrees(partialMenuList);
        printTrees(TREE);
      });
  }

  if (!menuList) {
    log('menuList 데이터가 없습니다. (AJAX로 부분 하이드레이션 진행 가능)');
    hydrateFromServerListsIfNeeded();
  } else {
    TREE = buildTrees(menuList);
    printTrees(TREE);
  }

  // ===== 7) 전역 디버그 노출 (개발 중 편의)
  window.MENU_DEBUG = {
    TREE: () => TREE,
    API,
    // 기존 개별 함수도 필요하면 노출
    getBrandList, getBrandBigSortList, getBrandMiddleSortList, getBrandSmallSortList, getBrandProductList,
    getBigSortList, getProductMiddleSortList, getProductSmallSortList, getProductList
  };

})();
 /*]]>*/