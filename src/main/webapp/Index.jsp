<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>IS480 Scheduling</title>
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
							<li class="active"><a href="Index.jsp">Home</a></li>
							<li><a href="Booking.jsp">Create Booking</a></li>
                                                        <li><a href="AcceptReject.jsp">Accept/Reject Booking</a></li>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>
        <h1>Welcome to the IS480 Scheduling Project</h1>
		<script src="js/bootstrap.js" type="text/javascript"></script>
    </body>
</html>
