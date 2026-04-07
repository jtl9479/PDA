package com.rgbsolution.highland_emart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.rgbsolution.highland_emart.adapter.DetailAdapter;
import com.rgbsolution.highland_emart.adapter.ShipmentListAdapter;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.common.HttpHelper;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.BixolonSocketPrinter;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;
// Woosim import 제거됨 - Bixolon SLCS 명령어로 대체
// 원본: import com.woosim.printer.WoosimBarcode;
// 원본: import com.woosim.printer.WoosimCmd;
// 원본: import com.woosim.printer.WoosimImage;
// 원본: import com.woosim.printer.WoosimService;
import com.rgbsolution.highland_emart.print.DeviceListActivity;
import com.rgbsolution.highland_emart.scanner.HoneywellScannerActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static com.rgbsolution.highland_emart.R.id.sp_center;

/**
 * ShipmentActivity - 출하 계근 작업 화면
 *
 * <h2>개요</h2>
 * PDA 앱에서 출하 대상 상품의 계근(무게 측정) 작업을 수행하는 핵심 Activity.
 * 바코드 스캔, 중량 입력, 바코드 라벨 인쇄, 서버 전송 기능을 담당한다.
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li><b>출하 대상 조회</b>: 센터별/BL번호별 출하 대상 상품 목록 표시</li>
 *   <li><b>바코드 스캔</b>: 상품 바코드 및 BL번호 바코드 스캔으로 작업 상품 식별</li>
 *   <li><b>계근 작업</b>: 저울 연동 또는 수기 입력으로 중량 측정</li>
 *   <li><b>라벨 인쇄</b>: 블루투스 프린터로 바코드 라벨 인쇄 (Woosim 프린터)</li>
 *   <li><b>서버 전송</b>: 계근 완료 데이터를 서버(G3)로 전송</li>
 * </ul>
 *
 * <h2>출하 유형 (Common.searchType)</h2>
 * <ul>
 *   <li>"0" : 이마트 출하 - 바코드 타입별 라벨 인쇄 (M0, M1, M3, M4, M8, M9, E0-E3, P0 등)</li>
 *   <li>"1" : 생산 투입 - 프린터 비활성화, 생산 공정용</li>
 *   <li>"3" : 도매 출하 - activity_shipment_wholesale 레이아웃 사용</li>
 *   <li>"4" : 홈플러스 출하</li>
 *   <li>"5" : 롯데 출하</li>
 *   <li>"6" : 원앤원 출하</li>
 * </ul>
 *
 * <h2>계근 방식 (ITEM_TYPE)</h2>
 * <ul>
 *   <li>"W", "HW" : 바코드 계근 - 바코드에서 중량 추출</li>
 *   <li>"S" : 저울 계근 - 저울에서 중량 입력 (소수점 2자리)</li>
 *   <li>"J" : 지정 중량 - PACKWEIGHT 값 사용</li>
 *   <li>"B" : 박스 계근</li>
 * </ul>
 *
 * <h2>바코드 타입 (BARCODE_TYPE) - 라벨 인쇄 분기</h2>
 * <ul>
 *   <li>"M0" : 이마트 기본 (미트센터 분기 포함)</li>
 *   <li>"M1" : 이마트 타입1</li>
 *   <li>"M3", "M4" : 이마트 타입3/4</li>
 *   <li>"M8" : 수입식별번호 포함</li>
 *   <li>"M9" : 납품일자 포함</li>
 *   <li>"E0", "E1", "E2", "E3" : 이마트 확장 타입</li>
 *   <li>"P0" : 기본 바코드</li>
 *   <li>"NA" : 도매용 (바코드 타입 없음)</li>
 * </ul>
 *
 * <h2>화면 구성</h2>
 * <ul>
 *   <li>sp_center_name : 이마트 센터 선택 스피너</li>
 *   <li>sp_bl_no : BL번호 선택 스피너</li>
 *   <li>sp_point_name : 지점 선택 스피너</li>
 *   <li>edit_barcode : 바코드/중량 입력 필드</li>
 *   <li>edit_product_name, edit_product_code : 상품명/상품코드 표시</li>
 *   <li>edit_wet_count, edit_wet_weight : 요청수량/중량 vs 계근수량/중량 표시</li>
 *   <li>sList : 작업 대상 지점 리스트뷰</li>
 * </ul>
 *
 * <h2>주요 데이터 흐름</h2>
 * <ol>
 *   <li>출하 대상 조회 (VIEW → JSP → 앱 로컬 DB)</li>
 *   <li>센터/BL/지점 선택</li>
 *   <li>상품 바코드 스캔 → 상품 매칭</li>
 *   <li>중량 입력 (바코드/저울/수기)</li>
 *   <li>라벨 인쇄 (프린터 연결 시)</li>
 *   <li>계근 데이터 로컬 DB 저장</li>
 *   <li>전송 버튼 → 서버 전송 (insert_goods_wet.jsp)</li>
 * </ol>
 *
 * <h2>관련 클래스</h2>
 * <ul>
 *   <li>{@link Shipments_Info} : 출하 대상 정보 DTO</li>
 *   <li>{@link Goodswets_Info} : 계근 데이터 DTO</li>
 *   <li>{@link Barcodes_Info} : 바코드 정보 DTO</li>
 *   <li>{@link DBHandler} : 로컬 SQLite DB 핸들러</li>
 *   <li>{@link ShipmentListAdapter} : 출하 리스트 어댑터</li>
 *   <li>{@link BluetoothPrintService} : 블루투스 프린터 서비스</li>
 *   <li>{@link WoosimService} : Woosim 프린터 명령어 서비스</li>
 * </ul>
 *
 * <h2>관련 VIEW</h2>
 * <ul>
 *   <li>VW_PDA_WID_LIST : 이마트 출하용</li>
 *   <li>VW_PDA_WID_WHOLESALE_LIST : 도매 출하용</li>
 *   <li>VW_PDA_WID_PRO_LIST : 생산 투입용</li>
 *   <li>VW_PDA_WID_LIST_LOTTE : 롯데 출하용</li>
 *   <li>VW_PDA_WID_HOMEPLUS_LIST : 홈플러스 출하용</li>
 * </ul>
 *
 * @see ScannerActivity 바코드 스캐너 기본 Activity
 * @see LoginActivity 로그인 및 출하 유형 선택
 * @see MainActivity 메인 화면
 */
public class BixolonShipmentActivity extends HoneywellScannerActivity {

    // ========================================================================================
    // 상수 정의
    // ========================================================================================
    private final String TAG = "BixolonShipmentActivity";

    // searchType 상수 (Common.searchType과 비교용)
    private static final String SEARCH_TYPE_EMART = "0";             // 이마트 출하
    private static final String SEARCH_TYPE_PRODUCTION = "1";        // 생산 계근 (이노이천)
    private static final String SEARCH_TYPE_HOMEPLUS = "2";          // 홈플러스 출하
    private static final String SEARCH_TYPE_WHOLESALE = "3";         // 도매 출하
    private static final String SEARCH_TYPE_NONFIXED = "4";          // 도매 비정량
    private static final String SEARCH_TYPE_HOMEPLUS_NONFIXED = "5"; // 홈플러스 비정량
    private static final String SEARCH_TYPE_LOTTE = "6";             // 롯데 출하
    private static final String SEARCH_TYPE_PRODUCTION_LABEL = "7";  // 생산 라벨

    // 업체 정보 상수
    private static final String COMPANY_CODE = "610933";                    // 회사 코드
    private static final String COMPANY_NAME = "(주)하이랜드이노베이션";    // 회사명

    // 미트센터 관련 상수
    private static final String MEAT_CENTER_CODE = "059015";         // 미트센터 업체코드
    private static final String MEAT_CENTER_STORE_CODE = "9231";     // 미트센터 지점코드
    private static final String KILKOY_PACKER_CODE = "30228";        // 킬코이 패커코드
    private static final String LOGIS_CODE_DEFAULT = "0000000";      // 로지스코드 기본값

    // 롯데 박스 순번 관련 상수
    private static final int LOTTE_BOX_ORDER_MAX = 9999;             // 롯데 박스 순번 최대값

    // 바코드 타입 상수
    private static final String BARCODE_TYPE_M0 = "M0";
    private static final String BARCODE_TYPE_M1 = "M1";
    private static final String BARCODE_TYPE_M3 = "M3";
    private static final String BARCODE_TYPE_M4 = "M4";
    private static final String BARCODE_TYPE_M8 = "M8";
    private static final String BARCODE_TYPE_M9 = "M9";
    private static final String BARCODE_TYPE_E0 = "E0";
    private static final String BARCODE_TYPE_E1 = "E1";
    private static final String BARCODE_TYPE_E2 = "E2";
    private static final String BARCODE_TYPE_E3 = "E3";
    private static final String BARCODE_TYPE_P0 = "P0";

    // 계근 방식 상수
    private static final String ITEM_TYPE_W = "W";    // 바코드 계근
    private static final String ITEM_TYPE_HW = "HW";  // 바코드 계근 확장
    private static final String ITEM_TYPE_S = "S";    // 저울 계근
    private static final String ITEM_TYPE_J = "J";    // 지정 중량
    private static final String ITEM_TYPE_B = "B";    // 홈플러스 비정량

    // 센터명 상수 (수입육 센터 판별용)
    private static final String CENTER_NAME_TRD = "TRD";
    private static final String CENTER_NAME_WET = "WET";
    private static final String CENTER_NAME_ET = "E/T";

    /** Handler 메시지 타입 - 리스트 행 체크 완료 */
    private final int MESSAGE_ROWCHECK = 1000;
    /** Handler 메시지 타입 - 계근 작업 완료 */
    private final int MESSAGE_COMPLETE = 1001;
    /** Handler 메시지 타입 - 검색 체크 완료 */
    private final int MESSAGE_SEARCHCHECK = 1002;

    // ========================================================================================
    // 블루투스 프린터 관련 상수 및 필드
    // ========================================================================================

    /** 프린터 연결 요청 코드 */
    public static final int REQUEST_CONNECT_DEVICE = 1;

    // BluetoothPrintService Handler 메시지 타입
    /** 프린터 디바이스명 수신 */
    public static final int MESSAGE_DEVICE_NAME = 1;
    /** Toast 메시지 표시 */
    public static final int MESSAGE_TOAST = 2;
    /** 프린터 데이터 읽기 */
    public static final int MESSAGE_READ = 3;
    /** 검색 완료 */
    public static final int MESSAGE_SEARCH = 4;
    /** 재인쇄 요청 */
    public static final int MESSAGE_REPRINT = 5;
    /** 소비기한 입력 화면에서 데이터 수신 요청 코드 */
    public static final int GET_DATA_REQUEST = 8;

    // Handler 키 이름
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    /** 비보안 모드 프린터 연결 요청 코드 */
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    /** 블루투스 활성화 요청 코드 */
    private static final int REQUEST_ENABLE_BT = 3;

    /** 연결된 프린터 디바이스명 */
    private String mConnectedDeviceName = null;
    /** 블루투스 어댑터 */
    private BluetoothAdapter mBluetoothAdapter = null;
    /** 블루투스 프린터 서비스 */
    private BixolonSocketPrinter mBixolonPrinter = null;
    /** Bixolon 프린터 이전 상태 (연결 실패 판단용) */
    private int mPreviousBixolonState = BixolonSocketPrinter.STATE_NONE;

    /** 라벨 출력 헬퍼 */
    private LabelPrintHelper labelPrintHelper = new LabelPrintHelper();
    /** 프린터 콜백 - LabelPrintHelper에서 프린터 데이터 전송 및 UI 업데이트 */
    private LabelPrintHelper.PrinterCallback printerCallback = new LabelPrintHelper.PrinterCallback() {
        @Override
        public void sendData(byte[] data) {
            BixolonShipmentActivity.this.sendData(data);
        }

        @Override
        public void clearBarcodeInput() {
            edit_barcode.setText("");
        }
    };

    // mWoosim 제거됨 - Bixolon SLCS 명령어로 대체
    /** 효과음 풀 */
    protected SoundPool sound_pool;
    /** 성공 효과음 ID */
    protected int sound_success;
    /** 실패 효과음 ID */
    protected int sound_fail;
    /** 롯데 전송 재시도 카운트 */
    private int lotte_TryCount = 0;

    // ========================================================================================
    // UI 컴포넌트 및 Activity 필드
    // ========================================================================================

    private LayoutInflater Inflater;
    /** 로딩 다이얼로그 */
    private ProgressDialog pDialog = null;
    /** 프린터 연결 다이얼로그 */
    private ProgressDialog cDialog = null;

    /**
     * 출하 대상 리스트 - VIEW에서 조회한 출하 대상 정보
     * @see Shipments_Info
     */
    private ArrayList<Shipments_Info> arSM;

    /** 작업 모드 선택 스피너 (바코드스캔/수기입력/상품코드) */
    private Spinner sp_work;
    /** 바코드/중량 입력 필드 - 스캔된 바코드 또는 수기 입력 중량 */
    private EditText edit_barcode;
    /** 입력 버튼 - 바코드 입력 또는 중량 입력 확인 */
    private Button btn_input;
    /** 센터 선택 스피너 - 이마트 물류센터 선택 */
    private Spinner sp_center_name;
    /** 상품명 표시 필드 - 현재 작업 중인 상품명 (ITEM_NAME) */
    private EditText edit_product_name;
    /** 상품코드 표시 필드 - 현재 작업 중인 패커상품코드 (PACKER_PRODUCT_CODE) */
    private EditText edit_product_code;
    /** BL번호 선택 스피너 - 동일 BL건 그룹핑 */
    private Spinner sp_bl_no;
    /** 센터 총 요청수량 - 선택된 센터의 전체 GI_REQ_PKG 합계 */
    private EditText edit_center_tcount;
    /** 센터 총 요청중량 - 선택된 센터의 전체 GI_REQ_QTY 합계 */
    private EditText edit_center_tweight;
    /** 지점 선택 스피너 - 출고 대상 지점 선택 (CLIENTNAME) */
    private Spinner sp_point_name;
    /** 지점 계근 현황 - "요청수량 / 완료수량" 형태 (GI_REQ_PKG / PACKING_QTY) */
    private EditText edit_wet_count;
    /** 지점 계근 중량 - "요청중량 / 완료중량" 형태 (GI_REQ_QTY / GI_QTY) */
    private EditText edit_wet_weight;

    /** 출하 대상 리스트 어댑터 */
    private ShipmentListAdapter sListAdapter;
    /** 출하 대상 리스트뷰 - 센터별 출하 대상 목록 표시 */
    private ListView sList;

    /** 뒤로가기 버튼 */
    private Button btn_back;
    /** 전송 버튼 - 계근 완료 데이터를 서버(G3)로 전송 */
    private Button btn_send;
    /** 선택 버튼 - 선택된 지점의 계근 상세정보 팝업 */
    private Button btn_select;

    // ========================================================================================
    // 계근 작업 상태 관리 필드
    // ========================================================================================

    /** 센터 총 요청수량 (GI_REQ_PKG 합계) */
    private int centerTotalCount;
    /** 센터 완료수량 (PACKING_QTY 합계) */
    private int centerWorkCount;
    /** 센터 총 요청중량 (GI_REQ_QTY 합계) */
    private double centerTotalWeight;
    /** 센터 완료중량 (GI_QTY 합계) */
    private double centerWorkWeight;
    /** 리스트에서 선택된 위치 (상세보기용) */
    private int select_position;
    /**
     * 현재 계근 작업 중인 리스트 위치
     * -1: 미선택, 0~n: arSM 리스트 인덱스
     */
    private int current_work_position;

    /**
     * 작업 모드 플래그
     * 1: 바코드 스캔 모드
     * 0: 수기 입력 모드 (중량 직접 입력)
     * 2: 상품코드 입력 모드
     */
    private int work_flag = 1;
    /**
     * 스캔 순서 플래그
     * true: 상품 바코드 스캔 차례
     * false: BL번호 바코드 스캔 차례
     */
    private boolean scan_flag = true;
    /**
     * 선택 모드 플래그
     * true: 스캔 모드
     * false: 선택 모드
     */
    private boolean select_flag = true;

    /** 진동 알림 */
    private Vibrator vibrator;
    AlertDialog alert;
    /** 다이얼로그 중복 표시 방지 플래그 */
    boolean alert_flag = false;
    /** 제조일자 입력 플래그 (킬코이 미트센터용) */
    boolean makingdateInputFlag = false;

    // ========================================================================================
    // Keyboard Wedge 자동 감지 (바코드 스캐너 지원)
    // ========================================================================================

    /** setBarcodeMsg 마지막 처리 시각 (중복 호출 방지용) */
    private long lastBarcodeProcessedTime = 0;
    /** 중복 처리 방지 간격 (ms) - 같은 바코드 처리 후 1초 이내 재처리 차단 */
    private static final long BARCODE_PROCESS_DEBOUNCE_MS = 1000;

    // ========================================================================================
    // Activity 생명주기 메서드
    // ========================================================================================

    /**
     * Activity 생성 시 호출
     * <p>
     * 주요 처리:
     * <ul>
     *   <li>출하 유형에 따른 레이아웃 설정 (도매: activity_shipment_wholesale, 기타: activity_shipment)</li>
     *   <li>UI 컴포넌트 초기화 및 이벤트 리스너 등록</li>
     *   <li>센터 리스트 로드</li>
     *   <li>블루투스 어댑터 초기화</li>
     *   <li>생산 계근 시 프린터 비활성화</li>
     * </ul>
     * </p>
     *
     * @param savedInstanceState 저장된 인스턴스 상태
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "=====================Common.searchType Check==================" + Common.searchType);

        super.onCreate(savedInstanceState);

        // 휴먼울림체 폰트 로드
        LabelPrintHelper.loadCustomFont(this);

        // 출하 유형에 따른 레이아웃 설정
        // searchType "3": 도매 출하 - 별도 레이아웃 사용
        if(Common.searchType.equals(SEARCH_TYPE_WHOLESALE)){
            setContentView(R.layout.activity_shipment_wholesale);
        }else{
            setContentView(R.layout.activity_shipment);
        }

        current_work_position = -1;

        centerTotalCount = 0;
        centerTotalWeight = 0.0;
        centerWorkCount = 0;
        centerWorkWeight = 0.0;

        work_flag = 1;
        scan_flag = true;       // 시작은 패커상품부터 스캔
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        sound_pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        //프린터 연결 성공
        sound_success = sound_pool.load(getBaseContext(), R.raw.beep, 1);
        //프린터 연결 실패
        sound_fail = sound_pool.load(getBaseContext(), R.raw.e, 1);
        Inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        sp_work = (Spinner) findViewById(R.id.sp_work);
        sp_work.setOnItemSelectedListener(workSelectedListener);
        edit_barcode = (EditText) findViewById(R.id.edit_barcode);

        // Keyboard Wedge 모드 지원 (1): ENTER/TAB 키 감지
        edit_barcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_TAB)
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String barcodeText = edit_barcode.getText().toString().trim();
                    if (!barcodeText.isEmpty()) {
                        Log.d(TAG, "Keyboard Wedge ENTER/TAB 감지, 바코드: " + barcodeText);
                        hideKeyboard();
                        if (work_flag == 1 || work_flag == 2) {
                            setBarcodeMsg(barcodeText);
                        } else if (work_flag == 0) {
                            btn_input.performClick();
                        }
                    }
                    return true;
                }
                return false;
            }
        });


        sp_center_name = (Spinner) findViewById(sp_center);

        Common.list_center_info = DBHandler.selectqueryCenterList(this);
        ArrayAdapter<String> center_adapter = new ArrayAdapter<String>(BixolonShipmentActivity.this, android.R.layout.simple_spinner_item, Common.list_center_info);
        center_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_center_name.setAdapter(center_adapter);
        sp_center_name.setOnItemSelectedListener(emartCenterSelectedListener);
        btn_input = (Button) findViewById(R.id.btn_input);
        btn_input.setOnClickListener(inputBtnListener);

        edit_product_name = (EditText) findViewById(R.id.edit_product_name);
        edit_product_code = (EditText) findViewById(R.id.edit_product_code);
        sp_bl_no = (Spinner) findViewById(R.id.sp_bl_no);
        edit_center_tcount = (EditText) findViewById(R.id.edit_center_tcount);
        edit_center_tweight = (EditText) findViewById(R.id.edit_center_tweight);

        sp_point_name = (Spinner) findViewById(R.id.sp_point);
        sp_point_name.setOnItemSelectedListener(emartPointSelectedListener);

        edit_wet_count = (EditText) findViewById(R.id.edit_wet_count);
        edit_wet_weight = (EditText) findViewById(R.id.edit_wet_weight);

        sList = (ListView) findViewById(R.id.sList);

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_select = (Button) findViewById(R.id.btn_select);

        btn_back.setOnClickListener(backBtnListener);
        btn_send.setOnClickListener(sendBtnListener);
        btn_select.setOnClickListener(selectBtnListener);

        btn_init.setOnClickListener(initBtnListener);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.toast_bt_na, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.i(TAG, "***********************onCreate 끝 " );

        if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION)){
            Log.i(TAG, "===================PRINTER DISABLE==================");
            swt_print.setChecked(false); //인쇄 안함으로 세팅
            swt_print.setClickable(false); //스위치 불가능하도록 변경
            Common.print_bool = false; //이마트 스티커 출력 로직 타지 않도록 false처리(처리해주지 않으면 계근 완료시 전송버튼 활성화 안됨)
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, TAG + " onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG + " onStart");
        // 출하대상 불러오기 끝, Print 연결 시작

        // ============================================================
        // Bixolon 프린터 첫 사용 시 기존 Woosim MAC 주소 초기화
        // - BixolonShipmentActivity 최초 진입 시 1회만 실행
        // - 이후에는 Bixolon 프린터 주소 저장/재사용
        // ============================================================
        SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
        boolean bixolonInitialized = spfBluetooth.getBoolean("bixolon_initialized", false);

        if (!bixolonInitialized) {
            // 첫 사용: 기존 프린터 주소 초기화 및 플래그 설정
            SharedPreferences.Editor editor = spfBluetooth.edit();
            editor.putString("printer_address", "");
            editor.putBoolean("bixolon_initialized", true);
            editor.apply();

            Common.printer_address = "";
            Log.i(TAG, "Bixolon 첫 사용: 기존 프린터 주소 초기화 완료");
        }

        if (!mBluetoothAdapter.isEnabled() && !Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {  //안드로이드 디바이스에서 블루투스 ON 여부, 이노이천에서 생산 계근일때는 블루투스 on 여부 묻지 않는다
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (Common.printer_setting && !Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {  //메인화면 프린터설정에서 ON으로 하면 아래 로직을 탄다, 이노이천에서 생산 계근일떄는 물어보지 않도록 변경
                if (mBixolonPrinter == null) {
                    mBixolonPrinter = new BixolonSocketPrinter(BixolonShipmentActivity.this, mBixolonHandler);
                    // mWoosim 제거됨 - Bixolon SLCS 명령어로 대체

                    if (Common.printer_address.equals("")) {
                        Intent i = new Intent(BixolonShipmentActivity.this, DeviceListActivity.class);
                        startActivityForResult(i, REQUEST_CONNECT_DEVICE);
                    } else {
                        new ProgressDlgPrintConnect(BixolonShipmentActivity.this).execute();
                    }

                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG + " onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy");

        if (mBixolonPrinter != null) {
            new ProgressDlgDiscon(BixolonShipmentActivity.this).execute();
        }

        if (cDialog != null && cDialog.isShowing()) {
            cDialog.dismiss(); //접속되면 여기서 종료
        }
    }

    private void hideKeyboard() {
        InputMethodManager btn_input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        btn_input.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // ========================================================================================
    // 버튼 클릭 리스너
    // ========================================================================================

    /**
     * 입력 버튼 클릭 리스너
     * <p>
     * 작업 모드(work_flag)에 따라 동작이 달라짐:
     * <ul>
     *   <li>work_flag=1 (바코드 스캔): setBarcodeMsg() 호출하여 바코드 처리</li>
     *   <li>work_flag=0 (수기 입력): 입력된 중량값으로 wet_data_insert() 호출</li>
     *   <li>work_flag=2 (상품코드): setBarcodeMsg() 호출하여 상품코드 처리</li>
     * </ul>
     * </p>
     * <p>
     * 수기 입력 시 특수 조건:
     * <ul>
     *   <li>킬코이 미트센터(패커코드 30228, 스토어 9231): 소비기한 입력 화면으로 이동</li>
     *   <li>수입육 센터(TRD/WET/E/T): 이마트/롯데 출하 시 소비기한 입력 화면으로 이동</li>
     *   <li>이마트 출하: 소수점 첫째자리까지 반올림</li>
     *   <li>생산/홈플러스: 입력값 그대로 사용</li>
     * </ul>
     * </p>
     */
    private View.OnClickListener inputBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "입력버튼 클릭");

            hideKeyboard();
            if (work_flag == 1) {     // 바코드 스캔 작업
                setBarcodeMsg(edit_barcode.getText().toString());
            } else if (work_flag == 0) {            // 수기 입력 작업
                if (edit_barcode.getText().toString().equals("")) {      // 중량값이 입력되지 않았을 때
                    Toast.makeText(getApplicationContext(), "중량을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (current_work_position == -1) {               // 작업 중인 지점이 선택되지 않았을 때
                    Toast.makeText(getApplicationContext(), "작업 중인 지점이 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (work_ppcode.equals("")) {                    // 상품패커코드가 정해지지 않았을 때
                    Toast.makeText(getApplicationContext(), "상품패커코드가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                } else {                                                // 필수값이 전부 입력되있을 때
                    work_item_fullbarcode = "";
                    String weight_str = edit_barcode.getText().toString();    // 입력값 저장

                    Log.i(TAG, "=====================weight_str 1==================" + weight_str);

                    double weight_double = Double.parseDouble(weight_str);    // 소수점 1자리로 변환

                    Log.i(TAG, "=====================weight_double 1==================" + weight_double);

                    String temp_weight = "";

                    if (Common.searchType.equals(SEARCH_TYPE_EMART)) { //이마트 출하대상일경우
                        weight_double = Math.floor(weight_double * 10);
                        Log.i(TAG, "=====================weight_double 1-1==================" + weight_double);
                        weight_double = weight_double / 10.0;
                        Log.i(TAG, "=====================weight_double 1-2==================" + weight_double);
                        Log.i(TAG, "=====================weight_double 2==================" + weight_double);
                        temp_weight = String.format("%.1f", weight_double); //출하일 경우 소숫점 첫째 자리까지 반올림, 위 단계에서 Math.floor로 소숫점 둘째 자리부터 날려서 의미는 없는 코드이나 일단 남겨놓음
                    } else { //이노 생산계근 or 홈플러스 추가계근일경우
                        temp_weight = Double.toString(weight_double); //생산일 경우 그대로 입력
                        Log.i(TAG, "=====================temp_weight production==================" + temp_weight);
                    }

                    Log.i(TAG, "=====================temp_weight out==================" + temp_weight);

                    weight_double = Double.parseDouble(temp_weight); //생산이든 출하든 똑같이 타야함

                    Log.i(TAG, "=====================weight_double 3==================" + weight_double);

                    weight_str = String.valueOf(weight_double);                                         // 반올림값 다시 저장

                    Log.i(TAG, "=====================패커코드 체크==================" + arSM.get(current_work_position).getPACKER_CODE());
                    Log.i(TAG, "=====================스토어코드 체크==================" + arSM.get(current_work_position).getSTORE_CODE());

                    if (arSM.get(current_work_position).getPACKER_CODE().equals(KILKOY_PACKER_CODE)
                            && arSM.get(current_work_position).getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) {
                          String makingFrom = work_item_bi_info.getMAKINGDATE_FROM();
                          String makingTo = work_item_bi_info.getMAKINGDATE_TO();

                          Intent IntentA = new Intent(BixolonShipmentActivity.this, ExpiryEnterActivity.class);

                          String weightStrKey = "weightStrKey";
                          String weightDblKey = "weightDblKey";
                          String makingFromKey = "makingFromKey";
                          String makingToKey = "makingToKey";

                          IntentA.putExtra(weightStrKey,weight_str);
                          IntentA.putExtra(weightDblKey,weight_double);

                          IntentA.putExtra(makingFromKey,makingFrom);
                          IntentA.putExtra(makingToKey,makingTo);

                          startActivityForResult(IntentA,GET_DATA_REQUEST);

                    }else if(arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_TRD) || arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_WET) || arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_ET) || Common.searchType.equals(SEARCH_TYPE_LOTTE)){
                        if(Common.searchType.equals(SEARCH_TYPE_EMART) || Common.searchType.equals(SEARCH_TYPE_LOTTE)){ //수입육 계근, 롯데계근일 때 수기입력시 소비기한 창 띄움
                            String makingFrom = work_item_bi_info.getMAKINGDATE_FROM();
                            String makingTo = work_item_bi_info.getMAKINGDATE_TO();

                            Intent IntentA = new Intent(BixolonShipmentActivity.this, ExpiryEnterActivity.class);

                            String weightStrKey = "weightStrKey";
                            String weightDblKey = "weightDblKey";
                            String makingFromKey = "makingFromKey";
                            String makingToKey = "makingToKey";

                            IntentA.putExtra(weightStrKey,weight_str);
                            IntentA.putExtra(weightDblKey,weight_double);

                            IntentA.putExtra(makingFromKey,makingFrom);
                            IntentA.putExtra(makingToKey,makingTo);

                            startActivityForResult(IntentA,GET_DATA_REQUEST);
                        }else{
                            wet_data_insert(weight_str, weight_double, "", "");
                        }
                    }else{
                        wet_data_insert(weight_str, weight_double, "", "");
                    }
                    edit_barcode.setText("");
                }
            } else if(work_flag == 2) {            // 상품코드
                setBarcodeMsg(edit_barcode.getText().toString());
            }
        }
    };

    /**
     * 뒤로가기 버튼 클릭 리스너
     * <p>
     * Activity를 종료하고 이전 화면(MainActivity)으로 복귀
     * </p>
     */
    private View.OnClickListener backBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Back 버튼 클릭");
            finish();
        }
    };

    /**
     * 전송 버튼 클릭 리스너
     * <p>
     * 계근 완료된 데이터를 서버(G3)로 전송
     * </p>
     * <p>
     * 처리 흐름:
     * <ol>
     *   <li>확인 다이얼로그 표시</li>
     *   <li>확인 시 ProgressDlgShipmentSend AsyncTask 실행</li>
     *   <li>insert_goods_wet.jsp 호출하여 계근 데이터 전송</li>
     * </ol>
     * </p>
     */
    private View.OnClickListener sendBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Send 버튼 클릭");

            dialog_flag = true;
            new AlertDialog.Builder(BixolonShipmentActivity.this, R.style.AppCompatDialogStyle)
                    .setIcon(R.drawable.highland)
                    .setTitle(R.string.shipment_wet_send)
                    .setMessage(R.string.shipment_wet_send_msg)
                    .setCancelable(false)
                    .setPositiveButton(R.string.shipment_wet_yes, (dialog, which) -> {
                        // 전송시작
                        dialog_flag = false;
                        new ProgressDlgShipmentSend(BixolonShipmentActivity.this).execute();
                    }).setNegativeButton(R.string.shipment_wet_no, (dialog, which) -> {
                dialog_flag = false;
            }).show();
        }
    };

    /**
     * 선택 버튼 클릭 리스너
     * <p>
     * 선택된 지점의 계근 상세정보 팝업을 표시
     * </p>
     * <p>
     * 처리 흐름:
     * <ol>
     *   <li>체크박스로 선택된 지점 확인</li>
     *   <li>선택된 지점이 있으면 show_wetDetailDialog() 호출하여 상세 팝업 표시</li>
     *   <li>선택된 지점이 없으면 경고 메시지 및 진동 알림</li>
     * </ol>
     * </p>
     */
    private View.OnClickListener selectBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "Select 버튼 클릭");
            setSelect_Position(-1);
            for (int i = 0; i < sListAdapter.cbStatus.size(); i++) {
                if (sListAdapter.cbStatus.get(i)) {
                    setSelect_Position(i);
                    break;
                }
            }
            if (getSelect_Position() != -1) {
                show_wetDetailDialog(arSM.get(getSelect_Position()), work_item_bi_info, getSelect_Position());
                set_scanFlag(true);
                work_ppcode = "";
            } else {
                Toast.makeText(getApplicationContext(), "지점을 선택해주세요.", Toast.LENGTH_SHORT).show();
                vibrator.vibrate(1000);
            }
        }
    };

    /**
     * 스캔 초기화 버튼 클릭 리스너
     * <p>
     * 스캔 상태를 초기화하여 상품 바코드 스캔부터 다시 시작
     * </p>
     */
    private View.OnClickListener initBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            scanFlag_init();
            Log.i(TAG, "스캔초기화 이벤트");
            Toast.makeText(getApplicationContext(), "          * 스캔초기화 *\n상품바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
        }
    };

    public void setSelect_Position(int i) {
        this.select_position = i;
    }

    public int getSelect_Position() {
        return this.select_position;
    }

    // ========================================================================================
    // Handler - 비동기 메시지 처리
    // ========================================================================================

    /**
     * 메인 핸들러 - UI 스레드에서 메시지 처리
     * <p>
     * 처리하는 메시지 유형:
     * <ul>
     *   <li>MESSAGE_ROWCHECK (1000): 리스트 행 체크 완료 - 해당 위치로 스크롤</li>
     *   <li>MESSAGE_COMPLETE (1001): 계근 작업 완료 알림</li>
     *   <li>MESSAGE_SEARCHCHECK (1002): 검색 체크 - GI_D_ID로 해당 행 선택</li>
     *   <li>MESSAGE_DEVICE_NAME (1): 프린터 연결 성공 - 디바이스명 저장 및 성공음 재생</li>
     *   <li>MESSAGE_TOAST (2): Toast 메시지 표시</li>
     *   <li>MESSAGE_READ (3): 프린터 데이터 읽기 - Woosim 서비스로 전달</li>
     *   <li>MESSAGE_SEARCH (4): 프린터 검색 - DeviceListActivity 호출</li>
     *   <li>MESSAGE_REPRINT (5): 재인쇄 요청 - 출하 유형별 프린팅 메서드 호출</li>
     *   <li>WoosimService.MESSAGE_PRINTER: Woosim 프린터 관련 메시지</li>
     * </ul>
     * </p>
     */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                int pos = 0;

                Log.i(TAG, "스캔 버튼 클릭!!!!");

                switch (msg.what) {
                    case MESSAGE_ROWCHECK:
                        pos = msg.getData().getInt("POSITION");
                        Toast.makeText(getApplicationContext(), "선택된 상품이 존재합니다.", Toast.LENGTH_SHORT).show();
                        sList.setSelection(pos);
                        break;
                    case MESSAGE_COMPLETE:
                        Toast.makeText(getApplicationContext(), "계근이 완료된 상품입니다.", Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_SEARCHCHECK:
                        String gi_d_id = msg.getData().getString("GI_D_ID");

                        for (int i = 0; i < arSM.size(); i++) {
                            if (arSM.get(i).getGI_D_ID().toString().equals(gi_d_id.toString())
                                    && arSM.get(i).getGI_L_ID().toString().equals(msg.getData().getString("GI_L_ID", "").toString())) {
                                pos = i;
                            }
                        }
                        sListAdapter.cbStatus.set(pos, true);
                        sListAdapter.notifyDataSetChanged();
                        sList.setSelection(pos);
                        if (Common.D) {
                            Log.d(TAG, "MESSAGE_SEARCHCHECK:: pos -> " + (pos));
                        }
                        break;
                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                        if (Common.D) {
                            Log.d(TAG, "MESSAGE_DEVICE_NAME : " + mConnectedDeviceName);
                        }
                        Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        sound_pool.play(sound_success, 1.0f, 1.0f, 0, 0, 1.0f);
                        if (cDialog != null && cDialog.isShowing()) {
                            cDialog.dismiss(); //접속되면 여기서 종료
                        }
                        break;
                    case MESSAGE_TOAST:
                        if (Common.D) {
                            Log.d(TAG, "MESSAGE TOAST : " + msg.getData().getString(TOAST));
                        }
                        Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();

                        if (1 == mBixolonPrinter.getState()) {
                            if (cDialog != null && cDialog.isShowing()) {
                                cDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), "접속이 원할하지 않습니다\n스캐너의 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            sound_pool.play(sound_fail, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        break;
                    case MESSAGE_READ:
                        // mWoosim.processRcvData 제거됨 - Bixolon은 별도 처리 불필요
                        break;
                    case MESSAGE_SEARCH:
                        if (mBixolonPrinter == null) {
                            mBixolonPrinter = new BixolonSocketPrinter(BixolonShipmentActivity.this, mBixolonHandler);
                            // mWoosim 제거됨 - Bixolon SLCS 명령어로 대체
                            Intent i = new Intent(BixolonShipmentActivity.this, DeviceListActivity.class);
                            startActivityForResult(i, REQUEST_CONNECT_DEVICE);
                        }
                        break;
                    case MESSAGE_REPRINT:

                        String print_weight_str = msg.getData().getString("WEIGHT").toString();
                        String making_date = msg.getData().getString("MAKINGDATE").toString();

                        if (Common.searchType.equals(SEARCH_TYPE_HOMEPLUS) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
                            labelPrintHelper.setHomeplusPrinting(Double.parseDouble(print_weight_str), arSM.get(select_position), true, printerCallback);
                        } else if (Common.searchType.equals(SEARCH_TYPE_LOTTE)) {
                            //Toast.makeText(getApplicationContext(), "롯데 재출력은 불가합니다.", Toast.LENGTH_SHORT).show();
                            // 롯데의 경우 바코드 시퀀스를 위해 BOX_ORDER 가져옴.
                            String box_order = msg.getData().getString("BOX_ORDER").toString();

                            labelPrintHelper.setPrintingLotte(Double.parseDouble(print_weight_str), arSM.get(select_position), true, making_date, box_order, Common.searchType, printerCallback);
                        } else if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)) {
                            labelPrintHelper.setPrinting_prod(Double.parseDouble(print_weight_str), arSM.get(select_position), true, printerCallback);
                        }else{ //이마트수기프린팅
                            labelPrintHelper.setPrinting(Double.parseDouble(print_weight_str), arSM.get(select_position), true, making_date, work_item_bi_info, arSM.get(current_work_position), Common.searchType, printerCallback);
                        }

                        break;
                    // WoosimService.MESSAGE_PRINTER 제거됨 - Bixolon은 별도 Handler 사용
                }
            } catch (Exception e) {
                if (Common.D) {
                    Log.d(TAG, "스캔 Exception:: " + e.getMessage());
                }
            }
        }
    };

    /**
     * Bixolon Handler
     */
    /**
     * Bixolon 프린터 Handler
     * <p>
     * BixolonSocketPrinter로부터 연결 상태 변경, 디바이스명, 인쇄 완료 등의 메시지를 수신한다.
     * 기존 ShipmentActivity의 mHandler와 동일한 동작을 유지한다.
     * </p>
     * <p>
     * STATE_NONE 처리 시 주의사항:
     * - connect() 호출 시 내부에서 disconnect()가 먼저 호출되어 STATE_NONE이 발생함
     * - 이때 실패 메시지를 표시하면 안 됨 (아직 연결 시도 전이므로)
     * - STATE_CONNECTING → STATE_NONE 일 때만 실제 연결 실패로 판단
     * </p>
     */
    private final Handler mBixolonHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonSocketPrinter.MESSAGE_STATE_CHANGE:
                    int newState = msg.arg1;
                    Log.d(TAG, "Bixolon state: " + mPreviousBixolonState + " -> " + newState);

                    switch (newState) {
                        case BixolonSocketPrinter.STATE_CONNECTED:
                            // 연결 성공 시 다이얼로그 닫기 (Toast와 성공음은 MESSAGE_DEVICE_NAME에서 처리)
                            if (cDialog != null && cDialog.isShowing()) {
                                cDialog.dismiss();
                            }
                            Log.d(TAG, "Bixolon connected");
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setSubtitle("Printer Connected");
                            }
                            break;
                        case BixolonSocketPrinter.STATE_CONNECTING:
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setSubtitle("Connecting...");
                            }
                            break;
                        case BixolonSocketPrinter.STATE_NONE:
                            // 연결 실패 판단: STATE_CONNECTING에서 STATE_NONE으로 변경된 경우만 실패
                            // (connect() 시작 시 disconnect()에서 발생하는 STATE_NONE은 무시)
                            if (mPreviousBixolonState == BixolonSocketPrinter.STATE_CONNECTING) {
                                // 실제 연결 시도 후 실패한 경우
                                if (cDialog != null && cDialog.isShowing()) {
                                    cDialog.dismiss();
                                }
                                // 연결 실패 시 Toast + 실패음 (기존 ShipmentActivity와 동일)
                                Toast.makeText(getApplicationContext(), "접속이 원할하지 않습니다\n스캐너의 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                                sound_pool.play(sound_fail, 1.0f, 1.0f, 0, 0, 1.0f);
                            }
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setSubtitle("Not Connected");
                            }
                            break;
                    }
                    // 이전 상태 저장
                    mPreviousBixolonState = newState;
                    break;
                case BixolonSocketPrinter.MESSAGE_DEVICE_NAME:
                    // 연결된 프린터 디바이스명 저장 및 연결 성공 알림 (기존 ShipmentActivity와 동일)
                    mConnectedDeviceName = (String) msg.obj;
                    if (Common.D) {
                        Log.d(TAG, "MESSAGE_DEVICE_NAME : " + mConnectedDeviceName);
                    }
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    sound_pool.play(sound_success, 1.0f, 1.0f, 0, 0, 1.0f);
                    break;
                case BixolonSocketPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.d(TAG, "Print complete");
                    break;
                case BixolonSocketPrinter.MESSAGE_TOAST:
                    Toast.makeText(BixolonShipmentActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void setMessage(String msg) {
        if (Common.D) {
            Log.d(TAG, "바코드스캐너 입력값 : " + msg);
        }

        if (msg != null) {
            if (work_flag == 1) {
                setBarcodeMsg(msg);
            } else if(work_flag == 0){
                // BL코드로 계근 리스트 조회하기
                new ProgressDlgShipSelect(BixolonShipmentActivity.this, sp_center_name.getSelectedItem().toString(), msg, scan_flag).execute();
            } else if(work_flag == 2){
                setBarcodeMsg(msg);
            }
        }
    }

    // ========================================================================================
    // 바코드 처리 관련 필드 및 메서드
    // ========================================================================================

    /** 현재 작업 중인 바코드 정보 (S_BARCODE_INFO 테이블 데이터) */
    Barcodes_Info work_item_bi_info;
    /** 현재 작업 중인 패커 상품 코드 */
    String work_ppcode = "";
    /** 현재 작업 중인 BL 번호 */
    String work_bl_no = "";
    /** 스캔된 전체 바코드 문자열 (중량, 제조일 추출용) */
    String work_item_fullbarcode = "";
    /** 바코드 상품 코드 (BARCODEGOODS) */
    String work_item_barcodegoods = "";
    /** 소비기한 전송용 변수 */
    String expiryDayTrans = "";
    /** 다이얼로그 표시 중 플래그 (중복 처리 방지) */
    boolean dialog_flag = false;

    /**
     * 바코드 메시지 처리 핵심 메서드
     * <p>
     * 스캔된 바코드를 분석하여 상품을 매칭하고 계근 작업을 수행한다.
     * scan_flag에 따라 상품 바코드 또는 BL번호 바코드를 처리한다.
     * </p>
     *
     * <h3>처리 흐름</h3>
     * <ol>
     *   <li>scan_flag=true (상품 바코드 스캔):
     *     <ul>
     *       <li>work_flag=1: find_PackerProduct()로 바코드에서 패커상품코드 추출</li>
     *       <li>work_flag=2: find_PackerProductBarcodeGoods()로 상품코드로 검색</li>
     *       <li>중복 스캔 체크 (홈플러스 비정량은 중복 허용)</li>
     *       <li>출하 대상 조회 (ProgressDlgShipSelect 실행)</li>
     *     </ul>
     *   </li>
     *   <li>scan_flag=false (BL번호 스캔):
     *     <ul>
     *       <li>BL번호 일치 확인</li>
     *       <li>ITEM_TYPE에 따른 중량 추출 (W/HW: 바코드, S: 저울, J: 지정)</li>
     *       <li>LB→KG 환산 (필요시)</li>
     *       <li>wet_data_insert() 호출</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param msg 스캔된 바코드 문자열
     */
    public void setBarcodeMsg(final String msg) {
        try {
            if (dialog_flag)
                return;

            // 중복 호출 방지: 동일 바코드가 1초 이내 재처리되면 무시
            long now = System.currentTimeMillis();
            if ((now - lastBarcodeProcessedTime) < BARCODE_PROCESS_DEBOUNCE_MS) {
                Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
                return;
            }
            lastBarcodeProcessedTime = now;

            Log.e(TAG, "========================setBarcodeMsg 시작======================");

            edit_barcode.setText(msg);
            if (scan_flag) { // 패커상품 스캔
                Log.e(TAG, "========================상품스캔======================" + work_flag);
                try {
                    String find_ppcodetemp = "";
                    if (work_flag == 1) {
                        Log.e(TAG, "========================상품바코드스캔1======================");
                        find_ppcodetemp = find_PackerProduct(msg);
                        Log.e(TAG, "========================상품바코드스캔1 ppcode ======================" + find_ppcodetemp);
                    }else {
                        Log.e(TAG, "========================상품코드스캔2======================");
                        find_ppcodetemp = find_PackerProductBarcodeGoods(msg);
                        Log.e(TAG, "========================상품코드스캔2 ppcode ======================" + find_ppcodetemp);
                    }
                    Log.e(TAG, "========================바코드 정보가져옴======================");
                    final String find_ppcode = find_ppcodetemp;
                    if (find_ppcode.equals("null")) {
                        Toast.makeText(getApplicationContext(), "패커상품이 존재하지않거나,\n바코드가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(1000);
                        work_item_fullbarcode = "";
                        work_item_barcodegoods = "";
                    } else {
                        if (work_ppcode.equals("")) {
                            boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), msg);
                            // 최초 스캔일 경우
                            Log.e(TAG, "========================최초 스캔11======================");
                            Log.e(TAG, "========================find_ppcode test!!======================"+find_ppcode);
                            work_ppcode = find_ppcode;
                            work_item_fullbarcode = msg;
                            new ProgressDlgShipSelect(this, sp_center_name.getSelectedItem().toString(), find_ppcode, scan_flag).execute();
                        } else if (!work_ppcode.equals("") && work_ppcode.equals(find_ppcode)) {         // 작업 중이고, 같은 상품을 스캔했을 경우
                            work_item_fullbarcode = msg;
                            Log.e("바코드", "" + work_item_fullbarcode);
                            boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), work_item_fullbarcode);

                            if(Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)){ //비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
                                dup = false;
                            }

                            if (dup) {
                                Log.e(TAG, "=====================오류지점1=========================");
                                Toast.makeText(getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
                                vibrator.vibrate(1000);
                                work_item_fullbarcode = "";
                                work_item_barcodegoods = "";
                                return;
                            } else{
                                Log.e(TAG, "=====================상품스캔일반=========================");
                                set_scanFlag(false);        // BL스캔 시작
                                work_ppcode = find_ppcode;
                                work_item_fullbarcode = msg;

                                if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {       // 총 계근 완료
                                    show_wetFinishDialog();
                                }
                                setBarcodeMsg(msg);
                            }
                        } else if (!work_ppcode.equals(find_ppcode)) {                                   // 작업 중이고, 다른 상품을 스캔했을 경우
                            Log.e(TAG, "=====================작업중다른상품스캔=========================");
                            Log.i(TAG, "작업 중 다른 상품 스캔 !");
                            vibrator.vibrate(500);
                            dialog_flag = true;
                            new AlertDialog.Builder(BixolonShipmentActivity.this, R.style.AppCompatDialogStyle)
                                    .setIcon(R.drawable.highland)
                                    .setTitle(R.string.shipment_wet_other)
                                    .setMessage(R.string.shipment_wet_other_msg)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.shipment_wet_yes, (dialog, which) -> {
                                        dialog_flag = false;
                                        work_ppcode = find_ppcode;
                                        work_item_fullbarcode = msg;
                                        new ProgressDlgShipSelect(BixolonShipmentActivity.this, sp_center_name.getSelectedItem().toString(), find_ppcode, scan_flag).execute();
                                    })
                                    .setNegativeButton(R.string.shipment_wet_no, (dialog, which) -> {
                                        dialog_flag = false;
                                    })
                                    .show();
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "setBarcodeMsg's 패커상품 스캔 Exception -> " + ex.getMessage().toString());
                }
            } else {//BL스캔
                Log.e(TAG, "========================BL스캔======================" + sp_bl_no.getItemAtPosition(sp_bl_no.getSelectedItemPosition()).toString());

                work_item_fullbarcode = msg;

                try {
                    if (true) {
                        String temp_bl_no = sp_bl_no.getItemAtPosition(sp_bl_no.getSelectedItemPosition()).toString();
                        for (int i = 0; i < arSM.size(); i++) {
                            if (temp_bl_no.equals(arSM.get(i).getBL_NO()) && !arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
                                // BL번호 같은 상품 검색 완료
                                current_work_position = i;

                                Log.e(TAG, "========================current_work_position======================" + i);

                                work_bl_no = temp_bl_no;

                                Log.e(TAG, "========================work_bl_no======================" + work_bl_no);

                                break;
                            } else {
                                work_bl_no = "";
                                current_work_position = -1;
                            }
                        }

                        expiryDayTrans = ""; //일단 스캔할 때 마다 초기화

                        Log.e(TAG, "========================TEST TEST======================" + arSM.get(current_work_position).getCENTERNAME()); //센터 선택하고 스캔할떄 여기 탐

                        if (arSM.get(current_work_position).getPACKER_CODE().equals(KILKOY_PACKER_CODE) && arSM.get(current_work_position).getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) { //킬코이제품이면서 이마트미트센터나갈때, 미트센터는 지점이 없기 때문에 센터코드와 스토어코드가 같다. 현재 뷰에 센터코드가 없어서 스토어코드로 처리
                            if (work_item_bi_info.getSHELF_LIFE().equals("") || work_item_bi_info.getMAKINGDATE_FROM().equals("") || work_item_bi_info.getMAKINGDATE_TO().equals("")) {
                                Toast.makeText(getApplicationContext(), "미트센터 납품 - KILKOY 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n 현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                                vibrator.vibrate(1000);
                                work_ppcode = "";
                                scan_flag = true;
                                return;
                            }
                        } else if (arSM.get(current_work_position).getCENTERNAME().equals("용인TRD") || arSM.get(current_work_position).getCENTERNAME().equals("대구TRD") || arSM.get(current_work_position).getCENTERNAME().equals("시화(W)_TRD") || arSM.get(current_work_position).getCENTERNAME().equals("여주TRD") || arSM.get(current_work_position).getCENTERNAME().substring(0, 3).equals(CENTER_NAME_ET) || arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_ET)  ||  arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_WET)) {
                            if (Common.searchType.equals(SEARCH_TYPE_EMART)) {
                                if (work_item_bi_info.getSHELF_LIFE().equals("") || work_item_bi_info.getMAKINGDATE_FROM().equals("") || work_item_bi_info.getMAKINGDATE_TO().equals("")) {
                                    Toast.makeText(getApplicationContext(), "트레이더스 납품 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n 현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                                    vibrator.vibrate(1000);
                                    work_ppcode = "";
                                    scan_flag = true;
                                    return;
                                }
                            }
                        }

                        if (current_work_position == -1) {
                            Toast.makeText(getApplicationContext(), "해당하는 BL상품이 없습니다.\nBL번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            vibrator.vibrate(300);
                            return;
                        } else {
                            sp_point_name.setSelection(current_work_position);
                        }

                        sList.setSelection(current_work_position);      // 현재 계근지점으로 위치 변경

                        if (arSM.get(current_work_position).getGI_REQ_PKG().equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY()))) {
                            if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {       // 총 계근 완료
                                show_wetFinishDialog();
                            }
                            return;
                        }

                        Log.e(TAG, "=====================work_item_fullbarcode=========================" + work_item_fullbarcode);
                        Log.e(TAG, "=====================arSM.get(current_work_position).getGI_D_ID()=========================" + arSM.get(current_work_position).getGI_D_ID());
                        Log.e(TAG, "=====================arSM.get(current_work_position).getPACKER_PRODUCT_CODE()=========================" + arSM.get(current_work_position).getPACKER_PRODUCT_CODE());

                        boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(), work_item_fullbarcode,
                                arSM.get(current_work_position).getGI_D_ID(), arSM.get(current_work_position).getPACKER_PRODUCT_CODE(), arSM.get(current_work_position).getGI_L_ID());

                        if (Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) { //비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
                            dup = false;
                        }

                        Log.e(TAG, "=====================체크1=========================" + arSM.get(current_work_position).getPACKER_CODE());
                        Log.e(TAG, "=====================체크2=========================" + arSM.get(current_work_position).getSTORE_CODE());
                        Log.e(TAG, "=====================체크3=========================" + work_item_bi_info.getSHELF_LIFE());

                        if (dup) {
                            Log.e(TAG, "=====================오류지점2=========================");
                            Toast.makeText(getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
                            vibrator.vibrate(1000);
                            work_item_fullbarcode = "";
                            work_item_barcodegoods = "";
                            return;
                        }

                        Goodswets_Info gi = new Goodswets_Info();
                        Log.i(TAG, "## 패커상품코드 & BL번호 확인 완료. 계근 시작 ##");
                        Log.i(TAG, "현재 계근할 FULL 바코드                     : " + work_item_fullbarcode);
                        Log.i(TAG, "현재 계근할 바코드의 BarcodeGodos            : " + work_item_barcodegoods);
                        /*
                         * 계근 필드값
                         */
                        String item_weight = "";            // 상품 최초 중량 절사값(XXXX)
                        Double item_weight_double = 0.0;    // 상품 Double 중량값
                        String item_weight_str = "";        // 상품 String 중량값

                        double item_pow = 0;              // 상품 zeroPoint에 대한 pow

                        String item_making_date = "";       // 상품 제조일
                        String item_box_serial = "";        // 상품 박스시리얼
                        /*
                         *      중량(LB체크) / 제조일 / 박스번호 Find
                         */
                        Log.d(TAG, "******************current_work_position:" + arSM.get(current_work_position).getITEM_TYPE());

                        if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_W) || arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_HW)) {
                            String weight_from = work_item_bi_info.getWEIGHT_FROM();
                            String weight_to = work_item_bi_info.getWEIGHT_TO();

                            Log.d(TAG, "weightfrom,to:" + weight_from + ":" + weight_to + ":");
                            if (weight_from.equals("0") || weight_to.equals("0")) {
                                showAlertDialog("weight", 0);
                                alert_flag = true;
                            }

                            // 이마트 ITEM_TYPE W (바코드 계근)
                            item_weight = work_item_fullbarcode.substring(
                                    Integer.parseInt(work_item_bi_info.getWEIGHT_FROM()) - 1, Integer.parseInt(work_item_bi_info.getWEIGHT_TO()));
                            Log.i(TAG, "Type W | 절사한 중량값 : " + item_weight);

                            item_pow = Math.pow(10, Integer.parseInt(work_item_bi_info.getZEROPOINT()));
                            Log.i(TAG, "Type W | item_pow 확인 : " + item_pow);
                            Log.i(TAG, "Type W | zero point 확인 : " + work_item_bi_info.getZEROPOINT());

                            item_weight_double = Double.parseDouble(item_weight) / item_pow;

                            Log.i(TAG, "Type W | item_weight 확인 : " + Double.parseDouble(item_weight));
                            Log.i(TAG, "Type W | item_weight_double 확인 : " + item_weight_double);

                            if ("LB".equals(work_item_bi_info.getBASEUNIT())) {
                                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                                double temp_weight_double = item_weight_double * 0.453592;
                                item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
                                item_weight_str = String.valueOf(item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
                            }

                            item_weight_double = Math.floor(item_weight_double * 10);
                            item_weight_double = item_weight_double / 10.0;

                            String temp_weight = String.format("%.1f", item_weight_double);
                            item_weight_double = Double.parseDouble(temp_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type W | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type W | ZeroPoint 적용 중량 String값 : " + item_weight_str);

                            if (work_item_bi_info.getMAKINGDATE_FROM() != "" && work_item_bi_info.getMAKINGDATE_TO() != "") {
                                item_making_date = work_item_fullbarcode.substring(
                                        Integer.parseInt(work_item_bi_info.getMAKINGDATE_FROM()) - 1, Integer.parseInt(work_item_bi_info.getMAKINGDATE_TO()));
                                Log.i(TAG, "Type W | 절사한 제조일 : " + item_making_date);
                            }

                            if (work_item_bi_info.getBOXSERIAL_FROM() != "" && work_item_bi_info.getBOXSERIAL_TO() != "") {
                                item_box_serial = work_item_fullbarcode.substring(
                                        Integer.parseInt(work_item_bi_info.getBOXSERIAL_FROM()) - 1, Integer.parseInt(work_item_bi_info.getBOXSERIAL_TO()));
                                Log.i(TAG, "Type W | 절사한 박스시리얼 : " + item_box_serial);
                            }
                        } else if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_S)) {
                            String weight_from = work_item_bi_info.getWEIGHT_FROM();
                            String weight_to = work_item_bi_info.getWEIGHT_TO();

                            Log.d(TAG, "weightfrom,to:" + weight_from + ":" + weight_to + ":");
                            if (weight_from.equals("0") || weight_to.equals("0")) {
                                showAlertDialog("weight", 0);
                                alert_flag = true;
                            }

                            // 이마트 ITEM_TYPE W (바코드 계근)
                            item_weight = work_item_fullbarcode.substring(
                                    Integer.parseInt(work_item_bi_info.getWEIGHT_FROM()) - 1, Integer.parseInt(work_item_bi_info.getWEIGHT_TO()));
                            Log.i(TAG, "Type S | 절사한 중량값 : " + item_weight);

                            item_pow = Math.pow(10, Integer.parseInt(work_item_bi_info.getZEROPOINT()));
                            item_weight_double = Double.parseDouble(item_weight) / item_pow;

                            if ("LB".equals(work_item_bi_info.getBASEUNIT())) {
                                Log.i(TAG, "LB로 들어옴, 환산");
                                Log.i(TAG, "LB 원 중량 : " + item_weight_double);
                                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                                double temp_weight_double = item_weight_double * 0.453592;

                                if (Common.searchType.equals(SEARCH_TYPE_EMART)) {
                                    item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
                                } else {
                                    item_weight_double = Math.floor(temp_weight_double * 100) / 100; //lb 변환 후 소수점 두자리까지 처리하도록 변경
                                }
                                item_weight_str = String.valueOf(item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
                            }

                            String temp_weight = String.format("%.2f", item_weight_double);
                            item_weight_double = Double.parseDouble(temp_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type S | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type S | ZeroPoint 적용 중량 String값 : " + item_weight_str);

                            if (work_item_bi_info.getMAKINGDATE_FROM() != "" && work_item_bi_info.getMAKINGDATE_TO() != "") {
                                item_making_date = work_item_fullbarcode.substring(
                                        Integer.parseInt(work_item_bi_info.getMAKINGDATE_FROM()) - 1, Integer.parseInt(work_item_bi_info.getMAKINGDATE_TO()));
                                Log.i(TAG, "Type S | 절사한 제조일 : " + item_making_date);
                            }

                            if (work_item_bi_info.getBOXSERIAL_FROM() != "" && work_item_bi_info.getBOXSERIAL_TO() != "") {
                                item_box_serial = work_item_fullbarcode.substring(
                                        Integer.parseInt(work_item_bi_info.getBOXSERIAL_FROM()) - 1, Integer.parseInt(work_item_bi_info.getBOXSERIAL_TO()));
                                Log.i(TAG, "Type S | 절사한 박스시리얼 : " + item_box_serial);
                            }
                        } else if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_J)) {
                            // 이마트 ITEM_TYPE J (지정된 중량 입력) | 바코드에서 중량, 제조일, 박스시리얼 X
                            item_weight = arSM.get(current_work_position).getPACKWEIGHT();
                            Log.i(TAG, "Type J | 지정된 중량값 : " + item_weight);
                            item_weight_double = Double.parseDouble(item_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type J | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type J | ZeroPoint 적용 중량 String값 : " + item_weight_str);
                        }

                        // Homeplus 비정량 "B"
                        if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_B)) {
                            String weight_from = work_item_bi_info.getWEIGHT_FROM();
                            String weight_to = work_item_bi_info.getWEIGHT_TO();

                            Log.d(TAG, "weightfrom,to:" + weight_from + ":" + weight_to + ":");
                            if (weight_from.equals("0") || weight_to.equals("0")) {
                                showAlertDialog("weight", 0);
                                alert_flag = true;
                            }

                            // 이마트 ITEM_TYPE W (바코드 계근)
                            item_weight = work_item_fullbarcode.substring(
                                    Integer.parseInt(work_item_bi_info.getWEIGHT_FROM()) - 1, Integer.parseInt(work_item_bi_info.getWEIGHT_TO()));
                            Log.i(TAG, "Type S | 절사한 중량값 : " + item_weight);

                            item_pow = Math.pow(10, Integer.parseInt(work_item_bi_info.getZEROPOINT()));
                            item_weight_double = Double.parseDouble(item_weight) / item_pow;

                            if ("LB".equals(work_item_bi_info.getBASEUNIT())) {
                                Log.i(TAG, "LB로 들어옴, 환산");
                                Log.i(TAG, "LB 원 중량 : " + item_weight_double);
                                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                                double temp_weight_double = item_weight_double * 0.453592;

                                if (Common.searchType.equals(SEARCH_TYPE_EMART)) {
                                    item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
                                } else {
                                    item_weight_double = Math.floor(temp_weight_double * 100) / 100; //lb 변환 후 소수점 두자리까지 처리하도록 변경
                                }
                                item_weight_str = String.valueOf(item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
                            }

                            String temp_weight = String.format("%.2f", item_weight_double);
                            item_weight_double = Double.parseDouble(temp_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type S | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type S | ZeroPoint 적용 중량 String값 : " + item_weight_str);

                            if (work_item_bi_info.getMAKINGDATE_FROM() != "" && work_item_bi_info.getMAKINGDATE_TO() != "") {
                                item_making_date = work_item_fullbarcode.substring(
                                        Integer.parseInt(work_item_bi_info.getMAKINGDATE_FROM()) - 1, Integer.parseInt(work_item_bi_info.getMAKINGDATE_TO()));
                                Log.i(TAG, "Type S | 절사한 제조일 : " + item_making_date);
                            }

                            if (work_item_bi_info.getBOXSERIAL_FROM() != "" && work_item_bi_info.getBOXSERIAL_TO() != "") {
                                item_box_serial = work_item_fullbarcode.substring(
                                        Integer.parseInt(work_item_bi_info.getBOXSERIAL_FROM()) - 1, Integer.parseInt(work_item_bi_info.getBOXSERIAL_TO()));
                                Log.i(TAG, "Type S | 절사한 박스시리얼 : " + item_box_serial);
                            }
                        }

                    wet_data_insert(item_weight_str, item_weight_double, item_making_date, item_box_serial);
                    } else {
                        Toast.makeText(getApplicationContext(), "BL번호가 일치하지않습니다.\n확인 후 다시 스캔해주세요.", Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(1000);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "setBarcodeMsg's BL 스캔 Exception -> " + ex.getMessage().toString());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "setBarcodeMsg Exception -> " + ex.getMessage().toString());
        }
    }

    // ========================================================================================
    // 핵심 비즈니스 로직 메서드
    // ========================================================================================

    /**
     * 계근 데이터 저장 및 UI 업데이트
     * <p>
     * 계근 완료된 데이터를 로컬 DB에 저장하고 화면에 반영하는 핵심 메서드.
     * 출하 유형에 따라 다른 DB 테이블/로직을 사용한다.
     * </p>
     *
     * <h3>처리 흐름</h3>
     * <ol>
     *   <li>요청수량 완료 여부 체크 - 완료 시 다음 지점으로 이동 알림</li>
     *   <li>Goodswets_Info 객체 생성 및 데이터 설정</li>
     *   <li>출하 유형별 DB 저장:
     *     <ul>
     *       <li>홈플러스(2): insertqueryGoodsWetHomeplus()</li>
     *       <li>롯데(6): insertqueryGoodsWetLotte() - 박스 순번(lotte_TryCount) 관리</li>
     *       <li>기타: insertqueryGoodsWet()</li>
     *     </ul>
     *   </li>
     *   <li>출하 유형별 중량 처리:
     *     <ul>
     *       <li>이마트(0): 소수점 첫째자리까지 (10단위 반올림)</li>
     *       <li>생산/홈플러스: 입력값 그대로</li>
     *     </ul>
     *   </li>
     *   <li>계근수량(PACKING_QTY), 계근중량(GI_QTY) 업데이트</li>
     *   <li>센터 합계 업데이트 (centerWorkCount, centerWorkWeight)</li>
     *   <li>UI 업데이트 (EditText, ListView)</li>
     *   <li>라벨 인쇄 (Common.print_bool이 true일 때):
     *     <ul>
     *       <li>홈플러스(2,5): setHomeplusPrinting()</li>
     *       <li>이마트(0,4): setPrinting()</li>
     *       <li>롯데(6): setPrintingLotte()</li>
     *       <li>생산(7): setPrinting_prod()</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param weight_str 중량 문자열 (서버 전송용)
     * @param weight_double 중량 실수값 (UI 표시 및 계산용)
     * @param making_date 제조일자 (바코드에서 추출, 없으면 빈 문자열)
     * @param box_serial 박스 시리얼 번호 (바코드에서 추출, 없으면 빈 문자열)
     */
    public void wet_data_insert(String weight_str, double weight_double, String making_date, String box_serial) {
        Log.e(TAG, "=========================계근입력 시작=========================" + weight_double);
        if (arSM.get(current_work_position).getGI_REQ_PKG().equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY()))) {
            if ((arSM.size() - 1) == current_work_position) {
                show_wetFinishDialog();
            } else {
                Toast.makeText(getApplicationContext(), "계근이 끝난 지점입니다.\n다음 지점을 작업해주세요.", Toast.LENGTH_SHORT).show();
                vibrator.vibrate(300);
            }
            return;
        }

        Goodswets_Info gi = new Goodswets_Info();
        gi.setGI_D_ID(arSM.get(current_work_position).getGI_D_ID());
        gi.setGI_L_ID(arSM.get(current_work_position).getGI_L_ID());
        gi.setWEIGHT(weight_str);
        gi.setWEIGHT_UNIT(work_item_bi_info.getBASEUNIT());
        gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE());
        gi.setBARCODE(work_item_fullbarcode);
        gi.setPACKER_CLIENT_CODE(work_item_bi_info.getPACKER_CLIENT_CODE());
        gi.setMAKINGDATE(making_date);
        gi.setBOXSERIAL(box_serial);
        gi.setBOX_CNT(String.valueOf((arSM.get(current_work_position).getPACKING_QTY() + 1)));
        gi.setEMARTITEM_CODE(arSM.get(current_work_position).getEMARTITEM_CODE());
        gi.setEMARTITEM(arSM.get(current_work_position).getEMARTITEM());
        gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE());
        gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE());
        gi.setREG_ID(Common.REG_ID);
        gi.setSAVE_TYPE("F");
        gi.setDUPLICATE("F");

        String lotteBoxOrder = ""; // 롯데 전용 박스 순번을 담을 변수

        if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS)) { //홈플러스
            int maxBoxOrder = DBHandler.selectMaxBoxOrder(this);
            Log.e(TAG, "=======================MAX BOX ORDER ###=========================" + maxBoxOrder);
            DBHandler.insertqueryGoodsWetHomeplus(this, gi, maxBoxOrder);
        } else if (Common.searchType.equals(SEARCH_TYPE_LOTTE)) { //롯데
            // 1. 현재 lotte_TryCount 값을 이 계근 건의 박스 순번으로 확정
            lotteBoxOrder = String.valueOf(lotte_TryCount);
            // 2. 확정된 번호를 사용하여 DB에 저장
            DBHandler.insertqueryGoodsWetLotte(this, gi, lotte_TryCount);
            // 3. DB 저장이 끝난 직후, 다음 계근을 위해 카운터 즉시 증가
            lotte_TryCount++;
            if (lotte_TryCount > LOTTE_BOX_ORDER_MAX) {
                lotte_TryCount = 1;
            }
        } else { //
            DBHandler.insertqueryGoodsWet(this, gi);
        }

        Log.e(TAG, "=========================계근중량 변환전=========================" + weight_double);

        String temp_weight = "";

        if(Common.searchType.equals(SEARCH_TYPE_EMART)) { //이마트
            weight_double = Math.floor(weight_double * 10);
            weight_double = weight_double / 10.0;
            temp_weight = String.format("%.1f", weight_double);
        }else{ //생산 혹은 홈플러스
            temp_weight = Double.toString(weight_double); //생산일 경우 그대로 입력
        }

        weight_double = Double.parseDouble(temp_weight);

        Log.e(TAG, "=========================계근중량 변환후=========================" + weight_double);

        arSM.get(current_work_position).setPACKING_QTY(arSM.get(current_work_position).getPACKING_QTY() + 1);           // 계근수량

        if(Common.searchType.equals(SEARCH_TYPE_EMART)) {
            arSM.get(current_work_position).setGI_QTY(Math.round((arSM.get(current_work_position).getGI_QTY() + weight_double) * 10.0) / 10.0);    // 계근중량
        }else{

            double v1 = arSM.get(current_work_position).getGI_QTY();
            double v2 = weight_double;

            double v3 = v1+v2;
            double v4 = Math.round(v3*1000)/1000.0;

            arSM.get(current_work_position).setGI_QTY(v4);    // 계근중량 변경 후
            Log.e(TAG, "=========================chk prod 계근중량=========================" + v4);
        }

        centerWorkCount++;
        centerWorkWeight += weight_double;

        Log.e(TAG, "=========================센터중량 변환전=========================" + centerWorkWeight);

        if(Common.searchType.equals(SEARCH_TYPE_EMART)) { //출하일 경우에만 round 처리
            centerWorkWeight = Math.round(centerWorkWeight * 100.0) / 100.0;
        }else{ //생산일 경우
            centerWorkWeight = Math.round(centerWorkWeight*1000)/1000.0; //생산일 경우 소수점 넷째자리에서 반올림
        }

        Log.e(TAG, "=========================센터중량 변환후=========================" + centerWorkWeight);

        edit_center_tcount.setText(centerTotalCount + " / " + centerWorkCount);

        if(Common.searchType.equals(SEARCH_TYPE_EMART)) { //출하일때
            edit_center_tweight.setText(Math.round(centerTotalWeight * 10) / 10.0 + " / " + centerWorkWeight);
        }else{ //생산일때
            edit_center_tweight.setText(centerTotalWeight + " / " + centerWorkWeight);
        }

        edit_wet_count.setText(arSM.get(current_work_position).getGI_REQ_PKG() + " / " + arSM.get(current_work_position).getPACKING_QTY());
        edit_wet_weight.setText(arSM.get(current_work_position).getGI_REQ_QTY() + " / " + arSM.get(current_work_position).getGI_QTY());

        Log.d(TAG, "==================================================");
        Log.d(TAG, "====================계근작업 종료===================");
        Log.i(TAG, "centerWorkCount : " + centerWorkCount);
        Log.i(TAG, "centerWorkWeight : " + centerWorkWeight);
        Log.d(TAG, "==================================================");

        for (int i = 0; i < arSM.size(); i++) {
            arSM.get(i).setWORK_FLAG(0);
        }
        arSM.get(current_work_position).setWORK_FLAG(1);
        sListAdapter.notifyDataSetChanged();
        sList.setSelection(current_work_position);

        if (Common.print_bool) {
            if (Common.searchType.equals(SEARCH_TYPE_HOMEPLUS) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
                Log.d(TAG, "===========홈플 출력 시작 ================");
                labelPrintHelper.setHomeplusPrinting(weight_double, arSM.get(current_work_position), false, printerCallback);
            }else if(Common.searchType.equals(SEARCH_TYPE_EMART)){
                Log.d(TAG, "===========이마트 출력 시작 ================");
                labelPrintHelper.setPrinting(weight_double, arSM.get(current_work_position), false, making_date, work_item_bi_info, arSM.get(current_work_position), Common.searchType, printerCallback);
            }else if(Common.searchType.equals(SEARCH_TYPE_NONFIXED)){
                Log.d(TAG, "===========이마트(비정량) 출력 시작 ================");
                labelPrintHelper.setPrinting(weight_double, arSM.get(current_work_position), false, making_date, work_item_bi_info, arSM.get(current_work_position), Common.searchType, printerCallback);
            }else if(Common.searchType.equals(SEARCH_TYPE_LOTTE)){
                Log.d(TAG, "===========롯데 출력 시작 ================");
                labelPrintHelper.setPrintingLotte(weight_double, arSM.get(current_work_position), false, making_date, lotteBoxOrder, Common.searchType, printerCallback);
            }else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)){
                Log.d(TAG, "===========생산 출력 시작 ================");
                labelPrintHelper.setPrinting_prod(weight_double, arSM.get(current_work_position), false, printerCallback);
            }
        }

        set_scanFlag(true);

        if (Integer.parseInt(arSM.get(current_work_position).getGI_REQ_PKG()) <= arSM.get(current_work_position).getPACKING_QTY()) {
            // 요청수량과 계근수량이 같을 때 (계근이 끝났을 때)
            if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {       // 총 계근 완료
                show_wetFinishDialog();
            }
        }
    }

    public void scanFlag_init() {
        if (work_flag == 1)
            scan_flag = true;
        else if (work_flag == 0)
            scan_flag = false;
        else if (work_flag == 2)
            scan_flag = true;
        Log.i(TAG, "############ scan_flag init ###########");
    }

    public void scanFlag_swap() {
        if (scan_flag)
            scan_flag = false;
        else
            scan_flag = true;
        Log.i(TAG, "############ scan_flag Swap ###########");
        Log.i(TAG, "####### scan_flag : " + scan_flag + " #######");
    }


    public void set_scanFlag(boolean bool) {
        scan_flag = bool;
        Log.i(TAG, "####### scan_flag : " + scan_flag + " #######");
    }

    public String find_PackerProduct(String barcode) {
        try {
            String pp_code = "";
            Log.e(TAG, "========================pp_code 가져오기 시작======================");
            pp_code = find_work_info(barcode, true);
            Log.e(TAG, "========================pp_code 가져오기 끝======================");
            if (!edit_product_name.getText().equals("")) {
                return pp_code;
            } else {
                return "null";
            }
        } catch (Exception ex) {
            Log.e(TAG, "find_PackerProduct Exception | " + ex.getMessage().toString());
            return "null";
        }
    }

    public String find_PackerProductBarcodeGoods(String barcode) {
        Log.e(TAG, "find_PackerProductBarcodeGoods");
        try {
            String pp_code = "";
            pp_code = find_work_info_barcodeGoods(barcode, false);
            if (!edit_product_name.getText().equals("")) {
                return pp_code;
            } else {
                return "null";
                //scanFlag_swap();
            }
        } catch (Exception ex) {
            Log.e(TAG, "find_PackerProduct Exception | " + ex.getMessage().toString());
            return "null";
        }
    }

    public boolean find_BL(String barcode) {
        boolean result = false;
        String BL_NO = barcode;
        if (BL_NO.equals(arSM.get(current_work_position).getBL_NO())) {
            result = true;
        } else {
            result = false;
        }

        return result;
    }

    private String find_work_info(String req, boolean type) {
        try {
            String pp_code = "";
            int count = 0;
            ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeInfo(this);
            Log.e(TAG, "===================바코드 디비조회 완료=======================");
            Log.e(TAG, "===================    req check !!   ======================="+req); //여기서 풀바코드를 던진다
            for (Barcodes_Info bi : list_barcode_info) {
                String bg = bi.getBARCODEGOODS();
                String bg_from = bi.getBARCODEGOODS_FROM();
                String bg_to = bi.getBARCODEGOODS_TO();
                String temp_bg;

                Log.i(TAG, "BARCODEGOODS \t\tFROM : " + bg_from + "\t TO : " + bg_to);
                Log.i(TAG, "BARCODEGOODS : \t\t" + bg);
                if (type && req.length() >= Integer.parseInt(bg_to)) {              // PACKER_PRODUCT_CODE로 찾을 경우
                    temp_bg = req.substring(Integer.parseInt(bg_from) - 1, Integer.parseInt(bg_to));
                } else {                // false : BL로 찾을 경우
                    temp_bg = req;
                }

                Log.i(TAG, "TEMP BARCODEGOODS : \t" + temp_bg);
                Log.i(TAG, "TEMP BARCODEGOODS eq : \t" + temp_bg.equals(bg));

                if (temp_bg.equals(bg)) {                       // barcodegoods find success
                    Log.i(TAG, "barcodegoods find success");
                    work_item_bi_info = bi;
                    edit_product_name.setText(bi.getITEM_NAME_KR());
                    edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
                    if(count == 0){
                        pp_code = bi.getPACKER_PRODUCT_CODE();
                    }else{
                        pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                    }
                    Log.i(TAG, "===================pp_code=================" + pp_code);
                    work_item_barcodegoods = bg;
                    count++;
                } else {
                    edit_product_name.setText("");
                    edit_product_code.setText("");
                    work_item_barcodegoods = "";
                }

                if(Common.searchType.equals(SEARCH_TYPE_NONFIXED)){
                    work_item_bi_info = bi;
                    edit_product_name.setText(bi.getITEM_NAME_KR());
                    edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
                    if(count == 0){
                        pp_code = bi.getPACKER_PRODUCT_CODE();
                    }else{
                        pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                    }
                    Log.i(TAG, "===================pp_code=================" + pp_code);
                    work_item_barcodegoods = bg;
                    count++;
                }
            }

            Log.i(TAG, "===================return pp_code test!!! =================" + pp_code);
            return pp_code;
        } catch (Exception ex) {
            Log.e(TAG, "======== find_work_info Exception ========");
            Log.e(TAG, ex.getMessage().toString());
            return "null";
        }
    }

    private String find_work_info_barcodeGoods(String req, boolean type) {
        Log.e(TAG, "find_work_info_barcodeGoods");
        try {
            String pp_code = "";
            int count = 0;
            ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeGoodsInfo(this);
            for (Barcodes_Info bi : list_barcode_info) {
                String bg = bi.getBARCODEGOODS();
                String bg_from = bi.getBARCODEGOODS_FROM();
                String bg_to = bi.getBARCODEGOODS_TO();
                String temp_bg;
                if (type) {              // PACKER_PRODUCT_CODE로 찾을 경우
                    temp_bg = req.substring(Integer.parseInt(bg_from) - 1, Integer.parseInt(bg_to));
                } else {                // false : BL로 찾을 경우
                    temp_bg = req;
                }
                Log.i(TAG, "BARCODEGOODS \t\tFROM : " + bg_from + "\t TO : " + bg_to);
                Log.i(TAG, "BARCODEGOODS : \t\t" + bg);
                Log.i(TAG, "TEMP BARCODEGOODS : \t" + temp_bg);

                if (temp_bg.equals(bg)) {                       // barcodegoods find success
                    //pp_name = bi.getITEM_NAME_KR();
                    work_item_bi_info = bi;
                    edit_product_name.setText(bi.getITEM_NAME_KR());
                    edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
                    if(count == 0){
                        pp_code = bi.getPACKER_PRODUCT_CODE();
                    }else{
                        pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                    }
                    Log.i(TAG, "===================pp_code=================" + pp_code);
                    //work_ppcode = bi.getPACKER_PRODUCT_CODE();
                    work_item_barcodegoods = bg;
                    count++;
                } else {
                    edit_product_name.setText("");
                    edit_product_code.setText("");
                    work_item_barcodegoods = "";
                }
            }
            return pp_code;
        } catch (Exception ex) {
            Log.e(TAG, "======== find_work_info_barcodeGoods Exception ========");
            Log.e(TAG, ex.getMessage().toString());
            return "null";
        }
    }

    private Spinner.OnItemSelectedListener workSelectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            try {
                Toast.makeText(getApplicationContext(), "현재작업 : " + sp_center_name.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                edit_barcode.setText("");
                switch (arg2) {
                    case 0:        // 바코드
                        work_flag = 1;
                        scan_flag = true;
                        break;
                    case 1:        // 수기
                        work_flag = 0;
                        work_item_fullbarcode = "";
                        set_scanFlag(false);        // 수기 BL스캔
                        if (arSM.size() > 0) {
                            current_work_position = -1;
                            for (int i = 0; i < arSM.size(); i++) {
                                if (!arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {     // 계근이 완료되지 않은 지점
                                    current_work_position = i;
                                    sp_point_name.setSelection(current_work_position);
                                    break;
                                }
                            }
                            if (current_work_position == -1)
                                show_wetFinishDialog();
                        }
                        break;
                    case 2:     // 상품코드
                        work_flag = 2;
                        scan_flag = true;
                        break;
                }
            } catch (Exception ex) {
                Log.e(TAG, "======== workSelectedListener Exception ========");
                Log.e(TAG, ex.getMessage().toString());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    //센터명 spinner
    private Spinner.OnItemSelectedListener emartCenterSelectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            try {
                Toast.makeText(getApplicationContext(), "센터명 : " + sp_center_name.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                if (!work_ppcode.equals("")) {
                    // ############# 바코드 작업이냐 수기 작업이냐 구분해서 scan_flag값 주기  ##################
                    Log.e(TAG, "========================1====================");
                    if (work_flag == 1) {
                        set_scanFlag(true);
                        work_ppcode = "";
                    } else if(work_flag == 0){
                        set_scanFlag(false);
                    } else if(work_flag == 2){
                        set_scanFlag(true);
                        work_ppcode = "";
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "======== emartCenterSelectedListener Exception ========");
                Log.e(TAG, ex.getMessage().toString());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private Spinner.OnItemSelectedListener emartPointSelectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            try {
                Toast.makeText(getApplicationContext(), "작업지점 : " + sp_point_name.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                current_work_position = sp_point_name.getSelectedItemPosition();
                //작업지점 선택시 List해당 지점 작업으로 보여지게 이동
                sp_point_name.setSelection(current_work_position);

                calc_info(current_work_position);

                if(arSM.get(current_work_position).getBL_NO() == ""){
                    showAlertDialog("bl",current_work_position+1);
                    alert_flag = true;
                }else{
                    alert_flag = false;
                }
            } catch (Exception ex) {
                Log.e(TAG, "======== emartPointSelectedListener Exception ========");
                Log.e(TAG, ex.getMessage().toString());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private void calc_info(int work_position) {
        try {
            ArrayList<String> list_bl = new ArrayList<String>();
            list_bl.add(arSM.get(work_position).getBL_NO());
            ArrayAdapter<String> bl_adapter = new ArrayAdapter<String>(BixolonShipmentActivity.this, android.R.layout.simple_spinner_item, list_bl);
            bl_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_bl_no.setAdapter(bl_adapter);

            work_bl_no = arSM.get(work_position).getBL_NO();
            edit_wet_count.setText(arSM.get(work_position).getGI_REQ_PKG() + " / " + arSM.get(work_position).getPACKING_QTY());
            edit_wet_weight.setText(arSM.get(work_position).getGI_REQ_QTY() + " / " + arSM.get(work_position).getGI_QTY());
            //해당 지점 내용불러오기
            edit_product_name.setText(arSM.get(work_position).getITEM_NAME());
            edit_product_code.setText(arSM.get(work_position).getPACKER_PRODUCT_CODE());

            if (!select_flag) {
                scanFlag_init();
            }
        } catch (Exception ex) {
            Log.e(TAG, "======== calc_info Exception ========");
            Log.e(TAG, ex.getMessage().toString());
        }
    }


    // ========================================================================================
    // Label printing methods moved to LabelPrintHelper
    // - setPrinting_prod(): Production label
    // - setPrinting(): Emart/Non-fixed label
    // - setHomeplusPrinting(): Homeplus label
    // - setPrintingLotte(): Lotte label
    // @see com.rgbsolution.highland_emart.print.LabelPrintHelper
    // ========================================================================================

    // ========================================================================================
    // SLCS 헬퍼 메서드 → LabelPrintHelper로 이동됨
    // ========================================================================================

    // SLCS 메소드들 - weight list printing(4143, 4188줄)에서 사용되므로 유지
    // LabelPrintHelper에도 복사본 존재 (라벨 출력용)
    private String slcsInit() {
        return "CB\r\n" + "CS13,0\r\n";
    }

    /**
     * SLCS 라벨 크기 설정
     *
     * @param width  라벨 너비 (도트)
     * @param height 라벨 높이 (도트)
     * @return SLCS 라벨 크기 명령어 문자열
     */
    private String slcsLabelSize(int width, int height) {
        return "SW" + width + "\r\n" + "SL" + height + "\r\n";
    }

    /**
     * SLCS 텍스트 출력
     * - V 명령어 사용 (벡터 폰트)
     *
     * @param x      X 좌표
     * @param y      Y 좌표
     * @param width  폰트 너비
     * @param height 폰트 높이
     * @param text   출력할 텍스트
     * @return SLCS 텍스트 명령어 문자열
     */
    private String slcsText(int x, int y, int width, int height, String text) {
        // V x,y,K,w,h,0,N,B,N,0,L,0,'text'
        // K: 한글, 0: 회전없음, N: 일반, B: 굵게, N: 이탤릭없음, 0: 자간, L: 왼쪽정렬, 0: 줄간격
        return "V" + x + "," + y + ",K," + width + "," + height + ",0,N,B,N,0,L,0,'" + text + "'\r\n";
    }

    /**
     * SLCS CODE128 바코드 생성
     *
     * @param x      X 좌표
     * @param y      Y 좌표
     * @param height 바코드 높이
     * @param data   바코드 데이터
     * @return SLCS 바코드 명령어 문자열
     */
    private String slcsBarcode(int x, int y, int height, String data) {
        // BD x,y,barcode_type,narrow,wide,height,rotation,HRI,quiet_zone,'data'
        // CODE128, narrow=2, wide=4, HRI=0(없음), quiet_zone=0
        return "BD" + x + "," + y + ",CODE128,2,4," + height + ",0,0,0,'" + data + "'\r\n";
    }

    /**
     * SLCS 선 그리기
     *
     * @param x1    시작 X 좌표
     * @param y1    시작 Y 좌표
     * @param x2    끝 X 좌표
     * @param y2    끝 Y 좌표
     * @param width 선 두께
     * @return SLCS 선 명령어 문자열
     */
    private String slcsLine(int x1, int y1, int x2, int y2, int width) {
        // LS x1,y1,x2,y2,width
        return "LS" + x1 + "," + y1 + "," + x2 + "," + y2 + "," + width + "\r\n";
    }

    /**
     * SLCS 박스 그리기
     *
     * @param x         X 좌표
     * @param y         Y 좌표
     * @param width     박스 너비
     * @param height    박스 높이
     * @param thickness 선 두께
     * @return SLCS 박스 명령어 문자열
     */
    private String slcsBox(int x, int y, int width, int height, int thickness) {
        // LB x1,y1,x2,y2,thickness
        return "LB" + x + "," + y + "," + (x + width) + "," + (y + height) + "," + thickness + "\r\n";
    }

    /**
     * SLCS 인쇄 실행
     *
     * @param copies 인쇄 매수
     * @return SLCS 인쇄 명령어 문자열
     */
    private String slcsPrint(int copies) {
        return "P" + copies + "\r\n";
    }

    /**
     * SLCS 라벨 피드 (마크 위치로 이동)
     * - 원본: WoosimCmd.feedToMark()에 대응
     * - SLCS T 명령어: Tear-off 위치로 라벨 이동
     *
     * @return SLCS 피드 명령어 문자열
     */
    private String slcsFeedToMark() {
        return "T\r\n";
    }
    // SLCS 메소드들 끝

    // ========================================================================================
    // 프린터 데이터 전송
    // ========================================================================================

    private void sendData(byte[] data) {
        // Check that we're actually connected before trying printing
        if (mBixolonPrinter.getState() != BixolonSocketPrinter.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        // EUC-KR로 이미 인코딩된 바이트 배열을 직접 전송
        if (data.length > 0)
            mBixolonPrinter.sendCommandBytes(data);
    }

    // ========================================================================================
    // AsyncTask 클래스 - 백그라운드 비동기 작업
    // ========================================================================================

    /**
     * 출하 대상 조회 AsyncTask
     * <p>
     * 패커상품코드 또는 BL번호로 출하 대상 목록을 조회한다.
     * 로컬 DB에서 조회하여 arSM 리스트에 저장하고 ListView에 표시한다.
     * </p>
     *
     * <h3>처리 흐름</h3>
     * <ol>
     *   <li>onPreExecute: 로딩 다이얼로그 표시, 카운터 초기화</li>
     *   <li>doInBackground: DBHandler.selectqueryShipment() 호출</li>
     *   <li>롯데의 경우 lotte_TryCount 계산 (박스 순번 관리)</li>
     *   <li>onPostExecute: ListView 어댑터 설정, 센터 합계 표시</li>
     * </ol>
     *
     * @see DBHandler#selectqueryShipment(Context, String, String, boolean)
     */
    class ProgressDlgShipSelect extends AsyncTask<Integer, String, Integer> {
        private Context mContext;
        private String center_name;
        private String condition;
        private boolean type;

        public ProgressDlgShipSelect(Context context, String center_name, String condition, boolean type) {
            mContext = context;
            this.center_name = center_name;
            this.condition = condition;
            this.type = type;           // true ? 패커상품코드 스캔 : BL스캔
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(mContext);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setTitle("출하대상 불러오는중...");
            pDialog.setMessage("잠시만 기다려 주세요..");
            pDialog.setCancelable(false);
            pDialog.show();

            //current_work_position = -1;
            centerTotalCount = 0;
            centerTotalWeight = 0.0;
            centerWorkCount = 0;
            centerWorkWeight = 0.0;
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                //계근지점 검색 하는 쿼리
                arSM = DBHandler.selectqueryShipment(mContext, this.center_name, this.condition, this.type);

                for (int i = 0; i < arSM.size(); i++) {
                    String[] row = new String[2];
                    row = DBHandler.selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getPACKER_PRODUCT_CODE(), arSM.get(i).getCLIENT_CODE(), arSM.get(i).getGI_L_ID());
                    arSM.get(i).setGI_QTY(Double.parseDouble(row[0]));      // 중량
                    arSM.get(i).setPACKING_QTY(Integer.parseInt(row[1]));   // 수량
                    arSM.get(i).setSAVE_CNT(Integer.parseInt(row[2]));      // 계근 상품 전송 개수
                    //arSM.get(i).setWH_AREA("A-01");
                }

                if (Common.D) {
                    Log.d(TAG, "result's Count : " + arSM.size());
                }

                // 롯데의 경우만 lotte_TryCount 사용, 초기화 후 현재 찍힌 수량 더해서 전역변수로 만들기.
                if(Common.searchType.equals(SEARCH_TYPE_LOTTE)) {
                    //lotte_TryCount = 1;

                    Shipments_Info si = arSM.get(0);
                    lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;
                    if (lotte_TryCount > LOTTE_BOX_ORDER_MAX) {
                        lotte_TryCount = 1;
                    }
                    Log.e(TAG, "***************************LAST_BOX_ORDER : " +si.getLAST_BOX_ORDER());
                    for (int i = 0; i < arSM.size(); i++) {
                        lotte_TryCount += arSM.get(i).getPACKING_QTY();
                    }
                    if (lotte_TryCount > LOTTE_BOX_ORDER_MAX) {
                        lotte_TryCount = lotte_TryCount % LOTTE_BOX_ORDER_MAX; //찍힌 수량까지 더했을 때 9999 넘는 경우 1번대로 다시 회귀한 넘버링 적용 (9999로 나눈 나머지)
                    }
                    Log.d(TAG, "======================== lotte_TryCount ========================="+ lotte_TryCount);
                }

            } catch (Exception e) {
                if (Common.D) {
                    Log.e(TAG, "e : " + e.toString());
                }
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress[0].equals("progress")) {
                pDialog.setProgress(Integer.parseInt(progress[1]));
                pDialog.setMessage(progress[2]);
            } else if (progress[0].equals("max")) {
                pDialog.setMax(Integer.parseInt(progress[1]));
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            pDialog.dismiss();
            sListAdapter = new ShipmentListAdapter(mContext, R.layout.list_shipment, arSM, mHandler);
            sList.setAdapter(sListAdapter);
            sListAdapter.notifyDataSetChanged();
            try {
                if (arSM.size() > 0) {       // 목록 존재

                    Log.e(TAG, "========================on post test======================");

                    if (!this.type)
                        work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type);
                    // find_work_info에서 처리

                    ArrayList<String> list_position = new ArrayList<String>();
                    for (int i = 0; i < arSM.size(); i++) {
                        /*list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getPACKER_PRODUCT_CODE() + " / " + arSM.get(i).getITEM_NAME());*/
                        list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO());
                    }

                    ArrayAdapter<String> position_adapter = new ArrayAdapter<String>(BixolonShipmentActivity.this, android.R.layout.simple_spinner_item, list_position);
                    position_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_point_name.setAdapter(position_adapter);
                    select_flag = true;
                    ArrayList<String> list_bl = new ArrayList<String>();

                    for (int i = 0; i < arSM.size(); i++) {
                        centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG());      // 센터 총 계근요청수량
                        centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY());   // 센터 총 계근요청중량

                        centerWorkCount += arSM.get(i).getPACKING_QTY();                        // 센터 총 계근수량
                        centerWorkWeight += arSM.get(i).getGI_QTY();                            // 센터 총 계근중량
                    }

                    edit_center_tcount.setText(centerTotalCount + " / " + centerWorkCount);
                    edit_center_tweight.setText(Math.round(centerTotalWeight * 100) / 100.0 + " / " + centerWorkWeight);

                    position_adapter.notifyDataSetChanged();

                    for (int i = 0; i < arSM.size(); i++) {
                        if (!arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
                            sp_point_name.setSelection(i);
                            break;
                        }
                    }

                    set_scanFlag(false);
                    if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {       // 총 계근 완료
                        show_wetFinishDialog();
                    }

                } else {            // 결과 없음
                    vibrator.vibrate(1000);
                    Log.e(TAG, "###############################################");
                    Log.e(TAG, "######### 출하대상 리스트 조회결과 없음 ###########");
                    Log.e(TAG, "###############################################");

                    if(work_item_barcodegoods == "" ) {      //TB_BARCODE_INFO의 BarcodeGoods데이터가 없을 경우
                        Log.d(TAG, "test");
                        Log.d(TAG, "alert_flag3: " + alert_flag);
                        showAlertDialog("barcode", 0);
                        alert_flag = true;
                        edit_product_code.setText("");
                    }

                    Toast.makeText(getApplicationContext(), "조회결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    if (work_flag == 1) {
                        set_scanFlag(true);
                    } else if (work_flag == 0){
                        set_scanFlag(false);
                    } else if (work_flag == 2){
                        set_scanFlag(true);
                    }
                    current_work_position = -1;
                    //edit_product_name.setText("");
                    //edit_product_code.setText("");
                    sp_point_name.setAdapter(null);
                    sp_bl_no.setAdapter(null);

                    centerTotalCount = 0;
                    centerTotalWeight = 0.0;
                    edit_center_tcount.setText("0 / 0");
                    edit_center_tweight.setText("0 / 0");
                    edit_wet_count.setText("0 / 0");
                    edit_wet_weight.setText("0 / 0");
                    edit_product_name.setText("");
                    edit_product_code.setText("");
                    //work_item_fullbarcode = "";
                    //work_ppcode = "";
                }
            } catch (Exception ex) {
                Log.e(TAG, "======== ProgressDlgShipSelect onPostExecute Exception ========");
                Log.e(TAG, ex.toString());
            }
        }
    }

    /**
     * BL번호 기반 출하 대상 조회 AsyncTask
     * <p>
     * BL번호로 출하 대상 목록을 조회한다.
     * ProgressDlgShipSelect와 유사하지만 BL번호 전용 조회에 사용.
     * </p>
     *
     * @see ProgressDlgShipSelect
     */
    class ProgressDlgShipSelectBL extends AsyncTask<Integer, String, Integer> {
        private Context mContext;
        private String center_name;
        private String bl_no;

        public ProgressDlgShipSelectBL(Context context, String center_name, String bl_no) {
            mContext = context;
            this.center_name = center_name;
            this.bl_no = bl_no;
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(mContext);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setTitle("출하대상 불러오는중...");
            pDialog.setMessage("잠시만 기다려 주세요..");
            pDialog.setCancelable(false);
            pDialog.show();

            current_work_position = -1;
            centerTotalCount = 0;
            centerTotalWeight = 0.0;
            centerWorkCount = 0;
            centerWorkWeight = 0.0;
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                arSM = DBHandler.selectqueryShipment(mContext, this.center_name, this.bl_no, false);

                for (int i = 0; i < arSM.size(); i++) {
                    String[] row = new String[2];
                    row = DBHandler.selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getPACKER_PRODUCT_CODE(), arSM.get(i).getCLIENT_CODE(), arSM.get(i).getGI_L_ID());
                    arSM.get(i).setGI_QTY(Double.parseDouble(row[0]));      // 중량
                    arSM.get(i).setPACKING_QTY(Integer.parseInt(row[1]));   // 수량
                    arSM.get(i).setSAVE_CNT(Integer.parseInt(row[2]));      // 계근 상품 전송 개수
                }

                if (Common.D) {
                    Log.d(TAG, "result's Count : " + arSM.size());
                }
            } catch (Exception e) {
                if (Common.D) {
                    Log.e(TAG, "e : " + e.toString());
                }
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress[0].equals("progress")) {
                pDialog.setProgress(Integer.parseInt(progress[1]));
                pDialog.setMessage(progress[2]);
            } else if (progress[0].equals("max")) {
                pDialog.setMax(Integer.parseInt(progress[1]));
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            pDialog.dismiss();
            sListAdapter = new ShipmentListAdapter(mContext, R.layout.list_shipment, arSM, mHandler);
            sList.setAdapter(sListAdapter);
            sListAdapter.notifyDataSetChanged();

            if (arSM.size() > 0) {       // 목록 존재
                edit_product_name.setText(arSM.get(0).getITEM_NAME().toString());
                edit_product_code.setText(arSM.get(0).getPACKER_PRODUCT_CODE().toString());
                ArrayList<String> list_position = new ArrayList<String>();
                for (int i = 0; i < arSM.size(); i++) {
                    list_position.add(arSM.get(i).getCLIENTNAME());
                }

                ArrayAdapter<String> position_adapter = new ArrayAdapter<String>(BixolonShipmentActivity.this, android.R.layout.simple_spinner_item, list_position);
                position_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_point_name.setAdapter(position_adapter);
                select_flag = true;
                ArrayList<String> list_bl = new ArrayList<String>();

                for (int i = 0; i < arSM.size(); i++) {
                    centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG());      // 센터 총 계근요청수량
                    centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY());   // 센터 총 계근요청중량

                    centerWorkCount += arSM.get(i).getPACKING_QTY();                        // 센터 총 계근수량
                    centerWorkWeight += arSM.get(i).getGI_QTY();                            // 센터 총 계근중량

                    int iCount = 0;
                    for (int j = 0; j < list_bl.size(); j++) {
                        if (list_bl.get(j).toString().equals(arSM.get(i).getBL_NO())) {
                            iCount++;
                            break;
                        }
                    }
                    if (iCount == 0) {
                        list_bl.add(arSM.get(i).getBL_NO());
                    }
                }
                edit_center_tcount.setText(centerTotalCount + " / " + centerWorkCount);
                edit_center_tweight.setText(Math.round(centerTotalWeight * 100) / 100.0 + " / " + centerWorkWeight);

                ArrayAdapter<String> bl_adapter = new ArrayAdapter<String>(BixolonShipmentActivity.this, android.R.layout.simple_spinner_item, list_bl);
                bl_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_bl_no.setAdapter(bl_adapter);

                if (current_work_position == -1) {
                    show_sendFinishDialog();
                    return;
                } else {
                    sp_point_name.setSelection(current_work_position);
                    for (int i = 0; i < sp_bl_no.getCount(); i++) {
                        if (sp_bl_no.getItemAtPosition(i).toString().equals(arSM.get(current_work_position).getBL_NO())) {
                            sp_bl_no.setSelection(i);
                            break;
                        }
                    }
                }

                position_adapter.notifyDataSetChanged();
                bl_adapter.notifyDataSetChanged();

                if (!work_item_fullbarcode.equals("")) {
                    boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(), work_item_fullbarcode,
                            arSM.get(current_work_position).getGI_D_ID(), arSM.get(current_work_position).getPACKER_PRODUCT_CODE(), arSM.get(current_work_position).getGI_L_ID());

                    if (dup) {
                        Log.e(TAG, "=====================오류지점3=========================");
                        Toast.makeText(getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(1000);
                        //scanFlag_swap();
                        set_scanFlag(true);
                    }
                }

                sList.setSelection(current_work_position);      // 현재 계근지점으로 위치 변경
                for (int i = 0; i < arSM.size(); i++) {
                    arSM.get(i).setWORK_FLAG(0);
                }

                arSM.get(current_work_position).setWORK_FLAG(1);
                sListAdapter.notifyDataSetChanged();
                if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {       // 총 계근 완료
                    show_wetFinishDialog();
                } else if (arSM.get(current_work_position).getGI_REQ_PKG().equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY()))) {
                    //scanFlag_swap();
                    //show_wetNextDialog();
                }
            } else {            // 결과 없음
                vibrator.vibrate(1000);
                Log.e(TAG, "###############################################");
                Log.e(TAG, "######### 출하대상 리스트 조회결과 없음 ###########");
                Log.e(TAG, "###############################################");
                Toast.makeText(getApplicationContext(), "조회결과가 없습니다.", Toast.LENGTH_SHORT).show();
                //scanFlag_swap();
                set_scanFlag(true);
                edit_product_name.setText("");
                edit_product_code.setText("");

                centerTotalCount = 0;
                centerTotalWeight = 0.0;
                edit_center_tcount.setText("0 / 0");
                edit_center_tweight.setText("0 / 0");
                edit_wet_count.setText("0 / 0");
                edit_wet_weight.setText("0 / 0");
                work_item_fullbarcode = "";
                //work_ppcode = "";
            }
        }
    }

    /** 전송할 계근 데이터 목록 */
    private ArrayList<Goodswets_Info> list_send_info;

    /**
     * 계근 데이터 서버 전송 AsyncTask
     * <p>
     * 로컬 DB에 저장된 계근 데이터를 서버(G3)로 전송한다.
     * 출하 유형별로 다른 JSP URL을 호출한다.
     * </p>
     *
     * <h3>전송 URL (Common.searchType 기준)</h3>
     * <ul>
     *   <li>이마트(0), 도매(3): insert_goods_wet.jsp (inno 스키마)</li>
     *   <li>생산(1), 생산출력(7): insert_goods_wet.jsp (inno 스키마)</li>
     *   <li>홈플러스(2), 롯데(6): insert_goods_wet_homeplus.jsp (inno 스키마)</li>
     *   <li>홈플러스 비정량(4,5): insert_goods_wet_homeplus.jsp</li>
     * </ul>
     *
     * <h3>전송 패킷 구조 (:: 구분자)</h3>
     * <pre>
     * GI_D_ID::WEIGHT::WEIGHT_UNIT::PACKER_PRODUCT_CODE::BARCODE::
     * PACKER_CLIENT_CODE::MAKINGDATE::BOXSERIAL::BOX_CNT::REG_ID::
     * ITEM_CODE::BRAND_CODE::CLIENT_TYPE::BOX_ORDER
     * </pre>
     *
     * @see HttpHelper#sendDataDb(String, String, String, String)
     */
    class ProgressDlgShipmentSend extends AsyncTask<Void, String, String> {
        private Context mContext;
        String receiveData = "";

        public ProgressDlgShipmentSend(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setTitle("전송 중 입니다.");
            pDialog.setMessage("잠시만 기다려 주세요..");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            String qry_where = "";
            try {
                for (int i = 0; i < arSM.size(); i++) {
                    if (i == (arSM.size() - 1)) {
                        qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID();
                    } else {
                        qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID() + " OR ";
                    }
                }
                Log.v(TAG, "전송상품 검색 where : " + qry_where);

                list_send_info = DBHandler.selectquerySendGoodsWet(mContext, qry_where);        // 목록 조회 성공
                publishProgress("max", Integer.toString(list_send_info.size()));

                int iCount = 0;
                int jChk = 0;

                if(Common.searchType.equals(SEARCH_TYPE_EMART) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS) || Common.searchType.equals(SEARCH_TYPE_LOTTE)){ //이마트 혹은 홈플러스, 롯데 출고일때..구로직
                    for (int i = 0; i < list_send_info.size(); i++) { //SAVE_TYPE 과 상관 없이 계근 데이터 모두 루프
                        if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
                            iCount++;
                            String packet = "";
                            packet += list_send_info.get(i).getGI_D_ID() + "::";
                            packet += list_send_info.get(i).getWEIGHT() + "::";
                            packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";
                            packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";
                            packet += list_send_info.get(i).getBARCODE() + "::";
                            packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::";
                            packet += list_send_info.get(i).getMAKINGDATE() + "::";
                            packet += list_send_info.get(i).getBOXSERIAL() + "::";
                            packet += list_send_info.get(i).getBOX_CNT() + "::";
                            packet += list_send_info.get(i).getREG_ID() + "::";
                            packet += Common.selectCompanyCode + "::";
                            packet += list_send_info.get(i).getBRAND_CODE() + "::";
                            packet += list_send_info.get(i).getCLIENT_TYPE() + "::";
                            packet += list_send_info.get(i).getBOX_ORDER() + "::";
                            packet += list_send_info.get(i).getGI_L_ID();

                            if (Common.D) {
                                Log.d(TAG, "Send Packet : '" + packet + "'");
                            }

                            Log.i(TAG, "=====================Common.searchType==================" + Common.searchType);

                            // 디비접속 설정
                            if(Common.searchType.equals(SEARCH_TYPE_EMART)||Common.searchType.equals(SEARCH_TYPE_WHOLESALE)) {
                                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
                            }else if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS)||Common.searchType.equals(SEARCH_TYPE_LOTTE)){   // 홈플러스, 롯데 같이 태우기
                                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS);
                            }else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION)){
                                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
                            }else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)){
                                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
                            }

                            //결과값의 앞, 뒤에 공백 제거
                            result = result.replace("\r\n", "");
                            result = result.replace("\n", "");
                            Log.d(TAG, "i number : " + i);
                            Log.v(TAG, "전송결과 : " + result);
                            //s : 성공, f : 실패
                            if (result.equals("s")) {
                                boolean bool = DBHandler.updatequeryGoodsWet(mContext, list_send_info.get(i).getGI_D_ID(), list_send_info.get(i).getBARCODE(), list_send_info.get(i).getBOX_CNT(), list_send_info.get(i).getGI_L_ID());
                                Log.d(TAG, "boolean " + bool);
                                if (bool) {          // 전송 & PDA SQLite update 성공
                                    publishProgress("progress", Integer.toString(iCount), Integer.toString(iCount) + "번 데이터 전송성공..");

                                    for (int j = 0; j < arSM.size(); j++) {
                                        if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())
                                                && arSM.get(j).getGI_L_ID().equals(list_send_info.get(i).getGI_L_ID())) {
                                            arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1);

                                            if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {            // 전송 개수와 요청 개수 비교
                                                    Log.v(TAG, "출하대상 계근 완료");
                                                    arSM.get(j).setSAVE_TYPE("Y");          // 전부 전송했다면 전송여부 Y로 변경
                                                    DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE(), arSM.get(j).getGI_L_ID());

                                                    jChk++;

                                                    if (jChk == arSM.size()) {
                                                        Log.d(TAG, "arSM.size() when return: " + arSM.size());
                                                        Log.d(TAG, "jChk number when return: " + jChk);
                                                        return "ss";
                                                    }
                                            }
                                        }
                                    }
                                }
                            } else if (result.equals("f")) {
                                return result;
                            }
                        }
                    }
                }else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION) || Common.searchType.equals(SEARCH_TYPE_WHOLESALE) || Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)){//생산계근 혹은 도매계근일때
                    Log.i(TAG, "=====================여기 들어오는지 확인==================");
                    Log.i(TAG, "=====================사이즈 확인=================="+list_send_info.size());
                    String packet = "";
                    //전문 전송용 for문
                    for (int i = 0; i < list_send_info.size(); i++) {
                        if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
                            iCount++;
                            packet += list_send_info.get(i).getGI_D_ID() + "::";
                            packet += list_send_info.get(i).getWEIGHT() + "::";
                            packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";
                            packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";
                            packet += list_send_info.get(i).getBARCODE() + "::";
                            packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::";
                            packet += list_send_info.get(i).getMAKINGDATE() + "::";
                            packet += list_send_info.get(i).getBOXSERIAL() + "::";
                            packet += list_send_info.get(i).getBOX_CNT() + "::";
                            packet += list_send_info.get(i).getREG_ID() + "::";
                            packet += Common.selectCompanyCode + "::";
                            packet += list_send_info.get(i).getBRAND_CODE() + "::";
                            packet += list_send_info.get(i).getCLIENT_TYPE() + "::";
                            packet += list_send_info.get(i).getBOX_ORDER() + "::" + list_send_info.get(i).getGI_L_ID() +"##";

                            if (Common.D) {
                                Log.d(TAG, "Send Packet : '" + packet + "'");
                            }

                            Log.i(TAG, "=====================여기 들어오는지 확인==================");

                            Log.i(TAG, "=====================Common.searchType==================" + Common.searchType);
                        }// "F이면" 끝
                    }//for문 끝
                    //새 로직 여기다가 넣어야 될 듯
                    Log.i(TAG, "===================send packet 확인==================" + packet);
                    boolean sendOrNot = true;

                    if(packet ==""){
                        sendOrNot = false;
                    }

                    if(sendOrNot){
                        //전문전송..
                        if(Common.searchType.equals(SEARCH_TYPE_EMART)) {   // 출하대상 리스트, 스토어 코드 넣은 이유는 앱을 종료로 안 닫고 앱정리로 닫은 후 생산리스트를 다운받지 않은 상태에서 계근입력후 전송하면 하이랜드 스키마로 데이터가 입력될 수 있음
                            result = HttpHelper.getInstance().sendData(packet, "goodswet_insert", Common.URL_INSERT_GOODS_WET);
                        }else if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS)){
                            result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS);
                        }else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION) || Common.searchType.equals(SEARCH_TYPE_NONFIXED)|| Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)|| Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)){
                            Log.i(TAG, "===================send packet 확인==================" + packet);
                            result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_NEW);
                        }else if(Common.searchType.equals(SEARCH_TYPE_WHOLESALE)){
                            Log.i(TAG, "==================여기로 들어옴==================");
                            Log.i(TAG, "===================send packet 확인==================" + packet);
                            result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_NEW);
                        }
                    }else{
                        result = "af"; //already finish
                    }

                    //결과값의 앞, 뒤에 공백 제거
                    result = result.replace("\r\n", "");
                    result = result.replace("\n", "");
                    //Log.d(TAG, "i number : " + i);
                    Log.v(TAG, "전송결과 : " + result); // s : success

                    //s : 성공, f : 실패
                    //list_send_info : PDA 계근데이터
                    //arSM : 출하대상
                    for (int i = 0; i < list_send_info.size(); i++) { //계근데이터 루프돌면서
                        if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
                            if (result.equals("s")) {
                                boolean bool = DBHandler.updatequeryGoodsWet(mContext, list_send_info.get(i).getGI_D_ID(), list_send_info.get(i).getBARCODE(), list_send_info.get(i).getBOX_CNT(), list_send_info.get(i).getGI_L_ID()); //PDA 계근테이블 SAVE TYPE Y로 업데이트
                                Log.d(TAG, "boolean " + bool);
                                if (bool) {          // 전송 & PDA SQLite update 성공
                                    publishProgress("progress", Integer.toString(iCount), Integer.toString(iCount) + "번 데이터 전송성공..");
                                    for (int j = 0; j < arSM.size(); j++) { //출하대상루프(GI_D_ID별 1 ROW)
                                        if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())
                                                && arSM.get(j).getGI_L_ID().equals(list_send_info.get(i).getGI_L_ID())) { //출하대상 루프의 GI_D_ID와 계근데이터의 GI_D_ID가 같으면
                                            arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1); //출하대상 데이터에 SAVE_CNT(저장갯수) 데이터 저장

                                            if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {  // 전송 개수와 출하요청 개수가 같으면
                                                    Log.v(TAG, "출하대상 계근 완료");
                                                    arSM.get(j).setSAVE_TYPE("Y");          // 전부 전송했다면 전송여부 Y로 변경
                                                    DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE(), arSM.get(j).getGI_L_ID());

                                                    jChk++;

                                                    if (jChk == arSM.size()) {
                                                        Log.d(TAG, "arSM.size() when return: " + arSM.size());
                                                        Log.d(TAG, "jChk number when return: " + jChk);
                                                        return "ss";
                                                    }
                                            }
                                        }
                                    }
                                }
                            } else if (result.equals("f")) {
                                return result;
                            }//result "s이면" 끝
                        }
                    }
                }
                return result;
            } catch (Exception ex) {
                Log.e(TAG, "======== ProgressDlgShipmentSend doInBackgounrd Exception ========");
                Log.e(TAG, ex.toString());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String _result) {
            Log.v(TAG, "전송결과 : " + _result);
            pDialog.dismiss();
            if (_result.equals("s")) {
                Toast.makeText(getApplicationContext(), "결과 s, 전송 성공.", Toast.LENGTH_SHORT).show();
                sListAdapter.notifyDataSetChanged();
            } else if (_result.equals("ss")) {
                Toast.makeText(getApplicationContext(), "결과 ss, 전송성공.", Toast.LENGTH_SHORT).show();
                sListAdapter.notifyDataSetChanged();
                show_sendFinishDialog();
            } else if (_result.equals("update_fail")) {
                Toast.makeText(getApplicationContext(), "출하대상 완료 작업실패", Toast.LENGTH_SHORT).show();
                vibrator.vibrate(500);
            }else if(_result.equals("f")){ //전송간 에러
                Toast.makeText(getApplicationContext(), "네트워크 에러로 인한 전송간 오류 발생, 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                vibrator.vibrate(500);
            }else if(_result.equals("af")){
                Toast.makeText(getApplicationContext(), "이미 모두 전송되었거나 전송할 건이 없습니다.", Toast.LENGTH_SHORT).show();
                vibrator.vibrate(500);
            }
            super.onPostExecute(_result);
        }
    }

    /**
     * 블루투스 프린터 연결 AsyncTask
     * <p>
     * Woosim 블루투스 프린터에 연결한다.
     * 저장된 프린터 주소(Common.printer_address)로 자동 연결을 시도한다.
     * </p>
     *
     * <h3>처리 흐름</h3>
     * <ol>
     *   <li>onPreExecute: 연결 로딩 다이얼로그 표시</li>
     *   <li>doInBackground: BluetoothAdapter로 디바이스 연결</li>
     *   <li>연결 성공 시 mHandler로 MESSAGE_DEVICE_NAME 메시지 전달</li>
     * </ol>
     */
    class ProgressDlgPrintConnect extends AsyncTask<Integer, String, Integer> {
        private Context mContext;

        public ProgressDlgPrintConnect(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            cDialog = new ProgressDialog(mContext);
            cDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            cDialog.setTitle("프린터 연결중...");
            cDialog.setMessage("잠시만 기다려 주세요..");
            cDialog.setCancelable(false);
            cDialog.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                //선택된 장비 연결 시도
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(Common.printer_address);
                if (Common.D) {
                    Log.e(TAG, "print_address :: " + Common.printer_address);
                }
                mBixolonPrinter.connect(device);
            } catch (Exception e) {
                if (Common.D) {
                    Log.e(TAG, "e : " + e.toString());
                }
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
        }
    }

    //	모바일프린터 장비 검색창의 결과를 받는 곳
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Common.D) {
            Log.d(TAG, "onActivityResult " + resultCode);
        }
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {                // 장비 선택 시
                    if (data != null) {
                        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        if (Common.D) {
                            Log.d(TAG, "address = " + address);
                        }

                        // 선택된 장비의 Address 저장
                        Common.printer_address = address;
                        try {
                            if (!"".equals(Common.printer_address)) {
                                // 모바일프린터 정보 Sharedpreferences에 저장
                                SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = spfBluetooth.edit();
                                editor.putString("printer_address", Common.printer_address);
                                editor.putInt("printer_no", 10);                //	30 = PM30
                                editor.commit();
                                new ProgressDlgPrintConnect(BixolonShipmentActivity.this).execute();        // 선택된 모바일프린터 연결 시도
                            }
                        } catch (Exception e) {
                            if (Common.D) {
                                Log.e(TAG, "e : " + e.toString());
                            }
                        }
                    } else {
                        finish();
                    }
                } else {
                    //모바일프린터 미선택 시 프린터 사용 OFF로 설정
                    if (Common.D) {
                        Log.d(TAG, "Print not Use");
                    }
                    Common.printer_address = "";
                    Common.printer_setting = false;
                    Common.print_bool = false;
                    SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = spfBluetooth.edit();
                    editor.putString("printer_address", Common.printer_address);
                    editor.putBoolean("printer_setting", Common.printer_setting);
                    editor.commit();
                    swt_print.setChecked(false);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                }
                break;
            case REQUEST_ENABLE_BT:
                // 장비의 블루투스 사용 가능여부 확인
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a print
                    // 장비 블루투스 사용 가능
                    Message msg = new Message();
                    msg.what = MESSAGE_SEARCH;
                    mHandler.sendMessage(msg);
                } else {
                    // 장비 블루투스 사용 불가능
                    if (Common.D) {
                        Log.d(TAG, "BT not enabled");
                    }
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
            case GET_DATA_REQUEST:
                //Log.d(TAG, "BT not enabled");
                if (resultCode == RESULT_OK) {
                    //weight 그대로 받아온 거..
                    String weight_str = data.getStringExtra("enteredWeightSend");
                    Double weight_double = data.getDoubleExtra("enteredWeightDblSend",0);
                    String making_date = data.getStringExtra("enteredMakingDateSend");
                    //Double weight_double1 = 0.0;

                    Log.d(TAG, "입력 데이터 확인... 1 : " + weight_str);
                    Log.d(TAG, "입력 데이터 확인... 2 : " + weight_double);
                    Log.d(TAG, "입력 데이터 확인... 3 : " + making_date);

                    //팝업에서 넘긴 데이터를 기준으로 insert 시작
                    wet_data_insert(weight_str, weight_double, making_date, "");
                }
        }
    }

    /**
     * 블루투스 프린터 연결 해제 AsyncTask
     * <p>
     * Activity 종료 시 프린터 연결을 해제한다.
     * onDestroy()에서 호출된다.
     * </p>
     */
    class ProgressDlgDiscon extends AsyncTask<Void, Void, Void> {
        private Context mContextDiscon;

        public ProgressDlgDiscon(Context context) {
            mContextDiscon = context;
        }

        @Override
        protected void onPreExecute() {
            cDialog = new ProgressDialog(mContextDiscon);
            cDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            cDialog.setTitle("프린터를 해제중 입니다.");
            cDialog.setMessage("잠시만 기다려 주세요..");
            cDialog.setCancelable(false);
            cDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (mBixolonPrinter != null) mBixolonPrinter.disconnect();
                mBixolonPrinter = null;
            } catch (Exception e) {
                if (Common.D) {
                    Log.e(TAG, "e : " + e.toString());
                }
            }
            return null;
        }
    }

    /*
        계근 상세내역 팝업 필드
     */
    private View detail_layout;
    private AlertDialog detail_dialog;
    private EditText detail_edit_position_name;
    private EditText detail_edit_ppname;
    private EditText detail_edit_ppcode;
    private EditText detail_edit_count;
    private EditText detail_edit_weight;
    private DetailAdapter detailAdapter;
    private ListView detail_list;
    private Button detail_btn_back;
    private Button detail_btn_delete;
    private Button detail_btn_sum;
    private ArrayList<Goodswets_Info> list_gi_info;

    private void show_wetDetailDialog(Shipments_Info si, Barcodes_Info bi, int position) {
        try {
            dialog_flag = true;
            detail_layout = Inflater.inflate(R.layout.dialog_detailshipment, null);

            detail_edit_position_name = (EditText) detail_layout.findViewById(R.id.detail_edit_position);
            detail_edit_ppname = (EditText) detail_layout.findViewById(R.id.detail_edit_ppname);
            detail_edit_ppcode = (EditText) detail_layout.findViewById(R.id.detail_edit_ppcode);
            detail_edit_count = (EditText) detail_layout.findViewById(R.id.detail_edit_count);
            detail_edit_weight = (EditText) detail_layout.findViewById(R.id.detail_edit_weight);

            detail_btn_back = (Button) detail_layout.findViewById(R.id.detail_btn_back);
            detail_btn_delete = (Button) detail_layout.findViewById(R.id.detail_btn_select);
            detail_btn_sum = (Button) detail_layout.findViewById(R.id.detail_btn_sum);

            detail_edit_position_name.setText(si.getCLIENTNAME());
            detail_edit_ppname.setText(si.getITEM_NAME());
            detail_edit_ppcode.setText(si.getPACKER_PRODUCT_CODE());
            detail_edit_count.setText(si.getGI_REQ_PKG() + " / " + si.getPACKING_QTY());
            detail_edit_weight.setText(si.getGI_REQ_QTY() + " / " + si.getGI_QTY());

            final AlertDialog.Builder dlog = new AlertDialog.Builder(this, R.style.AppCompatDialogStyle)
                    .setCancelable(false);
            dlog.setView(detail_layout);
            detail_dialog = dlog.create();
            detail_dialog.show();

            detail_btn_back.setOnClickListener(v -> {
                detail_dialog.dismiss();
                dialog_flag = false;
                for (int i = 0; i < sListAdapter.cbStatus.size(); i++) {
                    sListAdapter.cbStatus.set(i, false);
                }
                edit_barcode.setText("");
                edit_wet_count.setText("");
                edit_wet_weight.setText("");
                if (work_flag == 1) {
                    new ProgressDlgShipSelect(BixolonShipmentActivity.this, sp_center_name.getSelectedItem().toString(), work_ppcode, true).execute();
                    //setBarcodeMsg(msg);
                } else if (work_flag == 0){
                    //Toast.makeText(getApplicationContext(), "수기로 중량을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    //vibrator.vibrate(500);
                    Log.e(TAG, "수기일때 뒤로가기 = " + work_bl_no);
                    // BL코드로 계근 리스트 조회하기
                    new ProgressDlgShipSelect(BixolonShipmentActivity.this, sp_center_name.getSelectedItem().toString(), work_bl_no, false).execute();
                } else if (work_flag == 2){
                    new ProgressDlgShipSelect(BixolonShipmentActivity.this, sp_center_name.getSelectedItem().toString(), work_bl_no, false).execute();
                }
            });

            detail_btn_delete.setOnClickListener(v -> {
                // 선택된 Items 삭제
                try {
                    if (list_gi_info.size() == 0) {
                        Toast.makeText(getApplicationContext(), "삭제할 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(1000);
                    } else if (list_gi_info.size() > 0) {
                        ArrayList<Goodswets_Info> list_delete = new ArrayList<Goodswets_Info>();
                        for (int i = 0; i < detailAdapter.cbStatus.size(); i++) {
                            if (detailAdapter.cbStatus.get(i))
                                list_delete.add(list_gi_info.get(i));
                        }
                        if (list_delete.size() > 0) {
                            deleteQuestionDialog(getSelect_Shipment(getSelect_Position()), list_delete);
                        } else {
                            Toast.makeText(getApplicationContext(), "삭제할 항목을 선택하세요.", Toast.LENGTH_SHORT).show();
                            vibrator.vibrate(1000);
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "==== detail_btn_delete Exception ====");
                    Log.e(TAG, ex.getMessage().toString());
                }
            });

            detail_btn_sum.setOnClickListener(v -> {
                // 전체 리스트 합
                try {
                    if (list_gi_info.size() == 0) {
                        Toast.makeText(getApplicationContext(), "합산할 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(1000);
                    } else if (list_gi_info.size() > 0) {
                        // ========== SLCS 명령어로 합계 라벨 인쇄 (Bixolon 프린터) ==========
                        // 원본: Woosim ByteArrayOutputStream + WoosimCmd 명령어
                        // 변환: StringBuilder + SLCS 헬퍼 메서드
                        // 용도: 계근 내역 합계 버튼 클릭 시 중량 합산 라벨 인쇄
                        // 출력 항목:
                        //   [1] 개별 중량 - 동적 좌표(p_weight, p_hight) 40x40
                        //   [2] 페이지별 총 중량 - (100, 350) 60x60
                        // 주의: 36개 항목 단위로 라벨 1장 인쇄 (6열 x 6행)
                        try {
                            StringBuilder slcsCmd = new StringBuilder();

                            // 초기화: CB(버퍼클리어) + CS13,0(한글문자셋)
                            // 원본: WoosimCmd.initPrinter() + setPageMode() + selectTTF() + setTextStyle()
                            slcsCmd.append(slcsInit());

                            // 라벨 크기 설정: 576x460 도트
                            // 원본: WoosimCmd.PM_setArea(0, 0, 576, 460)
                            slcsCmd.append(slcsLabelSize(576, 460));

                            double weight_sum = 0;
                            int p_weight = 0;
                            int p_hight = 0;

                            for (int i = 0; i < list_gi_info.size(); i++) {
                                // 동적 좌표 계산 (6열 x 6행 = 36개 단위)
                                // p_hight: 행 위치 (0~5행 반복, 36개마다 리셋)
                                // p_weight: 열 위치 (0~5열 반복)
                                p_hight = 10 + (i / 6 * 50) - (i / 36 * 300);
                                p_weight = 100 * (i % 6);

                                // [1] 개별 중량 출력 (동적 좌표, 폰트크기 40x40)
                                // 원본: PM_setPosition(p_weight, p_hight) + getTTFcode(40, 40, weight)
                                slcsCmd.append(slcsText(p_weight, p_hight, 40, 40, list_gi_info.get(i).getWEIGHT()));

                                weight_sum += Double.parseDouble(list_gi_info.get(i).getWEIGHT());

                                // 36개 단위 완료 시 라벨 인쇄
                                if ((i + 1) % 36 == 0) {
                                    weight_sum = Math.floor(weight_sum * 100);
                                    weight_sum = weight_sum / 100.0;

                                    String temp_weight = String.format("%.1f", weight_sum);
                                    weight_sum = Double.parseDouble(temp_weight);

                                    // [2] 페이지별 총 중량 (x=100, y=350, 폰트크기 60x60)
                                    // 원본: PM_setPosition(100, 350) + getTTFcode(60, 60, "N번 총 중량 : XX.X")
                                    slcsCmd.append(slcsText(100, 350, 60, 60, ((i + 1) / 36) + "번 총 중량 : " + Double.toString(weight_sum)));

                                    // 인쇄 실행 + 라벨 피드
                                    // 원본: PM_printData() + feedToMark()
                                    slcsCmd.append(slcsPrint(1));
                                    slcsCmd.append(slcsFeedToMark());

                                    // 전송
                                    sendData(slcsCmd.toString().getBytes("EUC-KR"));

                                    // StringBuilder 초기화 (원본: byteStream.reset())
                                    slcsCmd.setLength(0);
                                    slcsCmd.append(slcsInit());
                                    slcsCmd.append(slcsLabelSize(576, 460));
                                    weight_sum = 0;

                                } else if ((i + 1) == list_gi_info.size()) {
                                    // 마지막 항목 처리 (36개 미만인 경우)
                                    weight_sum = Math.floor(weight_sum * 100);
                                    weight_sum = weight_sum / 100.0;

                                    String temp_weight = String.format("%.1f", weight_sum);
                                    weight_sum = Double.parseDouble(temp_weight);

                                    // [2] 페이지별 총 중량 (마지막 페이지)
                                    // 원본: PM_setPosition(100, 350) + getTTFcode(60, 60, "N번 총 중량 : XX.X")
                                    slcsCmd.append(slcsText(100, 350, 60, 60, (((i + 1) / 36) + 1) + "번 총 중량 : " + Double.toString(weight_sum)));

                                    // 인쇄 실행 + 라벨 피드
                                    // 원본: PM_printData() + feedToMark()
                                    slcsCmd.append(slcsPrint(1));
                                    slcsCmd.append(slcsFeedToMark());

                                    // 전송
                                    sendData(slcsCmd.toString().getBytes("EUC-KR"));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (Common.D) {
                                Log.d(TAG, "setPrinting Exception\n" + e.getMessage().toString());
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "==== detail_btn_sum Exception ====");
                    Log.e(TAG, ex.getMessage().toString());
                }
            });

            list_gi_info = DBHandler.selectqueryGoodsWet(BixolonShipmentActivity.this, si.getGI_D_ID(), si.getPACKER_PRODUCT_CODE(), si.getCLIENT_CODE(), si.getGI_L_ID());
            detailAdapter = new DetailAdapter(BixolonShipmentActivity.this, R.layout.list_detailshipment, list_gi_info, mHandler);

            detail_list = (ListView) detail_layout.findViewById(R.id.detail_list);
            detail_list.setAdapter(detailAdapter);
            detailAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            Log.e(TAG, "======== show_wetDetailDialog Exception ========");
            Log.e(TAG, ex.getMessage().toString());
        }
    }

    public Shipments_Info getSelect_Shipment(int pos) {
        return arSM.get(pos);
    }

    //	계근상품 삭제 Dialog
    public void deleteQuestionDialog(final Shipments_Info si, final ArrayList<Goodswets_Info> list_delete) {
        String alertTitle = "계근상품 삭제";
        String buttonMessage = "정말 삭제하시겠습니까?";
        String buttonYes = "삭제";
        String buttonNo = "취소";

        new AlertDialog.Builder(BixolonShipmentActivity.this, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(alertTitle)
                .setMessage(buttonMessage)
                .setCancelable(false)
                .setPositiveButton(buttonYes, (dialog, which) -> {
                    try {
                        if (Common.D) {
                            Log.d(TAG, "삭제할 계근상품 수 : " + list_delete.size());
                        }
                        for (int i = list_delete.size()-1; i >= 0; i--) {
                            // List 삭제 & SQLite 삭제
                            Log.i(TAG, "삭제 i count : " + i);
                            Log.i(TAG, "삭제 row position : " + list_delete.get(i).getBOX_CNT());
                            String delete_box = list_delete.get(i).getBOX_CNT();

                            DBHandler.deletequerySelectGoodsWet(getApplicationContext(),
                                    list_delete.get(i).getGI_D_ID(), list_delete.get(i).getBARCODE(), Integer.parseInt(delete_box), list_delete.get(i).getGI_L_ID());
                            refresh_delete(list_delete.get(i).getWEIGHT());

                        /*    int removeindex = Integer.parseInt(delete_box)-1;
                            detailAdapter.remove(removeindex);*/
                        }
                        /*detailAdapter.notifyDataSetChanged();*/

                        vibrator.vibrate(500);
                        btn_send.setEnabled(false);
                        btn_send.setBackgroundResource(R.drawable.disable_round_button);

                        detail_btn_back.performClick();

                        if (Common.D) {
                            Log.d(TAG, "계근 선택항목 삭제 성공 !");
                        }
                        Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();

                    } catch (Exception ex) {
                        if (Common.D) {
                            Log.d(TAG, "계근 선택항목 삭제 실패 -> " + ex.getMessage().toString());
                        }
                        Toast.makeText(getApplicationContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(buttonNo, null)
                .show();
    }

    public void refresh_delete(String delete_weight) {
        Log.d(TAG, "삭제되는 계근대상의 중량 : " + delete_weight);
        arSM.get(select_position).setPACKING_QTY(arSM.get(select_position).getPACKING_QTY() - 1);
        double temp = arSM.get(select_position).getGI_QTY() - Double.parseDouble(delete_weight);
        temp = Math.round(temp * 100) / 100.0;
        arSM.get(select_position).setGI_QTY(temp);

        detail_edit_count.setText(arSM.get(select_position).getGI_REQ_PKG() + " / " + arSM.get(select_position).getPACKING_QTY());
        detail_edit_weight.setText(arSM.get(select_position).getGI_REQ_QTY() + " / " + arSM.get(select_position).getGI_QTY());
    }

    // 전송이 끝났음을 알리는 Dialog
    private void show_sendFinishDialog() {
        dialog_flag = true;
        new AlertDialog.Builder(BixolonShipmentActivity.this, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(R.string.shipment_wet_send_finish)
                .setMessage(R.string.shipment_wet_send_finish_msg)
                .setCancelable(false)
                .setPositiveButton("확인", (dialog, which) -> {
                    btn_send.setEnabled(false);
                    btn_send.setBackgroundResource(R.drawable.disable_round_button);
                    dialog_flag = false;
                    if (work_flag == 1) {
                        scanFlag_init();
                    } else if (work_flag == 0){
                        set_scanFlag(false);
                    } else if (work_flag == 2){
                        scanFlag_init();
                    }
                    edit_barcode.setText("");
                    work_item_fullbarcode = "";
                    work_item_barcodegoods = "";
                }).show();
    }

    // 다음 지점 계근을 묻는 Dialog
    private void show_wetNextDialog() {
        dialog_flag = true;
        new AlertDialog.Builder(BixolonShipmentActivity.this, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(R.string.shipment_wet_finish)
                .setMessage(R.string.shipment_wet_next_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.shipment_wet_ok, (dialog, which) -> {
                    // 다음 지점 선택
                    if (work_flag == 1) {
                        scanFlag_init();
                    } else if (work_flag == 0){
                        set_scanFlag(false);
                    } else if (work_flag == 2){
                        scanFlag_init();
                    }
                    select_flag = false;

                    work_item_fullbarcode = "";
                    edit_barcode.setText("");
                    //edit_wet_count.setText("0 / 0");
                    //edit_wet_weight.setText("0 / 0");
                    btn_send.setEnabled(false);
                    btn_send.setBackgroundResource(R.drawable.disable_round_button);
                    dialog_flag = false;
                    //for (int i = 0; i < arSM.size(); i++) {
                    //    arSM.get(i).setWORK_FLAG(false);
                    //}
                    sListAdapter.notifyDataSetChanged();
                }).show();
    }

    // 계근이 끝났음을 알리는 Dialog
    private void show_wetFinishDialog() {
        dialog_flag = true;
        new AlertDialog.Builder(BixolonShipmentActivity.this, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(R.string.shipment_wet_finish)
                .setMessage(R.string.shipment_wet_finish_msg)
                .setCancelable(false)
                .setPositiveButton("확인", (dialog, which) -> {
                    btn_send.setEnabled(true);
                    btn_send.setBackgroundResource(R.drawable.round_button);
                    dialog_flag = false;
                    if (work_flag == 1) {
                        scanFlag_init();
                    } else if (work_flag == 0){
                        set_scanFlag(false);
                    } else if (work_flag == 2){
                        scanFlag_init();
                    }
                    edit_barcode.setText("");
                    //work_item_fullbarcode = "";
                    //work_item_barcodegoods = "";
                }).show();
    }

    // 에러가 났을 때, 알림창 표시 showAelrtDialog 추가
    public void showAlertDialog(String s,int i){
        try {
            Inflater = (LayoutInflater) BixolonShipmentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final AlertDialog.Builder builder = new AlertDialog.Builder(BixolonShipmentActivity.this, R.style.AppCompatDialogStyle);
            vibrator.vibrate(500);
            builder.setIcon(R.drawable.highland);
            builder.setTitle("스캔 오류");
            Log.d(TAG, "alert_flag1 : " + alert_flag);
            if(!alert_flag) {
                if (s.equals("weight")) {
                    builder.setMessage("중량위치정보가 없습니다.\n다른 바코드를 스캔해주세요.");
                } else if (s.equals("barcode")) {
                    builder.setMessage("바코드 정보(조회결과)가 없습니다.\n다른 바코드를 스캔해주세요");
                } else if (s.equals("bl")) {
                    builder.setMessage(i + "번 상품의 bl정보가 없습니다.");
                }
                builder.setNeutralButton("확인", (dialog, id) -> {
                    alert_flag = false;
                    alert.dismiss();
                });
                alert = builder.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
                alert_flag = true;
            }else if(alert_flag)
                return;

            Log.d(TAG, "alert.isShowing:" + alert.isShowing());
            Log.d(TAG, "alert_flag2: " + alert_flag);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}



