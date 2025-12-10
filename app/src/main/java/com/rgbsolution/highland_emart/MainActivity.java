package com.rgbsolution.highland_emart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.common.ProgressDlgShipSearch;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * MainActivity - 메인 메뉴 화면
 *
 * Highland E-Mart PDA 애플리케이션의 메인 화면으로서 다음 기능을 담당합니다:
 * - 8가지 작업 유형(출하, 생산, 홈플러스, 도매업체, 비정량, 홈플러스비정량, 롯데, 생산라벨) 선택
 * - 날짜 선택 및 관리
 * - 서버로부터 출하/생산 리스트 다운로드 (ProgressDlgShipSearch를 통한 서버 통신)
 * - 각 작업 유형별 계근 입력 화면(ShipmentActivity)으로 이동
 * - 앱 설정(SettingActivity) 및 종료 관리
 *
 * searchType 매핑:
 * - "0": 출하대상
 * - "1": 생산대상
 * - "2": 홈플러스 하이퍼
 * - "3": 도매업체
 * - "4": 비정량 출하
 * - "5": 홈플러스 비정량
 * - "6": 롯데
 * - "7": 생산(라벨)
 */
public class MainActivity extends AppCompatActivity {

    // 로그 태그 - 디버깅용
    private final String TAG = "MainActivity";

    // 진동 서비스 - 에러 발생 시 사용자 피드백용
    private Vibrator vibrator;

    // 생산/출하 구분 플래그 - "ship", "prod", "homplus", "lotte" 등 작업 유형 구분
    private String chkProdShip = "";


    // 날짜 선택용 캘린더 인스턴스
    Calendar calendar = Calendar.getInstance();

    /**
     * 날짜 선택 다이얼로그 리스너
     *
     * 사용자가 DatePickerDialog에서 날짜를 선택하면 실행됩니다.
     * 선택된 날짜를 "YYYYMMDD" 형식으로 변환하여 Common.selectDay에 저장합니다.
     * 예: 2025년 1월 31일 → "20250131"
     */
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener(){
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
            // 캘린더에 선택된 날짜 설정
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // 날짜를 YYYYMMDD 형식 문자열로 변환
            String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));

            // 월 포맷팅 (1-9월은 앞에 0 추가)
            if(calendar.get(Calendar.MONTH)+1 < 10) {
                inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
            }else{
                inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
            }

            // 일 포맷팅 (1-9일은 앞에 0 추가)
            if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            }else{
                inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            }

            // 전역 변수에 선택된 날짜 저장
            Common.selectDay = inPutDay;
            Log.i(TAG, TAG + "=====================selectDay======================" + inPutDay);
        }
    };

    /**
     * 액티비티 생성 시 호출
     *
     * - 레이아웃 설정 (activity_main)
     * - Vibrator 서비스 초기화
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 진동 서비스 초기화 - 에러 시 사용자 피드백용
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * 옵션 메뉴 생성
     *
     * 액션바에 메뉴 아이템을 추가합니다.
     * 메뉴 리소스: R.menu.main
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 옵션 메뉴 아이템 선택 처리
     *
     * @param item 선택된 메뉴 아이템
     * @return 처리 여부
     *
     * 메뉴 항목:
     * - action_pinrtsettings: 설정 화면(SettingActivity)으로 이동
     * - action_daysettings: 날짜 선택 다이얼로그 표시
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        int id = item.getItemId();

        // 프린터 설정 메뉴 - SettingActivity로 이동
        if (id == R.id.action_pinrtsettings) {
            i = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(i);
            return true;
        }

        // 날짜 설정 메뉴 - DatePickerDialog 표시
        if (id == R.id.action_daysettings) {
            new DatePickerDialog(MainActivity.this,date,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 버튼 클릭 이벤트 핸들러 (중앙 라우터)
     *
     * 모든 버튼의 클릭 이벤트를 이 메소드에서 처리합니다.
     * XML 레이아웃에서 android:onClick="onClick"으로 연결됩니다.
     *
     * @param v 클릭된 View
     *
     * 처리하는 버튼:
     * [날짜/데이터 관리]
     * - btnDay: 날짜 선택
     * - buttonDelete: 계근대상삭제
     *
     * [데이터 다운로드 - 서버 통신]
     * - btnDownload: 이마트 출하대상받기 (searchType "0")
     * - btnproductionlist: 생산계근대상받기 (searchType "1")
     * - btnDownloadHomeplus: 홈플러스하이퍼 출하대상받기 (searchType "2")
     * - btnDownloadWholesale: 도매업체 출하대상받기 (searchType "3")
     * - btnproductionNonfixedlist: (비정량)출하계근대상받기 (searchType "4")
     * - btnWetHomeplusNon: (비정량)홈플러스 출하대상받기 (searchType "5")
     * - btnDownloadLotte: 롯데 출하대상받기 (searchType "6")
     * - btnproductionlist4print: 생산대상받기(라벨) (searchType "7")
     *
     * [계근 입력 - ShipmentActivity로 이동]
     * - btnWet: (이마트출하) 계근입력시작 (searchType "0" 필요)
     * - btnProdWet: 생산계근입력시작 (searchType "1" 필요)
     * - btnWetHomeplus: (홈플러스하이퍼출하) 계근입력시작 (searchType "2" 필요)
     * - btnWetWholesale: (도매업체) 계근입력시작 (searchType "3" 필요)
     * - btnProdNonfixedWet: (비정량)출하계근입력시작 (searchType "4" 필요)
     * - btnWetHomeplusNon2: (비정량)홈플러스 계근입력시작 (searchType "5" 필요)
     * - btnWetLotte: (롯데출하) 계근입력시작 (searchType "6" 필요)
     * - btnProdWet4print: 생산입력시작(라벨) (searchType "7" 필요)
     *
     * [기타]
     * - btnProdWetCalc: 생산계근계산시작 (ProductionActivity로 이동)
     * - btnClose: 종료
     */
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {

            // ==================== 날짜 선택 ====================
            case R.id.btnDay:
                // DatePickerDialog를 표시하여 사용자가 날짜를 선택할 수 있도록 함
                new DatePickerDialog(MainActivity.this,date,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

                /* 주석 처리된 코드: 날짜 변경 시 확인 다이얼로그
                 * 원래는 날짜 변경 전 기존 데이터 삭제 경고를 표시하려 했으나 현재는 직접 변경
                new AlertDialog.Builder(MainActivity.this, R.style.AppCompatDialogStyle)
                        .setIcon(R.drawable.highland)
                        .setTitle(getResources().getString(R.string.app_name))
                        .setMessage("기존 출하 대상이 삭제 됩니다. 날짜를 변경 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("변경",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DBHandler.deletequeryShipment(getApplicationContext());
                                        new DatePickerDialog(MainActivity.this,date,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                                    }
                                }).setNegativeButton("취소", null).show();*/
                break;

            // ==================== 계근대상삭제 ====================
            case R.id.buttonDelete:
                // 로컬 DB의 계근대상 테이블 데이터 삭제
                DBHandler.deletequeryShipment(getApplicationContext());
                Toast.makeText(getApplicationContext(), "출하대상이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                break;

            // ==================== 이마트 출하대상받기 (searchType: 0) ====================
            // 서버에서 이마트 출하대상 리스트를 다운로드하여 로컬 DB에 저장
            case R.id.btnDownload:
                Log.i(TAG, TAG + "=====================출하대상받기======================" + Common.selectDay);

                chkProdShip = "ship";  // 작업 유형: 출하
                Common.searchType = "0";  // 서버 통신 시 사용되는 검색 타입

                // 기존 출하대상 데이터 삭제 (새 데이터 다운로드 전 초기화)
                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                // TODO: "==" 대신 "".equals() 사용 권장 (Java 문자열 비교)
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    // YYYYMMDD 형식으로 날짜 포맷팅
                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                // 관련 데이터 초기화
                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제

                // 서버 통신 시작 - AsyncTask로 백그라운드에서 데이터 다운로드
                new ProgressDlgShipSearch(this).execute();

                break;

            // ==================== 생산계근대상받기 (searchType: 1) ====================
            // 서버에서 생산계근대상 리스트를 다운로드하여 로컬 DB에 저장
            case R.id.btnproductionlist:
                Log.i(TAG, TAG + "=====================생산대상받기======================" + Common.selectDay);

                chkProdShip = "prod";  // 작업 유형: 생산
                Common.searchType = "1";  // 서버 통신 시 사용되는 검색 타입

                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제
                new ProgressDlgShipSearch(this).execute();  // 서버 통신 시작

                break;

            // ==================== 홈플러스하이퍼 출하대상받기 (searchType: 2) ====================
            // 서버에서 홈플러스하이퍼 출하대상 리스트를 다운로드
            case R.id.btnDownloadHomeplus:
                Log.i(TAG, TAG + "=====================홈플러스출하대상받기======================" + Common.selectDay);

                chkProdShip = "homplus";  // 작업 유형: 홈플러스
                Common.searchType = "2";

                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제
                new ProgressDlgShipSearch(this).execute();  // 서버 통신 시작

                break;

            // ==================== 도매업체 출하대상받기 (searchType: 3) ====================
            // 서버에서 도매업체 출하대상 리스트를 다운로드
            case R.id.btnDownloadWholesale:
                Log.i(TAG, TAG + "=====================도매업체출하대상받기======================" + Common.selectDay);
                //chkProdShip = "homplus";  // 주석 처리됨 - 도매업체용 별도 값 없음
                Common.searchType = "3";

                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제
                new ProgressDlgShipSearch(this).execute();  // 서버 통신 시작

                break;

            // ==================== (비정량)출하계근대상받기 (searchType: 4) ====================
            // 서버에서 비정량 출하계근대상 리스트를 다운로드
            case R.id.btnproductionNonfixedlist:
                Log.i(TAG, TAG + "=====================비정량출하대상받기======================" + Common.selectDay);
                //chkProdShip = "homplus";  // 주석 처리됨
                Common.searchType = "4";

                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제
                new ProgressDlgShipSearch(this).execute();  // 서버 통신 시작

                break;

            // ==================== 롯데 출하대상받기 (searchType: 6) ====================
            // 서버에서 롯데 출하대상 리스트를 다운로드
            // 주의: searchType "5"는 홈플러스 비정량용으로 예약됨
            case R.id.btnDownloadLotte:
                Log.i(TAG, TAG + "=====================롯데출하대상받기======================" + Common.selectDay);

                chkProdShip = "lotte";  // 작업 유형: 롯데
                Common.searchType = "6";  // searchType == 5는 홈플러스 비정량

                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제
                new ProgressDlgShipSearch(this).execute();  // 서버 통신 시작

                break;

            // ==================== (이마트출하) 계근입력시작 (searchType: 0 필요) ====================
            // 다운로드된 이마트 출하대상 리스트를 기반으로 계근 입력 화면으로 이동
            case R.id.btnWet:

                /* 주석 처리된 코드: chkProdShip으로 검증하는 이전 방식
                if (chkProdShip.equals("prod")) {
                    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }*/

                // searchType 검증 - 출하대상 데이터가 다운로드되었는지 확인
                if (!Common.searchType.equals("0")) {
                    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);  // 에러 피드백
                    break;
                }

                Common.searchType = "0";
                // 로컬 DB에서 출하대상 리스트 조회
                ArrayList<Shipments_Info> list_si = DBHandler.selectqueryAllShipment(MainActivity.this);

                // 리스트가 있으면 ShipmentActivity로 이동, 없으면 에러 메시지 표시
                if (list_si.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "출하대상 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            // ==================== (도매업체) 계근입력시작 (searchType: 3 필요) ====================
            case R.id.btnWetWholesale:

                /* 주석 처리된 코드
                if (chkProdShip.equals("prod")) {
                    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }*/

                // searchType 검증
                if (!Common.searchType.equals("3")) {
                    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }

                Common.searchType = "3";
                ArrayList<Shipments_Info> list_wholesale_si = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_wholesale_si.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "출하대상 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            // ==================== 생산계근입력시작 (searchType: 1 필요) ====================
            case R.id.btnProdWet:

                // searchType 검증
                if (!Common.searchType.equals("1")) {
                    Toast.makeText(getApplicationContext(), "생산 계근을 위해 생산 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }
                /* 주석 처리된 코드
                if (chkProdShip.equals("ship")) {
                    Toast.makeText(getApplicationContext(), "생산 계근을 위해 생산 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }*/

                Common.searchType = "1";
                ArrayList<Shipments_Info> list_prod_si = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_prod_si.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "생산대상 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            // ==================== (홈플러스하이퍼출하) 계근입력시작 (searchType: 2 필요) ====================
            case R.id.btnWetHomeplus:

                // searchType 검증
                if (!Common.searchType.equals("2")) {
                    Toast.makeText(getApplicationContext(), "홈플러스 출고분 계근을 위해 홈플러스 하이퍼 출고 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }

                Common.searchType = "2";

                ArrayList<Shipments_Info> list_homeplus_si = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_homeplus_si.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "홈플러스 하이퍼 출고 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            // ==================== (비정량)홈플러스 출하대상받기 (searchType: 5) ====================
            // 서버에서 홈플러스 비정량 출하대상 리스트를 다운로드
            case R.id.btnWetHomeplusNon:

                Log.i(TAG, TAG + "=====================홈플러스 비정량 출하대상받기======================" + Common.selectDay);

                //chkProdShip = "homplus";  // 주석 처리됨
                Common.searchType = "5";

                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제
                new ProgressDlgShipSearch(this).execute();  // 서버 통신 시작

                break;

            // ==================== (비정량)홈플러스 계근입력시작 (searchType: 5 필요) ====================
            case R.id.btnWetHomeplusNon2:

                // searchType 검증
                if (!Common.searchType.equals("5")) {
                    Toast.makeText(getApplicationContext(), "홈플러스 출고분 계근을 위해 홈플러스 비정량 출고 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }

                Common.searchType = "5";

                ArrayList<Shipments_Info> list_homeplus_si2 = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_homeplus_si2.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "홈플러스 비정량 출고 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }

                break;

            // ==================== (비정량)출하계근입력시작 (searchType: 4 필요) ====================
            case R.id.btnProdNonfixedWet:

                // searchType 검증
                if (!Common.searchType.equals("4")) {
                    Toast.makeText(getApplicationContext(), "비정량 출고분 계근을 위해 비정량 출고 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }

                Common.searchType = "4";

                ArrayList<Shipments_Info> list_prodNonfixed_si = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_prodNonfixed_si.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "비정량 출고 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            // ==================== (롯데출하) 계근입력시작 (searchType: 6 필요) ====================
            case R.id.btnWetLotte:

                // searchType 검증
                if (!Common.searchType.equals("6")) {
                    Toast.makeText(getApplicationContext(), "롯데 출고분 계근을 위해 롯데 출고 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }

                Common.searchType = "6";

                ArrayList<Shipments_Info> list_Lotte_si = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_Lotte_si.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "롯데 출고 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            // ==================== 생산대상받기(라벨) (searchType: 7) ====================
            // 서버에서 라벨 출력용 생산대상 리스트를 다운로드
            case R.id.btnproductionlist4print:
                Log.i(TAG, TAG + "=====================생산대상받기(라벨)======================" + Common.selectDay);

                chkProdShip = "prod";  // 작업 유형: 생산
                Common.searchType = "7";  // 라벨용 생산

                DBHandler.deletequeryShipment(getApplicationContext());

                // 날짜가 선택되지 않은 경우 현재 날짜로 설정
                if(Common.selectDay == ""){
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

                    String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
                    if(calendar.get(Calendar.MONTH)+1 < 10) {
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
                    }

                    if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
                        inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }else{
                        inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    }
                    Common.selectDay = inPutDay;
                }

                Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());  // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());     // 계근정보 삭제
                new ProgressDlgShipSearch(this).execute();  // 서버 통신 시작

                break;

            // ==================== 생산입력시작(라벨) (searchType: 7 필요) ====================
            case R.id.btnProdWet4print:

                Log.i(TAG, TAG + "=====================생산입력시작(라벨)======================" + Common.selectDay);

                // searchType 검증
                if (!Common.searchType.equals("7")) {
                    Toast.makeText(getApplicationContext(), "생산 계근을 위해 생산 리스트(라벨)를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }

                Common.searchType = "7";
                ArrayList<Shipments_Info> list_prod_si2 = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_prod_si2.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "생산대상 리스트(라벨)가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            // ==================== 생산계근계산시작 ====================
            // ProductionActivity로 이동 (생산계근계산 기능)
            case R.id.btnProdWetCalc:

                i = new Intent(this, ProductionActivity.class);
                startActivity(i);

                break;

            // ==================== 설정 화면 ====================
            // SettingActivity로 이동
            case R.id.action_daysettings:
                i = new Intent(this, SettingActivity.class);
                startActivity(i);
                break;

            // ==================== 종료 ====================
            // 종료 확인 다이얼로그 표시
            case R.id.btnClose:
                exitDialog();
                break;
        }
    }

    /**
     * 액티비티가 포그라운드로 돌아올 때 호출
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, TAG + " onResume");
    }

    /**
     * 액티비티가 사용자에게 보이기 시작할 때 호출
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG + " onStart");
    }

    /**
     * 액티비티가 백그라운드로 이동할 때 호출
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG + " onPause");
    }

    /**
     * 액티비티가 소멸될 때 호출
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy");
    }

    /**
     * 물리 버튼 KeyDown 이벤트 처리
     *
     * BACK 버튼을 누르면 종료 다이얼로그를 표시합니다.
     *
     * @param keyCode 눌린 키 코드
     * @param event 키 이벤트
     * @return 이벤트 처리 여부
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 앱 종료 확인 다이얼로그 표시
     *
     * "예" 선택 시 finish()를 호출하여 액티비티 종료
     * "아니오" 선택 시 다이얼로그 닫기
     */
    public void exitDialog() {
        String alertTitle = getResources().getString(R.string.app_name);
        String buttonMessage = getString(R.string.exit_message);
        String buttonYes = getString(R.string.exit_yes);
        String buttonNo = getString(R.string.exit_no);

        new AlertDialog.Builder(MainActivity.this, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(alertTitle)
                .setMessage(buttonMessage)
                .setCancelable(false)
                .setPositiveButton(buttonYes,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //log.Write("프로그램 종료");
                                finish();
                            }
                        }).setNegativeButton(buttonNo, null).show();
    }
}
