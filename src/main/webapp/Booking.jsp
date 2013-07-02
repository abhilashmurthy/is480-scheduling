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
		<div class="navbar navbar-inverse navbar-fixed-top">
			<div class="navbar-inner">
				<div class="container">
					<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button>
					<a class="brand" href="Index.jsp">IS480 Scheduling</a>
					<div class="nav-collapse collapse">
						<ul class="nav">
							<li><a href="Index.jsp">Home</a></li>
							<li class="active"><a href="Booking.jsp">Create Booking</a></li>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>

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