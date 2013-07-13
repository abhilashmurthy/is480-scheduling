<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Prakhar
--%>

<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Create Term</title>
    </head>
    <body>
        <!-- Navbar -->
        <%@include file="navbar.jsp" %>
        <div class="container">
        <h3>Create Term</h3>
        <form action="createTerm" method="post">
			<!-- Putting default values for testing purposes -->
            Choose Year <select name="year"> 
							<option value="2013">2013-2014</option>
							<option value="2014">2014-2015</option>
							<option value="2015">2015-2016</option>
							<option value="2016">2016-2017</option>
							<option value="2017">2017-2018</option>
							<option value="2018">2018-2019</option>
							<option value="2019">2019-2020</option>
							<option value="2020">2020-2021</option>
							<option value="2021">2021-2022</option>
							<option value="2022">2022-2023</option>
						</select> <br/>
            Choose Semester <select name="semester"/> 
								<option value="1">Semester 1</option>
								<option value="2">Semester 2</option>
							</select> <br/>
			<input type="submit" class="btn btn-primary" value="Create"/>
        </form>
	</div>
</body>
</html>
