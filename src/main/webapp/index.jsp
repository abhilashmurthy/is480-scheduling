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
        <!-- SECTION: Response Banner -->
        <div id="responseBanner" class="alert" hidden>
            <span id="responseMessage" style="font-weight: bold"></span>
        </div>
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
            
            //Default schedule to see upon opening index page
            populateSchedule(milestoneStr);
            
            //Index page stuff
            console.log("index init");
            $('#mileStoneTab a').on('click', function(e) {
                //Content TAB effect
                
                var id = $(this).attr('id');
                
                var contentId = id + "Content";
                $(".tab-pane").removeClass("active in");
                $(".tab-pane").hide();
                $("#" + contentId).addClass("active in");
                $("#" + contentId).show();
                
                milestoneStr = id.toUpperCase();
                clearSchedules();
                populateSchedule(milestoneStr);
            });
            
            function clearSchedules(){
                $(".scheduleTable").empty();
            }
            
            //View Schedule stuff
            function populateSchedule(milestone){
                //Generate schedule table
                var data = {milestoneString:milestone};
                console.log("Data to send: " + JSON.stringify(data));
                $('.scheduleTable').ready(function() {
                    $.ajax({
                        type: 'GET',
                        data: data,
                        url: 'getSchedule',
                        dataType: 'json'
                    }).done(function(response) {
                        makeSchedule(response);

                        $(".timeslotCell").mouseenter(function() {
                            $(this).css('border', '2px solid #FCFFBA');
                        });

                        $(".timeslotCell").mouseleave(function() {
                            $(this).css('border', '1px solid #dddddd');
                            clearCell(this);
                        });
                        setupPopovers();
                    });
                });
            }

            function setupPopovers() {
                //ViewBookingData JSON
                var viewBookingData = null;

                //Show/hide popovers
                $(".timeslotCell").on('click', function() {

                    //Clear cell
                    clearCell(this);

                    //Add clickedCell class
                    var currentClasses = $(this).attr('class');
                    $(this).removeClass("clickedCell");
                    $(this).attr('class', currentClasses + " " + "clickedCell");

                    //Populate JSON to show
                    viewBookingData = getJson(this);

                    function getJson(elem) {
                        var toReturn = null;

                        //Get cell ID
                        var cellId = $(elem).attr('id').split("_")[1];
                        var data = {timeslotId: cellId};

                        //AJAX View Booking details
                        $.ajax({
                            type: 'GET',
                            url: 'viewBookingJson',
                            data: data,
                            async: false,
                            dataType: 'json'
                        }).done(function(response) {
                            toReturn = response;
                        }).fail(function(error) {
                            toReturn = "AJAX fail";
                        });
                        return toReturn;
                    }
                });

                //Popover element
                $(".timeslotCell").popover({
                    container: 'body',
                    html: 'true',
                    trigger: 'click',
                    placement: 'right',
                    content: function() {
                        //Output in the form of a table
                        var output = "<table>";
                        var self = $(this);
                        if (!viewBookingData.error) {
                            //View booking
                            output += "<tr><td><b>Team Name: </b></td>";
                            output += "<td>" + viewBookingData.teamName + "</td></tr>";
                            output += "<tr><td><b>Date: </b></td>";
                            output += "<td>" + viewBookingData.startDate + "</td></tr>";
                            output += "<tr><td><b>Start Time: </b></td>";
                            output += "<td>" + viewBookingData.startTime + "</td></tr>";
                            output += "<tr><td><b>Team Wiki</b></td>";
                            output += "<td>" + viewBookingData.teamWiki + "</td></tr>";
                            output += "<tr><td><b>Attendees</b></td>";
                            output += "<td>";
                            for (var i = 0; i < viewBookingData.attendees.length; i++) {
                                output += viewBookingData.attendees[i].name;
                                output += "<br/>";
                            }
                            output += "</td></tr>";

                        } else {
                            //Create Booking
                            //Initialize values
                            var teamName = "<%= team.getTeamName() %>";
                            var supervisor = "<%= team.getSupervisor().getFullName() %>";
                            var reviewer1 = "<%= team.getReviewer1().getFullName() %>";
                            var reviewer2 = "<%= team.getReviewer2().getFullName() %>";
                            var dateToView = Date.parse(self.attr('value')).toString('dddd, dd MMM');
                            var date = Date.parse(self.attr('value')).toString('yyyy-MM-dd');
                            var startTimeToView = Date.parse(self.attr('value')).toString('HH:mm');
                            var startTime= Date.parse(self.attr('value')).toString('HH:mm:ss');
                            
                            //TODO: Change by milestone
                            var endTime = new Date(Date.parse(self.attr('value'))).addHours(1).toString('HH:mm');
                            var termId = "<%= activeTerm.getAcademicYear() %>,<%= activeTerm.getSemester() %>";
                            var termToView = termId.split(",")[0] + ", " + termId.split(",")[1];
                            var endTime = new Date(Date.parse(self.attr('value'))).addHours(1).toString('HH:mm');
                            
                            //Print values in form
                            output += "<tr><td><b>Team Name: </b></td>";
                            output += "<td>" + teamName + "</td></tr>";
                            output += "<tr><td><b>Supervisor </b></td>";
                            output += "<td>" + supervisor + "</td></tr>";
                            output += "<tr><td><b>Date </b></td>";
                            output += "<td>" + dateToView + "</td></tr>";
                            output += "<tr><td><b>Start Time </b></td>";
                            output += "<td>" + startTimeToView + "</td></tr>";
                            output += "<tr><td><b>End Time </b></td>";
                            output += "<td>" + endTime + "</td></tr>";
                            output += "<tr><td><b>Term </b></td>";
                            output += "<td>" + termToView + "</td></tr>";
                            output += "<tr><td><b>Milestone </b></td>";
                            output += "<td>" + milestoneStr + "</td></tr>";
                            output += "<tr><td><br/></td><td></td></tr>";
                            output += "<tr><td><button id='createBookingBtn' class='btn btn-primary'>Create</button></td>";
                            
                            //Create Booking AJAX
                            $("body").on('click', '#createBookingBtn', function(){
                                var data = {
                                  date: date,
                                  startTime: startTime,
                                  termId: termId,
                                  milestoneStr: milestoneStr.toLowerCase()
                                };
                                console.log("Submitting data: " + JSON.stringify(data));
                                $.ajax({
                                    type: 'POST',
                                    url: 'createBookingJson',
                                    data: data,
                                    dataType: 'json'
                                }).done(function(response) {
                                    $("#responseBanner").show();
                                    if (response.success) {
                                        $("#responseBanner").removeClass("alert-error").addClass("alert-success");
                                        $("#responseMessage").text(response.message);
                                        
                                        //Update the timeslot on the schedule
                                        self.html(teamName);
                                        self.addClass('bookedTimeslot');
                                        
                                    } else {
                                        $("#responseBanner").removeClass("alert-success").addClass("alert-error");
                                        $("#responseMessage").text(response.message);
                                    }
                                }).fail(function(response) {
                                    $("#responseBanner").show();
                                    $("#responseBanner").removeClass("alert-success").addClass("alert-error");
                                    $("#responseMessage").text("Oops. Something went wrong. Please try again!");
                                });
                                return false;
                            });
                            
                            
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
            }

            //Remove clickedCell class
            function clearCell(cell) {
                $(cell).removeClass("clickedCell");
            }

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
                        htmlString += "<td>" + time + "</td>";
                        rowloop: //Loop label
                                for (var j = 0; j < datesArray.length; j++) {
                            var date = datesArray[j];
                            date = new Date(date).toString("yyyy-MM-dd");
                            //Identifier for table cell
                            var datetimeString = date + " " + time + ":00";
                            //Checking if table cell is part of a timeslot
                            for (var k = 0; k < rowspanArr.length; k++) {

                                if (datetimeString === rowspanArr[k]) {
    //                                console.log("Skipped: " + datetimeString);
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
                            console.log("Class string: "+ classStr);
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