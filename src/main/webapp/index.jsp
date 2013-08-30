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
            <h3 id="activeTermName">
                <%
                    String semester = activeTerm.getSemester();
                    int startAcademicYear = activeTerm.getAcademicYear();
                    String endAcademicYear = String.valueOf(startAcademicYear + 1);
                    String academicYear = String.valueOf(startAcademicYear) + "-"
                            + endAcademicYear.substring(2);
                    out.print(academicYear + " " + semester);
                %>
            </h3>

            <!-- To display the list of active terms -->
            <div class="activeTerms">
                <table>
                    <tr>
                        <td style="width:90px; padding-bottom:11px"><b>Select Term</b></td>
                        <td><form id="activeTermForm" action="index" method="post">
                                <select name="termId" style="float:right" onchange="this.form.submit()"> 
                                    <option value=""><% out.print(academicYear + " " + semester);%></option>
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
                //Team Name and bookingExists
                var teamName = "<%= team != null ? team.getTeamName() : null%>";
                var teamId = null;
                
                //Admin specific variables - All teams data if for admins
                var teams = new Array();
                var teamDropDownSelect = null;
                var createBookingOutputForAdmin = null;
                
                //Booking specific variables
                var self = null;

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
//                    var count = 0;
//                    for (var key in scheduleData.timeslots) {
//                        if (scheduleData.timeslots.hasOwnProperty(key)) {
//                            ++count;
//                        }
//                    }
//                    console.log("New timeslots: " + count + ", Old timeslots: " + timeslots.length);
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
                    var viewBookingData = timeslots[bodyTd.attr('value')];

                    //Append bookingDiv classes based on status
                    var bookingDiv = bodyTd.children('div');
                    bookingDiv.addClass('booking');
                    if (viewBookingData.status === "APPROVED") {
                        bookingDiv.addClass('approvedBooking');
                    } else if (viewBookingData.status === "REJECTED") {
                        bookingDiv.addClass('rejectedBooking');
                    } else {
                        bookingDiv.addClass('pendingBooking');
                    }
                    bodyTd.html(bookingDiv);

                    //Popover
                    bodyTd.popover({
                        container: bodyTd, //This is important for the popover to overflow the schedule
                        trigger: 'manual',
                        html: 'true',
                        content: function() {
                            //Output in the form of a table
                            var outputTable = $(document.createElement('table'));
                            outputTable.attr('id', 'viewTimeslotTable');
                            var outputData = [
                                ["Team", viewBookingData.team],
                                ["Status", viewBookingData.status],
                                ["Date", viewBookingData.startDate],
                                ["Time", viewBookingData.time],
                                ["Team Wiki", viewBookingData.teamWiki],
                                ["Students", viewBookingData.students],
                                ["Faculty", viewBookingData.faculties]
                            ];
                            
                            //Add Delete button if user if part of team
                            if (viewBookingData.team === teamName) {
                                var buttonDelete = $(document.createElement('button'));
                                buttonDelete.attr('id', 'deleteBookingBtn');
                                buttonDelete.addClass('popoverBtn btn btn-danger');
                                var iconDelete = $(document.createElement('i'));
                                iconDelete.addClass('icon-trash icon-white');
                                buttonDelete.append(iconDelete);
                                buttonDelete.append("Delete");
                                outputData.push(["", buttonDelete]);
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
                                        input.attr('title', 'Enter date in YYYY-MM-DD format (e.g. 2013-08-01)');
                                        outputData[i][1] = input;
                                    }
                                    if (outputData[i][0] === "Time") {
                                        var input = $(document.createElement('input'));
                                        input.attr('id', 'updateFormStartTime');
                                        input.attr('type', 'text');
                                        input.attr('placeholder', outputData[i][1]);
                                        input.attr('title', 'Enter only start time in HH:mm format (e.g. 10:00)');
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
                            if (viewBookingData.teamName === teamName) {
                                return "Your Booking" + buttonClose.outerHTML();
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
                    var endTimeToView = new Date(Date.parse(bodyTd.attr('value'))).addHours(1).toString('HH:mm');

                    //Create Booking outputTable
                    var outputTable = $(document.createElement('table'));
                    outputTable.attr('id', 'createTimeslotTable');
                    var outputData = [
                        ["Date", dateToView],
                        ["Time", startTimeToView + " - " + endTimeToView],
                        ["Milestone", milestoneStr]
                    ];
                    if (<%= activeRole.equals(Role.STUDENT) %>) {
                        teamName = "<%= team != null ? team.getTeamName() : null%>";
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
                        iconUnavail.addClass('icon-plus-sign icon-white');
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
                    var bookingExists = null;
                    var teamsPendingBooking = null;
                    if (<%= activeRole.equals(Role.STUDENT) %>) {
                        bookingExists = $("#" + milestoneStr.toLowerCase() + "ScheduleTable").find(".booking :contains(" + teamName + ")").length;
                        console.log("Does booking exist? " + milestoneStr + " " + teamName + " " + bookingExists);
                        toReturn = {bookingExists:bookingExists};
                    } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
                        teams = JSON.parse('<%= session.getAttribute("allTeams")%>');
                        teamsPendingBooking = new Array();
                        teamDropDownSelect = $(document.createElement('select'));
                        teamDropDownSelect.attr('name', 'team');
                        teamDropDownSelect.attr('id', 'createTeamSelect');
                        for (var t = 0; t < teams.length; t++) {
                            //Append only teams without bookings
                            if (!$("#" + milestoneStr.toLowerCase() + "ScheduleTable").find(":contains(" + teams[t].teamName + ")").length) {
                                var teamDropDownOption = $(document.createElement('option'));
                                teamDropDownOption.attr('value', teams[t].teamId);
                                teamDropDownOption.html(teams[t].teamName);
                                teamDropDownSelect.append(teamDropDownOption);
                                teamsPendingBooking.push(teams[t].teamName);
                            }
                        }
                        toReturn = {teamsPendingBooking:teamsPendingBooking};
                    }
                    return toReturn;
                }

                //Function to create mouse UI events
                function setupMouseEvents() {
                    //Hide all popovers on page click
                    $("body").on('click', function() {
                        if ($('.popover').hasClass("in")) {
                            $('.popover').parent().popover('hide');
                        }
                    });
                    
                    /*****************************
                     CALENDAR UI INTERACTION
                     ****************************/

                    //Removed clicked
                    $(".timeslotCell").mouseleave(function() {
                        $(this).removeClass("clickedCell");
                    });

                    //Popover for booked timeslot
                    $('.bookedTimeslot').on('click', function(e) {
                        if ($(e.target).parents('.popover').length) return false;
                        console.log(".bookedTimeslot clicked: " + $(e.target).attr('class'));
                        self = $(this).is('.booking') ? $(this).parent('.timeslotCell') : $(this);
                        $('.timeslotCell').not(self).popover('hide');
                        self.popover('show');
                        return false;
                    });

                    $('.unbookedTimeslot, .unbookedTimeslot > .booking, .unavailableTimeslot, .unavailableTimeslot > .booking').on('click', function(e) {
                        if (e.target === this) {
                            self = $(this).is('div') ? $(this).parent('.timeslotCell') : $(this);
                            $('.timeslotCell').not(self).popover('hide');
                            var refreshData = refreshScheduleData();
                            if (<%= activeRole.equals(Role.STUDENT) %> && refreshData.bookingExists !== 0) {
                                showNotification("WARNING", self, "You already have a booking!");
                                return false;
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
                    $("td, td > div").on('click', '.close', function(e) {
                        e.stopPropagation();
                        self.popover('hide');
                        self.trigger('mouseleave');
                        return false;
                    });

                    //Create Booking Button
                    $("td, td > div").on('click', '#createBookingBtn', function(e) {
                        e.stopPropagation();
                        var returnData = createBooking(self);
                        //REFRESH STATE OF scheduleData
                        var bookingDiv = $(document.createElement('div'));
                        bookingDiv.addClass('booking pendingBooking');
                        bookingDiv.html(returnData.booking.team);
                        self.removeClass('unbookedTimeslot');
                        self.addClass('bookedTimeslot');
                        self.html(bookingDiv);
                        scheduleData.timeslots[self.attr('value')] = returnData.booking;
                        showNotification("CREATED", self, null);
                        if (<%= activeRole.equals(Role.STUDENT)%>) {
                            self.popover('destroy');
                            appendViewBookingPopover(self);
                        } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            appendPopovers();
                        }
                        return false;
                    });

                    //Delete Booking Button
                    $("td, td > div").on('click', '#deleteBookingBtn', function(e) {
                        e.stopPropagation();
                        deleteBooking(self);
                        showNotification("DELETED", self, null);
                        //REFRESH STATE OF scheduleData
                        self.popover('destroy');
                        self.html("");
                        self.removeClass();
                        self.addClass("timeslotCell unbookedTimeslot");
                        delete scheduleData.timeslots[self.attr('value')];
                        scheduleData.timeslots[self.attr('value')] = {id:self.attr('id').split(" ")[1], venue:"SIS Seminar Room 2-1", datetime: self.attr('value')};
                        if (<%= activeRole.equals(Role.STUDENT)%>) {
                            self.popover('destroy');
                            appendCreateBookingPopover(self);
                        } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            appendPopovers();
                        }
                        return false;
                    });

                    //Update Booking Button
                    $("td").on('click', '#updateBookingBtn', function(e) {
                        e.stopPropagation();
                        var startDate = document.getElementById('updateFormDate').value;
                        var startTime = document.getElementById('updateFormStartTime').value;
                        var newDateTime = "";
                        if (startDate !== null && startTime !== null) {
                            newDateTime = startDate + " " + startTime + ":00";
                        }
                        updateBooking(self, newDateTime);
                        showNotification("UPDATED", self, null);
                        var refreshData = refreshScheduleData();
                        var scheduleData = getScheduleData(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                        appendPopovers(scheduleData);
                        refreshScheduleData();
                        return false;
                    });
                    
                    //Set Available
                    $("td").on('click', '#availableTimeslotBtn', function(e) {
                        e.stopPropagation();
                        changeAvailability(self, true);
                        showNotification("WARNING", self, "Set as available");
                        self.popover('destroy');
                        console.log("New classes should be :" + self.attr('class'));
                        appendChangeAvailabilityPopover(self);
                        return false;
                    });
                    
                    //Set Unavailable
                    $("td").on('click', '#unavailableTimeslotBtn', function(e) {
                        e.stopPropagation();
                        changeAvailability(self, false);
                        showNotification("WARNING", self, "Set as unavailable");
                        self.popover('destroy');
                        console.log("New classes should be :" + self.attr('class'));
                        appendChangeAvailabilityPopover(self);
                        return false;
                    });
                }
                
                /*****************************
                 NOTIFICATIONS
                 ****************************/

                function showNotification(action, bodyTd, notificationMessage) {
                    var notificationTitle = null;
                    var notificationType = null;
                    var dateToView = Date.parse(bodyTd.attr('value')).toString("dd MMM");
                    var startTimeToView = Date.parse(bodyTd.attr('value')).toString("HH:mm");
                    switch (action) {
                        case "CREATED":
                            notificationTitle = "Booked";
                            notificationMessage = "Time: " + dateToView + " " + startTimeToView;
                            notificationMessage += "<br/> Emails have been sent";
                            notificationType = "success";
                            break;
                        case "DELETED":
                            notificationTitle = "Deleted";
                            notificationMessage = "Time: " + dateToView + " " + startTimeToView;
                            notificationMessage += "<br/> Emails have been sent";
                            notificationType = "error";
                            break;
                        case "UPDATED":
                            notificationTitle = "Updated";
                            notificationMessage = "Time: " + dateToView + " " + startTimeToView;
                            notificationMessage += "<br/> Emails have been sent";
                            notificationType = "warning";
                            break;
                        case "WARNING":
                            notificationTitle = "Note";
                            notificationType = "warning";
                            $.pnotify_remove_all();
                            break;
                        default:
                            alert("Something went wrong");
                    }
                    
                    $.pnotify({
                        title: notificationTitle,
                        text: notificationMessage,
                        type: notificationType,
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
                            teamId: teamId
                        };
                    }
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
                        if (response.exception) {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        alert("Oops. There was an error: " + error);
                    });
                }

                //update booking function
                function updateBooking(bodyTd, newDateTime) {
                    //getfunction up the timeslotID for that cell and send as request
                    var cellId = bodyTd.attr('id').split("_")[1];
                    var data = {timeslotId: cellId, changedDate: newDateTime};
                    console.log("Submitting update booking data: " + JSON.stringify(data));
                    $.ajax({
                        type: 'POST',
                        async: false,
                        url: 'updateBookingJson',
                        data: data,
                        cache: false,
                        dataType: 'json'
                    }).done(function(response) {
                        if (!response.exception) {
                            console.log('Destroying C');
                            bodyTd.popover('destroy');
                            var msg = response.message + "";
                            console.log(msg);
                            if (msg === ('Booking updated successfully! Update email has been sent to all attendees. (Coming soon..)')) {
                                bodyTd.html("");
                                bodyTd.removeClass();
                                bodyTd.addClass("timeslotCell unbookedTimeslot");
                            } else {
                                //Popover to mention updating problem
                                bodyTd.popover({
                                    container: bodyTd,
                                    trigger: "manual",
                                    title: "Booking <button type='button' class='close'>&times;</button>",
                                    placement: function() {
                                        if (bodyTd.parent().children().index(bodyTd) > 9) {
                                            return 'left';
                                        } else {
                                            return 'right';
                                        }
                                    },
                                    content: "Error!<br/>" + response.message,
                                    html: true
                                });
                                bodyTd.popover('show');
                            }
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        alert("Oops. There was an error: " + error);
                    });
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
                    console.log('Availability data is: ' + JSON.stringify(timeslotsData));
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
                                console.log(response.message);
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
                    var minTime = 9;
                    var maxTime = 19;

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
                    var headerTd = $(document.createElement('th'));
                    headerTr.attr('id', 'scheduleHeader');
                    headerTr.append(headerTd);
                    for (i = 0; i < datesArray.length; i++) {
                        var headerVal = new Date(datesArray[i]).toString('dd MMM') + "<br/>" + new Date(datesArray[i]).toString('ddd');
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
                                if (milestoneStr === "ACCEPTANCE") {
                                    bodyTd.attr('rowspan', '2'); //If acceptance, set 2
                                    temp = Date.parse(datetimeString).addMinutes(30).toString("yyyy-MM-dd HH:mm:ss");
                                    rowspanArr.push(temp);
                                } else {
                                    bodyTd.attr('rowspan', '3'); //else, set 3
                                    temp = Date.parse(datetimeString).addMinutes(30).toString("yyyy-MM-dd HH:mm:ss");
                                    rowspanArr.push(temp);
                                    temp = Date.parse(datetimeString).addHours(1).toString("yyyy-MM-dd HH:mm:ss");
                                    rowspanArr.push(temp);
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

            };

            addLoadEvent(viewScheduleLoad);
        </script>
        <br/>
        <%--<%@include file="navbar_footer.jsp" %>--%>
    </body>
</html>
