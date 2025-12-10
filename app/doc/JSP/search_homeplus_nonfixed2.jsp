<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.apache.log4j.Logger"%>
<%!
 static Logger logger = Logger.getLogger("search_barcode_info.jsp"); 
%>
<%
boolean connection = false;
Connection conn = null;
String driver = "oracle.jdbc.driver.OracleDriver";
 String url = "jdbc:oracle:thin:@1.1.1.1:SIDname";

request.setCharacterEncoding("UTF-8");

String qry_where = request.getParameter("data");
String dbid = request.getParameter("dbid");

System.out.println("=======qry_where========" + qry_where);

/* System.out.println("==================================");
System.out.println("=================================="); */
logger.info("============================================");
logger.info("=========search_barcode_info start==========");
logger.info("============================================");
logger.info("##search_barcode_info all parameter :" + qry_where);

try {
	Class.forName(driver); 
	conn = DriverManager.getConnection(url, "DBuser", "DBpassword");
	
	connection = true;
	System.out.println("INNO DB 연결 성공 HPLUS NONFIXED2 성공");
} catch (Exception e) {
	connection = false;
	System.out.println("INNO DB 연결 성공 HPLUS NONFIXED2 실패");
	out.println(e.getMessage().toString());
	e.printStackTrace();
} 
 //SQL 
  Statement stmt = conn.createStatement();
  //ResultSet rs = stmt.executeQuery("SELECT PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME, BRAND_CODE, BARCODEGOODS, BASEUNIT, ZEROPOINT, PARCKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO, BARCODEGOODS_FROM, BARCODEGOODS_TO, WEIGHT_FROM, WEIGHT_TO, STATUS, REG_ID, REG_DATE, REG_TIME, MEMO FROM S_BARCODE_INFO");

	String quertystring = "SELECT '홈플러스용' as PACKER_CLIENT_CODE"
		+ ", sbi.ITEM_CODE as PACKER_PRODUCT_CODE"
		+ ", sbi.ITEM_NAME_KR as PACKER_PRD_NAME"
		+ ", sbi.ITEM_CODE as ITEMCODE"
		+ ", sbi.ITEM_NAME_KR as ITEM_NAME_KR"
		+ ", '0000' as BRAND_CODE"
		+ ", sbi.ITEM_CODE as BARCODEGOODS"
		+ ", 'KG' as BASEUNIT"
		+ ", SBI.ZEROPOINT"
		+ ", SBI.PACKER_PRD_CODE_FROM"
		+ ", SBI.PACKER_PRD_CODE_TO"
		+ ", SBI.BARCODEGOODS_FROM"
		+ ", SBI.BARCODEGOODS_TO"
		+ ", SBI.WEIGHT_FROM"
		+ ", SBI.WEIGHT_TO"
		+ ", SBI.MAKINGDATE_FROM"
		+ ", SBI.MAKINGDATE_TO"
		+ ", SBI.BOXSERIAL_FROM"
		+ ", SBI.BOXSERIAL_TO"
		+ ", SBI.STATUS"
		+ ", SBI.REG_ID"
		+ ", SBI.REG_DATE"
		+ ", SBI.REG_TIME"
		+ ", '0000' AS memo"
		+ " FROM B_ITEM sbi"
		+ qry_where
		+ " ORDER BY ITEMCODE ASC";
  
  ResultSet rs = stmt.executeQuery(quertystring);
  
  //System.out.println(quertystring);
  
  logger.info("##search_barcode_info query :" + quertystring);
  
  ResultSetMetaData rsmd = rs.getMetaData();
	int columnCnt = rsmd.getColumnCount(); //컬럼????

  
  while(rs.next())
  {
//   out.println(" <TD>" + rs.getInt("IH.GI_H_ID") + "</TD>");
  out.println(rs.getString(rsmd.getColumnName(1)) + "::" + rs.getString(rsmd.getColumnName(2)) + "::" + rs.getString(rsmd.getColumnName(3)) + "::"	
			+ rs.getString(rsmd.getColumnName(4)) + "::" + rs.getString(rsmd.getColumnName(5)) + "::" + rs.getString(rsmd.getColumnName(6)) + "::" 
			+ rs.getString(rsmd.getColumnName(7)) + "::" + rs.getString(rsmd.getColumnName(8)) + "::" + rs.getString(rsmd.getColumnName(9)) + "::" 
			+ rs.getString(rsmd.getColumnName(10)) + "::" + rs.getString(rsmd.getColumnName(11)) + "::" + rs.getString(rsmd.getColumnName(12)) + "::" 
			+  rs.getString(rsmd.getColumnName(13)) + "::" + rs.getString(rsmd.getColumnName(14)) + "::" + rs.getString(rsmd.getColumnName(15)) + "::" 
			+ rs.getString(rsmd.getColumnName(16)) + "::" + rs.getString(rsmd.getColumnName(17)) + "::" + rs.getString(rsmd.getColumnName(18)) + "::" 
			+ rs.getString(rsmd.getColumnName(19)) + "::" + rs.getString(rsmd.getColumnName(20)) + "::" + rs.getString(rsmd.getColumnName(21)) + "::" 
			+ rs.getString(rsmd.getColumnName(22)) + "::" + rs.getString(rsmd.getColumnName(23)) + "::" + rs.getString(rsmd.getColumnName(24)) + ";;");
  }

  try{
	  if(rs != null) 
		  rs.close();
	  if(stmt != null) 
		  stmt.close();
	  if(conn != null) 
		  conn.close();
	 }catch(SQLException se){
	//	 System.out.println("객체오류");
	}

%>
