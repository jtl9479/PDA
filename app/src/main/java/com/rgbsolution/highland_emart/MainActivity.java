package com.rgbsolution.highland_emart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.common.ProgressDlgShipSearch;
import com.rgbsolution.highland_emart.common.TestDataHelper;
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

    // searchType 상수
    private static final String SEARCH_TYPE_EMART = "0";          // 출하대상
    private static final String SEARCH_TYPE_PRODUCTION = "1";        // 생산대상
    private static final String SEARCH_TYPE_HOMEPLUS = "2";          // 홈플러스 하이퍼
    private static final String SEARCH_TYPE_WHOLESALE = "3";         // 도매업체
    private static final String SEARCH_TYPE_NONFIXED = "4";          // 비정량 출하
    private static final String SEARCH_TYPE_HOMEPLUS_NONFIXED = "5"; // 홈플러스 비정량
    private static final String SEARCH_TYPE_LOTTE = "6";             // 롯데
    private static final String SEARCH_TYPE_PRODUCTION_LABEL = "7";  // 생산(라벨)

    // 진동 서비스 - 에러 발생 시 사용자 피드백용
    private Vibrator vibrator;

    // 날짜 선택용 캘린더 인스턴스
    Calendar calendar = Calendar.getInstance();

    /**
     * 날짜 선택 다이얼로그 리스너
     *
     * 사용자가 DatePickerDialog에서 날짜를 선택하면 실행됩니다.
     * 선택된 날짜를 "YYYYMMDD" 형식으로 변환하여 Common.selectDay에 저장합니다.
     * 예: 2025년 1월 31일 → "20250131"
     */
    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        // 캘린더에 선택된 날짜 설정
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // 날짜를 YYYYMMDD 형식 문자열로 변환
        String inPutDay = formatDateYYYYMMDD();

        // 전역 변수에 선택된 날짜 저장
        Common.selectDay = inPutDay;
        Log.i(TAG, TAG + "=====================selectDay======================" + inPutDay);
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

        // 출하대상 목록 팝업
        if (id == R.id.action_shipmentlist) {
            showShipmentListDialog();
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

            // ============================================================
            // 1. 날짜/데이터 관리
            // ============================================================

            // ==================== 날짜 선택 ====================
            case R.id.btnDay:
                new DatePickerDialog(MainActivity.this,date,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;

            // ==================== 계근대상삭제 ====================
            case R.id.buttonDelete:
                DBHandler.deletequeryShipment(getApplicationContext());
                Toast.makeText(getApplicationContext(), "출하대상이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                break;

            // ============================================================
            // 2. 다운로드 (서버에서 출하대상 리스트 받기)
            // ============================================================

            // ==================== 이마트 출하대상받기 (searchType: 0) ====================
            case R.id.btnDownload:
                if (TestDataHelper.TEST_MODE) {
                    Common.searchType = SEARCH_TYPE_EMART;
                    Common.selectDay = formatDateYYYYMMDD();
                    TestDataHelper.deleteAllTestData(this);
                    TestDataHelper.insertTestDataForEmartM0(this);
                    Toast.makeText(this, "[테스트] M0 테스트 데이터 삽입됨\n계근입력시작 버튼을 누르세요", Toast.LENGTH_SHORT).show();
                } else {
                    downloadShipmentList(SEARCH_TYPE_EMART, "출하대상받기");
                }
                break;

            // ==================== 생산계근대상받기 (searchType: 1) ====================
            case R.id.btnproductionlist:
                downloadShipmentList(SEARCH_TYPE_PRODUCTION, "생산대상받기");
                break;

            // ==================== 홈플러스하이퍼 출하대상받기 (searchType: 2) ====================
            case R.id.btnDownloadHomeplus:
                downloadShipmentList(SEARCH_TYPE_HOMEPLUS, "홈플러스출하대상받기");
                break;

            // ==================== 도매업체 출하대상받기 (searchType: 3) ====================
            case R.id.btnDownloadWholesale:
                downloadShipmentList(SEARCH_TYPE_WHOLESALE, "도매업체출하대상받기");
                break;

            // ==================== (비정량)출하계근대상받기 (searchType: 4) ====================
            case R.id.btnproductionNonfixedlist:
                downloadShipmentList(SEARCH_TYPE_NONFIXED, "비정량출하대상받기");
                break;

            // ==================== (비정량)홈플러스 출하대상받기 (searchType: 5) ====================
            case R.id.btnWetHomeplusNon:
                downloadShipmentList(SEARCH_TYPE_HOMEPLUS_NONFIXED, "홈플러스 비정량 출하대상받기");
                break;

            // ==================== 롯데 출하대상받기 (searchType: 6) ====================
            case R.id.btnDownloadLotte:
                downloadShipmentList(SEARCH_TYPE_LOTTE, "롯데출하대상받기");
                break;

            // ==================== 생산대상받기(라벨) (searchType: 7) ====================
            case R.id.btnproductionlist4print:
                downloadShipmentList(SEARCH_TYPE_PRODUCTION_LABEL, "생산대상받기(라벨)");
                break;

            // ============================================================
            // 3. 계근입력 (ShipmentActivity로 이동)
            // ============================================================

            // ==================== 이마트 계근입력시작 (searchType: 0) ====================
            case R.id.btnWet:
                startWeighing(SEARCH_TYPE_EMART, "출하를 위해 출하 리스트를 받아주세요.", "출하대상 리스트가 없습니다.\n리스트를 받아주세요.");
                break;

            // ==================== 생산 계근입력시작 (searchType: 1) ====================
            case R.id.btnProdWet:
                startWeighing(SEARCH_TYPE_PRODUCTION, "생산 계근을 위해 생산 리스트를 받아주세요.", "생산대상 리스트가 없습니다.\n리스트를 받아주세요.");
                break;

            // ==================== 홈플러스 계근입력시작 (searchType: 2) ====================
            case R.id.btnWetHomeplus:
                startWeighing(SEARCH_TYPE_HOMEPLUS, "홈플러스 출고분 계근을 위해 홈플러스 하이퍼 출고 리스트를 받아주세요.", "홈플러스 하이퍼 출고 리스트가 없습니다.\n리스트를 받아주세요.");
                break;

            // ==================== 도매업체 계근입력시작 (searchType: 3) ====================
            case R.id.btnWetWholesale:
                startWeighing(SEARCH_TYPE_WHOLESALE, "출하를 위해 출하 리스트를 받아주세요.", "출하대상 리스트가 없습니다.\n리스트를 받아주세요.");
                break;

            // ==================== 비정량 계근입력시작 (searchType: 4) ====================
            case R.id.btnProdNonfixedWet:
                startWeighing(SEARCH_TYPE_NONFIXED, "비정량 출고분 계근을 위해 비정량 출고 리스트를 받아주세요.", "비정량 출고 리스트가 없습니다.\n리스트를 받아주세요.");
                break;

            // ==================== 홈플러스 비정량 계근입력시작 (searchType: 5) ====================
            case R.id.btnWetHomeplusNon2:
                startWeighing(SEARCH_TYPE_HOMEPLUS_NONFIXED, "홈플러스 출고분 계근을 위해 홈플러스 비정량 출고 리스트를 받아주세요.", "홈플러스 비정량 출고 리스트가 없습니다.\n리스트를 받아주세요.");
                break;

            // ==================== 롯데 계근입력시작 (searchType: 6) ====================
            case R.id.btnWetLotte:
                startWeighing(SEARCH_TYPE_LOTTE, "롯데 출고분 계근을 위해 롯데 출고 리스트를 받아주세요.", "롯데 출고 리스트가 없습니다.\n리스트를 받아주세요.");
                break;

            // ==================== 생산(라벨) 계근입력시작 (searchType: 7) ====================
            case R.id.btnProdWet4print:
                Log.i(TAG, TAG + "=====================생산입력시작(라벨)======================" + Common.selectDay);
                startWeighing(SEARCH_TYPE_PRODUCTION_LABEL, "생산 계근을 위해 생산 리스트(라벨)를 받아주세요.", "생산대상 리스트(라벨)가 없습니다.\n리스트를 받아주세요.");
                break;

            // ============================================================
            // 4. 기타
            // ============================================================

            // ==================== 생산계근계산시작 ====================
            case R.id.btnProdWetCalc:
                i = new Intent(this, ProductionActivity.class);
                startActivity(i);
                break;

            // ==================== 설정 화면 ====================
            case R.id.action_daysettings:
                i = new Intent(this, SettingActivity.class);
                startActivity(i);
                break;

            // ==================== 종료 ====================
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
     * 계근입력 시작 처리
     *
     * @param searchType 검색 타입 ("0"~"7")
     * @param wrongTypeMessage searchType이 일치하지 않을 때 표시할 메시지
     * @param emptyListMessage 리스트가 비어있을 때 표시할 메시지
     */
    private void startWeighing(String searchType, String wrongTypeMessage, String emptyListMessage) {
        // 1. searchType 검증 (다운로드한 리스트 타입과 일치하는지 확인)
        //    불일치 시 에러 메시지 표시, 진동 후 종료
        if (!Common.searchType.equals(searchType)) {
            Toast.makeText(getApplicationContext(), wrongTypeMessage, Toast.LENGTH_SHORT).show();
            vibrator.vibrate(300);
            return;
        }

        // 2. DB에서 출하대상 리스트 조회
        ArrayList<Shipments_Info> list = DBHandler.selectqueryAllShipment(MainActivity.this);

        // 3. 리스트 존재 여부에 따라 분기
        if (list.size() > 0) {
            // 3-1. 리스트 있음: BixolonShipmentActivity로 이동 (계근 입력 화면 - Bixolon 프린터용)
            Intent i = new Intent(this, BixolonShipmentActivity.class);
            startActivity(i);
        } else {
            // 3-2. 리스트 없음: 에러 메시지 표시, 진동
            Toast.makeText(getApplicationContext(), emptyListMessage, Toast.LENGTH_SHORT).show();
            vibrator.vibrate(300);
        }
    }

    /**
     * 출하/생산 대상 리스트 다운로드
     *
     * @param searchType 검색 타입 ("0"~"7")
     * @param logMessage 로그에 출력할 메시지
     */
    private void downloadShipmentList(String searchType, String logMessage) {
        // 1. 다운로드 시작 로그 출력
        Log.i(TAG, TAG + "=====================" + logMessage + "======================" + Common.selectDay);

        // 2. 전역 searchType 설정 (서버 통신 시 사용)
        Common.searchType = searchType;

        // 3. 기존 출하대상 데이터 삭제
        DBHandler.deletequeryShipment(getApplicationContext());

        // 4. 날짜 미선택 시 오늘 날짜로 설정
        if ("".equals(Common.selectDay)) {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
            Common.selectDay = formatDateYYYYMMDD();
        }

        // 5. 선택된 날짜/창고 로그 출력
        Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
        Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

        // 6. 바코드 정보 및 계근 내역 데이터 삭제
        DBHandler.deletequeryBarcodeInfo(getApplicationContext());
        DBHandler.deletequeryGoodsWet(getApplicationContext());

        // 7. 서버에서 출하/생산 대상 리스트 다운로드 (비동기)
        new ProgressDlgShipSearch(this).execute();
    }

    /**
     * 날짜를 YYYYMMDD 형식 문자열로 변환
     *
     * @return YYYYMMDD 형식의 날짜 문자열 (예: "20260107")
     */
    private String formatDateYYYYMMDD() {
        // 1. 연도 추출 (4자리)
        String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));

        // 2. 월 추출 (Calendar.MONTH는 0부터 시작하므로 +1)
        //    10 미만이면 앞에 0 추가 (예: 1월 → "01")
        if(calendar.get(Calendar.MONTH)+1 < 10) {
            inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
        }else{
            inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
        }

        // 3. 일 추출
        //    10 미만이면 앞에 0 추가 (예: 7일 → "07")
        if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
            inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }else{
            inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }

        // 4. 최종 결과 반환 (예: "20260107")
        return inPutDay;
    }

    /**
     * 출하대상 목록 팝업 표시
     * ActionBar 메뉴 "출하대상" 클릭 시 호출
     */
    private void showShipmentListDialog() {
        ArrayList<Shipments_Info> list = DBHandler.selectqueryShipmentForPopup(this);

        if (list.size() == 0) {
            Toast.makeText(this, "출하대상이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Shipments_Info si : list) {
            sb.append("[" + si.getITEM_CODE() + "] " + si.getITEM_NAME() + "\n");
            sb.append("  타입:" + Common.searchType
                    + "  중량:" + si.getGI_REQ_QTY()
                    + "  수량:" + si.getGI_REQ_PKG()
                    + "  계근:" + si.getPACKING_QTY() + "\n");
            sb.append("─────────────────────────\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("출하대상 목록 (" + list.size() + "건)");
        builder.setMessage(sb.toString());
        builder.setPositiveButton("닫기", null);
        builder.show();
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
                .setPositiveButton(buttonYes, (dialog, which) -> finish())
                .setNegativeButton(buttonNo, null)
                .show();
    }
}
