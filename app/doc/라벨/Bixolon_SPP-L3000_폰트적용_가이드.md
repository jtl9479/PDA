# Bixolon SPP-L3000 폰트 적용 가이드

**작성일**: 2026-03-17
**대상 프린터**: Bixolon SPP-L3000

---

## 1. 현재 상태

| 항목 | 원본 (Woosim) | 현재 (Bixolon) |
|------|-------------|---------------|
| 폰트 | 휴먼울림체 (HYWULM.TTF) | 프린터 내장 벡터 폰트 |
| 설정 방법 | `WoosimCmd.selectTTF("HYWULM.TTF")` | SLCS `V` 명령 (기본 폰트) |

---

## 2. Bixolon SPP-L3000 지원 폰트

### 2.1 내장 한글 비트맵 폰트

| 폰트 ID | 크기 | 설명 |
|:-------:|:----:|------|
| Korean a | 16×16 | 소형 |
| Korean b | 24×24 | 중형 |
| Korean c | 20×20 | 중소형 |
| Korean d | 26×26 | 중대형 |
| Korean e | 20×26 | 세로 확장 |
| Korean f | 38×38 | 대형 |

### 2.2 내장 벡터 폰트 (현재 사용 중)

SLCS `V` 명령으로 사용. 크기 자유 조절 가능.

```
V x, y, K, width, height, 0, N, B, N, 0, L, 0, 'text'
```
- `K`: 한글 폰트 지정
- `width, height`: 폰트 크기 (도트)

### 2.3 다운로드 폰트 (TTF)

프린터 메모리에 TTF 파일을 업로드하여 사용. 휴먼울림체 적용 가능.

---

## 3. 커스텀 폰트(휴먼울림체) 적용 방법

### 3.1 필요 준비물

| 항목 | 설명 |
|------|------|
| HYWULM.TTF | 휴먼울림체 폰트 파일 |
| Bixolon Font Downloader | BixFD.exe (Bixolon 프린터 드라이버 설치 시 포함) |
| USB 케이블 또는 블루투스 연결 | PC ↔ SPP-L3000 연결 |

### 3.2 폰트 다운로드 절차

#### Step 1: Font Downloader 실행

Bixolon Label 프린터 드라이버 설치 디렉토리에서 `BixFD.exe` 실행

#### Step 2: 프린터 연결

1. SPP-L3000 전원 ON
2. Font Downloader에서 프린터 포트 선택 (USB 또는 블루투스)
3. 연결 확인

#### Step 3: 폰트 업로드

1. "Add Font" 버튼 클릭
2. HYWULM.TTF 파일 선택
3. 폰트 ID 지정 (예: 1)
4. 문자셋 지정: Korean (한글)
5. "Download" 버튼 클릭
6. 업로드 완료 대기 (폰트 크기에 따라 수 분 소요)

#### Step 4: 업로드 확인

Font Downloader에서 "Information" 버튼으로 프린터에 저장된 폰트 목록 확인

### 3.3 SLCS 명령어에서 다운로드 폰트 사용

#### 다운로드 폰트 관련 SLCS 명령

| 명령 | 설명 |
|------|------|
| `DT` | Download True Type Font (폰트 다운로드) |
| `DD` | Downloaded font Delete (폰트 삭제) |
| `DI` | Downloaded font Information (폰트 정보 조회) |

#### 텍스트 출력 시 다운로드 폰트 사용

```
// 내장 벡터 폰트 (현재)
V x, y, K, width, height, 0, N, B, N, 0, L, 0, 'text'

// 다운로드 폰트 사용 시
T x, y, font_id, x_scale, y_scale, rotation, 'text'
```

#### LabelPrintHelper.java 수정 예시

```java
// 현재 (내장 벡터 폰트)
private String slcsText(int x, int y, int width, int height, String text) {
    return "V" + x + "," + y + ",K," + width + "," + height + ",0,N,B,N,0,L,0,'" + text + "'\r\n";
}

// 다운로드 폰트 사용 시 (폰트 ID=1로 가정)
private String slcsText(int x, int y, int width, int height, String text) {
    return "T" + x + "," + y + ",1," + width + "," + height + ",0,'" + text + "'\r\n";
}
```

**주의**: `T` 명령의 정확한 파라미터는 폰트 다운로드 방식(비트맵/벡터)에 따라 다를 수 있으므로, 프린터에 폰트 업로드 후 SLCS 매뉴얼의 `T` 명령 상세 사양을 확인해야 한다.

---

## 4. 방법별 비교

| 항목 | 방법 1: 내장 벡터 폰트 (현재) | 방법 2: 다운로드 폰트 (휴먼울림체) |
|------|-------------------------|--------------------------|
| 설정 난이도 | 없음 (바로 사용) | 폰트 다운로드 필요 |
| 폰트 외형 | Bixolon 기본 한글 | 원본(Woosim)과 동일 |
| 프린터 메모리 | 사용 안 함 | 폰트 저장 공간 필요 |
| 프린터 교체 시 | 설정 불필요 | 폰트 재다운로드 필요 |
| 인쇄 속도 | 빠름 | 다소 느릴 수 있음 |
| SLCS 명령 | `V` 명령 | `T` 명령 (수정 필요) |

---

## 5. 권장 사항

1. **현재 내장 벡터 폰트로 기능 검증 우선 진행**
2. 폰트 외형이 반드시 원본과 동일해야 하는 경우에만 다운로드 폰트 적용
3. 다운로드 폰트 적용 시 프린터 교체/초기화 시마다 재업로드 필요하므로 운영 부담 고려
4. 여러 대의 프린터를 사용하는 경우 모든 프린터에 동일 폰트 업로드 필요

---

**문서 버전**: 1.0
