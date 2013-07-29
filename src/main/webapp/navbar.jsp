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
   List<Role> userRoles = (List<Role>)session.getAttribute("userRoles");

   //Getting parameter from url
   String roleParam = request.getParameter("role");  
   List<Role> inactiveRoles = null;
   if (userRoles.size() > 1) {
	    //Validation checking for user's roles and setting the active role (in case of multiple roles)
		if (roleParam != null) {
			if (roleParam.equals("ar")) {
				if (isAdmin) {
					 session.setAttribute("activeRole", "Administrator");
				} else {
					 response.sendRedirect("multipleroles.jsp");   
				}
			} else if (roleParam.equals("sr")) {
				if (isSupervisor) {
					 session.setAttribute("activeRole", "Supervisor");
				} else {
					response.sendRedirect("multipleroles.jsp");
				}
			} else if (roleParam.equals("rr")) {
				if (isReviewer) {
					 session.setAttribute("activeRole", "Reviewer");
				} else {
					response.sendRedirect("multipleroles.jsp");
				}
			} else {
				//send error message
				response.sendRedirect("multipleroles.jsp");
			}
		}
		//Checking user's inactive role(s)
		inactiveRoles = new ArrayList<Role>();
		String activeRole = (String) session.getAttribute("activeRole");
		for (Role role: userRoles) {
			if (!role.getName().equalsIgnoreCase(activeRole)) {
				inactiveRoles.add(role);
			}
		}
   } else if (userRoles.size() == 1) {    //Setting active role if user has only 1 role
	   for (Role role: userRoles) {
		   session.setAttribute("activeRole", role.getName());
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
            <a class="brand" href="index.jsp">IS480 Scheduling</a>
            <div class="nav-collapse collapse">
                <ul class="nav">
				<%  String activeRole = (String) session.getAttribute("activeRole"); 
					if (activeRole.equalsIgnoreCase("Admin")) { %>
						<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject">Approve Booking</a></li>
                                                                <li role="presentation"><a role="menuitem" tabindex="-1" href="viewBooking.jsp">View Booking</a></li>
                                                        </ul>
							<a id="scheduleDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Schedule<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="newSchedule">Create Schedule</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="createterm.jsp">Create Term</a></li>
							</ul>
							<a id="adminConfigDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Admin Config<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>
							</ul>
						</li>
						<li id="Report"><a href="#">Report</a></li>
				<% } else if (activeRole.equalsIgnoreCase("Supervisor") || activeRole.equalsIgnoreCase("Reviewer")) { %>
						<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject">Approve Booking</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="viewBooking.jsp">View Booking</a></li>
							</ul>
                                                        
						</li>
				<% } else if (activeRole.equalsIgnoreCase("Student")) { %>
						<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="booking.jsp">Create Booking</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="viewBooking.jsp">View Booking</a></li>
							</ul>
						</li>
				<% } else if (activeRole.equalsIgnoreCase("TA")) { %>	
						<!--Nothing for now!-->
				<% } %>
                </ul>
				<!--<li id="navKnockout"><a href="timeslots.jsp">Timeslot Test</a></li>-->
            </div>

            <div class="btn-group userbox">
                <button class="btn" id="userDashboard"><i class="icon-user icon-black"></i>&nbsp;<%= user.getFullName().split(" ")[0]%> -  Dashboard</button>
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
    <p><strong>Name</strong><br/><% out.print(user.getFullName());%></p>
	
	 <% if (isStudent) { %>
		<p><strong>Team<br/></strong><% out.print(user.getTeam().getTeamName());%></p>
	 <% } %>
    
	<strong>Current Role</strong>
	<!-- For all roles -->
	<%  activeRole = (String) session.getAttribute("activeRole"); %>
		<ul class="unstyled">
			<li><%= activeRole %></li>
		</ul>
	<!-- For multiple roles -->
	<%  if (userRoles.size() > 1) { %>
		<strong>Other Role(s)</strong><br/>
		<div class="btn-group">
			<% for (Role role: inactiveRoles) { 
					if (role.getName().equalsIgnoreCase("Admin")) { %>
						<button class="btn" onclick="document.location.href='index.jsp?role=ar';"><%= role.getName() %></button>
			<%		} else if (role.getName().equalsIgnoreCase("Supervisor")) { %>
						<button class="btn" onclick="document.location.href='index.jsp?role=sr';"><%= role.getName() %></button>
			<%		} else if (role.getName().equalsIgnoreCase("Reviewer")) {  %>
						<button class="btn" onclick="document.location.href='index.jsp?role=rr';"><%= role.getName() %></button>
			<%		}  %>
					<!--<button type="button" class="btn btn-small" onClick="refreshPage()">Switch</button>-->
			<% } %>
		</div>
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
            title: 'Your Information',
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