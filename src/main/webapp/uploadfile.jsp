<%-- 
    Document   : uploadfile
    Created on : Sep 15, 2013, 3:35:17 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Upload File </title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
		<!-- Kick unauthorized user -->
        <%
            if (activeRole != Role.ADMINISTRATOR && activeRole != Role.COURSE_COORDINATOR) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
		 
        <div class="container">
			<h3>Upload File</h3>
			<form enctype="multipart/form-data" action="UploadFileAction">
			<div style="float: left; margin-right: 50px;">
			<table class="table" style="width:auto">
				<thead>
					<tr><th>Select Term</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>
						<select id="termChosen">
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
			<div style="float:left;">
				<table id="smsTable" class="table">
				<thead>
					<tr><th>Upload File (.csv)</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>
							<input type="file" id="fileUploaded" name="fileUploaded" accept="text/comma-separated-values, text/csv, 
								application/csv, application/excel, application/vnd.ms-excel, application/vnd.msexcel" 
								onchange="checkFile(this);" />
						</td>
					</tr>
				</tbody>
				</table>
			</div>
			
			<div style="clear: both;">
				<br/>
				<button id="submitFormBtn" class="btn btn-primary" data-loading-text="Saving..." style="margin-bottom: 20px;">Save</button>
			</div>
			</form>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript" src="js/plugins/jquery.ajaxfileupload.js"></script>
		<script type="text/javascript">
			$(document).ready(function(){
			});
			
			//To check whether the file selected has correct extension or not
			function checkFile(sender) {
				var validExts = new Array(".xlsx", ".xls", ".csv");
				var fileExt = sender.value;
				fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
				if (validExts.indexOf(fileExt) < 0) {
					showNotification("ERROR", "Invalid file selected! Valid files are of " +
						   validExts.toString() + " types.");
					return false;
				} else {
					return true;
				}
			}
			
			//Submit changes to backend
			$('#submitFormBtn').click(function() {
				$(this).button('loading');
				var file = $('#fileUploaded').val();
				alert(file.size);
				var termSelected = $('#termChosen').val();
				
				//Checking whether user has selected file or not
				if (file === "" || file === null) {
					showNotification("ERROR", "Please select a file!");
					$("#submitFormBtn").button('reset');
					return false;
				}
				
				//Checking whether user has selected term or not
				if (termSelected === "" || termSelected === null) {
					showNotification("ERROR", "Please select a term!");
					$("#submitFormBtn").button('reset');
					return false;
				}
				
				//Checking the extension of the file
				var validExts = new Array(".xlsx", ".xls", ".csv");
				file = file.substring(file.lastIndexOf('.'));
				if (validExts.indexOf(file) < 0) {
					showNotification("ERROR", "Invalid file selected! Valid files are of " +
						   validExts.toString() + " types.");
				    $("#submitFormBtn").button('reset');
					return false;
				} 
				
				var fileJson = {};
				fileJson['termId'] = termSelected;
				fileJson['fileUploaded'] = file;
				
//				$('input[type="file"]').ajaxfileupload({
//				   'action': 'uploadFileToBackend',
//				   'params': {
//					 'extra': 'info'
//				   },
//				   'onComplete': function(response) {
//					 console.log('custom handler for file:');
//					 alert(JSON.stringify(response));
//				   },
//				   'onStart': function() {
//					 if(weWantedTo) return false; // cancels upload
//				   },
//				   'onCancel': function() {
//					 console.log('no file selected');
//				   }
//				});
				$.ajax({
					type: 'POST',
					async: false,
					url: 'updateUserPreferences',
					data: {jsonData: JSON.stringify(fileJson)}	
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
