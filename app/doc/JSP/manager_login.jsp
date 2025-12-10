<%@ page language="java" contentType="text/html; charset=UTF-8"   pageEncoding="UTF-8"%>
<%@ page import = "java.sql.*" %>
<%@ include file="common/db_connection.jsp" %>

<%
	request.setCharacterEncoding("UTF-8");
	String companyCode = request.getParameter("companyCode");
	String id = request.getParameter("id");
	String pwd = request.getParameter("pwd");
	byte[] pwdII = null;
	StringBuilder sb = new StringBuilder();
	System.out.println("id : " + id);

	Connection conn = getMSSQLConnection();

	if(conn == null) {
		out.println("fail");
		return;
	}

	System.out.println("id=" + id + "   pwd " + pwd);

	//SQL
	//비밀번호 조회 비밀번호
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("SELECT 비밀번호 from CO_비밀번호 WHERE 회사코드 = '" + companyCode + "' AND 사번 = '" + id + "' ");
	ResultSetMetaData rsmd = rs.getMetaData();

	int count = 0;

	while (rs.next()) {
		count++;
		pwdII = rs.getBytes(rsmd.getColumnName(1));

		for (byte b : pwdII) {
			sb.append(String.format("%02x", b));  // 16진수로 변환
		}

		System.out.println(sb.toString());
	}

	if(sb.toString().equals(pwd)) {
		out.println(id);
	} else {
		out.println("fail");
	}

	closeDB(conn, stmt, rs);
%>
