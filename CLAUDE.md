# PDA-INNO 프로젝트 규칙

## 프로젝트 개요
- Android PDA 앱 (이마트 출하 관리)
- Java, SQLite(로컬DB), Oracle(서버DB)
- searchType: 0=이마트, 1=생산, 2=홈플러스, 3=도매, 4=비정량, 5=홈플러스비정량, 6=롯데, 7=생산라벨

## 문서 작성 규칙 (Excel/MD)

### Index 기준
- **Index = JSP out.println 출력 순서** (0부터 시작)
- VIEW DDL 컬럼 순서가 아님

### Index '-' 조건
- JSP에서 전송하지 않는 컬럼 (WHERE/ORDER BY 조건용)
- ProgressDlgShipSearch에서 temp[]로 파싱하지 않는 컬럼

### 문서 작성 후 필수 검증 (매번 수행)
1. **JSP 확인**: 외부 JSP 프로젝트 경로에서 해당 JSP의 out.println 순서 확인
2. **Index 검증**: JSP out.println 순서와 문서 Index 일치 여부 확인
3. **파싱 검증**: `ProgressDlgShipSearch.java` temp[] 파싱 코드와 Index 일치 여부 확인
4. **미파싱 컬럼**: JSP 전송하나 Java 미파싱이면 Index '-'로 표시
5. **TB_SHIPMENT 컬럼**: DBHandler.createqueryShipment()와 실제 컬럼 일치 여부 확인

### 용도 표기 기준
- JSP 미전달 컬럼: "미사용(JSP미전달·로컬DB없음)"
- Java 미파싱 컬럼: "미사용(Java미파싱)"
- INSERT만 사용: "로컬DB저장용"
- 실제 사용: 구체적 용도 명시

## 주요 파일 경로
- JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29` (외부 Tomcat 프로젝트)
- 컬럼 정의: `app/doc/column/PDA컬럼정의_SGI.xlsx`
- MD 문서: `app/doc/column/`
- ProgressDlgShipSearch: `app/src/main/java/.../common/ProgressDlgShipSearch.java`
- LabelPrintHelper: `app/src/main/java/.../print/LabelPrintHelper.java`
- DBHandler: `app/src/main/java/.../db/DBHandler.java`

## 알려진 버그 (미수정)
1. `app/doc/오류/01_계근완료후_동일바코드_라벨재출력_버그.md` - dup 결과 미사용, return 누락
2. `app/doc/오류/02_WH_AREA_null체크_OR연산자_버그.md` - `||` → `&&` 수정 필요 (12곳)


## 커밋 메세지 작성
1. 문서 기반으로 step 별 개발 진행사항 커밋의 경우
    문서명 : step명
    ex) 12_LabelPrintHelper_분리_계획 ### Step 9: 통합 테스트

2. 문서 커밋의 경우
    문서 : 문서 주제
    문서: 개발 가이드 파일명 번호 부여 및 내용 수정

3. JSP 파일 커밋의 경우
    JSP 파일명 [수정/개발]
    search_production.jsp [수정]

-- 커밋 후 PUSH 까지 진행

## 외부 프로젝트 경로
JSP 프로젝트 경로
목적 : JSP 파일에 관해 데이터를 읽을때 해당 경로에서 확인하라
D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29

HL_ERP 프로젝트 경로
목적 : ERP 파일에 관해 데이터를 읽을때 해당 경로에서 확인하라
D:\HL_ERP\workspace\SGIS_HL_WEBERP

단 읽을때는 나에게 허락을 받지 않아도 괜찮다.