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
        <title>IS480 Scheduling System | Manage Notifications </title>

        <%@include file="footer.jsp"%>
		<link type="text/css" rel="stylsheet" hred="css/bootstrap.css">

		
		
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <div class="container">
            <h3>Manage Notifications</h3>

            <!-- Kick unauthorized user -->
            <%	if (!activeRole.equals(Role.ADMINISTRATOR) && !activeRole.equals(Role.COURSE_COORDINATOR)) {
					request.setAttribute("error", "Oops. You are not authorized to access this page!");
					RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
					rd.forward(request, response);
				}
            %>

			<s:iterator value="data"> 
				
				<table class="table table-hover zebra-striped">
				
				
				<% String eStatus = (String)request.getAttribute("emailStatus"); %> 
				<tr><td>
				
				<div id="emailStatusBlock">
					<form>
					<i class="icon-white icon-envelope"></i>Email Notifications:
					<% if(eStatus.equalsIgnoreCase("On")){ %>
					<input type="radio" id="onPrefa" class="pref" name="pref" value="On" onchange="onOption(onPrefa)" checked>&nbsp; On 
					<input type="radio" id="offPrefb" class="pref" name="pref" value="Off" onchange="onOption(offPrefb)">&nbsp; Off
					<% }else{%>
					<input type="radio" id="onPref1" class="pref" name="pref" value="On" onchange="onOption(onPref1)">&nbsp; On 
					<input type="radio" id="offPref2" class="pref" name="pref" value="Off" onchange="onOption(offPref2)" checked>&nbsp; Off
					<% } %>
					</form>
				</div>

				<div id="emailFrequencyBlock">
					Email reminders sent for (days):
					<div class="input-append">
						
						<input id="emailFrequency" style="width: 18px;height: 20px" type="text" value="<s:property value="emailFrequency"/>" disabled />
						<div class="btn-group">
							<button class="btn" type="button" onclick="upOne(emailFrequency);" >&#9650;</button>
							<button class="btn" type="button" onclick="downOne(emailFrequency);" >&#9660;</button>
						</div>
					</div>
				</div>
				</td></tr>
						
				<tr>
				<td>
				
				
				<% String sStatus = (String)request.getAttribute("smsStatus"); %> 
				
				<div id="smsStatusBlock">
					<form>
				    <i class="icon-white icon-comment"></i>SMS Notifications:
					<% if(sStatus.equalsIgnoreCase("On")){ %>
					<input type="radio" id="onPrefc" class="pref" name="pref" value="On" onchange="onOption2(onPrefc)" checked>&nbsp; On 
					<input type="radio" id="offPrefd" class="pref" name="pref" value="Off" onchange="onOption2(offPrefd)">&nbsp; Off
					<% }else{%>
					<input type="radio" id="onPref3" class="pref" name="pref" value="On" onchange="onOption2(onPref3)">&nbsp; On 
					<input type="radio" id="offPref4" class="pref" name="pref" value="Off" onchange="onOption2(offPref4)" checked>&nbsp; Off
					<% } %>
					</form>
				</div>

					<div id="smsFrequencyBlock">
					SMS sent (days before):
					<div class="input-append">	
						<input id="smsFrequency" style="width: 18px;height: 20px" type="text" value="<s:property value="smsFrequency"/>" disabled />
						<div class="btn-group">
							<button class="btn" type="button" onclick="upOne(smsFrequency);" >&#9650;</button>
							<button class="btn" type="button" onclick="downOne(smsFrequency);" >&#9660;</button>
						</div>
					</div>
				
					</div>
						
				</td></tr>
						</table>

			</s:iterator>
				
				<button type="button" id="saveButton" class="btn btn-primary" style="width:80px; height:30px;" id="save" onclick="edited();">
					<strong>Save</strong>
				</button>
			

			<script type="js/bootstrap.js"></script>

			<script type="text/javascript">
					
					$(document).ready(function(){
						//if it is off don't show
						var values = $('input:checked').map(function() {
							return this.value;
						}).get();
						
						var splitArray = new Array();
						splitArray = values.slice(',');
						
						//alert(splitArray[0] + splitArray[1]);
						
						var ons = "On";
						//this is for email
						if(splitArray[0] !== ons){
							
							onOption("offPrefb");
							onOption("offPref2");
							
						}
						
						if(splitArray[1] !== ons){
							
							onOption2("offPrefd");
							onOption2("offPref4");
							
						}


					});
					
					
					function upOne(id) {
						id.value++;
					}

					function downOne(id) {
						if (parseInt(id.value, 0) !== 0) {
							id.value--;
						}
					}
					
					function onOption(id){
						
						var status = id.value;
						
						var ons = "On";
						if(status !== ons){
							//if it is on show the  frequency
							jQuery(emailFrequencyBlock).hide();
						}else{
							jQuery(emailFrequencyBlock).show();
							
						}
						
					}
					
					function onOption2(id){
						
						var status = id.value;
						
						var ons = "On";
						if(status !== ons){
							//if it is on show the  frequency
							jQuery(smsFrequencyBlock).hide();
						}else{
							jQuery(smsFrequencyBlock).show();
						}
						
					}
					
					function edited(){
						var values = $('input:checked').map(function() {
							return this.value;
						}).get();
						
						var splitArray = new Array();
						splitArray = values.slice(',');
						
						var ons = "On";
						
						var toSend = "email,";
						//this is for email
						if(splitArray[0] === ons){
							
							//if on get the frequency
							var eFrq = document.getElementById("emailFrequency").value;
							toSend += "On," + eFrq + ",";
							
						}else{
							
							//if off, then set frequency to nothing
							toSend += "Off," + "0,";
							
						}
						
						toSend += "sms,"
						
						//this is for sms
						if(splitArray[1] === ons){
							
							//if on get the frequency
							var eFrq = document.getElementById("smsFrequency").value;
							toSend += "On," + eFrq;
							
						}else{
							
							//if off, then set frequency to nothing
							toSend += "Off," + "0";
							
						}
						
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
							   timedRefresh(2000);
						   } else {
							   var eid = btoa(response.message);
							   window.location = "error.jsp?eid=" + eid;
						   }
						}).fail(function(response) {
						   //$("#saveButton").button('reset');
						   //$("#addRowBtn").button('reset');
						   showNotification("WARNING", "Oops.. something went wrong");
						});

					}
					
					//Notification-------------
					function showNotification(action, notificationMessage) {
						var opts = {
							title: "Note",
							text: notificationMessage,
							type: "action",
							icon: false,
							sticker: false,
							mouse_reset: false,
							animation: "fade",
							animate_speed: "medium",
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
