<%-- 
    Document   : mysubscriptions
    Created on : Oct 17, 2013, 6:15:21 PM
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
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | My Subscriptions</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
		 <!-- Kick unauthorized user -->
        <%
            if (activeRole.equals(Role.COURSE_COORDINATOR) || activeRole.equals(Role.GUEST)) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
		 
        <div class="container">
			<h3 style="float: left; margin-right: 50px;">My RSVP's</h3>
			<s:if test="%{data != null && data.size() > 0}"> 
					<table id="mySubscriptionsTable" class="table table-hover" style="font-size: 13px;">
						<thead>
							<tr>
								<th></th>
								<th>Presenting Team</th>
								<th>Milestone</th>
								<th>Date of Presentation</th>
								<th>Venue</th>
								<th>More Information</th>
								<th style="width:110px">Cancel RSVP</th>
							</tr>
						</thead>
						<tbody> 
							<s:iterator value="data">
								<s:if test="%{bookingStatus.equalsIgnoreCase('Pending')}"> 
									<tr class="warning" style="height:40px">
								</s:if>
								<s:if test="%{bookingStatus.equalsIgnoreCase('Approved')}">
									<tr class="success" style="height:40px">
								</s:if>
								<s:if test="%{bookingStatus.equalsIgnoreCase('Rejected')}">
									<tr class="error" style="height:40px">
								</s:if>
								<s:if test="%{bookingStatus.equalsIgnoreCase('Deleted')}">
									<tr class="info" style="height:40px">
								</s:if>
<!--								<tr class="success" style="height:40px">-->
									<td style="vertical-align: middle"><i class="fa fa-check"></i></td>
									<td style="vertical-align: middle"><s:property value="teamName"/></td>
									<td style="vertical-align: middle"><s:property value="termMilestone"/></td>
									<td style="vertical-align: middle"><s:property value="time"/></td>
									<td style="vertical-align: middle"><s:property value="venue"/></td>
									<td style="vertical-align: middle"><s:property value="wikiLink"/></td>
									<td style="width:90px; text-align:center;vertical-align: middle"> 
										<button type="button" class="unsubscribeBtn updateStatusBtn btn btn-danger" value="<s:property value="bookingId"/>"
												name="Unsubscribe!"><i class="fa fa-trash-o fa-white"></i>
										</button>
									</td>
								</tr>
							</s:iterator>
							</tbody>
						</table>
						<br/><br/>
			</s:if><s:else>
				<div style="clear: both;">
					<h4>You are yet to RSVP to any presentations!</h4>
				</div>
			</s:else>
		</div>
		 
		<%@include file="footer.jsp"%>
		
		<script type='text/javascript'>
			mySubscriptionsLoad = function() {
				var activeBtn = null;
				
				//Unsubscribing user from the booking
					$('.unsubscribeBtn').on('click', function(e){
					var $this = $(this);
					$('.updateStatusBtn').attr('disabled', true);
					activeBtn = $this;
					console.log('Submitting: ' + JSON.stringify({bookingId: activeBtn.attr('value'), status: "Unsubscribe"}));
					$.ajax({
						type: 'POST',
						async: false,
						url: 'setSubscriptionStatus',
						data: {jsonData: JSON.stringify({subscribedBooking: activeBtn.attr('value'), subscriptionStatus: "Unsubscribe"})}
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
				
					$('#mySubscriptionsTable').dataTable({
//						"aLengthMenu": [
//							[5, 10, 20, -1],[5, 10, 20, "All"]], 
//						"iDisplayLength" : -1,
		//				"bPaginate": false,
						"bLengthChange": false,
		//				"bFilter": false,
//						"bSort": false,
						"bInfo": false,
		//				"bAutoWidth": false,
		//				"asStripClasses": null,
						//To prevent highlighing of sorted column
						"bSortClasses": false,
						"aaSorting":[]
					});

					$('.dataTables_filter input').attr("placeholder", "e.g. SIS SR 2.1");
					$('.dataTables_filter input').attr("title", "Search any keyword in the table below");
					$('.dataTables_filter input').on('mouseenter', function(){
						$(this).tooltip('show');
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
			addLoadEvent(mySubscriptionsLoad);
		</script>
    </body>
</html>
