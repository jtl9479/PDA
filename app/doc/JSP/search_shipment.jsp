<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.Connection" %>
<%@ page import = "java.sql.DriverManager" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.apache.log4j.Logger"%>
<%@ include file="common/db_connection.jsp" %>
<%!
 static Logger logger = Logger.getLogger("search_shipment.jsp");
%>
<%
boolean connection = false;
Connection conn = null;

request.setCharacterEncoding("UTF-8");

String qry_where = request.getParameter("data");
/* System.out.println("==================================");
System.out.println("=========search_shipment==========");
System.out.println("==================================");
System.out.println(qry_where); */

System.out.println("====================================");
System.out.println("=========search_shipment============");
System.out.println("====================================");
System.out.println("##search_shipment all parameter :" + qry_where);

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
  String quertystring = "SELECT /* 출고상세 종합 조회 */"
								+ " D.SEQ AS GI_D_ID"
								+ ", I.품목코드 AS ITEM_CODE"
								+ ", I.품목명 AS ITEM_NAME"
								+ ", ME.상품코드 AS EMARTITEM_CODE"
								+ ", ME.상품명 AS EMARTITEM"
								+ ", D.출고수량 AS GI_REQ_PKG"
								+ ", D.출고중량 AS GI_REQ_QTY"
								+ ", D.출고일자 AS GI_REQ_DATE"
								+ ", V.BLNO AS BL_NO"
								+ ", '' AS BRAND_CODE"
								+ ", ME.점포코드 AS CLIENT_CODE"
								+ ", ME.점포명 AS CLIENTNAME"
								+ ", B.상호 AS CENTERNAME"
								+ ", I.규격 AS ITEM_SPEC"
								+ ", I.원산지 AS CT_CODE"
								+ ", I.패커코드 AS PACKER_CODE"
								+ ", V.이력번호 AS IMPORT_ID_NO"
								+ ", I.PPCODE AS PACKER_PRODUCT_CODE"
								+ ", M.바코드타입 AS BARCODE_TYPE"
								+ ", IIF(I.원료육여부 = 1, 'W', '') AS ITEM_TYPE"
								+ ", COALESCE(NULLIF(V.평균중량,0), I.박스중량) AS PACKWEIGHT"
								+ ", I.상품바코드 AS BARCODEGOODS"
								+ ", SD.납기일자 AS STORE_IN_DATE"
								+ ", M.물류코드 AS EMARTLOGIS_CODE"
								+ ", B.창고구역 AS WH_AREA"
								+ ", C.명칭 AS USE_NAME"
								+ ", I.제품용도 AS USE_CODE"
								+ ", C1.명칭 AS CT_NAME"
								+ ", ME.점포코드 AS STORE_CODE"
								+ ", '' AS EMART_PLANT_CODE"
								+ " FROM SM_출고상세 D"
								+ " INNER JOIN SM_출고머리 H"
								+ "   ON H.회사코드 = D.회사코드"
								+ "  AND H.출고사업장 = D.출고사업장"
								+ "  AND H.출고일자 = D.출고일자"
								+ "  AND H.출고일련번호 = D.출고일련번호"
								+ " JOIN CO_품목코드 I"
								+ "   ON D.회사코드 = I.회사코드"
								+ "  AND D.출고품목코드 = I.품목코드"
								+ " JOIN SM_수주머리 SH"
								+ "   ON D.회사코드 = SH.회사코드"
								+ "  AND D.수주사업장 = SH.수주사업장"
								+ "  AND D.수주일자 = SH.수주일자"
								+ "  AND D.수주일련번호 = SH.수주일련번호"
								+ " JOIN SM_수주상세 SD"
								+ "   ON SH.회사코드 = SD.회사코드"
								+ "  AND SH.수주사업장 = SD.수주사업장"
								+ "  AND SH.수주일자 = SD.수주일자"
								+ "  AND SH.수주일련번호 = SD.수주일련번호"
								+ "  AND D.순번 = SD.순번"
								+ " JOIN SM_마트사발주이마트 ME"
								+ "   ON SD.마트사SEQ = ME.SEQ"
								+ " JOIN CO_거래처MASTER B"
								+ "   ON ME.회사코드 = B.회사코드"
								+ "  AND ME.점포코드 = B.거래처코드"
								+ " JOIN CO_매출처품목코드매핑 M"
								+ "   ON D.회사코드 = M.회사코드"
								+ "  AND D.출고품목코드 = M.품목코드"
								+ "  AND H.출고거래처 = M.거래처코드"
								+ " JOIN SM_출고LOT L"
								+ "   ON L.출고상세SEQ = D.SEQ"
								+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
								+ "   ON V.회사코드 = D.회사코드"
								+ "  AND V.사업장 = D.출고사업장"
								+ "  AND V.창고코드 = D.창고코드"
								+ "  AND V.품목코드 = D.출고품목코드"
								+ "  AND V.LOTNO = L.LOTNO"
								+ " JOIN CO_각종소분류코드 C"
								+ "   ON C.회사코드 = I.회사코드"
								+ "  AND C.대분류 = '043'"
								+ "  AND C.소분류 = I.제품용도"
								+ " JOIN CO_각종소분류코드 C1"
								+ "   ON C1.회사코드 = I.회사코드"
								+ "  AND C1.대분류 = 'Q14'"
								+ "  AND C1.소분류 = I.원산지"
								+ " WHERE D.회사코드 = '20'"
								+ "   AND D.출고일자 > '20260316'"
								+ "   AND D.창고코드 = '1'"
								+ "   AND H.마트사구분 = '1'"
								+ "   AND D.출고수량 > 0"
								+ "   AND I.원료육여부 = '1'"
								/* + qry_where */
								+ " ORDER BY GI_D_ID ASC";

  ResultSet rs = stmt.executeQuery(quertystring);

  /* System.out.println(quertystring); */
  System.out.println("##search_shipment query :" + quertystring);

  ResultSetMetaData rsmd = rs.getMetaData();
	int columnCnt = rsmd.getColumnCount(); //컬럼????

  while(rs.next()) {
	out.println(
		rs.getString("GI_D_ID") + "::" +           // 0
		rs.getString("ITEM_CODE") + "::" +         // 1
		rs.getString("ITEM_NAME") + "::" +         // 2
		rs.getString("EMARTITEM_CODE") + "::" +    // 3
		rs.getString("EMARTITEM") + "::" +         // 4
		rs.getString("GI_REQ_PKG") + "::" +        // 5
		rs.getString("GI_REQ_QTY") + "::" +        // 6
		rs.getString("GI_REQ_DATE") + "::" +       // 7
		rs.getString("BL_NO") + "::" +             // 8
		rs.getString("BRAND_CODE") + "::" +        // 9
		rs.getString("CLIENT_CODE") + "::" +       // 10
		rs.getString("CLIENTNAME") + "::" +        // 11
		rs.getString("CENTERNAME") + "::" +        // 12
		rs.getString("ITEM_SPEC") + "::" +         // 13
		rs.getString("CT_CODE") + "::" +           // 14
		rs.getString("IMPORT_ID_NO") + "::" +      // 15
		rs.getString("PACKER_CODE") + "::" +       // 16
		rs.getString("PACKER_PRODUCT_CODE") + "::" + // 17
		rs.getString("BARCODE_TYPE") + "::" +      // 18
		rs.getString("ITEM_TYPE") + "::" +         // 19
		rs.getString("PACKWEIGHT") + "::" +        // 20
		rs.getString("BARCODEGOODS") + "::" +      // 21
		rs.getString("STORE_IN_DATE") + "::" +     // 22
		rs.getString("EMARTLOGIS_CODE") + "::" +   // 23
		rs.getString("WH_AREA") + "::" +           // 24
		rs.getString("USE_NAME") + "::" +          // 25
		rs.getString("USE_CODE") + "::" +          // 26
		rs.getString("CT_NAME") + "::" +           // 27
		rs.getString("STORE_CODE") + "::" +        // 28
		rs.getString("EMART_PLANT_CODE") + ";;"    // 29
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
	//	 System.out.println("?�결 객체 ?�기 ?�료");
	}

%>