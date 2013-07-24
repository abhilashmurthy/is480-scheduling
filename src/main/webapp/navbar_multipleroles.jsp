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
                <button class="btn btn-inverse" id="userDashboard"><i class="icon-user icon-white"></i>&nbsp;<%= user.getFullName().split(" ")[0]%> -  Dashboard</button>
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
    <p><strong>Name:</strong> <% out.print(user.getFullName());%></p>
    <p><strong>Team:</strong> <% out.print(user.getTeam().getTeamName());%></p>
    <p><strong>User Roles: </strong></p>
    <ul>

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
            title: "Your Information",
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