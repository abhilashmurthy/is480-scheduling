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
        <title>IS480 Scheduling System | CSV Upload </title>
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
			<h3>CSV Upload</h3>
			<br/>
			<form id="uploadForm" enctype="multipart/form-data" action="uploadFileToBackend" method="POST">
			<%--<s:form action="uploadFileToBackend" method="post" enctype="multipart/form-data">--%>
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
					<tr><th>Upload File (.csv)</th></tr>
				</thead>
				<tbody>
					<tr>
						<td>
							<input type="file" id="fileUploaded" name="file" />
							<%--<s:file name="file" onchange="checkFile(this);"></s:file>--%>
						</td>
					</tr>
				</tbody>
				</table>
			</div>
			
			<div style="float: left;">
				<br/><br/>
				<table class>
					<tbody>
					<td>
						<button id="submitFormBtn" class="btn btn-primary" data-loading-text="Uploading..." 
							style="margin-bottom: 20px;">Upload</button>
							<!--<input type="submit" id="submitFormBtn" value="Save"/>-->
							<%--<s:submit value="Upload"></s:submit>--%>
					</td>
					</tbody>
				</table>
			</div>
			</form>
					
			<div class="well well-small" style="float:right">
				<h5>Note</h5>
				<ul style='font-size: 13px'>
					<li>Download Sample CSV File:
						<a href="SampleCSVFile/is480SampleFileUpload.csv" target="_blank">
						Sample File</a>
					</li>
					<li>Please ensure that you have created a new schedule before uploading </li>
					<li>Please upload a single-tabbed CSV file only</li>
					<li>Please do not use this feature to overwrite existing data</li>
					<li>Please ensure the file contains data for 1 semester only </li>
					<li>Please do not include Administrator and Course Coordinator in the file</li>
					<li>Please ensure that for every team - Students are listed first followed <br/>by Supervisor and Reviewers</li>
					<li>Please ensure every Team has a name</li>
					<li>Please ensure every Student has a username</li>
					<li>Please put "-" where Supervisor/Reviewer 1/Reviewer 2 have not been <br/>assigned to a team</li>
				</ul>
			</div>
			<%--</s:form>--%>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
//			$(document).ready(function(){
		uploadFileLoad = function () {
			//Reset upload button status
			$("#submitFormBtn").button('reset');
			
			//Disable Pines Notify Settings
			$.pnotify.defaults.history = false;
			$.pnotify.defaults.delay = 10000;

//			Getting value from upload file action class and displaying success/error message accordingly
			var result = '<%= session.getAttribute("csvMsg") %>';
			if (result !== null && result !== "") {
				if (result.substring(0,5) === ("Wrong") || result.substring(0,9) === ("Incorrect")) {
					showNotification("ERROR", result);
				} else if (result.substring(0,7) === ('Success')) {
					showNotification("SUCCESS", result);
				}
			}
			//Resetting the session object
			'<% session.removeAttribute("csvMsg"); %>';
			
			
			//To check whether the file selected has correct extension or not
//			function checkFile(sender) {
////				var validExts = new Array(".xlsx", ".xls", ".csv");
//				var validExts = new Array(".csv");
//				var fileExt = sender.value;
//				fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
//				if (validExts.indexOf(fileExt) < 0) {
//					showNotification("ERROR", "Invalid file selected! File should be " +
//						   validExts.toString() + " type!");
//					return false;
//				} else {
//					return true;
//				}
//			}
			
			//Submit changes to backend
			$('#submitFormBtn').click(function(e) {
//			function validate(saveButton) {
//				e.stopPropagation();
//				e.preventDefault();
				$(this).button('loading');
//				saveButton.button('loading');
				
				var termSelected = $('#termChosen').val();
				//Checking whether user has selected term or not
				if (termSelected === "" || termSelected === null) {
					showNotification("ERROR", "Please select a term!");
					$("#submitFormBtn").button('reset');
					return false;
				}
				
				var file = $('#fileUploaded').val();
				//Checking whether user has selected file or not
				if (file === "" || file === null) {
					showNotification("ERROR", "Please select a file!");
					$("#submitFormBtn").button('reset');
					return false;
				}

				//Checking the extension of the file
//				var validExts = new Array(".xlsx", ".xls", ".csv");
				var validExts = new Array(".csv");
				file = file.substring(file.lastIndexOf('.'));
				if (validExts.indexOf(file) < 0) {
					showNotification("ERROR", "Invalid file selected! File should be " +
						   validExts.toString() + " type!");
				    $("#submitFormBtn").button('reset');
					return false;
				} 
				
				var fileSize = $("#fileUploaded")[0].files[0].size;
				//If file size is greater than 2 mb then display error
				if (fileSize > 5242880) {
					showNotification("ERROR", "File size too large! It should be less than 5 mb!");
					$("#submitFormBtn").button('reset');
					return false;
				}
				
				//If file size is 0 then display error
				if (fileSize === 0) {
					showNotification("ERROR", "The file is empty! Please upload correct file!");
					$("#submitFormBtn").button('reset');
					return false;
				}
				
				var termText = $("#termChosen option:selected").html();
				var a = bootbox.confirm({
					message: "Are you sure you want to upload <b>" + $('#fileUploaded').val() + "</b> for <b>" + termText + "</b>?",
					callback: function(result) {
						if (!result) {
							$("#submitFormBtn").button('reset');
						} else {
							$("#uploadForm").trigger('submit');
						}
					}
				});
				
				return false;
//				var fileJson = {};
//				fileJson['termId'] = termSelected;
//				fileJson['fileUploaded'] = file;
				
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
//				 
//				});
//				alert(JSON.stringify(fileJson));
			});
			
			$('#termChosen').on('change', function() {
				var termId = $("#termChosen").val();
				var termData = {};
				termData['termId'] = termId;
				$.ajax({
					type: 'POST',
					async: false,
					url: 'updateActiveTermCSV',
					data: {jsonData: JSON.stringify(termData)}	
				}).done(function(response) {
//					$("#submitFormBtn").button('reset');
					console.log(response);
//					if (response.success) {
//						showNotification("SUCCESS", response.message);
//					} else {
//						showNotification("ERROR", response.message);
//					}
				}).fail(function(response) {
//					$("#submitFormBtn").button('reset');
					console.log(response);
					showNotification("WARNING", "Oops. Something went wrong. Please select the term again!");
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
			};
			
			addLoadEvent(uploadFileLoad);
		</script>
    </body>
</html>
