<%-- 
    Document   : navbar
    Created on : Jul 2, 2013, 11:26:58 PM
    Author     : ABHILASHM.2010
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
            <a class="brand" href="index.jsp">IS480 Scheduling</a>
            <div class="nav-collapse collapse">
                <ul class="nav">
                    <li class="dropdown">
                        <a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="booking.jsp">Create Booking</a></li>
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="approveReject">Approve Booking</a></li>
                        </ul>
                    </li>
                    <li class="dropdown">
                        <a id="scheduleDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Schedule<b class="caret"></b></a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="newSchedule">Create Schedule</a></li>
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="createterm.jsp">Create Term</a></li>
                        </ul>
                    </li>
                    <li id="navKnockout"><a href="timeslots.jsp">Timeslot Test</a></li>
                </ul>
            </div>

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
    <p>Name: <% out.print(user.getFullName());%></p>
    <p>Team: <% out.print(user.getTeam().getTeamName());%></p>
    <p>User Roles:</p>
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
            title: "User Information",
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