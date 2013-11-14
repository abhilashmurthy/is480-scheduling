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

        <%@include file="footer.jsp"%>
		<link type="text/css" rel="stylsheet" hred="css/bootstrap.css">



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
			
			<div>
				<h3>Manage Reminder Settings</h3>

				<s:iterator value="data"> 

					<div style="float: left; margin-right: 50px;">
					<table class="table ">

						<% String eStatus = (String) request.getAttribute("emailStatus");%> 
						<% String eStatusClear = (String) request.getAttribute("emailClearStatus");%>
						<thead>
						<tr style="color:blue">
							<th style="float:left;border:none"><i class="fa fa-white fa-envelope"></i>Email Reminders:</td></th>
						</thead>
						<tr><td style="float:left;">

								<div id="emailStatusBlock">
									Remind pending bookings (days):&nbsp&nbsp;

									<form>

										<% if (eStatus.equalsIgnoreCase("On")) {%>
										<input type="radio" id="onPrefa" class="pref" name="pref" value="On" onchange="onOption(onPrefa)" checked>&nbsp; On 
										<input type="radio" id="offPrefb" class="pref" name="pref" value="Off" onchange="onOption(offPrefb)">&nbsp; Off
										<% } else {%>
										<input type="radio" id="onPref1" class="pref" name="pref" value="On" onchange="onOption(onPref1)">&nbsp; On 
										<input type="radio" id="offPref2" class="pref" name="pref" value="Off" onchange="onOption(offPref2)" checked>&nbsp; Off
										<% }%>
									</form></div></td>

							<td id="emailFrequencyRow" style="float:left;border:none">

								<div id="emailFrequencyBlock">

									<div class="input-append">

										<input id="emailFrequency" style="width: 18px;height: 20px" type="text" value="<s:property value="emailFrequency"/>" disabled />
										<div class="btn-group">
											<button class="btn" type="button" onclick="upOne(emailFrequency);" >&#9650;</button>
											<button class="btn" type="button" onclick="downOne(emailFrequency);" >&#9660;</button>
										</div>
									</div>
								</div>
						</td></tr>



						<!-- to do for clear pending bookings --> <tr><td style="float:left;border:none">
								<form>
									Remove pending bookings (days):
									<br/>
									<% if (eStatusClear.equalsIgnoreCase("On")) {%>
									<input type="radio" id="onPrefe" class="pref" name="pref" value="On" onchange="onOption3(onPrefe)" checked>&nbsp; On 
									<input type="radio" id="offPreff" class="pref" name="pref" value="Off" onchange="onOption3(offPreff)">&nbsp; Off
									<% } else {%>
									<input type="radio" id="onPref5" class="pref" name="pref" value="On" onchange="onOption3(onPref5)">&nbsp; On 
									<input type="radio" id="offPref6" class="pref" name="pref" value="Off" onchange="onOption3(offPref6)" checked>&nbsp; Off
									<% }%>
								</form>

								</div></td>

						<td id="emailClearPendingRow" style="float:left;border:none">
								<div id="emailClearPending">

									<div class="input-append">

										<input id="emailClear" style="width: 18px;height: 20px" type="text" value="<s:property value="emailClearFrequency"/>" disabled />
										<div class="btn-group">
											<button class="btn" type="button" onclick="upOne(emailClear);" >&#9650;</button>
											<button class="btn" type="button" onclick="downOne(emailClear);" >&#9660;</button>
										</div>
									</div>
								</div>
							</td>
						</tr>
					</table>
			</div>
	<!--					<tr>
							<td style="border:none"></td>
						</tr>-->
				<div style="float: right;margin-right:200px">
					<table class="table">
						<thead>
						<tr>
							<th style="color:blue;">
								<i class="fa fa-white fa-comment"></i>SMS Reminders:
							</th>
						</tr>
						</thead>

						<!-- SMS part -->
						<tr><td style="float:left;">

								<% String sStatus = (String) request.getAttribute("smsStatus");%> 

								Remind confirmed bookings (hours):&nbsp;
								<div id="smsStatusBlock">
									<form>

										<% if (sStatus.equalsIgnoreCase("On")) {%>
										<input type="radio" id="onPrefc" class="pref" name="pref" value="On" onchange="onOption2(onPrefc)" checked>&nbsp; On 
										<input type="radio" id="offPrefd" class="pref" name="pref" value="Off" onchange="onOption2(offPrefd)">&nbsp; Off
										<% } else {%>
										<input type="radio" id="onPref3" class="pref" name="pref" value="On" onchange="onOption2(onPref3)">&nbsp; On 
										<input type="radio" id="offPref4" class="pref" name="pref" value="Off" onchange="onOption2(offPref4)" checked>&nbsp; Off
										<% }%>
									</form>
								</div>
						</td>

						<td style="border:none;float:left">

							<div id="smsFrequencyBlock">
								<div class="input-append">	
									<input id="smsFrequency" style="width: 18px;height: 20px" type="text" value="<s:property value="smsFrequency"/>" disabled />
									<div class="btn-group">
										<button class="btn" type="button" onclick="upOne(smsFrequency);" >&#9650;</button>
										<button class="btn" type="button" onclick="downOne(smsFrequency);" >&#9660;</button>
									</div>
								</div>

							</div>

						</td>
						</tr>
					</table>
				</div>
				</s:iterator>

				<div style="clear: both;">
					<button type="button" id="saveButton" class="btn btn-primary" data-loading-text="Saving..."
							style="width:80px; height:30px;">
						<strong>Save</strong>
					</button>
				</div>
			</div>
			
			<!-- END OF REMINDER SETTINGS SECTION -->
			<br />
			<!-- ADMINISTRATOR PASSWORD SECTION -->
			
			<div>
				<h3>Change Administrator Password</h3>
				<table>
					<tr>
						<td>Current Password: </td>
						<td><input type="password" id="currentPassword"/></td>
					</tr>
					<tr>
						<td>New Password: </td>
						<td><input type="password" id="newPassword"/></td>
					</tr>
					<tr>
						<td>Confirm New Password: </td>
						<td><input type="password" id="verifyPassword"/></td>
					</tr>
				</table> <br />
				<button id="passwordChangeSubmitButton" class="btn btn-primary" data-loading-text="Saving...">Save</button>
			</div>
			
			<!-- END OF ADMINISTRATOR PASSWORD SECTION -->
			
			<script type="text/javascript">

				var loadForm = function() {
						//if it is off don't show
						var values = $('input:checked').map(function() {
							return this.value;
						}).get();

						var splitArray = new Array();
						splitArray = values.slice(',');
						console.log(splitArray);
						//alert(splitArray[0] + splitArray[1]);

						var ons = "On";
						//this is for email
						if (splitArray[0] !== ons) {

							onOption("offPrefb");
							onOption("offPref2");

						}

						if (splitArray[2] !== ons) {

							onOption2("offPrefd");
							onOption2("offPref4");

						}

						if (splitArray[1] !== ons) {

							onOption3("offPreff");
							onOption3("offPref6");

						}
						
						$("#passwordChangeSubmitButton").on("click", changePassword);
					};


				function upOne(id) {
					id.value++;
				}

				function downOne(id) {
					if (parseInt(id.value, 0) > 1) {
						id.value--;
					}
				}

				function onOption(id) {

					var status = id.value;

					var ons = "On";
					if (status !== ons) {
						//if it is on show the  frequency
						jQuery(emailFrequencyRow).hide();
					} else {
						jQuery(emailFrequencyRow).show();

					}

				}

				function onOption2(id) {

					var status = id.value;

					var ons = "On";
					if (status !== ons) {
						//if it is on show the  frequency
						jQuery(smsFrequencyBlock).hide();
					} else {
						jQuery(smsFrequencyBlock).show();
					}

				}

				function onOption3(id) {

					var status = id.value;

					var ons = "On";
					if (status !== ons) {
						//if it is on show the  frequency
						jQuery(emailClearPendingRow).hide();
					} else {
						jQuery(emailClearPendingRow).show();
					}

				}

				$("#saveButton").on('click', function(e) {
					//$(this).button('loading');
					e.preventDefault();
					e.stopPropagation();
					var values = $('input:checked').map(function() {
						return this.value;
					}).get();

					var splitArray = new Array();
					splitArray = values.slice(',');
					

					var ons = "On";

					var toSend = "email,";
					//this is for email frequency
					if (splitArray[0] === ons) {

						//if on get the frequency
						var eFrq = document.getElementById("emailFrequency").value;
						toSend += "On," + eFrq + ",";

					} else {

						//if off, then set frequency to nothing
						toSend += "Off," + "0,";

					}
					
					toSend += "sms,";

					//this is for sms
					if (splitArray[2] === ons) {

						//if on get the frequency
						var eFrq = document.getElementById("smsFrequency").value;
						toSend += "On," + eFrq;

					} else {

						//if off, then set frequency to nothing
						toSend += "Off," + "0";

					}
					
					//this is for clear email
					toSend += ",toClear,";
					if (splitArray[1] === ons) {

						//if on get the frequency
						var eFrq = document.getElementById("emailClear").value;
						toSend += "On," + eFrq + ",";

					} else {

						//if off, then set frequency to nothing
						toSend += "Off," + "0,";

					}
					
					console.log(toSend);

					//send data to store information
					$.ajax({
						type: 'POST',
						async: false,
						url: 'updateNotificationSettings',
						data: {settingDetails: toSend}
					}).done(function(response) {
						if (!response.exception) {
							if (response.success) {
								showNotification("SUCCESS", response.message);
							} else {
								showNotification("INFO", response.message);
							}
							//timedRefresh(2000);
						} else {
							var eid = btoa(response.message);
							window.location = "error.jsp?eid=" + eid;
						}
					}).fail(function(response) {
						//$("#saveButton").button('reset');
						//$("#addRowBtn").button('reset');
						showNotification("WARNING", "Oops.. something went wrong");
					});

					return false;
				});
				
				function changePassword() {
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
					console.log(testingInput);
					$.ajax({
						type: 'POST',
						async: false,
						url: 'changePassword',
						data: {jsonData: JSON.stringify(testingInput)}
					}).done(function(response) {
						$("#passwordChangeSubmitButton").button('reset');
						console.log(JSON.stringify(response));
						var success = response.success;
						var notificationType = (success) ? "SUCCESS" : "ERROR";
						var message = (response.message) ? response.message : "Password changed successfully" ;
						showNotification(notificationType, message);
					}).fail(function(response) {
						$("#passwordChangeSubmitButton").button('reset');
						console.log(JSON.stringify(response));
						showNotification("ERROR", "Error in contacting the server. Please try again.");
					});
					
					return false;
				}

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

				//Append page load functions
				addLoadEvent(loadForm);
			</script>
    </body>
</html>
