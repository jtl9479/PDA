package com.rgbsolution.highland_emart.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@SuppressLint("SimpleDateFormat")
public class DBHandler {
    private static final String TAG = "DBHandler";
    static SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
    static SimpleDateFormat timeformat = new SimpleDateFormat("HHmmss");

    // 출하대상 CREATE
    public static void createqueryShipment(Context context) {

        Log.v(TAG, "테이블 만들러 들어옴!!!");

        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS "
                    + DBInfo.TABLE_NAME_SHIPMENT + " ("
                    + DBInfo.SHIPMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DBInfo.GI_H_ID + " TEXT NOT NULL, "
                    + DBInfo.GI_D_ID + " TEXT NOT NULL, "
                    + DBInfo.EOI_ID + " TEXT NOT NULL, "
                    + DBInfo.ITEM_CODE + " TEXT NOT NULL, "
                    + DBInfo.ITEM_NAME + " TEXT NOT NULL, "
                    + DBInfo.EMARTITEM_CODE + " TEXT, "
                    + DBInfo.EMARTITEM + " TEXT, "
                    + DBInfo.GI_REQ_PKG + " TEXT NOT NULL, "
                    + DBInfo.GI_REQ_QTY + " TEXT NOT NULL, "
                    + DBInfo.AMOUNT + " TEXT NOT NULL, "
                    + DBInfo.GOODS_R_ID + " TEXT NOT NULL, "
                    + DBInfo.GR_REF_NO + " TEXT NOT NULL, "
                    + DBInfo.GI_REQ_DATE + " TEXT NOT NULL, "
                    + DBInfo.BL_NO + " TEXT NOT NULL, "
                    + DBInfo.BRAND_CODE + " TEXT NOT NULL, "
                    + DBInfo.BRANDNAME + " TEXT NOT NULL, "
                    + DBInfo.CLIENT_CODE + " TEXT NOT NULL, "
                    + DBInfo.CLIENTNAME + " TEXT NOT NULL, "
                    + DBInfo.CENTERNAME + " TEXT, "
                    + DBInfo.ITEM_SPEC + " TEXT NOT NULL, "
                    + DBInfo.CT_CODE + " TEXT NOT NULL, "
                    + DBInfo.IMPORT_ID_NO + " TEXT NOT NULL, "
                    + DBInfo.PACKER_CODE + " TEXT NOT NULL, "
                    + DBInfo.PACKERNAME + " TEXT NOT NULL, "
                    + DBInfo.PACKER_PRODUCT_CODE + " TEXT NOT NULL, "
                    + DBInfo.BARCODE_TYPE + " TEXT NOT NULL, "
                    + DBInfo.ITEM_TYPE + " TEXT NOT NULL, "
                    + DBInfo.PACKWEIGHT + " TEXT, "
                    + DBInfo.BARCODEGOODS + " TEXT, "
                    + DBInfo.STORE_IN_DATE + " TEXT, "
                    + DBInfo.EMARTLOGIS_CODE + " TEXT, "
                    + DBInfo.EMARTLOGIS_NAME + " TEXT, "
                    + DBInfo.SAVE_TYPE + " TEXT NOT NULL,"
                    + DBInfo.WH_AREA + " TEXT,"
                    + DBInfo.USE_NAME + " TEXT,"
                    + DBInfo.USE_CODE + " TEXT,"
                    + DBInfo.CT_NAME + " TEXT,"
                    + DBInfo.STORE_CODE + " TEXT,"
                    + DBInfo.EMART_PLANT_CODE + " TEXT,"
                    + DBInfo.LAST_BOX_ORDER  + " INTEGER"
                    + ")";
            if (Common.D) {
                Log.v(TAG, "createqueryShilment -> " + createTable);
            }
            dbHelper.executeSql(createTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "createqueryShilment exception -> " + e.getMessage().toString());
            }
        }
        dbHelper.close();
    }

    // 출하대상 SELECT
    public static ArrayList<Shipments_Info> selectqueryShipment(Context context, String center_name, String condition, boolean type) {
        ArrayList<Shipments_Info> list_si_info = new ArrayList<Shipments_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr;
            String qry_condition;

            if (type) {      // true : pp_code
                qry_condition = " AND PACKER_PRODUCT_CODE in ('" + condition + "' )"; //전체 지점 검색
                /*qry_condition = ""; //전체 지점 검색*/
            } else {         // false : bl_no
                qry_condition = " AND BL_NO = '" + condition + "' ";
            }

            // 선택 날짜 지정
            qry_condition = qry_condition + " AND GI_REQ_DATE = " + Common.selectDay;

            sqlStr = "SELECT "
                    + DBInfo.SHIPMENT_ID + ", "
                    + DBInfo.GI_H_ID + ", "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.EOI_ID + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.GI_REQ_PKG + ", "
                    + DBInfo.GI_REQ_QTY + ", "
                    + DBInfo.AMOUNT + ", "
                    + DBInfo.GOODS_R_ID + ", "
                    + DBInfo.GR_REF_NO + ", "
                    + DBInfo.GI_REQ_DATE + ", "
                    + DBInfo.BL_NO + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.BRANDNAME + ", "
                    + DBInfo.CLIENT_CODE + ", "
                    + DBInfo.CLIENTNAME + ", "
                    + DBInfo.CENTERNAME + ", "
                    + DBInfo.ITEM_SPEC + ", "
                    + DBInfo.CT_CODE + ", "
                    + DBInfo.IMPORT_ID_NO + ", "
                    + DBInfo.PACKER_CODE + ", "
                    + DBInfo.PACKERNAME + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE_TYPE + ", "
                    + DBInfo.ITEM_TYPE + ", "
                    + DBInfo.PACKWEIGHT + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.STORE_IN_DATE + ", "
                    + DBInfo.EMARTLOGIS_CODE + ", "
                    + DBInfo.EMARTLOGIS_NAME + ", "
                    + DBInfo.WH_AREA + ", "
                    + DBInfo.USE_NAME + ", "
                    + DBInfo.USE_CODE + ", "
                    + DBInfo.CT_NAME + ", "
                    + DBInfo.STORE_CODE + ", "
                    + DBInfo.SAVE_TYPE  + ", "
                    + DBInfo.EMART_PLANT_CODE + ", "
                    + DBInfo.LAST_BOX_ORDER + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " WHERE CENTERNAME = '" + center_name + "' "
                    + qry_condition
                    + " ORDER BY SAVE_TYPE ASC, SHIPMENT_ID ASC, CLIENTNAME ASC";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryShipment       ->" + sqlStr);
                Log.v(TAG, "selectqueryShipment Count ->" + cursor.getCount());
            }

            Shipments_Info si;

            while (cursor.moveToNext()) {
                si = new Shipments_Info();
                si.setSHIPMENT_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SHIPMENT_ID")), ""));
                si.setGI_H_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_H_ID")), ""));
                si.setGI_D_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_D_ID")), ""));
                si.setEOI_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EOI_ID")), ""));
                si.setITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
                si.setITEM_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_NAME")), ""));
                si.setEMARTITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM_CODE")), ""));
                si.setEMARTITEM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM")), ""));
                si.setGI_REQ_PKG(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_PKG")), ""));
                si.setGI_REQ_QTY(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_QTY")), ""));
                si.setAMOUNT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("AMOUNT")), ""));
                si.setGOODS_R_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GOODS_R_ID")), ""));
                si.setGR_REF_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GR_REF_NO")), ""));
                si.setGI_REQ_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_DATE")), ""));
                si.setBL_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BL_NO")), ""));
                si.setBRAND_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
                si.setBRANDNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRANDNAME")), ""));
                si.setCLIENT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CLIENT_CODE")), ""));
                si.setCLIENTNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CLIENTNAME")), ""));
                si.setCENTERNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CENTERNAME")), ""));
                si.setITEM_SPEC(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_SPEC")), ""));
                si.setCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CT_CODE")), ""));
                si.setIMPORT_ID_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("IMPORT_ID_NO")), ""));
                si.setPACKER_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CODE")), ""));
                si.setPACKERNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKERNAME")), ""));
                si.setPACKER_PRODUCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
                si.setBARCODE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE_TYPE")), ""));
                si.setITEM_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_TYPE")), ""));
                si.setPACKWEIGHT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKWEIGHT")), ""));
                si.setBARCODEGOODS(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
                si.setSTORE_IN_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("STORE_IN_DATE")), ""));
                si.setEMARTLOGIS_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTLOGIS_CODE")), ""));
                si.setEMARTLOGIS_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTLOGIS_NAME")), ""));
                si.setSAVE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SAVE_TYPE")), ""));
                si.setWH_AREA(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WH_AREA")), ""));
                si.setUSE_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("USE_NAME")), ""));
                si.setUSE_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("USE_CODE")), ""));
                si.setCT_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CT_NAME")), ""));
                si.setSTORE_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("STORE_CODE")), ""));
                si.setEMART_PLANT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMART_PLANT_CODE")), ""));
                si.setLAST_BOX_ORDER(Common.nullCheck(cursor.getString(cursor.getColumnIndex("LAST_BOX_ORDER")), ""));

                list_si_info.add(si);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryShipment exception -> " + e.toString());
            }
        }

        mDbHelper.close();
        return list_si_info;
    }

    // 출하대상 SELECT
    public static ArrayList<Shipments_Info> selectqueryShipmentOnly(Context context, String barcodegoods) {
        ArrayList<Shipments_Info> list_si_info = new ArrayList<Shipments_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr;

            sqlStr = "SELECT "
                    + DBInfo.SHIPMENT_ID + ", "
                    + DBInfo.GI_H_ID + ", "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.EOI_ID + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.GI_REQ_PKG + ", "
                    + DBInfo.GI_REQ_QTY + ", "
                    + DBInfo.AMOUNT + ", "
                    + DBInfo.GOODS_R_ID + ", "
                    + DBInfo.GR_REF_NO + ", "
                    + DBInfo.GI_REQ_DATE + ", "
                    + DBInfo.BL_NO + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.BRANDNAME + ", "
                    + DBInfo.CLIENT_CODE + ", "
                    + DBInfo.CLIENTNAME + ", "
                    + DBInfo.CENTERNAME + ", "
                    + DBInfo.ITEM_SPEC + ", "
                    + DBInfo.CT_CODE + ", "
                    + DBInfo.IMPORT_ID_NO + ", "
                    + DBInfo.PACKER_CODE + ", "
                    + DBInfo.PACKERNAME + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE_TYPE + ", "
                    + DBInfo.ITEM_TYPE + ", "
                    + DBInfo.PACKWEIGHT + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.STORE_IN_DATE + ", "
                    + DBInfo.EMARTLOGIS_CODE + ", "
                    + DBInfo.EMARTLOGIS_NAME + ", "
                    + DBInfo.SAVE_TYPE + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " WHERE BARCODEGOODS = '" + barcodegoods + "' "
                    + " ORDER BY SAVE_TYPE ASC, CLIENTNAME ASC";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryShipment       ->" + sqlStr);
                Log.v(TAG, "selectqueryShipment Count ->" + cursor.getCount());
            }

            Shipments_Info si;
            while (cursor.moveToNext()) {
                si = new Shipments_Info();
                si.setSHIPMENT_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SHIPMENT_ID")), ""));
                si.setGI_H_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_H_ID")), ""));
                si.setGI_D_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_D_ID")), ""));
                si.setEOI_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EOI_ID")), ""));
                si.setITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
                si.setITEM_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_NAME")), ""));
                si.setEMARTITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM_CODE")), ""));
                si.setEMARTITEM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM")), ""));
                si.setGI_REQ_PKG(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_PKG")), ""));
                si.setGI_REQ_QTY(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_QTY")), ""));
                si.setAMOUNT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("AMOUNT")), ""));
                si.setGOODS_R_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GOODS_R_ID")), ""));
                si.setGR_REF_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GR_REF_NO")), ""));
                si.setGI_REQ_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_DATE")), ""));
                si.setBL_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BL_NO")), ""));
                si.setBRAND_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
                si.setBRANDNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRANDNAME")), ""));
                si.setCLIENT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CLIENT_CODE")), ""));
                si.setCLIENTNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CLIENTNAME")), ""));
                si.setCENTERNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CENTERNAME")), ""));
                si.setITEM_SPEC(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_SPEC")), ""));
                si.setCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CT_CODE")), ""));
                si.setIMPORT_ID_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("IMPORT_ID_NO")), ""));
                si.setPACKER_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CODE")), ""));
                si.setPACKERNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKERNAME")), ""));
                si.setPACKER_PRODUCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
                si.setBARCODE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE_TYPE")), ""));
                si.setITEM_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_TYPE")), ""));
                si.setPACKWEIGHT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKWEIGHT")), ""));
                si.setBARCODEGOODS(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
                si.setSTORE_IN_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("STORE_IN_DATE")), ""));
                si.setEMARTLOGIS_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTLOGIS_CODE")), ""));
                si.setEMARTLOGIS_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTLOGIS_NAME")), ""));
                si.setSAVE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SAVE_TYPE")), ""));

                list_si_info.add(si);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryShipment exception -> " + e.toString());
            }
        }

        mDbHelper.close();
        return list_si_info;
    }

    public static ArrayList<Shipments_Info> selectqueryShipmentBL(Context context, String center_name, String bl_no) {
        ArrayList<Shipments_Info> list_si_info = new ArrayList<Shipments_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr;

            sqlStr = "SELECT "
                    + DBInfo.SHIPMENT_ID + ", "
                    + DBInfo.GI_H_ID + ", "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.EOI_ID + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.GI_REQ_PKG + ", "
                    + DBInfo.GI_REQ_QTY + ", "
                    + DBInfo.AMOUNT + ", "
                    + DBInfo.GOODS_R_ID + ", "
                    + DBInfo.GR_REF_NO + ", "
                    + DBInfo.GI_REQ_DATE + ", "
                    + DBInfo.BL_NO + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.BRANDNAME + ", "
                    + DBInfo.CLIENT_CODE + ", "
                    + DBInfo.CLIENTNAME + ", "
                    + DBInfo.CENTERNAME + ", "
                    + DBInfo.ITEM_SPEC + ", "
                    + DBInfo.CT_CODE + ", "
                    + DBInfo.IMPORT_ID_NO + ", "
                    + DBInfo.PACKER_CODE + ", "
                    + DBInfo.PACKERNAME + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE_TYPE + ", "
                    + DBInfo.ITEM_TYPE + ", "
                    + DBInfo.PACKWEIGHT + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.STORE_IN_DATE + ", "
                    + DBInfo.EMARTLOGIS_CODE + ", "
                    + DBInfo.EMARTLOGIS_NAME + ", "
                    + DBInfo.SAVE_TYPE + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " WHERE  CENTERNAME = '" + center_name + "' "
                    + " AND BL_NO = '" + bl_no + "' "
                    + " ORDER BY SAVE_TYPE ASC, CLIENTNAME ASC";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryShipmentBL       ->" + sqlStr);
                Log.v(TAG, "selectqueryShipmentBL Count ->" + cursor.getCount());
            }

            HashMap<String, String> data;
            Shipments_Info si;
            while (cursor.moveToNext()) {
                si = new Shipments_Info();
                si.setSHIPMENT_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SHIPMENT_ID")), ""));
                si.setGI_H_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_H_ID")), ""));
                si.setGI_D_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_D_ID")), ""));
                si.setEOI_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EOI_ID")), ""));
                si.setITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
                si.setITEM_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_NAME")), ""));
                si.setEMARTITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM_CODE")), ""));
                si.setEMARTITEM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM")), ""));
                si.setGI_REQ_PKG(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_PKG")), ""));
                si.setGI_REQ_QTY(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_QTY")), ""));
                si.setAMOUNT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("AMOUNT")), ""));
                si.setGOODS_R_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GOODS_R_ID")), ""));
                si.setGR_REF_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GR_REF_NO")), ""));
                si.setGI_REQ_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_REQ_DATE")), ""));
                si.setBL_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BL_NO")), ""));
                si.setBRAND_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
                si.setBRANDNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRANDNAME")), ""));
                si.setCLIENT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CLIENT_CODE")), ""));
                si.setCLIENTNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CLIENTNAME")), ""));
                si.setCENTERNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CENTERNAME")), ""));
                si.setITEM_SPEC(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_SPEC")), ""));
                si.setCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CT_CODE")), ""));
                si.setIMPORT_ID_NO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("IMPORT_ID_NO")), ""));
                si.setPACKER_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CODE")), ""));
                si.setPACKERNAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKERNAME")), ""));
                si.setPACKER_PRODUCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
                si.setBARCODE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE_TYPE")), ""));
                si.setITEM_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_TYPE")), ""));
                si.setPACKWEIGHT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKWEIGHT")), ""));
                si.setBARCODEGOODS(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
                si.setSTORE_IN_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("STORE_IN_DATE")), ""));
                si.setEMARTLOGIS_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTLOGIS_CODE")), ""));
                si.setEMARTLOGIS_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTLOGIS_NAME")), ""));
                si.setSAVE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SAVE_TYPE")), ""));

                list_si_info.add(si);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryShipment exception -> " + e.toString());
            }
        }
        mDbHelper.close();
        return list_si_info;
    }

    //	출하대상 검색
    public static ArrayList<Shipments_Info> selectqueryAllShipment(Context context) {//
        ArrayList<Shipments_Info> list_si = new ArrayList<Shipments_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.EOI_ID
                    + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " ORDER BY EOI_ID ASC";

            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryAllShipment -> " + sqlStr);
                Log.v(TAG, "selectqueryAllShipment ->" + cursor.getCount());
            }

            Shipments_Info si;
            while (cursor.moveToNext()) {
                si = new Shipments_Info();
                si.setGI_D_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_D_ID")), ""));
                si.setEOI_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EOI_ID")), ""));

                list_si.add(si);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryAllShipment exception -> " + e.getMessage().toString());
            }
        }
        mDbHelper.close();
        return list_si;
    }

    //	생산대상 검색
    public static ArrayList<Shipments_Info> selectqueryAllProduction(Context context) {//
        ArrayList<Shipments_Info> list_si = new ArrayList<Shipments_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.EOI_ID
                    + " FROM "
                    + DBInfo.TABLE_NAME_PRODUCTION
                    + " ORDER BY EOI_ID ASC";

            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryAllProduction -> " + sqlStr);
                Log.v(TAG, "selectqueryAllProduction ->" + cursor.getCount());
            }

            Shipments_Info si;
            while (cursor.moveToNext()) {
                si = new Shipments_Info();
                si.setGI_D_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_D_ID")), ""));
                si.setEOI_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EOI_ID")), ""));

                list_si.add(si);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryAllProduction exception -> " + e.getMessage().toString());
            }
        }
        mDbHelper.close();
        return list_si;
    }

    // 출하대상's Packer_Product_Code list SELECT
    public static ArrayList<String[]> selectqueryCodeList(Context context) {
        ArrayList<String[]> list_code_info = new ArrayList<String[]>();
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT DISTINCT "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.ITEM_NAME
                    + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " ORDER BY " + DBInfo.PACKER_PRODUCT_CODE + " ASC";

            cursor = dbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryCodeList -> " + sqlStr);
                Log.v(TAG, "selectqueryCodeList ->" + cursor.getCount());
            }

            while (cursor.moveToNext()) {
                String[] temp = new String[2];
                temp[0] = cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE"));
                temp[1] = cursor.getString(cursor.getColumnIndex("ITEM_NAME"));
                list_code_info.add(temp);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryCodeList exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
        return list_code_info;
    }

    //
    public static ArrayList<String[]> selectqueryCodeListForNonFixed(Context context) {
        ArrayList<String[]> list_code_info = new ArrayList<String[]>();
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT DISTINCT "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME
                    + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " ORDER BY " + DBInfo.PACKER_PRODUCT_CODE + " ASC";

            cursor = dbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryCodeList -> " + sqlStr);
                Log.v(TAG, "selectqueryCodeList ->" + cursor.getCount());
            }

            while (cursor.moveToNext()) {
                String[] temp = new String[2];
                temp[0] = cursor.getString(cursor.getColumnIndex("ITEM_CODE"));
                temp[1] = cursor.getString(cursor.getColumnIndex("ITEM_NAME"));
                list_code_info.add(temp);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryCodeList exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
        return list_code_info;
    }

    // 출하대상's GI_D_ID list SELECT
    public static ArrayList<String> selectqueryGIDIDList(Context context) {
        ArrayList<String> list_id_info = new ArrayList<String>();
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT DISTINCT "
                    + DBInfo.GI_D_ID
                    + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " ORDER BY " + DBInfo.GI_D_ID + " ASC";

            cursor = dbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryGIDIDList -> " + sqlStr);
                Log.v(TAG, "selectqueryGIDIDList ->" + cursor.getCount());
            }

            while (cursor.moveToNext()) {
                String temp;
                temp = cursor.getString(cursor.getColumnIndex("GI_D_ID"));
                list_id_info.add(temp);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryGIDIDList exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
        return list_id_info;
    }

    // 출하대상's CENTERNAME list SELECT
    public static ArrayList<String> selectqueryCenterList(Context context) {
        ArrayList<String> list_center_info = new ArrayList<String>();
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT DISTINCT "
                    + DBInfo.CENTERNAME
                    + " FROM "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " ORDER BY " + DBInfo.CENTERNAME + " ASC";

            cursor = dbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryCenterList -> " + sqlStr);
                Log.v(TAG, "selectqueryCenterList ->" + cursor.getCount());
            }

            while (cursor.moveToNext()) {
                String temp;
                temp = cursor.getString(cursor.getColumnIndex(DBInfo.CENTERNAME));
                list_center_info.add(temp);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryCenterList exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
        return list_center_info;
    }

    // 출하대상 INSERT
    public static boolean insertqueryShipment(Context context, Shipments_Info si, String saveType) {// 집하입고 INSERT
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        boolean result;

        try {
            String sqlStr = "INSERT INTO " + DBInfo.TABLE_NAME_SHIPMENT + " ("
                    + DBInfo.GI_H_ID + ", "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.EOI_ID + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.GI_REQ_PKG + ", "
                    + DBInfo.GI_REQ_QTY + ", "
                    + DBInfo.AMOUNT + ", "
                    + DBInfo.GOODS_R_ID + ", "
                    + DBInfo.GR_REF_NO + ", "
                    + DBInfo.GI_REQ_DATE + ", "
                    + DBInfo.BL_NO + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.BRANDNAME + ", "
                    + DBInfo.CLIENT_CODE + ", "
                    + DBInfo.CLIENTNAME + ", "
                    + DBInfo.CENTERNAME + ", "
                    + DBInfo.ITEM_SPEC + ", "
                    + DBInfo.CT_CODE + ", "
                    + DBInfo.IMPORT_ID_NO + ", "
                    + DBInfo.PACKER_CODE + ", "
                    + DBInfo.PACKERNAME + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE_TYPE + ", "
                    + DBInfo.ITEM_TYPE + ", "
                    + DBInfo.PACKWEIGHT + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.STORE_IN_DATE + ", "
                    + DBInfo.EMARTLOGIS_CODE + ", "
                    + DBInfo.EMARTLOGIS_NAME + ", "
                    + DBInfo.WH_AREA + ", "
                    + DBInfo.USE_NAME + ", "
                    + DBInfo.USE_CODE + ", "
                    + DBInfo.CT_NAME + ", "
                    + DBInfo.STORE_CODE + ", "
                    + DBInfo.SAVE_TYPE + ", "
                    + DBInfo.EMART_PLANT_CODE + ", "
                    + DBInfo.LAST_BOX_ORDER
                    + ") VALUES('"
                    + Common.nullCheck(si.getGI_H_ID(), "") + "','"
                    + Common.nullCheck(si.getGI_D_ID(), "") + "','"
                    + Common.nullCheck(si.getEOI_ID(), "") + "','"
                    + Common.nullCheck(si.getITEM_CODE(), "") + "','"
                    + Common.nullCheck(si.getITEM_NAME(), "") + "','"
                    + Common.nullCheck(si.getEMARTITEM_CODE(), "") + "','"
                    + Common.nullCheck(si.getEMARTITEM(), "") + "','"
                    + Common.nullCheck(si.getGI_REQ_PKG(), "") + "','"
                    + Common.nullCheck(si.getGI_REQ_QTY(), "") + "','"
                    + Common.nullCheck(si.getAMOUNT(), "") + "','"
                    + Common.nullCheck(si.getGOODS_R_ID(), "") + "','"
                    + Common.nullCheck(si.getGR_REF_NO(), "") + "','"
                    + Common.nullCheck(si.getGI_REQ_DATE(), "") + "','"
                    + Common.nullCheck(si.getBL_NO(), "") + "','"
                    + Common.nullCheck(si.getBRAND_CODE(), "") + "','"
                    + Common.nullCheck(si.getBRANDNAME(), "") + "','"
                    + Common.nullCheck(si.getCLIENT_CODE(), "") + "','"
                    + Common.nullCheck(si.getCLIENTNAME(), "") + "','"
                    + Common.nullCheck(si.getCENTERNAME(), "") + "','"
                    + Common.nullCheck(si.getITEM_SPEC(), "") + "','"
                    + Common.nullCheck(si.getCT_CODE(), "") + "','"
                    + Common.nullCheck(si.getIMPORT_ID_NO(), "") + "','"
                    + Common.nullCheck(si.getPACKER_CODE(), "") + "','"
                    + Common.nullCheck(si.getPACKERNAME(), "") + "','"
                    + Common.nullCheck(si.getPACKER_PRODUCT_CODE(), "") + "','"
                    + Common.nullCheck(si.getBARCODE_TYPE(), "") + "','"
                    + Common.nullCheck(si.getITEM_TYPE(), "") + "','"
                    + Common.nullCheck(si.getPACKWEIGHT(), "") + "','"
                    + Common.nullCheck(si.getBARCODEGOODS(), "") + "','"
                    + Common.nullCheck(si.getSTORE_IN_DATE(), "") + "','"
                    + Common.nullCheck(si.getEMARTLOGIS_CODE(), "") + "','"
                    + Common.nullCheck(si.getEMARTLOGIS_NAME(), "") + "','"
                    + Common.nullCheck(si.getWH_AREA(), "") + "','"
                    + Common.nullCheck(si.getUSE_NAME(), "") + "','"
                    + Common.nullCheck(si.getUSE_CODE(), "") + "','"
                    + Common.nullCheck(si.getCT_NAME(), "") + "','"
                    + Common.nullCheck(si.getSTORE_CODE(), "") + "','"
                    + saveType + "','"
                    + Common.nullCheck(si.getEMART_PLANT_CODE(), "")  + "','"
                    + Common.nullCheck(si.getLAST_BOX_ORDER(), "")
                    + "')";
            if (Common.D) {
                Log.d(TAG, "insertqueryShipment -> " + sqlStr);
            }
            dbHelper.executeSql(sqlStr);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.v(TAG, "insertqueryShipment exception -> " + e.getMessage().toString());
            }
            dbHelper.close();
            result = false;
            return result;
        }

        dbHelper.close();
        return result;
    }

    // 출하대상 DELETE
    public static void deletequeryShipment(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "DELETE FROM " + DBInfo.TABLE_NAME_SHIPMENT;

            if (Common.D) {
                Log.v(TAG, "deletequeryShipment -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);

            deleteTable = "DELETE FROM sqlite_sequence WHERE name = '" + DBInfo.TABLE_NAME_SHIPMENT + "'";
            dbHelper.executeSql(deleteTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "deletequeryShipment exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    // 출하대상 UPDATE
    public static void updatequeryShipment(Context context, String gi_d_id, String packer_product_code) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String updateTable = "UPDATE "
                    + DBInfo.TABLE_NAME_SHIPMENT
                    + " SET "
                    + DBInfo.SAVE_TYPE + " = 'Y'"
                    + " WHERE GI_D_ID = '" + gi_d_id
                    + "' AND "
                    + "PACKER_PRODUCT_CODE = '" + packer_product_code + "'";

            if (Common.D) {
                Log.v(TAG, "updatequeryShipment -> " + updateTable);
            }
            dbHelper.executeSql(updateTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "updatequeryShipment exception -> " + e.getMessage().toString());
            }
        } finally {
            dbHelper.close();
        }
    }

    // 바코드 정보 CREATE
    public static void createqueryBarcodeInfo(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS "
                    + DBInfo.TABLE_NAME_BARCODE_INFO + " ("
                    + DBInfo.BARCODE_INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DBInfo.PACKER_CLIENT_CODE + " TEXT NOT NULL, "
                    + DBInfo.PACKER_PRODUCT_CODE + " TEXT NOT NULL, "
                    + DBInfo.PACKER_PRD_NAME + " TEXT NOT NULL, "
                    + DBInfo.ITEM_CODE + " TEXT NOT NULL, "
                    + DBInfo.ITEM_NAME_KR + " TEXT, "
                    + DBInfo.BRAND_CODE + " TEXT NOT NULL, "
                    + DBInfo.BARCODEGOODS + " TEXT NOT NULL, "
                    + DBInfo.BASEUNIT + " TEXT NOT NULL, "
                    + DBInfo.ZEROPOINT + " TEXT NOT NULL, "
                    + DBInfo.PACKER_PRD_CODE_FROM + " TEXT, "
                    + DBInfo.PACKER_PRD_CODE_TO + " TEXT, "
                    + DBInfo.BARCODEGOODS_FROM + " TEXT NOT NULL, "
                    + DBInfo.BARCODEGOODS_TO + " TEXT NOT NULL, "
                    + DBInfo.WEIGHT_FROM + " TEXT NOT NULL, "
                    + DBInfo.WEIGHT_TO + " TEXT NOT NULL, "
                    + DBInfo.MAKINGDATE_FROM + " TEXT , "
                    + DBInfo.MAKINGDATE_TO + " TEXT , "
                    + DBInfo.BOXSERIAL_FROM + " TEXT , "
                    + DBInfo.BOXSERIAL_TO + " TEXT , "
                    + DBInfo.STATUS + " TEXT , "
                    + DBInfo.REG_ID + " TEXT NOT NULL, "
                    + DBInfo.REG_DATE + " TEXT , "
                    + DBInfo.REG_TIME + " TEXT , "
                    + DBInfo.MEMO + " TEXT , "
                    + DBInfo.SHELF_LIFE + " TEXT"
                    + ")";
            if (Common.D) {
                Log.v(TAG, "createqueryBarcodeInfo -> " + createTable);
            }
            dbHelper.executeSql(createTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "createqueryBarcodeInfo exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    // 바코드 정보 SELECT
    public static ArrayList<Barcodes_Info> selectqueryBarcodeInfo(Context context) {//
        ArrayList<HashMap<String, String>> hMaps = new ArrayList<HashMap<String, String>>();
        ArrayList<Barcodes_Info> list_barcode_info = new ArrayList<Barcodes_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT "
                    + DBInfo.BARCODE_INFO_ID + ", "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.PACKER_PRD_NAME + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME_KR + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.BASEUNIT + ", "
                    + DBInfo.ZEROPOINT + ", "
                    + DBInfo.PACKER_PRD_CODE_FROM + ", "
                    + DBInfo.PACKER_PRD_CODE_TO + ", "
                    + DBInfo.BARCODEGOODS_FROM + ", "
                    + DBInfo.BARCODEGOODS_TO + ", "
                    + DBInfo.WEIGHT_FROM + ", "
                    + DBInfo.WEIGHT_TO + ", "
                    + DBInfo.MAKINGDATE_FROM + ", "
                    + DBInfo.MAKINGDATE_TO + ", "
                    + DBInfo.BOXSERIAL_FROM + ", "
                    + DBInfo.BOXSERIAL_TO + ", "
                    + DBInfo.STATUS + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.REG_DATE + ", "
                    + DBInfo.REG_TIME + ", "
                    + DBInfo.MEMO + ", "
                    + DBInfo.SHELF_LIFE + " FROM "
                    + DBInfo.TABLE_NAME_BARCODE_INFO
                    + " WHERE "
                    + DBInfo.BARCODEGOODS_TO + " != ''";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryBarcodeInfo -> " + sqlStr);
                Log.v(TAG, "selectqueryBarcodeInfo -> " + cursor.getCount());
            }

            Barcodes_Info bi;
            while (cursor.moveToNext()) {
                bi = new Barcodes_Info();
                bi.setBARCODE_INFO_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE_INFO_ID")), ""));
                bi.setPACKER_CLIENT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CLIENT_CODE")), ""));
                bi.setPACKER_PRODUCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
                bi.setPACKER_PRD_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_NAME")), ""));
                bi.setITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
                bi.setITEM_NAME_KR(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_NAME_KR")), ""));
                bi.setBRAND_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
                bi.setBARCODEGOODS(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
                bi.setBASEUNIT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BASEUNIT")), ""));
                bi.setZEROPOINT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ZEROPOINT")), ""));
                bi.setPACKER_PRD_CODE_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_FROM")), ""));
                bi.setPACKER_PRD_CODE_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_TO")), ""));
                bi.setBARCODEGOODS_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_FROM")), ""));
                bi.setBARCODEGOODS_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_TO")), ""));
                bi.setWEIGHT_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_FROM")), ""));
                bi.setWEIGHT_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_TO")), ""));
                bi.setMAKINGDATE_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_FROM")), ""));
                bi.setMAKINGDATE_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_TO")), ""));
                bi.setBOXSERIAL_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_FROM")), ""));
                bi.setBOXSERIAL_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_TO")), ""));
                bi.setSTATUS(Common.nullCheck(cursor.getString(cursor.getColumnIndex("STATUS")), ""));
                bi.setREG_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_ID")), ""));
                bi.setREG_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_DATE")), ""));
                bi.setREG_TIME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_TIME")), ""));
                bi.setMEMO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MEMO")), ""));
                bi.setSHELF_LIFE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SHELF_LIFE")), ""));
                list_barcode_info.add(bi);
                /*
                data.put("BARCODE_INFO_ID", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE_INFO_ID")), ""));
				data.put("PACKER_CLIENT_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CLIENT_CODE")), ""));
				data.put("BRAND_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
				data.put("PACKER_PRODUCT_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
				data.put("PACKER_PRD_NAME", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_NAME")), ""));
				data.put("ITEM_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
				data.put("ITEM_NAME_KR", Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_NAME_KR")), ""));
				data.put("BARCODEGOODS", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
				data.put("BASEUNIT", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BASEUNIT")), ""));
				data.put("ZEROPOINT", Common.nullCheck(cursor.getString(cursor.getColumnIndex("ZEROPOINT")), ""));
				data.put("PACKER_PRD_CODE_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_FROM")), ""));
				data.put("PACKER_PRD_CODE_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_TO")), ""));
				data.put("BARCODEGOODS_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_FROM")), ""));
				data.put("BARCODEGOODS_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_TO")), ""));
				data.put("WEIGHT_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_FROM")), ""));
				data.put("WEIGHT_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_TO")), ""));
				data.put("MAKINGDATE_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_FROM")), ""));
				data.put("MAKINGDATE_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_TO")), ""));
				data.put("BOXSERIAL_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_FROM")), ""));
				data.put("BOXSERIAL_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_TO")), ""));
				data.put("STATUS", Common.nullCheck(cursor.getString(cursor.getColumnIndex("STATUS")), ""));
				data.put("REG_ID", Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_ID")), ""));
				data.put("REG_DATE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_DATE")), ""));
				data.put("REG_TIME", Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_TIME")), ""));
				data.put("MEMO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MEMO")), ""));
				hMaps.add(data);
				*/
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryBarcodeInfo exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return list_barcode_info;
    }

    // 바코드 정보 SELECT
    public static ArrayList<Barcodes_Info> selectqueryBarcodeGoodsInfo(Context context) {//
        ArrayList<HashMap<String, String>> hMaps = new ArrayList<HashMap<String, String>>();
        ArrayList<Barcodes_Info> list_barcode_info = new ArrayList<Barcodes_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT "
                    + DBInfo.BARCODE_INFO_ID + ", "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.PACKER_PRD_NAME + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME_KR + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.BASEUNIT + ", "
                    + DBInfo.ZEROPOINT + ", "
                    + DBInfo.PACKER_PRD_CODE_FROM + ", "
                    + DBInfo.PACKER_PRD_CODE_TO + ", "
                    + DBInfo.BARCODEGOODS_FROM + ", "
                    + DBInfo.BARCODEGOODS_TO + ", "
                    + DBInfo.WEIGHT_FROM + ", "
                    + DBInfo.WEIGHT_TO + ", "
                    + DBInfo.MAKINGDATE_FROM + ", "
                    + DBInfo.MAKINGDATE_TO + ", "
                    + DBInfo.BOXSERIAL_FROM + ", "
                    + DBInfo.BOXSERIAL_TO + ", "
                    + DBInfo.STATUS + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.REG_DATE + ", "
                    + DBInfo.REG_TIME + ", "
                    + DBInfo.MEMO + ", "
                    + DBInfo.SHELF_LIFE + " FROM "
                    + DBInfo.TABLE_NAME_BARCODE_INFO
                    + " WHERE "
                    + DBInfo.BARCODEGOODS_TO + " != ''";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryBarcodeInfo -> " + sqlStr);
                Log.v(TAG, "selectqueryBarcodeInfo -> " + cursor.getCount());
            }

            Barcodes_Info bi;
            while (cursor.moveToNext()) {
                bi = new Barcodes_Info();
                bi.setBARCODE_INFO_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE_INFO_ID")), ""));
                bi.setPACKER_CLIENT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CLIENT_CODE")), ""));
                bi.setPACKER_PRODUCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
                bi.setPACKER_PRD_NAME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_NAME")), ""));
                bi.setITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
                bi.setITEM_NAME_KR(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_NAME_KR")), ""));
                bi.setBRAND_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
                bi.setBARCODEGOODS(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
                bi.setBASEUNIT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BASEUNIT")), ""));
                bi.setZEROPOINT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ZEROPOINT")), ""));
                bi.setPACKER_PRD_CODE_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_FROM")), ""));
                bi.setPACKER_PRD_CODE_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_TO")), ""));
                bi.setBARCODEGOODS_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_FROM")), ""));
                bi.setBARCODEGOODS_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_TO")), ""));
                bi.setWEIGHT_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_FROM")), ""));
                bi.setWEIGHT_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_TO")), ""));
                bi.setMAKINGDATE_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_FROM")), ""));
                bi.setMAKINGDATE_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_TO")), ""));
                bi.setBOXSERIAL_FROM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_FROM")), ""));
                bi.setBOXSERIAL_TO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_TO")), ""));
                bi.setSTATUS(Common.nullCheck(cursor.getString(cursor.getColumnIndex("STATUS")), ""));
                bi.setREG_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_ID")), ""));
                bi.setREG_DATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_DATE")), ""));
                bi.setREG_TIME(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_TIME")), ""));
                bi.setMEMO(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MEMO")), ""));
                bi.setSHELF_LIFE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SHELF_LIFE")), ""));
                list_barcode_info.add(bi);
                /*
                data.put("BARCODE_INFO_ID", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE_INFO_ID")), ""));
				data.put("PACKER_CLIENT_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CLIENT_CODE")), ""));
				data.put("BRAND_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
				data.put("PACKER_PRODUCT_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
				data.put("PACKER_PRD_NAME", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_NAME")), ""));
				data.put("ITEM_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
				data.put("ITEM_NAME_KR", Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_NAME_KR")), ""));
				data.put("BARCODEGOODS", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
				data.put("BASEUNIT", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BASEUNIT")), ""));
				data.put("ZEROPOINT", Common.nullCheck(cursor.getString(cursor.getColumnIndex("ZEROPOINT")), ""));
				data.put("PACKER_PRD_CODE_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_FROM")), ""));
				data.put("PACKER_PRD_CODE_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRD_CODE_TO")), ""));
				data.put("BARCODEGOODS_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_FROM")), ""));
				data.put("BARCODEGOODS_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_TO")), ""));
				data.put("WEIGHT_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_FROM")), ""));
				data.put("WEIGHT_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_TO")), ""));
				data.put("MAKINGDATE_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_FROM")), ""));
				data.put("MAKINGDATE_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_TO")), ""));
				data.put("BOXSERIAL_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_FROM")), ""));
				data.put("BOXSERIAL_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_TO")), ""));
				data.put("STATUS", Common.nullCheck(cursor.getString(cursor.getColumnIndex("STATUS")), ""));
				data.put("REG_ID", Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_ID")), ""));
				data.put("REG_DATE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_DATE")), ""));
				data.put("REG_TIME", Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_TIME")), ""));
				data.put("MEMO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MEMO")), ""));
				hMaps.add(data);
				*/
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryBarcodeInfo exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return list_barcode_info;
    }

    // 바코드정보 INSERT
    public static boolean insertqueryBarcodeInfo(Context context, Barcodes_Info bi) {// 집하입고 INSERT
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        boolean result;

        try {
            String sqlStr = "INSERT INTO "
                    + DBInfo.TABLE_NAME_BARCODE_INFO + " ("
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.PACKER_PRD_NAME + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME_KR + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.BASEUNIT + ", "
                    + DBInfo.ZEROPOINT + ", "
                    + DBInfo.PACKER_PRD_CODE_FROM + ", "
                    + DBInfo.PACKER_PRD_CODE_TO + ", "
                    + DBInfo.BARCODEGOODS_FROM + ", "
                    + DBInfo.BARCODEGOODS_TO + ", "
                    + DBInfo.WEIGHT_FROM + ", "
                    + DBInfo.WEIGHT_TO + ", "
                    + DBInfo.MAKINGDATE_FROM + ", "
                    + DBInfo.MAKINGDATE_TO + ", "
                    + DBInfo.BOXSERIAL_FROM + ", "
                    + DBInfo.BOXSERIAL_TO + ", "
                    + DBInfo.STATUS + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.SHELF_LIFE
                    + ") VALUES('"
                    + Common.nullCheck(bi.getPACKER_CLIENT_CODE(), "") + "','"
                    + Common.nullCheck(bi.getBRAND_CODE(), "") + "','"
                    + Common.nullCheck(bi.getPACKER_PRODUCT_CODE(), "") + "','"
                    + Common.nullCheck(bi.getPACKER_PRD_NAME(), "") + "','"
                    + Common.nullCheck(bi.getITEM_CODE(), "") + "','"
                    + Common.nullCheck(bi.getITEM_NAME_KR(), "") + "','"
                    + Common.nullCheck(bi.getBARCODEGOODS(), "") + "','"
                    + Common.nullCheck(bi.getBASEUNIT(), "") + "','"
                    + Common.nullCheck(bi.getZEROPOINT(), "") + "','"
                    + Common.nullCheck(bi.getPACKER_PRD_CODE_FROM(), "") + "','"
                    + Common.nullCheck(bi.getPACKER_PRD_CODE_TO(), "") + "','"
                    + Common.nullCheck(bi.getBARCODEGOODS_FROM(), "") + "','"
                    + Common.nullCheck(bi.getBARCODEGOODS_TO(), "") + "','"
                    + Common.nullCheck(bi.getWEIGHT_FROM(), "") + "','"
                    + Common.nullCheck(bi.getWEIGHT_TO(), "") + "','"
                    + Common.nullCheck(bi.getMAKINGDATE_FROM(), "") + "','"
                    + Common.nullCheck(bi.getMAKINGDATE_TO(), "") + "','"
                    + Common.nullCheck(bi.getBOXSERIAL_FROM(), "") + "','"
                    + Common.nullCheck(bi.getBOXSERIAL_TO(), "") + "','"
                    + Common.nullCheck(bi.getSTATUS(), "") + "','"
                    + Common.nullCheck(bi.getREG_ID(), "") + "','"
                    + Common.nullCheck(bi.getSHELF_LIFE(), "") + "')";
            if (Common.D) {
                Log.d(TAG, "insertqueryBarcodeInfo -> " + sqlStr);
            }
            dbHelper.executeSql(sqlStr);
            result = true;
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "insertqueryBarcodeInfo exception -> " + e.getMessage().toString());
            }
            dbHelper.close();
            result = false;
            return result;
        }

        dbHelper.close();
        return result;
    }

    // 바코드정보 UPDATE
    public static void updatequeryBarcodeInfo(Context context, HashMap<String, String> hTemp) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "UPDATE "
                    + DBInfo.TABLE_NAME_BARCODE_INFO
                    + " SET "
                    + DBInfo.BRAND_CODE + " = '" + hTemp.get("BRAND_CODE") + "', "
                    + DBInfo.PACKER_PRODUCT_CODE + " = '" + hTemp.get("PACKER_PRODUCT_CODE") + "', "
                    + DBInfo.PACKER_PRD_NAME + " = '" + hTemp.get("PACKER_PRD_NAME") + "', "
                    + DBInfo.ITEM_CODE + " = '" + hTemp.get("ITEM_CODE") + "', "
                    + DBInfo.ITEM_NAME_KR + " = '" + hTemp.get("ITEM_NAME_KR") + "', "
                    + DBInfo.BARCODEGOODS + " = '" + hTemp.get("BARCODEGOODS") + "', "
                    + DBInfo.BASEUNIT + " = '" + hTemp.get("BASEUNIT") + "', "
                    + DBInfo.ZEROPOINT + " = '" + hTemp.get("ZEROPOINT") + "', "
                    + DBInfo.PACKER_PRD_CODE_FROM + " = '" + hTemp.get("PACKER_PRD_CODE_FROM") + "', "
                    + DBInfo.PACKER_PRD_CODE_TO + " = '" + hTemp.get("PACKER_PRD_CODE_TO") + "', "
                    + DBInfo.BARCODEGOODS_FROM + " = '" + hTemp.get("BARCODEGOODS_FROM") + "', "
                    + DBInfo.BARCODEGOODS_TO + " = '" + hTemp.get("BARCODEGOODS_TO") + "', "
                    + DBInfo.WEIGHT_FROM + " = '" + hTemp.get("WEIGHT_FROM") + "', "
                    + DBInfo.WEIGHT_TO + " = '" + hTemp.get("WEIGHT_TO") + "', "
                    + DBInfo.MAKINGDATE_FROM + " = '" + hTemp.get("MAKINGDATE_FROM") + "', "
                    + DBInfo.MAKINGDATE_TO + " = '" + hTemp.get("MAKINGDATE_TO") + "', "
                    + DBInfo.BOXSERIAL_FROM + " = '" + hTemp.get("BOXSERIAL_FROM") + "', "
                    + DBInfo.BOXSERIAL_TO + " = '" + hTemp.get("BOXSERIAL_TO") + "', "
                    + DBInfo.STATUS + " = '" + hTemp.get("STATUS") + "' "
                    + "WHERE PACKER_PRODUCT_CODE = '" + hTemp.get("PACKER_PRODUCT_CODE") + "' AND "
                    + "BRAND_CODE = '" + hTemp.get("BRAND_CODE") + "'";
            if (Common.D) {
                Log.v(TAG, "updatequeryBarcodeInfo -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);
        } catch (Exception e) {
            Log.d(TAG, "updatequeryBarcodeInfo exception -> " + e.getMessage().toString());
        }

        dbHelper.close();
    }

    // 바코드정보 DELETE
    public static void deletequeryBarcodeInfo(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "DELETE FROM " + DBInfo.TABLE_NAME_BARCODE_INFO;

            if (Common.D) {
                Log.v(TAG, "deletequeryBarcodeInfo -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);
            deleteTable = "DELETE FROM sqlite_sequence WHERE name = '" + DBInfo.TABLE_NAME_BARCODE_INFO + "'";
            dbHelper.executeSql(deleteTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "deletequeryBarcodeInfo exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    // 상품 계근 CREATE
    public static void createqueryGoodsWet(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS "
                    + DBInfo.TABLE_NAME_GOODS_WET + " ("
                    + DBInfo.GOODS_WET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DBInfo.GI_D_ID + " TEXT NOT NULL, "
                    + DBInfo.WEIGHT + " TEXT NOT NULL, "
                    + DBInfo.WEIGHT_UNIT + " TEXT NOT NULL, "
                    + DBInfo.PACKER_PRODUCT_CODE + " TEXT NOT NULL, "
                    + DBInfo.BARCODE + " TEXT, "
                    + DBInfo.PACKER_CLIENT_CODE + " TEXT NOT NULL, "
                    + DBInfo.MAKINGDATE + " TEXT, "
                    + DBInfo.BOXSERIAL + " TEXT, "
                    + DBInfo.BOX_CNT + " INTEGER NOT NULL, "
                    + DBInfo.EMARTITEM_CODE + " TEXT, "
                    + DBInfo.EMARTITEM + " TEXT, "
                    + DBInfo.ITEM_CODE + " TEXT, "
                    + DBInfo.BRAND_CODE + " TEXT, "
                    + DBInfo.REG_ID + " TEXT, "
                    + DBInfo.REG_DATE + " TEXT, "
                    + DBInfo.REG_TIME + " TEXT NOT NULL, "
                    + DBInfo.SAVE_TYPE + " TEXT NOT NULL, "
                    + DBInfo.MEMO + " TEXT, "
                    + DBInfo.DUPLICATE + " TEXT, "
                    + DBInfo.CLIENT_TYPE + " TEXT, "
                    + DBInfo.BOX_ORDER + " Integer DEFAULT 0"
                    + ")";
            if (Common.D) {
                Log.v(TAG, "createqueryGoodsWet -> " + createTable);
            }
            dbHelper.executeSql(createTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "createqueryGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    // 계근 상품 작업내역 SELECT :: GOODS_WET
    public static ArrayList<Goodswets_Info> selectqueryGoodsWet(Context context, String gi_d_id, String packer_product_code, String client_code) {//
        ArrayList<Goodswets_Info> list_gi_info = new ArrayList<Goodswets_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr;
            sqlStr = "SELECT "
                    + DBInfo.GOODS_WET_ID + ", "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.WEIGHT + ", "
                    + DBInfo.WEIGHT_UNIT + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE + ", "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.BOXSERIAL + ", "
                    + DBInfo.BOX_CNT + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.SAVE_TYPE + ", "
                    + DBInfo.MAKINGDATE + ", "
                    + DBInfo.BOX_ORDER + ", "
                    + DBInfo.DUPLICATE + " "
                    + " FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET
                    + " WHERE "
                    + "GI_D_ID = '" + gi_d_id + "' AND "
                    + "PACKER_PRODUCT_CODE = '" + packer_product_code + "' "
                    + "ORDER BY BOX_CNT DESC";

            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryGoodsWet -> " + sqlStr);
                Log.v(TAG, "selectqueryGoodsWet -> " + cursor.getCount());
            }

            Goodswets_Info gi;
            while (cursor.moveToNext()) {
                gi = new Goodswets_Info();
                gi.setGI_D_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_D_ID")), ""));
                gi.setWEIGHT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT")), ""));
                gi.setWEIGHT_UNIT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_UNIT")), ""));
                gi.setPACKER_PRODUCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
                gi.setBARCODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE")), ""));
                gi.setPACKER_CLIENT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CLIENT_CODE")), ""));
                gi.setBOXSERIAL(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL")), ""));
                gi.setBOX_CNT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOX_CNT")), ""));
                gi.setEMARTITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM_CODE")), ""));
                gi.setEMARTITEM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM")), ""));
                gi.setREG_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_ID")), ""));
                gi.setSAVE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SAVE_TYPE")), ""));
                gi.setMAKINGDATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE")), ""));
                gi.setBOX_ORDER(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOX_ORDER")), ""));
                gi.setDUPLICATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("DUPLICATE")), ""));
                list_gi_info.add(0, gi);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return list_gi_info;
    }

    // 전송할 계근 목록 SELECT :: GOODS_WET
    public static ArrayList<Goodswets_Info> selectquerySendGoodsWet(Context context, String qry_where) {//
        ArrayList<Goodswets_Info> list_gi_info = new ArrayList<Goodswets_Info>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr;
            sqlStr = "SELECT "
                    + DBInfo.GOODS_WET_ID + ", "
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.WEIGHT + ", "
                    + DBInfo.WEIGHT_UNIT + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE + ", "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.MAKINGDATE + ", "
                    + DBInfo.BOXSERIAL + ", "
                    + DBInfo.BOX_CNT + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.SAVE_TYPE + ", "
                    + DBInfo.DUPLICATE + ", "
                    + DBInfo.CLIENT_TYPE + ", "
                    + DBInfo.BOX_ORDER + " "
                    + " FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET
                    + " WHERE "
                    + qry_where         // 검색조건은 GI_D_ID
                    + " ORDER BY GI_D_ID ASC, BOX_CNT ASC";

            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectquerySendGoodsWet -> " + sqlStr);
                Log.v(TAG, "selectquerySendGoodsWet -> " + cursor.getCount());
            }

            Goodswets_Info gi;
            while (cursor.moveToNext()) {
                gi = new Goodswets_Info();
                gi.setGI_D_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("GI_D_ID")), ""));
                gi.setWEIGHT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT")), ""));
                gi.setWEIGHT_UNIT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_UNIT")), ""));
                gi.setPACKER_PRODUCT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_PRODUCT_CODE")), ""));
                gi.setBARCODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODE")), ""));
                gi.setPACKER_CLIENT_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CLIENT_CODE")), ""));
                gi.setMAKINGDATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE")), ""));
                gi.setBOXSERIAL(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL")), ""));
                gi.setBOX_CNT(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOX_CNT")), ""));
                gi.setEMARTITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM_CODE")), ""));
                gi.setEMARTITEM(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EMARTITEM")), ""));
                gi.setITEM_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("ITEM_CODE")), ""));
                gi.setBRAND_CODE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BRAND_CODE")), ""));
                gi.setREG_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("REG_ID")), ""));
                gi.setSAVE_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("SAVE_TYPE")), ""));
                gi.setDUPLICATE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("DUPLICATE")), ""));
                gi.setCLIENT_TYPE(Common.nullCheck(cursor.getString(cursor.getColumnIndex("CLIENT_TYPE")), ""));
                gi.setBOX_ORDER(Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOX_ORDER")), ""));
                list_gi_info.add(gi);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectquerySendGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return list_gi_info;
    }

    public static String[] selectqueryListGoodsWetInfo(Context context, String gi_d_id, String pp_code, String client_code) {
        String[] row_info = new String[4];
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr;
            sqlStr = "SELECT "
                    + " sum(" + DBInfo.WEIGHT + ") as WEIGHT, "
                    + " count(" + DBInfo.GI_D_ID + ") as COUNT,"
                    + " count(" + DBInfo.SAVE_TYPE + "='Y') as SEND_Y,"
                    + " count(" + DBInfo.SAVE_TYPE + "='F') as SEND_N"
                    //+ " max(" + DBInfo.WH_AREA + ") as WH_AREA"
                    + " FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET
                    + " WHERE "
                    + "GI_D_ID = '" + gi_d_id + "' AND "
                    + "PACKER_PRODUCT_CODE = '" + pp_code + "' ";

            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectqueryListGoodsWetInfo -> " + sqlStr);
                Log.v(TAG, "selectqueryListGoodsWetInfo -> " + cursor.getCount());
            }

            while (cursor.moveToNext()) {
                row_info[0] = Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT")), "0");
                row_info[1] = Common.nullCheck(cursor.getString(cursor.getColumnIndex("COUNT")), "0");
                row_info[2] = Common.nullCheck(cursor.getString(cursor.getColumnIndex("SEND_Y")), "0");
                row_info[3] = Common.nullCheck(cursor.getString(cursor.getColumnIndex("SEND_N")), "0");
            }

            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectqueryGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return row_info;
    }

    // 상품정보 중복체크
    public static boolean duplicatequeryGoodsWet_check(Context context, String barcode) {//
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();
        boolean duplicate = false;
        try {
            Cursor cursor;
            String sqlStr = "SELECT "
                    + DBInfo.BARCODE
                    + " FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET
                    + " WHERE BARCODE = '" + barcode + "'";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "duplicatequeryGoodsWet_check -> " + sqlStr);
                Log.v(TAG, "duplicatequeryGoodsWet_check -> " + cursor.getCount());
                Log.v(TAG, "duplicatequeryGoodsWet_check -> " + cursor.getColumnNames());
            }
            if (cursor.getCount() == 0) {
                duplicate = false;
            } else {
                duplicate = true;
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "duplicatequeryGoodsWet_check exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return duplicate;
    }

    // 계근정보 중복체크
    public static boolean duplicatequeryGoodsWet(Context context, String barcode, String gi_d_id, String pp_code) {//
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();
        boolean duplicate = false;
        try {
            Cursor cursor;
            String sqlStr = "SELECT "
                    + DBInfo.BARCODE
                    + " FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET
                    + " WHERE BARCODE = '" + barcode
                    + "' AND"
                    + " GI_D_ID = '" + gi_d_id
                    + "' AND"
                    + " PACKER_PRODUCT_CODE = '" + pp_code + "'";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "duplicatequeryGoodsWet -> " + sqlStr);
                Log.v(TAG, "duplicatequeryGoodsWet -> " + cursor.getCount());
            }
            if (cursor.getCount() == 0) {
                duplicate = false;
            } else {
                duplicate = true;
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "duplicatequeryGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return duplicate;
    }

    // 계근정보 INSERT
    public static boolean insertqueryGoodsWet(Context context, Goodswets_Info gi) {// 집하입고 INSERT
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        boolean result = false;
        long now = System.currentTimeMillis();
        Date datetime = new Date(now);
        String dateStr = dateformat.format(datetime);
        String timeStr = timeformat.format(datetime);

        try {
            String sqlStr = "INSERT INTO "
                    + DBInfo.TABLE_NAME_GOODS_WET + " ("
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.WEIGHT + ", "
                    + DBInfo.WEIGHT_UNIT + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE + ", "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.MAKINGDATE + ", "
                    + DBInfo.BOXSERIAL + ", "
                    + DBInfo.BOX_CNT + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.REG_DATE + ", "
                    + DBInfo.REG_TIME + ", "
                    + DBInfo.BOX_ORDER + ", "
                    + DBInfo.SAVE_TYPE + ", "
                    + DBInfo.DUPLICATE
                    + ") VALUES('"
                    + Common.nullCheck(gi.getGI_D_ID(), "") + "','"
                    + Common.nullCheck(gi.getWEIGHT(), "") + "','"
                    + Common.nullCheck(gi.getWEIGHT_UNIT(), "") + "','"
                    + Common.nullCheck(gi.getPACKER_PRODUCT_CODE(), "") + "','"
                    + Common.nullCheck(gi.getBARCODE(), "") + "','"
                    + Common.nullCheck(gi.getPACKER_CLIENT_CODE(), "") + "','"
                    + Common.nullCheck(gi.getMAKINGDATE(), "") + "','"
                    + Common.nullCheck(gi.getBOXSERIAL(), "") + "',"
                    + Common.nullCheck(String.valueOf(gi.getBOX_CNT()), "") + ",'"        // integer
                    + Common.nullCheck(gi.getEMARTITEM_CODE(), "") + "','"
                    + Common.nullCheck(gi.getEMARTITEM(), "") + "','"
                    + Common.nullCheck(gi.getITEM_CODE(), "") + "','"
                    + Common.nullCheck(gi.getBRAND_CODE(), "") + "','"
                    + Common.nullCheck(gi.getREG_ID(), "") + "','"
                    + dateStr + "','"
                    + timeStr + "','"
                    + Common.nullCheck(gi.getBOX_ORDER(), "") + "','"
                    + Common.nullCheck(gi.getSAVE_TYPE(), "F") + "','"
                    + Common.nullCheck(gi.getDUPLICATE(), "F") + "')";
            if (Common.D) {
                Log.d(TAG, "insertqueryGoodsWet -> " + sqlStr);
            }
            dbHelper.executeSql(sqlStr);
            result = true;
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "insertqueryGoodsWet exception -> " + e.getMessage().toString());
            }
            dbHelper.close();
            result = false;
            return result;
        } finally {
            dbHelper.close();
        }

        return result;
    }

    public static boolean insertqueryGoodsWetHomeplus(Context context, Goodswets_Info gi, int maxBoxOrder) {// 집하입고 INSERT
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        boolean result = false;
        long now = System.currentTimeMillis();
        Date datetime = new Date(now);
        String dateStr = dateformat.format(datetime);
        String timeStr = timeformat.format(datetime);

        try {
            String sqlStr = "INSERT INTO "
                    + DBInfo.TABLE_NAME_GOODS_WET + " ("
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.WEIGHT + ", "
                    + DBInfo.WEIGHT_UNIT + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE + ", "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.MAKINGDATE + ", "
                    + DBInfo.BOXSERIAL + ", "
                    + DBInfo.BOX_CNT + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.REG_DATE + ", "
                    + DBInfo.REG_TIME + ", "
                    + DBInfo.SAVE_TYPE + ", "
                    + DBInfo.DUPLICATE + ", "
                    + DBInfo.CLIENT_TYPE + ", "
                    + DBInfo.BOX_ORDER
                    + ") VALUES('"
                    + Common.nullCheck(gi.getGI_D_ID(), "") + "','"
                    + Common.nullCheck(gi.getWEIGHT(), "") + "','"
                    + Common.nullCheck(gi.getWEIGHT_UNIT(), "") + "','"
                    + Common.nullCheck(gi.getPACKER_PRODUCT_CODE(), "") + "','"
                    + Common.nullCheck(gi.getBARCODE(), "") + "','"
                    + Common.nullCheck(gi.getPACKER_CLIENT_CODE(), "") + "','"
                    + Common.nullCheck(gi.getMAKINGDATE(), "") + "','"
                    + Common.nullCheck(gi.getBOXSERIAL(), "") + "',"
                    + Common.nullCheck(String.valueOf(gi.getBOX_CNT()), "") + ",'"        // integer
                    + Common.nullCheck(gi.getEMARTITEM_CODE(), "") + "','"
                    + Common.nullCheck(gi.getEMARTITEM(), "") + "','"
                    + Common.nullCheck(gi.getITEM_CODE(), "") + "','"
                    + Common.nullCheck(gi.getBRAND_CODE(), "") + "','"
                    + Common.nullCheck(gi.getREG_ID(), "") + "','"
                    + dateStr + "','"
                    + timeStr + "','"
                    + Common.nullCheck(gi.getSAVE_TYPE(), "F") + "','"
                    + Common.nullCheck(gi.getDUPLICATE(), "F") + "','"
                    + "06" + "','"  //홈플러스 채널코드 06
                    + maxBoxOrder
                    + "')";
            if (Common.D) {
                Log.d(TAG, "insertqueryGoodsWetHomeplus -> " + sqlStr);
            }
            dbHelper.executeSql(sqlStr);
            result = true;
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "insertqueryGoodsWetHomeplus exception -> " + e.getMessage().toString());
            }
            dbHelper.close();
            result = false;
            return result;
        } finally {
            dbHelper.close();
        }

        return result;
    }

    public static boolean insertqueryGoodsWetLotte(Context context, Goodswets_Info gi, int lotte_TryCount) {// 집하입고 롯데 INSERT
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        boolean result = false;
        long now = System.currentTimeMillis();
        Date datetime = new Date(now);
        String dateStr = dateformat.format(datetime);
        String timeStr = timeformat.format(datetime);

        try {
            String sqlStr = "INSERT INTO "
                    + DBInfo.TABLE_NAME_GOODS_WET + " ("
                    + DBInfo.GI_D_ID + ", "
                    + DBInfo.WEIGHT + ", "
                    + DBInfo.WEIGHT_UNIT + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.BARCODE + ", "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.MAKINGDATE + ", "
                    + DBInfo.BOXSERIAL + ", "
                    + DBInfo.BOX_CNT + ", "
                    + DBInfo.EMARTITEM_CODE + ", "
                    + DBInfo.EMARTITEM + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.REG_DATE + ", "
                    + DBInfo.REG_TIME + ", "
                    + DBInfo.SAVE_TYPE + ", "
                    + DBInfo.DUPLICATE + ", "
                    + DBInfo.CLIENT_TYPE + ", "
                    + DBInfo.BOX_ORDER
                    + ") VALUES('"
                    + Common.nullCheck(gi.getGI_D_ID(), "") + "','"
                    + Common.nullCheck(gi.getWEIGHT(), "") + "','"
                    + Common.nullCheck(gi.getWEIGHT_UNIT(), "") + "','"
                    + Common.nullCheck(gi.getPACKER_PRODUCT_CODE(), "") + "','"
                    + Common.nullCheck(gi.getBARCODE(), "") + "','"
                    + Common.nullCheck(gi.getPACKER_CLIENT_CODE(), "") + "','"
                    + Common.nullCheck(gi.getMAKINGDATE(), "") + "','"
                    + Common.nullCheck(gi.getBOXSERIAL(), "") + "',"
                    + Common.nullCheck(String.valueOf(gi.getBOX_CNT()), "") + ",'"        // integer
                    + Common.nullCheck(gi.getEMARTITEM_CODE(), "") + "','"
                    + Common.nullCheck(gi.getEMARTITEM(), "") + "','"
                    + Common.nullCheck(gi.getITEM_CODE(), "") + "','"
                    + Common.nullCheck(gi.getBRAND_CODE(), "") + "','"
                    + Common.nullCheck(gi.getREG_ID(), "") + "','"
                    + dateStr + "','"
                    + timeStr + "','"
                    + Common.nullCheck(gi.getSAVE_TYPE(), "F") + "','"
                    + Common.nullCheck(gi.getDUPLICATE(), "F") + "','"
                    + "07" + "','"  //롯데 채널코드 07
                    + lotte_TryCount
                    + "')";
            if (Common.D) {
                Log.d(TAG, "insertqueryGoodsWetLotte -> " + sqlStr);
            }
            dbHelper.executeSql(sqlStr);
            result = true;
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "insertqueryGoodsWetLotte exception -> " + e.getMessage().toString());
            }
            dbHelper.close();
            result = false;
            return result;
        } finally {
            dbHelper.close();
        }

        return result;
    }


    // 계근정보 UPDATE
    public static boolean updatequeryGoodsWet(Context context, String gi_d_id, String barcode, String box_cnt) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "UPDATE "
                    + DBInfo.TABLE_NAME_GOODS_WET
                    + " SET "
                    + DBInfo.SAVE_TYPE + " = 'Y' "
                    + "WHERE GI_D_ID = '" + gi_d_id
                    + "' AND "
                    + "BARCODE = '" + barcode
                    + "' AND "
                    + "BOX_CNT = " + box_cnt;
            if (Common.D) {
                Log.v(TAG, "updatequeryGoodsWet -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);
            return true;
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "updatequeryGoodsWet exception -> " + e.getMessage().toString());
            }
            return false;
        } finally {
            dbHelper.close();
        }
    }

    // 미전송 선택 계근정보 DELETE
    public static void deletequerySelectGoodsWet(Context context, String gi_d_id, String barcode, int box_cnt) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "DELETE FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET + " "
                    + "WHERE BARCODE = '" + barcode
                    + "' AND "
                    + "GI_D_ID = '" + gi_d_id
                    + "' AND "
                    + "BOX_CNT = " + box_cnt
                    + " AND "
                    + "SAVE_TYPE = 'F'";
            if (Common.D) {
                Log.v(TAG, "deletequerySelectGoodsWet -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);

            // BOX_CNT 새로고침
            String updateBoxCnt = "UPDATE " + DBInfo.TABLE_NAME_GOODS_WET + " "
                    + "SET BOX_CNT = BOX_CNT-1 "
                    + "WHERE BOX_CNT > " + box_cnt;
            dbHelper.executeSql(updateBoxCnt);
            if (Common.D) {
                Log.v(TAG, "deletequerySelectGoodsWet_boxcnt refresh -> " + updateBoxCnt);
            }
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "deletequerySelectGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    // 계근정보 DELETE
    public static void deletequeryGoodsWet(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "DELETE FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET
                    + " WHERE SAVE_TYPE = 'Y'";
            if (Common.D) {
                Log.v(TAG, "deletequeryGoodsWet -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);
            deleteTable = "DELETE FROM sqlite_sequence WHERE name = '" + DBInfo.TABLE_NAME_GOODS_WET + "'";
            dbHelper.executeSql(deleteTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "deletequeryGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    // 계근정보 DELETE
    public static void deletequeryAllGoodsWet(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "DELETE FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET;
            if (Common.D) {
                Log.v(TAG, "deletequeryAllGoodsWet -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);
            deleteTable = "DELETE FROM sqlite_sequence WHERE name = '" + DBInfo.TABLE_NAME_GOODS_WET + "'";
            dbHelper.executeSql(deleteTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "deletequeryAllGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    //	출하대상에서 바코드정보 SELECT
    public static ArrayList<HashMap<String, String>> selectquerySearchBarcodeInfo(Context context, String packer_product_code, String brand_code) {//
        ArrayList<HashMap<String, String>> hMaps = new ArrayList<HashMap<String, String>>();
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr = "SELECT "
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.BASEUNIT + ", "
                    + DBInfo.ZEROPOINT + ", "
                    + DBInfo.BARCODEGOODS_FROM + ", "
                    + DBInfo.BARCODEGOODS_TO + ", "
                    + DBInfo.WEIGHT_FROM + ", "
                    + DBInfo.WEIGHT_TO + ", "
                    + DBInfo.MAKINGDATE_FROM + ", "
                    + DBInfo.MAKINGDATE_TO + ", "
                    + DBInfo.BOXSERIAL_FROM + ", "
                    + DBInfo.BOXSERIAL_TO + " FROM "
                    + DBInfo.TABLE_NAME_BARCODE_INFO
                    + " WHERE PACKER_PRODUCT_CODE = '" + packer_product_code
                    + "' AND "
                    + "BRAND_CODE = '" + brand_code + "'";
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectquerySearchBarcodeInfo -> " + sqlStr);
                Log.v(TAG, "selectquerySearchBarcodeInfo -> " + cursor.getCount());
            }

            HashMap<String, String> data;
            while (cursor.moveToNext()) {
                data = new HashMap<String, String>();
                data.put("PACKER_CLIENT_CODE", Common.nullCheck(cursor.getString(cursor.getColumnIndex("PACKER_CLIENT_CODE")), ""));
                data.put("BARCODEGOODS", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS")), ""));
                data.put("BASEUNIT", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BASEUNIT")), ""));
                data.put("ZEROPOINT", Common.nullCheck(cursor.getString(cursor.getColumnIndex("ZEROPOINT")), ""));
                data.put("BARCODEGOODS_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_FROM")), ""));
                data.put("BARCODEGOODS_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BARCODEGOODS_TO")), ""));
                data.put("WEIGHT_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_FROM")), ""));
                data.put("WEIGHT_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("WEIGHT_TO")), ""));
                data.put("MAKINGDATE_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_FROM")), ""));
                data.put("MAKINGDATE_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("MAKINGDATE_TO")), ""));
                data.put("BOXSERIAL_FROM", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_FROM")), ""));
                data.put("BOXSERIAL_TO", Common.nullCheck(cursor.getString(cursor.getColumnIndex("BOXSERIAL_TO")), ""));
                hMaps.add(data);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectquerySearchBarcodeInfo exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return hMaps;
    }

    // 출하대상 수정사항만 변경하기
    public static boolean refreshShipmentList(Context context, ArrayList<String> list_delete, ArrayList<Shipments_Info> list_insert) {
        Context mContext = context;
        boolean result;
        try {
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.open();
            if (list_delete.size() > 0) {
                String delete_where = "";
                for (int i = 0; i < list_delete.size(); i++) {
                    if (i == list_delete.size() - 1) {
                        delete_where += " GI_D_ID = '" + list_delete.get(i) + "'";
                    } else {
                        delete_where += "GI_D_ID = '" + list_delete.get(i) + "' AND ";
                    }
                }

                String delete_qry = "DELETE FROM " + DBInfo.TABLE_NAME_SHIPMENT
                        + " WHERE "
                        + delete_where;
                Log.v(TAG, "refreshShipmentList deleteQuery -> " + delete_qry);
                dbHelper.executeSql(delete_qry);
            }

            if (list_insert.size() > 0) {
                for (int i = 0; i < list_insert.size(); i++) {
                    insertqueryShipment(mContext, list_insert.get(i), "F");
                }
            }

            result = true;

            return result;
        } catch (Exception ex) {
            result = false;
            return result;
        }
    }

    public static int selectMaxBoxOrder(Context context) {

        long now = System.currentTimeMillis();
        Date datetime = new Date(now);
        String dateStr = dateformat.format(datetime);
        String timeStr = timeformat.format(datetime);
        int maxBoxN1 = 0;
        String[] row_info = new String[0];
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();

        try {
            Cursor cursor;
            String sqlStr;
            sqlStr =
                    " SELECT IFNULL(MAX(BOX_ORDER),0) + 1 CNT "
                            + " FROM "
                            + DBInfo.TABLE_NAME_GOODS_WET
                            + " WHERE "
                            + "CLIENT_TYPE = '06' AND "
                            + "REG_DATE = '"+ dateStr + "' "
            ;
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectMaxBoxOrder -> " + sqlStr);
                Log.v(TAG, "selectMaxBoxOrder -> " + cursor.getCount());
            }

            while (cursor.moveToNext()) {
                maxBoxN1 = Log.e(TAG, "=======================db 커서 값 확인 확인 222##========================="+ cursor.getInt(0));
                maxBoxN1 = cursor.getInt(0);
            }

            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "selectMaxBoxOrder exception -> " + e.getMessage().toString());
            }
        }
        mDbHelper.close();
        return maxBoxN1;
    }

    // 생산계근 계산 바코드 중복 확인 테이블 CREATE
    public static void createqueryGoodsWetProductionCalc(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS "
                    + DBInfo.TABLE_NAME_GOODS_WET_PRODUCTION_CALC + " ("
                    + DBInfo.BARCODE + " TEXT "
                    + ")";
            if (Common.D) {
                Log.v(TAG, "createqueryGoodsWetProductionCalc -> " + createTable);
            }
            dbHelper.executeSql(createTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "createqueryGoodsWetProductionCalc exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

    // 생산계근 계산기 insert
    public static boolean insertGoodsWetProductionCalc(Context context, String msg) {// 집하입고 INSERT
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        boolean result;

        try {
            String sqlStr = "INSERT INTO " + DBInfo.TABLE_NAME_GOODS_WET_PRODUCTION_CALC + " ("
                    + DBInfo.BARCODE
                    + ") VALUES('"
                    + msg + "')";
            if (Common.D) {
                Log.d(TAG, "insertGoodsWetProductionCalc -> " + sqlStr);
            }
            dbHelper.executeSql(sqlStr);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.v(TAG, "insertqueryShipment exception -> " + e.getMessage().toString());
            }
            dbHelper.close();
            result = false;
            return result;
        }

        dbHelper.close();
        return result;
    }

    // 계근정보 중복체크
    public static String selectGoodsWetProductionCalc(Context context, String msg){
        DBHelper mDbHelper = new DBHelper(context);
        mDbHelper.open();
        //String duplicate = "";
        String count = "";

        try {
            Cursor cursor;
            String sqlStr = "SELECT COUNT(1) FROM "
                    + DBInfo.TABLE_NAME_GOODS_WET_PRODUCTION_CALC
                    + " WHERE BARCODE = '" + msg + "'"
                    ;
            cursor = mDbHelper.selectSql(sqlStr);
            if (Common.D) {
                Log.v(TAG, "selectGoodsWetProductionCalc -> " + sqlStr);
                Log.v(TAG, "selectGoodsWetProductionCalc cursor -> " + cursor.getCount());
            }
            while (cursor.moveToNext()) {
                //maxBoxN1 = Log.e(TAG, "=======================db 커서 값 확인 확인 222##========================="+ cursor.getString(0));
                count = cursor.getString(0);
            }
            cursor.close();
        } catch (Exception e) {
            if (Common.D) {
                Log.v(TAG, "duplicatequeryGoodsWet exception -> " + e.getMessage().toString());
            }
        }

        mDbHelper.close();
        return count;
    }

    public static void deleteGoodsWetProductionCalc(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteTable = "DELETE FROM " + DBInfo.TABLE_NAME_GOODS_WET_PRODUCTION_CALC;

            if (Common.D) {
                Log.v(TAG, "deleteGoodsWetProductionCalc -> " + deleteTable);
            }
            dbHelper.executeSql(deleteTable);
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "deleteGoodsWetProductionCalc exception -> " + e.getMessage().toString());
            }
        }

        dbHelper.close();
    }

}
