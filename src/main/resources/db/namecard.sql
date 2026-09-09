-- =====================================================================
-- 디지털 명함 (name_card / name_card_document)
--
-- 이 프로젝트는 스키마를 하이버네이트가 만들지 않는다 (ddl-auto 없음).
-- 배포 전에 운영 DB(bionls)에 이 파일을 한 번 실행해야 명함 기능이 뜬다.
--
--   mysql -u bionls -p bionls < namecard.sql
-- =====================================================================

CREATE TABLE IF NOT EXISTS name_card (
    name_card_id    BIGINT       NOT NULL AUTO_INCREMENT,
    slug            VARCHAR(60)  NOT NULL COMMENT '명함 주소. 사내 메일 아이디와 같다',
    use_yn          CHAR(1)      NOT NULL DEFAULT 'Y',

    name_ko         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100)          DEFAULT NULL,
    title_ko        VARCHAR(60)           DEFAULT NULL,
    title_en        VARCHAR(100)          DEFAULT NULL,
    dept_ko         VARCHAR(200)          DEFAULT NULL,
    dept_en         VARCHAR(200)          DEFAULT NULL,
    company_ko      VARCHAR(200)          DEFAULT NULL,
    company_en      VARCHAR(200)          DEFAULT NULL,

    mobile          VARCHAR(40)           DEFAULT NULL,
    tel             VARCHAR(40)           DEFAULT NULL,
    fax             VARCHAR(40)           DEFAULT NULL,
    email           VARCHAR(200)          DEFAULT NULL,

    address_ko      VARCHAR(500)          DEFAULT NULL,
    address_en      VARCHAR(500)          DEFAULT NULL,
    map_query_ko    VARCHAR(300)          DEFAULT NULL,
    map_query_en    VARCHAR(300)          DEFAULT NULL,

    website_url     VARCHAR(300)          DEFAULT NULL,
    catalog_url     VARCHAR(300)          DEFAULT NULL,
    catalog_note_ko VARCHAR(200)          DEFAULT NULL,
    catalog_note_en VARCHAR(200)          DEFAULT NULL,

    photo_road      VARCHAR(255)          DEFAULT NULL,
    photo_path      VARCHAR(255)          DEFAULT NULL,
    photo_name      VARCHAR(255)          DEFAULT NULL,
    og_road         VARCHAR(255)          DEFAULT NULL,
    og_path         VARCHAR(255)          DEFAULT NULL,
    og_name         VARCHAR(255)          DEFAULT NULL,

    show_docs_yn    CHAR(1)      NOT NULL DEFAULT 'N',
    biz_no          VARCHAR(40)           DEFAULT NULL,

    show_qr_yn      CHAR(1)      NOT NULL DEFAULT 'N',
    qr_url          VARCHAR(500)          DEFAULT NULL,
    qr_caption_ko   VARCHAR(300)          DEFAULT NULL,
    qr_caption_en   VARCHAR(300)          DEFAULT NULL,

    created_at      DATETIME              DEFAULT NULL,
    updated_at      DATETIME              DEFAULT NULL,

    PRIMARY KEY (name_card_id),
    UNIQUE KEY uk_name_card_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS name_card_document (
    name_card_document_id BIGINT      NOT NULL AUTO_INCREMENT,
    name_card_id          BIGINT      NOT NULL,
    doc_type              VARCHAR(40) NOT NULL COMMENT '허용된 서류 종류만 들어간다',
    file_path             VARCHAR(500) NOT NULL COMMENT '웹으로 바로 열리지 않는 폴더의 실제 경로',
    file_name             VARCHAR(255)         DEFAULT NULL,
    file_size             BIGINT               DEFAULT NULL,
    sort_index            INT                  DEFAULT 0,

    PRIMARY KEY (name_card_document_id),
    KEY idx_name_card_document_card (name_card_id),
    CONSTRAINT fk_name_card_document_card FOREIGN KEY (name_card_id)
        REFERENCES name_card (name_card_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 지금 쓰고 있는 명함 (optomation.dev/card/ 에 있던 것) 을 그대로 옮긴다.
-- 사업자등록증 PDF 는 파일이라 여기서 넣을 수 없다.
-- 관리자 > 디지털 명함 관리 에서 올린 뒤 "거래 서류 영역" 을 켜 주세요.
-- ---------------------------------------------------------------------
INSERT INTO name_card (
    slug, use_yn, name_ko, name_en, title_ko, title_en, dept_ko, dept_en,
    company_ko, company_en, mobile, tel, fax, email,
    address_ko, address_en, map_query_ko, map_query_en,
    website_url, catalog_url, catalog_note_ko, catalog_note_en,
    photo_road, show_docs_yn, biz_no, show_qr_yn, qr_url, qr_caption_ko, qr_caption_en,
    created_at, updated_at
) VALUES (
    'dh_jang', 'Y', '장동훈', 'DongHoon Jang', '부장', 'General Manager',
    '광학솔루션사업부', 'Optical Solution Business Department',
    '(주)바이온라이프사이언스', 'Bion Lifescience, Inc.',
    '010-5541-0988', '031-572-1992', '031-572-2273', 'dh_jang@bionlifescience.com',
    '12106 경기도 남양주시 순화궁로 282\n에이스하이엔드타워 1006호 (별내동)',
    '#1006, 10th Fl., Ace High-End Tower,\n282 Sunhwagung-ro, Namyangju-si,\nGyeonggi-do 12106, Republic of Korea',
    '경기도 남양주시 순화궁로 282',
    '282 Sunhwagung-ro, Namyangju-si, Gyeonggi-do, Korea',
    'https://www.bionlifescience.com', 'https://optomation.dev',
    '광학 제품 카탈로그', 'Optical products catalog',
    '/front/image/namecard/dh_jang.jpg',
    'N', '209-81-56186',
    'Y', 'https://bionlifescience.com/brandList/own/73',
    'Photonic Lattice · 편광·위상차 실시간 가시화\nQR 을 찍으면 제품 페이지가 열립니다',
    'Photonic Lattice · real-time birefringence imaging\nScan to open the product page',
    NOW(), NOW()
);
