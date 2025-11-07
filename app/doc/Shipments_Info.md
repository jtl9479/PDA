# Shipments_Info 클래스 문서

## 개요
`Shipments_Info`는 출하대상 정보를 관리하는 데이터 모델 클래스입니다. 이 클래스는 출하 요청부터 실제 출고까지의 전 과정에 필요한 정보를 담고 있으며, 상품, 브랜드, 패커, 센터 정보 등을 포함합니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\items\Shipments_Info.java`
**총 라인 수**: 422줄
**패키지**: `com.rgbsolution.highland_emart.items`

## 필드 목록

### 출하 기본 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| SHIPMENT_ID | String | "" | 출하대상 ID |
| GI_H_ID | String | "" | GI_H_ID |
| GI_D_ID | String | "" | 출고번호(출고상세번호id 값) |
| EOI_ID | String | "" | 이마트 출하번호(발주번호) |
| GI_REQ_PKG | String | "" | 출하요청수량 |
| GI_REQ_QTY | String | "" | 출하요청중량 |
| AMOUNT | String | "" | 출하상품금액 |
| GI_REQ_DATE | String | "" | 출하요청일 |
| STORE_IN_DATE | String | "" | 납품일자 |

### 상품 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| ITEM_CODE | String | "" | 상품코드 |
| ITEM_NAME | String | "" | 상품명 |
| EMARTITEM_CODE | String | "" | 이마트 상품코드 |
| EMARTITEM | String | "" | 이마트 상품명 |
| ITEM_SPEC | String | "" | 스펙 |
| ITEM_TYPE | String | "" | 상품 타입 |
| PROC_ITEM_CODE | String | "" | 처리 상품코드 |
| PROC_ITEM_NAME | String | "" | 처리 상품명 |

### 브랜드 및 업체 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| BRAND_CODE | String | "" | 브랜드코드 |
| BRANDNAME | String | "" | 브랜드명 |
| CLIENT_CODE | String | "" | 출고업체코드 |
| CLIENTNAME | String | "" | 출고업체명 |
| CENTERNAME | String | "" | 센터명 |

### 패커 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| PACKER_CODE | String | "" | 패커 코드 |
| PACKERNAME | String | "" | 패커 이름 |
| PACKER_PRODUCT_CODE | String | "" | 패커 상품코드 |
| PACKWEIGHT | String | "" | 팩 중량 |

### 바코드 및 입고 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| BARCODE_TYPE | String | "" | 바코드 유형 |
| BARCODEGOODS | String | "" | 바코드의 상품코드 |
| GOODS_R_ID | String | "" | 입고번호 |
| GR_REF_NO | String | "" | 창고입고번호 |
| BL_NO | String | "" | BL 번호 |

### 원산지 및 수입 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| CT_CODE | String | "" | 원산지 코드 |
| CT_NAME | String | "" | 원산지명 |
| IMPORT_ID_NO | String | "" | 수입식별번호 |

### 계근 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| PACKING_QTY | int | 0 | 계근 수량 |
| GI_QTY | double | 0 | 계근 중량 |

### 작업 상태 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| SAVE_CNT | int | 0 | 전송 개수 |
| SAVE_TYPE | String | "" | 저장 여부 |
| WORK_FLAG | int | 0 | 현재 작업 여부 |
| LAST_BOX_ORDER | String | "" | 마지막 박스 순서 |

### 물류 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| EMARTLOGIS_CODE | String | "" | 납품코드 |
| EMARTLOGIS_NAME | String | "" | 납품명 |
| WH_AREA | String | "" | 창고 구역 |
| STORE_CODE | String | "" | 점포 코드 |

### 기타 정보
| 필드명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| USE_NAME | String | "" | 사용명 |
| USE_CODE | String | "" | 사용코드 |
| EMART_PLANT_CODE | String | "" | 이마트 공장코드 |

## 주요 메서드

### Getter/Setter 메서드
모든 필드에 대해 표준 JavaBeans 패턴의 Getter/Setter 메서드가 제공됩니다.

**예시**:
```java
// String 타입 필드
public String getSHIPMENT_ID()
public void setSHIPMENT_ID(String SHIPMENT_ID)

// int 타입 필드
public int getPACKING_QTY()
public void setPACKING_QTY(int PACKING_QTY)

// double 타입 필드
public double getGI_QTY()
public void setGI_QTY(double GI_QTY)
```

### 특수 메서드
- `isWORK_FLAG()`: WORK_FLAG 값을 반환하는 추가 Getter 메서드
- `getWORK_FLAG()`: WORK_FLAG 값을 반환하는 표준 Getter 메서드

## 용도 및 활용

### 주요 용도
1. **출하 관리**: 출하 요청부터 실제 출고까지의 전체 프로세스 관리
2. **상품 추적**: 상품의 입고부터 출고까지 이력 추적
3. **계근 데이터**: 출하 시 계근(계량) 정보 저장 및 관리
4. **이마트 연동**: 이마트 시스템과의 데이터 연동을 위한 정보 보관

### 데이터 흐름
1. 출하 요청 정보 수신 (EOI_ID, GI_REQ_DATE 등)
2. 상품 및 브랜드 정보 매핑 (ITEM_CODE, BRAND_CODE 등)
3. 계근 작업 수행 (PACKING_QTY, GI_QTY 업데이트)
4. 출하 완료 처리 (SAVE_TYPE, WORK_FLAG 업데이트)
5. 서버 전송 (SAVE_CNT 관리)

### 관련 클래스
- `Barcodes_Info`: 바코드 정보 연동
- `Goodswets_Info`: 계근 상세 정보 연동

## 비고
- 모든 String 필드는 빈 문자열("")로 초기화됩니다.
- 숫자 타입(int, double)은 0으로 초기화됩니다.
- 필드명은 대문자와 언더스코어(_)를 사용하는 명명 규칙을 따릅니다.
- public 접근 제어자로 모든 필드가 직접 접근 가능합니다.
