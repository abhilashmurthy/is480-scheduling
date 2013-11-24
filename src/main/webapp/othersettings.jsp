<%-- 
    Document   : Manage Notifications
    Created on : Sep 19, 2013, 7:04:41 AM
    Author     : Tarlochan
--%>

<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
		<meta charset=”utf-8”> 
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Other Settings </title>
    </head>
    <body>
		<%@include file="navbar.jsp" %>
		<!-- Kick unauthorized user -->
				<%	if (!activeRole.equals(Role.ADMINISTRATOR) && !activeRole.equals(Role.COURSE_COORDINATOR)) {
						request.setAttribute("error", "Oops. You are not authorized to access this page!");
						RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
						rd.forward(request, response);
					}
				%>
        
        <div class="container">
			<!-- REMINDER SETTINGS SECTION -->
			<div class='reminderSettings fuelux'>
				<h3>Settings</h3>
				<table class='otherSettingsTable'>
					<tr><td colspan="4"><h4>Schedule</h4></td></tr>
					<tr class='reminderItem'>
						<td>Delete <b>booking</b> if pending for</td>
						<td class='spinnerTd'>
							<div id='clear_reminder' class="clearPendingSpinner reminderSpinner spinner">
								<input class="durationInput spinner-input" type="text" style='width: 50px !important'/>
								<div class="spinner-buttons btn-group btn-group-vertical">
									<button class="btn spinner-up" type="button">
										<i class="fa fa-chevron-up"></i>
									</button>
									<button class="btn spinner-down" type="button">
										<i class="fa fa-chevron-down"></i>
									</button>
								</div>
							</div>
						</td>
						<td>days</td>
						<td class='reminderInfo muted'>(0 = disabled)</td>
					</tr>
					<tr><td colspan="4"><h4>Email</h4></td></tr>
					<tr class='reminderItem'>
						<td>Remind <b>faculty</b> to approve booking in advance of</td>
						<td class='spinnerTd'>
							<div id='approve_reminder' class="approveReminderSpinner reminderSpinner spinner">
								<input class="durationInput spinner-input" type="text" style='width: 50px !important'/>
								<div class="spinner-buttons btn-group btn-group-vertical">
									<button class="btn spinner-up" type="button">
										<i class="fa fa-chevron-up"></i>
									</button>
									<button class="btn spinner-down" type="button">
										<i class="fa fa-chevron-down"></i>
									</button>
								</div>
							</div>
						</td>
						<td>days</td>
						<td class='reminderInfo muted'>(0 = disabled)</td>
					</tr>
					<tr><td colspan="4"><h4>SMS</h4></td></tr>
					<tr class='reminderItem'>
						<td>Remind <b>user</b> to attend presentation in advance of </td>
						<td class='spinnerTd'>
							<div id='attend_reminder' class="attendReminderSpinner reminderSpinner spinner">
								<input class="durationInput spinner-input" type="text" style='width: 50px !important'/>
								<div class="spinner-buttons btn-group btn-group-vertical">
									<button class="btn spinner-up" type="button">
										<i class="fa fa-chevron-up"></i>
									</button>
									<button class="btn spinner-down" type="button">
										<i class="fa fa-chevron-down"></i>
									</button>
								</div>
							</div>
						</td>
						<td>hours</td>
						<td class='reminderInfo muted'>(0 = disabled)</td>
					</tr>
					<tr class='reminderItem'><td colspan="4"><button id="remindersSubmitBtn" class="btn btn-primary" data-loading-text="Saving...">Save</button></td></tr>
				</table>
			</div>
			
			<!-- END OF REMINDER SETTINGS SECTION -->
			<!-- ADMINISTRATOR PASSWORD SECTION -->
			
			<div class='passwordSettings'>
				<h4>Administrator Password</h4>
				<table id='passwordChangeTable'>
					<tr>
						<td>Current Password </td>
						<td><input type="password" id="currentPassword"/></td>
					</tr>
					<tr>
						<td>New Password </td>
						<td><input type="password" id="newPassword"/></td>
					</tr>
					<tr>
						<td>Confirm New Password </td>
						<td><input type="password" id="verifyPassword"/></td>
					</tr>
				</table> <br />
				<button id="passwordChangeSubmitButton" class="btn btn-primary" data-loading-text="Saving...">Save</button>
			</div>
			<!-- END OF ADMINISTRATOR PASSWORD SECTION -->
		</div>
			<%@include file="footer.jsp" %>
			<script type="text/javascript">
				var otherSettingsLoad = function() {
				var remindersJson = JSON.parse('<s:property escape="false" value="remindersJson"/>');
				console.log(JSON.stringify(remindersJson));
				readCrappyJson();
				
				function readCrappyJson(){
					//Remind faculty
					if (remindersJson[0].emailStatus === "On") {
						$('.approveReminderSpinner').spinner({
							min: 0,
							max: 7
						});
						$('.approveReminderSpinner').spinner('value', remindersJson[0].emailFrequency);
					} else {
						$('.approveReminderSpinner').spinner('value', 0);
					}
					
					//SMS attend
					if (remindersJson[1].smsStatus === "On") {
						$('.attendReminderSpinner').spinner({
							min: 0,
							max: 72
						});
						$('.attendReminderSpinner').spinner('value', remindersJson[1].smsFrequency);
					} else {
						$('.attendReminderSpinner').spinner('value', 0);
					}
					
					//Clear booking
					if (remindersJson[2].emailClearStatus === "On") {
						$('.clearPendingSpinner').spinner({
							min: 0,
							max: 7
						});
						$('.clearPendingSpinner').spinner('value', remindersJson[2].emailClearFrequency);
					} else {
						$('.clearPendingSpinner').spinner('value', 0);
					}
				}
				
				$('body').on('click', '#remindersSubmitBtn', function(e){
					if (uatMode) recordHumanInteraction(e);
					var crappyInputJson = '';
					if ($('.approveReminderSpinner').spinner('value')) crappyInputJson += 'email,On,' + $('.approveReminderSpinner').spinner('value') + ',';
					else crappyInputJson += 'email,Off,0,';
					if ($('.attendReminderSpinner').spinner('value')) crappyInputJson += 'sms,On,' + $('.attendReminderSpinner').spinner('value') + ',';
					else crappyInputJson += 'sms,Off,0,';
					if ($('.clearPendingSpinner').spinner('value')) crappyInputJson += 'toClear,On,' + $('.clearPendingSpinner').spinner('value');
					else crappyInputJson += 'toClear,Off,0';
					console.log('Submitting ' + crappyInputJson);
					$.ajax({
						type: 'POST',
						async: false,
						url: 'updateNotificationSettings',
						data: {settingDetails: crappyInputJson}
					}).done(function(response) {
						if (!response.exception) {
							if (response.success) {
								showNotification("SUCCESS", response.message);
							} else {
								showNotification("INFO", response.message);
							}
						} else {
							var eid = btoa(response.message);
							window.location = "error.jsp?eid=" + eid;
						}
					}).fail(function(response) {
						showNotification("WARNING", "Oops.. something went wrong");
					});
					return false;
				});
				
				$('body').on('click', '#passwordChangeSubmitButton', function(){
					if (uatMode) recordHumanInteraction(e);
					$("#passwordChangeSubmitButton").button('loading');
					var currentPasswordStr = $("#currentPassword").val();
					var newPasswordStr = $("#newPassword").val();
					var verifyPasswordStr = $("#verifyPassword").val();
					
					//Validate if the new passwords match
					if (newPasswordStr !== verifyPasswordStr) {
						showNotification("ERROR", "New password doesn't match the confirmation. Please try again!");
						$("#passwordChangeSubmitButton").button('reset');
						return false;
					}
					var testingInput = {
						currentPassword: currentPasswordStr,
						newPassword: newPasswordStr,
						verifyPassword: verifyPasswordStr
					};
					console.log('Submitting: ' + JSON.stringify(testingInput));
					$.ajax({
						type: 'POST',
						async: false,
						url: 'changePassword',
						data: {jsonData: JSON.stringify(testingInput)}
					}).done(function(response) {
						$("#passwordChangeSubmitButton").button('reset');
						var success = response.success;
						var notificationType = (success) ? "SUCCESS" : "ERROR";
						var message = (response.message) ? response.message : "Password changed successfully";
								showNotification(notificationType, message);
							}).fail(function(response) {
								$("#passwordChangeSubmitButton").button('reset');
								showNotification("ERROR", "Error in contacting the server. Please try again.");
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

				//Append page load functions
				addLoadEvent(otherSettingsLoad);
			</script>
    </body>
</html>
