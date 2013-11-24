<%-- 
    Document   : restoreDB
    Created on : Nov 04, 2013, 4:13:17 AM
    Author     : Abhilash
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Restore Database </title>
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
			<h3>Database Restore</h3>
			<h5 style="color: green;">BACKUP_DIR in General.properties: <s:property value= "backupPath"/></h5>
			<table id='filesTable' class='usersTable table table-hover zebra-striped' style="margin-top: 20px">
				<thead>
					<tr><th></th><th>File Name</th><th>Last Modified</th><th>DML Restore</th><th>DDL Restore</th></tr>
				</thead>
				<tbody>
					<s:if test= "%{fileData != null && fileData.size() > 0}">
						<s:iterator var= "file" value= "fileData">
							<tr id='file_<s:property value= "datetime"/>'>
								<td><i class='fa fa-save fa-black'></i></td>
								<td class='fileName'><s:property value= "fileName"/></td>
								<td class='datetime'><s:property value= "datetime"/></td>
								<td>
									<button type='button' title= "Restore only data" class='restoreDMLFileBtn btn btn-primary btn-small'>
										<i class='fa fa-file-text fa-white'></i>
									</button>
								</td>
								<td>
									<button type='button' title= "Restore schema and data - BE CAREFUL!" class='restoreDDLFileBtn btn btn-danger btn-small'>
										<i class='fa fa-fast-backward fa-white'></i>
									</button>
								</td>
							</tr>
						</s:iterator>
					</s:if>
					<s:else>
						<tr><td colspan= "3"><h3 class='noFilesMsg'>No Backup Files found!</h3></td></tr>
					</s:else>
				</tbody>
			</table>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
		restoreDBLoad = function () {
			
			$('body').on('click', '.restoreDDLFileBtn', function(){
				var fileName = $(this).closest('tr').children('.fileName').text();
				$.ajax({
					type: 'POST',
					url: 'restoreSQLDump',
					data: {"jsonData": JSON.stringify({"fileName": fileName, "restoreType": "ddl"})},
					cache: false
				}).done(function(response){
					if (response.success) {
						bootbox.dialog({
							title: 'Restore Success!',
							message: 'Hurray! Please logout and <b>restart Apache Tomcat</b>',
							buttons: {
								Logout: {
									className: 'btn-primary',
									callback: function() {
										window.open('http://localhost/phpmyadmin/index.php?db=is480-scheduling', '_blank');
										setTimeout(function(){window.location = 'logout';}, 1000);
									}
								}
							}
						});
					} else {
						showNotification("ERROR", response.message);
					}
				}).fail(function(error){
					var eid = btoa("Something went wrong");
					window.location="error.jsp?eid=" + eid;
				});
				return false;
			});
			
			$('body').on('click', '.restoreDMLFileBtn', function(){
				var fileName = $(this).closest('tr').children('.fileName').text();
				$.ajax({
					type: 'POST',
					url: 'restoreSQLDump',
					data: {"jsonData": JSON.stringify({"fileName": fileName, "restoreType": "dml"})},
					cache: false
				}).done(function(response){
					if (response.success) {
						bootbox.dialog({
							title: 'Restore Success!',
							message: 'Hurray! Please logout and <b>restart Apache Tomcat</b>',
							buttons: {
								Logout: {
									className: 'btn-primary',
									callback: function() {
										window.open('http://localhost/phpmyadmin/index.php?db=is480-scheduling', '_blank');
										setTimeout(function(){window.location = 'logout';}, 1000);
									}
								}
							}
						});
					} else {
						showNotification("ERROR", response.message);
					}
				}).fail(function(error){
					var eid = btoa("Something went wrong");
					window.location="error.jsp?eid=" + eid;
				});
				return false;
			});
			
			$('#filesTable').dataTable({
				aaSorting: [],
				bPaginate: false,
				bJqueryUI: false,
				bLengthChange: true,
				bFilter: false,
				bSort: true,
				sDom: '<lfti>'
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
			
			addLoadEvent(restoreDBLoad);
		</script>
    </body>
</html>
