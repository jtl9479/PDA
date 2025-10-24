package com.rgbsolution.highland_emart;

import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Vibrator;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ExpiryEnterActivity extends AppCompatActivity {

    private EditText enteredWeight;
    private EditText makingDateFrom;
    private EditText makingDateTo;
    private Vibrator vibrator;
    //private Button buttonClose;

    private final String TAG = "ExpiryEnterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Intent intent;
        // ... 코드 계속
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expiry_enter);
        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        Button buttonClose = (Button) findViewById(R.id.buttonClose);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //ShipmentAcitivity 에서 받아옴
        Intent intentB = getIntent();

        final String weightReceivedStr;
        final Double weightReceivedDbl;
        String makingFromReceived;
        String makingToReceived;

        weightReceivedStr = intentB.getStringExtra("weightStrKey");
        weightReceivedDbl = intentB.getDoubleExtra("weightDblKey",0);

        makingFromReceived = intentB.getStringExtra("makingFromKey");
        makingToReceived = intentB.getStringExtra("makingToKey");

        Log.i(TAG, "=====================메인에서 받아온 값 체크 weightReceivedStr==================" + weightReceivedStr);
        Log.i(TAG, "=====================메인에서 받아온 값 체크 weightReceivedDbl==================" + weightReceivedDbl);
        Log.i(TAG, "=====================메인에서 받아온 값 체크 makingFromReceived==================" + makingFromReceived);
        Log.i(TAG, "=====================메인에서 받아온 값 체크 makingToReceived==================" + makingToReceived);

        enteredWeight = (EditText) findViewById(R.id.enteredWeight);  //ppcode 입력창
        makingDateFrom = (EditText) findViewById(R.id.makingDateFrom); //packer code 입력창
        makingDateTo = (EditText) findViewById(R.id.makingDateTo); //바코드/수기 입력창

        enteredWeight.setText(weightReceivedStr);
        makingDateFrom.setText(makingFromReceived);
        makingDateTo.setText(makingToReceived);

        enteredWeight.setClickable(false);
        enteredWeight.setFocusable(false);
        makingDateFrom.setClickable(false);
        makingDateFrom.setFocusable(false);
        makingDateTo.setClickable(false);
        makingDateTo.setFocusable(false);

        buttonSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //ShipmentActivity로 보낼 때
                Intent intent = new Intent();
                intent.putExtra("enteredWeightSend", weightReceivedStr);
                intent.putExtra("enteredWeightDblSend", weightReceivedDbl);

                //DATE 입력받은 것 메인으로 전달
                EditText enteredMakingDate = (EditText) findViewById(R.id.enteredMakingDate);
                String enteredMakingDateSend = enteredMakingDate.getText().toString();

                //날짜 형식 맞는지 검증한다

                String checkDate = "20"+enteredMakingDateSend;
                Log.i(TAG, "===================체크데이트 확인=================="+checkDate);
                try{
                    SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyyMMdd");
                    dateFormat.setLenient(false);
                    dateFormat.parse(checkDate);
                    //문제없으므로 통과
                    intent.putExtra("enteredMakingDateSend", enteredMakingDateSend);
                    setResult(RESULT_OK, intent);
                    finish();
                }catch (ParseException e){
                    Toast.makeText(getApplicationContext(), "날짜를 알맞은 형식으로 입력하세요.", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(1000);
                    makingDateTo.requestFocus();
                    return;
                }
            }
        });

        //닫기버튼
        buttonClose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
