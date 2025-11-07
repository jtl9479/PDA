# Goodswets_Info 클래스 문서

## 개요
`Goodswets_Info`는 계근(計斤, 무게 측정) 정보를 관리하는 데이터 모델 클래스입니다. 출하 작업 시 각 박스별 중량, 바코드, 제조일 등의 상세 정보를 저장하며, 실제 계근 작업의 결과 데이터를 담는 핵심 클래스입니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\items\Goodswets_Info.java`
**총 라인 수**: 205줄
**패키지**: `com.rgbsolution.highland_emart.items`

## 필드 목록

### 기본 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| GOODS_WET_ID | String | "" | Seq (Primary Key) |
| GI_D_ID | String | "" | 출고번호(출고상세번호id 값) |

### 중량 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| WEIGHT | String | "" | 중량, 소숫점 2자리 |
| WEIGHT_UNIT | String | "" | 중량 단위(LB, KG) |

### 패커 및 바코드 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| PACKER_PRODUCT_CODE | String | "" | 패커 상품코드 |
| PACKER_CLIENT_CODE | String | "" | 패커 거래처코드 |
| BARCODE | String | "" | 스캔한 바코드 |

### 제조 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| MAKINGDATE | String | "" | 제조일 |
| BOXSERIAL | String | "" | 박스번호 |

### 계근 작업 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| BOX_CNT | String | "" | 계근 순서번호 |
| BOX_ORDER | String | "" | 박스 순서 |
| DUPLICATE | String | "" | 중복스캔 여부 |

### 상품 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| EMARTITEM_CODE | String | "" | 이마트 상품코드 |
| EMARTITEM | String | "" | 이마트 상품명 |
| ITEM_CODE | String | "" | 상품코드 |
| BRAND_CODE | String | "" | 브랜드코드 |

### 등록 및 관리 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| REG_ID | String | "" | 등록자 ID |
| REG_DATE | String | "" | 등록 날짜 |
| REG_TIME | String | "" | 등록 시간 |
| SAVE_TYPE | String | "" | 전송 여부 |
| MEMO | String | "" | 메모, 관리 목적 컬럼 |
| CLIENT_TYPE | String | "" | 거래처 유형 |

## 주요 메서드

### Getter/Setter 메서드
모든 필드에 대해 표준 JavaBeans 패턴의 Getter/Setter 메서드가 제공됩니다.

**예시**:
```java
// 기본 정보
public String getGOODS_WET_ID()
public void setGOODS_WET_ID(String GOODS_WET_ID)

// 중량 정보
public String getWEIGHT()
public void setWEIGHT(String WEIGHT)

public String getWEIGHT_UNIT()
public void setWEIGHT_UNIT(String WEIGHT_UNIT)

// 바코드 정보
public String getBARCODE()
public void setBARCODE(String BARCODE)

// 제조 정보
public String getMAKINGDATE()
public void setMAKINGDATE(String MAKINGDATE)

public String getBOXSERIAL()
public void setBOXSERIAL(String BOXSERIAL)
```

## 용도 및 활용

### 주요 용도
1. **계근 데이터 저장**: 각 박스별 실측 중량 및 바코드 정보 기록
2. **출하 이력 관리**: 출하된 상품의 상세 내역 추적
3. **중복 검사**: 동일 바코드의 중복 스캔 방지 및 관리
4. **품질 관리**: 제조일, 박스번호 등을 통한 추적성 확보
5. **데이터 전송**: 서버로의 계근 정보 전송 관리

### 계근 작업 프로세스
1. **바코드 스캔**: BARCODE 필드에 스캔 데이터 저장
2. **바코드 파싱**:
   - `Barcodes_Info`를 참조하여 바코드 분석
   - PACKER_PRODUCT_CODE, WEIGHT, MAKINGDATE, BOXSERIAL 추출
3. **중량 측정**:
   - 저울에서 측정된 중량을 WEIGHT에 저장
   - 중량 단위(LB/KG)를 WEIGHT_UNIT에 저장
4. **중복 검사**:
   - 동일 바코드 재스캔 시 DUPLICATE 플래그 설정
5. **데이터 저장**:
   - 로컬 DB에 계근 정보 저장
   - SAVE_TYPE으로 전송 상태 관리
6. **서버 전송**:
   - 저장된 계근 데이터를 서버로 전송

### 데이터 흐름
```
[바코드 스캔] → [바코드 파싱] → [중량 측정] → [DB 저장] → [서버 전송]
       ↓              ↓              ↓            ↓           ↓
    BARCODE    PACKER_CODE      WEIGHT    GOODS_WET_ID  SAVE_TYPE
              MAKINGDATE     WEIGHT_UNIT
              BOXSERIAL
```

### 관련 클래스
- `Shipments_Info`: 출하 대상 정보, GI_D_ID로 연결
- `Barcodes_Info`: 바코드 파싱 규칙 참조

## 사용 시나리오

### 1. 계근 데이터 생성
```java
Goodswets_Info goodswet = new Goodswets_Info();
goodswet.setGI_D_ID("GI123456");
goodswet.setBARCODE("1234567890123456");
goodswet.setWEIGHT("15.25");
goodswet.setWEIGHT_UNIT("KG");
goodswet.setPACKER_PRODUCT_CODE("P001");
goodswet.setMAKINGDATE("20250131");
goodswet.setBOXSERIAL("001");
goodswet.setBOX_CNT("1");
goodswet.setREG_ID("USER01");
goodswet.setSAVE_TYPE("N");
```

### 2. 중복 스캔 체크
```java
// 동일 바코드 스캔 시
if (isDuplicate(barcode)) {
    goodswet.setDUPLICATE("Y");
    // 중복 처리 로직
}
```

### 3. 계근 완료 및 전송
```java
// 계근 작업 완료
goodswet.setREG_DATE("20250131");
goodswet.setREG_TIME("143000");

// 서버 전송 후
goodswet.setSAVE_TYPE("Y");
```

## 데이터 검증 사항

### 필수 입력 필드
- GI_D_ID: 출고번호 (연결키)
- BARCODE: 스캔된 바코드
- WEIGHT: 측정 중량
- WEIGHT_UNIT: 중량 단위

### 선택 입력 필드
- MAKINGDATE: 제조일 (바코드에서 추출)
- BOXSERIAL: 박스번호 (바코드에서 추출)
- BOX_CNT: 계근 순서
- DUPLICATE: 중복 여부

## 비고
- 모든 String 필드는 빈 문자열("")로 초기화됩니다.
- WEIGHT는 String 타입이지만 숫자 형식으로 저장됩니다 (소수점 2자리).
- WEIGHT_UNIT은 "LB" 또는 "KG" 값을 가집니다.
- DUPLICATE는 중복 스캔 여부를 나타내며, 일반적으로 "Y" 또는 빈 문자열입니다.
- SAVE_TYPE은 서버 전송 여부를 나타냅니다 ("Y": 전송완료, "N": 미전송).
- public 접근 제어자로 모든 필드가 직접 접근 가능합니다.
- BOX_CNT는 같은 출하 건 내에서 계근 순서를 나타냅니다.
