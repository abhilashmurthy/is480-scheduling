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
				<table class="table table-hover">
				<thead>
					<tr><th>Select Report</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>
							<select id="reportId" name="reportId" style="width:400px">
								<option value=""></option>
								<option value="1">Teams which haven't signed up for a presentation slot</option>
								<option value="2"></option>
								<option value="3"></option>
								<option value="4"></option>
								<option value="5"></option>
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
									style="margin-bottom: 20px;">Generate</button>
									<!--<input type="submit" id="submitFormBtn" value="Save"/>-->
									<%--<s:submit value="Upload"></s:submit>--%>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			
			<div style="float:left; margin-left: 70px; margin-top:5px;">
				<br/><br/>
				<a id="downloadFile" href="SampleCSVFile/is480SampleFileUpload.csv" target="_blank" style="display:none">Sample File</a>
			</div>
						
		 </div>
					
		<%@include file="footer.jsp"%>
		<script type="text/javascript" src="js/plugins/jquery.ajaxfileupload.js"></script>
		<script type="text/javascript">
//			$(document).ready(function(){
		uploadFileLoad = function () {
			//Reset upload button status
			$("#submitBtn").button('reset');
			
			//Disable Pines Notify Settings
			$.pnotify.defaults.history = false;
			$.pnotify.defaults.delay = 10000;

			//Submit changes to backend
			$('#submitBtn').click(function(e) {
				$(this).button('loading');
				
				//Check whether term has been selected
				var termSelected = $('#termChosen').val();
				//Checking whether user has selected term or not
				if (termSelected === "" || termSelected === null) {
					showNotification("ERROR", "Please select a term!");
					$("#submitBtn").button('reset');
					return false;
				}
				
				//Check whether report has been selected
				var reportSelected = $('#reportId').val();
				//Checking whether user has selected term or not
				if (reportSelected === "" || reportSelected === null) {
					showNotification("ERROR", "Please select a report!");
					$("#submitBtn").button('reset');
					return false;
				}
				
				var reportData = {};
				reportData['termId'] = termSelected;
				reportData['reportId'] = reportSelected;
				$.ajax({
					type: 'POST',
					async: false,
					url: '',
					data: {jsonData: JSON.stringify(reportData)}	
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
			};
			
			addLoadEvent(uploadFileLoad());
		</script>
	</body>
</html>
