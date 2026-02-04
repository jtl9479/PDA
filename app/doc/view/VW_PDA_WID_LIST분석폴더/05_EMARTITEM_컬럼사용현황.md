# EMARTITEM 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 이마트 상품명
**파싱 위치**: temp[4]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | ● |
| 로직분기 | |
| DB저장 | ● |
| 바코드검증 | |
| 조회조건 | |
| 앱미전달 | |

> **구분 기준**
> - **서버전송**: 서버에 데이터 전송 시 패킷에 포함
> - **화면표시**: 앱 화면(Activity)에 텍스트로 표시
> - **바코드생성**: 바코드 문자열 생성에 사용
> - **라벨출력**: 라벨 인쇄 시 텍스트/값으로 출력
> - **로직분기**: if/switch 등 조건 분기에 사용
> - **DB저장**: 로컬 SQLite(TB_SHIPMENT)에 저장
> - **바코드검증**: 스캔 바코드와 매칭 검증
> - **조회조건**: 서버/로컬 DB 쿼리 WHERE 조건
> - **앱미전달**: 서버 VIEW에서만 사용, 앱에 전달 안 됨

---

## 1. 용도

| 용도 | 설명 |
|------|------|
| **라벨 출력** | 라벨에 상품명 인쇄 |
| **로그 출력** | 디버그 로그에 상품명 표시 |
| **DB 저장** | TB_SHIPMENT, TB_GOODS_WET 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| Goodswets_Info.java | 계근 데이터 DTO |
| DBHandler.java | TB_SHIPMENT, TB_GOODS_WET 테이블 CRUD |
| BixolonShipmentActivity.java | 라벨 출력 |
| ShipmentActivity.java | 라벨 출력 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블          | 용도                |
| ------------ | ----------------- |
| TB_SHIPMENT  | 출하대상 이마트 상품명 저장   |
| TB_GOODS_WET | 계근 데이터 이마트 상품명 저장 |

---

## 4. 라벨 출력 사용 방식

### 4.1 E0 라벨 (상품명 + 스펙)

**파일**: BixolonShipmentActivity.java (Line 2001~2006)

```java
// 글자 길이에 따라 폰트 크기 조정
if (si.EMARTITEM.length() > 14) {
    slcsCmd.append(slcsText(50, 120, 35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC));  // 14자 초과: 크기 35
} else {
    slcsCmd.append(slcsText(50, 120, 40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC));  // 14자 이하: 크기 40
}
```

### 4.2 M0~M9 라벨 (상품명만)

**파일**: BixolonShipmentActivity.java (Line 2484~2490)

```java
// 글자 길이에 따라 폰트 크기 조정
if (si.EMARTITEM.length() > 14) {
    slcsCmd.append(slcsText(itemX, itemY, 35, 35, si.EMARTITEM));  // 14자 초과: 크기 35
} else {
    slcsCmd.append(slcsText(itemX, itemY, 40, 40, si.EMARTITEM));  // 14자 이하: 크기 40
}
```

### 4.3 M9 라벨 (미트센터 - 상품명 + 용도명)

**파일**: BixolonShipmentActivity.java (Line 2380)

```java
// 바코드 하단 문자열: 상품명 + 용도명
pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME();
```

### 4.4 홈플러스 라벨

**파일**: BixolonShipmentActivity.java (Line 2817~2820)

```java
// 글자 길이에 따라 폰트 크기 조정 (홈플러스는 17자 기준)
if (si.EMARTITEM.length() > 17) {
    slcsCmd.append(slcsText(170, 287, 25, 25, si.EMARTITEM));  // 17자 초과: 크기 25
} else {
    slcsCmd.append(slcsText(170, 283, 30, 30, si.EMARTITEM));  // 17자 이하: 크기 30
}
```

### 4.5 롯데 라벨

**파일**: BixolonShipmentActivity.java (Line 3051)

```java
// 롯데 라벨: 고정 폰트 크기 35
slcsCmd.append(slcsText(10, 12, 35, 35, si.EMARTITEM));
```

---

## 5. 폰트 크기 조정 기준

| 라벨 타입 | 글자 수 기준 | 초과 시 폰트 | 이하 시 폰트 |
|-----------|-------------|-------------|-------------|
| E0, M0~M9 | 14자 | 35 | 40 |
| 홈플러스 | 17자 | 25 | 30 |
| 롯데 | - | 35 (고정) | 35 (고정) |

---

## 6. 결론

**상태**: ✅ 필수 (삭제 불가)

EMARTITEM은 **라벨 출력의 핵심 컬럼**으로, 라벨에 상품명을 인쇄하는 데 사용됩니다.
- 글자 길이에 따라 **폰트 크기 자동 조정**
- 일부 라벨에서 **스펙(ITEM_SPEC)** 또는 **용도명(USE_NAME)**과 결합

---

**최종 수정일**: 2026-02-03
