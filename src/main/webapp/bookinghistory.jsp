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
        <%@include file="header.jsp" %>
		<title>
		<%  Role activeR = (Role) session.getAttribute("activeRole"); %>
		<% if (activeR.equals(Role.ADMINISTRATOR) || activeR.equals(Role.COURSE_COORDINATOR)) { %>
	        IS480 Scheduling System | All Bookings
		<% } else { %>
			IS480 Scheduling System | My Bookings
		<% } %>
		</title>
		<link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    </head>
    <body>
		<%@include file="navbar.jsp" %>
		<div class="container">
		<% if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)){ %>
			<h3 style="float: left; margin-right: 50px;">All Bookings</h3> 
		<% } else { %>
			<h3 style="float: left; margin-right: 50px;">My Bookings</h3> 
		<% } %>
		<s:if test="%{data != null && data.size() > 0}">
			<div style="float:right">
				<input type="hidden" id="dropdownValues"/>
				Hide Columns:
				<a rel="tooltip" data-placement="right" title="Press Ctrl to select / deselect columns">
				<select id="hideColumns" size="4" multiple="multiple" style="font-size:13px; width:200px" 
						onchange="onChangeInDropdown();">
					<% if (activeRole.equals(Role.STUDENT)){ %>
						<option value="1">My Team</option>
					<% } else { %>
						<option value="1">Team</option>
					<% } %>
					<option value="2">Presentation</option>
					<option value="3">Date & Time</option>
					<option value="4">Venue</option>
					<% if (activeRole.equals(Role.FACULTY)) { %>
						<option value="5">My Response</option>
					<% } else { %>
						<option value="5">Response</option>
					<% } %>
					<option value="6">Booking Status</option>
					<option value="7">Reason for Rejection</option>
				</select>
				</a>
			</div>
			
			<div style="clear: both;">
			<br/>
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
							<th>Response</th>
							<th>Booking Status</th>
							<th>Reason for Rejection</th>
						</tr>
					<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<tr>
							<!--<th>#</th>-->
							<th>Team</th>
							<th>Presentation</th>
							<th>Date & Time</th>
							<th>Venue</th>
							<th>My Response</th>
							<th>Booking Status</th>
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
								<%--<s:if test="%{rejectReason != null)}">--%> 
								<td><s:property value="rejectReason"/></td>
								<%--</s:if><s:else>--%>
									<!--<td>-</td>-->
								<%--</s:else>--%>
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
							<%--<s:if test="%{rejectReason != null)}">--%> 
								<td><s:property value="rejectReason"/></td>
							<%--</s:if><s:else>--%>
								<!--<td>-</td>-->
							<%--</s:else>--%>
							<%--<% count = count + 1; %>--%>
						</tr>
						<% } %>
					</s:iterator>
					</tbody>
				</table>
				<br/><br/>
		</s:if><s:else>
			<div style="clear: both;">
				<h4>No bookings have been made!</h4>
			</div>
		</s:else>
		</div>
		</div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>
		<script type="text/javascript">
		//For data tables
		$(document).ready(function(){
			//To select all values in multiple select dropdown by default
			$('#hideColumns option').attr('selected', 'selected');
			var values = $("#hideColumns").val();
			$('#dropdownValues').val(values);
			$('#hideColumns option').removeAttr('selected');
			
			$('#hideColumns').change(function(){
				var n = $('#dropdownValues').val();
				var allValues = n.split(","); 
				//First show all columns then hide whichever column has been chosen
				for (var j=0; j<allValues.length; j++) {
					$('td:nth-child('+ allValues[j] +'),th:nth-child('+ allValues[j] +')').show();
				}
				//Hiding the columns which have been selected
				var selectedValues = $("#hideColumns").val();
				//Only if a column has been selected
				if (selectedValues.length > 0) {
					for (var i=0; i<selectedValues.length; i++) {
						$('td:nth-child('+ selectedValues[i] +'),th:nth-child('+ selectedValues[i] +')').hide();
					}
				}
			}); //end of function
		
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
			});
		});
		
		$(document).on('mouseenter','[rel=tooltip]', function(){
			$(this).tooltip('show');
		});

		$(document).on('mouseleave','[rel=tooltip]', function(){
			$(this).tooltip('hide');
		});
//		$("#teamHide").click(function() {
////                $('td:nth-child(2)').hide();
//			 // if your table has header(th), use this
//			 var thisCheck = $(this);
//			 if (thisCheck.is (':checked')) {
//				 $('td:nth-child(1),th:nth-child(1)').hide();
//			 } else {
//				 $('td:nth-child(1),th:nth-child(1)').show();
//			 }
//		 });
		</script>
    </body>
</html>
