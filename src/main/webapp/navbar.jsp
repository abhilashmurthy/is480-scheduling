<%-- 
    Document   : navbar
    Created on : Jul 2, 2013, 11:26:58 PM
    Author     : ABHILASHM.2010
--%>

<%@page import="manager.UserManager"%>
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
            <a class="brand" href="index">IS480 Scheduling</a>
            <div class="nav-collapse collapse">
                <ul class="nav navbar-nav">
				<%  if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) { %>
						<li class="dropdown">
							<a href="#" id="scheduleDropDown" class="dropdown-toggle navbar-title" data-toggle="dropdown"><b>Schedule</b><b class="caret" style="border-bottom-color: white; border-top-color: white"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<!--<li role="presentation"><a role="menuitem" tabindex="-1" href="newSchedule">Create Schedule</a></li>-->
								<li role="presentation"><a role="menuitem" tabindex="-1" href="createSchedule">Create Schedule</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="editSchedule">Edit Schedule</a></li>
							</ul>
						</li>
						<li class="dropdown">
							<a href="#" id="adminConfigDropDown" class="dropdown-toggle navbar-title" data-toggle="dropdown"><b>Admin Config</b><b class="caret" style="border-bottom-color: white; border-top-color: white"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
								<li role="presentation"><a role="menuitem" tabindex="-1" href="getMilestoneSettings">Manage Milestones</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="manageActiveTerms">Manage Terms</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="uploadFile">CSV Upload</a></li>
								<li role="presentation"><a role="menuitem" tabindex="-1" href="getNotificationSettings">Manage Notifications</a></li>
								<!--<li role="presentation"><a role="menuitem" tabindex="-1" href=""></a></li>-->
							</ul>
						</li>
						<!--<li id="Report"><a href="#"><b>Report</b></a></li>-->
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>All Bookings</b></a></li>
						<li id="help"><a href="help.jsp" class="navbar-title"><i class="icon-question-sign icon-white"></i>&nbsp;<b>Help</b></a></li>
				<% } else if (activeRole.equals(Role.FACULTY)) { %>
						<!--<li class="dropdown">   
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a> -->
							<!--<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
						<li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject" class="navbar-title"><b>Approve Booking</b></a></li>
						<!--</ul>-->
						<!--</li>-->
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>My Bookings</b></a></li>
						<li id="bookingHistory"><a href="yourAvailability" class="navbar-title"><b>My Availability</b></a></li>
						<li id="help"><a href="help.jsp" class="navbar-title"><i class="icon-question-sign icon-white"></i>&nbsp;<b>Help</b></a></li>
				<% } else if (activeRole.equals(Role.STUDENT)) { %>
<!--						<li class="dropdown">
							<a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
							<ul class="dropdown-menu" role="menu" aria-labelledby="drop1">-->
							<!--</ul>-->
						<!--</li>-->
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>My Bookings</b></a></li>
						<li id="help"><a href="help.jsp" class="navbar-title"><i class="icon-question-sign icon-white"></i>&nbsp;<b>Help</b></a></li>
				<% } else if (activeRole.equals(Role.TA)) { %>	
						<li id="bookingHistory"><a href="bookingHistory" class="navbar-title"><b>My Bookings</b></a></li>
						<li id="bookingHistory"><a href="taAvailability" class="navbar-title"><b>Sign Up for Filming!</b></a></li>
						<li id="help"><a href="help.jsp" class="navbar-title"><i class="icon-question-sign icon-white"></i>&nbsp;<b>Help</b></a></li>
				<% } %>
				</ul>
			</div>
				<ul class="nav pull-right">
				<!-- To display the user information -->
				<li id="userInfo">
					<a class='navbar-username'>
					<i class="icon-user icon-white icon-large"></i>&nbsp;<% out.print(user.getFullName());%>
					&nbsp;-&nbsp;
					<% if (activeRole.equals(Role.STUDENT)) { %>
						Student
					<% } else if (activeRole.equals(Role.ADMINISTRATOR)) { %>
						Administrator
					<% } else if (activeRole.equals(Role.COURSE_COORDINATOR)) { %>
						Course Coordinator
					<% } else if (activeRole.equals(Role.FACULTY)) { %>
						Faculty
					<% } else if (activeRole.equals(Role.TA)) { %>
						TA
					<% } %>
					</a>	
				</li>
							
				<!-- To display the settings menu -->
				<li class="dropdown">
					<a class="dropdown-toggle navbar-title" data-toggle="dropdown" href="#">
						<i class="icon-cogs icon-large"></i>
					</a>
					<ul class="dropdown-menu pull-right" role="menu" aria-labelledby="dropdownMenu">
						<%  if (userRoles.size() > 1) { %>
						<%  if (isAdministrator == true || isCourseCoordinator == true) { %>
						<li class="dropdown-submenu">
							<a tabindex="-1" href="#"><i class="icon-user"></i>&nbsp&nbsp;Switch Role:</a>
							<ul class="dropdown-menu">
							<form id="myform" action="setRole" method="post">
								<% if (isAdministrator && (!activeRole.equals(Role.ADMINISTRATOR))) { %>
								<li>
									<button type="submit" class="btn-link" value="Administrator" name="administrator">Administrator</button>
								</li>
								<% } %>
								<% if (isFaculty && (!activeRole.equals(Role.FACULTY))) { %>
								<li>
									<button type="submit" class="btn-link" value="Faculty" name="faculty">Faculty</button>
								</li>
								<% } %>
								<% if (isCourseCoordinator && (!activeRole.equals(Role.COURSE_COORDINATOR))) {  %>
								<li>
									<button type="submit" class="btn-link" value="Course Coordinator" name="courseCoordinator">Course Coordinator</button>
								</li>
								<% } %>
							</form>
							 </ul>
						</li>
							<% } %>		
						<% } %>
							
						<li><a tabindex="-1" href="getUserPreferences"><i class="icon-wrench"></i>&nbsp;Manage settings</a></li>
						<li><a id="logoutLink" tabindex="-1" href="#"><i class="icon-off"></i>&nbsp;Logout</a></li>
					</ul>
				</li>
			</ul>
		</div>
	</div>
</div>


<script type="text/javascript">
    //Makes use of footer.jsp's jQuery and bootstrap imports
    navbarLoad = function(){
        
        //Dropdown menu from bootstrap
        $(".dropdown-toggle").dropdown();
        
        //Dashboard popover
//        $('#userDashboard').on('click', function(e) {
//            e.stopPropagation();
//            if ($(".userbox").hasClass('open')) {
//                $(".userbox").removeClass('open');
//            }
//            $(this).popover({
//                container: '#userDashboard',
//                placement: 'bottom',
//                title: '<b>Your Information</b>',
//                trigger: 'manual',
//                html: true,
//                content: function() {
//                    return $('#userDashboardContent').html();
//                }
//            });
//            $(this).popover('show');
//        });
        
        //Logout link
        $("#logoutLink").on('click', function() {
            document.location.href = '/is480-scheduling/logout';
			return false;
        });
        
        //Hide all popovers on other button click
        $("#userAccess").on('click', function(){
            if ($('.popover').hasClass("in")) {
                $('.popover').parent().popover('hide');
            }
			return false;
        });
        
        //Hide all popovers on page click
        $('body').on('click', function(e) {
            //Hide all popovers
			if ($(e.target).closest('.ui-datepicker').length || $(e.target).closest('.ui-timepicker-wrapper').length) return false;
            $('.popover.in').each(function(e){
                var self = $(this);
                //Don't detect datepicker and timepicker
				self.parent().popover('hide');
				$(".hasDatepicker").datepicker('destroy');
            });
            //Hide all notifications
            $.pnotify_remove_all();
        });    
            
        
        //Disable Pines Notify Settings
        $.pnotify.defaults.history = false;
        $.pnotify.defaults.delay = 3000;
    };
    
    addLoadEvent(navbarLoad);
</script>
