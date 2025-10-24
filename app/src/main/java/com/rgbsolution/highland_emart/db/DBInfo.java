package com.rgbsolution.highland_emart.db;

public class DBInfo {

	public static final String TABLE_NAME_SHIPMENT 			= "TB_SHIPMENT";
	public static final String TABLE_NAME_PRODUCTION		= "TB_PRODUCTION";
	public static final String TABLE_NAME_BARCODE_INFO 		= "TB_BARCODE_INFO";
	public static final String TABLE_NAME_GOODS_WET 		= "TB_GOODS_WET";
	public static final String TABLE_NAME_GOODS_WET_PRODUCTION_CALC = "TB_GOODS_WET_PRODUCTION_CALC";
	public static final String TABLE_NAME_COMPLETE_ITEM		= "TB_COMPLETE_ITEM";

	//	::::::::::::::: ↓ 공통 Columns ↓ ::::::::::::::::::
	public static final String GI_D_ID 						= "GI_D_ID";					// 출고번호(출고상세번호id 값)
	public static final String BRAND_CODE 					= "BRAND_CODE";				// 브랜드코드, 공통 Column
	public static final String PACKER_CLIENT_CODE 			= "PACKER_CLIENT_CODE";
	public static final String PACKER_PRODUCT_CODE 			= "PACKER_PRODUCT_CODE";	// 패커 상품코드
	public static final String REG_ID 						= "REG_ID";
	public static final String REG_DATE 					= "REG_DATE";
	public static final String REG_TIME 					= "REG_TIME";
	public static final String MEMO 						= "MEMO";
	//	::::::::::::::: ↑ 공통 Columns ↑ ::::::::::::::::::


	//	::::::::::::::: ↓ SHIPMENT Columns ↓ ::::::::::::::::::
	public static final String SHIPMENT_ID					= "SHIPMENT_ID";				// 출하대상 ID
	public static final String GI_H_ID 						= "GI_H_ID";					// GI_H_ID
	//	public static final String GI_D_ID 						= "GI_D_ID";					// 출고번호(출고상세번호id 값)
	public static final String EOI_ID						= "EOI_ID";						// 이마트 출하번호(발주번호)
	public static final String ITEM_CODE 					= "ITEM_CODE";					// 상품코드
	public static final String ITEM_NAME 					= "ITEM_NAME";					// 상품명
	public static final String EMARTITEM_CODE				= "EMARTITEM_CODE";				// 이마트 상품코드
	public static final String EMARTITEM					= "EMARTITEM";					// 이마트 상품명
	public static final String GI_REQ_PKG 					= "GI_REQ_PKG";					// 출하요청수량
	public static final String GI_REQ_QTY					= "GI_REQ_QTY";					// 출하요청중량
	public static final String AMOUNT 						= "AMOUNT";						// 출하상품금액
	public static final String GOODS_R_ID 					= "GOODS_R_ID";					// 입고번호
	public static final String GR_REF_NO 					= "GR_REF_NO";					// 창고입고번호
	public static final String GI_REQ_DATE 					= "GI_REQ_DATE";				// 출하요청일
	public static final String BL_NO						= "BL_NO";						// BL 번호
	//	public static final String BRAND_CODE 					= "BRAND_CODE";					// 브랜드코드
	public static final String BRANDNAME 					= "BRANDNAME";					// 브랜드명
	public static final String CLIENT_CODE 					= "CLIENT_CODE";				// 출고업체코드
	public static final String CLIENTNAME 					= "CLIENTNAME";					// 출고업체명
	public static final String CENTERNAME					= "CENTERNAME";					// 센터명
	public static final String ITEM_SPEC	 				= "ITEM_SPEC";					// 스펙
	public static final String CT_CODE 						= "CT_CODE";					// 원산지
	public static final String IMPORT_ID_NO					= "IMPORT_ID_NO";				// 수입식별번호
	public static final String PACKER_CODE 					= "PACKER_CODE";				// 패커 코드
	public static final String PACKERNAME 					= "PACKERNAME";					// 패커 이름
	//	public static final String PACKER_PRODUCT_CODE 			= "PACKER_PRODUCT_CODE";		// 패커 상품코드
	public static final String BARCODE_TYPE					= "BARCODE_TYPE";				// 바코드 유형
	public static final String ITEM_TYPE					= "ITEM_TYPE";					// 상품 타입
	public static final String PACKWEIGHT					= "PACKWEIGHT";					// 팩 중량
	//	public static final String BARCODEGOODS 				= "BARCODEGOODS";				// 바코드의 상품코드
	public static final String GI_QTY						= "GI_QTY";						// 계근 중량
	public static final String PACKING_QTY					= "PACKING_QTY";				// 계근 수량
	public static final String STORE_IN_DATE				= "STORE_IN_DATE";				// 납품일자
	public static final String EMARTLOGIS_CODE				= "EMARTLOGIS_CODE";			// 납품코드
	public static final String EMARTLOGIS_NAME				= "EMARTLOGIS_NAME";			// 납품명
	public static final String SAVE_TYPE 					= "SAVE_TYPE";		     		// 저장 여부
	public static final String EMART_PLANT_CODE 			= "EMART_PLANT_CODE";		   	// 이마트 가공장 코드
	//	::::::::::::::: ↑ SHIPMENT Columns ↑ ::::::::::::::::::


	//	::::::::::::::: ↓ BARCODEINFO Columns ↓ ::::::::::::::::::
	public static final String BARCODE_INFO_ID 				= "BARCODE_INFO_ID";			// ID키
	//	public static final String BRAND_CODE 					= "BRAND_CODE";					// 브랜드코드
	//	public static final String PACKER_CLIENT_CODE 			= "PACKER_CLIENT_CODE";			// 패커 거래처 코드
	//	public static final String PACKER_PRODUCT_CODE 			= "PACKER_PRODUCT_CODE";		// 패커 상품코드
	public static final String PACKER_PRD_NAME 				= "PACKER_PRD_NAME";			// 패커 상품명
	//	public static final String ITEM_CODE					= "ITEM_CODE"					// 아이템코드
	public static final String ITEM_NAME_KR					= "ITEM_NAME_KR";				// 한글 상품명
	public static final String BARCODEGOODS 				= "BARCODEGOODS";				// 바코드의 상품코드
	public static final String BASEUNIT 					= "BASEUNIT";					// LB, KG 구분
	public static final String ZEROPOINT 					= "ZEROPOINT";					// 소수점 자리수
	public static final String PACKER_PRD_CODE_FROM 		= "PACKER_PRD_CODE_FROM";		// 패커 상품코드의 시작
	public static final String PACKER_PRD_CODE_TO 			= "PACKER_PRD_CODE_TO";			// 패커 상품코드의 끝
	public static final String BARCODEGOODS_FROM 			= "BARCODEGOODS_FROM";			// 바코드의 상품코드 시작
	public static final String BARCODEGOODS_TO 				= "BARCODEGOODS_TO";			// 바코드의 상품코드 끝
	public static final String WEIGHT_FROM 					= "WEIGHT_FROM";				// 중량 시작
	public static final String WEIGHT_TO 					= "WEIGHT_TO";					// 중량 끝
	public static final String MAKINGDATE_FROM				= "MAKINGDATE_FROM";			// 제조일 시작
	public static final String MAKINGDATE_TO				= "MAKINGDATE_TO";				// 제조일 끝
	public static final String BOXSERIAL_FROM				= "BOXSERIAL_FROM";				// 박스번호 시작
	public static final String BOXSERIAL_TO					= "BOXSERIAL_TO";				// 박스번호 끝
	public static final String STATUS 						= "STATUS";						// 상태값, 사용여부 (Y, N)
	//	public static final String REG_ID 						= "REG_ID";						// 등록자 ID
	//	public static final String REG_DATE 					= "REG_DATE";					// 등록 날짜
	//	public static final String REG_TIME 					= "REG_TIME";					// 등록 시간
	//	public static final String MEMO 						= "MEMO";						// 메모, 관리 목적 컬럼
	//	::::::::::::::: ↑ BARCODEINFO Columns ↑ ::::::::::::::::::


	//	::::::::::::::: ↓ GOODS_WET Columns ↓ ::::::::::::::::::
	public static final String GOODS_WET_ID					= "GOODS_WET_ID";
	//	public static final String GI_D_ID 						= "GI_D_ID";					// 출고번호(출고상세번호id 값)
	public static final String WEIGHT						= "WEIGHT";						// 중량, 소숫점 2자리
	public static final String WEIGHT_UNIT					= "WEIGHT_UNIT";				// 중량 단위(LB, KG)
	//	public static final String PACKER_PRODUCT_CODE 			= "PACKER_PRODUCT_CODE";		// 패커 상품코드
	public static final String BARCODE						= "BARCODE";					// 스캔한 바코드
	//	public static final String PACKER_CLIENT_CODE 			= "PACKER_CLIENT_CODE";			// 패커 거래처코드
	public static final String MAKINGDATE					= "MAKINGDATE";					// 제조일
	public static final String BOXSERIAL					= "BOXSERIAL";					// 박스번호
	public static final String BOX_CNT						= "BOX_CNT";					// 계근 순서번호
	//	public static final String EMARTITEM_CODE				= "EMARTITEM_CODE";				// 이마트 상품코드
	//	public static final String EMARTITEM					= "EMARTITEM";					// 이마트 상품명
	//	public static final String ITEM_CODE 					= "ITEM_CODE";					// 상품코드
	//	public static final String BRAND_CODE 					= "BRAND_CODE";					// 브랜드코드
	//	public static final String REG_ID 						= "REG_ID";						// 등록자 ID
	//	public static final String REG_DATE 					= "REG_DATE";					// 등록 날짜
	//	public static final String REG_TIME 					= "REG_TIME";					// 등록 시간
	//	public static final String SAVE_TYPE 					= "SAVE_TYPE";		     		// 전송 여부
	//	public static final String MEMO 						= "MEMO";						// 메모, 관리 목적 컬럼
	public static final String DUPLICATE					= "DUPLICATE";					// 중복스캔
	public static final String CLIENT_TYPE					= "CLIENT_TYPE";
	public static final String BOX_ORDER					= "BOX_ORDER";
	public static final String WH_AREA						= "WH_AREA";
	public static final String USE_NAME						= "USE_NAME";
	public static final String USE_CODE						= "USE_CODE";
	public static final String CT_NAME						= "CT_NAME";
	public static final String STORE_CODE				    = "STORE_CODE";
	public static final String SHELF_LIFE				    = "SHELF_LIFE";
	public static final String LAST_BOX_ORDER				= "LAST_BOX_ORDER";

	//	::::::::::::::: ↑ GOODS_WET Columns ↑ ::::::::::::::::::
}
