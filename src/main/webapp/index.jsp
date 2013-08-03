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
			//User user = (User) session.getAttribute("user");
            Team team = user.getTeam();
        %>

        <!-- Welcome Text -->
        <div class="container" />
        <h2 id="activeTermName">
            <%
                String yearPlus1 = String.valueOf(activeTerm.getAcademicYear() + 1);
                String termName = activeTerm.getAcademicYear()
                        + "/" + yearPlus1.substring(2) + " " + activeTerm.getSemester();
                out.print(termName);
            %>
        </h2>
    </div>
	<table class="legend">
		<tr>
			<!-- <td style="width:50px"><b>Legend:</b></td>-->
			<td style="background-color:#AEC7C9;width:17px;"></td><td>&nbsp;No Booking</td> 
			<td style="width:15px"></td>
			<td style="background-color:#F6EE4E;width:17px;"></td><td>&nbsp;Booking Made</td> 
			<td style="width:15px"></td>
			<td style="background-color:#a9dba9;width:17px;"></td><td>&nbsp;Booking Confirmed</td> 
			<td style="width:15px"></td>
			<td style="background-color:#D1D0CE;width:17px;"></td><td>&nbsp;Holiday</td> 
		</tr>
	</table>
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

                    //Draw the schedule table
                    makeSchedule(response);

                    //Setup mouse events
                    setupMouseEvents();

                    //Setup Booking popover data
                    setupPopovers();
                });
            }

            //Function to create mouse UI events
            function setupMouseEvents() {
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
                
                //This will be populated based on a clicked timeslotCell
                var viewBookingData = null;
                
                $(".timeslotCell").on('click', function() {
                    console.log("clicked");
                    var self = $(this);

                    //Clear other popovers
                    $(".popover").detach();

                    //Add clickedCell class
                    $(this).removeClass("clickedCell");
                    var currentClasses = $(this).attr('class');
                    $(this).removeClass("clickedCell");
                    $(this).attr('class', currentClasses + " " + "clickedCell");

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
                            toReturn = response;
                        }).fail(function(error) {
                            toReturn = "AJAX fail";
                        });
                        return toReturn;
                    }
                });

                //Setup common variables
                var self = null;
                var date = null;
                var teamName = null;
                //var supervisor = null;
                var reviewer1 = null;
                var reviewer2 = null;
                var dateToView = null;
                var date = null;
                var startTimeToView = null;
                var startTime = null;
                var endTime = null;
                var termId = null;
                var termToView = null;
                var endTime = null;

                //Create Popover based on view booking data
                $(".timeslotCell").popover({
                    container: 'body', //This is important for the popover to overflow the schedule
                    html: 'true',
                    trigger: 'click',
                    placement: 'right',
                    content: function() {
                        self = $(this);

                        //Output in the form of a table
                        var output = "<table id='viewTimeslotTable' width='600'>";

                        //If there is a booking on the timeslotCell
                        if (!viewBookingData.error) {
                            //View booking
							var output = "<table id='viewTimeslotTable' width='300'>";
							
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
                        } else {
                            //Create Booking
                            //Initialize values
                            teamName = "<%= team.getTeamName()%>";
                            //supervisor = "<%= team.getSupervisor().getFullName()%>";
                            reviewer1 = "<%= team.getReviewer1().getFullName()%>";
                            reviewer2 = "<%= team.getReviewer2().getFullName()%>";
                            dateToView = Date.parse(self.attr('value')).toString('dddd, dd MMM');
                            date = Date.parse(self.attr('value')).toString('yyyy-MM-dd');
                            startTimeToView = Date.parse(self.attr('value')).toString('HH:mm');
                            startTime = Date.parse(self.attr('value')).toString('HH:mm:ss');

                            //TODO: Change by milestone
                            endTime = new Date(Date.parse(self.attr('value'))).addHours(1).toString('HH:mm');
                            termId = activeAcademicYearStr + "," + activeSemesterStr;
                            termToView = termId.split(",")[0] + ", " + termId.split(",")[1];
                            endTime = new Date(Date.parse(self.attr('value'))).addHours(1).toString('HH:mm');
							
							var output = "<table id='viewTimeslotTable' width='250'>";
							
                            //Print values in create booking
                            output += "<tr><td><b>Team: </b></td>";
                            output += "<td>" + teamName + "</td></tr>";
                            //output += "<tr><td><b>Supervisor </b></td>";
                            //output += "<td>" + supervisor + "</td></tr>";
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
                            output += "<tr><td><br/></td><td></td></tr>";
                            output += "<tr><td><button id='createBookingBtn' class='btn btn-primary'>Create</button></td>";
                            
                            //Todo: Change this according to acceptance, midterm, and final
//                            output += "<tr><td><b>Reviewer 1: </b></td>";
//                            output += "<td>" + reviewer1 + "</td></tr>";
//                            output += "<tr><td><b>Reviewer 2: </b></td>";
//                            output += "<td>" + reviewer2 + "</td></tr>";
                        }
                        //Close table
                        output += "</table>";
                        return output;
                    },
                    title: function() {
                        if (!viewBookingData.error) {
                            return "View Booking";
                        } else {
                            return "Create Booking";
                        }
                    }
                });
                viewBookingData = null;
                
                 //NOTE: Using body.onclick instead createBookingBtn.onclick
                 //Use this kind of event trigger for dynamic buttons
                 $("body").on('click', '#createBookingBtn', function() {
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
                         self.popover('destroy');
                         var resultStr = "<table id='viewTimeslotTable'><tr><td>";
                         resultStr += "<div id='responseBanner'";
                         if (response.success) {
                             resultStr += " class='alert-success'>";

                             //Update the timeslot on the schedule
                             self.html(teamName);
                             self.addClass('bookedTimeslot');
                         } else {
                             resultStr += " class='alert-error'>";
                         }
                         resultStr += "<span id='responseMessage'>";
                         resultStr += response.message;
                         resultStr += "</span>";
                         resultStr += "</div>";
                         resultStr += "</td></tr>";
                         resultStr += "</table>";

                         self.popover({
                             container: 'body',
                             title: 'Result',
                             placement: 'right',
                             content: resultStr,
                             html: true
                         });
                         self.popover('show');

                         setTimeout(function(){
                             $(".popover").slideUp(300, function(){
                                 $(this).remove();
                             });
                         }, 3000);


                     }).fail(function(response) {
                         alert("Oops. There was an error");
                     });
                     return false;
                 });
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
                            if (new Date(currentDate).isWeekday()) {
                                dateArray.push(currentDate);
                            }
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
</body>
</html>