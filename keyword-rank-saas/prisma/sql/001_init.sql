-- CreateSchema
CREATE SCHEMA IF NOT EXISTS "public";

-- CreateEnum
CREATE TYPE "PlanTier" AS ENUM ('FREE', 'PRO', 'BUSINESS');

-- CreateEnum
CREATE TYPE "MemberRole" AS ENUM ('OWNER', 'ADMIN', 'MEMBER');

-- CreateEnum
CREATE TYPE "SearchEngine" AS ENUM ('NAVER_WEB', 'NAVER_BLOG', 'GOOGLE', 'DAUM_WEB');

-- CreateEnum
CREATE TYPE "RankSource" AS ENUM ('API', 'DEMO');

-- CreateEnum
CREATE TYPE "RunStatus" AS ENUM ('RUNNING', 'SUCCESS', 'FAILED');

-- CreateTable
CREATE TABLE "organization" (
    "id" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "slug" TEXT NOT NULL,
    "plan" "PlanTier" NOT NULL DEFAULT 'FREE',
    "planSince" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "organization_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "app_user" (
    "id" TEXT NOT NULL,
    "orgId" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "passwordHash" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "role" "MemberRole" NOT NULL DEFAULT 'MEMBER',
    "active" BOOLEAN NOT NULL DEFAULT true,
    "lastLoginAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "app_user_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "project" (
    "id" TEXT NOT NULL,
    "orgId" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "targetDomain" TEXT NOT NULL,
    "memo" TEXT,
    "active" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "project_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "keyword" (
    "id" TEXT NOT NULL,
    "projectId" TEXT NOT NULL,
    "text" TEXT NOT NULL,
    "engines" "SearchEngine"[],
    "active" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "keyword_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "rank_snapshot" (
    "id" TEXT NOT NULL,
    "keywordId" TEXT NOT NULL,
    "engine" "SearchEngine" NOT NULL,
    "checkedOn" DATE NOT NULL,
    "checkedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "rank" INTEGER,
    "foundUrl" TEXT,
    "foundTitle" TEXT,
    "source" "RankSource" NOT NULL DEFAULT 'API',

    CONSTRAINT "rank_snapshot_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "collect_run" (
    "id" TEXT NOT NULL,
    "orgId" TEXT,
    "status" "RunStatus" NOT NULL DEFAULT 'RUNNING',
    "triggeredBy" TEXT NOT NULL DEFAULT 'cron',
    "totalCount" INTEGER NOT NULL DEFAULT 0,
    "okCount" INTEGER NOT NULL DEFAULT 0,
    "failCount" INTEGER NOT NULL DEFAULT 0,
    "message" TEXT,
    "startedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "finishedAt" TIMESTAMP(3),

    CONSTRAINT "collect_run_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "organization_slug_key" ON "organization"("slug");

-- CreateIndex
CREATE UNIQUE INDEX "app_user_email_key" ON "app_user"("email");

-- CreateIndex
CREATE INDEX "app_user_orgId_idx" ON "app_user"("orgId");

-- CreateIndex
CREATE INDEX "project_orgId_idx" ON "project"("orgId");

-- CreateIndex
CREATE INDEX "keyword_projectId_idx" ON "keyword"("projectId");

-- CreateIndex
CREATE UNIQUE INDEX "keyword_projectId_text_key" ON "keyword"("projectId", "text");

-- CreateIndex
CREATE INDEX "rank_snapshot_keywordId_checkedOn_idx" ON "rank_snapshot"("keywordId", "checkedOn");

-- CreateIndex
CREATE UNIQUE INDEX "rank_snapshot_keywordId_engine_checkedOn_key" ON "rank_snapshot"("keywordId", "engine", "checkedOn");

-- CreateIndex
CREATE INDEX "collect_run_orgId_startedAt_idx" ON "collect_run"("orgId", "startedAt");

-- AddForeignKey
ALTER TABLE "app_user" ADD CONSTRAINT "app_user_orgId_fkey" FOREIGN KEY ("orgId") REFERENCES "organization"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project" ADD CONSTRAINT "project_orgId_fkey" FOREIGN KEY ("orgId") REFERENCES "organization"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "keyword" ADD CONSTRAINT "keyword_projectId_fkey" FOREIGN KEY ("projectId") REFERENCES "project"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "rank_snapshot" ADD CONSTRAINT "rank_snapshot_keywordId_fkey" FOREIGN KEY ("keywordId") REFERENCES "keyword"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "collect_run" ADD CONSTRAINT "collect_run_orgId_fkey" FOREIGN KEY ("orgId") REFERENCES "organization"("id") ON DELETE SET NULL ON UPDATE CASCADE;

