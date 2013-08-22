<%-- 
    Document   : bookinghistory
    Created on : Aug 2, 2013, 10:09:25 PM
    Author     : Prakhar
--%>

<!-- Booking History page -->
<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Booking History</title>
		<%@include file="footer.jsp"%>
    </head>
    <body>
		<%@include file="navbar.jsp" %>
		<div class="container">
		<h3>Booking History</h3>
		
		<!-- SECTION: Booking History -->
		<s:if test="%{data.size() > 0 && data != null}"> 
			<table class="table table-hover zebra-striped">
				<thead>
					<% if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) 
							|| activeRole.equals(Role.COURSE_COORDINATOR)) { %>
						<tr>
							<th>#</th>
							<th>
							<% if (activeRole.equals(Role.STUDENT)) { %>
								My Team
							<% } else { %>
								Team
							<% } %>
							</th>
							<th>Presentation</th>
							<th>Date</th>
							<th>Time</th>
							<th>Venue</th>
							<th>Booking Status</th>
							<th>Overall Booking Status</th>
						</tr>
					<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<tr>
							<th>#</th>
							<th>Team</th>
							<th>Presentation</th>
							<th>Date</th>
							<th>Time</th>
							<th>Venue</th>
							<th>My Status</th>
							<th>Overall Booking Status</th>
						</tr>
					<% } %>
				</thead>
				<tbody> 
					<% int count = 1; %>
					<s:iterator value="data">
						<% if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) 
							|| activeRole.equals(Role.COURSE_COORDINATOR)) { %>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Pending')}"> 
							<tr class="warning">
						</s:if><s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Approved')}">
							<tr class="success">
						</s:elseif><s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Rejected')}">
							<tr class="error">
						</s:elseif>
								<td><%= count %></td>
								<td><s:property value="teamName"/></td>
								<td><s:property value="milestone"/></td>
								<td><s:property value="date"/></td>
								<td><s:property value="time"/></td>
								<td><s:property value="venue"/></td>
								<td>
								<s:iterator value="individualBookingStatus">
									<s:property value="status"/> by <s:property value="name"/> <br/>
								</s:iterator>
								</td>
								<td><s:property value="overallBookingStatus"/></td>
								<% count = count + 1; %>
							</tr>
						<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Pending')}"> 
							<tr class="warning">
						</s:if><s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Approved')}">
							<tr class="success">
						</s:elseif><s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Rejected')}">
							<tr class="error">
						</s:elseif>
							<td><%= count %></td>
							<td><s:property value="teamName"/></td>
							<td><s:property value="milestone"/></td>
							<td><s:property value="date"/></td>
							<td><s:property value="time"/></td>
							<td><s:property value="venue"/></td>
							<td><s:property value="myStatus"/></td>
							<td><s:property value="overallBookingStatus"/></td>
							<% count = count + 1; %>
						</tr>
						<% } %>
					</s:iterator>
					</tbody>
				</table>
		</s:if><s:else>
			<h4>No bookings have been made!</h4>
		</s:else>
		</div>
    </body>
</html>
