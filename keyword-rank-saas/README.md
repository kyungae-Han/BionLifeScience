# 랭크레이더 (keyword-rank-saas)

네이버·구글·다음 **검색어 순위를 매일 추적**하는 멀티테넌트 SaaS.
Next.js 15 (App Router) 풀스택 · TypeScript · Prisma · PostgreSQL.

---

## 1. 무엇이 들어있나

| 영역 | 내용 |
|---|---|
| 랜딩/요금제 | `/`, `/pricing` — 가입 유도용 마케팅 페이지 |
| 인증 | 회원가입(조직 자동 생성) · 로그인 · 로그아웃, JWT 세션 쿠키(HttpOnly) |
| 멀티테넌트 | 조직(Organization) → 사용자/프로젝트/키워드. 모든 조회에 `orgId` 격리 적용 |
| 권한 | OWNER(결제·조직) / ADMIN(프로젝트·키워드) / MEMBER(조회 전용) |
| 프로젝트 | 추적 대상 도메인 단위. 대행사는 고객사별로 분리 운영 |
| 키워드 | 여러 줄·쉼표로 일괄 등록, 키워드별 검색영역 선택 |
| 순위 수집 | 네이버 웹문서/블로그, 구글, 다음 웹문서. 수동 + 매일 자동(cron) |
| 대시보드 | KPI 타일, 키워드×검색영역 순위표, 상승/하락 표시 |
| 키워드 상세 | 30일 추이 선그래프(호버 툴팁) + 수집 이력 표 |
| 요금제 한도 | 프로젝트/키워드/검색영역/팀원/일일 수동수집 횟수 서버측 강제 |
| 내보내기 | CSV (Pro 이상) |

---

## 2. 로컬 실행

```bash
cp .env.example .env      # 값 채우기 (아래 3번 참고)
npm install
npx prisma migrate dev    # 또는 prisma/sql/001_init.sql 직접 실행
npm run db:seed           # (선택) 데모 데이터 30일치 생성
npm run dev               # http://localhost:3000
```

시드 계정: `demo@rankradar.test` / `demo1234`

---

## 3. 환경변수

| 변수 | 필수 | 설명 |
|---|---|---|
| `DATABASE_URL` | ✅ | PostgreSQL 접속 URL |
| `AUTH_SECRET` | ✅ | 세션 JWT 서명키. **32자 이상**. `openssl rand -base64 48` |
| `CRON_SECRET` | ✅ | 자동 수집 엔드포인트 인증키. `openssl rand -hex 32` |
| `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` | – | [네이버 개발자센터](https://developers.naver.com) 검색 API |
| `KAKAO_REST_API_KEY` | – | [카카오 developers](https://developers.kakao.com) 검색 API (다음) |
| `GOOGLE_API_KEY` / `GOOGLE_SEARCH_ENGINE_ID` | – | Google Programmable Search (Custom Search JSON API) |
| `GOOGLE_MAX_RANK` | – | 구글 조회 깊이. 기본 30 (10위 단위로 API 1회 소모) |
| `ALLOW_SIGNUP` | – | `false` 로 두면 신규 가입 차단 |

> **API 키가 없어도 동작합니다.** 키가 없는 검색엔진은 자동으로 *데모 모드*로 떨어져
> (키워드+엔진+날짜 기준의) 결정적 가짜 순위를 만들어 냅니다. 화면·차트·요금제 로직을
> 그대로 확인할 수 있고, 키를 넣는 순간 실제 순위로 바뀝니다. 설정 화면에서 각 엔진의
> 연동 상태(`실제 API 연동됨` / `데모 데이터`)를 확인할 수 있습니다.

---

## 4. DB 마이그레이션

Prisma 마이그레이션을 쓰지 않고 직접 SQL 을 실행하려면:

```bash
psql "$DATABASE_URL" -f prisma/sql/001_init.sql
```

`prisma/sql/001_init.sql` 은 스키마 전체 DDL(테이블 8개 + enum 5개 + 인덱스)입니다.
이후 스키마를 바꿀 때는 아래로 증분 SQL 을 뽑아 쓰세요.

```bash
npx prisma migrate diff \
  --from-url "$DATABASE_URL" \
  --to-schema-datamodel prisma/schema.prisma \
  --script > prisma/sql/002_xxx.sql
```

### MariaDB/MySQL 로 쓰고 싶다면
`prisma/schema.prisma` 의 `provider` 를 `mysql` 로 바꾸고, `SearchEngine[]` 배열 필드는
MySQL 이 배열을 지원하지 않으므로 `keyword_engine` 조인 테이블로 분리해야 합니다.
(그 외 로직은 그대로 사용 가능)

---

## 5. 자동 수집 (cron)

`GET /api/cron/collect` 를 하루 1회 호출하면 **자동 수집 요금제(Pro 이상)** 조직의
모든 활성 키워드를 수집합니다.

```bash
curl -H "Authorization: Bearer $CRON_SECRET" https://<도메인>/api/cron/collect
```

- **Vercel**: `vercel.json` 에 이미 등록되어 있습니다(매일 06:00 KST = 21:00 UTC).
  프로젝트 환경변수에 `CRON_SECRET` 을 넣어두면 Vercel 이 자동으로
  `Authorization: Bearer` 헤더를 붙여 호출합니다.
- **일반 서버**: crontab 에 위 `curl` 한 줄을 등록하면 됩니다.

같은 날 여러 번 호출해도 `(키워드, 검색영역, 날짜)` 기준으로 upsert 하므로
행이 중복되지 않고 최신값으로 갱신됩니다.

---

## 6. 순위 판정 방식과 한계

- 검색결과의 링크 호스트가 프로젝트의 **추적 도메인과 같거나 서브도메인**이면 노출로 판정합니다
  (`www.` 와 프로토콜은 무시).
- 조회 범위 밖이면 순위를 `null`(미노출)로 저장합니다. 그래프에서는 선이 끊깁니다.
- **네이버 통합검색(스마트블록)의 실제 노출 순서와는 다를 수 있습니다.** 공식 검색 API 는
  웹문서/블로그 등 영역별 결과를 돌려주기 때문입니다. 통합검색 순위가 꼭 필요하면
  `src/lib/collector/naver.ts` 를 HTML 파싱 방식으로 교체해야 하며, 이 경우 네이버
  이용약관·robots 정책을 반드시 먼저 확인하세요.
- 구글은 Custom Search JSON API 무료 할당량이 **하루 100쿼리**입니다. 기본 조회 깊이가
  30위(=3쿼리/키워드)이므로 무료로는 하루 약 33개 키워드가 한계입니다. 더 필요하면
  유료 결제 또는 Search Console API 연동을 고려하세요.

---

## 7. 주요 디렉터리

```
prisma/
  schema.prisma        데이터 모델
  sql/001_init.sql     전체 DDL (직접 실행용)
  seed.ts              데모 데이터
src/
  app/
    page.tsx           랜딩
    pricing/           요금제
    login/ signup/     인증 화면
    app/               로그인 후 대시보드 (layout/page/projects/keywords/settings)
    api/               REST 엔드포인트
  components/          UI 컴포넌트 (차트·표·폼)
  lib/
    auth.ts session.ts 인증·세션
    plan.ts limits.ts  요금제 정의와 한도 강제
    rank.ts            수집 실행 + 조회용 집계
    collector/         검색엔진 어댑터 (naver/google/daum/demo)
  middleware.ts        /app/* 세션 가드
```

---

## 8. 아직 붙이지 않은 것 (다음 단계 후보)

- 결제(PG) 연동 — 지금은 소유자가 설정에서 등급을 직접 바꿉니다.
  `PATCH /api/org/plan` 을 결제 성공 웹훅에서 호출하도록 바꾸면 됩니다.
- 비밀번호 재설정 메일, 팀원 초대 메일
- 순위 급락 알림(메일/슬랙)
- 경쟁사 도메인 비교 추적
