<%-- 
    Document   : mybookings
    Created on : Aug 2, 2013, 10:09:25 PM
    Author     : Prakhar
--%>

<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>My Bookings</title>
		<%@include file="footer.jsp"%>
    </head>
    <body>
		<%@include file="navbar.jsp" %>
        
		<div class="container">
		<h3>Booking History</h3>
		
		<%-- Tab to view Accept/Reject Bookings --%>
		<ul class="nav nav-tabs">
			<li class="active">
				<a data-toggle="tab">My Bookings</a>
			</li>
		</ul>
			
		<!-- SECTION: Booking History -->
		<s:if test="%{data.size() > 0 && data != null}"> 
			<table class="table table-hover zebra-striped">
				<thead>
					<% if (activeRole.equalsIgnoreCase("Student")) { %>
						<tr>
							<th>#</th>
							<th>My Team</th>
							<th>Presentation Type</th>
							<th>Start Time</th>
							<th>End Time</th>
							<th>Venue</th>
							<th>Booking Status</th>
							<th>Overall Status</th>
						</tr>
					<% } else if (activeRole.equalsIgnoreCase("Supervisor") || activeRole.equalsIgnoreCase("Reviewer")) { %>
						<tr>
							<th>#</th>
							<th>Team Name</th>
							<th>Presentation Type</th>
							<th>Start Time</th>
							<th>End Time</th>
							<th>Venue</th>
							<th>My Status</th>
							<th>Booking Status</th>
						</tr>
					<% } %>
				</thead>
				<tbody> 
					<s:iterator value="data">
						<% if (activeRole.equalsIgnoreCase("Student")) { %>
						<tr>
							<td>1</td>
							<td><s:property value="teamName"/></td>
							<td><s:property value="milestone"/></td>
							<td><s:property value="startTime"/></td>
							<td><s:property value="endTime"/></td>
							<td><s:property value="venue"/></td>
							<td><s:property value="myStatus"/></td>
							<td>
							<s:iterator value="overallBookingStatus">
								<s:property value="status"/> by <s:property value="name"/> <br/>
							</s:iterator>
							</td>
						</tr>
						<% } %>
					</tbody>
				</table>
				</s:iterator>
		</s:if><s:else>
			<h4>No bookings have been made!</h4>
		</s:else>
    </body>
</html>
