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
                <div class="pendingBookings alert" style="width: 230px; text-align: center">
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
                    <td class="pendingBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Pending</td> 
                    <td style="width:15px"></td>
                    <td class="approvedBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Approved</td> 
                    <td style="width:15px"></td>
                    <td class="timeslotCell unavailableTimeslot" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Not Available</td> 
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

                //Declare common variables
                //Default milestoneStr is ACCEPTANCE
                var milestoneStr = "<%= session.getAttribute("lastSelectedMilestone") == null ? "ACCEPTANCE" : (String) session.getAttribute("lastSelectedMilestone")%>";
                var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                var activeSemesterStr = "<%= activeTerm.getSemester()%>";
                var self = null;
                //Logged in team name
                var teamName = "<%= team != null ? team.getTeamName() : null%>";
                var bookingExists = $("#" + milestoneStr.toLowerCase() + "ScheduleTable").find(":contains(" + teamName + ")").length;
                var teamId = null;
                var date = null;
                var startTime = null;
                var termId = null;

                //All teams data if for admins
                var teams = new Array();
                var teamsPendingBooking = new Array();

                //Make schedule rows and columns
                var datesArray = new Array();
                var timesArray = new Array();

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

                //View Schedule stuff
                //Function to populate schedule data based on ACTIVE TERM
                function populateSchedule(milestone, year, semester) {
                    //Hide schedule tab and show progress bar
                    $("#milestoneTabContent").hide();
                    $("#scheduleProgressBar").show();
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

                            //Get Teams data if user is administrator
                            if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                                teams = JSON.parse('<%= session.getAttribute("allTeams")%>');
                            }

                            //Draw the schedule table
                            makeSchedule(response);

                            appendPopovers(response);

                            //Setup mouse events
                            setupMouseEvents();

                            //Unhide progressbar and show schedule
                            $("#scheduleProgressBar").hide();
                            $("#milestoneTabContent").show();
                        } else {
                            //Unhide progressbar and show schedule
                            $("#scheduleProgressBar").hide();
                            $("#milestoneTabContent").show();
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        //Unhide progressbar and show schedule
                        $("#scheduleProgressBar").hide();
                        $("#milestoneTabContent").show();
                        alert("There was an error in retrieving schedule");
                    });
                }

                //Append popover data
                function appendPopovers(scheduleData) {

                    var timeslots = scheduleData.timeslots;

                    //Delete all old popovers
                    $(".timeslotCell").trigger('mouseleave');
                    $(".timeslotCell").popover('destroy');

                    //Add View Booking popovers
                    $(".bookedTimeslot").each(function() {
                        appendViewBookingPopover($(this));
                    });

                    function appendViewBookingPopover(bodyTd) {
                        //Get View Booking Data
                        var cellId = bodyTd.attr('id').split("_")[1];
                        var viewBookingData = null;

                        for (var i = 0; i < timeslots.length; i++) {
                            var timeslot = timeslots[i];
                            if (timeslot.team && parseInt(timeslot.id) === parseInt(cellId)) {
                                console.log("Found!");
                                viewBookingData = timeslot;
                                break;
                            }
                        }

                        //Append bookingDiv classes based on status
                        var bookingDiv = bodyTd.children('div');
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
                                    outputData.push(["", "<button id='deleteBookingBtn' class='popoverBtn btn btn-danger'><i class='icon-trash icon-white'></i>Delete</button>"]);
                                }

                                //Make fields editable and add Update and Delete buttons if user is admin
                                if (<%=activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                                    //Replace startDate and startTime with editable fields
                                    for (var i = 0; i < outputData.length; i++) {
                                        if (outputData[i][0] === "Date") {
                                            outputData[i][1] = "<input type='text' id='updateFormDate'" + " placeholder='" + outputData[i][1] + "' title='Enter date in YYYY-MM-DD format (e.g. 2013-01-10)' /input>";
                                        }
                                        if (outputData[i][0] === "Time") {
                                            outputData[i][1] = "<input type='text' id='updateFormStartTime'" + " placeholder='" + outputData[i][1] + "' title='Enter only start time in HH:mm format (e.g. 10:00)' /input>";
                                        }
                                    }
                                    //Add Update AND Delete button
                                    outputData.push(["", "<button id='deleteBookingBtn' class='popoverBtn btn btn-danger'><i class='icon-trash icon-white'></i>Delete</button>"
                                                + "<button id='updateBookingBtn' class='popoverBtn btn btn-info'><i class='icon-edit icon-white'></i>Save</button>"]);
                                }

                                //Append all fields
                                for (var i = 0; i < outputData.length; i++) {
                                    var outputTr = $(document.createElement('tr'));
                                    var outputTdKey = $(document.createElement('td')).html('<b>' + outputData[i][0] + '</b>');
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
                                if (viewBookingData.teamName === teamName) {
                                    return "<b>Your Booking <b><button type='button' class='close'>&times;</button>";
                                } else {
                                    return "<b>Team Booking <b><button type='button' class='close'>&times;</button>";
                                }
                            }
                        });
                    }

                    //Add Create Booking Popovers
                    var teamDropDownSelect = null;
                    var createBookingOutput = null;
                    if (bookingExists) {
                        //Add warning popover for all timeslots
                        $(".unbookedTimeslot, .unavailableTimeslot").each(function() {
                            var bodyTd = $(this);
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
                                title: "Warning <button type='button' class='close'>&times;</button>",
                                content: 'You already have a booking!'
                            });
                        });
                    } else {
                        //Make a dropdown of all teams that have not booked yet if user is admin
                        if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            if (teams.length === 0) {
                                createBookingOutput = "There are no teams for this term";
                            } else {
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
                                if (teamsPendingBooking.length === 0) {
                                    createBookingOutput = "All teams have bookings!";
                                }
                            }
                            if (createBookingOutput !== null) {
                                //Add warning popover for all timeslots
                                $(".unbookedTimeslot, .unavailableTimeslot").each(function() {
                                    var bodyTd = $(this);
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
                                        title: "Warning <button type='button' class='close'>&times;</button>",
                                        content: createBookingOutput
                                    });
                                });
                                console.log("Not adding create bookings!");
                                return; //Don't add any more popovers
                            }
                        }

                        //Add Create Booking popover for all timeslots if is student, admin, or course coordinator
                        if (<%= activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            $(".unbookedTimeslot, .unavailableTimeslot").each(function() {
                                appendCreateBookingPopover($(this));
                            });
                        }
                    }

                    function appendCreateBookingPopover(bodyTd) {
                        //Initialize variables
                        if (!bodyTd.attr('id')) return;
                        var popoverTitle = null;
                        termId = activeAcademicYearStr + "," + activeSemesterStr;
                        var dateToView = Date.parse(bodyTd.attr('value')).toString("dd MMM");
                        var startTimeToView = Date.parse(bodyTd.attr('value')).toString("HH:mm");
                        var endTimeToView = new Date(Date.parse(bodyTd.attr('value'))).addHours(1).toString('HH:mm');

                        //Create Booking outputTable
                        var outputTable = $(document.createElement('table'));
                        outputTable.attr('id', 'createTimeslotTable');
                        var outputData = [
                            ["Team", teamDropDownSelect !== null ? teamDropDownSelect.outerHTML() : teamName],
                            ["Date", dateToView],
                            ["Time", startTimeToView + " - " + endTimeToView],
                            ["Milestone", milestoneStr]
                        ];
                        if (bodyTd.is('.unavailableTimeslot')) {
                            var cellId = bodyTd.attr('id').split("_")[1];
                            var unavailableTimeslotData = null;
                            for (var i = 0; i < timeslots.length; i++) {
                                var timeslot = timeslots[i];
                                if (!(timeslot.available) && parseInt(timeslot.id) === parseInt(cellId)) {
                                    unavailableTimeslotData = timeslot;
                                    break;
                                }
                            }
                            outputData.push(["Unavailable", unavailableTimeslotData.unavailable]);
//                            outputData.push(["", "<button id='createAnywayBookingBtn' class='btn btn-warning'><i class='icon-plus-sign icon-white'></i>Create Anyway</button>"]);
                            popoverTitle = "Unavailable Timeslot <button type='button' class='close'>&times;</button>";
                        } else {
                            outputData.push(["", "<button id='createBookingBtn' class='btn btn-primary'><i class='icon-plus-sign icon-white'></i>Create</button>"]);
                            popoverTitle = "Create Booking <button type='button' class='close'>&times;</button>";
                        }
                        //Print data
                        for (var i = 0; i < outputData.length; i++) {
                            var outputTr = $(document.createElement('tr'));
                            var outputTdKey = $(document.createElement('td')).html('<b>' + outputData[i][0] + '</b>');
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
                            title: popoverTitle
                        });
                    }
                };

                //Function to create mouse UI events
                function setupMouseEvents() {

                    //Hide all popovers on page click
                    $("body").on('click', function() {
                        if ($('.popover').hasClass("in")) {
                            $('.popover').parent().popover('hide');
                        }
                    });

                    //Removed clicked
                    $(".timeslotCell").mouseleave(function() {
                        $(this).removeClass("clickedCell");
                    });

                    //Add clickedCell and initialize common variables
                    $(".timeslotCell, .timeslotCell > div").on('click', function(e) {
                        if (e.target === this) {
                            self = $(this).is('div') ? $(this).parent() : $(this);
                            console.log("clicked");
                            //Hide other popovers
                            $(".timeslotCell").each(function() {
                                if (self.attr('value') !== $(this).attr('value')) {
                                    $(this).popover('hide');
                                }
                            });
                            //Add clickedCell class
                            $('td').removeClass("clickedCell");
                            self.addClass("clickedCell");
                        }
                        return false;
                    });

                    //Popover for booked timeslot
                    $(".bookedTimeslot, .bookedTimeslot > div").on('click', function(e) {
                        if (e.target === this) {
                            console.log(".bookedTimeslot clicked");
                            self = $(this).is('div') ? $(this).parent() : $(this);
                            self.popover('show');
                        }
                        return false;
                    });

                    $(".unbookedTimeslot, .unbookedTimeslot > div").on('click', function(e) {
                        if (e.target === this) {
                            console.log(".unbookedTimeslot clicked");
                            self = $(this).is('div') ? $(this).parent() : $(this);
                            self.popover('show');
                            date = Date.parse(self.attr('value')).toString("yyyy-MM-dd");
                            startTime = Date.parse(self.attr('value')).toString("HH:mm:ss");
                            termId = activeAcademicYearStr + "," + activeSemesterStr;
                        }
                        return false;
                    });
                    
                    $(".unavailableTimeslot, .unavailableTimeslot > div").on('click', function(e) {
                        if (e.target === this) {
                            console.log(".unavailableTimeslot clicked");
                            self = $(this).is('div') ? $(this).parent() : $(this);
                            self.popover('show');
                            date = Date.parse(self.attr('value')).toString("yyyy-MM-dd");
                            startTime = Date.parse(self.attr('value')).toString("HH:mm:ss");
                            termId = activeAcademicYearStr + "," + activeSemesterStr;
                        }
                        return false;
                    });

                    //
                    //Dynamic button bindings
                    //Use this kind of event trigger for dynamic buttons
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
                        if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            teamName = $("#createTeamSelect option:selected").text();
                            teamId = $("#createTeamSelect").val();
                        }
                        createBooking(self);
                        showResult(self, "Booked <br/> Confirmation email sent");
                        setTimeout(function() {
                            window.location.reload();
                        }, 3000); //Refresh all popovers
                        return false;
                    });

                    //Delete Booking Button
                    $("td, td > div").on('click', '#deleteBookingBtn', function(e) {
                        e.stopPropagation();
                        deleteBooking(self);
                        showResult(self, "Deleted <br/> Confirmation email sent");
                        setTimeout(function() {
                            window.location.reload();
                        }, 3000); //Refresh all popovers
                        return false;
                    });

                    //Update Booking Button
                    $("td").on('click', '#updateBookingBtn', function(e) {
                        e.stopPropagation();
                        updateBooking(self);
                        showResult(self, "Updated <br/> Confirmation email sent");
                        window.location.reload();
                        return false;
                    });
                }

                function showResult(self, message) {
                    //Result popover
                    self.popover({
                        container: self,
                        trigger: "manual",
                        placement: function() {
                            if (self.parent().children().index(self) > 9) {
                                return 'left';
                            } else {
                                return 'right';
                            }
                        },
                        content: message,
                        html: true
                    });
                    console.log('Toggling D');
                    self.popover('show');
                }

                //AJAX CALL functions
                function createBooking(self) {
                    var data = {
                        timeslotId: self.attr('id').split("_")[1],
                        teamId: teamId
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

                            var bookingDiv = $(document.createElement('div'));
                            bookingDiv.addClass('pendingBooking');
                            bookingDiv.html(teamName);
                            self.removeClass();
                            self.addClass("timeslotCell bookedTimeslot");
                            self.html(bookingDiv);
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
                            console.log('Destroying C');
                            self.popover('destroy');

                            self.html("");
                            self.removeClass();
                            self.addClass("timeslotCell unbookedTimeslot");
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        alert("Oops. There was an error: " + error);
                    });
                }

                //update booking function
                function updateBooking(self) {
                    //getfunction up the timeslotID for that cell and send as request
                    var cellId = self.attr('id').split("_")[1];
                    console.log("data");

                    var startDate = document.getElementById('updateFormDate').value;
                    var startTime = document.getElementById('updateFormStartTime').value;
                    var concatStart = "";
                    if (startDate !== null && startTime !== null) {
                        concatStart = startDate + " " + startTime + ":00";
                    }
                    console.log(concatStart + " this is concat start");

                    var data = {timeslotId: cellId, changedDate: concatStart};

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
                            self.popover('destroy');
                            var msg = response.message + "";
                            console.log(msg);

                            if (msg === ('Booking updated successfully! Update email has been sent to all attendees. (Coming soon..)')) {
                                self.html("");
                                self.removeClass("");
                                self.addClass("timeslotCell unbookedTimeslot");
                            } else {
                                //Popover to mention updating problem

                                self.popover({
                                    container: self,
                                    trigger: "manual",
                                    title: "Booking <button type='button' class='close'>&times;</button>",
                                    placement: function() {
                                        if (self.parent().children().index(self) > 9) {
                                            return 'left';
                                        } else {
                                            return 'right';
                                        }
                                    },
                                    content: "Error!<br/>" + response.message,
                                    html: true
                                });
                                self.popover('show');
                            }
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

                        timesArray = new Array();
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
                        datesArray = getDates(minDate, maxDate);

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
                            bodyTd.html('<b>' + time + '</b>');
                            bodyTd.addClass('timeDisplayCell');
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
                                var timeslot = getTimeslot(timeslots, date, time); //Get the timeslot
                                var id = timeslot.id;
                                var team = timeslot.team;
                                var isAvailable = timeslot.available;
                                bodyTd = $(document.createElement('td'));
                                bodyTd.addClass('timeslotCell');

                                //If timeslot is available
                                if (id) {
                                    var temp = null;
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
                                    bodyTd.attr('id', 'timeslot_' + id);
                                    bodyTd.attr('value', datetimeString);

                                    if (team) {
                                        var bookingDiv = $(document.createElement('div'));
                                        bookingDiv.html(team);
                                        bodyTd.html(bookingDiv);
                                        bodyTd.addClass('bookedTimeslot');
                                    } else {
                                        if (<%= activeRole.equals(Role.STUDENT) %> && !isAvailable) {
                                            bodyTd.addClass('unavailableTimeslot');
                                        } else {
                                            bodyTd.addClass('unbookedTimeslot');
                                        }
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
                        var datetimeString = (date + " " + time + ":00").trim();
                        for (var i = 0; i < timeslots.length; i++) {
                            if (timeslots[i].datetime === datetimeString) {
                                return timeslots[i];
                            }
                        }
                        return -1;
                    }
                }

            };

            addLoadEvent(viewScheduleLoad);
        </script>
        <br/>
        <%--<%@include file="navbar_footer.jsp" %>--%>
    </body>
</html>
