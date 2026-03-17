<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.util.logging.Logger" %>
<%@ include file="common/db_connection.jsp" %>
<%!
 static Logger logger = Logger.getLogger("insert_goods_wet.jsp");
%>
<%
boolean connection = false;
Connection conn = null;

request.setCharacterEncoding("UTF-8");
String data = request.getParameter("data");
String dbid = request.getParameter("dbid");

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
	String[] splitData = data.split("::");

  System.out.println("============================================");
  System.out.println("=========insert_goods_wet start=============");
  System.out.println("============================================");
  System.out.println("##insert_goods_wet all parameter :" + data);

  SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
  SimpleDateFormat timeformat = new SimpleDateFormat("HHmmss");

  long now = System.currentTimeMillis();
  Date datetime = new Date(now);
  String dateStr = dateformat.format(datetime);
  String timeStr = timeformat.format(datetime);

 //SQL
  String qry = "INSERT INTO SM_출고계근(SEQ"
		+ ", 출고상세SEQ"
		+ ", 계근중량"
		+ ", 계근중량단위"
		+ ", ppCode"
		+ ", 계근바코드"
		+ ", 패커코드"
		+ ", 제조일자"
		+ ", 박스시리얼"
		+ ", 계근순번"
		+ ", 등록사원"
		+ ", 등록일자"
		+ ", 등록시간"
		+ ", 회사코드"
		+ ", 수정사원"
		+ ", 수정일자"
		+ ", 수정시간)"
		+ " VALUES "
		+ "(NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  PreparedStatement pstmt = conn.prepareStatement(qry);


  pstmt.setInt(1, Integer.parseInt(splitData[0]));
  pstmt.setDouble(2, (Double.parseDouble(splitData[1]) * 100) / 100.0);
  pstmt.setString(3, splitData[2]);
  pstmt.setString(4, splitData[3]);
  pstmt.setString(5, splitData[4]);
  pstmt.setString(6, splitData[5]);
  pstmt.setString(7, splitData[6]);
  pstmt.setString(8, splitData[7]);
  pstmt.setInt(9, Integer.parseInt(splitData[8]));
  pstmt.setString(10, splitData[9]);
  pstmt.setString(11, dateStr);
  pstmt.setString(12, timeStr);
  pstmt.setString(13, splitData[10]);
  pstmt.setString(14, splitData[9]);
  pstmt.setString(15, dateStr);
  pstmt.setString(16, timeStr);


/*
  pstmt.setInt(1, 1234567);
//  pstmt.setDouble(2, ((Double.parseDouble("12.30")) * 100) / 100);
//  pstmt.setDouble(2, 123.30);
  pstmt.setString(2, "test");
  pstmt.setString(3, "test");
  pstmt.setString(4, "test");
  pstmt.setString(5, "test");
  pstmt.setInt(6, 1);
  pstmt.setString(7, "test");
  pstmt.setString(8, dateStr);
  pstmt.setString(9, timeStr);
*/

  System.out.println("##insert_goods_wet query start, query :"+ qry);
  pstmt.executeUpdate();
/*
	String update_qry = "UPDATE W_GOODS_ID SET "
			+ "GI_QTY = GI_QTY +  ? "
			+ ", PACKING_QTY = PACKING_QTY + 1 "
			+ ", MOD_ID = ? "
			+ ", MOD_DATE = ? "
			+ ", MOD_TIME = ? "
			+ "WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?";
	pstmt = conn.prepareStatement(update_qry);

	pstmt.setDouble(1, (Double.parseDouble(splitData[1]) * 100) / 100.0);
	pstmt.setString(2, splitData[9]);
	pstmt.setString(3, dateStr);
	pstmt.setString(4, timeStr);
	pstmt.setInt(5, Integer.parseInt(splitData[0]));
	pstmt.setString(6, splitData[10]);		// ITEM_CODE
	pstmt.setString(7, splitData[11]);		// BRAND_CODE

	pstmt.executeUpdate();
*/
	/* System.out.println("============================");
	System.out.println("========GI_D_ID=======" + splitData[0]);
	System.out.println("========WEIGHT========" + splitData[1]);
	System.out.println("========DATE==========" + dateStr + timeStr);
	System.out.println("========REG_ID========" + splitData[9]);
	System.out.println("============================"); */ //logger 추가 후 System.out.println 주석처리, 2019.01.28 park.sj

  conn.commit();

  System.out.println("##insert_goods_wet parameter : ======INSERT_GOODS_WET PARAMS=====");
  System.out.println("##insert_goods_wet parameter : ========GI_D_ID===================" + splitData[0]);
  System.out.println("##insert_goods_wet parameter : ========WEIGHT====================" + splitData[1]);
  System.out.println("##insert_goods_wet parameter : ========DATE======================" + dateStr + timeStr);
  System.out.println("##insert_goods_wet parameter : ========REG_ID====================" + splitData[9]);
  System.out.println("##insert_goods_wet parameter : ==================================");

  if(pstmt != null)
	  pstmt.close();
  if(conn != null)
	  conn.close();
  out.println("s");
} catch (Exception ex) {
		out.println("f");
		out.println(ex.getMessage());
		ex.printStackTrace();
		System.out.println("=============insert_goods_wet exception============== message :" + ex.getMessage().toString());
		conn.rollback();
		conn.close();
}
	
  
%>
