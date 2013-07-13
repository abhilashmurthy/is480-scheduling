<%-- 
    Document   : navbar
    Created on : Jul 2, 2013, 11:26:58 PM
    Author     : ABHILASHM.2010
--%>

<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="model.User"%>

<%@include file="imports.jsp"%>

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
                    <li class="dropdown">
                        <a id="bookingDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Booking<b class="caret"></b></a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="newBooking">Create Booking</a></li>
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="ApproveReject.jsp">Approve Booking</a></li>
                        </ul>
                    </li>
                    <li class="dropdown">
                        <a id="scheduleDropDown" role="button" class="dropdown-toggle" data-toggle="dropdown">Schedule<b class="caret"></b></a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="drop1">
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="Schedule.jsp">Create Schedule</a></li>
                            <li role="presentation"><a role="menuitem" tabindex="-1" href="CreateTerm.jsp">Create Term</a></li>
                        </ul>
                    </li>
                    <!--<li id="navKnockout"><a href="KnockoutTest.jsp">Knockout Test</a></li>-->
                </ul>
            </div>

            <div class="btn-group userbox">
                <button class="btn btn-inverse"><%= user.getFullName() + " of Team " + user.getTeam().getTeamName() + ""%></button>
                <button class="btn btn-success dropdown-toggle" data-toggle="dropdown">
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu">
                    <li class="disabled"><a tabindex="-1" href="#">Manage settings</a></li>
                    <li><a id="logoutLink" tabindex="-1" href="#">Logout</a></li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript" src="js/plugins/jquery-2.0.2.js"></script>
<script type="text/javascript">
    console.log("nav init");
    //Nav specific
//    $(".dropdown-toggle").on('click', function() {
//        this.dropdown;
//    });
    $("#logoutLink").on('click', function() {
        document.location.href = '/is480-scheduling/logout';
    });
    $(".nav li").on('click', function() {
        $(".nav li").removeClass("active");
        $(this).addClass("active");
    });
</script>
