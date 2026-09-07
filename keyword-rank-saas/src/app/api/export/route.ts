import { apiUser } from "@/lib/api";
import { toErrorResponse } from "@/lib/http";
import { assertCsvExport } from "@/lib/limits";
import { engineLabel } from "@/lib/engines";
import { isoDate, kstDateOnly } from "@/lib/date";
import { buildKeywordRows } from "@/lib/rank";

export const runtime = "nodejs";

/** 현재 순위 현황을 CSV 로 내려준다. */
export async function GET(request: Request) {
  try {
    const user = await apiUser();
    await assertCsvExport(user.org);

    const projectId = new URL(request.url).searchParams.get("projectId") ?? undefined;
    const rows = await buildKeywordRows({ orgId: user.orgId, projectId });

    const lines = ["프로젝트,키워드,검색영역,현재순위,직전순위,변동,수집일,노출URL"];
    for (const row of rows) {
      for (const cell of row.cells) {
        lines.push(
          [
            csv(row.projectName),
            csv(row.text),
            csv(engineLabel(cell.engine)),
            cell.rank ?? "미노출",
            cell.prevRank ?? "-",
            cell.delta === null ? "-" : cell.delta > 0 ? `+${cell.delta}` : cell.delta,
            cell.checkedOn ? isoDate(cell.checkedOn) : "-",
            csv(cell.url ?? ""),
          ].join(","),
        );
      }
    }

    // 엑셀에서 한글이 깨지지 않도록 BOM 을 붙인다
    const body = "﻿" + lines.join("\r\n");
    return new Response(body, {
      headers: {
        "Content-Type": "text/csv; charset=utf-8",
        "Content-Disposition": `attachment; filename="rank-${isoDate(kstDateOnly())}.csv"`,
      },
    });
  } catch (error) {
    return toErrorResponse(error);
  }
}

function csv(value: string): string {
  return `"${value.replace(/"/g, '""')}"`;
}
