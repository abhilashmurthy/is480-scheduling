<%--
    Document   : ApproveReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Tarlochan
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
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Approve</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
		 <!-- Kick unauthorized user -->
        <%
            if (!activeRole.equals(Role.FACULTY)) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
		 
        <div class="container">
			<h3 style="float: left; margin-right: 25px;">Approve Booking</h3>
			<!-- TERM SELECTION DROP DOWN -->
			<div style="float:left; margin-top:16px" class="btn-group">
				<a class="btn dropdown-toggle" data-toggle="dropdown" href="#" >
					<b><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></b> <span class="caret"></span>
				</a>
				<ul class="dropdown-menu">
					<form action="approveReject" method="post">
						<!--<select name="termId" style="float:right" onchange="this.form.submit()">--> 
							<s:iterator value="termData">
								<li>
									<button type="submit" class="btn btn-link" name="chosenTermId" value="<s:property value="termId"/>">
										<s:property value="termName"/>
									</button>
								</li>
							</s:iterator>
						<!--</select>-->
					</form>
				</ul>
			</div>
<!--			<div style="float:right; margin-top:20px">
				<form action="approveReject" method="post">
					Select Term: <select name="chosenTermId" onchange="this.form.submit()">
						<option value='<%= ((Term)session.getAttribute("currentActiveTerm")).getId() %>'><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></option>
						<s:iterator value="termData">
							<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
						</s:iterator>
					</select>
				</form>
			</div>-->
			<!-- For switching between Pending and Confirmed Tabs -->
			<div class="tabbable" style="clear: both;">
				<br/>
				<ul id="tabs" class="nav nav-tabs" data-tab="tabs">  
					<li class="active">
						<a href="#pendingBookingSection" data-toggle="tab">Pending Bookings</a>
					</li>     
					<li>
						<a href="#confirmedBookingSection" data-toggle="tab">Confirmed Bookings</a>
					</li>  
				</ul>
			</div>
			
			<div id="my-tab-content" class="tab-content">
				<div class="tab-pane active" id="pendingBookingSection">
				<s:if test="%{pendingData != null && pendingData.size() > 0}"> 
						<table id="approveRejectTable" class="table table-hover" style="font-size: 13px;">
							<thead>
								<tr>
									<th>Team Name</th>
									<th>Presentation</th>
									<th>My Role</th>
									<th>Date & Time</th>
									<th>Venue</th>
									<th>My Status</th>
									<th></th>
								</tr>
							</thead>
							<tbody> 
								<s:iterator value="pendingData">
									<tr class="warning" style="height:50px">
										<td style="vertical-align: middle"><s:property value="teamName"/></td>
										<td style="vertical-align: middle"><s:property value="milestone"/></td>
										<td style="vertical-align: middle"><s:property value="userRole"/></td>
										<td style="vertical-align: middle"><s:property value="date"/> <s:property value="time"/></td>
										<td style="vertical-align: middle"><s:property value="venue"/></td>
										<td style="vertical-align: middle">
											<s:property value="myStatus"/>
										</td>
										<td style="vertical-align: middle">
											<button type="button" class="approveBookingBtn updateStatusBtn btn btn-success" value="<s:property value="bookingId"/>" name="approve">
												Approve
											</button>
											<span class="button-divider">
											<button type="button" class="rejectBookingBtn updateStatusBtn btn btn-danger" value="<s:property value="bookingId"/>" name="reject">
												Reject
											</button>
											</span>
										</td>
									</tr>
								</s:iterator>
								</tbody>
							</table>
							<br/><br/>
					<div class="modal hide fade in" id="rejectionModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
							<h3 id="myModalLabel">Information Required</h3>
						</div>
						<div class="modal-body">
							<form id="rejectForm">
							<table>
								<tr>
									<td width="150px">
										Reason for Rejecting <br/>
										<i style="font-size: 13px;">(55 characters max.)</i>
									</td>
									<!--<th>Add Proxy</th>-->
									<td><textarea rows="1" id="rejectionText" name="rejectiontText" style="width:350px; height:50px;" 
												  placeholder="Unexpected Meeting..." maxlength="55"></textarea>
									</td>
								</tr>
								<tr>
									<td></td>
									<td>
										<span id="errorMsg" class="hide text-error">Please enter a reason for rejecting this booking!</span>
									</td>
								</tr>
							</table>
							</form>
						</div>
						<div class="modal-footer">
							<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
							<button class="btn btn-primary" data-dismiss="modal" id="rejectionTextSubmit">Save</button>
						</div>
					</div>
				</s:if><s:else>
					<div style="clear: both;">
					<h4>No pending bookings available!</h4>
					</div>
				</s:else>
			<h4 id="approveRejectMessage"></h4>
			</div>

			<div class="tab-pane" id="confirmedBookingSection">
				<s:if test="%{confirmedData != null && confirmedData.size() > 0}"> 
						<table id="confirmTable" class="table table-hover" style="font-size: 13px;">
							<thead>
								<tr>
									<th>Team Name</th>
									<th>Presentation</th>
									<th>My Role</th>
									<th>Date & Time</th>
									<th>Venue</th>
									<th>My Status</th>
									<th></th>
								</tr>
							</thead>
							<tbody> 
								<s:iterator value="confirmedData">
									<tr class="success" style="height:50px">
										<td style="width: 50px; vertical-align: middle"><s:property value="teamName"/></td>
										<td style="width: 50px; vertical-align: middle"><s:property value="milestone"/></td>
										<td style="width: 50px; vertical-align: middle"><s:property value="userRole"/></td>
										<td style="width: 100px; vertical-align: middle"><s:property value="date"/> <s:property value="time"/></td>
										<td style="width: 80px; vertical-align: middle"><s:property value="venue"/></td>
										<td style="width: 50px; vertical-align: middle">
											<s:property value="myStatus"/>
										</td>
										<td style="width:30px; vertical-align: middle">
											<button type="button" class="rejectBookingBtn updateStatusBtn btn btn-danger" value="<s:property value="bookingId"/>" name="reject">
												<i class="fa fa-times">&nbsp; Revoke</i>
											</button>
										</td>
									</tr>
								</s:iterator>
								</tbody>
							</table>
							<br/><br/>
					<div class="modal hide fade in" id="rejectionModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
							<h3 id="myModalLabel">Information Required</h3>
						</div>
						<div class="modal-body">
							<form id="rejectForm">
							<table>
								<tr>
									<td width="150px">
										Reason for Rejecting <br/>
										<i style="font-size: 13px;">(55 characters max.)</i>
									</td>
									<!--<th>Add Proxy</th>-->
									<td><textarea rows="1" id="rejectionText" name="rejectiontText" style="width:350px; height:50px;" 
												  placeholder="Unexpected Meeting..." maxlength="55"></textarea>
									</td>
								</tr>
								<tr>
									<td></td>
									<td>
										<span id="errorMsg" class="hide text-error">Please enter a reason for rejecting this booking!</span>
									</td>
								</tr>
							</table>
							</form>
						</div>
						<div class="modal-footer">
							<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
							<button class="btn btn-primary" data-dismiss="modal" id="rejectionTextSubmit">Save</button>
						</div>
					</div>
				</s:if><s:else>
					<div style="clear: both;">
					<h4>No bookings have yet been confirmed!</h4>
					</div>
				</s:else>
			</div>
		</div>
		<%@include file="footer.jsp"%>
		
		<script type='text/javascript'>
			approveRejectLoad = function() {
//				jQuery(document).ready(function($) {
//					$('#tabs').tab();
//				});
				var activeBtn = null;
					
					$('#confirmTable').dataTable({
						"aLengthMenu": [
							[5, 10, 20, -1],[5, 10, 20, "All"]], 
						"iDisplayLength" : -1,
//						"bPaginate": false,
		//				"bLengthChange": false,
		//				"bFilter": false,
		//				"bSort": false,
						"bInfo": false,
		//				"bAutoWidth": false,
		//				"asStripClasses": null,
						//To prevent highlighing of sorted column
						"bSortClasses": false,
						"aaSorting":[],
						"fnDrawCallback": function() {
							if ($("#confirmTable").find("tr:not(.ui-widget-header)").length <= 5) {
								  $('div.dataTables_paginate')[0].style.display = "none";
							} 
							else {
								  $('div.dataTables_paginate')[0].style.display = "block";
							}
						}
					});
					
					$('#approveRejectTable').dataTable({
						"aLengthMenu": [
							[5, 10, 20, -1],[5, 10, 20, "All"]], 
						"iDisplayLength" : -1,
		//				"bPaginate": false,
		//				"bLengthChange": false,
		//				"bFilter": false,
		//				"bSort": false,
						"bInfo": false,
		//				"bAutoWidth": false,
		//				"asStripClasses": null,
						//To prevent highlighing of sorted column
						"bSortClasses": false,
						"aaSorting":[],
						"fnDrawCallback": function() {
							if ($("#approveRejectTable").find("tr:not(.ui-widget-header)").length <= 5) {
								  $('div.dataTables_paginate')[0].style.display = "none";
							} 
							else {
								  $('div.dataTables_paginate')[0].style.display = "block";
							}
						}
					});

					$('.dataTables_filter input').attr("placeholder", "e.g. Acceptance");
					$('.dataTables_filter input').attr("title", "Search any keyword in the table below");
					$('.dataTables_filter input').on('mouseenter', function(){
						$(this).tooltip('show');
					});
				
				$('.approveBookingBtn').on('click', function(e){
					var $this = $(this);
					$('.updateStatusBtn').attr('disabled', true);
					activeBtn = $this;
					console.log('Submitting: ' + JSON.stringify({bookingId: activeBtn.attr('value'), status: "approve"}));
					$.ajax({
						type: 'POST',
						async: false,
						url: 'updateBookingStatus',
						data: {jsonData: JSON.stringify({bookingId: activeBtn.attr('value'), status: "approve"})}
					})
					.done(function(response) {
						console.log('Got ' + JSON.stringify(response));
						if (!response.exception) {
							if (response.success) {
								showNotification("SUCCESS", response.message);
								activeBtn.closest('tr').fadeOut('slow', function(){
									activeBtn.closest('tr').remove();
								});
								$('.updateStatusBtn').attr('disabled', false);
							} else {
								showNotification("ERROR", response.message);
							}
						} else {
							var eid = btoa(response.message);
							window.location = "error.jsp?eid=" + eid;
						}
					}).fail(function(error) {
						   showNotification("WARNING", "Oops.. something went wrong");
					});
					return false;
				});
				
				$(".rejectBookingBtn").on('click', function(e){
					$('.updateStatusBtn').attr('disabled', true);
					$('#rejectionModal').modal({
						keyboard: true
					});
					$('#rejectionModal').modal('show');
					activeBtn = $(this);
					return false;
				});
				
				$('#rejectionModal').on('shown', function (e){
					$('#rejectionText').focus();
				});
				
				$('#rejectionModal').on('hidden', function(e){
					$('.updateStatusBtn').attr('disabled', false);
				});
				
				$('#rejectionTextSubmit').on('click', function(e){
					if ($('#rejectionText').val() === "") {
						$('#errorMsg').show();
						return false;
					}
					$('#rejectionModal').modal('hide');
					console.log('Submitting: ' + JSON.stringify({bookingId: activeBtn.attr('value'), status: "reject", comment: $('#rejectionText').val()}));
					$.ajax({
						type: 'POST',
						async: false,
						url: 'updateBookingStatus',
						data: {jsonData: JSON.stringify({bookingId: activeBtn.attr('value'), status: "reject", comment: $('#rejectionText').val()})}
					})
					.done(function(response) {
						if (!response.exception) {
							if (response.success) {
								showNotification("SUCCESS", response.message);
								activeBtn.closest('tr').fadeOut('slow', function(){
									activeBtn.closest('tr').remove();
								});
								$('.updateStatusBtn').attr('disabled', false);
							} else {
								showNotification("ERROR", response.message);
							}
						} else {
							var eid = btoa(response.message);
							window.location = "error.jsp?eid=" + eid;
						}
					}).fail(function(error) {
						   $('button[type=button]').attr('disabled', false);
						   showNotification("WARNING", "Oops.. something went wrong");
					});
					return false;
				});
				
				//Notification-------------
				function showNotification(action, notificationMessage) {
					var opts = {
						title: "Note",
						text: notificationMessage,
						type: "warning",
						icon: false,
						sticker: false,
						mouse_reset: false,
						animation: "fade",
						animate_speed: "fast",
						before_open: function(pnotify) {
							pnotify.css({
							   top: "52px",
							   left: ($(window).width() / 2) - (pnotify.width() / 2)
							});
						}
					};
					switch (action) {
						case "SUCCESS":
							opts.title = "Updated";
							opts.type = "success";
							break;
						case "ERROR":
							opts.title = "Error";
							opts.type = "error";
							break;
						case "INFO":
							opts.title = "Error";
							opts.type = "info";
							break;
						case "WARNING":
							$.pnotify_remove_all();
							opts.title = "Note";
							opts.type = "warning";
							break;
						default:
							alert("Something went wrong");
					}
					$.pnotify(opts);
				}
				
			};
			
			addLoadEvent(approveRejectLoad);
		</script>
    </body>
</html>
