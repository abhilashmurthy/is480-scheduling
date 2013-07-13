<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>IS480 Scheduling</title>
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        
        <!-- Welcome Text -->
        <div class="container page" />
            <h1 id="welcomeText">Welcome to the IS480 Scheduling Project</h1>
        </div>
        
        <!-- Main schedule navigation -->
        <div class="container">
            <ul id="mileStoneTab" class="nav nav-tabs">
                <!-- TODO: populate dynamic milestones -->
                <li class="active">
                    <a id="acceptance" href="#acceptance" data-toggle="tab">Acceptance</a>
                </li>
                <li class>
                    <a id="midterm" href="#midterm" data-toggle="tab">Midterm</a>
                </li>
                <li class>
                    <a id="final" href="#final" data-toggle="tab">Final</a>
                </li>
            </ul>
            <div id="milestoneTabContent" class="tab-content">
                <div class="tab-pane fade active in" id="acceptanceContent">
                    <p>Acceptance stuff</p>
                    <div class="container" data-bind="load: loadData()">

                        <table data-bind="visible: bookings().length > 0" class="bookings">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Start Time</th>
                                    <th>End Time</th>
                                    <th>Team</th>
                                    <th>Update Booking</th>
                                    <th>Delete Booking</th>
                                </tr>
                            </thead>
                            <tbody data-bind="foreach: bookings">
                                <tr>
                                    <td>
                                        <span data-bind="text: date"></span>
                                        <!--<input type="text" class="username" data-bind="value: username, visible: usernameUpdate, hasfocus: usernameUpdate">-->
                                    </td>
                                    <td>
                                        <span data-bind="text: startTime"></span>
                                        <!--<input type="text" class="username" data-bind="value: username, visible: usernameUpdate, hasfocus: usernameUpdate">-->
                                    </td>
                                    <td>
                                        <span data-bind="text: endTime"></span>
                                        <!--<input type="text" class="username" data-bind="value: username, visible: usernameUpdate, hasfocus: usernameUpdate">-->
                                    </td>
                                    <td>
                                        <span data-bind="text: team"></span>
                                        <!--<input type="text" class="username" data-bind="value: username, visible: usernameUpdate, hasfocus: usernameUpdate">-->
                                    </td>
                                    <td data-bind="click: $root.updateBooking"><a href="#">Update</a></td>
                                    <td data-bind="click: $root.deleteBooking"><a href="#">Delete</a></td>
                                </tr>
                            </tbody>
                        </table>  
                    </div>
                </div>
                <div class="tab-pane fade" id="midtermContent"><p>Midterm stuff</p></div>
                <div class="tab-pane fade" id="finalContent"><p>Final stuff</p></div>
            </div>
        </div>
    </body>
</html>