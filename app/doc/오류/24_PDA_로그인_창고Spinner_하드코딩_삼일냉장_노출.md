# PDA 로그인 창고Spinner 하드코딩 삼일냉장 노출

## 발견일
2026-06-29

## 에러 발생 시나리오

```
1. PDA 앱 최초 실행
2. LoginActivity 진입 → WarehouseSearchTask 실행
3. search_warehouse.jsp 호출: WHERE 회사코드='20' AND PDA여부='1'
4. CO_창고관리에 PDA여부='1'인 행이 없어 0건 반환
5. LoginActivity fallback 실행 → Common.warehouseNames에 "삼일냉장"/"1" 하드코딩 삽입
6. 창고 Spinner에 "삼일냉장"만 표시됨
```

---

## 현상
- PDA 로그인 화면의 창고 선택 Spinner에 DB 실제 창고명 대신 "삼일냉장"만 표시됨
- 창고코드 "1"이 선택된 상태로 로그인되어, 이후 출하/조회 로직이 잘못된 창고 기준으로 동작할 수 있음
- **실제 조회 결과 0건임에도 오류 메시지 없이 "정상처럼" 동작하는 것처럼 보여 문제 발견이 어려움**

## 원래부터 있던 버그인가?

**NO - 전환 과정에서 발생한 신규 버그**

```java
// LoginActivity.java:127-130
// 조회 실패 시 기본값 — 원본(삼일냉장 구 시스템)에서 이식된 하드코딩이
// 회사코드='20' 환경에서 DB 미설정과 맞물려 항상 발동되는 상태
if (Common.warehouseNames.isEmpty()) {
    Common.warehouseNames.add("삼일냉장");
    Common.warehouseCodes.add("1");
}
```

원본 시스템에서 이관된 하드코딩 fallback이 새 회사코드(20) 환경에서 DB 데이터가
정상 설정되지 않은 상태와 겹쳐 항상 발동되는 구조.

## 원인

### 문제 1 (주요): CO_창고관리 PDA여부 미설정

#### 코드 위치
- `WrhsMasterEntity.java` : 182줄
- `C0114_SQL.xml` : 32줄 (ERP C0114 창고관리 화면)

#### 현재 문제 코드
```java
// WrhsMasterEntity.java:182
@Column(name = "PDA여부", nullable = false, columnDefinition = "VARCHAR(1) DEFAULT '0'")
private String pdaAt;
// ★ DEFAULT '0' — 신규 등록된 창고는 PDA여부가 자동으로 '0'
//   ERP C0114 화면에서 수동으로 체크박스를 체크하지 않으면 '1'로 변경되지 않음
```

```xml
<!-- C0114_SQL.xml:32 -->
, A.PDA여부 AS pdaAt
<!-- ★ ERP 창고관리 화면(C0114)의 chkPdaAt 체크박스로만 PDA여부='1' 설정 가능 -->
```

#### 발생 시나리오
회사코드='20'의 창고들이 ERP C0114 화면에서 PDA여부 체크박스를 체크하지 않은 상태로
등록되어 있어, `search_warehouse.jsp`의 `AND PDA여부 = '1'` 조건에 해당하는 행이 0건.

---

### 문제 2 (보조): 하드코딩 fallback이 조회 실패를 은폐

#### 코드 위치
- `LoginActivity.java` : 127-130줄

#### 문제 코드
```java
// LoginActivity.java:127-130
// 조회 실패 시 기본값
if (Common.warehouseNames.isEmpty()) {
    Common.warehouseNames.add("삼일냉장");  // ★ 구 시스템 창고명 하드코딩
    Common.warehouseCodes.add("1");         // ★ 구 시스템 창고코드 하드코딩
}
// ★ 0건 반환(DB 미설정 포함)을 사용자에게 알리지 않고 조용히 덮어씀
// ★ 프로젝트 규칙: 하드코딩 항목에 해당 → 변경 대상
```

## 상세 흐름

1. **앱 시작** (LoginActivity.onCreate)
   - `Common.selectCompanyCode = "20"` (Common.java:41 기본값)
   - `new WarehouseSearchTask().execute()` 호출

2. **서버 조회** (WarehouseSearchTask.doInBackground)
   - `data = " WHERE 회사코드 = '20'"` 생성
   - `search_warehouse.jsp` POST 호출

3. **JSP 실행** (search_warehouse.jsp:36-41)
   - 최종 쿼리: `SELECT 회사코드 AS COMPANY_CODE, 창고코드 AS WAREHOUSE_CODE, 창고명 AS WAREHOUSE_NAME FROM CO_창고관리 WHERE 회사코드 = '20' AND PDA여부 = '1'`
   - CO_창고관리에 PDA여부='1'인 행 없음 → **결과 0건 반환**

4. **응답 처리** (WarehouseSearchTask.onPostExecute:108-124)
   - receiveData = "" (빈 문자열)
   - `Common.warehouseNames`, `Common.warehouseCodes` 비어있음

5. **문제 발생 단계** (LoginActivity.java:127-130)
   - **경로 A (정상)**: warehouseNames에 데이터 있음 → Spinner에 실제 창고명 표시
   - **경로 B (현재 발동)**: warehouseNames 비어있음 → fallback 실행 → "삼일냉장"/"1" 삽입 → Spinner에 "삼일냉장" 표시, 실제 오류 은폐

6. **로그인 후 영향**
   - `Common.selectWarehouseCode = "1"` (구 시스템 창고코드)
   - 이후 출하 대상 조회, 계근 저장 등 창고코드 기반 쿼리 전체가 "1"로 동작

## 영향 범위
- `LoginActivity.java` (88-153줄) — 창고 Spinner 구성 전체
- `Common.java` (41, 50-51줄) — `selectCompanyCode`, `warehouseNames`, `warehouseCodes` 전역 변수
- `search_warehouse.jsp` (36-41줄) — PDA여부 조건 쿼리
- `CO_창고관리` 테이블 — 회사코드='20', PDA여부 컬럼 데이터
- **하위 영향**: `Common.selectWarehouseCode`를 참조하는 모든 출하 조회/계근/라벨 출력 로직 (창고코드 "1"로 잘못 동작 가능)

## 수정 방안

### 수정 1: [데이터] ERP C0114에서 PDA 사용 창고 PDA여부 설정

ERP 창고관리 화면(C0114)에서 회사코드='20'의 PDA를 사용할 창고를 선택하고
`PDA여부` 체크박스(chkPdaAt)를 체크하여 저장.

```sql
-- 확인 쿼리 (현재 PDA여부='1' 창고 없음)
SELECT 회사코드, 창고코드, 창고명, PDA여부
FROM CO_창고관리
WHERE 회사코드 = '20'
ORDER BY 창고코드;

-- 직접 수정 시 (ERP 화면 사용이 원칙, 부득이한 경우에만)
-- UPDATE CO_창고관리 SET PDA여부 = '1' WHERE 회사코드 = '20' AND 창고코드 = '{실제창고코드}';
```

> 이 조치만으로 서버 데이터가 정상 반환되어 Spinner가 올바르게 동작함.

---

### 수정 2: [코드] 하드코딩 fallback 처리 방식 검토 (별도 개발 문서에서 다룸)

현재 하드코딩 fallback("삼일냉장"/"1")은 조회 실패 원인을 은폐하므로,
오류 메시지 표시 또는 Spinner 비활성화 + 로그인 차단 방식으로 변경 필요.

```java
// 수정 방향 (상세 구현은 개발 문서에서 결정)
if (Common.warehouseNames.isEmpty()) {
    // AS-IS: Common.warehouseNames.add("삼일냉장"); Common.warehouseCodes.add("1");
    // TO-BE 안 ①: 오류 Toast + 로그인 버튼 비활성화
    Toast.makeText(LoginActivity.this, "창고 목록을 불러오지 못했습니다.\n관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
    // TO-BE 안 ②: 빈 Spinner 유지 + 로그인 시 창고 미선택 검증
}
```

> 코드 수정은 이 문서에서 진행하지 않음. 개발/ 폴더 문서 작성 후 진행.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/참고자료/오류패턴_분석.md` — 패턴 F: UI/로직 버그 (하드코딩 잔존)
- `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java`
- `app/src/main/java/com/rgbsolution/highland_emart/common/Common.java`
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_warehouse.jsp`
- `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\co\bizbasic\entity\WrhsMasterEntity.java`
- `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\resources\mapper\co\bizbasic\C0114_SQL.xml`
