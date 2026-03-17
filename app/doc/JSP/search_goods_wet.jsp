<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="org.apache.log4j.Logger"%>
<%@ include file="common/db_connection.jsp" %>
<%!
 static Logger logger = Logger.getLogger("search_goods_wet.jsp");
%>
<%
boolean connection = false;
Connection conn = null;
Statement stmt = null;
ResultSet rs = null;

request.setCharacterEncoding("UTF-8");

String qry_where = request.getParameter("data");
String dbid = request.getParameter("dbid");

/* System.out.println("==================================");
System.out.println("========search_goods_wet==========");
System.out.println("=================================="); */

System.out.println("============================================");
System.out.println("=========search_goods_wet start=============");
System.out.println("============================================");
System.out.println("##search_goods_wet all parameter :" + qry_where);

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

try {
	SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
	long now = System.currentTimeMillis();
	Date datetime = new Date(now);
	String dateStr = dateformat.format(datetime);

 //SQL
 	stmt = conn.createStatement();
  //ResultSet rs = stmt.executeQuery("SELECT PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME, BRAND_CODE, BARCODEGOODS, BASEUNIT, ZEROPOINT, PARCKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO, BARCODEGOODS_FROM, BARCODEGOODS_TO, WEIGHT_FROM, WEIGHT_TO, STATUS, REG_ID, REG_DATE, REG_TIME, MEMO FROM S_BARCODE_INFO");

	String quertystring = "SELECT 출고상세SEQ AS GI_D_ID"
		+ ", 계근중량 AS WEIGHT"
		+ ", 계근중량단위 AS WEIGHT_UNIT"
		+ ", ppCode AS PACKER_PRODUCT_CODE"
		+ ", 계근바코드 AS BARCODE"
		+ ", 패커코드 AS PACKER_CLIENT_CODE"
		+ ", 계근순번 AS BOX_CNT"
		+ ", 등록사원 AS REG_ID"
		+ ", 등록일자 AS REG_DATE"
		+ ", 등록시간 AS REG_TIME"
		+ ", 제조일자 AS MAKINGDATE"
		+ ", 박스시리얼 AS BOXSERIAL"
		+ " FROM SM_출고계근"
		+ qry_where
		+ " ORDER BY GI_D_ID ASC";

	rs = stmt.executeQuery(quertystring);

	/* System.out.println(quertystring); */
	System.out.println("##search_gooes_wet query :" + quertystring);
	
  	ResultSetMetaData rsmd = rs.getMetaData();
	int columnCnt = rsmd.getColumnCount(); //컬럼????

 	 while(rs.next())
  	{
	//   out.println(" <TD>" + rs.getInt("IH.GI_H_ID") + "</TD>");
 	 out.println(rs.getString(rsmd.getColumnName(1)) + "::" + rs.getString(rsmd.getColumnName(2)) + "::" + rs.getString(rsmd.getColumnName(3)) + "::"
			+ rs.getString(rsmd.getColumnName(4)) + "::" + rs.getString(rsmd.getColumnName(5)) + "::" + rs.getString(rsmd.getColumnName(6)) + "::"
			+ rs.getString(rsmd.getColumnName(7)) + "::" + rs.getString(rsmd.getColumnName(8)) + "::" + rs.getString(rsmd.getColumnName(9)) + "::"
			+ rs.getString(rsmd.getColumnName(10)) + "::" + rs.getString(rsmd.getColumnName(11)) + "::" + rs.getString(rsmd.getColumnName(12)) + ";;");
  	}
	} catch (Exception ex) {
		out.println(ex.getMessage().toString());
		ex.printStackTrace();
	}
 	 try{
	  if(rs != null) 
		  rs.close();
	  if(stmt != null) 
		  stmt.close();
	  if(conn != null) 
		  conn.close();
	 }catch(SQLException se){
	//	 System.out.println("객체에러");
	}

%>
