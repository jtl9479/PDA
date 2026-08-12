export const meta = {
  name: 'notion-code-verify',
  description: '개발 단계 후 코드검증 게이트: 변경 diff에 code-verifier + original-comparator 적대적 fan-out (읽기 전용)',
  phases: [
    { title: '변경수집', detail: 'git diff로 변경 파일/헌크 수집' },
    { title: '검증', detail: 'code-verifier + original-comparator 병렬 검증' },
  ],
}

const CHANGES_SCHEMA = {
  type: 'object',
  required: ['files'],
  properties: {
    files: { type: 'array', items: { type: 'string' }, description: '변경된 소스 파일 경로' },
    summary: { type: 'string', description: '변경 요약' },
  },
}

const VERDICT_SCHEMA = {
  type: 'object',
  required: ['pass', 'findings'],
  properties: {
    pass: { type: 'boolean', description: '검증 통과 여부' },
    findings: { type: 'array', items: { type: 'string' }, description: '문제/위험(없으면 빈 배열)' },
    guardrailViolations: { type: 'array', items: { type: 'string' }, description: '원본 100% 동일 원칙 위반 항목' },
  },
}

// args.base: 비교 기준(git ref). 없으면 working tree 변경분.
const base = (args && args.base) || ''

phase('변경수집')
const changes = await agent(
  `이 저장소의 변경된 소스 파일을 수집하라.\n` +
  (base
    ? `\`git diff --name-only ${base}\` 와 \`git diff ${base}\` 를 사용한다.`
    : `\`git diff --name-only\` (스테이징 포함하려면 \`git diff --name-only HEAD\`) 와 \`git diff HEAD\` 를 사용한다.`) +
  `\n.md/.html 문서는 제외하고 .java 등 소스만 files에 담는다.`,
  { agentType: 'general-purpose', schema: CHANGES_SCHEMA, label: '변경수집', phase: '변경수집' }
)
if (!changes || !changes.files || changes.files.length === 0) {
  return { pass: true, note: '검증할 소스 변경 없음', changes: changes || null }
}

const fileList = changes.files.join('\n')

phase('검증')
const verdicts = await parallel([
  () => agent(
    `다음 변경 파일들의 정합성을 검증하라(읽기 전용). 인덱스(JSP out.println 순서) ↔ Java temp[] 파싱 ↔ ` +
    `column 정의 ↔ 로컬DB CREATE 컬럼 ↔ WHERE절 실제 컬럼명 ↔ 컴파일 가능성을 점검한다.\n변경 파일:\n${fileList}`,
    { agentType: 'code-verifier', schema: VERDICT_SCHEMA, label: '검증:정합성', phase: '검증' }
  ),
  () => agent(
    `적대적 검증: 아래 변경이 원본 프로젝트(D:\\PDA\\PDA-INNO(원본))와 비교해 ` +
    `"기존 기능 100% 동일" 원칙을 위반하지 않는지 의심하며 확인하라. 회사코드/하드코딩 변경 외에 ` +
    `로직·조건이 바뀐 부분이 있으면 guardrailViolations 에 모두 적고 pass=false.\n변경 파일:\n${fileList}`,
    { agentType: 'original-comparator', schema: VERDICT_SCHEMA, label: '검증:원본동일', phase: '검증' }
  ),
])

const v = verdicts.filter(Boolean)
const ok = v.length > 0 && v.every(x => x.pass)
return {
  changes,
  verdicts: v,
  pass: ok,
  nextGate: ok
    ? '✅ 코드검증 통과 → 사용자 검토 후 [테스트 단계] 진행'
    : '❌ 검증 실패 → findings/guardrailViolations 확인 후 수정 (오류면 error-doc 문서화)',
}
