# M0 라벨 프린터 TTF 폰트 다운로드

**작성일**: 2026-04-03
**목적**: Bixolon 프린터에 HYWULM.TTF(휴먼울림체)를 다운로드하여 원본과 동일한 폰트 품질 확보
**관련 오류**: `app/doc/오류/07_M0라벨_텍스트품질저하_비트맵렌더링[이마트출하대상받기].md`
**참고 문서**: `app/doc/라벨/Bixolon_SPP-L3000_폰트적용_가이드.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### 현재 텍스트 출력 방식 (비트맵)

```java
// LabelPrintHelper.java:251 — slcsBitmapText()
Bitmap bitmap = Bitmap.createBitmap(textWidth, textHeight, ...);
Canvas canvas = new Canvas(bitmap);
canvas.drawText(text, 0, -fm.ascent, paint);
// → 1bpp 변환 → LD 명령으로 이미지 전송
```

- 앱에서 비트맵 렌더링 → 프린터에 이미지로 전송
- 품질 저하 (계단현상)

### 원본 텍스트 출력 방식 (프린터 내장 TTF)

```java
// ShipmentActivity.java:1631 (원본)
byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
byteStream.write(WoosimCmd.getTTFcode(35, 35, text));
```

- 프린터 내장 TTF 폰트를 프린터가 직접 렌더링
- 선명한 출력

### 문제점

- 현재 Bixolon 프린터에 HYWULM.TTF가 설치되어 있지 않음
- 앱에서 비트맵으로 변환하여 전송하므로 품질 저하
- 고객사 요구: 기존 폰트(휴먼울림체) 그대로 사용

---

## 2. 변경 구조

### 데이터 흐름 (변경 후)

```
[사전 작업] BixFD.exe로 프린터에 HYWULM.TTF 다운로드 (1회)
    ↓
[LabelPrintHelper] slcsBitmapText() → slcsDownloadFontText() 교체
    ↓ SLCS T 명령으로 텍스트 전송
[Bixolon 프린터] 내장된 HYWULM.TTF로 직접 렌더링
    ↓
[라벨 출력] 원본과 동일한 폰트 품질
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **프린터** | Bixolon SPP-L3000 | BixFD.exe로 HYWULM.TTF 다운로드 (1회) |
| 2 | **LabelPrintHelper.java** | PDA-INNO | `slcsBitmapText` 호출 → `T` 명령 기반 메서드로 교체 |

---

## 4. 수정 상세

### 4.1 프린터에 HYWULM.TTF 다운로드 (사전 작업)

**필요 준비물:**

| 항목 | 설명 |
|------|------|
| HYWULM.TTF | `app/src/main/assets/hywulm.ttf` (프로젝트에 존재) |
| BixFD.exe | Bixolon Font Downloader (프린터 드라이버 설치 시 포함) |
| USB 또는 블루투스 | PC ↔ SPP-L3000 연결 |

**절차:**

```
1. BixFD.exe 실행
2. 프린터 연결 (USB 또는 블루투스)
3. "Add Font" → hywulm.ttf 선택
4. 폰트 ID 지정 (예: 1)
5. 문자셋: Korean (한글)
6. "Download" 클릭
7. 업로드 완료 대기
8. "Information"으로 프린터 폰트 목록 확인
```

---

### 4.2 LabelPrintHelper.java — T 명령 메서드 추가 및 교체

**경로**: `app/src/main/java/.../print/LabelPrintHelper.java`

**추가할 메서드:**

```java
/**
 * 다운로드 폰트로 텍스트 출력 (SLCS T 명령)
 * - 프린터에 미리 다운로드된 HYWULM.TTF 사용
 *
 * @param x      X 좌표
 * @param y      Y 좌표
 * @param width  폰트 너비
 * @param height 폰트 높이
 * @param text   출력할 텍스트
 * @return SLCS T 명령어 문자열
 */
private String slcsDownloadFontText(int x, int y, int width, int height, String text) {
    // T x,y,font_id,x_scale,y_scale,rotation,'text'
    return "T" + x + "," + y + ",1," + width + "," + height + ",0,'" + text + "'\r\n";
}
```

**교체 대상:**

모든 `slcsBitmapText()` 호출 → `slcsDownloadFontText()` 호출로 교체

```java
// 변경 전
labelData.write(slcsBitmapText(20, 12, 35, si.CENTERNAME, true));

// 변경 후
labelData.write(slcsDownloadFontText(20, 12, 35, 35, si.CENTERNAME).getBytes("EUC-KR"));
```

> 주의: `T` 명령의 정확한 파라미터는 프린터에 폰트 업로드 후 SLCS 매뉴얼 확인 필요. 폰트 ID, x_scale, y_scale 등이 다를 수 있음.

**검증**: M0 라벨 인쇄 후 원본과 폰트 품질 비교

---

## 5. 사이드이펙트

### 프린터 의존성

- HYWULM.TTF가 프린터에 다운로드되어 있어야 동작
- 프린터 교체/초기화 시 재다운로드 필요
- 여러 대 프린터 사용 시 모든 프린터에 동일 폰트 업로드 필요

### slcsBitmapText 제거 시

- `slcsBitmapText`를 완전 제거하면 폰트 미다운로드 프린터에서 출력 불가
- 폴백(fallback)으로 `slcsBitmapText` 유지 검토

---

## 6. 데이터 저장 구조

해당 없음

---

## 7. 호출 시점

해당 없음 (기존 호출 흐름 변경 없음, 텍스트 출력 명령만 변경)

---

## 8. 개발 플랜

### Step 1: 프린터에 HYWULM.TTF 다운로드

**Part 1. 분석**
- 메서드: N/A (프린터 설정)
- 범위: Bixolon SPP-L3000 프린터
- 용도: 프린터에 휴먼울림체 폰트 설치
- 주의할 점: BixFD.exe 필요, 폰트 ID 확인

| # | 항목 | 내용 |
|---|------|------|
| 1 | BixFD.exe 실행 | Bixolon Font Downloader |
| 2 | 프린터 연결 | USB 또는 블루투스 |
| 3 | hywulm.ttf 업로드 | 폰트 ID=1, Korean |
| 4 | 업로드 확인 | Information으로 폰트 목록 확인 |

**Part 2. 변환 계획**
- 변환 방식: BixFD.exe 사용 (1회)
- 주의사항: 프린터 메모리에 저장되므로 전원 OFF 후에도 유지

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 폰트 다운로드 확인
- [ ] Part 5: T 명령 테스트 출력

**Part 5. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: LabelPrintHelper.java 수정

**Part 1. 분석**
- 메서드: slcsDownloadFontText() 추가, slcsBitmapText() 호출 교체
- 범위: LabelPrintHelper.java 전체 (slcsBitmapText 호출 약 30곳)
- 용도: 비트맵 → 다운로드 폰트(T 명령)로 교체
- 주의할 점: T 명령 파라미터(폰트 ID, scale) 프린터 확인 후 적용

**Part 2. 변환 계획**
- 변환 방식: slcsDownloadFontText 메서드 추가 + 호출부 교체
- 주의사항: slcsBitmapText는 폴백용으로 유지

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: 라벨 인쇄 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | M0 라벨 인쇄 → 텍스트 선명도 원본과 비교 | □ |
| 2 | 한글/영문/숫자 모두 정상 출력 | □ |
| 3 | 라벨 레이아웃(좌표, 크기) 원본과 동일 확인 | □ |
| 4 | 바코드 정상 출력 확인 | □ |
| 5 | 앱 비정상 종료 없음 | □ |

---

### 개발 순서 요약

```
Step 1: 프린터에 HYWULM.TTF 다운로드 (BixFD.exe, 1회)
    ↓
Step 2: LabelPrintHelper.java 수정 (slcsBitmapText → T 명령)
    ↓
Step 3: 라벨 인쇄 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 폰트 품질 비교

```
1. Step 1 완료 후 (프린터에 HYWULM.TTF 다운로드)
2. Step 2 완료 후 (T 명령 적용)
3. M0 라벨 인쇄
4. 원본 라벨(M0원본.jpg)과 텍스트 품질 비교
5. 폰트 외형이 휴먼울림체와 동일한지 확인
```

### 시나리오 2: 폰트 미다운로드 프린터

```
1. 폰트가 다운로드되지 않은 다른 프린터로 연결
2. M0 라벨 인쇄 시도
3. 출력 결과 확인 (깨짐/에러 발생 여부)
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | BixFD.exe 없음 | Bixolon 드라이버 미설치 | Bixolon 홈페이지에서 드라이버 다운로드 |
| 2 | T 명령 파라미터 불일치 | 폰트 ID, scale 값 다름 | 프린터에 폰트 업로드 후 DI 명령으로 확인 |
| 3 | 프린터 메모리 부족 | TTF 파일 크기 초과 | 폰트 서브셋(사용 글자만) 생성 |
| 4 | 폰트 미다운로드 프린터 | 신규/교체 프린터 | 운영 매뉴얼에 폰트 다운로드 절차 포함 |
| 5 | 한글 출력 깨짐 | T 명령 인코딩 문제 | EUC-KR 인코딩 확인 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 프린터에 HYWULM.TTF 다운로드 | ⏳ 대기 |
| 2 | LabelPrintHelper.java 수정 | ⏳ 대기 |
| 3 | 라벨 인쇄 테스트 | ⏳ 대기 |

---

**문서 버전**: 1.0
