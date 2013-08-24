<%-- 
    Document   : navbar
    Created on : Jul 2, 2013, 11:26:58 PM
    Author     : ABHILASHM.2010
--%>

<%@page import="model.role.Student"%>
<%@page import="constant.Role"%>
<%@page import="java.util.ArrayList"%>
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
   List<User> userRoles = (List<User>) session.getAttribute("userRoles");
   Role activeRole = (Role) session.getAttribute("activeRole");  //Active Role can never be empty
   boolean isFaculty = false;
   boolean isAdministrator = false;
   boolean isCourseCoordinator = false;
   for (User userObj: userRoles) {
		if (userObj.getRole().equals(Role.FACULTY)) {
			isFaculty = true;
		} else if (userObj.getRole().equals(Role.ADMINISTRATOR)) {
			isAdministrator = true;
		} else if (userObj.getRole().equals(Role.COURSE_COORDINATOR)) {
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
            <a class="brand navbar-title" href="index">IS480 Scheduling</a>
            <div class="nav-collapse collapse">
                <ul class="nav">
				<%  if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) { %>
						<!--<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown"><b>Booking</b><b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject">Approve Booking</a></li>
							</ul>
						</li>-->
						<li class="dropdown">
							<a href="#" id="scheduleDropDown" class="dropdown-toggle navbar-title" data-toggle="dropdown"><b>Schedule</b><b class="caret" style="border-bottom-color: white; border-top-color: white"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<!--<li role="presentation"><a role="menuitem" tabindex="-1" href="newSchedule">Create Schedule</a></li>-->
								<li role="presentation"><a role="menuitem" tabindex="-1" href="createschedule.jsp">Create Schedule</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="editschedule.jsp">Edit Schedule</a></li>
							</ul>
						</li>
						<li class="dropdown">
							<a href="#" id="adminConfigDropDown" class="dropdown-toggle navbar-title" data-toggle="dropdown"><b>Admin Config</b><b class="caret" style="border-bottom-color: white; border-top-color: white"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="getMilestoneSettings">Manage Milestones</a></li>
								<!--<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>-->
							</ul>
						</li>
						<!--<li id="Report"><a href="#"><b>Report</b></a></li>-->
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>Booking History</b></a></li>
				<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<!--<li class="dropdown">   
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a> -->
							<!--<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
						<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject" class="navbar-title"><b>Approve Booking</b></a></li>
						<!--</ul>-->
						<!--</li>-->
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>Booking History</b></a></li>
						<li id="bookingHistory"><a href="availability.jsp" class="navbar-title"><b>Faculty Availability</b></a></li>
				<% } else if (activeRole.equals(Role.STUDENT)) { %>
<!--						<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
							<!--</ul>-->
						<!--</li>-->
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>Booking History</b></a></li>
				<% } else if (activeRole.equals(Role.TA)) { %>	
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>Booking History</b></a></li>
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
	
	 <% if (activeRole.equals(Role.STUDENT)) { 
			Student student = (Student) session.getAttribute("user");
	 %>
		<p><strong>Team</strong><br/><% out.print(student.getTeam().getTeamName());%></p>
	 <% } %>
    
	<strong>Current Role</strong>
	<!-- For all roles -->
	<ul class="unstyled">
		<li><%= activeRole.getDisplayName() %></li>
	</ul>
	<!-- For multiple roles -->
	<%  if (userRoles.size() > 1) { %>
		<%  if (isAdministrator == true || isCourseCoordinator == true) { %>
			<strong>Other Role(s)</strong><br/>
			<form id="myform" action="setRole" method="post">
			<div class="btn-group">
				<% if (isAdministrator && (!activeRole.equals(Role.ADMINISTRATOR))) { %>
					<button type="submit" class="btn btn-small" value="Administrator" name="administrator">Administrator</button><br/>
				<% } %>
				<% if (isFaculty && (!activeRole.equals(Role.FACULTY))) { %>
					<button type="submit" class="btn btn-small" value="Faculty" name="faculty">Faculty</button><br/>
				<% } %>
				<% if (isCourseCoordinator && (!activeRole.equals(Role.COURSE_COORDINATOR))) {  %>
					<button type="submit" class="btn btn-small" value="Course Coordinator" name="courseCoordinator">Course Coordinator</button>
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
        $('#userDashboard').on('click', function(e) {
            e.stopPropagation();
            if ($(".userbox").hasClass('open')) {
                $(".userbox").removeClass('open');
            }
            $(this).popover({
                container: '#userDashboard',
                placement: 'bottom',
                title: '<b>Your Information</b>',
                trigger: 'manual',
                html: true,
                content: function() {
                    return $('#userDashboardContent').html();
                }
            });
            $(this).popover('show');
        });
        
        //Logout link
        $("#logoutLink").on('click', function() {
            document.location.href = '/is480-scheduling/logout';
        });
        
        //Hide all popovers on other button click
        $("#userAccess").on('click', function(){
            if ($('.popover').hasClass("in")) {
                $('.popover').parent().popover('hide');
            }
        });
        
        //Hide all popovers on page click
        $("body").on('click', function(){
            if ($('.popover').hasClass("in")) {
                $('.popover').parent().popover('hide');
            }
        });
        
    };
    
    addLoadEvent(navbarLoad);
</script>
