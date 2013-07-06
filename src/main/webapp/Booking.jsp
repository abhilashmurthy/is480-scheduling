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
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <div class="container page">
            <h2>Create Booking</h2>
            <form action="createBooking" method="post">
                Start Time: <input type="text" class="input-medium" name="startTime" /> &nbsp;
                End Time: <input type="text" class="input-medium" name="endTime"/> <br/>
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
