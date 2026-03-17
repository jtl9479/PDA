<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.apache.log4j.Logger"%>
<%@ include file="common/db_connection.jsp" %>
<%!
 static Logger logger = Logger.getLogger("search_barcode_info.jsp");
%>
<%
boolean connection = false;
Connection conn = null;

request.setCharacterEncoding("UTF-8");

String qry_where = request.getParameter("data");
String dbid = request.getParameter("dbid");
/* System.out.println("==================================");
System.out.println("=======search_barcode_info========");
System.out.println("=================================="); */
System.out.println("============================================");
System.out.println("=========search_barcode_info start==========");
System.out.println("============================================");
System.out.println("##search_barcode_info all parameter :" + qry_where);

try {
	conn = getMSSQLConnection();
	if(conn != null) {
		connection = true;
	}
} catch (Exception e) {
	connection = false;
	out.println(e.getMessage().toString());
	e.printStackTrace();
}
 //SQL
  Statement stmt = conn.createStatement();
  //ResultSet rs = stmt.executeQuery("SELECT PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME, BRAND_CODE, BARCODEGOODS, BASEUNIT, ZEROPOINT, PARCKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO, BARCODEGOODS_FROM, BARCODEGOODS_TO, WEIGHT_FROM, WEIGHT_TO, STATUS, REG_ID, REG_DATE, REG_TIME, MEMO FROM S_BARCODE_INFO");

	String quertystring = "SELECT 패커코드		AS PACKER_CLIENT_CODE"
							 + ", ppCode	AS PACKER_PRODUCT_CODE"
							 + ", '' 		AS PACKER_PRD_NAME"
							 + ", '' 		AS ITEMCODE"
							 + ", 품목명 	AS ITEM_NAME_KR"
							 + ", '' 		AS BRAND_CODE"
							 + ", 상품바코드 AS BARCODEGOODS"
							 + ", 기준단위 	AS BASEUNIT"
							 + ", 소수점 	AS ZEROPOINT"
							 + ", '' 		AS PACKER_PRD_CODE_FROM"
							 + ", '' 		AS PACKER_PRD_CODE_TO"
							 + ", 바코드상품코드시작 AS BARCODEGOODS_FROM"
							 + ", 바코드상품코드끝 AS BARCODEGOODS_TO"
							 + ", 중량시작 AS WEIGHT_FROM"
							 + ", 중량끝 AS WEIGHT_TO"
							 + ", 제조일자시작 AS MAKINGDATE_FROM"
							 + ", 제조일자끝 AS MAKINGDATE_TO"
							 + ", 박스시리얼시작 AS BOXSERIAL_FROM"
							 + ", 박스시리얼끝 AS BOXSERIAL_TO"
							 + ", '' AS STATUS"
							 + ", '' AS REG_ID"
							 + ", '' AS REG_DATE"
							 + ", '' AS REG_TIME"
							 + ", '' AS MEMO"
							 + ", CASE WHEN 적용단위 = 1 THEN 적용값 * 365"
							 + "       WHEN 적용단위 = 2 THEN 적용값 * 30"
							 + "       ELSE 적용값 END AS SHELF_LIFE"
					     + " FROM CO_품목코드"
							+ qry_where
						+ " ORDER BY PACKER_PRODUCT_CODE ASC";

  ResultSet rs = stmt.executeQuery(quertystring);

  /* System.out.println(quertystring); */

  System.out.println("##search_barcode_info query :" + quertystring);
  
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
			+ rs.getString(rsmd.getColumnName(22)) + "::" + rs.getString(rsmd.getColumnName(23)) + "::" + rs.getString(rsmd.getColumnName(24)) + "::" + rs.getString(rsmd.getColumnName(25)) +";;");
  }

  try{
	  if(rs != null) 
		  rs.close();
	  if(stmt != null) 
		  stmt.close();
	  if(conn != null) 
		  conn.close();
	 }catch(SQLException se){
	//	 System.out.println("?�결 객체 ?�기 ?�료");
	}

%>