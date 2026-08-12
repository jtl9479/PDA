export const meta = {
  name: 'notion-dev-pipeline',
  description: 'Notion 개발 DB 요구 → 개발문서 초안 작성 + 적대적 검증 (코드 수정/커밋은 사람 게이트, 워크플로우 밖)',
  phases: [
    { title: '요구분석', detail: 'Notion 개발 DB 행에서 요구사항 구조화' },
    { title: '개발문서초안', detail: 'dev-guide로 템플릿 기반 개발문서 작성' },
    { title: '문서검증', detail: 'doc-consistency + 적대적 요구누락 검증' },
  ],
}

// ── 입력: args.rowId = Notion 개발 DB 행의 page id (또는 url) ──
const rowId = args && (args.rowId || args.url)
if (!rowId) {
  log('args.rowId (Notion 개발 DB 행 id 또는 url)가 필요합니다.')
  return { error: 'no rowId' }
}

const REQ_SCHEMA = {
  type: 'object',
  required: ['title', 'category', 'requirement'],
  properties: {
    title: { type: 'string', description: '개발명' },
    category: { type: 'string', description: '분류(신규/수정/오류)' },
    requirement: { type: 'string', description: '요구사항 본문 요약' },
    constraints: { type: 'array', items: { type: 'string' }, description: '제약/주의(원본 100% 동일 등)' },
    targets: { type: 'array', items: { type: 'string' }, description: '영향 받을 파일/모듈 추정' },
  },
}

const VERDICT_SCHEMA = {
  type: 'object',
  required: ['pass', 'issues'],
  properties: {
    pass: { type: 'boolean', description: '템플릿/번호/링크 준수 및 요구 반영 통과 여부' },
    issues: { type: 'array', items: { type: 'string' }, description: '발견된 문제(없으면 빈 배열)' },
    missingRequirements: { type: 'array', items: { type: 'string' }, description: '문서에 누락된 요구사항' },
    docPath: { type: 'string', description: '검증한 개발문서 경로' },
  },
}

// S1. 요구분석 — Notion 개발 DB 행을 읽어 구조화
phase('요구분석')
const req = await agent(
  `Notion 개발 DB 행을 읽어 개발 요구사항을 구조화하라.\n` +
  `1) ToolSearch로 mcp__notion__notion-fetch 를 로드한다.\n` +
  `2) id "${rowId}" 페이지를 fetch 하여 제목/분류/본문(요구내용)을 추출한다.\n` +
  `3) 프로젝트 최우선 원칙(기존 기능 100% 동일, 임의 변경 금지)을 constraints에 반드시 포함한다.\n` +
  `4) 본문에서 영향 받을 파일/모듈을 추정해 targets에 적는다(불확실하면 비워둔다).`,
  { agentType: 'general-purpose', schema: REQ_SCHEMA, label: '요구분석', phase: '요구분석' }
)
if (!req) return { error: '요구분석 실패' }

// S2. 개발문서 초안 — dev-guide 에이전트가 템플릿 기반으로 작성
phase('개발문서초안')
const draft = await agent(
  `다음 요구사항으로 개발 가이드 문서를 작성하라. 반드시 app/doc/개발/ 의 템플릿/번호 규칙을 따르고 ` +
  `영향 분석 + step 설계 + 체크리스트를 포함한다. "관련 문서" 섹션에 참조 경로를 명시한다.\n\n` +
  `요구사항(JSON):\n${JSON.stringify(req, null, 2)}\n\n` +
  `작성한 문서의 최종 파일 경로를 결과 마지막 줄에 "DOC_PATH: <경로>" 형식으로 출력하라.`,
  { agentType: 'dev-guide', label: '개발문서초안', phase: '개발문서초안' }
)
if (!draft) return { error: '개발문서초안 실패', requirement: req }

// S3. 문서검증 — 일관성 + 적대적 요구누락 검증 (병렬)
phase('문서검증')
const verdicts = await parallel([
  () => agent(
    `방금 작성된 개발문서를 검증하라. app/doc/개발/ 의 최신 문서를 찾아: ` +
    `(1) 번호/파일명 규칙, (2) 템플릿 섹션 누락, (3) "관련 문서" 링크 정합성을 점검한다.\n` +
    `초안 작성 결과:\n${draft}`,
    { agentType: 'doc-consistency', schema: VERDICT_SCHEMA, label: '검증:일관성', phase: '문서검증' }
  ),
  () => agent(
    `적대적 검증: 아래 요구사항이 방금 작성된 개발문서에 빠짐없이 반영됐는지 의심하며 확인하라. ` +
    `누락/왜곡된 요구를 missingRequirements 에 모두 적고, 하나라도 있으면 pass=false.\n\n` +
    `요구사항(JSON):\n${JSON.stringify(req, null, 2)}\n\n초안 결과:\n${draft}`,
    { agentType: 'general-purpose', schema: VERDICT_SCHEMA, label: '검증:요구누락', phase: '문서검증' }
  ),
])

const ok = verdicts.filter(Boolean).every(v => v.pass)
return {
  requirement: req,
  draftResult: draft,
  verdicts: verdicts.filter(Boolean),
  pass: ok,
  nextGate: ok
    ? '✅ 개발문서 통과 → 사용자 검토 후 [개발 단계] 수동 진행'
    : '❌ 검증 실패 → 개발문서 보완 필요 (issues/missingRequirements 확인)',
}
