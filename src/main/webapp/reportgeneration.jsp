<%-- 
    Document   : reportgeneration
    Created on : Oct 28, 2013, 2:37:16 PM
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
        <title>IS480 Scheduling System | Report</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
		 <!-- Kick unauthorized user -->
        <%
            if (!activeRole.equals(Role.ADMINISTRATOR) && !activeRole.equals(Role.COURSE_COORDINATOR)) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
		 
		 <div class="container">
			<h3>Generate Report</h3>
			<br/>
			<div style="float: left; margin-right: 50px;">
				<table class="table table-hover" style="width:auto">
					<thead>
						<tr><th>Select Term</th></tr>
					</thead>
					<tbody>
						<tr>
							<td>
							<select id="termChosen" name="termChosen" style="width:200px">
								<option value=""></option>
								<s:iterator value="dataList">
									<option id="<s:property value="termId"/>">
										<s:property value="termName"/>
									</option>
								</s:iterator>
							</select>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
				
			<div style="float:left; margin-right: 40px;">
				<table class="table table-hover" style="width:auto">
					<thead>
						<tr><th>Select Milestone</th></tr>
					</thead>
					<tbody>
						<tr>
							<td>
							<select id="milestoneChosen" name="milestoneChosen" style="width:200px">
								<option value=""></option>
								<s:iterator value="dataList">
									<option value="<s:property value="milestone"/>">
										<s:property value="milestone"/>
									</option>
								</s:iterator>
							</select>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			
			<div style="float: left;">
				<br/><br/>
				<table>
					<tbody>
						<tr>
							<td>
								<button id="submitBtn" class="btn btn-primary" data-loading-text="Generating..." 
									style="margin-bottom: 20px;">Generate Report</button>
									<!--<input type="submit" id="submitFormBtn" value="Save"/>-->
									<%--<s:submit value="Upload"></s:submit>--%>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<br/><br/>				
			<div style="float: left">
				<table>
				<br/><br/>
				<a id="downloadFile" href="ReportCSV/ScheduleReport.csv" target="_blank" style="display:none">Download Report</a>
				</table>
			</div>
						
		 </div>
					
		<%@include file="footer.jsp"%>
		<script type="text/javascript" src="js/plugins/jquery.ajaxfileupload.js"></script>
		<script type="text/javascript">
//			$(document).ready(function(){
		window.onload = function  () {
			
		   //removes duplicate	
		   var seen = {};
			$('option').each(function() {
				var txt = $(this).text();
				if (seen[txt])
					$(this).remove();
				else
					seen[txt] = true;
			});
			
			//var milestones = document.getElementById("milestoneChosen").value;
			var mArr = new Array();
			//mArr = milestones.split(",");
			
			var ddlArray= new Array();
			var ddl = document.getElementById('milestoneChosen');
			
			var counter = 0;
			
			for (var i = ddl.options.length-1; i >= 0 ; i--) {
				
			   ddlArray[i] = ddl.options[i].value;
			   var inArr = new Array();
			   inArr = ddlArray[i].split(",");
			   
			   for(var j=0;j<inArr.length;j++){
				   
				   var isTrue = false;
				   
				   if(mArr.length===0){
					   mArr[counter] = inArr[j];
				   }else{
						for(var x=0;x<mArr.length;x++){		
							if(mArr[x] === inArr[j]){
								isTrue = true;
							}
						}

						if(isTrue===false){
							 mArr[counter] = inArr[j];
						}
						
						 isTrue===false;
				   }

				   counter++;
			   }
			   
			}

			document.getElementById('milestoneChosen').options.length = 0;
			var opt = document.createElement("option");
		
			for(var i=mArr.length-1;i>=0;i--){

				var select = document.getElementById("milestoneChosen");
				var	opt = document.createElement("option");
				opt.textContent = mArr[i];
				opt.value = mArr[i];
				select.appendChild(opt);
				
			}

			//Reset upload button status
			$("#submitBtn").button('reset');
			
			//Disable Pines Notify Settings
			$.pnotify.defaults.history = false;
			$.pnotify.defaults.delay = 10000;

			//Submit changes to backend
			$('#submitBtn').click(function(e) {
				$(this).button('loading');
				
				//Check whether term has been selected
				//var termSelected = $('#termChosen').val();
				//Checking whether user has selected term or not
				/*if (termSelected === "" || termSelected === null) {
					showNotification("ERROR", "Please select a term!");
					$("#submitBtn").button('reset');
					return false;
				}*/
		
				var e = document.getElementById("termChosen");
				var selectedTerm = e.options[e.selectedIndex].text;
				var e = document.getElementById("milestoneChosen");
				var selectedMilestone = e.options[e.selectedIndex].text;
				 var optionID = $('option:selected').attr('id');
				
				var settings = "";
				
				if((selectedTerm ==="" || selectedTerm === null) || (selectedMilestone ==="" || selectedMilestone === null)){
					showNotification("ERROR", "Please select term and milestone.");
					$("#submitBtn").button('reset');
					return false;	
				}else{
					//alert(selectedTerm);
					//alert(selectedMilestone);
					settings+= selectedMilestone+",";
					settings+= optionID;
				}
				
				console.log("tosend: " + settings);
				//var val = document.getElementById("milestoneChosen").value;
				//console.log(val);
				$.ajax({
					type: 'POST',
					async: false,
					url: 'viewScheduleReport',
					data: {settingDetails: settings}
				}).done(function(response) {
					$("#submitBtn").button('reset');
					console.log(response);
					if (response.success) {
						showNotification("SUCCESS", response.message);
						//Create the download link
						
						$("#downloadFile").show();
					} else {
						showNotification("ERROR", response.message);
					}
				}).fail(function(response) {
					$("#submitBtn").button('reset');
					console.log(response);
					showNotification("WARNING", "Oops. Something went wrong. Please select the term again!");
				});
			});
			
			$('#downloadFile').attr("title", "Click to download");
			$('#downloadFile').on('mouseenter', function(){
				$(this).tooltip('show');
			});
			
			console.log("HERE");

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
						opts.title = "Success";
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
			
			//addLoadEvent(uploadFileLoad());
		</script>
	</body>
</html>
