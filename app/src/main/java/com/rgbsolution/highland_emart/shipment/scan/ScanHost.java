package com.rgbsolution.highland_emart.shipment.scan;

import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * ScanProcessor 가 호출하는 화면·태스크 기능 (개발/77 Step 8) — BixolonShipmentActivity 가 구현
 */
public interface ScanHost {
    void setBarcodeMsgProduction(String msg);                    // 생산(1) 전용 흐름
    String findPackerProduct(String barcode);                    // find_PackerProduct
    String findPackerProductBarcodeGoods(String barcode);        // find_PackerProductBarcodeGoods
    void setBarcodeText(String text);                            // edit_barcode.setText
    void vibrate(long ms);                                       // vibrator.vibrate
    String getSelectedCenterName();                              // sp_center_name.getSelectedItem().toString()
    void startShipSelect(String centerName, String condition, boolean type);   // new ShipSelectTask(...).execute()
    void setScanFlag(boolean flag);                              // set_scanFlag
    int getCenterTotalCount();                                   // centerTotalCount
    int getCenterWorkCount();                                    // centerWorkCount
    void showWetFinishDialog();                                  // show_wetFinishDialog
    String getSelectedBlNo();                                    // sp_bl_no 선택 항목
    ArrayList<Shipments_Info> getShipments();                    // arSM
    void selectPoint(int position);                              // sp_point_name.setSelection
    void selectListPosition(int position);                       // sList.setSelection
    void showAlertDialog(String type, int i);                    // showAlertDialog
    void wetDataInsert(String weight_str, double weight_double, String making_date, String box_serial);   // wet_data_insert
}
