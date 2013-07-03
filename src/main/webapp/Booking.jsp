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
        <!-- Bootstrap -->
        <link href="css/bootstrap.css" rel="stylesheet" media="screen">
        <style>
            body {
                padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
                padding-left: 10px;
            }
        </style>
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>

        <h2>Create Booking</h2>
        <form action="createBooking" method="post">
            Enter Team Id <input type="text" name="teamId"/> <br/>
            Enter Start Time <input type="text" name="startTime"/> <br/>
            Enter End Time <input type="text" name="endTime"/> <br/>
            Enter Term Id <input type="text" name="termId"/> <br/>
            Enter Milestone <input type="text" name="milestone"/> <br/>
            <input type="submit" class="btn btn-primary" value="Create"/>
        </form>
        <script src="js/bootstrap.js" type="text/javascript"></script>
    </body>
</html>
