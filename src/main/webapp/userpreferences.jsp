<%-- 
    Document   : userPreferences
    Created on : Sep 10, 2013, 2:06:49 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Preferences </title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
        <div class="container">
			<h3>Manage Preferences</h3>
			<br>
			<table id="smsTable" class="table">
				<tbody>
					<tr align="center">
						<td style="width:250px">Subscribe to SMS Notification:</td>
<!--						<td style="width:50px"> 
							<input type="radio" id="onPref" class="pref" name="pref" value="on">&nbsp; On 
						</td>
						<td>
							<input type="radio" id="offPref" class="pref" name="pref" value="off">&nbsp; Off 
						</td>-->
						<td style="width:70px"> 
							<div id="switchButton" class="make-switch switch-small" data-on="success" data-off="danger">
								<input type="checkbox">
							</div>
						</td>
						<td></td>
					</tr>
					<tr id="setMobileNumber" hidden>
						<td>SMS Notification will be sent to:</td>
						<td style="width:70px">
							<input type="image" src="img/singaporeFlag.png" style="height:20px; width:20px"/>
							<input type="text" name="countryCode" value="+65" style="width:30px" disabled/>
						</td>
						<td>
						<form>
							<input type="text" id="mobileNumber" class="input-medium bfh-phone" data-format="dddddddd" placeholder="e.g. 81256296" rel="tooltip" data-placement="right" title="Enter Singapore No." />
						</form>
						</td>
					</tr>
				</tbody>
			</table>
			<button id="submitFormBtn" class="btn btn-primary" data-loading-text="Saving..." style="margin-bottom: 20px;">Save</button>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			userPreferencesLoad = function() {
				var mobileNo = '<s:property value="mobileNumber"/>';
				$('#switchButton').bootstrapSwitch('setState', false);
				if (mobileNo !== null && mobileNo !== "") {
					$('#setMobileNumber').show();
					$('#switchButton').bootstrapSwitch('setState', true);
					$('#mobileNumber').val(mobileNo).change();
				}

				//For hiding and showing table row
				$('#switchButton').on('switch-change', function(e, data) {
					if (data.value === true) {
						$('#setMobileNumber').show();
					} else {
						$('#setMobileNumber').hide();
					}
				});

				//Submit changes to backend
				$('#submitFormBtn').click(function(e) {
					if (uatMode) recordHumanInteraction(e);
					$(this).button('loading');
					var mobNo = "";
					if ($('#setMobileNumber').is(":visible")) {
						mobNo = $('#mobileNumber').val();
						if (mobNo === "" || mobNo.length < 8 || (mobNo.substring(0,1) !== "8" && mobNo.substring(0,1) !== "9")) {
							showNotification("ERROR", "Mobile Number is invalid!");
							$("#submitFormBtn").button('reset');
							return false;
						}
					}
					var mobJson = {mobileNumber: mobNo};
					$.ajax({
						type: 'POST',
						async: false,
						url: 'updateUserPreferences',
						data: {jsonData: JSON.stringify(mobJson)}	
					}).done(function(response) {
						$("#submitFormBtn").button('reset');
						if (response.success) {
							if (response.message.split("No changes").length > 1) {
								showNotification("WARNING", response.message);
							} else {
								showNotification("SUCCESS", response.message);
							}
						} else {
							showNotification("ERROR", response.message);
						}
					}).fail(function(response) {
						$("#submitFormBtn").button('reset');
						showNotification("WARNING", "Oops. Something went wrong. Please try again!");
					});
					return false;
				});

				//Tooltip
				$('body').on('mouseenter', '[rel=tooltip]', function() {
					$(this).tooltip('show');
				});
				$('body').on('mouseleave', '[rel=tooltip]', function() {
					$(this).tooltip('hide');
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
			
			addLoadEvent(userPreferencesLoad);
		</script>
    </body>
</html>
