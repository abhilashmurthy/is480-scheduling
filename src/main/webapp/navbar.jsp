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
   boolean isStudent = (Boolean)session.getAttribute("isStudent");
   boolean isSupervisor = (Boolean)session.getAttribute("isSupervisor");
   boolean isReviewer = (Boolean)session.getAttribute("isReviewer");
   boolean isTA = (Boolean)session.getAttribute("isTA");
   boolean isAdmin = (Boolean)session.getAttribute("isAdmin");
   List<Role> userRoles = (List<Role>) session.getAttribute("userRoles");
   String activeRole = (String) session.getAttribute("activeRole");  //Active Role can never be empty
   
   List<Role> inactiveRoles = null;
   if (userRoles.size() > 1) {
		inactiveRoles = (List<Role>) session.getAttribute("inactiveRolesList");
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
            <a class="brand" href="index.jsp">IS480 Scheduling</a>
            <div class="nav-collapse collapse">
                <ul class="nav">
				<%  if (activeRole.equalsIgnoreCase("Administrator")) { %>
						<li class="dropdown">
							<a id="scheduleDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Schedule<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="newSchedule">Create Schedule</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="createterm.jsp">Create Term</a></li>
							</ul>
						</li>
						<li class="dropdown">
							<a id="adminConfigDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Admin Config<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>
							</ul>
						</li>
						<li id="Report"><a href="#">Report</a></li>
						
				<% } else if (activeRole.equalsIgnoreCase("Supervisor") || activeRole.equalsIgnoreCase("Reviewer")) { %>
						<!--<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a> -->
							<!--<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
						<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject">Approve Booking</a></li>
						<!--</ul>-->
						<!--</li>-->
						<li id="myBookings"><a href="myBookings">My Bookings</a></li>
				<% } else if (activeRole.equalsIgnoreCase("Student")) { %>
<!--						<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
							<!--</ul>-->
						<!--</li>-->
						<li id="myBookings"><a href="myBookings">My Bookings</a></li>
				<% } else if (activeRole.equalsIgnoreCase("TA")) { %>	
						<li id="myBookings"><a href="myBookings">My Bookings</a></li>
						<!--Nothing for now!-->
				<% } %>
                </ul>
            </div>
				
            <div class="btn-group userbox">
                <button class="btn" id="userDashboard"><i class="icon-user icon-black"></i>&nbsp;<%= user.getFullName()%></button>
                <button class="btn btn-success dropdown-toggle" data-toggle="dropdown">
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
	
	 <% if (isStudent) { %>
		<p><strong>Team<br/></strong><% out.print(user.getTeam().getTeamName());%></p>
	 <% } %>
    
	<strong>Role</strong>
	<!-- For all roles -->
	<%  activeRole = (String) session.getAttribute("activeRole"); %>
		<ul class="unstyled">
			<li><%= activeRole %></li>
		</ul>
	<!-- For multiple roles -->
	<%  if (userRoles.size() > 1) { %>
		<strong>Other Role(s)</strong><br/>
		<form id="myform" action="setRole" method="post">
		<div class="btn-group">
			<% for (Role role: inactiveRoles) { 
					if (role.getName().equalsIgnoreCase("Administrator")) { %>
						<button type="submit" class="btn" value="Administrator" name="administrator"><%= role.getName() %></button>
						<!--<button class="btn"><%= role.getName() %></button>-->
			<%		} else if (role.getName().equalsIgnoreCase("Supervisor")) { %>
						<button type="submit" class="btn" value="Supervisor" name="supervisor"><%= role.getName() %></button>
						<!--<button class="btn" onclick="document.location.href='index.jsp?role=sr';"><%= role.getName() %></button>-->
			<%		} else if (role.getName().equalsIgnoreCase("Reviewer")) {  %>
						<button type="submit" class="btn" value="Reviewer" name="reviewer"><%= role.getName() %></button>
						<!--<button class="btn" onclick="document.location.href='index.jsp?role=rr';"><%= role.getName() %></button>-->
			<%		}  %>
					<!--<button type="button" class="btn btn-small" onClick="refreshPage()">Switch</button>-->
			<% } %>
		</div>
		</form>
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
        
    };
    
    addLoadEvent(navbarLoad);
</script>