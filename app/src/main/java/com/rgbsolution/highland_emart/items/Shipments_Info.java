package com.rgbsolution.highland_emart.items;

public class Shipments_Info {

    public String SHIPMENT_ID					= "";				    // 출하대상 ID
    public String GI_H_ID 						= "";					// GI_H_ID
    public String GI_D_ID 						= "";					// 출고번호(출고상세번호id 값)
    public String EOI_ID						= "";					// 이마트 출하번호(발주번호)
    public String ITEM_CODE 					= "";					// 상품코드
    public String ITEM_NAME 					= "";					// 상품명
    public String EMARTITEM_CODE				= "";				    // 이마트 상품코드
    public String EMARTITEM					    = "";					// 이마트 상품명
    public String GI_REQ_PKG 					= "";					// 출하요청수량
    public String GI_REQ_QTY					= "";					// 출하요청중량
    public String AMOUNT 						= "";					// 출하상품금액
    public String GOODS_R_ID 					= "";					// 입고번호
    public String GR_REF_NO 					= "";					// 창고입고번호
    public String GI_REQ_DATE 					= "";				    // 출하요청일
    public String BL_NO						    = "";					// BL 번호
    public String BRAND_CODE 					= "";					// 브랜드코드
    public String BRANDNAME 					= "";					// 브랜드명
    public String CLIENT_CODE 					= "";				    // 출고업체코드
    public String CLIENTNAME 					= "";				    // 출고업체명
    public String CENTERNAME					= "";					// 센터명
    public String ITEM_SPEC	 				    = "";					// 스펙
    public String CT_CODE 						= "";					// 원산지
    public String IMPORT_ID_NO					= "";				    // 수입식별번호
    public String PACKER_CODE 					= "";				    // 패커 코드
    public String PACKERNAME 					= "";				    // 패커 이름
    public String PACKER_PRODUCT_CODE 			= "";		            // 패커 상품코드
    public String BARCODE_TYPE					= "";				    // 바코드 유형
    public String ITEM_TYPE					    = "";					// 삼품 타입
    public String PACKWEIGHT					= "";					// 팩 중량
    public String BARCODEGOODS 				    = "";				    // 바코드의 상품코드
    public String STORE_IN_DATE                 = "";                   // 납품일자
    public int PACKING_QTY                      = 0;                    // 계근 수량
    public double GI_QTY                        = 0;                    // 계근 중량
    public int SAVE_CNT                         = 0;                    // 전송 개수
    public String SAVE_TYPE 					= "";		     		// 저장 여부
    public int WORK_FLAG                        = 0;                    // 현재 작업 여부
    public String EMARTLOGIS_CODE               = "";                   // 납품코드
    public String EMARTLOGIS_NAME               = "";                   // 납품명

    public String WH_AREA                       = "";
    public String USE_NAME                       = "";
    public String USE_CODE                       = "";
    public String CT_NAME                       = "";
    public String STORE_CODE                    = "";
    public String EMART_PLANT_CODE              = ""; // emart 공장코드

    public String PROC_ITEM_CODE               = "";
    public String PROC_ITEM_NAME               = "";
    public String LAST_BOX_ORDER               = "";

    public int getWORK_FLAG() {
        return WORK_FLAG;
    }

    public String getEMART_PLANT_CODE() {
        return EMART_PLANT_CODE;
    }

    public void setEMART_PLANT_CODE(String EMART_PLANT_CODE) {
        this.EMART_PLANT_CODE = EMART_PLANT_CODE;
    }

    public String getEMARTLOGIS_CODE() { return EMARTLOGIS_CODE; }

    public void setEMARTLOGIS_CODE(String EMARTLOGIS_CODE) { this.EMARTLOGIS_CODE = EMARTLOGIS_CODE; }

    public String getEMARTLOGIS_NAME() { return EMARTLOGIS_NAME; }

    public void setEMARTLOGIS_NAME(String EMARTLOGIS_NAME) { this.EMARTLOGIS_NAME = EMARTLOGIS_NAME; }

    public String getSTORE_IN_DATE() { return STORE_IN_DATE; }

    public void setSTORE_IN_DATE(String STORE_IN_DATE) { this.STORE_IN_DATE = STORE_IN_DATE; }

    public int isWORK_FLAG() {
        return WORK_FLAG;
    }

    public void setWORK_FLAG(int WORK_FLAG) {
        this.WORK_FLAG = WORK_FLAG;
    }

    public String getGI_H_ID() {
        return GI_H_ID;
    }

    public void setGI_H_ID(String GI_H_ID) {
        this.GI_H_ID = GI_H_ID;
    }

    public String getGI_D_ID() {
        return GI_D_ID;
    }

    public void setGI_D_ID(String GI_D_ID) {
        this.GI_D_ID = GI_D_ID;
    }

    public String getEOI_ID() {
        return EOI_ID;
    }

    public void setEOI_ID(String EOI_ID) {
        this.EOI_ID = EOI_ID;
    }

    public String getITEM_CODE() {
        return ITEM_CODE;
    }

    public void setITEM_CODE(String ITEM_CODE) {
        this.ITEM_CODE = ITEM_CODE;
    }

    public String getITEM_NAME() {
        return ITEM_NAME;
    }

    public void setITEM_NAME(String ITEM_NAME) {
        this.ITEM_NAME = ITEM_NAME;
    }

    public String getEMARTITEM_CODE() {
        return EMARTITEM_CODE;
    }

    public void setEMARTITEM_CODE(String EMARTITEM_CODE) {
        this.EMARTITEM_CODE = EMARTITEM_CODE;
    }

    public String getEMARTITEM() {
        return EMARTITEM;
    }

    public void setEMARTITEM(String EMARTITEM) {
        this.EMARTITEM = EMARTITEM;
    }

    public String getGI_REQ_PKG() {
        return GI_REQ_PKG;
    }

    public void setGI_REQ_PKG(String GI_REQ_PKG) {
        this.GI_REQ_PKG = GI_REQ_PKG;
    }

    public String getGI_REQ_QTY() {
        return GI_REQ_QTY;
    }

    public void setGI_REQ_QTY(String GI_REQ_QTY) {
        this.GI_REQ_QTY = GI_REQ_QTY;
    }

    public String getAMOUNT() {
        return AMOUNT;
    }

    public void setAMOUNT(String AMOUNT) {
        this.AMOUNT = AMOUNT;
    }

    public String getGOODS_R_ID() {
        return GOODS_R_ID;
    }

    public void setGOODS_R_ID(String GOODS_R_ID) {
        this.GOODS_R_ID = GOODS_R_ID;
    }

    public String getGR_REF_NO() {
        return GR_REF_NO;
    }

    public void setGR_REF_NO(String GR_REF_NO) {
        this.GR_REF_NO = GR_REF_NO;
    }

    public String getGI_REQ_DATE() {
        return GI_REQ_DATE;
    }

    public void setGI_REQ_DATE(String GI_REQ_DATE) {
        this.GI_REQ_DATE = GI_REQ_DATE;
    }

    public String getBL_NO() {
        return BL_NO;
    }

    public void setBL_NO(String BL_NO) {
        this.BL_NO = BL_NO;
    }

    public String getBRAND_CODE() {
        return BRAND_CODE;
    }

    public void setBRAND_CODE(String BRAND_CODE) {
        this.BRAND_CODE = BRAND_CODE;
    }

    public String getBRANDNAME() {
        return BRANDNAME;
    }

    public void setBRANDNAME(String BRAND_NAME) {
        this.BRANDNAME = BRAND_NAME;
    }

    public String getCLIENT_CODE() {
        return CLIENT_CODE;
    }

    public void setCLIENT_CODE(String CLIENT_CODE) {
        this.CLIENT_CODE = CLIENT_CODE;
    }

    public String getCLIENTNAME() {
        return CLIENTNAME;
    }

    public void setCLIENTNAME(String CLIENT_NAME) {
        this.CLIENTNAME = CLIENT_NAME;
    }

    public String getCENTERNAME() {
        return CENTERNAME;
    }

    public void setCENTERNAME(String CENTERNAME) {
        this.CENTERNAME = CENTERNAME;
    }

    public String getITEM_SPEC() {
        return ITEM_SPEC;
    }

    public void setITEM_SPEC(String ITEM_SPEC) {
        this.ITEM_SPEC = ITEM_SPEC;
    }

    public String getCT_CODE() {
        return CT_CODE;
    }

    public void setCT_CODE(String CT_CODE) {
        this.CT_CODE = CT_CODE;
    }

    public String getIMPORT_ID_NO() {
        return IMPORT_ID_NO;
    }

    public void setIMPORT_ID_NO(String IMPORT_ID_NO) {
        this.IMPORT_ID_NO = IMPORT_ID_NO;
    }

    public String getPACKER_CODE() {
        return PACKER_CODE;
    }

    public void setPACKER_CODE(String PACKER_CODE) {
        this.PACKER_CODE = PACKER_CODE;
    }

    public String getPACKERNAME() {
        return PACKERNAME;
    }

    public void setPACKERNAME(String PACKER_NAME) {
        this.PACKERNAME = PACKER_NAME;
    }

    public String getPACKER_PRODUCT_CODE() {
        return PACKER_PRODUCT_CODE;
    }

    public void setPACKER_PRODUCT_CODE(String PACKER_PRODUCT_CODE) {
        this.PACKER_PRODUCT_CODE = PACKER_PRODUCT_CODE;
    }

    public String getBARCODE_TYPE() {
        return BARCODE_TYPE;
    }

    public void setBARCODE_TYPE(String BARCODE_TYPE) {
        this.BARCODE_TYPE = BARCODE_TYPE;
    }

    public String getITEM_TYPE() {
        return ITEM_TYPE;
    }

    public void setITEM_TYPE(String ITEM_TYPE) {
        this.ITEM_TYPE = ITEM_TYPE;
    }

    public String getPACKWEIGHT() {
        return PACKWEIGHT;
    }

    public void setPACKWEIGHT(String PACKWEIGHT) {
        this.PACKWEIGHT = PACKWEIGHT;
    }

    public String getBARCODEGOODS() {
        return BARCODEGOODS;
    }

    public void setBARCODEGOODS(String BARCODEGOODS) {
        this.BARCODEGOODS = BARCODEGOODS;
    }

    public double getGI_QTY() {
        return GI_QTY;
    }

    public void setGI_QTY(double GI_QTY) {
        this.GI_QTY = GI_QTY;
    }

    public int getPACKING_QTY() {
        return PACKING_QTY;
    }

    public void setPACKING_QTY(int PACKING_QTY) {
        this.PACKING_QTY = PACKING_QTY;
    }

    public int getSAVE_CNT() {
        return SAVE_CNT;
    }

    public void setSAVE_CNT(int SAVE_CNT) {
        this.SAVE_CNT = SAVE_CNT;
    }

    public String getSAVE_TYPE() {
        return SAVE_TYPE;
    }

    public void setSAVE_TYPE(String SAVE_TYPE) {
        this.SAVE_TYPE = SAVE_TYPE;
    }

    public String getSHIPMENT_ID() {
        return SHIPMENT_ID;
    }

    public void setSHIPMENT_ID(String SHIPMENT_ID) {
        this.SHIPMENT_ID = SHIPMENT_ID;
    }

    ////
    public String getWH_AREA() {
        return WH_AREA;
    }

    public void setWH_AREA(String WH_AREA) {
        this.WH_AREA = WH_AREA;
    }

    public String getUSE_NAME() {
        return USE_NAME;
    }

    public void setUSE_NAME(String USE_NAME) {
        this.USE_NAME = USE_NAME;
    }

    public String getUSE_CODE() {
        return USE_CODE;
    }

    public void setUSE_CODE(String USE_CODE) {
        this.USE_CODE = USE_CODE;
    }

    public String getCT_NAME() {
        return CT_NAME;
    }

    public void setCT_NAME(String CT_NAME) {
        this.CT_NAME = CT_NAME;
    }

    public String getSTORE_CODE() {
        return STORE_CODE;
    }

    public void setSTORE_CODE(String STORE_CODE) {
        this.STORE_CODE = STORE_CODE;
    }
    public String getPROC_ITEM_CODE() {
        return PROC_ITEM_CODE;
    }

    public void setPROC_ITEM_CODE(String PROC_ITEM_CODE) {
        this.PROC_ITEM_CODE = PROC_ITEM_CODE;
    }

    public String getPROC_ITEM_NAME() {
        return PROC_ITEM_NAME;
    }

    public void setPROC_ITEM_NAME(String PROC_ITEM_NAME) {
        this.PROC_ITEM_NAME = PROC_ITEM_NAME;
    }

    public void setLAST_BOX_ORDER(String last_box_order) {
        this.LAST_BOX_ORDER = last_box_order;
    }

    public String getLAST_BOX_ORDER() {
        return LAST_BOX_ORDER;
    }
}
