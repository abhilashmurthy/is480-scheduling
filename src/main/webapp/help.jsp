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
			<h3>Help Section</h3>
			<br>
			<div class="well well-large">
				<p>
				Hi <% out.print(user.getFullName()); %>, <br/><br/>
				Having trouble with the system?
				Please <a href="mailto:xuling.dai.2010@sis.smu.edu.sg?cc=is480.scheduling@gmail.com&Subject=Problem%20-%20IS480%20Scheduling%20System">click here</a> to send us an email.
				<i>(Do attach screenshots where appropriate)</i>
				<br/><br/>
				We will contact you as soon as possible!<br/><br/>
				Regards, <br/>
				IS480 Scheduling Team
				</p>
			</div>
			<div class="well well-large">
				If the above link does not work, please email your issue to <a>dai.xuling.2010@sis.smu.edu.sg</a>
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

