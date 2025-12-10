<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.apache.log4j.Logger"%>
<%!
 static Logger logger = Logger.getLogger("search_shipment.jsp");
%>
<%
boolean connection = false;
Connection conn = null;
String driver = "oracle.jdbc.driver.OracleDriver";
String url = "jdbc:oracle:thin:@1.1.1.1:SIDname";

request.setCharacterEncoding("UTF-8");

String qry_where = request.getParameter("data");
/* System.out.println("==================================");
System.out.println("=========search_shipment==========");
System.out.println("==================================");
System.out.println(qry_where); */

logger.info("====================================");
logger.info("=========search_shipment============");
logger.info("====================================");
logger.info("##search_shipment all parameter :" + qry_where);

try {
	Class.forName(driver); 	
	conn = DriverManager.getConnection(url,"DBuser", "DBpassword");
	connection = true;
} catch (Exception e) {
	connection = false;
	System.out.println("DB 연결 실패");
	out.println(e.getMessage().toString());
	e.printStackTrace();
} 
 //SQL 
  Statement stmt = conn.createStatement();
  String quertystring = "SELECT " 
								+ "GI_H_ID"
								+ ", GI_D_ID"
								+ ", EOI_ID"
								+ ", ITEM_CODE"
								+ ", ITEM_NAME"
								+ ", EMARTITEM_CODE"
								+ ", EMARTITEM"
								+ ", GI_REQ_PKG"
								+ ", GI_REQ_QTY"
								+ ", AMOUNT"
								+ ", GOODS_R_ID"
								+ ", GR_REF_NO"
								+ ", GI_REQ_DATE"
								+ ", BL_NO"
								+ ", BRAND_CODE"
								+ ", BRANDNAME"
								+ ", CLIENT_CODE"
								+ ", CLIENTNAME"
								+ ", CENTERNAME"
								+ ", ITEM_SPEC"
								+ ", CT_CODE"
								+ ", IMPORT_ID_NO"
								+ ", PACKER_CODE"
								+ ", PACKERNAME"
								+ ", PACKER_PRODUCT_CODE"
								+ ", BARCODE_TYPE"
								+ ", ITEM_TYPE"
								+ ", PACKWEIGHT"
								+ ",'999999' AS BARCODEGOODS"
								+ ", STORE_IN_DATE"
								+ ", EMARTLOGIS_CODE"
								+ ", EMARTLOGIS_NAME"
								+ ", WH_AREA"
								+ " FROM VW_PDA_WID_LIST_ONO_DIFF_PRD"
								+ qry_where
								+ " ORDER BY EOI_ID ASC";
  
  ResultSet rs = stmt.executeQuery(quertystring);
  
  /* System.out.println(quertystring); */
  logger.info("##search_shipment query :" + quertystring);
  
  ResultSetMetaData rsmd = rs.getMetaData();
	int columnCnt = rsmd.getColumnCount(); //컬럼????

  while(rs.next())
  {
   out.println(rs.getString("GI_H_ID") + "::" + rs.getString("GI_D_ID") + "::" + rs.getString("EOI_ID") + "::" + rs.getString("ITEM_CODE") + "::"
			+ rs.getString("ITEM_NAME") + "::" + rs.getString("EMARTITEM_CODE") + "::" + rs.getString("EMARTITEM") + "::" 
			+ rs.getString("GI_REQ_PKG") + "::" + rs.getString("GI_REQ_QTY") + "::" + rs.getString("AMOUNT") + "::" 
			+ rs.getString("GOODS_R_ID") + "::" + rs.getString("GR_REF_NO") + "::" + rs.getString("GI_REQ_DATE") + "::" + rs.getString("BL_NO") + "::"
			+ rs.getString("BRAND_CODE") + "::" + rs.getString("BRANDNAME") + "::" + rs.getString("CLIENT_CODE") + "::" 
			+ rs.getString("CLIENTNAME") + "::" + rs.getString("CENTERNAME") + "::" + rs.getString("ITEM_SPEC") + "::" 
			+ rs.getString("CT_CODE") + "::" + rs.getString("IMPORT_ID_NO") + "::" + rs.getString("PACKER_CODE") + "::" 
			+ rs.getString("PACKERNAME") + "::" + rs.getString("PACKER_PRODUCT_CODE") + "::" + rs.getString("BARCODE_TYPE") + "::" 
			+ rs.getString("ITEM_TYPE") + "::" + rs.getString("PACKWEIGHT") + "::" + rs.getString("BARCODEGOODS") + "::" + rs.getString("STORE_IN_DATE") + "::"
			+ rs.getString("EMARTLOGIS_CODE") + "::" + rs.getString("EMARTLOGIS_NAME") + "::" + rs.getString("WH_AREA") + ";;");
//   out.println(rs.getString("DE_CLIENT(IH.CLIENT_CODE)") + "::" + rs.getString("DE_ITEM(ITEM_CODE)") + ";;");
//      out.println(rs.getString(rsmd.getColumnName(1)));
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