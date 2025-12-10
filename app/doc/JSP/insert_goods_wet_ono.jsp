<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.*" %>
<%@ page import="java.lang.*" %>

<%
boolean connection = false;
Connection conn = null;
String driver = "oracle.jdbc.driver.OracleDriver";
String url = "jdbc:oracle:thin:@1.1.1.1:SIDname";

request.setCharacterEncoding("UTF-8");
String data = request.getParameter("data");

try {
	Class.forName(driver); 
	conn = DriverManager.getConnection(url, "DBuser", "DBpassword");
	connection = true;
} catch (Exception e) {
	connection = false;
	out.println(e.getMessage().toString());
	e.printStackTrace();
} 
try {
	String[] splitData = data.split("::");

  SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
  SimpleDateFormat timeformat = new SimpleDateFormat("HHmmss");

  long now = System.currentTimeMillis();
  Date datetime = new Date(now);
  String dateStr = dateformat.format(datetime);
  String timeStr = timeformat.format(datetime);

 //SQL 
  String qry = "INSERT INTO W_GOODS_WET(GOODS_WET_ID"
		+ ", GI_D_ID"
		+ ", WEIGHT"
		+ ", WEIGHT_UNIT"
		+ ", PACKER_PRODUCT_CODE"
		+ ", BARCODE"
		+ ", PACKER_CLIENT_CODE"
		+ ", MAKINGDATE"
		+ ", BOXSERIAL"
		+ ", BOX_CNT"
		+ ", REG_ID"
		+ ", REG_DATE"
		+ ", REG_TIME)"
		+ " VALUES "
		+ "(W_GOODS_WET_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?)";
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
	
  pstmt.executeUpdate();

	String update_qry = "UPDATE W_GOODS_ID SET "
			+ "GI_QTY = GI_QTY +  ? "
			+ ", PACKING_QTY = PACKING_QTY + 1 "
			+", REG_ID = ? "
			+ ", REG_DATE = ? "
			+ ", REG_TIME = ? "
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


  conn.commit();
  if(pstmt != null) 
	  pstmt.close();
  if(conn != null) 
	  conn.close();
} catch (Exception ex) {
		out.println(ex.getMessage());
		ex.printStackTrace();
		conn.rollback();
		conn.close();
}
	
  
%>
