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
			<table class="table table-hover" style="width:auto">
				<thead>
					<tr><th>Select Report</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>
						<select id="reportChosen" name="termChosen" style="width:220px">
							<option value="0"></option>
							<option value="1">Schedule Report</option>
							<option value="2">Schedule for Wiki Page</option>
							<option value="3">Log Activity Report</option>
						</select>
						</td>
					</tr>
				</tbody>
			</table>
				
			<div id="scheduleWikiReport" style="display: none">
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
										<option value="<s:property value="termId"/>">
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
								<%--	<s:iterator value="dataListMilestones">
										<option value="<s:property value="milestone"/>">
											<s:property value="milestone"/>
										</option>
									</s:iterator> --%>
								</select>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<div id="logActivityReport" style="display: none">
				<div style="float: left; margin-right: 50px;">
				<table class="table table-hover" style="width:auto">
					<thead>
						<tr>
							<th>Start Date</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>
								<input type="text" id="startDatePicker" style="width:150px;" />
							</td>
						</tr>
					</tbody>
				</table>
				</div>
				
				<div style="float:left; margin-right: 40px;">
				<table class="table table-hover" style="width:auto">
					<thead>
						<tr>
							<th>End Date</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>
								<input type="text" id="endDatePicker" style="width:150px;"/>
							</td>
						</tr>
					</tbody>
				</table>
				</div>
			</div>
			
			<div style="float:left; margin-right: 40px;">
				<br/><br/>
				<table>
					<tbody>
						<tr>
							<td>
								<button id="submitBtn" class="btn btn-primary" data-loading-text="Generating..." 
									style="margin-bottom: 20px;" disabled><i class='fa fa-download'></i>&nbsp;
									Generate Report</button>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<br/><br/>				
<!--			<div style="float: left">
				<table>
					<a id="downloadFile" class="btn" target="_blank" style="display: none">
						<i class='fa fa-download'></i>&nbsp;Download Report
					</a>
				</table>
			</div>-->
						
		 </div>
					
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
		reportGenerationLoad = function() {
//			window.onload = function() {
			$(function() {
				$("#startDatePicker").datepicker({
					dateFormat: 'dd-mm-yy'
				});
				$("#endDatePicker").datepicker({
					dateFormat: 'dd-mm-yy'
				});
			});

			//Getting milestones list from backend
			var milestonesJson = JSON.parse('<s:property escape= "false" value= "milestonesList"/>');

			$('#termChosen').on('change', function() {
				$("#milestoneChosen").empty(); 
				var dropdownSelect = null;
				var termId = $("#termChosen").val();
				for (var i = 0; i < milestonesJson.length; i++) {
					if (milestonesJson[i].termId === termId) {
						//Split the milestone string
						var milestonesList = milestonesJson[i].milestone.substring(0, milestonesJson[i].milestone.length - 1);
						var mList = milestonesList.split(",");
						dropdownSelect = $('#milestoneChosen');
						for (var j = 0; j < mList.length; j++) {
							dropdownSelect.append(
								$('<option></option>').val(mList[j]).html(mList[j])
							);
						}
					}
				}
			});
				
			//On dropdown change
			$('#reportChosen').change(function() { 
				var sel = $(this).val();
				if (sel === "0") {
					$("#logActivityReport").hide();
					$("#scheduleWikiReport").hide();
//					$("#downloadFile").hide();
					$('#submitBtn').prop('disabled', true);
					showNotification("ERROR", "Please select a report!");
				} else if (sel === "1" || sel === "2") {
					$("#logActivityReport").hide();
//					$("#downloadFile").hide();
					$('#submitBtn').prop('disabled', false);
					$("#scheduleWikiReport").show();
				} else if (sel === "3") {
					$("#scheduleWikiReport").hide();
//					$("#downloadFile").hide();
					$('#submitBtn').prop('disabled', false);
					$("#logActivityReport").show();
				}
				return false;
			});
			
			//Preventing user from entering/deleting anything in date fields
			$("#startDatePicker").keypress(function (e) {
				e.preventDefault();
			});
			$("#startDatePicker").keydown(function (e) {
				e.preventDefault();
			});
			$("#endDatePicker").keypress(function (e) {
				e.preventDefault();
			});
			$("#endDatePicker").keydown(function (e) {
				e.preventDefault();
			});
		
		
			//Submit changes to backend
			$('#submitBtn').click(function(e) {
				if (uatMode) recordHumanInteraction(e);
				$(this).button('loading');
				var reportSel = $('#reportChosen').val();
				var downloadLink = "";
				//For the data to send to backend
				var reportData = {};
				//For the url parameter
				var urlParam = "";
				//Validation checks
				if (reportSel === "0") {
					showNotification("ERROR", "Please select a report!");
					return false;
				} else if (reportSel === "1") {
//					$("#downloadFile").attr("href", "ReportCSV/ScheduleReport.csv");
					downloadLink = "ReportCSV/ScheduleReport.csv";
					urlParam = 'generateScheduleReport';
					//Check whether term has been selected
					var termSelected = $('#termChosen').val();
					if (termSelected === "" || termSelected === null) {
						showNotification("ERROR", "Please select a term!");
						$("#submitBtn").button('reset');
						return false;
					}
					//Check whether milestone has been selected
					var milestoneSelected = $('#milestoneChosen').val();
					if (milestoneSelected === "" || milestoneSelected === null) {
						showNotification("ERROR", "Please select a milestone!");
						$("#submitBtn").button('reset');
						return false;
					}
					//Prepare data
					reportData["reportNumber"] = reportSel;
					reportData["termId"] = termSelected;
					reportData["milestoneName"] = milestoneSelected;
					
				} else if (reportSel === "2") {
//					$("#downloadFile").attr("href", "ReportCSV/WikiReport.txt");
					downloadLink = "ReportCSV/WikiReport.txt";
					urlParam = 'generateWikiReport';
					//Check whether term has been selected
					var termSelected = $('#termChosen').val();
					if (termSelected === "" || termSelected === null) {
						showNotification("ERROR", "Please select a term!");
						$("#submitBtn").button('reset');
						return false;
					}
					//Check whether milestone has been selected
					var milestoneSelected = $('#milestoneChosen').val();
					if (milestoneSelected === "" || milestoneSelected === null) {
						showNotification("ERROR", "Please select a milestone!");
						$("#submitBtn").button('reset');
						return false;
					}
					//Prepare data
					reportData["reportNumber"] = reportSel;
					reportData["termId"] = termSelected;
					reportData["milestoneName"] = milestoneSelected;
					
				} else if (reportSel === "3") {
//					$("#downloadFile").attr("href", "ReportCSV/LoggingReport.csv");
					downloadLink = "ReportCSV/LoggingReport.csv";
					urlParam = 'generateLoggingReport';
					//Check whether start date has been selected
					var startDate = $('#startDatePicker').val();
//					console.log("Start Date:" + startDate);
					var endDate = $('#endDatePicker').val();
//					console.log("End Date:" + endDate);
					
					if ((!startDate.length > 0) && (endDate.length > 0)) {
						showNotification("ERROR", "Please select the Start Date!");
						$("#submitBtn").button('reset');
						return false;
					} 
					//Check whether end date has been selected
					if ((startDate.length > 0) && (!endDate.length > 0)) {
						showNotification("ERROR", "Please select the End Date!");
						$("#submitBtn").button('reset');
						return false;
					} 
					//Check that end date is after start date
					var sDate = new Date($('#startDate').val());
					var eDate = new Date($('#endDate').val());
					if (sDate > eDate) {
						showNotification("ERROR", "End Date should be after Start Date!");
						$("#submitBtn").button('reset');
						return false;
					}
					//Preparing data
					reportData["reportNumber"] = reportSel;
					reportData["startDate"] = startDate;
					reportData["endDate"] = endDate;
				}
				
				//Making ajax call and submitting data to backend
				$.ajax({
					type: 'POST',
					async: false,
					url: urlParam,
					data: {jsonData: JSON.stringify(reportData)}
				}).done(function(response) {
					$("#submitBtn").button('reset');
					if (response.success) {
						//Create the download link
//						$("#downloadFile").show();
						window.open(downloadLink);
						showNotification("SUCCESS", response.message);
					} else {
						showNotification("ERROR", response.message);
					}
				}).fail(function(response) {
					$("#submitBtn").button('reset');
					showNotification("WARNING", "Oops. Something went wrong. Please select the term again!");
				});
				return false;
			});

			$('#downloadFile').attr("title", "Click to download");
			$('#downloadFile').on('mouseenter', function(){
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
		
		addLoadEvent(reportGenerationLoad);
		</script>
	</body>
</html>
