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
		<% } else if (activeR.equals(Role.TA)) { %>
			IS480 Scheduling System | My Sign Ups!
		<% } else { %>
			IS480 Scheduling System | My Bookings
		<% } %>
		</title>
		<link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
		<link rel="stylesheet" href="css/bootstrap-multiselect.css" type="text/css">
    </head>
    <body>
		<%@include file="navbar.jsp" %>
		<div class="container">
		<!-- TERM SELECTION DROP DOWN -->
		<div style="float:right; margin-top:20px">
			<form action="bookingHistory" method="post">
				Select Term: <select name="chosenTermId" onchange="this.form.submit()">
					<option value='<%= ((Term)session.getAttribute("currentActiveTerm")).getId() %>'><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></option>
					<s:iterator value="termData">
						<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
					</s:iterator>
				</select>
			</form>
		</div>
		<% if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)){ %>
			<h3 style="float: left; margin-right: 50px;">All Bookings</h3> 
		<% } else if (activeR.equals(Role.TA)) { %>
			<h3 style="float: left; margin-right: 50px;">My Sign Ups</h3> 
		<% } else { %>
			<h3 style="float: left; margin-right: 50px;">My Bookings</h3> 
		<% } %>
		<s:if test="%{data != null && data.size() > 0}">
			<% if (!activeRole.equals(Role.STUDENT) && !activeRole.equals(Role.TA) && !activeRole.equals(Role.FACULTY)) { %>
			<div style="float:right; clear:both ;margin-bottom:16px">
				<input type="hidden" id="dropdownValues"/>
				Hide/Show Columns:
				<!--<a rel="tooltip" data-placement="bottom" title="Press Ctrl to select / deselect columns">-->
				<select id="hideColumns" class="multiselect" multiple="multiple">
					<option value="0">Select/Deselect All</option>
					<% if (!activeRole.equals(Role.STUDENT)){ %>
						<option value="1">Team</option>
					<% } %>
					<option value="2">Presentation</option>
					<option value="3">Date & Time</option>
					<option value="4">Venue</option>
					<% if (!activeRole.equals(Role.TA)) { %>
						<% if (activeRole.equals(Role.FACULTY)) { %>
							<option value="5">My Response</option>
						<% } else { %>
							<option value="5">Response</option>
						<% } %>
					<% } %>
					<% if (!activeRole.equals(Role.TA)) { %>
						<option value="6">Booking Status</option>
					<% } %>
					<% if (!activeRole.equals(Role.TA) && !activeRole.equals(Role.FACULTY)) { %>
						<option value="7">Subscribed Users</option>
					<% } %>
					<% if (!activeRole.equals(Role.TA)) { %>
						<option value="8">Reason for Rejection</option>
					<% } %>
					<% if (!activeRole.equals(Role.TA)) { %>
						<option value="9">Last Modified</option>
					<% } %>
				</select>
				<!--</a>-->
			</div>
			<% } %>
			<div style="clear: both;">
			<table id="bookingHistoryTable" class="table table-hover zebra-striped" style="font-size: 13px;">
				<thead>
					<% if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) 
							|| activeRole.equals(Role.COURSE_COORDINATOR)) { %>
						<tr>
							<!--<th>#</th>-->
							<% if (!activeRole.equals(Role.STUDENT)) { %>
							<th>
								Team
							</th>
							<% } %> 
							<th>Presentation</th>
							<th>Date & Time</th>
							<th>Venue</th>
							<th>Response</th>
							<th>Booking Status</th>
							<th style="width:50px; text-align:center">Subscribed Users</th>
							<th style="text-align:center">Reason for Rejection</th>
							<th>Last Modified</th>
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
							<th style="text-align:center">Reason for Rejection</th>
							<th>Last Modified</th>
						</tr>
					<% } else { %>
						<tr>
							<!--For TA's-->
							<th>Team</th>
							<th>Presentation</th>
							<th>Date & Time</th>
							<th>Venue</th>
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
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Approved')}">
							<tr class="success">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Rejected')}">
							<tr class="error">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Deleted')}">
							<tr class="info">
						</s:if>
								<%--<td><%= count %></td>--%>
								<% if (!activeRole.equals(Role.STUDENT)) { %>
									<td><s:property value="teamName"/></td>
								<% } %>
								<td><s:property value="milestone"/></td>
								<td style="width:110px"><s:property value="date"/> <br/> <s:property value="time"/></td>
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
								<td style="width:70px"><s:property value="overallBookingStatus"/></td>
								<%--<s:if test="%{rejectReason != null)}">--%> 
								<td style="width:50px; text-align:center">
									<s:if test="%{noOfSubscribers > 0}">
										<a href="#"><s:property value="noOfSubscribers"/></a>
									</s:if><s:else>
										-
									</s:else>
<!--									<div id="sUsers" style="visibility:hidden">
										<s:iterator value="subscribedUsers">
											<s:property value="username"/> <br/>
										</s:iterator>
									</div>-->
								</td>
								<td style="width:170px;">
									<s:if test="%{rejectReason.length() > 0}">
										<s:property value="rejectReason"/>
									</s:if><s:else>
										<div style="text-align:center">-</div>
									</s:else>	
								</td>
								<td style="width:140px"><s:property value="lastModifiedAt"/> by
									<s:property value="lastModifiedBy"/></td>
								<%--</s:if><s:else>--%>
									<!--<td>-</td>-->
								<%--</s:else>--%>
								<%--<% count = count + 1; %>--%>
							</tr>
						<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Pending')}"> 
							<tr class="warning">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Approved')}">
							<tr class="success">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Rejected')}">
							<tr class="error">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Deleted')}">
							<tr class="warning">
						</s:if>
							<%--<td><%= count %></td>--%>
							<td><s:property value="teamName"/></td>
							<td><s:property value="milestone"/></td>
							<td><s:property value="date"/> <br/> <s:property value="time"/></td>
							<td><s:property value="venue"/></td>
							<td style="width:110px"><s:property value="myStatus"/></td>
							<td style="width:130px">
								<s:property value="overallBookingStatus"/><br/><br/>
							</td>
							<%--<s:if test="%{rejectReason != null)}">--%> 
								<td style="width:200px">
									<s:if test="%{rejectReason.length() > 0}">
										<s:property value="rejectReason"/>
									</s:if><s:else>
										<div style="text-align:center">-</div>
									</s:else>	
								</td>
								<td style="width:200px"><s:property value="lastModifiedAt"/> by
									<s:property value="lastModifiedBy"/></td>
							<%--</s:if><s:else>--%>
								<!--<td>-</td>-->
							<%--</s:else>--%>
							<%--<% count = count + 1; %>--%>
						</tr>
						<% } else { %>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Pending')}"> 
							<tr class="warning">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Approved')}">
							<tr class="success">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Rejected')}">
							<tr class="error">
						</s:if>
						<s:if test="%{overallBookingStatus.equalsIgnoreCase('Deleted')}">
							<tr class="warning">
						</s:if>
							<%--<td><%= count %></td>--%>
							<td><s:property value="teamName"/></td>
							<td><s:property value="milestone"/></td>
							<td><s:property value="date"/> <s:property value="time"/></td>
							<td><s:property value="venue"/><br/><br/></td>
						</tr>
						<% } %>
					</s:iterator>
					</tbody>
				</table>
				<br/><br/>
		</s:if><s:else>
			<div style="clear: both;">
				<% if (activeR.equals(Role.TA)) { %>
					<h4> No sign ups yet! </h4>
				<% } else { %>
					<h4>No bookings have been made!</h4>
				<% } %>
			</div>
		</s:else>
		</div>
		</div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>
		<script type="text/javascript" src="js/plugins/bootstrap-multiselect.js"></script>
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
				console.log("All values: " + n);
				var allValues = n.split(","); 
				//First show all columns then hide whichever column has been chosen
				//Starting from 1st index, not 0th index
				for (var j=1; j<allValues.length; j++) {
					$('td:nth-child('+ allValues[j] +'),th:nth-child('+ allValues[j] +')').show();
				}
				//Hiding the columns which have been selected
				var selectedValues = $("#hideColumns").val();
				console.log("Value to hide: " + selectedValues);
				//Only if a column has been selected
				if (selectedValues.length > 0) {
					if (selectedValues.length > 1) {
						console.log("Multiple values: " + selectedValues);
						if (selectedValues[0] === "0") {
							for (var i=1; i<=9; i++) {
								$('td:nth-child('+ i +'),th:nth-child('+ i +')').hide();
							}
							$('#hideColumns option').attr('selected', 'selected');
						} else {
							for (var i=0; i<selectedValues.length; i++) {
								$('td:nth-child('+ selectedValues[i] +'),th:nth-child('+ selectedValues[i] +')').hide();
							}
						}
					} else {
						console.log("Single value: " + selectedValues);
						if (selectedValues[0] === "0") {
							for (var i=1; i<=9; i++) {
								$('td:nth-child('+ i +'),th:nth-child('+ i +')').hide();
							}
							$('#hideColumns option').attr('selected', 'selected');
						} else {
							for (var i=0; i<selectedValues.length; i++) {
								$('td:nth-child('+ selectedValues[i] +'),th:nth-child('+ selectedValues[i] +')').hide();
							}
						}
					}
				}
			}); //end of function
		
			var role = '<%= (Role) session.getAttribute("activeRole") %>';
			if (role === "STUDENT") {
				$('#bookingHistoryTable').dataTable({
					"aLengthMenu": [
						[10, 25, 50, 100, -1],[10, 25, 50, 100, "All"]], 
					"iDisplayLength" : -1,
	//				"bPaginate": false,
					"bLengthChange": false,
	//				"bFilter": false,
	//				"bSort": false,
					"bInfo": false,
	//				"bAutoWidth": false,
	//				"asStripClasses": null,
					"bSortClasses": false,
					"aaSorting":[]
	//				"oSearch": {"sSearch": "Initial search"}
				});
			} else {
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
					"bSortClasses": false,
					"aaSorting":[]
	//				"oSearch": {"sSearch": "Initial search"}
				});
			}
			
			//Tooltip and placeholder for search box of datatable
			$('.dataTables_filter input').attr("placeholder", "e.g. Acceptance");
			$('.dataTables_filter input').attr("title", "Search any keyword in the table below");
			$('.dataTables_filter input').on('mouseenter', function(){
				$(this).tooltip('show');
			});
			
			//For the multiselect dropdown
			$('.multiselect').multiselect({
//			   includeSelectAllOption: true,
//			   selectAllText: true,
//			   selectAllValue: 'multiselect-all',
			   buttonClass: 'btn',
			   buttonWidth: '175px',
			   buttonContainer: '<div class="btn-group" />',
			   maxHeight: false,
			   buttonText: function(options) {
				   if (options.length === 0) {
					   return 'None selected <b class="caret"></b>';
				   } else if (options.length === 8) {
					   return 'All selected <b class="caret"></b>';
				   } else if (options.length > 1) {
					   return options.length + ' selected <b class="caret"></b>';
				   } 
				   else {
					   var selected = '';
					   options.each(function() {
					   selected += $(this).text() + ', ';
					   });
					   return selected.substr(0, selected.length -2) + ' <b class="caret"></b>';
				   }
			   }
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
