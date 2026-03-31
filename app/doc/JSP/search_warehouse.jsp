<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.apache.log4j.Logger"%>
<%@ include file="common/db_connection.jsp" %>
<%!
 static Logger logger = Logger.getLogger("search_warehouse.jsp");
%>
<%
boolean connection = false;
Connection conn = null;

request.setCharacterEncoding("UTF-8");

System.out.println("============================================");
System.out.println("=========search_warehouse start=============");
System.out.println("============================================");

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
  String quertystring = "SELECT 회사코드 AS COMPANY_CODE"
  								+ ", 창고코드 AS WAREHOUSE_CODE"
								+ ", 창고명 AS WAREHOUSE_NAME"
								+ " FROM CO_창고관리"
								+ " WHERE 회사코드 = '20'";

  ResultSet rs = stmt.executeQuery(quertystring);

  System.out.println("##search_warehouse query :" + quertystring);

  ResultSetMetaData rsmd = rs.getMetaData();
	int columnCnt = rsmd.getColumnCount();

  while(rs.next()) {
	out.println(
		rs.getString("COMPANY_CODE") + "::" +
		rs.getString("WAREHOUSE_CODE") + "::" +
		rs.getString("WAREHOUSE_NAME") + ";;"
		);
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