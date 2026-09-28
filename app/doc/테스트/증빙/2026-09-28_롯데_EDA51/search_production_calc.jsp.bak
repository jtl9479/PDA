<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="org.apache.log4j.Logger"%>
<%@ page import="java.sql.*" %>
<%@ include file="common/db_connection.jsp" %>
<%!
 static Logger logger = Logger.getLogger("wet_production_calc.jsp"); //log4j를 위해
%>
<%
boolean connection = false;
Connection conn = null;

request.setCharacterEncoding("UTF-8");

String qry_where = request.getParameter("data");
System.out.println("==================================");
System.out.println("=========search_shipment==========");
System.out.println("==================================");
System.out.println(qry_where);
try {
	conn = getMSSQLConnection();
	if(conn != null) {
		connection = true;
	}
} catch (Exception e) {
	connection = false;
	System.out.println("DB 연결 실패");
	out.println(e.getMessage().toString());
	e.printStackTrace();
} 
 //SQL 
  Statement stmt = conn.createStatement();
 
  String quertystring = "SELECT " 
			+ " C.WEIGHT_FROM"
			+ ",C.WEIGHT_TO"
			+ ",C.ZEROPOINT"
			+ ",C.BASEUNIT"
			+ " FROM B_SUPPLIER_ITEM a"
			+ ",S_BARCODE_INFO c"
			+ " WHERE    a.SUPPLIER_ITEM_ID = c.SUPPLIER_ITEM_ID "
			+ " AND A.STATUS = 'Y'"
			+ " AND ROWNUM = 1"
			+ qry_where;
			
  ResultSet rs = stmt.executeQuery(quertystring);
  
  System.out.println(quertystring);
  System.out.println("##serch_shipment query: " + quertystring);
  
  ResultSetMetaData rsmd = rs.getMetaData();
	int columnCnt = rsmd.getColumnCount();

  while(rs.next())
  {
     out.println(rs.getString("WEIGHT_FROM") + "::" + rs.getString("WEIGHT_TO") + "::" + rs.getString("ZEROPOINT") + "::" + rs.getString("BASEUNIT"));
  }
	try{
	  if(rs != null) 
		  rs.close();
	  if(stmt != null) 
		  stmt.close();
	  if(conn != null) 
		  conn.close();
	 }catch(SQLException se){
	
	}
%>