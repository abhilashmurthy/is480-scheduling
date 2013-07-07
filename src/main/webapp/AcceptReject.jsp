<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Tarlochan
--%>

<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.dao.TimeslotStatusDAO"%>
<%@page import="model.dao.TimeslotDAO"%>
<%@page import="util.Milestone"%>
<%@page import="model.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@page import="model.dao.ScheduleDAO"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Accept/Reject Booking</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <div class="container">
        <h2>Accept/Reject Booking</h2>
        <form action="acceptReject" method="post">
            Enter Term Id <input type="text" name="termId"/> <br/>
            Enter User Id <input type="text" name="userId"/> <br/>
            <input type="submit" class="btn btn-primary" value="Search"/>
        </form>
        <script src="js/bootstrap.js" type="text/javascript"></script>
        <%-- <% String statuses = '<s:property value="message" />'; %> --%>
		
		<br/>
        <s:set var = "breakLoop" value = "%{false}" />
        <s:iterator value="message">
            <s:if test="!#breakLoop">
                <h4> List of Teams to Approve: </h4>
                <s:set var = "breakLoop" value = "%{true}"/>
            </s:if>
        </s:iterator>


        <s:iterator value="message">
			<%-- <s:textfield name="message" value="%{[0].toString()}" /><br/>--%>
			<s:property value="teamName"/><br/>
            <form action="SlotUpdated.jsp" method="post">
                <input type="submit" class="btn btn-primary" value="Approve" name="Approve"/>
                <input type="submit" class="btn btn-primary" value="Reject" name="Reject"/>
                <input type="hidden" name="teamId" value="<s:property value="teamIdInt"/>" />
            </form>
        </s:iterator>

        </div>
    </body>
</html>
