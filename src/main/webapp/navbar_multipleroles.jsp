<%-- 
    Document   : navbar
    Created on : Jul 24, 2013, 11:26:58 PM
    Author     : Prakhar
--%>

<%@page import="model.Role"%>
<%@page import="java.util.List"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="model.User"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<style type="text/css">
    i
    {
        padding-right:2px;
    }
</style>
<%  List<Role> userRoles = (List<Role>) session.getAttribute("userRoles");
	boolean isSupervisorReviewer = false;
	boolean isAdministrator = false;
	boolean isCourseCoordinator = false;
	//Kicking user out
	if (userRoles.size() > 1) {
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
		//If user is just supervisor && reviewer, kick him out 
		if (isAdministrator == false && isCourseCoordinator == false) {
			request.setAttribute("error", "Oops. You are not authorized to access this page!");
			RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
			rd.forward(request, response);
		}
	} else {
		request.setAttribute("error", "Oops. You are not authorized to access this page!");
		RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
		rd.forward(request, response);
	}
%>
<%@include file="imports.jsp"%>

<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="brand">IS480 Scheduling</a>
           
			<div class="btn-group userbox">
                <button class="btn" id="userDashboard"><i class="icon-user icon-black"></i>&nbsp;<%= user.getFullName() %></button>
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
    <strong>Roles</strong>
	<ul class="unstyled">
		<%--<s:if test="%{isAdministrator}">--%>
		<% if (isAdministrator) { %>
		   <li>Administrator</li>
		<% } %>
		<%--</s:if>--%>
		<%--<s:if test="%{isSupervisorReviewer}">--%>
		<% if (isSupervisorReviewer) { %>
		   <li>Supervisor/Reviewer</li>
		<% } %>
		<%--</s:if>--%>
		<%--<s:if test="%{isCourseCoordinator}">--%>
		<% if (isCourseCoordinator) { %>
		   <li>Course Coordinator</li>
		<% } %>
		<%--</s:if>--%>
	</ul>
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