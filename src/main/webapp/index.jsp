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
            
            //Declare common variables
            //Default milestoneStr is ACCEPTANCE
            var milestoneStr = "ACCEPTANCE";
            var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
            var activeSemesterStr = "<%= activeTerm.getSemester()%>";
            var self = null;
            var teamName = null;
            var date = null;
            var startTime = null;
            var termId = null;

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
                        
                        //Append popovers after schedule load
                        appendPopovers();

                        //Setup mouse events
                        setupMouseEvents();
                    } else {
                        var eid = btoa(response.message);
                        window.location = "error.jsp?eid=" + eid;
                    }
                }).fail(function(error) {
                    alert("There was an error in retrieving schedule");
                });
            }
            
            //Append popover data
            function appendPopovers() {
            
                //Delete all old popovers
                $(".timeslotCell").popover('destroy');
                
                $(".bookedTimeslot").each(function(){
                   appendViewBookingPopover($(this));
                });
                
                function appendViewBookingPopover(bodyTd) {    
                    //Get View Booking Data
                    var cellId = bodyTd.attr('id').split("_")[1];
                    var data = {timeslotId: cellId};
                    var viewBookingData = getJson();

                    function getJson() {
                        var toReturn = null;
                        $.ajax({
                            type: 'GET',
                            async: false,
                            url: 'viewBookingJson',
                            data: data,
                            dataType: 'json'
                        }).done(function(response){
                            if (response.success) {
                                toReturn = response;
                            } else {
                                var eid = btoa(response.message);
                                window.location = "error.jsp?eid=" + eid;
                            }
                        }).fail(function(error){
                            toReturn = "AJAX fail";
                        });
                        return toReturn;
                    };

                    //Create Popover
                    bodyTd.popover({
                        container: '.page', //This is important for the popover to overflow the schedule
                        trigger: 'manual',
                        html: 'true',
                        content: function() {
                            //Output in the form of a table
                            var outputTable = $(document.createElement('table'));
                            outputTable.attr('id', 'viewTimeslotTable');

                            var outputData = [
                                ["Team", viewBookingData.teamName],
                                ["Date", viewBookingData.startDate],
                                ["Start Time", viewBookingData.startTime],
                                ["Team Wiki", viewBookingData.teamWiki],
                                ["Attendees", viewBookingData.attendees],
                                ["", "<button id='deleteBookingBtn' class='btn btn-danger'>Delete</button>"]
                            ];
                            for (var i = 0; i < outputData.length; i++) {
                                var outputTr = $(document.createElement('tr'));
                                var outputTdKey = $(document.createElement('td'))
                                    .html('<b>' + outputData[i][0] + '</b>');
                                var outputTdValue = $(document.createElement('td'))
                                    .html(outputData[i][1]);
                                outputTr.append(outputTdKey);
                                outputTr.append(outputTdValue);
                                outputTable.append(outputTr);
                            }
                            return outputTable;
                        },
                        placement: 'right',
                        title: function() {
                            if (viewBookingData.success) {
                                return "<b>Your Booking <b><button type='button' class='close'>&times;</button>";
                            } else {
                                //return "Error <button id='closeBookingBtn' class='btn btn-small btn-danger'>X</button>";
                                return "Error <button type='button' class='close'>&times;</button>";
                            }
                        }
                    });
                }
                
                teamName = "<%= team != null?team.getTeamName():null %>";
                var bookingExists = $("#" + milestoneStr.toLowerCase() + "ScheduleTable").find(":contains(" + teamName + ")").length;
                $(".unbookedTimeslot").each(function(){
                   appendCreateBookingPopover($(this));
                });
                
                function appendCreateBookingPopover(bodyTd) {
                    if (teamName !== null) {
                        //Is student and can book
                        if (bookingExists) {
                            bodyTd.popover({
                               container: '.page',
                               html: 'true',
                               trigger: 'manual',
                               placement: 'right',
                               title: '<b>Booking<b>',
                               content: 'You already have a booking!'
                            });
                        } else {
                            //Initialize variables
                            date = Date.parse(bodyTd.attr('value')).toString("yyyy-MM-dd");
                            startTime = Date.parse(bodyTd.attr('value')).toString("HH:mm:ss");
                            termId = activeAcademicYearStr + "," + activeSemesterStr;
                            var dateToView = Date.parse(date).toString("dd MMM");
                            var startTimeToView = Date.parse(startTime).toString("HH:mm");
                            var endTimeToView = new Date(Date.parse(bodyTd.attr('value'))).addHours(1).toString('HH:mm');
                            
                            //Create Booking outputTable
                            var outputTable = $(document.createElement('table'));
                            outputTable.attr('id', 'createTimeslotTable');                            

                            var outputData = [
                                ["Team", teamName],
                                ["Date", dateToView],
                                ["Start Time", startTimeToView],
                                ["End Time", endTimeToView],
                                ["Milestone", milestoneStr],
                                ["", "<button id='createBookingBtn' class='btn btn-primary'>Create</button>"]
                            ];
                            for (var i = 0; i < outputData.length; i++) {
                                var outputTr = $(document.createElement('tr'));
                                var outputTdKey = $(document.createElement('td'))
                                    .html('<b>' + outputData[i][0] + '</b>');
                                var outputTdValue = $(document.createElement('td'))
                                    .html(outputData[i][1]);
                                outputTr.append(outputTdKey);
                                outputTr.append(outputTdValue);
                                outputTable.append(outputTr);
                            }
                            
                            bodyTd.popover({
                               container: '.page',
                               html: 'true',
                               trigger: 'manual',
                               placement: 'right',
                               content: outputTable,
                               title: "Create Booking <button type='button' class='close'>&times;</button>"
                            });
                        }
                    }
                };
            };

            //Function to create mouse UI events
            function setupMouseEvents() {
                //Highlight cell
                $(".timeslotCell").mouseenter(function() {
                    $(this).css('border', '2px solid #1E647C');
                    $(this).css('cursor', 'pointer');
                });
                
                //Unhighlight cell
                $(".timeslotCell").mouseleave(function() {
                    $(this).css('border', '1px solid #dddddd');
                    $(this).removeClass("clickedCell");
                });
                
                //Hide all popovers on page click
                $(".page").on('click', function() {
                    console.log("page clicked");
                    //Close all timeslots
                    $(".timeslotCell").popover('hide');
                });
                
                //Add clickedCell and initialize common variables
                $(".timeslotCell").on('click', function(e){                    
                    self = $(this);
                    e.stopPropagation();
                    console.log("clicked");
                    
                    //Hide other popovers
                    $(".timeslotCell").each(function(){
                       if (self.attr('value') !== $(this).attr('value')) {
                           $(this).popover('hide');
                       } 
                    });
                    
                    //Add clickedCell class
                    $('td').removeClass("clickedCell");
                    self.addClass("clickedCell");
                });
                
                //Popover for booked timeslot
                $(".bookedTimeslot").on('click', function() {
                    self = $(this);
                    self.popover('show');
                });
                
                $(".unbookedTimeslot").on('click', function() {
                    self = $(this);
                    self.popover('show');
                    date = Date.parse(self.attr('value')).toString("yyyy-MM-dd");
                    startTime = Date.parse(self.attr('value')).toString("HH:mm:ss");
                    termId = activeAcademicYearStr + "," + activeSemesterStr;
                });
                
                //
                //Dynamic button bindings
                //Use this kind of event trigger for dynamic buttons
                //Close Booking Button
                $(".page").on('click', '#closeBookingBtn', function(e) {
                    e.stopPropagation();
                    self.popover('hide');
                    return false;
                });
                
                //Create Booking Button
                $(".page").on('click', '#createBookingBtn', function(e) {
                    e.stopPropagation();
                    createBooking(self);
                    appendPopovers(); //Refresh all popovers
                    return false;
                });

                //Delete Booking Button
                $(".page").on('click', '#deleteBookingBtn', function(e) {
                    e.stopPropagation();
                    deleteBooking(self);
                    appendPopovers(); //Refresh all popovers
                    return false;
                });
            }
            
            //AJAX CALL functions
            function createBooking(self) {
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
                        var outputTable = $(document.createElement('table'));
                        outputTable.attr('id', 'createTimeslotTable');
                        outputTable.addClass('bookingResult');
                        var responseBannerDiv = $(document.createElement('div'));
                        responseBannerDiv.attr('id', 'responseBanner');
                        if (response.success) {
                            responseBannerDiv.addClass('alert-success');
                            //Update the timeslot to bookedTimeslot on the schedule
                            self.html(teamName);
                            self.removeClass('unbookedTimeslot');
                            self.addClass('bookedTimeslot');
                        } else {
                            responseBannerDiv.addClass('alert-error');
                        }
                        var responseBannerSpan = $(document.createElement('span'));
                        responseBannerSpan.attr('id', 'responseMessage');
                        responseBannerSpan.html(response.message);
                        responseBannerDiv.append(responseBannerSpan);
                        outputTable.append(responseBannerDiv);

                        self.popover({
                            container: ".page",
                            title: "Result <button type='button' class='close'>&times;</button>",
                            placement: "right",
                            content: outputTable,
                            html: true
                        });
                        console.log('Toggling D');
                        self.popover('toggle');
                    } else {
                        var eid = btoa(response.message);
                        window.location = "error.jsp?eid=" + eid;
                    }
                }).fail(function(error) {
                    alert("Oops. There was an error: " + JSON.stringify(error));
                });
            }
            
            function deleteBooking(self) {
                //get the timeslotID for that cell and send as request
                var cellId = $(self).attr('id').split("_")[1];
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
                    var datesArray = getDates(minDate, maxDate);
                    
                    //Get dates between minDate and maxDate
                    function getDates(startDate, stopDate) {
                        var dateArray = new Array();
                        var currentDate = startDate;
                        while (currentDate <= stopDate) {
                            dateArray.push(currentDate);
                            currentDate = new Date(currentDate).addDays(1);
                        }
                        return dateArray;
                    }
                    
                    //Append header names
                    var headerDom = $(document.createElement('thead'));
                    var headerTr = $(document.createElement('tr'));
                    var headerTd = $(document.createElement('th'));
                    headerTr.attr('id', 'scheduleHeader');
                    headerTr.append(headerTd);
                    for (i = 0; i < datesArray.length; i++) {
                        var headerVal = new Date(datesArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(datesArray[i]).toString('ddd');
                        headerTd = $(document.createElement('th'));
                        headerTd.html(headerVal);
                        headerTr.append(headerTd);
                    }
                    headerDom.append(headerTr);
                    $("." + tableClass).append(headerDom);

                    //Append timeslot data
                    var rowspanArr = new Array();
                    for (var i = 0; i < timesArray.length; i++) {
                        
                        //Append time column
                        var bodyTr = $(document.createElement('tr'));
                        var bodyTd = $(document.createElement('td'));
                        var time = timesArray[i];
                        bodyTd.html('<b>' + time + '</b>');
                        bodyTr.append(bodyTd);
                        
                        //Append timeslot td's row by row
                        rowloop:
                        for (var j = 0; j < datesArray.length; j++) {
                            var date = datesArray[j];
                            date = new Date(date).toString("yyyy-MM-dd");
                            var datetimeString = date + " " + time + ":00";
                            
                            //Checking if table cell is part of a timeslot
                            for (var k = 0; k < rowspanArr.length; k++) {
                                if (datetimeString === rowspanArr[k]) {
                                    continue rowloop;
                                }
                            }

                            //Table cell not part of timeslot yet. Proceed.
                            var id = getTimeslotId(timeslots, date, time); //Get the timeslot id from datetime
                            var classes = new Array();  //Declare CSS classes of td
                            var team = null;
                            bodyTd = $(document.createElement('td'));
                            bodyTd.addClass('timeslotCell');

                            //If timeslot is available
                            if (id !== -1) {
                                bodyTd.attr('rowspan', '2');
                                var temp = new Date(Date.parse(datetimeString)).addMinutes(30).toString("yyyy-MM-dd HH:mm:ss");
                                rowspanArr.push(temp);
                                bodyTd.attr('id', 'timeslot_' + id);
                                bodyTd.attr('value', datetimeString);

                                //Get the team name from id
                                team = getTeam(timeslots, id);
                                if (team !== null) {
                                    bodyTd.html(team);
                                    bodyTd.addClass('bookedTimeslot');
                                } else {
                                    bodyTd.addClass('unbookedTimeslot');
                                }
                            } else {
                                bodyTd.addClass('noTimeslot');
                            }

                            //Append to row
                            bodyTr.append(bodyTd);
                        }
                        $("." + tableClass).append(bodyTr);
                    }
                }
                
                //Function to get timeslot_id from timestamp
                function getTimeslotId(timeslots, date, time) {
                    var datetimeString = (date + " " + time + ":00").trim();
                    for (var i = 0; i < timeslots.length; i++) {
                        if (timeslots[i].datetime === datetimeString) {
                            return timeslots[i].id;
                        }
                    }
                    return -1;
                }
                
                //Function to get team from timeslot_id
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
