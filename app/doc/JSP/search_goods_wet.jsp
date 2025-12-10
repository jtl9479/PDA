<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="org.apache.log4j.Logger"%>
<%!
 static Logger logger = Logger.getLogger("search_goods_wet.jsp");
%>
<%
boolean connection = false;
Connection conn = null;
Statement stmt = null;
ResultSet rs = null;
String driver = "oracle.jdbc.driver.OracleDriver";
 String url = "jdbc:oracle:thin:@1.1.1.1:SIDname";


request.setCharacterEncoding("UTF-8");

String qry_where = request.getParameter("data");
String dbid = request.getParameter("dbid");

/* System.out.println("==================================");
System.out.println("========search_goods_wet==========");
System.out.println("=================================="); */

logger.info("============================================");
logger.info("=========search_goods_wet start=============");
logger.info("============================================");
logger.info("##search_goods_wet all parameter :" + qry_where);

try {
	Class.forName(driver); 
	conn = DriverManager.getConnection(url, dbid, "DBpassword");
	connection = true;
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

	String quertystring = "SELECT GI_D_ID"
		+ ", WEIGHT"
		+ ", WEIGHT_UNIT"
		+ ", PACKER_PRODUCT_CODE"
		+ ", BARCODE"
		+ ", PACKER_CLIENT_CODE"
		+ ", BOX_CNT"
		+ ", REG_ID"
		+ ", REG_DATE"
		+ ", REG_TIME"
		+ ", MAKINGDATE"
		+ ", BOXSERIAL"
		+ " FROM W_GOODS_WET"
		+ qry_where
		+ " ORDER BY GI_D_ID ASC";
		
	rs = stmt.executeQuery(quertystring);
	
	/* System.out.println(quertystring); */
	logger.info("##search_gooes_wet query :" + quertystring);
	
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
