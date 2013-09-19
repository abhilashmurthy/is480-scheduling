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
		<link rel="stylesheet" href="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.min.css">
		<link href="css/bootstrap-switch.css" rel="stylesheet">
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
						<td style="width:250px">Subscribe to SMS Notification</td>
						<td style="width:50px"> 
							<input type="radio" id="onPref" class="pref" name="pref" value="on">&nbsp; On 
						</td>
						<td>
							<input type="radio" id="offPref" class="pref" name="pref" value="off">&nbsp; Off 
						</td>
						
					</tr>
					<tr id="setMobileNumber">
						<td>SMS Notification will be sent to:</td>
						<td style="width:70px"> 
							<!--<a >-->
							<input type="image" src="img/singaporeFlag.png" style="height:20px; width:20px" rel="tooltip" data-placement="bottom" title="Singapore">
							<!--</a>-->
							<input type="text" name="countryCode" value="+65" style="width:30px" disabled/>
						</td>
						<td>
							<form>
								<input type="text" id="mobileNumber" class="input-medium bfh-phone" data-format="dddddddd" value="">
							</form>
						</td>
					</tr>
				</tbody>
			</table>
			<button id="submitFormBtn" class="btn btn-primary" data-loading-text="Saving..." style="margin-bottom: 20px;">Save</button>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript" src="js/plugins/bootstrap-switch.js"></script>
		<script type="text/javascript" src="js/plugins/bootstrap-switch.min.js"></script>
		<script src="http://cdnjs.cloudflare.com/ajax/libs/bootstrap-switch/1.7/bootstrap-switch.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				var mobileNo = '<s:property value="mobileNumber"/>';
				if (mobileNo === null || mobileNo === "") {
					$('#setMobileNumber').hide();
					$("#offPref" ).prop("checked", true);
					$("#offPref").attr('checked', 'checked');
				} else {
					$('#setMobileNumber').show();
					$("#mobileNumber").attr("value", mobileNo);
					$("#onPref" ).prop("checked", true);
					$("#offPref" ).prop("checked", false);
				}
			});
			
			//For hiding and showing table row
			$("input:radio").change(function () {
				if ($(this).val() === 'on') {
					$('#setMobileNumber').show();
				} else {
					$('#setMobileNumber').hide();
				}
			});
			
			//Submit changes to backend
			$('#submitFormBtn').click(function() {
				$(this).button('loading');
				var mobNo = "";
				//Checking if the element is visible on page currently or not
				if($('#mobileNumber').is(':visible')) {
					mobNo = $('#mobileNumber').val();
					//Checking whether mobile number is incorrect or not
					if (mobNo === "" || mobNo.length < 8) {
						showNotification("ERROR", "Mobile Number is invalid!");
						$("#submitFormBtn").button('reset');
						return false;
					}
					//Checking whether the mobile number starts with 8 or 9
					if (mobNo.substring(0,1) !== "8" && mobNo.substring(0,1) !== "9") {
						showNotification("ERROR", "Mobile Number is invalid!");
						$("#submitFormBtn").button('reset');
						return false;
					}
				}
				var mobJson = {};
				mobJson['mobileNumber'] = mobNo;
				
				$.ajax({
					type: 'POST',
					async: false,
					url: 'updateUserPreferences',
					data: {jsonData: JSON.stringify(mobJson)}	
				}).done(function(response) {
					$("#submitFormBtn").button('reset');
					console.log(response);
					if (response.success) {
						showNotification("SUCCESS", response.message);
					} else {
						showNotification("ERROR", response.message);
					}
				}).fail(function(response) {
					$("#submitFormBtn").button('reset');
					console.log(response);
					showNotification("WARNING", "Oops. Something went wrong. Please try again!");
				});
			});

			//Tooltip
			$(document).on('mouseenter','[rel=tooltip]', function(){
				$(this).tooltip('show');
			});

			$(document).on('mouseleave','[rel=tooltip]', function(){
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
					animate_speed: "slow",
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
		</script>
    </body>
</html>
