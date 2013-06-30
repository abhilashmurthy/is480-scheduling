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
            <h2>Create Timeslot</h2>
		<form action="/createBooking" method="post">
			Enter Team Id <input type="text" name="teamId"/> <br/>
			Enter Start Time <input type="text" name="startTime"/> <br/>
			Enter End Time <input type="text" name="endTime"/> <br/>
			Enter Term Id <input type="text" name="termId"/> <br/>
			Enter Milestone <input type="text" name="milestone"/> <br/>
			<input type="submit" value="Create"/>
		</form>
	</body>
</html>