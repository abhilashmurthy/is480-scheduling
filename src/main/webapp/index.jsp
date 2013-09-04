<%@page import="model.role.Student"%>
<%@page import="model.Team"%>
<%@page import="model.Term"%>
<%@page import="model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
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
            Team team = null;
            String fullName = null;
            if (activeRole.equals(Role.STUDENT)) {
                Student studentUser = (Student) session.getAttribute("user");
                team = studentUser.getTeam();
                fullName = studentUser.getFullName();
            }
        %>

        <!-- Welcome Text -->
        <div class="container page" >
            <h3 id="activeTermName"><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></h3>

            <!-- To display the list of active terms -->
            <div class="activeTerms">
                <table>
                    <tr>
                        <td style="width:90px; padding-bottom:11px"><b>Select Term</b></td>
                        <td><form id="activeTermForm" action="index" method="post">
                                <select name="termId" style="float:right" onchange="this.form.submit()"> 
                                    <option value=""><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></option>
                                    <s:iterator value="data">
                                        <option value="<s:property value="termId"/>"><s:property value="termName"/></option>
                                    </s:iterator>
                                </select>
                            </form></td>
                    </tr>
                </table>
            </div>

            <!-- To display number of pending bookings for supervisor/reviewer -->
            <% if (activeRole.equals(Role.FACULTY)) {%>
            <s:if test="%{pendingBookingCount > 0}">
                <div class="pendingBookings alert">
                    <button type="button" class="close" data-dismiss="alert">×</button>
                    <a href="approveReject" style="color:#B88A00;">
                        <s:if test="%{pendingBookingCount > 1}">
                            <b>You have <s:property value="pendingBookingCount"/> pending bookings!</b>
                        </s:if><s:else>
                            <b>You have <s:property value="pendingBookingCount"/> pending booking!</b>
                        </s:else>
                    </a>
                </div>
            </s:if>
            <% }%>

            <!-- To display legend for the calendar -->
            <table class="legend">
                <tr>
                    <!-- <td style="width:50px"><b>Legend:</b></td>-->
                    <td style="background-color:#AEC7C9;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Available</td> 
                    <td style="width:15px"></td>
                    <td class="legendBox pendingBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Pending</td> 
                    <td style="width:15px"></td>
                    <td class="legendBox approvedBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Approved</td> 
                    <td style="width:15px"></td>
                    <td class="legendBox timeslotCell unavailableTimeslot" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Not Available</td> 
                </tr>
            </table>

        </div>

        <!-- Main schedule navigation -->
        <div class="container page">
            <ul id="milestoneTab" class="nav nav-tabs">
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
            <div id="milestoneTabContent" class="tab-content" hidden="">
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
            <div id="scheduleProgressBar" class="progress progress-striped active">
                <div class="bar" style="width: 100%;"></div>
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
                //Index page stuff
                console.log("index init");
                
                /*****************************
                    DECLARE COMMON VARIABLES
                ******************************/
               
                //Default milestoneStr is ACCEPTANCE
                var milestoneStr = "ACCEPTANCE";
                var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                var activeSemesterStr = "<%= activeTerm.getSemester()%>";
                var scheduleData = null; //This state shall be stored here
                
                //Student-specific variables
                var teamName = "<%= team != null ? team.getTeamName() : null%>"; //Student's active team name
                
                //Admin specific variables
                var teams = JSON.parse('<%= session.getAttribute("allTeams")%>'); //All teams JSON
                var teamDropDownSelect = null;
                var createBookingOutputForAdmin = null;
                
                //Booking specific variables
                var self = null;
                var users = JSON.parse('<%= session.getAttribute("allUsers") %>'); //All users JSON

                loadDefault();

                function loadDefault() {
                    //Default schedule to see upon opening index page
                    $("#milestoneTab a#" + milestoneStr.toLowerCase()).tab('show');
                    populateSchedule(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                }

                //Function to change schedule based on selected milestone tab
                $('#milestoneTab a').on('click', function(e) {
                    $("#milestoneTab").removeClass('active in');
                    $(this).tab('show');
                    clearSchedules();
                    milestoneStr = $(this).attr('id').toUpperCase();
                    activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                    activeSemesterStr = "<%= activeTerm.getSemester()%>";
                    populateSchedule(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                    return false;
                });

                //Function to empty schedules
                function clearSchedules() {
                    $(".scheduleTable").empty();
                }
                
                //GetScheduleAction data
                function getScheduleData(milestone, year, semester) {
                    var toReturn = null;
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
                        async: false,
                        cache: false,
                        dataType: 'json'
                    }).done(function(response) {
                        toReturn = response;
                    }).fail(function(error) {
                        alert("There was an error in retrieving schedule");
                    });
                    return toReturn;
                }

                //View Schedule stuff
                //Function to populate schedule data based on ACTIVE TERM
                function populateSchedule(milestone, year, semester) {
                    //Hide schedule tab and show progress bar
                    $("#milestoneTabContent").hide();
                    $("#scheduleProgressBar").show();
                    //Generate schedule table
                    scheduleData = getScheduleData(milestone, year, semester);
                    if (scheduleData.success) {
                        //Convert scheduleData timeslots
                        convertScheduleData();
                        //Draw the schedule table
                        makeSchedule();
                        appendPopovers();
                        //Setup mouse events
                        setupMouseEvents();
                    } else {
                        var eid = btoa(scheduleData.message);
                        window.location = "error.jsp?eid=" + eid;
                    }
                    $("#scheduleProgressBar").hide();
                    $("#milestoneTabContent").show();
                }
                
                //Convert scheduleData to better JSON object
                function convertScheduleData() {
                    var timeslots = scheduleData.timeslots;
                    var newTimeslots = {};
                    for (var i = 0; i < timeslots.length; i++) {
                        var timeslot = timeslots[i];
                        var key = timeslot.datetime;
                        newTimeslots[key] = timeslot;
                    }
                    scheduleData["timeslots"] = newTimeslots;
                }
                
                //Append popover data
                function appendPopovers() {
                    //Show progress bar
                    $("#milestoneTabContent").hide();
                    $("#scheduleProgressBar").show();
                    
                    //Delete all old popovers
                    $(".timeslotCell").trigger('mouseleave');
                    $(".timeslotCell").popover('destroy');
                    
                    refreshScheduleData();

                    //Add View Booking popovers
                    $(".bookedTimeslot").each(function() {
                        appendViewBookingPopover($(this));
                    });

                    //Add Create Booking popover for all timeslots if is student, admin, or course coordinator
                    if (<%= activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                        $(".unbookedTimeslot, .unavailableTimeslot").each(function() {
                            appendCreateBookingPopover($(this));
                        });
                    }
                    if (<%= activeRole.equals(Role.FACULTY) %>) {
                        $(".unbookedTimeslot, .unavailableTimeslot").each(function() {
                            appendChangeAvailabilityPopover($(this));
                        });
                    }
                    //Show progress bar
                    $("#scheduleProgressBar").hide();
                    $("#milestoneTabContent").show();
                }
                
                
                function appendViewBookingPopover(bodyTd) {
                    //Get View Booking Data
                    var timeslots = scheduleData.timeslots;
                    var booking = bodyTd.children('.booking');
                    var viewBookingData = timeslots[bodyTd.attr('value')];

                    //Append booking classes based on status
                    if (booking) {
                        if (viewBookingData.status === "APPROVED") {
                            booking.addClass('approvedBooking');
                        } else if (viewBookingData.status === "REJECTED") {
                            booking.addClass('rejectedBooking');
                        } else {
                            booking.addClass('pendingBooking');
                        }
                    }

                    //Popover
                    booking.popover({
                        container: booking, //This is important for the popover to overflow the schedule
                        trigger: 'manual',
                        html: 'true',
                        content: function() {
                            //Output in the form of a table
                            var outputTable = $(document.createElement('table'));
                            outputTable.attr('id', 'viewTimeslotTable');
                            var optionalAttendees = $(document.createElement('input')).attr('id', 'updateAttendees').addClass('optionalAttendees');
                            var outputData = [
                                ["Team", viewBookingData.team],
                                ["Status", viewBookingData.status],
                                ["Date", viewBookingData.startDate],
                                ["Time", viewBookingData.time],
                                ["Team Wiki", viewBookingData.teamWiki],
                                ["Students", viewBookingData.students],
                                ["Faculty", viewBookingData.faculties],
                                ["Optional", optionalAttendees]
                            ];
                            
                            //Add Delete and Update button if user is a student with a team
                            if (viewBookingData.team === teamName) {
                                console.log("How come ah?");
                                var buttonDelete = $(document.createElement('button'));
                                buttonDelete.attr('id', 'deleteBookingBtn');
                                buttonDelete.addClass('popoverBtn btn btn-danger');
                                var iconDelete = $(document.createElement('i'));
                                iconDelete.addClass('icon-trash icon-white');
                                buttonDelete.append(iconDelete);
                                buttonDelete.append("Delete");
                                
                                var buttonUpdate = $(document.createElement('button'));
                                buttonUpdate.attr('id', 'updateBookingBtn');
                                buttonUpdate.addClass('popoverBtn btn btn-info');
                                var iconUpdate = $(document.createElement('i'));
                                iconUpdate.addClass('icon-edit icon-white');
                                buttonUpdate.append(iconUpdate);
                                buttonUpdate.append("Save");
                                
                                outputData.push(["", buttonDelete.outerHTML() + buttonUpdate.outerHTML()]);
                            }

                            //Make fields editable and add Update and Delete buttons if user is admin
                            if (<%=activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                                //Replace startDate and startTime with editable fields
                                for (var i = 0; i < outputData.length; i++) {
                                    if (outputData[i][0] === "Date") {
                                        var input = $(document.createElement('input'));
                                        input.attr('id', 'updateFormDate');
                                        input.attr('type', 'text');
                                        input.attr('placeholder', outputData[i][1]);
                                        input.attr('title', 'Enter a new date (YYYY-MM-DD)');
                                        input.addClass('updateFormDate popoverInput');
                                        input.datepicker({
                                            dateFormat: "yy-mm-dd",
                                            beforeShowDay: $.datepicker.noWeekends
                                        });
                                        outputData[i][1] = input;
                                    }
                                    if (outputData[i][0] === "Time") {
                                        var input = $(document.createElement('input'));
                                        input.attr('id', 'updateFormStartTime');
                                        input.attr('type', 'text');
                                        input.attr('placeholder', outputData[i][1]);
                                        input.attr('title', 'Enter a new start time (HH:MM)');
                                        input.addClass('updateFormStartTime popoverInput');
                                        input.timepicker({
                                            minTime: Date.parse(scheduleData.dayStartTime + ":00").toString("HH:mm"),
                                            maxTime: Date.parse(scheduleData.dayEndTime + ":00").addHours(-2).toString("HH:mm"),
                                            step: 30,
                                            forceRoundTime: true,
                                            timeFormat: 'H:i'
                                        });
                                        outputData[i][1] = input;
                                    }
                                }
                                //Add Update AND Delete button
                                var buttonDelete = $(document.createElement('button'));
                                buttonDelete.attr('id', 'deleteBookingBtn');
                                buttonDelete.addClass('popoverBtn btn btn-danger');
                                var iconDelete = $(document.createElement('i'));
                                iconDelete.addClass('icon-trash icon-white');
                                buttonDelete.append(iconDelete);
                                buttonDelete.append("Delete");
                                
                                var buttonUpdate = $(document.createElement('button'));
                                buttonUpdate.attr('id', 'updateBookingBtn');
                                buttonUpdate.addClass('popoverBtn btn btn-info');
                                var iconUpdate = $(document.createElement('i'));
                                iconUpdate.addClass('icon-edit icon-white');
                                buttonUpdate.append(iconUpdate);
                                buttonUpdate.append("Save");
                                
                                outputData.push(["", buttonDelete.outerHTML() + buttonUpdate.outerHTML()]);
                            }

                            //Append all fields
                            for (var i = 0; i < outputData.length; i++) {
                                var outputTr = $(document.createElement('tr'));
                                var outputTdKey = $(document.createElement('td')).html(outputData[i][0]);
                                var outputTdValue = null;
                                if (outputData[i][1] instanceof Array) {
                                    var outputArray = outputData[i][1];
                                    var outputArrayStr = "";
                                    //If print students
                                    if (outputData[i][0] === "Students") {
                                        for (var j = 0; j < outputArray.length; j++) {
                                            outputArrayStr += outputArray[j].name + "<br/>";
                                        }
                                    }
                                    //If print faculty
                                    if (outputData[i][0] === "Faculty") {
                                        for (var j = 0; j < outputArray.length; j++) {
                                            outputArrayStr += outputArray[j].name + " (" + outputArray[j].status.toLowerCase() + ")" + "<br/>";
                                        }
                                    }
                                    outputTdValue = $(document.createElement('td')).html(outputArrayStr);
                                } else {
                                    outputTdValue = $(document.createElement('td')).html(outputData[i][1]);
                                }
                                outputTr.append(outputTdKey);
                                outputTr.append(outputTdValue);
                                outputTable.append(outputTr);
                            }
                            return outputTable;
                        },
                        placement: function() {
                            if (bodyTd.parent().children().index(bodyTd) > 9) {
                                return 'left';
                            } else {
                                return 'right';
                            }
                        },
                        title: function() {
                            var buttonClose = $(document.createElement('button'));
                            buttonClose.attr('type', 'button');
                            buttonClose.addClass('close');
                            buttonClose.append("&times;"); //X sign
                            if (viewBookingData.team === teamName) {
                                return "<b>Your Booking</b>" + buttonClose.outerHTML();
                            } else {
                                return "Team Booking" + buttonClose.outerHTML();
                            }
                        }
                    });
                }
                
                function appendCreateBookingPopover(bodyTd) {
                    if (bodyTd.hasClass('legendBox')) return;
                    //Initialize variables
                    var popoverTitle = null;
                    var termId = activeAcademicYearStr + "," + activeSemesterStr;
                    var dateToView = Date.parse(bodyTd.attr('value')).toString("dd MMM");
                    var startTimeToView = Date.parse(bodyTd.attr('value')).toString("HH:mm");
                    var endTimeToView = new Date(Date.parse(bodyTd.attr('value'))).addMinutes(scheduleData.duration).toString('HH:mm');

                    //Create Booking outputTable
                    var outputTable = $(document.createElement('table'));
                    outputTable.attr('id', 'createTimeslotTable');
                    var outputData = [
                        ["Date", dateToView],
                        ["Time", startTimeToView + " - " + endTimeToView],
                        ["Milestone", milestoneStr]
                    ];
                    if (<%= activeRole.equals(Role.STUDENT) %>) {
                        outputData.unshift(["Team", teamName]); //Add to top of table
                    } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
                        outputData.unshift(["Team", teamDropDownSelect.outerHTML()]); //Add to top of table
                    }
                    if (bodyTd.is('.unavailableTimeslot')) {
                        var timeslots = scheduleData.timeslots;
                        var unavailableTimeslotData = timeslots[bodyTd.attr('value')];
                        outputData.push(["Unavailable", unavailableTimeslotData.unavailable]);
                        popoverTitle = "Unavailable Timeslot";
                        //TODO: Can create booking still? Add button here
                    } else {
                        var buttonCreate = $(document.createElement('button'));
                        buttonCreate.attr('id', 'createBookingBtn');
                        buttonCreate.addClass('btn btn-primary');
                        var iconCreate = $(document.createElement('i'));
                        iconCreate.addClass('icon-plus-sign icon-white');
                        buttonCreate.append(iconCreate);
                        buttonCreate.append("Book");
                        outputData.push(["", buttonCreate]);
                        popoverTitle = "Available Timeslot";
                    }
                    //Print data
                    for (var i = 0; i < outputData.length; i++) {
                        var outputTr = $(document.createElement('tr'));
                        var outputTdKey = $(document.createElement('td')).html(outputData[i][0]);
                        if (outputData[i][1] instanceof Array) {
                            var outputList = "";
                            for (var j = 0; j < outputData[i][1].length; j++) {
                                outputList += outputData[i][1][j] + "<br/>";
                            }
                            outputData[i][1] = outputList;
                        }
                        var outputTdValue = $(document.createElement('td')).html(outputData[i][1]);
                        outputTr.append(outputTdKey);
                        outputTr.append(outputTdValue);
                        outputTable.append(outputTr);
                    }
                    
                    bodyTd.popover({
                        container: bodyTd,
                        html: 'true',
                        trigger: 'manual',
                        placement: function() {
                            if (bodyTd.parent().children().index(bodyTd) > 9) {
                                return 'left';
                            } else {
                                return 'right';
                            }
                        },
                        content: outputTable,
                        title: function() {
                            var buttonClose = $(document.createElement('button'));
                            buttonClose.attr('type', 'button');
                            buttonClose.addClass('close');
                            buttonClose.append("&times;"); //X sign
                            return popoverTitle + buttonClose.outerHTML();
                        }
                    });
                }
                
                function appendChangeAvailabilityPopover(bodyTd) {
                    if (bodyTd.hasClass('legendBox')) return;
                    //Create Booking outputTable
                    var outputTable = $(document.createElement('table'));
                    outputTable.attr('id', 'createTimeslotTable');
                    var outputData = [];
                    if (bodyTd.is('.unavailableTimeslot')) {
                        var buttonAvail = $(document.createElement('button'));
                        buttonAvail.attr('id', 'availableTimeslotBtn');
                        buttonAvail.addClass('btn btn-primary');
                        var iconAvail = $(document.createElement('i'));
                        iconAvail.addClass('icon-plus-sign icon-white');
                        buttonAvail.append(iconAvail);
                        buttonAvail.append("Available");
                        outputData.push(["You Are", buttonAvail]);
                    } else {
                        var buttonUnavail = $(document.createElement('button'));
                        buttonUnavail.attr('id', 'unavailableTimeslotBtn');
                        buttonUnavail.addClass('btn btn-primary');
                        var iconUnavail = $(document.createElement('i'));
                        iconUnavail.addClass('icon-minus-sign icon-white');
                        buttonUnavail.append(iconUnavail);
                        buttonUnavail.append("Unavailable");
                        outputData.push(["You Are", buttonUnavail]);
                    }
                    //Print data
                    for (var i = 0; i < outputData.length; i++) {
                        var outputTr = $(document.createElement('tr'));
                        var outputTdKey = $(document.createElement('td')).html(outputData[i][0]);
                        var outputTdValue = $(document.createElement('td')).html(outputData[i][1]);
                        outputTr.append(outputTdKey);
                        outputTr.append(outputTdValue);
                        outputTable.append(outputTr);
                    }

                    bodyTd.popover({
                        container: bodyTd,
                        html: 'true',
                        trigger: 'manual',
                        placement: function() {
                            if (bodyTd.parent().children().index(bodyTd) > 9) {
                                return 'left';
                            } else {
                                return 'right';
                            }
                        },
                        content: outputTable,
                        title: function() {
                            var buttonClose = $(document.createElement('button'));
                            buttonClose.attr('type', 'button');
                            buttonClose.addClass('close');
                            buttonClose.append("&times;"); //X sign
                            return "Change availability" + buttonClose.outerHTML();
                        }
                    });
                }
                
                //Function to refresh booking exists
                function refreshScheduleData() {
                    var toReturn = null;
                    var bookingExists = 0;
                    var existingTimeslot = null;
                    var teamsPendingBooking = null;
                    if (<%= activeRole.equals(Role.STUDENT) %>) {
                        for (var key in scheduleData.timeslots) {
                            if (scheduleData.timeslots.hasOwnProperty(key)) {
                                var timeslot = scheduleData.timeslots[key];
                                if (timeslot.team && timeslot.team === teamName) {
                                    bookingExists = 1;
                                    existingTimeslot = $(document.createElement('div'));
                                    existingTimeslot.attr('value', timeslot.datetime);
                                    existingTimeslot.addClass('existingTimeslot');
                                    break;
                                }
                            }
                        }
//                        console.log("Does booking exist? " + milestoneStr + " " + teamName + " " + bookingExists);
                        toReturn = {bookingExists:bookingExists, existingTimeslot: existingTimeslot};
                    } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
                        teamsPendingBooking = new Array();
                        teamDropDownSelect = $(document.createElement('select'));
                        teamDropDownSelect.attr('name', 'team');
                        teamDropDownSelect.attr('id', 'createTeamSelect');
                        outerTeams: 
                        for (var t = 0; t < teams.length; t++) {
                            //Append only teams without bookings
                            var adminTeamName = teams[t].teamName;
                            for (var key in scheduleData.timeslots) {
                                if (scheduleData.timeslots.hasOwnProperty(key)) {
                                    var timeslot = scheduleData.timeslots[key];
                                    if (timeslot.team && timeslot.team === adminTeamName) {
                                        continue outerTeams;
                                    }
                                }
                            }
                            var teamDropDownOption = $(document.createElement('option'));
                            teamDropDownOption.attr('value', teams[t].teamId);
                            teamDropDownOption.html(adminTeamName);
                            teamDropDownSelect.append(teamDropDownOption);
                            teamsPendingBooking.push(adminTeamName);
                        }
                        toReturn = {teamsPendingBooking:teamsPendingBooking};
                    }
                    return toReturn;
                }

                //Function to create mouse UI events
                function setupMouseEvents() {
                    
                    /*****************************
                     CALENDAR UI INTERACTION
                     ****************************/

                    //Removed clicked
                    $(".timeslotCell").mouseleave(function() {
                        $(this).removeClass("clickedCell");
                    });

                    //Popover for booked timeslot
                    $('body').off('click', '.bookedTimeslot');
                    $('body').on('click', '.bookedTimeslot, .bookedTimeslot > .booking', function(e) {
                        self = (!$(this).is('.booking')) ? $(this).children('.booking') : $(this);
                        if ($(e.target).parents('.popover').length) return false;
                        console.log(".booking clicked: " + $(e.target).attr('class'));
                        $('.booking').not(self).popover('hide');
                        $('.timeslotCell').popover('hide');
                        self.popover('show');
                        appendTokenInput(self); //Optional attendees
                        return false;
                    });
                    
                    //Popover for unbooked or unavailable timeslot
                    $('body').off('click', '.unbookedTimeslot, .unbookedTimeslot > .booking, .unavailableTimeslot, .unavailableTimeslot > .booking');
                    $('body').on('click', '.unbookedTimeslot, .unbookedTimeslot > .booking, .unavailableTimeslot, .unavailableTimeslot > .booking', function(e) {
                        if (e.target === this) {
                            self = $(this).is('div') ? $(this).parent('.timeslotCell') : $(this);
                            $('.timeslotCell').not(self).popover('hide');
                            var refreshData = refreshScheduleData();
                            if (<%= activeRole.equals(Role.STUDENT) %>) {
                                if (refreshData.bookingExists !== 0) {
                                    showNotification("WARNING", refreshData.existingTimeslot, "You already have a booking!");
                                    return false;
                                }
                                var teamTerm = "<% try {out.print(!team.getTerm().equals(activeTerm)?team.getTerm().getAcademicYear() + " " + team.getTerm().getSemester():"thisTerm");} catch (Exception e) {out.print("thisTerm");} %>";
                                if (teamTerm !== "thisTerm") {
                                    showNotification("WARNING", self, "You can only book for Term " + teamTerm);
                                    return false;
                                }
                            }
                            if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                                //Make a dropdown of all teams that have not booked yet if user is admin
                                if (teams.length === 0) {
                                    createBookingOutputForAdmin = "There are no teams for this term";
                                } else if (refreshData.teamsPendingBooking.length === 0) {
                                    createBookingOutputForAdmin = "All teams have bookings!";
                                }
                                if (createBookingOutputForAdmin) {
                                    showNotification("WARNING", self, createBookingOutputForAdmin);
                                    createBookingOutputForAdmin = null;
                                    return false;
                                }
                            }
                            console.log(".unbookedTimeslot clicked.");
                            self.popover('show');
                        }
                        return false;
                    });
                    
                    /*****************************
                     POPOVER INTERACTION
                     ****************************/

                    //Close Booking Button
                    $(".timeslotCell").off('click', '.close');
                    $(".timeslotCell").on('click', '.close', function(e) {
                        e.stopPropagation();
                        self.popover('hide');
                        self.trigger('mouseleave');
                        return false;
                    });

                    //Create Booking Button
                    $(".timeslotCell").off('click', '#createBookingBtn');
                    $(".timeslotCell").on('click', '#createBookingBtn', function(e) {
                        //NOTE: self is a .timeslotCell here
                        var returnData = createBooking(self);
                        //REFRESH STATE OF scheduleData
                        self.popover('destroy');
                        self.removeClass('unbookedTimeslot');
                        self.addClass('bookedTimeslot');
                        var bookingDiv = $(document.createElement('div'));
                        bookingDiv.addClass('booking pendingBooking');
                        bookingDiv.html(returnData.booking.team);
                        bookingDiv.css('display', 'none');
                        self.append(bookingDiv);
                        showNotification("SUCCESS", self, null);
                        scheduleData.timeslots[self.attr('value')] = returnData.booking;
                        refreshScheduleData();
                        if (<%= activeRole.equals(Role.STUDENT)%>) {
                            appendViewBookingPopover(self);
                        } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            appendPopovers();
                        }
                        bookingDiv.show('clip', 'slow');
                        return false;
                    });

                    //Delete Booking Button
                    $(".timeslotCell").off('click', '#deleteBookingBtn');
                    $(".timeslotCell").on('click', '#deleteBookingBtn', function(e) {
                        e.stopPropagation();
                        var timeslot = self.parents('.timeslotCell');
                        deleteBooking(timeslot);
                        showNotification("ERROR", timeslot, null);
                        //REFRESH STATE OF scheduleData
                        self.effect('clip', 'slow');
                        timeslot.removeClass();
                        timeslot.addClass("timeslotCell unbookedTimeslot");
                        delete scheduleData.timeslots[timeslot.attr('value')];
                        scheduleData.timeslots[timeslot.attr('value')] = {id:timeslot.attr('id').split(" ")[1], venue:"SIS Seminar Room 2-1", datetime: timeslot.attr('value')}; //TODO: Change SIS Seminar Room 2-1
                        refreshScheduleData();
                        if (<%= activeRole.equals(Role.STUDENT)%>) {
                            appendCreateBookingPopover(timeslot);
                        } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            appendPopovers();
                        }
                        return false;
                    });
                    
                    //Datepicker
                    $('body').off('click', '#updateFormDate');
                    $('body').on('click', '#updateFormDate', function(e){
                        //Add date and timepickers
                        if (e.target === this) {
                            console.log("clicked " + $(this).parents(".bookedTimeslot").attr('value'));
                            $(this).datepicker('show');
                        }
                        return false;
                    });
                    
                    //Timepicker
                    $('body').off('click', '#updateFormStartTime');
                    $('body').on('click', '#updateFormStartTime', function(e){
                        //Add timepicker
                        if (e.target === this) {
                            $(this).timepicker('show');
                        }
                        return false;
                    });
                    
                    //Token input
                    function appendTokenInput(booking){
                        var opts = {
                            preventDuplicates: true,
                            theme: "facebook",
                            allowNewTokens: true,
                            propertyToSearch: "name",
                            tokenFormatter: function(item) {
                                if (item && item.id) {
                                    return "<li><p>" + item.id + "</p></li>";
                                } else if (item) {
                                    return "<li><p>" + item.name + "</p></li>";
                                }
                            }
                        };
                        var viewBookingData = scheduleData.timeslots[booking.parents('.timeslotCell').attr('value')];
                        if (viewBookingData.optionals && viewBookingData.optionals.length > 0) {
                            opts["prePopulate"] = viewBookingData.optionals;
                        }
                        booking.find('.optionalAttendees').tokenInput(users, opts);
                    }
                    
                    $(".timeslotCell").on('mouseleave', "#updateFormDate", function(){
                        var inputDate = $(this).val();
                        if (inputDate) {
                            inputDate = inputDate.replace(/ /g, '-');
                            var date = Date.parse(inputDate);
                            if (!date) {
                                $(this).val("").change();
                                showNotification("WARNING", self, "Please enter a proper date");
                                return false;
                            }
                            var wellFormedDate = date.toString("yyyy-MM-dd");
                            $(this).val(wellFormedDate).change();
                        }
                        return false;
                    });
                    
                    $(".timeslotCell").on('mouseleave', "#updateFormStartTime", function(){
                        var inputTime = $(this).val();
                        if (inputTime) {
                            inputTime = inputTime.split(" - ")[0];
                            var time = Date.parse(inputTime);
                            if (!time) {
                                $(this).val("").change();
                                showNotification("WARNING", self, "Please enter a proper time");
                                return false;
                            }
                            var wellFormedTime = time.toString("HH:mm");
                            $(this).val(wellFormedTime).change();
                        }
                        return false;
                    });

                    //Update Booking Button
                    $(".timeslotCell").off('click', '#updateBookingBtn');
                    $(".timeslotCell").on('click', '#updateBookingBtn', function(e) {
                        e.stopPropagation();
                        var startDateVal = $('#updateFormDate').val();
                        var startTimeVal = $('#updateFormStartTime').val();
                        var newDateTime = "";
                        var attendees = $(".optionalAttendees").tokenInput('get');
                        if (startDateVal && startTimeVal) {
                            newDateTime = Date.parse(startDateVal).toString("yyyy-MM-dd") + " " + Date.parse($.trim(startTimeVal.split(" - ")[0])).toString("HH:mm:ss");
                        }
                        var timeslot = self.parents('.timeslotCell');
                        var returnData = updateBooking(timeslot, newDateTime, attendees);
                        if (returnData && returnData.success) {
                            showNotification("INFO", timeslot, null);
                            if (newDateTime) {
                                var newTimeslot = $("#timeslot_" + returnData.booking.id);
                                newTimeslot.popover('destroy');
                                newTimeslot.removeClass();
                                newTimeslot.addClass('timeslotCell bookedTimeslot');
                                var bookingDiv = $(document.createElement('div'));
                                bookingDiv.addClass('booking pendingBooking');
                                bookingDiv.html(returnData.booking.team);
                                bookingDiv.css('display', 'none');
                                newTimeslot.append(bookingDiv);
                                scheduleData.timeslots[newTimeslot.attr('value')] = returnData.booking;
                                appendViewBookingPopover(newTimeslot);
                                
                                timeslot.removeClass('bookedTimeslot');
                                timeslot.addClass('unbookedTimeslot');
                                timeslot.popover('destroy');
                                timeslot.html("");
                                delete scheduleData.timeslots[timeslot.attr('value')];
                                scheduleData.timeslots[timeslot.attr('value')] = {id:timeslot.attr('id').split("_")[1], venue:"SIS Seminar Room 2-1", datetime: timeslot.attr('value')}; //TODO: Change SIS Seminar Room 2-1
                                appendCreateBookingPopover(timeslot);
                                
                                bookingDiv.show('clip', 'slow');

//                                scheduleData.timeslots[newDateTime] = returnData.booking;
//                                var newId = returnData.booking.id; //Add new booking
//                                var newTimeslot = $("#timeslot_" + newId);
//                                console.log("ID: " + newTimeslot.attr('id'));
//                                newTimeslot.removeClass();
//                                newTimeslot.addClass('timeslotCell bookedTimeslot');
//                                var bookingDiv = $(document.createElement('div'));
//                                bookingDiv.addClass(self.attr('class'));
//                                bookingDiv.html(self.html());
//                                newTimeslot.append(bookingDiv);
//                                self.effect('clip', 'slow'); //Delete old booking
//                                appendViewBookingPopover(newTimeslot);
//                                newTimeslot.children('.booking').show('clip', 'slow'); //Show new booking
                            } else {
                                scheduleData.timeslots[timeslot.attr('value')] = returnData.booking;
                                appendViewBookingPopover(timeslot);
                            }
                        } else {
                            showNotification("WARNING", timeslot, returnData.message);
                        }
                        return false;
                    });
                    
                    //Set Available
                    $(".timeslotCell").off('click', '#availableTimeslotBtn');
                    $(".timeslotCell").on('click', '#availableTimeslotBtn', function(e) {
                        e.stopPropagation();
                        changeAvailability(self, true);
                        self.addClass('existingTimeslot');
                        showNotification("WARNING", self, "Set as available");
                        self.popover('destroy');
//                        console.log("New classes should be :" + self.attr('class'));
                        appendChangeAvailabilityPopover(self);
                        return false;
                    });
                    
                    //Set Unavailable
                    $(".timeslotCell").off('click', '#unavailableTimeslotBtn');
                    $(".timeslotCell").on('click', '#unavailableTimeslotBtn', function(e) {
                        e.stopPropagation();
                        changeAvailability(self, false);
                        self.addClass('existingTimeslot');
                        showNotification("WARNING", self, "Set as unavailable");
                        self.popover('destroy');
//                        console.log("New classes should be :" + self.attr('class'));
                        appendChangeAvailabilityPopover(self);
                        return false;
                    });
                }
                
                /***************************
                    AJAX CALL FUNCTIONS
                 ***************************/
                 
                function createBooking(bodyTd) {
                    var toReturn = null;
                    var data = null;
                    var tName = null;
                    if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            tName = $("#createTeamSelect option:selected").text();
                            var tId = $("#createTeamSelect").val();
                            data = {
                                timeslotId: bodyTd.attr('id').split("_")[1],
                                teamId: tId
                            };
                    } else if (<%= activeRole.equals(Role.STUDENT)%>) {
                        tName = teamName;
                        data = {
                            timeslotId: bodyTd.attr('id').split("_")[1],
                        };
                    }
//                    console.log("Submitting create booking data: " + JSON.stringify(data));
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
                            toReturn = response;
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        alert("Oops. There was an error: " + JSON.stringify(error));
                    });
                    return toReturn;
                }

                function deleteBooking(bodyTd) {
                    //get the timeslotID for that cell and send as request
                    var cellId = $(bodyTd).attr('id').split("_")[1];
                    var data = {timeslotId: cellId};
//                    console.log("Submitting delete booking data: " + JSON.stringify(data));
                    //Delete Booking AJAX
                    $.ajax({
                        type: 'POST',
                        async: false,
                        url: 'deleteBookingJson',
                        data: data,
                        cache: false,
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.exception) {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        alert("Oops. There was an error: " + error);
                    });
                }

                //update booking function
                function updateBooking(bodyTd, newDateTime, attendees) {
                    //getfunction up the timeslotID for that cell and send as request
                    var toReturn = null;
                    var cellId = bodyTd.attr('id').split("_")[1];
                    var attendeesArray = new Array();
                    for (var i = 0; i < attendees.length; i++) {
                        attendeesArray.push(attendees[i].id?attendees[i].id:attendees[i].name);
                    }
                    var data = {timeslotId: cellId, newDateTime: newDateTime, attendees: attendeesArray};
                    console.log("Submitting update booking data: " + JSON.stringify(data));
                    //Update booking AJAX
                    $.ajax({
                        type: 'POST',
                        async: false,
                        url: 'updateBookingJson',
                        data: {jsonData: JSON.stringify(data)},
                        cache: false,
                        dataType: 'json'
                    }).done(function(response) {
                        toReturn = response;
                        if (response.exception) {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        alert("Oops. There was an error: " + error);
                    });
                    return toReturn;
                }
                
                //Update Timeslots AJAX Call            
                function changeAvailability(bodyTd, available) {
                    var timeslotsData = {};
                    var timeslot_data = new Array();
                    var allTimeslots = $(".unavailableTimeslot", ".scheduleTable").get();
                    var timeslotsSet = new HashSet();
                    for (var i = 0; i < allTimeslots.length; i++) {
                        var obj = allTimeslots[i];
                        timeslotsSet.add(obj.id);
                    }
                    timeslot_data = timeslotsSet.values().sort();
                    if (!available) {
                        timeslot_data.push(bodyTd.attr('id'));
                    } else {
                        var index = timeslot_data.indexOf(bodyTd.attr('id'));
                        timeslot_data.splice(index, 1);
                    }
                    timeslotsData["timeslot_data[]"] = timeslot_data;
                    timeslotsData["scheduleId"] = scheduleData.id;
//                    console.log('Submitting availability data: ' + JSON.stringify(timeslotsData));
                    $.ajax({
                        type: 'POST',
                        async: false,
                        url: 'setAvailabilityJson',
                        data: timeslotsData,
                        dataType: 'json'
                    }).done(function(response) {
                        if (!response.exception) {
                            if (response.success) {
                                if (!available) {
                                    bodyTd.removeClass();
                                    bodyTd.addClass("timeslotCell unavailableTimeslot");
                                } else {
                                    bodyTd.removeClass();
                                    bodyTd.addClass("timeslotCell unbookedTimeslot");
                                }
                            } else {
                                var eid = btoa(response.message);
//                                console.log(response.message);
                                window.location = "error.jsp?eid=" + eid;
                            }
                        } else {
                            var eid = btoa(response.message);
                            window.location="error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        var eid = btoa("Something went wrong");
                        window.location="error.jsp?eid=" + eid;
                    });
                    return false;
                }

                //Function to make schedule based on GetScheduleAction response
                function makeSchedule() {
                    var tableClass = "scheduleTable";
                    var timeslots = scheduleData.timeslots;
                    
                    //TODO: Get from server/admin console/whatevs
                    var minTime = scheduleData.dayStartTime;
                    var maxTime = scheduleData.dayEndTime;

                    var timesArray = new Array();
                    for (var i = minTime; i < maxTime; i++) {
                        var timeVal = Date.parse(i + ":00:00");
                        timesArray.push(timeVal.toString("HH:mm:ss"));
                        timeVal.addMinutes(30);
                        timesArray.push(timeVal.toString("HH:mm:ss"));
                    }
                    
                    var datesArray = getDateArrayBetween(Date.parse(scheduleData.startDate), Date.parse(scheduleData.endDate)); //Gets whole range of dates
//                    var datesArray = datesHashArray; //Gets only specific dates

                    //Get dates between minDate and maxDate
                    function getDateArrayBetween(startDate, stopDate) {
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
                    var headerTd = $(document.createElement('td'));
                    headerTr.attr('id', 'scheduleHeader');
                    headerTr.append(headerTd);
                    for (i = 0; i < datesArray.length; i++) {
                        var headerVal = new Date(datesArray[i]).toString('dd MMM') + "<br/>" + new Date(datesArray[i]).toString('ddd');
                        headerTd = $(document.createElement('td'));
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
                        bodyTd.html(time.substring(0, 5));
                        bodyTd.addClass('timeDisplayCell');
                        bodyTr.append(bodyTd);

                        //Append timeslot td's row by row
                        rowloop:
                        for (var j = 0; j < datesArray.length; j++) {
                            var date = datesArray[j];
                            date = new Date(date).toString("yyyy-MM-dd");
                            var datetimeString = date + " " + time;

                            //Checking if table cell is part of a timeslot
                            for (var k = 0; k < rowspanArr.length; k++) {
                                if (datetimeString === rowspanArr[k]) {
                                    continue rowloop;
                                }
                            }

                            //Table cell not part of timeslot yet. Proceed.
                            var timeslot = timeslots[datetimeString]; //Get the timeslot
                            bodyTd = $(document.createElement('td'));
                            bodyTd.addClass('timeslotCell');
                            
                            //If timeslot is available
                            //TODO: Remove hardcoding of milestone data
                            if (timeslot) {
                                var id = timeslot.id;
                                var team = timeslot.team;
                                var isAvailable = timeslot.available;
                                bodyTd.attr('id', 'timeslot_' + id);
                                bodyTd.attr('value', datetimeString);

                                if (team) {
                                    var bookingDiv = $(document.createElement('div'));
                                    bookingDiv.html(team);
                                    bookingDiv.addClass('booking pendingBooking');
                                    bodyTd.append(bookingDiv);
                                    bodyTd.addClass('bookedTimeslot');
                                } else {
                                    if (<%= activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY)%> && !isAvailable) {
                                        bodyTd.addClass('unavailableTimeslot');
                                    } else {
                                        bodyTd.addClass('unbookedTimeslot');
                                    }
                                }
                                var temp = null;
                                bodyTd.attr('align', 'center');
                                var duration = scheduleData.duration;
                                for (var t = 30; t < duration; t++) {
                                    temp = Date.parse(datetimeString).addMinutes(t).toString("yyyy-MM-dd HH:mm:ss");
                                    rowspanArr.push(temp);
                                }
                                bodyTd.attr('rowspan', (scheduleData.duration/30));
                            } else {
                                bodyTd.addClass('noTimeslot');
                            }

                            //Append to row
                            bodyTr.append(bodyTd);
                        }
                        $("." + tableClass).append(bodyTr);
                    }
                }
                
                //Function to get timeslot object from timestamp
                function getTimeslot(timeslots, date, time) {
                    var datetimeString = (date + " " + (time.length > 5?time:time + ":00")).trim();
                    for (var i = 0; i < timeslots.length; i++) {
                        if (timeslots[i].datetime === datetimeString) {
                            return timeslots[i];
                        }
                    }
                    return 0;
                }
                
                /*****************************
                 NOTIFICATIONS
                 ****************************/

                function showNotification(action, bodyTd, notificationMessage) {
                    var opts = {
                        title: "Note",
                        text: notificationMessage,
                        type: "warning",
                        icon: false,
                        sticker: false,
                        mouse_reset: false,
                        animation: "fade",
                        animate_speed: "fast",
                        before_open: function(pnotify) {
                            pnotify.css({
                               top: "52px",
                               left: ($(window).width() / 2) - (pnotify.width() / 2)
                            });
                        }
                    };
                    var dateToView = Date.parse(bodyTd.attr('value')).toString("dd MMM");
                    var startTimeToView = Date.parse(bodyTd.attr('value')).toString("HH:mm");
                    switch (action) {
                        case "SUCCESS":
                            opts.title = "Booked";
                            opts.text = "Time: " + dateToView + " " + startTimeToView + "<br/> Emails have been sent";
                            opts.type = "success";
                            break;
                        case "ERROR":
                            opts.title = "Deleted";
                            opts.text = "Time: " + dateToView + " " + startTimeToView + "<br/> Emails have been sent";
                            opts.type = "error";
                            break;
                        case "INFO":
                            opts.title = "Updated";
                            opts.text = "Time: " + dateToView + " " + startTimeToView + "<br/> Emails have been sent";
                            opts.type = "info";
                            break;
                        case "WARNING":
                            $.pnotify_remove_all();
                            opts.title = "Note";
                            opts.type = "warning";
                            if (bodyTd.hasClass('existingTimeslot')){
                                opts.text += "<br/> Time: " + dateToView + " " + startTimeToView;
                                var id = scheduleData.timeslots[bodyTd.attr('value')].id;
                                $("#timeslot_" + id).children('.booking').effect('bounce', {distance: 50}, 'slow');
                                bodyTd.removeClass('existingTimeslot');
                            }
                            break;
                        default:
                            alert("Something went wrong");
                    }
                    $.pnotify(opts);
                }

            };

            addLoadEvent(viewScheduleLoad);
        </script>
        <br/>
        <%--<%@include file="navbar_footer.jsp" %>--%>
    </body>
</html>
