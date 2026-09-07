import Link from "next/link";

import DeleteButton from "@/components/DeleteButton";
import ProjectForm from "@/components/ProjectForm";
import { requireUser } from "@/lib/auth";
import { prisma } from "@/lib/db";
import { planOf } from "@/lib/plan";

export const dynamic = "force-dynamic";

export default async function ProjectsPage() {
  const user = await requireUser();
  const plan = planOf(user.org.plan);

  const projects = await prisma.project.findMany({
    where: { orgId: user.orgId },
    include: { _count: { select: { keywords: true } } },
    orderBy: { createdAt: "asc" },
  });

  const canManage = user.role !== "MEMBER";

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">프로젝트</h1>
          <p className="mt-1 text-sm text-ink-2">
            사이트(도메인) 단위로 키워드를 나눠 관리합니다.{" "}
            <span className="num">
              {projects.length} / {plan.maxProjects}
            </span>
          </p>
        </div>
        {canManage && <ProjectForm disabled={projects.length >= plan.maxProjects} />}
      </div>

      {projects.length === 0 ? (
        <p className="card p-10 text-center text-sm text-muted">
          등록된 프로젝트가 없습니다. 오른쪽 위에서 추가해 주세요.
        </p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {projects.map((project) => (
            <div key={project.id} className="card p-5">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <Link
                    href={`/app/projects/${project.id}`}
                    className="text-lg font-bold hover:underline"
                  >
                    {project.name}
                  </Link>
                  <p className="mt-0.5 text-sm text-ink-2">{project.targetDomain}</p>
                </div>
                <span className="chip num">키워드 {project._count.keywords}</span>
              </div>

              {project.memo && (
                <p className="mt-3 text-sm text-muted">{project.memo}</p>
              )}

              <div className="mt-4 flex gap-2">
                <Link href={`/app/projects/${project.id}`} className="btn btn-sm">
                  열기
                </Link>
                {canManage && (
                  <DeleteButton
                    endpoint={`/api/projects/${project.id}`}
                    confirmMessage={`'${project.name}' 프로젝트와 등록된 키워드·순위 이력이 모두 삭제됩니다. 계속할까요?`}
                  />
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
