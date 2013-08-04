<%@page import="model.Team"%>
<%@page import="model.Term"%>
<%@page import="model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<%
    Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>IS480 Scheduling</title>
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>

        <%
            Team team = user.getTeam();
            String fullName = user.getFullName();
        %>

        <!-- Welcome Text -->
        <div class="container page" />
        <h3 id="activeTermName">
            <%
                String yearPlus1 = String.valueOf(activeTerm.getAcademicYear() + 1);
                String termName = activeTerm.getAcademicYear()
                        + "/" + yearPlus1.substring(2) + " " + activeTerm.getSemester();
                out.print(termName);
            %>
        </h3>
    </div>

    <table class="legend">
        <tr>
            <!-- <td style="width:50px"><b>Legend:</b></td>-->
            <td style="background-color:#AEC7C9;width:17px;"></td><td>&nbsp;Available</td> 
            <td style="width:15px"></td>
            <td style="background-color:#a9dba9;width:17px;"></td><td>&nbsp;Confirmed</td> 
            <td style="width:15px"></td>
            <td style="background-color:#F75D59;width:17px;"></td><td>&nbsp;Rejected</td> 
            <td style="width:15px"></td>
            <td style="background-color:#F6EE4E;width:17px;"></td><td>&nbsp;Pending</td> 
            <td style="width:15px"></td>
            <td style="background-color:#D1D0CE;width:17px;"></td><td>&nbsp;Not Available</td> 
        </tr>
    </table>

    <!-- Main schedule navigation -->
    <div class="container page">
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
                <table id="acceptanceScheduleTable" class="scheduleTable table-condensed table-hover table-bordered">
                </table>
            </div>
            <div class="tab-pane fade" id="midtermContent" hidden>
                <table id="midtermScheduleTable" class="scheduleTable table-condensed table-hover table-bordered">
                </table>
            </div>
            <div class="tab-pane fade" id="finalContent" hidden>
                <table id="finalScheduleTable" class="scheduleTable table-condensed table-hover table-bordered">
                </table>
            </div>
        </div>
        <br />
    </div>

    <%@include file="footer.jsp"%>

    <!-- View Schedule Javascript -->
    <!-- jshashset imports -->
    <script type="text/javascript" src="js/plugins/jshashtable-3.0.js"></script>
    <script type="text/javascript" src="js/plugins/jshashset-3.0.js"></script>
    <script type="text/javascript">
        //Makes use of footer.jsp's jQuery and bootstrap imports
        viewScheduleLoad = function() {

            //Default milestoneStr is ACCEPTANCE
            var milestoneStr = "ACCEPTANCE";
            var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
            var activeSemesterStr = "<%= activeTerm.getSemester()%>";

            //Default schedule to see upon opening index page
            populateSchedule(milestoneStr, activeAcademicYearStr, activeSemesterStr);

            //Index page stuff
            console.log("index init");
            //Function to change schedule based on selected milestone tab
            $('#mileStoneTab a').on('click', function(e) {
                //Content TAB effect
                var tabId = $(this).attr('id');
                var contentId = tabId + "Content";
                $(".tab-pane").removeClass("active in");
                $(".tab-pane").hide();
                $("#" + contentId).addClass("active in");
                $("#" + contentId).show();

                clearSchedules();
                milestoneStr = tabId.toUpperCase();
                activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                activeSemesterStr = "<%= activeTerm.getSemester()%>";
                populateSchedule(milestoneStr, activeAcademicYearStr, activeSemesterStr);
            });

            //Function to empty schedules
            function clearSchedules() {
                $(".scheduleTable").empty();
            }

            //View Schedule stuff
            //Function to populate schedule data based on ACTIVE TERM
            //TODO: Change this to populate schedule based on dropdown select
            function populateSchedule(milestone, year, semester) {
                //Generate schedule table
                var data = {
                    milestoneString: milestone,
                    academicYearString: year,
                    semesterString: semester
                };
                //View Schedule AJAX
                $.ajax({
                    type: 'GET',
                    data: data,
                    url: 'getSchedule',
                    cache: false,
                    dataType: 'json'
                }).done(function(response) {
                    if (response.success) {
                        //Draw the schedule table
                        makeSchedule(response);

                        //Setup mouse events
                        setupMouseEvents();

                        //Setup Booking popover data
                        setupPopovers();
                    } else {
                        var eid = btoa(response.message);
                        window.location = "error.jsp?eid=" + eid;
                    }
                }).fail(function(error) {
                    alert("There was an error in retrieving schedule");
                });
            }

            //Function to create mouse UI events
            function setupMouseEvents() {
                $(".page").on('click', function() {
                    console.log("page clicked");
                    var popovers = $(".container div.popover:visible");
                    if (popovers.length) {
                        popovers.remove();
                    }
                });

                $(".timeslotCell").mouseenter(function() {
                    $(this).css('border', '2px solid #1E647C');
                    $(this).css('cursor', 'crosshair');
                });

                $(".timeslotCell").mouseleave(function() {
                    $(this).css('border', '1px solid #dddddd');
                    $(this).removeClass("clickedCell");
                });
            }

            //Function to setup popover events
            function setupPopovers() {
                //Setup common variables
                var self = null;
                var date = null;
                var teamName = null;
                var supervisor = null;
                var reviewer1 = null;
                var reviewer2 = null;
                var date = null;
                var startTime = null;
                var endTime = null;
                var termId = null;
                var endTime = null;

                //This will be populated based on a clicked timeslotCell
                var viewBookingData = null;

                $(".timeslotCell").on('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log("clicked");

                    //Initialize variables
                    self = $(this);
                    teamName = "<%= team.getTeamName()%>";
                    supervisor = "<%= team.getSupervisor().getFullName()%>";
                    reviewer1 = "<%= team.getReviewer1().getFullName()%>";
                    reviewer2 = "<%= team.getReviewer2().getFullName()%>";
                    startTime = Date.parse(self.attr('value')).toString('HH:mm:ss');
                    //TODO: Change endtime by milestone
                    endTime = new Date(Date.parse(self.attr('value'))).addHours(1).toString('HH:mm');
                    date = Date.parse(self.attr('value')).toString('yyyy-MM-dd');
                    termId = activeAcademicYearStr + "," + activeSemesterStr;

                    //Clear other popovers
                    $(".popover").detach();

                    //Add clickedCell class
                    $(this).removeClass("clickedCell");
                    $(this).addClass("clickedCell");

                    //Populate JSON to show
                    viewBookingData = getViewBookingJson(this);

                    function getViewBookingJson(elem) {
                        var toReturn = null;

                        //Get cell ID
                        var cellId = $(elem).attr('id').split("_")[1];
                        var data = {timeslotId: cellId};

                        //View Booking AJAX
                        $.ajax({
                            type: 'GET',
                            url: 'viewBookingJson',
                            data: data,
                            async: false,
                            cache: false,
                            dataType: 'json'
                        }).done(function(response) {
                            if (response.success) {
                                toReturn = response;
                            } else {
                                var eid = btoa(response.message);
                                window.location = "error.jsp?eid=" + eid;
                            }
                        }).fail(function(error) {
                            toReturn = "AJAX fail";
                        });
                        return toReturn;
                    }

                    //delete booking
                    $(".page").on('click', '#deleteBookingBtn', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        deleteBooking(self);
                        return false;
                    });

                    //delete booking button
                    $(".page").on('click', '#closeBookingBtn', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        console.log('Destroying B');
                        self.popover('destroy');
                        return false;
                    });

                    //delete booking function
                    function deleteBooking(elem) {
                        //get the timeslotID for that cell and send as request
                        var cellId = $(elem).attr('id').split("_")[1];
                        var data = {timeslotId: cellId};
                        console.log("Submitting delete booking data: " + JSON.stringify(data));
                        console.log("data");
                        //Delete Booking AJAX
                        $.ajax({
                            type: 'POST',
                            async: false,
                            url: 'deleteBookingJson',
                            data: data,
                            cache: false,
                            dataType: 'json'
                        }).done(function(response) {
                            if (!response.exception) {
                                console.log('Destroying A');
                                self.popover('destroy');
                                var resultStr = "<table id='createTimeslotTable' class='bookingResult'><tr><td>";
                                resultStr += "<div id='responseBanner'";
                                if (response.success) {
                                    resultStr += " class='alert-success'>";

                                    //Update the timeslot to unbookedTimeslot on the schedule
                                    self.html('');
                                    self.removeClass('bookedTimeslot');
                                    self.addClass('unbookedTimeslot');
                                } else {
                                    resultStr += " class='alert-error'>";
                                }
                                resultStr += "<span id='responseMessage'>";
                                resultStr += response.message;
                                resultStr += "</span>";
                                resultStr += "</div>";
                                resultStr += "</table>";

                                self.popover({
                                    container: ".page",
                                    title: "Result <button type='button' class='close'>&times;</button>",
                                    placement: "right",
                                    content: resultStr,
                                    html: true
                                });
                                console.log('Toggling D');
                                self.popover('toggle');
                            } else {
                                var eid = btoa(response.message);
                                window.location = "error.jsp?eid=" + eid;
                            }
                        }).fail(function(error) {
                            alert("Oops. There was an error: " + error);
                        });
                    }

                });

                //-----------------------------//

                //Popover for booked timeslot
                $(".bookedTimeslot").on('click', function() {
                    $(this).popover({
                        container: '.page', //This is important for the popover to overflow the schedule
                        trigger: 'manual',
                        html: 'true',
                        placement: 'right',
                        content: function() {
                            //Output in the form of a table
                            var output = "<table id='viewTimeslotTable'>";

                            //There should be no error for bookedTimeslot
                            if (!viewBookingData.error) {
                                //View booking
                                output += "<tr><td><b>Team: </b></td>";
                                output += "<td>" + viewBookingData.teamName + "</td></tr>";
                                output += "<tr><td><b>Date: </b></td>";
                                output += "<td>" + viewBookingData.startDate + "</td></tr>";
                                output += "<tr><td><b>Start Time: </b></td>";
                                output += "<td>" + viewBookingData.startTime + "</td></tr>";
                                output += "<tr><td><b>Team Wiki: </b></td>";
                                output += "<td>" + viewBookingData.teamWiki + "</td></tr>";
                                output += "<tr><td><b>Attendees: </b></td>";
                                output += "<td>";
                                for (var i = 0; i < viewBookingData.attendees.length; i++) {
                                    output += viewBookingData.attendees[i].name;
                                    output += "<br/>";
                                }
                                output += "</td></tr>";

                                //go through the list of attendees and check if the current user name matches that
                                //of the attendees and also check if it is a student
                                for (var i = 0; i < viewBookingData.attendees.length; i++) {
                                    var personnel = viewBookingData.attendees[i].name;
                                    //var status = viewBookingData.attendees[i].status;
                                    if ($.trim(personnel) === '<%=fullName%>' && <%=isStudent == true%> || <%=isAdmin == true%>) {
                                        output += "<tr>";
                                        output += "<td><button id='deleteBookingBtn' class='btn btn-primary'>Delete</button></td>";
                                        output += "</tr>";
                                        break;
                                    }
                                }

                            } else {
                                //There was an error
                                output += "<tr><td>Oops. There was an error..</td></tr>";
                            }
                            //Close table
                            output += "</table>";
                            return output;
                        },
                        title: function() {
                            if (!viewBookingData.error) {
                                return "View Booking <button type='button' class='close'>&times;</button>";
                            } else {
                                //return "Error <button id='closeBookingBtn' class='btn btn-small btn-danger'>X</button>";
                                return "Error <button type='button' class='close'>&times;</button>";
                            }
                        }
                    });
                    console.log('Toggling A');
                    $(this).popover('toggle');
                });

                //Popover for unbookedTimeslot
                $(".unbookedTimeslot").on('click', function() {
                    //Check if booking already exists
                    var exists = $("#" + milestoneStr.toLowerCase() + "ScheduleTable").find(":contains(" + teamName + ")").length;
                    if (exists) {
                        //If booking exists, just create it so that the error can be produced
                        createBooking();
                    } else {
                        $(this).popover({
                            container: '.page', //This is important for the popover to overflow the schedule
                            html: 'true',
                            trigger: 'manual',
                            placement: 'right',
                            content: function() {
                                //Output in the form of a table
                                var output = "<table id='createTimeslotTable'>";

                                //There should be an error in viewBookingData for an unbookedTimeslot
                                if (viewBookingData.error) {
                                    //Create Booking
                                    var termToView = termId;
                                    var dateToView = Date.parse(date).toString("dd MMM");
                                    var startTimeToView = Date.parse(startTime).toString("HH:mm");

                                    //Print values in create booking
                                    output += "<tr><td><b>Team: </b></td>";
                                    output += "<td>" + teamName + "</td></tr>";
                                    output += "<tr><td><b>Date: </b></td>";
                                    output += "<td>" + dateToView + "</td></tr>";
                                    output += "<tr><td><b>Start Time: </b></td>";
                                    output += "<td>" + startTimeToView + "</td></tr>";
                                    output += "<tr><td><b>End Time: </b></td>";
                                    output += "<td>" + endTime + "</td></tr>";
                                    output += "<tr><td><b>Term: </b></td>";
                                    output += "<td>" + termToView + "</td></tr>";
                                    output += "<tr><td><b>Milestone: </b></td>";
                                    output += "<td>" + milestoneStr + "</td></tr>";
                                    //TODO: Change this according to acceptance, midterm, and final
                                    //                            output += "<tr><td><b>Reviewer 1: </b></td>";
                                    //                            output += "<td>" + reviewer1 + "</td></tr>";
                                    //                            output += "<tr><td><b>Reviewer 2: </b></td>";
                                    //                            output += "<td>" + reviewer2 + "</td></tr>";
                                    output += "<tr><td><br/></td><td></td></tr>";
                                    output += "<tr>";
                                    output += "<td><button id='createBookingBtn' class='btn btn-primary'>Create</button></td>";
                                    output += "</tr>";
                                } else {
                                    output += "<tr><td>There was an error... </td></tr>";
                                }
                                //Close table
                                output += "</table>";
                                return output;
                            },
                            title: function() {
                                if (viewBookingData.error) {
                                    return "Create Booking <button type='button' class='close'>&times;</button>";
                                } else {
                                    return "Error <button type='button' class='close'>&times;</button>";
                                }
                            }
                        });
                        console.log('Toggling B');
                        $(this).popover('toggle');
                    }
                });

                //-----------------------------//

                //NOTE: Using body.onclick instead createBookingBtn.onclick
                //Use this kind of event trigger for dynamic buttons
                $(".page").on('click', '#createBookingBtn', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    createBooking();
                    return false;
                });

                $(".page").on('click', '#closeBookingBtn', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log('Destroying B');
                    self.popover('destroy');
                    return false;
                });

                function createBooking() {
                    var data = {
                        date: date,
                        startTime: startTime,
                        termId: termId,
                        milestoneStr: milestoneStr.toLowerCase()
                    };
                    console.log("Submitting create booking data: " + JSON.stringify(data));
                    //Create Booking AJAX
                    $.ajax({
                        type: 'POST',
                        async: false,
                        url: 'createBookingJson',
                        data: data,
                        cache: false,
                        dataType: 'json'
                    }).done(function(response) {
                        if (!response.exception) {
                            console.log('Destroying A');
                            self.popover('destroy');
                            var resultStr = "<table id='viewTimeslotTable' class='bookingResult'><tr><td>";
                            resultStr += "<div id='responseBanner'";
                            if (response.success) {
                                resultStr += " class='alert-success'>";

                                //Update the timeslot to bookedTimeslot on the schedule
                                self.html(teamName);
                                self.removeClass('unbookedTimeslot');
                                self.addClass('bookedTimeslot');
                            } else {
                                resultStr += " class='alert-error'>";
                            }
                            resultStr += "<span id='responseMessage'>";
                            resultStr += response.message;
                            resultStr += "</span>";
                            resultStr += "</div>";
                            resultStr += "</table>";

                            self.popover({
                                container: ".page",
                                title: "Result <button type='button' class='close'>&times;</button>",
                                placement: "right",
                                content: resultStr,
                                html: true
                            });
                            console.log('Toggling D');
                            self.popover('toggle');
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        alert("Oops. There was an error: " + error);
                    });
                }
            }

            //Function to make schedule based on GetScheduleAction response
            function makeSchedule(data) {

                makeSchedule("scheduleTable", data.timeslots);

                function makeSchedule(tableClass, timeslots) {

                    //TODO: Get from server/admin console/whatevs
                    var minTime = 9;
                    var maxTime = 19;

                    var timesArray = new Array();
                    for (var i = minTime; i < maxTime; i++) {
                        var timeVal = Date.parse(i + ":00:00");
                        timesArray.push(timeVal.toString("HH:mm"));
                        timeVal.addMinutes(30);
                        timesArray.push(timeVal.toString("HH:mm"));
                    }

                    //Get unique dates, minDate, and maxDate
                    var datesSet = new HashSet();
                    for (i = 0; i < timeslots.length; i++) {
                        datesSet.add(Date.parse(timeslots[i].datetime).toString("yyyy-MM-dd"));
                    }
                    var datesHashArray = datesSet.values().sort();
                    var minDate = new Date(datesHashArray[0]);
                    var maxDate = new Date(datesHashArray[datesHashArray.length - 1]);
                    //                console.log("Mindate: " + minDate + ", maxDate: " + maxDate);
                    var datesArray = getDates(minDate, maxDate);

                    function getDates(startDate, stopDate) {
                        var dateArray = new Array();
                        var currentDate = startDate;
                        while (currentDate <= stopDate) {
                            dateArray.push(currentDate);
                            currentDate = new Date(currentDate).addDays(1);
                        }
                        return dateArray;
                    }

                    //Get dates between minDate and maxDate

                    //Append header names
                    var headerString = "<thead><tr id='scheduleHeader'><th></th>";
                    for (i = 0; i < datesArray.length; i++) {
                        headerString += "<th>" + new Date(datesArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(datesArray[i]).toString('ddd') + "</th>";
                    }
                    headerString += "</tr></thead>";
                    $("." + tableClass).append(headerString);

                    //Append timeslot data
                    var rowspanArr = new Array();
                    for (var i = 0; i < timesArray.length; i++) {
                        var htmlString = "<tr>";
                        var time = timesArray[i];
                        htmlString += "<td><b>" + time + "</b></td>";
                        rowloop: //Loop label
                                for (var j = 0; j < datesArray.length; j++) {
                            var date = datesArray[j];
                            date = new Date(date).toString("yyyy-MM-dd");
                            //Identifier for table cell
                            var datetimeString = date + " " + time + ":00";
                            //Checking if table cell is part of a timeslot
                            for (var k = 0; k < rowspanArr.length; k++) {

                                if (datetimeString === rowspanArr[k]) {
                                    //                              console.log("Skipped: " + datetimeString);
                                    continue rowloop;
                                }
                            }

                            //Table cell not part of timeslot yet. Proceed.

                            //Get the timeslot id from datetime
                            var id = getTimeslotId(timeslots, date, time);
                            var classes = new Array();
                            var team = null;
                            classes.push('timeslotCell');
                            htmlString += "<td";

                            //If timeslot is available
                            if (id !== -1) {
                                htmlString += " rowspan='2'";
                                var temp = new Date(Date.parse(datetimeString)).addMinutes(30).toString("yyyy-MM-dd HH:mm:ss");
                                //                            console.log("Temp is: " + temp);
                                rowspanArr.push(temp);
                                htmlString += " id='timeslot_" + id + "'";
                                htmlString += " value='" + datetimeString + "'";

                                //Get the team name from id
                                team = getTeam(timeslots, id);
                                if (team !== null) {
                                    classes.push('bookedTimeslot');
                                } else {
                                    classes.push('unbookedTimeslot');
                                }
                            } else {
                                classes.push('noTimeslot');
                            }

                            //Add classes
                            var classStr = " class='";
                            for (var c = 0; c < classes.length; c++) {
                                classStr += classes[c] + ' ';
                            }
                            classStr += "'>";
                            htmlString += classStr;

                            //Add team
                            if (team !== null) {
                                htmlString += team;
                            }

                            //Close td
                            htmlString += "</td>";

                        }
                        htmlString += "</tr>";
                        $("." + tableClass).append(htmlString);
                    }
                }

                function getTimeslotId(timeslots, date, time) {
                    var datetimeString = (date + " " + time + ":00").trim();
                    //                console.log("Date string: " + datetimeString);
                    for (var i = 0; i < timeslots.length; i++) {
                        if (timeslots[i].datetime === datetimeString) {
                            return timeslots[i].id;
                        }
                    }
                    return -1;
                }

                function getTeam(timeslots, id) {
                    for (var i = 0; i < timeslots.length; i++) {
                        if (timeslots[i].id === id && timeslots[i].team) {
                            return timeslots[i].team;
                        }
                    }
                    return null;
                }
            }

        };

        addLoadEvent(viewScheduleLoad);
    </script>
    <br/>
    <%--<%@include file="navbar_footer.jsp" %>--%>
</body>
</html>
