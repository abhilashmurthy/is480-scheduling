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
		<link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    </head>
    <body>
		<%@include file="navbar.jsp" %>
		<div class="container">
		<h3>Booking History</h3>
		
		<!-- SECTION: Booking History -->
		<s:if test="%{data != null && data.size() > 0}"> 
			<table id="bookingHistoryTable" class="table table-hover zebra-striped" style="font-size: 13px;">
				<thead>
					<% if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) 
							|| activeRole.equals(Role.COURSE_COORDINATOR)) { %>
						<tr>
							<!--<th>#</th>-->
							<th>
							<% if (activeRole.equals(Role.STUDENT)) { %>
								My Team
							<% } else { %>
								Team
							<% } %>
							</th>
							<th>Presentation</th>
							<th>Date & Time</th>
							<th>Venue</th>
							<th>Booking Status</th>
							<th>Overall Booking Status</th>
							<th>Reason for Rejection</th>
						</tr>
					<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<tr>
							<th>#</th>
							<th>Team</th>
							<th>Presentation</th>
							<th>Date & Time</th>
							<th>Venue</th>
							<th>My Status</th>
							<th>Overall Booking Status</th>
							<th>Reason for Rejection</th>
						</tr>
					<% } %>
				</thead>
				<tbody> 
					<%--<% int count = 1; %> --%>
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
						<%--<s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Deleted')}">
							<tr class="info">
						</s:elseif>--%>
								<%--<td><%= count %></td>--%>
								<td><s:property value="teamName"/></td>
								<td><s:property value="milestone"/></td>
								<td><s:property value="date"/> <s:property value="time"/></td>
								<td><s:property value="venue"/></td>
								<td>
								<% int countRows = 0; %>
								<s:iterator value="individualBookingStatus">
									<s:property value="status"/> by <s:property value="name"/> <br/>
									<% countRows++ ; %>
								</s:iterator> 
								<% if (countRows == 1) { %>
									<br/>
								<% } %>
								</td>
								<td><s:property value="overallBookingStatus"/></td>
								<s:if test="%{rejectReason != null)}"> 
									<td><s:property value="rejectReason"/></td>
								</s:if><s:else>
									<td>-</td>
								</s:else>
								<%--<% count = count + 1; %>--%>
							</tr>
						<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Pending')}"> 
							<tr class="warning">
						</s:if><s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Approved')}">
							<tr class="success">
						</s:elseif><s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Rejected')}">
							<tr class="error">
						</s:elseif>
						<%--<s:elseif test="%{overallBookingStatus.equalsIgnoreCase('Deleted')}">
							<tr class="warning">
						</s:elseif>--%>
							<%--<td><%= count %></td>--%>
							<td><s:property value="teamName"/></td>
							<td><s:property value="milestone"/></td>
							<td><s:property value="date"/> <s:property value="time"/></td>
							<td><s:property value="venue"/></td>
							<td><s:property value="myStatus"/></td>
							<td>
								<s:property value="overallBookingStatus"/><br/><br/>
							</td>
							<s:if test="%{rejectReason != null)}"> 
								<td><s:property value="rejectReason"/></td>
							</s:if><s:else>
								<td>-</td>
							</s:else>
							<%--<% count = count + 1; %>--%>
						</tr>
						<% } %>
					</s:iterator>
					</tbody>
				</table>
				<br/><br/>
		</s:if><s:else>
			<h4>No bookings have been made!</h4>
		</s:else>
		</div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>
		<script type="text/javascript">
			
		//For data tables
		$(document).ready(function(){
			$('#bookingHistoryTable').dataTable({
				"aLengthMenu": [
					[10, 25, 50, 100, -1],[10, 25, 50, 100, "All"]], 
				"iDisplayLength" : -1,
//				"bPaginate": false,
//				"bLengthChange": false,
//				"bFilter": false,
//				"bSort": false,
				"bInfo": false,
//				"bAutoWidth": false,
//				"asStripClasses": null,
				"bSortClasses": false
			})
		});
		</script>
    </body>
</html>
