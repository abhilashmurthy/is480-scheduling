<%-- 
    Document   : help
    Created on : Sep 16, 2013, 3:23:00 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@page import="model.User"%>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Help </title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <div class="container">
			<h3>Help</h3>
			<% if (user.getRole() != Role.GUEST) { %>
			<!-- USER GUIDE -->
			<div>
				User Guide: &nbsp; <a class="btn btn-inverse" href="user-guide/
					<% if (user.getRole() == Role.ADMINISTRATOR || user.getRole() == Role.COURSE_COORDINATOR) { %>
					IS480 Scheduling System User Guide for Admin & Course Coordinator.pdf
					<% } else if (user.getRole() == Role.STUDENT) { %>
					IS480 Scheduling System User Guide for Student.pdf
					<% } else if (user.getRole() == Role.FACULTY) { %>
					IS480 Scheduling System User Guide for Faculty.pdf
					<% } else if (user.getRole() == Role.TA) { %>
					IS480 Scheduling System User Guide for TA.pdf
					<% } %>">Download</a>
			</div>
			<br>
			<% } %>
			<!-- USER FEEDBACK -->
			<div class="well well-large">
				<p>
				Hi <% out.print(user.getFullName()); %>, <br/><br/>
				Having trouble with the system?
				Please drop any one of us an email: <i>(Do attach screenshots where appropriate)</i> <br />
				<ul>
				<s:iterator value="adminEmails">
					<li><a href="mailto:<s:property />"><s:property /></a></li>
				</s:iterator>
				</ul>
				We will contact you as soon as possible!<br/><br/>
				Regards, <br/>
				IS480 Scheduling Team
				</p>
			</div>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			$(document).ready(function(){
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

