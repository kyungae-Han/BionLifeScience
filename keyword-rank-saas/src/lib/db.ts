import { PrismaClient } from "@prisma/client";

// Next.js dev 모드의 HMR 로 커넥션이 계속 늘어나는 것을 막기 위한 싱글턴
const globalForPrisma = globalThis as unknown as { prisma?: PrismaClient };

export const prisma =
  globalForPrisma.prisma ??
  new PrismaClient({
    log: process.env.NODE_ENV === "development" ? ["warn", "error"] : ["error"],
  });

if (process.env.NODE_ENV !== "production") globalForPrisma.prisma = prisma;
