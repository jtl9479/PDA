package com.rgbsolution.highland_emart.scanner;

public class Constants {

	public static final String ACTION_BARCODE_OPEN = "kr.co.bluebird.android.bbapi.action.BARCODE_OPEN";
	public static final String ACTION_BARCODE_CLOSE = "kr.co.bluebird.android.bbapi.action.BARCODE_CLOSE";
	public static final String ACTION_BARCODE_SET_TRIGGER = "kr.co.bluebird.android.bbapi.action.BARCODE_SET_TRIGGER";	
    public static final String ACTION_BARCODE_SET_DEFAULT_PROFILE = "kr.co.bluebird.android.bbapi.action.BARCODE_SET_DEFAULT_PROFILE";
	public static final String ACTION_BARCODE_SETTING_CHANGED = "kr.co.bluebird.android.bbapi.action.BARCODE_SETTING_CHANGED";
	public static final String ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS = "kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_SUCCESS";
	public static final String ACTION_BARCODE_CALLBACK_REQUEST_FAILED = "kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_FAILED";
	public static final String ACTION_BARCODE_CALLBACK_DECODING_DATA = "kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_DECODING_DATA";
	public static final String ACTION_MDM_BARCODE_SET_SYMBOLOGY = "kr.co.bluebird.android.bbapi.action.MDM_BARCODE_SET_SYMBOLOGY";
	public static final String ACTION_MDM_BARCODE_SET_MODE = "kr.co.bluebird.android.bbapi.action.MDM_BARCODE_SET_MODE";
	public static final String ACTION_MDM_BARCODE_SET_DEFAULT = "kr.co.bluebird.android.bbapi.action.MDM_BARCODE_SET_DEFAULT";
	
	//default profile setting done.
	public static final String ACTION_BARCODE_CALLBACK_DEFAULT_PROFILE_SETTING_COMPLETE = "kr.co.bluebird.android.bbapi.action.BARCODE_DEFAULT_PROFILE_SETTING_COMPLETE";
	//request barcode status
	public static final String ACTION_BARCODE_GET_STATUS = "kr.co.bluebird.android.action.BARCODE_GET_STATUS";
	//repond barcode status
	public static final String ACTION_BARCODE_CALLBACK_GET_STATUS = "kr.co.bluebird.android.action.BARCODE_CALLBACK_GET_STATUS";
	
	//barcode status
	public static final int BARCODE_CLOSE = 0;
	public static final int BARCODE_OPEN = 1;	
	public static final int BARCODE_TRIGGER_ON = 2;

	public static final String EXTRA_BARCODE_BOOT_COMPLETE = "EXTRA_BARCODE_BOOT_COMPLETE";
	public static final String EXTRA_BARCODE_PROFILE_NAME = "EXTRA_BARCODE_PROFILE_NAME";
	public static final String EXTRA_BARCODE_TRIGGER = "EXTRA_BARCODE_TRIGGER";
	public static final String EXTRA_BARCODE_DECODING_DATA = "EXTRA_BARCODE_DECODING_DATA";
	public static final String EXTRA_HANDLE = "EXTRA_HANDLE";
	public static final String EXTRA_INT_DATA2 = "EXTRA_INT_DATA2";
	public static final String EXTRA_STR_DATA1 = "EXTRA_STR_DATA1";
	public static final String EXTRA_INT_DATA3 = "EXTRA_INT_DATA3";

	public static final int ERROR_FAILED = -1;
	public static final int ERROR_NOT_SUPPORTED = -2;
	public static final int ERROR_NO_RESPONSE = -4;
	public static final int ERROR_BATTERY_LOW = -5;
	public static final int ERROR_BARCODE_DECODING_TIMEOUT = -6;
	public static final int ERROR_BARCODE_ERROR_USE_TIMEOUT = -7;
	public static final int ERROR_BARCODE_ERROR_ALREADY_OPENED = -8;

	public static final int MDM_MSR_MODE__SET_READING_TIMEOUT = 0;
}
