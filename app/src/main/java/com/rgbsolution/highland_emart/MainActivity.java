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


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Vibrator vibrator;
    private String chkProdShip = "";

    Calendar calendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener(){
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

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
            Log.i(TAG, TAG + "=====================selectDay======================" + inPutDay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        int id = item.getItemId();

        if (id == R.id.action_pinrtsettings) {
            i = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_daysettings) {
            new DatePickerDialog(MainActivity.this,date,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btnDay:                                // 날짜 선택
                new DatePickerDialog(MainActivity.this,date,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

                /*new AlertDialog.Builder(MainActivity.this, R.style.AppCompatDialogStyle)
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
            case R.id.buttonDelete:                                // 출하대상 삭제
                DBHandler.deletequeryShipment(getApplicationContext());
                Toast.makeText(getApplicationContext(), "출하대상이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnDownload:                                // 출하대상받기 Activity
                Log.i(TAG, TAG + "=====================출하대상받기======================" + Common.selectDay);

                chkProdShip = "ship";

                Common.searchType = "0";

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;
            case R.id.btnproductionlist:                                // 생산대상받기 Activity
                Log.i(TAG, TAG + "=====================생산대상받기======================" + Common.selectDay);

                chkProdShip = "prod";
                Common.searchType = "1";

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;
            case R.id.btnDownloadHomeplus:                                // 생산대상받기 Activity
                Log.i(TAG, TAG + "=====================홈플러스출하대상받기======================" + Common.selectDay);

                chkProdShip = "homplus";
                Common.searchType = "2";

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;
            case R.id.btnDownloadWholesale:                                // 생산대상받기 Activity
                Log.i(TAG, TAG + "=====================도매업체출하대상받기======================" + Common.selectDay);
                //chkProdShip = "homplus";
                Common.searchType = "3";

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;
            case R.id.btnproductionNonfixedlist:                                // 생산대상받기 Activity
                Log.i(TAG, TAG + "=====================비정량출하대상받기======================" + Common.selectDay);
                //chkProdShip = "homplus";
                Common.searchType = "4";

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;
            case R.id.btnDownloadLotte:                                // 롯데 생산대상받기 Activity
                Log.i(TAG, TAG + "=====================롯데출하대상받기======================" + Common.selectDay);

                chkProdShip = "lotte";
                Common.searchType = "6"; //이노 PDA searchType == 5 홈플러스 비정량

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;
            case R.id.btnWet:                                // 바코드정보 입력/수정 Activity

                /*if (chkProdShip.equals("prod")) {
                    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }*/

                if (!Common.searchType.equals("0")) {
                    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }

                Common.searchType = "0";
                ArrayList<Shipments_Info> list_si = DBHandler.selectqueryAllShipment(MainActivity.this);

                if (list_si.size() > 0) {
                    i = new Intent(this, ShipmentActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "출하대상 리스트가 없습니다.\n리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                }
                break;

            case R.id.btnWetWholesale:

                /*if (chkProdShip.equals("prod")) {
                    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }*/

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

            case R.id.btnProdWet:                                // 바코드정보 입력/수정 Activity

                if (!Common.searchType.equals("1")) {
                    Toast.makeText(getApplicationContext(), "생산 계근을 위해 생산 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(300);
                    break;
                }
                /*if (chkProdShip.equals("ship")) {
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

            case R.id.btnWetHomeplus:                                // 홈플러스 계근 입력

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

            // 홈플러스 비정량 계근 출하대상 받기
            case R.id.btnWetHomeplusNon:

                Log.i(TAG, TAG + "=====================홈플러스 비정량 출하대상받기======================" + Common.selectDay);

//                chkProdShip = "homplus";
                Common.searchType = "5";

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;

            // 홈플러스 비정량 계근 입력
            case R.id.btnWetHomeplusNon2:

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

            case R.id.btnProdNonfixedWet:                                // 비정량 계근 입력

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
            case R.id.btnWetLotte:                                // 롯데 계근 입력

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
            case R.id.btnproductionlist4print:
                Log.i(TAG, TAG + "=====================생산대상받기(라벨)======================" + Common.selectDay);

                chkProdShip = "prod";
                Common.searchType = "7";

                DBHandler.deletequeryShipment(getApplicationContext());

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

                DBHandler.deletequeryBarcodeInfo(getApplicationContext());            // 바코드정보 삭제
                DBHandler.deletequeryGoodsWet(getApplicationContext());
                new ProgressDlgShipSearch(this).execute();

                break;
            case R.id.btnProdWet4print:                                // 바코드정보 입력/수정 Activity

                Log.i(TAG, TAG + "=====================생산입력시작(라벨)======================" + Common.selectDay);
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

            case R.id.btnProdWetCalc:

                i = new Intent(this, ProductionActivity.class);
                startActivity(i);

                break;

            case R.id.action_daysettings:
                i = new Intent(this, SettingActivity.class);
                startActivity(i);
                break;

            case R.id.btnClose:                                // 종료 Dialog 호출
                exitDialog();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, TAG + " onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG + " onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG + " onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy");
    }

    //	물리버튼 KeyDown 이벤트
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    //	프로그램 종료 Dialog
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
}//test for commit 11
