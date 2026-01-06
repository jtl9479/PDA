package com.rgbsolution.highland_emart.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ProgressDlgShipSearch - 출하대상 조회 AsyncTask
 *
 * 서버에서 출하대상 리스트를 다운로드하여 PDA 로컬 DB(SQLite)에 저장하는 비동기 작업 클래스
 *
 * ■ 주요 기능:
 *   1. searchType에 따라 해당 서버 URL로 출하대상 조회 요청
 *   2. 서버 응답 데이터 파싱 → Shipments_Info 객체 변환
 *   3. PDA 기존 데이터와 비교하여 추가/삭제 처리
 *   4. 완료 후 ProgressDlgBarcodeSearch 자동 실행
 *
 * ■ searchType별 출하 유형:
 *   - "0": 이마트 출하         → URL_SEARCH_SHIPMENT
 *   - "1": 생산 계근 (이노이천) → URL_SEARCH_PRODUCTION
 *   - "2": 홈플러스 출하        → URL_SEARCH_SHIPMENT_HOMEPLUS
 *   - "3": 도매업체 출하        → URL_SEARCH_SHIPMENT_WHOLESALE
 *   - "4": 비정량 출고          → URL_SEARCH_PRODUCTION_NONFIXED
 *   - "5": 홈플러스 비정량      → URL_SEARCH_HOMEPLUS_NONFIXED
 *   - "6": 롯데 출하            → URL_SEARCH_SHIPMENT_LOTTE
 *   - "7": 생산 계근 (라벨)     → URL_SEARCH_PRODUCTION_4LABEL
 *
 * ■ 서버 응답 형식:
 *   - row 구분자: ";;"
 *   - column 구분자: "::"
 *   - 예: "데이터1::데이터2::데이터3;;데이터4::데이터5::데이터6;;"
 *
 * ■ 호출 위치:
 *   - MainActivity.java의 각 "대상받기" 버튼 클릭 시
 *
 * @see ProgressDlgBarcodeSearch 출하대상 조회 완료 후 자동 실행되는 바코드 정보 조회
 * @see Shipments_Info 출하대상 정보 DTO
 */
public class ProgressDlgShipSearch extends AsyncTask<Integer, String, Integer> {

    private static final String TAG = "ProgressDlgShipSearch";

    /** 로딩 다이얼로그 - 조회 중 사용자에게 진행 상태 표시 */
    ProgressDialog pDialog;

    /** 서버에서 다운로드한 출하대상 리스트 */
    ArrayList<Shipments_Info> list_si_info;

    /** 컨텍스트 - MainActivity에서 전달받음 */
    private Context mContext;

    /** 서버 응답 데이터 (문자열) */
    String receiveData = "";

    /**
     * 생성자
     * @param context MainActivity 컨텍스트
     */
    public ProgressDlgShipSearch(Context context) {
        mContext = context;
    }

    /**
     * 백그라운드 작업 시작 전 실행 (UI 스레드)
     * - 로딩 다이얼로그 표시
     */
    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("대상 조회중 입니다.");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);  // 뒤로가기 버튼으로 취소 불가
        pDialog.show();
        super.onPreExecute();
    }

    /**
     * 백그라운드 작업 실행 (백그라운드 스레드)
     * - 서버에서 출하대상 데이터 다운로드
     * - 응답 데이터 파싱 및 DB 저장
     *
     * @param params 사용하지 않음
     * @return 0 (성공)
     */
    @Override
    protected Integer doInBackground(Integer... params) {
        try {
            Log.d(TAG, "대상 조회 시작");

            // ========================================
            // 1. PDA 로컬 DB에 저장된 기존 출하대상 조회
            // ========================================
            ArrayList<Shipments_Info> list_si_pda = DBHandler.selectqueryAllShipment(mContext);
            Log.d(TAG, "============== PDA List's size : " + list_si_pda.size() + "================");

            // ========================================
            // 2. 서버 조회 조건 생성 (WHERE절)
            // ========================================
            // 기본 조건: 선택한 날짜의 출하대상만 조회
            String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'";

            // ========================================
            // 3. searchType별 서버 URL 호출
            // ========================================

            // ----- searchType "0": 이마트 출하대상 -----
            if(Common.searchType.equals("0")){
                // 창고별 조건 추가 (이마트는 창고 구분 있음)
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천1센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }

                // 서버 API 호출 (DB: inno)
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT);
                Log.d(TAG, "============== 출하리스트 조회조건 : " + data + "================");

            // ----- searchType "1": 생산 계근 (이노이천) -----
            }else if(Common.searchType.equals("1")){
                // 생산 계근은 창고 조건 없음
                receiveData = HttpHelper.getInstance().sendDataDb(data, "Inno", "search_shipment", Common.URL_SEARCH_PRODUCTION);
                Log.d(TAG, "============== 생산리스트 조회조건 : " + data + "================");

            // ----- searchType "2": 홈플러스 출하대상 -----
            }else if(Common.searchType.equals("2")){
                // 창고별 조건 추가
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천1센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_HOMEPLUS);
                Log.d(TAG, "============== 출하리스트 조회조건 홈플러스로 들어옴 : " + data + "================");

            // ----- searchType "3": 도매업체 출하대상 -----
            }else if(Common.searchType.equals("3")){
                // 창고별 조건 추가
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천1센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_WHOLESALE);
                Log.d(TAG, "============== 출하리스트 조회조건 홈플러스로 들어옴 : " + data + "================");

            // ----- searchType "4": 비정량 출고 -----
            }else if(Common.searchType.equals("4")){
                // 비정량은 박스별 중량이 다른 상품 (창고 조건 없음)
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_PRODUCTION_NONFIXED);
                Log.d(TAG, "============== 출하리스트 조회조건 비정량출고로 들어옴 : " + data + "================");

            // ----- searchType "5": 홈플러스 비정량 출고 -----
            }else if(Common.searchType.equals("5")){
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_HOMEPLUS_NONFIXED);
                Log.d(TAG, "============== 출하리스트 조회조건 홈플러스 비정량출고로 들어옴 : " + data + "================");

            // ----- searchType "6": 롯데 출하대상 -----
            }else if(Common.searchType.equals("6")){
                // 창고별 조건 추가
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }
                Log.d(TAG, "==============  Common.URL_SEARCH_SHIPMENT_LOTTE : " + Common.URL_SEARCH_SHIPMENT_LOTTE + "================");
                // 롯데는 DB: highland 사용
                receiveData = HttpHelper.getInstance().sendDataDb(data, "highland", "search_shipment", Common.URL_SEARCH_SHIPMENT_LOTTE);
                Log.d(TAG, "============== 출하리스트 조회조건 롯데 들어옴 : " + data + "================");

            // ----- searchType "7": 생산 계근 (라벨 출력용) -----
            }else if(Common.searchType.equals("7")){
                receiveData = HttpHelper.getInstance().sendDataDb(data, "Inno", "search_shipment", Common.URL_SEARCH_PRODUCTION_4LABEL);
                Log.d(TAG, "============== 생산대상리스트(라벨) 조회 : " + data + "================");
            }

            Log.d(TAG, "============== Common.searchType : " + Common.searchType + "================");

            // ========================================
            // 4. 서버 응답 데이터 전처리
            // ========================================
            // 개행문자 제거
            receiveData = receiveData.replace("\r\n", "");
            receiveData = receiveData.replace("\n", "");
            if (Common.D) {
                Log.d(TAG, "receiveData : " + receiveData.toString());
            }

            // ========================================
            // 5. 서버 응답 데이터 파싱
            // ========================================
            // row 단위로 분리 (구분자: ";;")
            String[] result = receiveData.split(";;");
            list_si_info = new ArrayList<Shipments_Info>();
            String[] temp;
            Shipments_Info si;
            if (Common.D) {
                Log.d(TAG, "result's Count : " + result.length);
            }

            // 각 row를 Shipments_Info 객체로 변환
            if (result.length > 0) {
                for (String s : result) {
                    // column 단위로 분리 (구분자: "::")
                    temp = s.split("::");
                    si = new Shipments_Info();

                    // ----- 기본 필드 설정 (0~7) -----
                    si.setGI_H_ID(temp[0].toString());           // 출하헤더ID
                    si.setGI_D_ID(temp[1].toString());           // 출하상세ID (PK)
                    si.setEOI_ID(temp[2].toString());            // 주문ID
                    si.setITEM_CODE(temp[3].toString());         // 품목코드
                    si.setITEM_NAME(temp[4].toString());         // 품목명
                    si.setEMARTITEM_CODE(temp[5].toString());    // 이마트품목코드
                    si.setEMARTITEM(temp[6].toString());         // 이마트품목명
                    si.setGI_REQ_PKG(temp[7].toString());        // 요청수량 (박스)

                    // ----- 요청중량 소수점 처리 (8) -----
                    // 소수점 4자리 이상이면 1자리로 절사
                    String[] split_qty = temp[8].split("[.]");
                    if (split_qty.length > 1) {
                        if (split_qty[1].length() > 3) {
                            double double_qty = Double.parseDouble(temp[8].toString());
                            if (Common.D) {
                                Log.d(TAG, "string_qty : " + temp[8].toString());
                                Log.d(TAG, "double_qty : " + double_qty);
                            }
                            // 소수점 1자리까지 절사 (예: 12.3456 → 12.3)
                            double_qty = Math.floor(double_qty * 10);
                            double result_qty = double_qty / 10.0;
                            temp[8] = String.valueOf(result_qty);
                            if (Common.D) {
                                Log.d(TAG, "result_qty : " + result_qty);
                                Log.d(TAG, "result_string_qty : " + temp[8].toString());
                            }
                        } else {
                            if (Common.D) {
                                Log.d(TAG, "3자리이하_qty : " + temp[8].toString());
                            }
                        }
                    }

                    // ----- 추가 필드 설정 (8~31) -----
                    si.setGI_REQ_QTY(temp[8].toString());        // 요청중량 (kg)
                    si.setAMOUNT(temp[9].toString());            // 금액
                    si.setGOODS_R_ID(temp[10].toString());       // 입고ID
                    si.setGR_REF_NO(temp[11].toString());        // 입고참조번호
                    si.setGI_REQ_DATE(temp[12].toString());      // 요청일자
                    si.setBL_NO(temp[13].toString());            // BL번호
                    si.setBRAND_CODE(temp[14].toString());       // 브랜드코드
                    si.setBRANDNAME(temp[15].toString());        // 브랜드명
                    si.setCLIENT_CODE(temp[16].toString());      // 거래처코드
                    si.setCLIENTNAME(temp[17].toString());       // 거래처명 (지점명)
                    si.setCENTERNAME(temp[18].toString());       // 센터명
                    si.setITEM_SPEC(temp[19].toString());        // 품목규격
                    si.setCT_CODE(temp[20].toString());          // CT코드
                    si.setIMPORT_ID_NO(temp[21].toString());     // 수입신고번호
                    si.setPACKER_CODE(temp[22].toString());      // 패커코드
                    si.setPACKERNAME(temp[23].toString());       // 패커명
                    si.setPACKER_PRODUCT_CODE(temp[24].toString()); // 패커상품코드
                    si.setBARCODE_TYPE(temp[25].toString());     // 바코드타입 (M0~M9, E0~E3, L0 등)
                    si.setITEM_TYPE(temp[26].toString());        // 아이템타입 (W, S, J, B 등)
                    si.setPACKWEIGHT(temp[27].toString());       // 포장중량
                    si.setBARCODEGOODS(temp[28].toString());     // 바코드상품코드
                    si.setSTORE_IN_DATE(temp[29].toString());    // 입고일자
                    si.setEMARTLOGIS_CODE(temp[30].toString());  // 이마트물류코드
                    si.setEMARTLOGIS_NAME(temp[31].toString());  // 이마트물류명

                    // ----- searchType별 추가 필드 설정 (32~) -----
                    // 이마트(0), 비정량(4): 추가 6개 필드
                    if(Common.searchType.equals("0") || Common.searchType.equals("4")) {
                        si.setWH_AREA(temp[32].toString());          // 창고구역
                        si.setUSE_NAME(temp[33].toString());         // 용도명
                        si.setUSE_CODE(temp[34].toString());         // 용도코드
                        si.setCT_NAME(temp[35].toString());          // CT명
                        si.setSTORE_CODE(temp[36].toString());       // 점포코드
                        si.setEMART_PLANT_CODE(temp[37].toString()); // 이마트공장코드

                    // 홈플러스 비정량(5): 추가 5개 필드
                    } else if(Common.searchType.equals("5")) {
                        si.setWH_AREA(temp[32].toString());          // 창고구역
                        si.setUSE_NAME(temp[33].toString());         // 용도명
                        si.setUSE_CODE(temp[34].toString());         // 용도코드
                        si.setCT_NAME(temp[35].toString());          // CT명
                        si.setSTORE_CODE(temp[36].toString());       // 점포코드

                    // 롯데(6): 추가 2개 필드
                    } else if(Common.searchType.equals("6")) {
                        si.setWH_AREA(temp[32].toString());          // 창고구역
                        si.setLAST_BOX_ORDER(temp[33].toString());   // 마지막 박스순번 (1~9999 순환)
                    }

                    // 저장상태: "F" = 미전송 (False)
                    si.setSAVE_TYPE("F");

                    // 리스트에 추가
                    list_si_info.add(si);
                }
            }
            // ↑ 서버 출하리스트 다운로드 및 파싱 완료

            // ========================================
            // 6. PDA 기존 데이터와 비교 (동기화)
            // ========================================
            ArrayList<String> list_delete = new ArrayList<String>();      // 삭제할 GI_D_ID 목록
            ArrayList<Shipments_Info> list_insert = new ArrayList<Shipments_Info>(); // 추가할 출하대상 목록

            // ----- 삭제 대상 검색 -----
            // PDA에는 있지만 서버에는 없는 항목 → 삭제 대상
            for (int i = 0; i < list_si_pda.size(); i++) {
                int check = 0;
                for (int j = 0; j < list_si_info.size(); j++) {
                    if (list_si_pda.get(i).getGI_D_ID().equals(list_si_info.get(j).getGI_D_ID())) {
                        check++;
                        break;
                    }
                }
                if (check == 0) {
                    // 서버에 없는 항목 → 삭제 대상에 추가
                    list_delete.add(list_si_pda.get(i).getGI_D_ID());
                    Log.e(TAG, "==============================================");
                    Log.e(TAG, "================ 삭제 ItemInfo ================");
                    Log.e(TAG, "GI_D_ID : " + list_si_pda.get(i).getGI_D_ID());
                    Log.e(TAG, "==============================================");
                }
            }

            // ----- 추가 대상 검색 -----
            // 서버에는 있지만 PDA에는 없는 항목 → 추가 대상
            for (int i = 0; i < list_si_info.size(); i++) {
                int check = 0;
                for (int j = 0; j < list_si_pda.size(); j++) {
                    if (list_si_info.get(i).getGI_D_ID().equals(list_si_pda.get(j).getGI_D_ID())) {
                        check++;
                        break;
                    }
                }

                if (check == 0) {
                    // PDA에 없는 항목 → 추가 대상에 추가
                    list_insert.add(list_si_info.get(i));
                    Log.e(TAG, "==============================================");
                    Log.e(TAG, "================ 추가 ItemInfo ================");
                    Log.e(TAG, "GI_D_ID : " + list_si_info.get(i).getGI_D_ID());
                    Log.e(TAG, "==============================================");
                }
            }

            // ========================================
            // 7. DB 동기화 실행 (삭제 + 추가)
            // ========================================
            boolean refresh_result = DBHandler.refreshShipmentList(mContext, list_delete, list_insert);
            if (refresh_result) {
                Log.v(TAG, "Shipment Refresh 성공");
            } else {
                Log.v(TAG, "Shipment Refresh 실패");
            }

        } catch (Exception e) {
            if (Common.D) {
                Log.e(TAG, "e : " + e.toString());
            }
        }

        return 0;
    }

    /**
     * 진행 상태 업데이트 (UI 스레드)
     * - 다이얼로그의 진행률 및 메시지 업데이트
     *
     * @param progress progress[0]: "progress" 또는 "max"
     *                 progress[1]: 값
     *                 progress[2]: 메시지 (progress일 때만)
     */
    @Override
    protected void onProgressUpdate(String... progress) {
        if (progress[0].equals("progress")) {
            pDialog.setProgress(Integer.parseInt(progress[1]));
            pDialog.setMessage(progress[2]);
        } else if (progress[0].equals("max")) {
            pDialog.setMax(Integer.parseInt(progress[1]));
        }
    }

    /**
     * 백그라운드 작업 완료 후 실행 (UI 스레드)
     * - 로딩 다이얼로그 닫기
     * - 바코드 정보 조회 시작 (ProgressDlgBarcodeSearch)
     *
     * @param result doInBackground 반환값 (0)
     */
    @Override
    protected void onPostExecute(Integer result) {
        pDialog.dismiss();
        // 출하대상 조회 완료 → 바코드 정보 조회 자동 시작
        new ProgressDlgBarcodeSearch(mContext).execute();
    }
}
