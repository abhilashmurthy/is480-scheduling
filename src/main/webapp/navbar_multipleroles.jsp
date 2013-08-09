<%-- 
    Document   : navbar
    Created on : Jul 24, 2013, 11:26:58 PM
    Author     : Prakhar
--%>

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
<% boolean isStudent = (Boolean)session.getAttribute("isStudent");
   boolean isSupervisor = (Boolean)session.getAttribute("isSupervisor");
   boolean isReviewer = (Boolean)session.getAttribute("isReviewer");
   boolean isTA = (Boolean)session.getAttribute("isTA");
   boolean isAdmin = (Boolean)session.getAttribute("isAdmin");
%>
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
    <!--<p><strong>Name</strong><br/> <% out.print(user.getFullName());%></p>-->
    <strong>Role(s)</strong>
	<ul class="unstyled">
		<% if (isAdmin) { %>
		   <li>Administrator</li>
		<% } %>
		<% if (isSupervisor) { %>
		   <li>Supervisor</li>
		<% } %>
		<% if (isReviewer) { %>
		   <li>Reviewer</li>
		<% } %>
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