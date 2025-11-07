# DBInfo.java - 데이터베이스 스키마 정의

## 개요
DBInfo 클래스는 Highland E-mart PDA 애플리케이션의 데이터베이스 스키마를 정의하는 클래스입니다. 모든 테이블명과 컬럼명을 상수로 정의하여 타입 안정성과 유지보수성을 제공합니다.

- **파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\db\DBInfo.java`
- **패키지**: `com.rgbsolution.highland_emart.db`
- **총 라인 수**: 126줄
- **타입**: 데이터베이스 스키마 상수 정의 클래스

## 테이블 구조

### 1. 테이블명 상수

```java
public static final String TABLE_NAME_SHIPMENT                    = "TB_SHIPMENT";
public static final String TABLE_NAME_PRODUCTION                  = "TB_PRODUCTION";
public static final String TABLE_NAME_BARCODE_INFO                = "TB_BARCODE_INFO";
public static final String TABLE_NAME_GOODS_WET                   = "TB_GOODS_WET";
public static final String TABLE_NAME_GOODS_WET_PRODUCTION_CALC   = "TB_GOODS_WET_PRODUCTION_CALC";
public static final String TABLE_NAME_COMPLETE_ITEM               = "TB_COMPLETE_ITEM";
```

#### 테이블 설명
- **TB_SHIPMENT**: 출하대상 정보 테이블
- **TB_PRODUCTION**: 생산 정보 테이블
- **TB_BARCODE_INFO**: 바코드 정보 테이블
- **TB_GOODS_WET**: 계근(무게 측정) 상품 작업내역 테이블
- **TB_GOODS_WET_PRODUCTION_CALC**: 생산계근 계산 테이블
- **TB_COMPLETE_ITEM**: 완료 항목 테이블

## 컬럼 구조

### 2. 공통 컬럼 (12-21줄)

모든 테이블에서 공통으로 사용되는 컬럼입니다.

```java
public static final String GI_D_ID                = "GI_D_ID";              // 출고번호(출고상세번호id 값)
public static final String BRAND_CODE             = "BRAND_CODE";           // 브랜드코드
public static final String PACKER_CLIENT_CODE     = "PACKER_CLIENT_CODE";   // 패커 거래처 코드
public static final String PACKER_PRODUCT_CODE    = "PACKER_PRODUCT_CODE";  // 패커 상품코드
public static final String REG_ID                 = "REG_ID";               // 등록자 ID
public static final String REG_DATE               = "REG_DATE";             // 등록 날짜
public static final String REG_TIME               = "REG_TIME";             // 등록 시간
public static final String MEMO                   = "MEMO";                 // 메모
```

### 3. SHIPMENT 테이블 컬럼 (24-62줄)

출하 관련 정보를 저장하는 컬럼들입니다.

#### 기본 식별 정보
```java
public static final String SHIPMENT_ID            = "SHIPMENT_ID";          // 출하대상 ID
public static final String GI_H_ID                = "GI_H_ID";              // GI_H_ID
public static final String EOI_ID                 = "EOI_ID";               // 이마트 출하번호(발주번호)
```

#### 상품 정보
```java
public static final String ITEM_CODE              = "ITEM_CODE";            // 상품코드
public static final String ITEM_NAME              = "ITEM_NAME";            // 상품명
public static final String EMARTITEM_CODE         = "EMARTITEM_CODE";       // 이마트 상품코드
public static final String EMARTITEM              = "EMARTITEM";            // 이마트 상품명
public static final String ITEM_SPEC              = "ITEM_SPEC";            // 스펙
public static final String ITEM_TYPE              = "ITEM_TYPE";            // 상품 타입
```

#### 출하 수량 및 금액
```java
public static final String GI_REQ_PKG             = "GI_REQ_PKG";           // 출하요청수량
public static final String GI_REQ_QTY             = "GI_REQ_QTY";           // 출하요청중량
public static final String AMOUNT                 = "AMOUNT";               // 출하상품금액
public static final String GI_QTY                 = "GI_QTY";               // 계근 중량
public static final String PACKING_QTY            = "PACKING_QTY";          // 계근 수량
public static final String PACKWEIGHT             = "PACKWEIGHT";           // 팩 중량
```

#### 입고 및 출하 정보
```java
public static final String GOODS_R_ID             = "GOODS_R_ID";           // 입고번호
public static final String GR_REF_NO              = "GR_REF_NO";            // 창고입고번호
public static final String GI_REQ_DATE            = "GI_REQ_DATE";          // 출하요청일
public static final String BL_NO                  = "BL_NO";                // BL 번호
```

#### 업체 및 브랜드 정보
```java
public static final String BRANDNAME              = "BRANDNAME";            // 브랜드명
public static final String CLIENT_CODE            = "CLIENT_CODE";          // 출고업체코드
public static final String CLIENTNAME             = "CLIENTNAME";           // 출고업체명
public static final String CENTERNAME             = "CENTERNAME";           // 센터명
```

#### 패커 정보
```java
public static final String PACKER_CODE            = "PACKER_CODE";          // 패커 코드
public static final String PACKERNAME             = "PACKERNAME";           // 패커 이름
```

#### 원산지 및 수입 정보
```java
public static final String CT_CODE                = "CT_CODE";              // 원산지
public static final String IMPORT_ID_NO           = "IMPORT_ID_NO";         // 수입식별번호
```

#### 바코드 정보
```java
public static final String BARCODE_TYPE           = "BARCODE_TYPE";         // 바코드 유형
```

#### 납품 정보
```java
public static final String STORE_IN_DATE          = "STORE_IN_DATE";        // 납품일자
public static final String EMARTLOGIS_CODE        = "EMARTLOGIS_CODE";      // 납품코드
public static final String EMARTLOGIS_NAME        = "EMARTLOGIS_NAME";      // 납품명
```

#### 저장 및 가공장 정보
```java
public static final String SAVE_TYPE              = "SAVE_TYPE";            // 저장 여부
public static final String EMART_PLANT_CODE       = "EMART_PLANT_CODE";     // 이마트 가공장 코드
```

### 4. BARCODE_INFO 테이블 컬럼 (65-91줄)

바코드 정보를 저장하는 컬럼들입니다.

#### 기본 정보
```java
public static final String BARCODE_INFO_ID        = "BARCODE_INFO_ID";      // ID키
public static final String PACKER_PRD_NAME        = "PACKER_PRD_NAME";      // 패커 상품명
public static final String ITEM_NAME_KR           = "ITEM_NAME_KR";         // 한글 상품명
public static final String BARCODEGOODS           = "BARCODEGOODS";         // 바코드의 상품코드
```

#### 단위 및 포맷
```java
public static final String BASEUNIT               = "BASEUNIT";             // LB, KG 구분
public static final String ZEROPOINT              = "ZEROPOINT";            // 소수점 자리수
```

#### 범위 설정 - 패커 상품코드
```java
public static final String PACKER_PRD_CODE_FROM   = "PACKER_PRD_CODE_FROM"; // 패커 상품코드의 시작
public static final String PACKER_PRD_CODE_TO     = "PACKER_PRD_CODE_TO";   // 패커 상품코드의 끝
```

#### 범위 설정 - 바코드 상품코드
```java
public static final String BARCODEGOODS_FROM      = "BARCODEGOODS_FROM";    // 바코드의 상품코드 시작
public static final String BARCODEGOODS_TO        = "BARCODEGOODS_TO";      // 바코드의 상품코드 끝
```

#### 범위 설정 - 중량
```java
public static final String WEIGHT_FROM            = "WEIGHT_FROM";          // 중량 시작
public static final String WEIGHT_TO              = "WEIGHT_TO";            // 중량 끝
```

#### 범위 설정 - 제조일
```java
public static final String MAKINGDATE_FROM        = "MAKINGDATE_FROM";      // 제조일 시작
public static final String MAKINGDATE_TO          = "MAKINGDATE_TO";        // 제조일 끝
```

#### 범위 설정 - 박스번호
```java
public static final String BOXSERIAL_FROM         = "BOXSERIAL_FROM";       // 박스번호 시작
public static final String BOXSERIAL_TO           = "BOXSERIAL_TO";         // 박스번호 끝
```

#### 상태 정보
```java
public static final String STATUS                 = "STATUS";               // 상태값, 사용여부 (Y, N)
```

### 5. GOODS_WET 테이블 컬럼 (94-124줄)

계근(무게 측정) 작업 정보를 저장하는 컬럼들입니다.

#### 기본 정보
```java
public static final String GOODS_WET_ID           = "GOODS_WET_ID";         // 계근 ID
```

#### 중량 정보
```java
public static final String WEIGHT                 = "WEIGHT";               // 중량, 소숫점 2자리
public static final String WEIGHT_UNIT            = "WEIGHT_UNIT";          // 중량 단위(LB, KG)
```

#### 바코드 및 박스 정보
```java
public static final String BARCODE                = "BARCODE";              // 스캔한 바코드
public static final String MAKINGDATE             = "MAKINGDATE";           // 제조일
public static final String BOXSERIAL              = "BOXSERIAL";            // 박스번호
public static final String BOX_CNT                = "BOX_CNT";              // 계근 순서번호
public static final String BOX_ORDER              = "BOX_ORDER";            // 박스 순서
public static final String LAST_BOX_ORDER         = "LAST_BOX_ORDER";       // 마지막 박스 순서
```

#### 중복 및 저장 정보
```java
public static final String DUPLICATE              = "DUPLICATE";            // 중복스캔
public static final String CLIENT_TYPE            = "CLIENT_TYPE";          // 거래처 타입
```

#### 창고 및 용도 정보
```java
public static final String WH_AREA                = "WH_AREA";              // 창고 구역
public static final String USE_NAME               = "USE_NAME";             // 용도명
public static final String USE_CODE               = "USE_CODE";             // 용도코드
```

#### 원산지 및 점포 정보
```java
public static final String CT_NAME                = "CT_NAME";              // 원산지명
public static final String STORE_CODE             = "STORE_CODE";           // 점포코드
public static final String SHELF_LIFE             = "SHELF_LIFE";           // 유통기한
```

## 주요 특징

1. **타입 안정성**: 모든 컬럼명을 상수로 관리하여 오타 방지
2. **유지보수성**: 컬럼명 변경 시 한 곳에서만 수정
3. **가독성**: 주석을 통한 명확한 컬럼 설명
4. **재사용성**: 공통 컬럼을 여러 테이블에서 재사용
5. **구조화**: 테이블별, 기능별로 컬럼을 그룹화

## 데이터 흐름

1. **출하 프로세스**
   - TB_SHIPMENT: 출하 대상 정보 저장
   - TB_GOODS_WET: 계근 작업 내역 기록
   - TB_BARCODE_INFO: 바코드 정보 참조

2. **바코드 스캔**
   - TB_BARCODE_INFO에서 바코드 형식 확인
   - TB_GOODS_WET에 스캔 결과 저장

3. **계근 작업**
   - TB_GOODS_WET에 무게 정보 저장
   - TB_GOODS_WET_PRODUCTION_CALC에서 생산 계산

## 관련 파일

- **DBHelper.java**: 데이터베이스 생성 및 관리
- **DBHandler.java**: 데이터베이스 CRUD 작업
- **Shipments_Info.java**: 출하 정보 데이터 모델
- **Barcodes_Info.java**: 바코드 정보 데이터 모델
- **Goodswets_Info.java**: 계근 정보 데이터 모델

## 사용 예시

```java
// 테이블명 사용
String tableName = DBInfo.TABLE_NAME_SHIPMENT;

// 컬럼명 사용
String itemCode = DBInfo.ITEM_CODE;
String brandCode = DBInfo.BRAND_CODE;

// SQL 쿼리에서 사용
String query = "SELECT " + DBInfo.ITEM_CODE + ", " + DBInfo.ITEM_NAME +
               " FROM " + DBInfo.TABLE_NAME_SHIPMENT;
```

## 주의사항

1. 이 클래스의 상수는 읽기 전용이며 수정하지 않아야 합니다
2. 새로운 컬럼 추가 시 관련 테이블 섹션에 추가해야 합니다
3. 컬럼명 변경 시 DBHelper, DBHandler 파일도 함께 수정해야 합니다
4. 공통 컬럼은 중복 정의하지 않고 재사용합니다
