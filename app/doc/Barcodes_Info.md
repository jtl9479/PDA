# Barcodes_Info 클래스 문서

## 개요
`Barcodes_Info`는 바코드 정보를 관리하는 데이터 모델 클래스입니다. 바코드의 구조와 파싱 규칙을 정의하며, 바코드 내에서 상품코드, 중량, 제조일, 박스번호 등의 위치 정보를 저장합니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\items\Barcodes_Info.java`
**총 라인 수**: 239줄
**패키지**: `com.rgbsolution.highland_emart.items`

## 필드 목록

### 기본 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| BARCODE_INFO_ID | String | "" | ID키 (Primary Key) |
| BRAND_CODE | String | "" | 브랜드코드 |
| STATUS | String | "" | 상태값, 사용여부 (Y, N) |

### 패커 및 상품 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| PACKER_CLIENT_CODE | String | "" | 패커 거래처 코드 |
| PACKER_PRODUCT_CODE | String | "" | 패커 상품코드 |
| PACKER_PRD_NAME | String | "" | 패커 상품명 |
| ITEM_CODE | String | "" | 아이템코드 |
| ITEM_NAME_KR | String | "" | 한글 상품명 |

### 바코드 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| BARCODEGOODS | String | "" | 바코드의 상품코드 |
| BASEUNIT | String | "" | LB, KG 구분 (중량 단위) |
| ZEROPOINT | String | "" | 소수점 자리수 |

### 바코드 파싱 규칙 - 패커 상품코드
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| PACKER_PRD_CODE_FROM | String | "" | 패커 상품코드의 시작 위치 |
| PACKER_PRD_CODE_TO | String | "" | 패커 상품코드의 끝 위치 |

### 바코드 파싱 규칙 - 바코드 상품코드
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| BARCODEGOODS_FROM | String | "" | 바코드의 상품코드 시작 위치 |
| BARCODEGOODS_TO | String | "" | 바코드의 상품코드 끝 위치 |

### 바코드 파싱 규칙 - 중량
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| WEIGHT_FROM | String | "" | 중량 시작 위치 |
| WEIGHT_TO | String | "" | 중량 끝 위치 |

### 바코드 파싱 규칙 - 제조일
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| MAKINGDATE_FROM | String | "" | 제조일 시작 위치 |
| MAKINGDATE_TO | String | "" | 제조일 끝 위치 |

### 바코드 파싱 규칙 - 박스번호
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| BOXSERIAL_FROM | String | "" | 박스번호 시작 위치 |
| BOXSERIAL_TO | String | "" | 박스번호 끝 위치 |

### 등록 및 관리 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| REG_ID | String | "" | 등록자 ID |
| REG_DATE | String | "" | 등록 날짜 |
| REG_TIME | String | "" | 등록 시간 |
| MEMO | String | "" | 메모, 관리 목적 컬럼 |
| SHELF_LIFE | String | "" | 유통기한 |

## 주요 메서드

### Getter/Setter 메서드
모든 필드에 대해 표준 JavaBeans 패턴의 Getter/Setter 메서드가 제공됩니다.

**예시**:
```java
// 기본 정보
public String getBARCODE_INFO_ID()
public void setBARCODE_INFO_ID(String BARCODE_INFO_ID)

// 바코드 파싱 정보
public String getWEIGHT_FROM()
public void setWEIGHT_FROM(String WEIGHT_FROM)

public String getWEIGHT_TO()
public void setWEIGHT_TO(String WEIGHT_TO)

// 상태 정보
public String getSTATUS()
public void setSTATUS(String STATUS)
```

## 용도 및 활용

### 주요 용도
1. **바코드 템플릿 관리**: 다양한 형식의 바코드 구조를 정의하고 저장
2. **바코드 파싱**: 스캔된 바코드에서 필요한 정보를 추출하기 위한 규칙 제공
3. **상품 매핑**: 패커 상품코드와 시스템 상품코드 간의 매핑 관리
4. **중량 추출**: 바코드에서 중량 정보를 파싱하여 계근 작업에 활용

### 바코드 파싱 프로세스
1. 바코드 스캔
2. BRAND_CODE 및 PACKER_CLIENT_CODE로 해당 바코드 정보 검색
3. FROM/TO 위치 값을 이용하여 바코드 문자열 파싱:
   - 패커 상품코드 추출 (PACKER_PRD_CODE_FROM ~ TO)
   - 바코드 상품코드 추출 (BARCODEGOODS_FROM ~ TO)
   - 중량 추출 (WEIGHT_FROM ~ TO)
   - 제조일 추출 (MAKINGDATE_FROM ~ TO)
   - 박스번호 추출 (BOXSERIAL_FROM ~ TO)
4. 추출된 정보를 이용하여 계근 및 출하 처리

### 데이터 구조 예시
```
바코드: 1234567890123456789012345
        ├─ [0-5]: 패커 상품코드 (123456)
        ├─ [6-10]: 바코드 상품코드 (78901)
        ├─ [11-15]: 중량 (23456)
        ├─ [16-21]: 제조일 (789012)
        └─ [22-25]: 박스번호 (345)
```

### 관련 클래스
- `Shipments_Info`: 출하 정보에서 바코드 유형 및 상품코드 참조
- `Goodswets_Info`: 계근 작업 시 바코드 파싱 정보 활용

## 사용 시나리오

### 1. 새로운 바코드 형식 등록
```java
Barcodes_Info barcodeInfo = new Barcodes_Info();
barcodeInfo.setBRAND_CODE("B001");
barcodeInfo.setPACKER_CLIENT_CODE("P001");
barcodeInfo.setWEIGHT_FROM("6");
barcodeInfo.setWEIGHT_TO("11");
barcodeInfo.setBASEUNIT("KG");
barcodeInfo.setZEROPOINT("2");
barcodeInfo.setSTATUS("Y");
```

### 2. 바코드에서 중량 추출
```java
// 바코드 정보 조회 후
String barcode = "1234567890123456";
int from = Integer.parseInt(barcodeInfo.getWEIGHT_FROM());
int to = Integer.parseInt(barcodeInfo.getWEIGHT_TO());
String weight = barcode.substring(from, to);
```

## 비고
- 모든 String 필드는 빈 문자열("")로 초기화됩니다.
- FROM/TO 필드는 바코드 문자열에서의 인덱스 위치를 저장합니다.
- BASEUNIT은 중량 단위(LB 또는 KG)를 나타냅니다.
- ZEROPOINT는 중량 소수점 자리수를 의미합니다.
- STATUS가 "Y"인 경우에만 활성 상태로 간주됩니다.
- public 접근 제어자로 모든 필드가 직접 접근 가능합니다.
