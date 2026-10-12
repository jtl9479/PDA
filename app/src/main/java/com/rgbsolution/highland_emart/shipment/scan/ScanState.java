package com.rgbsolution.highland_emart.shipment.scan;

import com.rgbsolution.highland_emart.items.Barcodes_Info;

/**
 * 스캔 흐름 상태 (개발/77 Step 8) — BixolonShipmentActivity 필드를 그대로 옮김 (초기값 동일)
 * Activity 와 ScanProcessor 가 같은 객체를 공유한다.
 */
public class ScanState {

    /**
     * 현재 계근 작업 중인 리스트 위치
     * -1: 미선택, 0~n: arSM 리스트 인덱스
     */
    public int current_work_position;

    /**
     * 작업 모드 플래그
     * 1: 바코드 스캔 모드
     * 0: 수기 입력 모드 (중량 직접 입력)
     * 2: 상품코드 입력 모드
     */
    public int work_flag = 1;

    /**
     * 스캔 순서 플래그
     * true: 상품 바코드 스캔 차례
     * false: BL번호 바코드 스캔 차례
     */
    public boolean scan_flag = true;

    /** 현재 작업 중인 바코드 정보 (S_BARCODE_INFO 테이블 데이터) */
    public Barcodes_Info work_item_bi_info;

    /** 현재 작업 중인 패커 상품 코드 */
    public String work_ppcode = "";

    /** 현재 작업 중인 BL 번호 */
    public String work_bl_no = "";

    /** 스캔된 전체 바코드 문자열 (중량, 제조일 추출용) */
    public String work_item_fullbarcode = "";

    /** 바코드 상품 코드 (BARCODEGOODS) */
    public String work_item_barcodegoods = "";

    /** 소비기한 전송용 변수 */
    public String expiryDayTrans = "";

    /** 다이얼로그 중복 표시 방지 플래그 */
    public boolean alert_flag = false;

    /** 다이얼로그 표시 중 플래그 (중복 처리 방지) */
    public boolean dialog_flag = false;

    /** setBarcodeMsg 마지막 처리 시각 (중복 호출 방지용) */
    public long lastBarcodeProcessedTime = 0;

    /** setBarcodeMsg 마지막 처리 바코드 값 (같은 바코드 여부 판별용) */
    public String lastProcessedBarcode = "";
}
