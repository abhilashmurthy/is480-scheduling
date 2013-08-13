<%-- 
    Document   : navbar
    Created on : Jul 2, 2013, 11:26:58 PM
    Author     : ABHILASHM.2010
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="model.Role"%>
<%@page import="java.util.List"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="model.User"%>
<style type="text/css">
    i
    {
        padding-right:2px;
    }
</style>
<%@include file="imports.jsp"%>

<% //Getting session objects
   List<Role> userRoles = (List<Role>) session.getAttribute("userRoles");
   String activeRole = (String) session.getAttribute("activeRole");  //Active Role can never be empty
   boolean isSupervisorReviewer = false;
   boolean isAdministrator = false;
   boolean isCourseCoordinator = false;
   for (Role role: userRoles) {
		if (role.getName().equalsIgnoreCase("Supervisor") || 
				role.getName().equalsIgnoreCase("Reviewer")) {
			isSupervisorReviewer = true;
		} else if (role.getName().equalsIgnoreCase("Administrator")) {
			isAdministrator = true;
		} else if (role.getName().equalsIgnoreCase("Course Coordinator")) {
			isCourseCoordinator = true;
		}
   }
%>

<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="brand" href="index">IS480 Scheduling</a>
            <div class="nav-collapse collapse">
                <ul class="nav">
				<%  if (activeRole.equalsIgnoreCase("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator")) { %>
						<!--<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown"><b>Booking</b><b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject">Approve Booking</a></li>
							</ul>
						</li>-->
						<li class="dropdown">
							<a id="scheduleDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown"><b>Schedule</b><b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<!--<li role="presentation"><a role="menuitem" tabindex="-1" href="newSchedule">Create Schedule</a></li>-->
								<li role="presentation"><a role="menuitem" tabindex="-1" href="createschedule.jsp">Create Schedule</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="editschedule.jsp">Edit Schedule</a></li>
							</ul>
						</li>
<!--						<li class="dropdown">
							<a id="adminConfigDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown"><b>Admin Config</b><b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>
							</ul>
						</li>-->
						<!--<li id="Report"><a href="#"><b>Report</b></a></li>-->
						<li id="bookingHistory"><a href="bookingHistory"><b>Booking History</b></a></li>
				<% } else if (activeRole.equalsIgnoreCase("Supervisor/Reviewer")) { %>
						<!--<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a> -->
							<!--<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
						<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject"><b>Approve Booking</b></a></li>
						<!--</ul>-->
						<!--</li>-->
						<li id="bookingHistory"><a href="bookingHistory"><b>Booking History</b></a></li>
				<% } else if (activeRole.equalsIgnoreCase("Student")) { %>
<!--						<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
							<!--</ul>-->
						<!--</li>-->
						<li id="bookingHistory"><a href="bookingHistory"><b>Booking History</b></a></li>
				<% } else if (activeRole.equalsIgnoreCase("TA")) { %>	
						<li id="bookingHistory"><a href="bookingHistory"><b>Booking History</b></a></li>
						<!--Nothing for now!-->
				<% } %>
                </ul>
            </div>
				
            <div class="btn-group userbox">
                <button class="btn" id="userDashboard"><i class="icon-user icon-black"></i>&nbsp;<%= user.getFullName()%></button>
                <button class="btn btn-success dropdown-toggle" id="userAccess" data-toggle="dropdown">
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu pull-right" role="menu" aria-labelledby="dropdownMenu">
                    <li class="disabled"><a tabindex="-1" href="#"><i class="icon-wrench"></i>&nbsp;Manage settings</a></li>
                    <li><a id="logoutLink" tabindex="-1" href="#"><i class="icon-off"></i>&nbsp;Logout</a></li>
                </ul>
            </div>
        </div>
    </div>
</div>
				
<!-- USER DASHBOARD POPOVER CONTENT -->
<div style="visibility: collapse" id="userDashboardContent" hidden="">
    <!--<p><strong>Name</strong><br/><% out.print(user.getFullName());%></p>-->
	
	 <% if (activeRole.equalsIgnoreCase("Student")) { %>
		<p><strong>Team</strong><br/><% out.print(user.getTeam().getTeamName());%></p>
	 <% } %>
    
	<strong>Current Role</strong>
	<!-- For all roles -->
	<ul class="unstyled">
		<li><%= activeRole %></li>
	</ul>
	<!-- For multiple roles -->
	<%  if (userRoles.size() > 1) { %>
		<%  if (isAdministrator == true || isCourseCoordinator == true) { %>
			<strong>Other Role(s)</strong><br/>
			<form id="myform" action="setRole" method="post">
			<div class="btn-group">
				<% if (isAdministrator && (!activeRole.equalsIgnoreCase("Administrator"))) { %>
					<button type="submit" class="btn" value="Administrator" name="administrator">Administrator</button><br/>
				<% } %>
				<% if (isSupervisorReviewer && (!activeRole.equalsIgnoreCase("Supervisor/Reviewer"))) { %>
					<button type="submit" class="btn" value="Supervisor/Reviewer" name="supervisorReviewer">Supervisor/Reviewer</button><br/>
				<% } %>
				<% if (isCourseCoordinator && (!activeRole.equalsIgnoreCase("Course Coordinator"))) {  %>
					<button type="submit" class="btn" value="Course Coordinator" name="courseCoordinator">Course Coordinator</button>
				<% } %>
			</div>
			</form>
		<% } %>		
	<% } %>
</div>

<script type="text/javascript">
    //Makes use of footer.jsp's jQuery and bootstrap imports
    navbarLoad = function(){
        
        //Nav specific
        console.log("nav init");
        
        //Dropdown menu from bootstrap
        $(".dropdown-toggle").dropdown();
        
        //Dashboard popover
        $('#userDashboard').popover({
            container: '#userDashboard',
            placement: 'bottom',
            title: '<b>Your Information</b>',
			trigger: 'click',
            html: true,
            content: function() {
                return $('#userDashboardContent').html();
            }
        });
        
        //Logout link
        $("#logoutLink").on('click', function() {
            document.location.href = '/is480-scheduling/logout';
        });
        
        $("#userAccess").on('click', function(){
            if ($('#userDashboard > .popover').hasClass("in")) {
                $('#userDashboard').popover('hide');
            }
        });
        
    };
    
    addLoadEvent(navbarLoad);
</script>