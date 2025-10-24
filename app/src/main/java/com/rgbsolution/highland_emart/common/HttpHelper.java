package com.rgbsolution.highland_emart.common;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class HttpHelper {
	
	private static HttpHelper manager = new HttpHelper();
	
	private HttpHelper() {}
	
	public static HttpHelper getInstance() {
		return manager;
	}
	
	//	::::::::::::::::::::::::: ↓ LOGIN ↓ :::::::::::::::::::::::::::::		//
	
	public String loginData(String name, String password, String type, String url) throws Exception {
		try {
		HttpPost request = makeHttpPost(name, password, type, url);

		HttpClient client = new DefaultHttpClient();
		ResponseHandler<String> reshandler = new BasicResponseHandler();
		String result = client.execute(request, reshandler);
		
		return result;
		} catch (Exception e) {
			return e.getMessage().toString();
		}
	}
    
	private HttpPost makeHttpPost(String name, String password, String type, String url) throws Exception {
		HttpPost request = null;
		try {
		request = new HttpPost(url);
 
		Vector<BasicNameValuePair> nameValue = new Vector<BasicNameValuePair>();
		nameValue.add(new BasicNameValuePair("id", name));
		nameValue.add(new BasicNameValuePair("pwd", password));
		
		request.setEntity(makeEntity(nameValue));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}
	//	::::::::::::::::::::::::: ↑ LOGIN ↑ :::::::::::::::::::::::::::::::		//
	
	//	::::::::::::::::::::::::: ↓ DATA SEND ↓ :::::::::::::::::::::::::::::::		//
	
	public String sendData(String data, String type, String url) throws Exception {
		try {
			HttpPost request = makeHttpPost(data, type, url);
	
			HttpClient client = new DefaultHttpClient();
			ResponseHandler<String> reshandler = new BasicResponseHandler();
			String result = client.execute(request, reshandler);
			
			return result;
		} catch (Exception e) {
			return e.getMessage().toString();
			
		}
	}
	
	private HttpPost makeHttpPost(String data, String type, String url) throws Exception {
		HttpPost request = null;
		try {
			request = new HttpPost(url);
	 
			Vector<BasicNameValuePair> nameValue = new Vector<BasicNameValuePair>();
			nameValue.add(new BasicNameValuePair("data", data));		
			request.setEntity(makeEntity(nameValue));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	public String sendDataDb(String data, String dbid, String type, String url) throws Exception {
		try {
			HttpPost request = makeHttpPostDb(data, dbid, type, url);

			HttpClient client = new DefaultHttpClient();
			ResponseHandler<String> reshandler = new BasicResponseHandler();
			String result = client.execute(request, reshandler);

			return result;
		} catch (Exception e) {
			return e.getMessage().toString();
		}
	}

	private HttpPost makeHttpPostDb(String data, String dbid, String type, String url) throws Exception {
		HttpPost request = null;
		try {
			request = new HttpPost(url);

			Vector<BasicNameValuePair> nameValue = new Vector<BasicNameValuePair>();
			nameValue.add(new BasicNameValuePair("data", data));
			nameValue.add(new BasicNameValuePair("dbid", dbid));
			request.setEntity(makeEntity(nameValue));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	private HttpEntity makeEntity(Vector<BasicNameValuePair> nameValue) throws Exception {
		HttpEntity result = null;
		try {
			result = new UrlEncodedFormEntity(nameValue, "euc-kr");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//	::::::::::::::::::::::::: ↑ DATA SEND ↑ :::::::::::::::::::::::::::::::		//

}
