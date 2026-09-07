/**
 * 데모 데이터 생성 스크립트
 *   npm run db:seed
 *
 * 조직 1개 / 프로젝트 1개 / 키워드 6개와 최근 30일치 순위 이력을 만든다.
 * 로그인: demo@rankradar.test / demo1234
 */
import { PrismaClient, type SearchEngine } from "@prisma/client";
import bcrypt from "bcryptjs";

const prisma = new PrismaClient();

const DEMO_EMAIL = "demo@rankradar.test";
const DEMO_PASSWORD = "demo1234";
const TARGET_DOMAIN = "example.com";

const KEYWORDS: { text: string; engines: SearchEngine[] }[] = [
  { text: "실험실 소모품", engines: ["NAVER_WEB", "GOOGLE", "DAUM_WEB"] },
  { text: "세포배양 배지", engines: ["NAVER_WEB", "NAVER_BLOG", "GOOGLE"] },
  { text: "원심분리기 가격", engines: ["NAVER_WEB", "GOOGLE"] },
  { text: "실시간 PCR 장비", engines: ["NAVER_WEB", "GOOGLE", "DAUM_WEB"] },
  { text: "바이오 시약 공급업체", engines: ["NAVER_WEB", "NAVER_BLOG"] },
  { text: "연구용 항체", engines: ["GOOGLE", "DAUM_WEB"] },
];

function kstDateOnly(base: Date): Date {
  const kst = new Date(base.getTime() + 9 * 3600 * 1000);
  return new Date(Date.UTC(kst.getUTCFullYear(), kst.getUTCMonth(), kst.getUTCDate()));
}

function random(seed: number): number {
  const x = Math.sin(seed) * 10000;
  return x - Math.floor(x);
}

function hash(value: string): number {
  let h = 2166136261;
  for (let i = 0; i < value.length; i += 1) {
    h ^= value.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return Math.abs(h);
}

async function main() {
  const org = await prisma.organization.upsert({
    where: { slug: "demo-org" },
    update: {},
    create: { name: "데모 컴퍼니", slug: "demo-org", plan: "PRO" },
  });

  await prisma.user.upsert({
    where: { email: DEMO_EMAIL },
    update: { orgId: org.id },
    create: {
      orgId: org.id,
      email: DEMO_EMAIL,
      name: "데모 관리자",
      role: "OWNER",
      passwordHash: await bcrypt.hash(DEMO_PASSWORD, 12),
    },
  });

  let project = await prisma.project.findFirst({
    where: { orgId: org.id, targetDomain: TARGET_DOMAIN },
  });
  if (!project) {
    project = await prisma.project.create({
      data: {
        orgId: org.id,
        name: "메인 사이트",
        targetDomain: TARGET_DOMAIN,
        memo: "데모용 프로젝트",
      },
    });
  }

  const today = kstDateOnly(new Date());

  for (const item of KEYWORDS) {
    const keyword = await prisma.keyword.upsert({
      where: { projectId_text: { projectId: project.id, text: item.text } },
      update: { engines: item.engines },
      create: { projectId: project.id, text: item.text, engines: item.engines },
    });

    for (const engine of item.engines) {
      // src/lib/collector/demo.ts 와 동일한 계산식이라 이후 수동 수집과 자연스럽게 이어진다
      const seed = hash(`${item.text}|${engine}|${TARGET_DOMAIN}`);
      const base = 3 + (seed % 38);

      for (let back = 29; back >= 0; back -= 1) {
        const checkedOn = new Date(today.getTime() - back * 86_400_000);
        const day = Math.floor(checkedOn.getTime() / 86_400_000);
        const wave = Math.round(5 * Math.sin(day / 4 + (seed % 100) / 16));
        const jitter = Math.round(random(seed + day) * 5) - 2;
        const rankRaw = base + wave + jitter;
        const rank = rankRaw < 1 ? 1 : rankRaw > 100 ? null : rankRaw;

        await prisma.rankSnapshot.upsert({
          where: {
            keywordId_engine_checkedOn: { keywordId: keyword.id, engine, checkedOn },
          },
          update: { rank },
          create: {
            keywordId: keyword.id,
            engine,
            checkedOn,
            rank,
            source: "DEMO",
            foundUrl: rank ? `https://${TARGET_DOMAIN}/products` : null,
            foundTitle: rank ? `${item.text} | ${TARGET_DOMAIN}` : null,
          },
        });
      }
    }
  }

  console.log(`데모 데이터 생성 완료 — ${DEMO_EMAIL} / ${DEMO_PASSWORD}`);
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
