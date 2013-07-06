<%-- 
    Document   : Booking
    Created on : Jun 30, 2013, 5:45:00 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Create Booking</title>
		<script type="text/javascript" src="js/plugins/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"></script>
		<script type="text/javascript" src="js/plugins/jquery-2.0.2.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$("#datepicker").datepicker();
			});
		</script>
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <div class="container page">
            <h2>Create Booking</h2>
            <form action="createBooking" method="post">
                Date: <input type="text" class="input-medium" name="date" id="datepicker"/> &nbsp;
				Start Time:
				<select name="startTime" style="width: 100px;">
					<% for (int i = 9; i < 19; i++) {%>
					<option value="<% out.print(i + ":00:00");%>">
						<% out.print(i + ":00");%>
					</option>
					<% }%>
				</select> <br />
				<select name="termId">
					<option value="2013,1">2013-14 Term 1</option>
					<option value="2013,2">2013-14 Term 2</option>
				</select> &nbsp;
                <select name="milestone">
					<option value="acceptance">Acceptance</option>
					<option value="midterm">Midterm</option>
					<option value="final">Final</option>
				</select> <br /> <br />
                <input type="submit" class="btn btn-primary" value="Create"/>
            </form>
        </div>
		
    </body>
</html>
