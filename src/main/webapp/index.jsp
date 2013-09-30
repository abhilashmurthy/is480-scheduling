<%@page import="manager.UserManager"%>
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
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System</title>
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <%
            Team team = null;
            String fullName = null;
            if (activeRole.equals(Role.STUDENT)) {
                Student studentUser = UserManager.getUser(user.getId(), Student.class);
                team = studentUser.getTeam();
                fullName = studentUser.getFullName();
            }
        %>

        <!-- Welcome Text -->
        <div class="container page">
            <h3 id="activeTermName"><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></h3>

            <!-- To display the list of active terms -->
            <div class="activeTerms">
                <table>
                    <tr>
                        <td style="padding-right:10px"><b>Select Term</b></td>
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
            
			
            <div class="periodView">
				<span id="weekViewLabel">Select View: </span>
				<div id="weekView" data-on="primary" data-off="info" data-on-label="Full" data-off-label="Week" class="make-switch switch-small">
					<input type="checkbox" checked>
				</div>
				<span id="previousWeek" class="traverseWeek icon-circle-arrow-left" style="color: #5bc0de; display: none;"></span>
				<span id="nextWeek" class="traverseWeek icon-circle-arrow-right" style="color: #5bc0de; display: none;"></span>
            </div>

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
                    <td class="legendBox rejectedBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Rejected</td> 
                    <td style="width:15px"></td>
					<% if (activeRole.equals(Role.FACULTY)) {%>
                    <td class="legendBox timeslotCell unavailableTimeslot" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Not Available</td> 
					<% } else if (activeRole.equals(Role.TA)) { %>
					<td class="legendBox timeslotCell taChosenTimeslot" style="border-width:1px!important;width:19px;"></td><td>&nbsp;Your video signup</td>
					<% } %>
                </tr>
            </table>
        </div>

        <!-- Main schedule navigation -->
        <div class="scheduleContainer container page">
            <ul id="milestoneTab" class="nav nav-tabs">
                <!-- milestone tabs populated dynamically -->
            </ul>
            <div id="milestoneTabContent" class="tab-content" hidden="">
                <!-- milestone tables populated dynamically -->
            </div>
            <div id="scheduleProgressBar" class="progress progress-striped active">
                <div class="bar" style="width: 100%;"></div>
            </div>
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
				
                /*****************************
                    DECLARE COMMON VARIABLES
                ******************************/
               
                //Default milestone is ACCEPTANCE
                var milestone = "ACCEPTANCE";
                var year = "<%= activeTerm.getAcademicYear()%>";
                var semester = "<%= activeTerm.getSemester()%>";
                var scheduleData = null; //This state shall be stored here
                var weekView = null;
				var maxWeekView = null;
                
                //Student-specific variables
                var teamName = "<%= team != null ? team.getTeamName() : null%>"; //Student's active team name
                
                //Admin specific variables
                var teams = JSON.parse('<%= session.getAttribute("allTeams")%>'); //All teams JSON
                var teamDropDownSelect = null;
                var createBookingOutputForAdmin = null;
				
                //TA specific variables
                var loggedInTa = <%= user.getId() %>;
                
                //Booking specific variables
                var self = null;
                var users = JSON.parse('<%= session.getAttribute("allUsers") %>'); //All users JSON
                
                populateMilestones();
                populateSchedule(milestone, year, semester);
                
                //Build the base of the page (Milestone tabs and tables)
                function populateMilestones() {
                    var milestones = getScheduleData(null, year, semester).milestones;
					//Sort milestones by order
                    function compare(a, b) {
                        if (a.milestoneOrder < b.milestoneOrder) {
                            return -1;
                        } else if (a.milestoneOrder > b.milestoneOrder) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
					milestones.sort(compare);
					var setAsActive = true;
                    for (var i = 0; i < milestones.length; i++) {
                        var milestone = milestones[i];
						if (!milestone.bookable) continue;
						$('ul#milestoneTab') //Add the milestone tab
							.append(
								$(document.createElement('li'))
									.addClass(setAsActive?'active':'')
									.append(
										$(document.createElement('a'))
											.attr('id', milestone.name.toLowerCase())
											.attr('href', '#' + milestone.name.toLowerCase())
											.attr('data-toggle', 'tab')
											.html(milestone.name)
									)
							);
						$('div#milestoneTabContent') //Add the milestone table
							.append(
								$(document.createElement('div'))
									.attr('id', milestone.name.toLowerCase() + "Content")
									.addClass('tab-pane fade')
									.addClass(setAsActive?'active in':'')
									.append(
										$(document.createElement('table'))
											.attr('id', milestone.name.toLowerCase() + "ScheduleTable")
											.addClass('scheduleTable table-condensed table-hover table-bordered')
									)
							);
						setAsActive = false;
                    }
                }

                //Function to change schedule based on selected milestone tab
                $('#milestoneTab a').on('click', function(e) {
                    $("#milestoneTab").removeClass('active in');
                    $(this).tab('show');
                    milestone = $(this).attr('id').toUpperCase();
                    year = "<%= activeTerm.getAcademicYear()%>";
                    semester = "<%= activeTerm.getSemester()%>";
                    populateSchedule(milestone, year, semester);
					$("#weekView").bootstrapSwitch('setState', true);
                    return false;
                });
                
                //GetScheduleAction data
                function getScheduleData(milestone, year, semester) {
                    var toReturn = null;
                    var data = {
						milestone: milestone,
                        year: year,
                        semester: semester
                    };
                    //Get schedule action
                    $.ajax({
                        type: 'GET',
                        data: data,
                        async: false,
                        url: 'getSchedule',
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

                //View Schedule stuff
                //Function to populate schedule data based on ACTIVE TERM
                function populateSchedule(milestone, year, semester) {
                    $(".scheduleTable").empty();
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
                    var timeslot = timeslots[bodyTd.attr('value')];

                    //Append booking classes based on status
                    if (booking) {
                        if (timeslot.status === "APPROVED") {
                            booking.addClass('approvedBooking');
                        } else if (timeslot.status === "REJECTED") {
                            booking.addClass('rejectedBooking');
                        } else {
                            booking.addClass('pendingBooking');
                        }
                    }
                    
                    //Getting the content
                    //Additional details
                    var optionalAttendees = $(document.createElement('input')).attr('id', 'updateAttendees').addClass('optionalAttendees popoverInput');

                    //Output in the form of a table
                    var bookingDetails = $(document.createElement('table'));
                    bookingDetails.attr('id', 'viewTimeslotTable');
                    var outputData = [
                        ["Team", timeslot.team],
                        ["Status", timeslot.status],
                        ["Date", timeslot.startDate],
                        ["Time", timeslot.time],
                        ["Venue", timeslot.venue],
                        ["Students", timeslot.students],
                        ["Faculty", timeslot.faculties],
                        ["TA", timeslot.TA],
                        ["Optional", optionalAttendees]
                    ];

                    //Add Delete and Update button if user is a student with a team
                    if (timeslot.team === teamName) {
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
                            if (outputData[i][0] === "Venue") {
                                var input = $(document.createElement('input'));
                                input.attr('id', 'updateFormVenue');
                                input.attr('type', 'text');
                                input.attr('placeholder', outputData[i][1]);
                                input.addClass('updateFormVenue popoverInput');
                                input.val(outputData[i][1]).change();
                                outputData[i][1] = input;
                            }
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
                                input.val(outputData[i][1]).change();
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
                                input.val(outputData[i][1]).change();
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
                        bookingDetails.append(outputTr);
                    }

                    //Popover
                    makePopover(
                        booking,
                        title = function() {
                            if (timeslot.team === teamName) {
                                return "<b>Your Booking</b>";
                            } else {
                                return "<b>Team Booking</b>";
                            }
                        },
                        bookingDetails
                    );
                }
                
                function appendCreateBookingPopover(bodyTd) {
                    if (bodyTd.hasClass('legendBox')) return;
                    //Initialize variables
                    var termId = year + "," + semester;
                    var dateToView = Date.parse(bodyTd.attr('value')).toString("dd MMM");
                    var startTimeToView = Date.parse(bodyTd.attr('value')).toString("HH:mm");
                    var endTimeToView = new Date(Date.parse(bodyTd.attr('value'))).addMinutes(scheduleData.duration).toString('HH:mm');
                    
                    //Additional details
                    var timeslot = scheduleData.timeslots[bodyTd.attr('value')];
                    var optionalAttendees = $(document.createElement('input')).attr('id', 'updateAttendees').addClass('optionalAttendees popoverInput');
                    
                    //If booking was deleted, 
                    if (timeslot.lastBookingWasRemoved) {
                        var deletedDiv = $(document.createElement('div'));
                        if (timeslot.lastBookingRejectReason) {
                            deletedDiv.addClass('rejectedBooking');
                            makeTooltip(bodyTd, 'Removed by ' + timeslot.lastBookingEditedBy);
                        } else {
                            deletedDiv.addClass('deletedBookingOnTimeslot');
                            deletedDiv.addClass('icon-info-sign icon-red');
                            makeTooltip(deletedDiv, 'Removed by ' + timeslot.lastBookingEditedBy);
                        }
                        bodyTd.append(deletedDiv);
                    }

                    //Create Booking outputTable
                    var createBookingDetails = $(document.createElement('table'));
                    createBookingDetails.attr('id', 'createTimeslotTable');
                   
                    var outputData = [
                        ["Date", dateToView],
                        ["Time", startTimeToView + " - " + endTimeToView],
                        ["Milestone", milestone],
                        ["Venue", timeslot.venue],
                        ["TA", timeslot.TA],
                        ["Optional", optionalAttendees]
                    ];
                    if (<%= activeRole.equals(Role.STUDENT) %>) {
                        outputData.unshift(["Team", teamName]); //Add to top of table
                    } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
                        outputData.unshift(["Team", teamDropDownSelect.outerHTML()]); //Add to top of table
                        for (var i = 0; i < outputData.length; i++) {
                            if (outputData[i][0] === "Venue") { //Make Venue editable
                                var input = $(document.createElement('input'));
                                input.attr('id', 'updateFormVenue');
                                input.attr('type', 'text');
                                input.attr('placeholder', outputData[i][1]);
                                input.addClass('updateFormVenue popoverInput');
                                input.val(outputData[i][1]).change();
                                outputData[i][1] = input;
								break;
                            }
                        }
                    }
					
                    if (bodyTd.is('.unavailableTimeslot')) {
                        var timeslots = scheduleData.timeslots;
                        var unavailableTimeslotData = timeslots[bodyTd.attr('value')];
                        outputData.push(["Unavailable", unavailableTimeslotData.unavailable]);
						if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>){
							//Add update button
							var buttonUpdate = $(document.createElement('button')); //Add Update button
							buttonUpdate.attr('id', 'updateTimeslotBtn');
							buttonUpdate.css('float', 'right');
							buttonUpdate.addClass('popoverBtn btn btn-info');
							var iconUpdate = $(document.createElement('i'));
							iconUpdate.addClass('icon-edit icon-white');
							buttonUpdate.append(iconUpdate);
							buttonUpdate.append("Save");
							outputData.push(["", buttonUpdate.outerHTML()]);
						}
                        //TODO: Can create booking still? Add button here
                    } else {
                        var buttonCreate = $(document.createElement('button')); //Add Create button
                        buttonCreate.attr('id', 'createBookingBtn');
                        buttonCreate.addClass('btn btn-primary');
                        var iconCreate = $(document.createElement('i'));
                        iconCreate.addClass('icon-plus-sign icon-white');
                        buttonCreate.append(iconCreate);
                        buttonCreate.append("Book");
                        var buttonUpdate = $(document.createElement('button')); //Add Update button
                        buttonUpdate.attr('id', 'updateTimeslotBtn');
						buttonUpdate.css('float', 'right');
                        buttonUpdate.addClass('popoverBtn btn btn-info');
                        var iconUpdate = $(document.createElement('i'));
                        iconUpdate.addClass('icon-edit icon-white');
                        buttonUpdate.append(iconUpdate);
                        buttonUpdate.append("Save");
						if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>){
							outputData.push(["", buttonCreate.outerHTML() + buttonUpdate.outerHTML()]);
						} else {
							outputData.push(["", buttonCreate]);
						}                        
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
                        createBookingDetails.append(outputTr);
                    }
                    
                    makePopover(
                        bodyTd,
                        title = function(){
                            if (bodyTd.is('.unavailableTimeslot')) {
                                return "Unavailable Timeslot";
                            } else {
                                return "Available Timeslot";
                            }
                        },
                        createBookingDetails
                    );
                }
                
                function appendChangeAvailabilityPopover(bodyTd) {
                    if (bodyTd.hasClass('legendBox')) return;
                    //Create Booking outputTable
                    var changeAvailabailityDetails = $(document.createElement('table'));
                    changeAvailabailityDetails.attr('id', 'createTimeslotTable');
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
                        changeAvailabailityDetails.append(outputTr);
                    }
                    
                    makePopover(
                        bodyTd,
                        "Change Availabaility",
                        changeAvailabailityDetails
                    );
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
//                        console.log("Does booking exist? " + milestone + " " + teamName + " " + bookingExists);
                        toReturn = {bookingExists:bookingExists, existingTimeslot: existingTimeslot};
                    } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
                        teamsPendingBooking = new Array();
                        teamDropDownSelect = $(document.createElement('select'));
                        teamDropDownSelect.attr('name', 'team');
                        teamDropDownSelect.attr('id', 'createTeamSelect');
						teamDropDownSelect.addClass('popoverInput');
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
                    
                    //Hide other popovers when others clicked
                    $('body').off('click', '.bookedTimeslot');
                    $('body').on('click', '.timeslotCell, .booking', function(e) {
                        self = $(this);
                        $('.booking').not(self).popover('hide');
                        $('.timeslotCell').not(self).popover('hide');
                        $.pnotify_remove_all();
                        return false;
                    });

                    //Popover for booked timeslot
                    $('body').off('click', '.bookedTimeslot');
                    $('body').on('click', '.bookedTimeslot, .bookedTimeslot > .booking', function(e) {
                        self = (!$(this).is('.booking')) ? $(this).children('.booking') : $(this);
                        if ($(e.target).parents('.popover').length) return false;
                        self.popover('show');
                        self.find('ul').remove();
                        appendTokenInput(self); //Optional attendees
                        return false;
                    });
                    
                    //Popover for unbooked or unavailable timeslot
                    $('body').off('click', '.unbookedTimeslot, .unbookedTimeslot > .booking, .unavailableTimeslot, .unavailableTimeslot > .booking');
                    $('body').on('click', '.unbookedTimeslot, .unbookedTimeslot > .booking, .unavailableTimeslot, .unavailableTimeslot > .booking', function(e) {
                        if (e.target === this) {
                            self = $(this).is('div') ? $(this).parent('.timeslotCell') : $(this);
                            var timeslot = scheduleData.timeslots[self.attr('value')];
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
                                if (timeslot.lastBookingRejectReason) showNotification("ERROR", self, timeslot.lastBookingEditedBy + ": <br/>" + timeslot.lastBookingRejectReason);
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
                            self.tooltip('hide');
                            self.popover('show');
                            self.find('ul').remove(); //Remove all old tokenInputs
                            appendTokenInput(self); //Optional attendees
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
                        var attendees = $(".optionalAttendees").tokenInput('get');
                        var returnData = createBooking(self, attendees);
                        //REFRESH STATE OF scheduleData
                        self.popover('destroy');
                        self.tooltip('destroy');
                        self.removeClass('unbookedTimeslot');
                        self.addClass('bookedTimeslot');
                        var $deletedDiv = self.children('.deletedBookingOnTimeslot, .rejectedBooking');
                        if ($deletedDiv) $deletedDiv.remove();
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
                        timeslot.removeClass();
                        timeslot.addClass("timeslotCell unbookedTimeslot");
                        timeslot.popover('destroy');
                        delete scheduleData.timeslots[timeslot.attr('value')];
                        scheduleData.timeslots[timeslot.attr('value')] = {id:timeslot.attr('id').split("_")[1], venue:"SIS Seminar Room 2-1", datetime: timeslot.attr('value')}; //TODO: Change SIS Seminar Room 2-1
                        if (<%= activeRole.equals(Role.STUDENT)%>) {
                            self.effect('clip', 'slow', function(){
                                self.remove();
                                var deletedDiv = $(document.createElement('div'));
                                deletedDiv.addClass('deletedBookingOnTimeslot');
                                deletedDiv.addClass('icon-info-sign');
                                makeTooltip(deletedDiv, 'Removed by ' + "<%= user.getFullName() %>");
                                timeslot.append(deletedDiv);
                            });
                            appendCreateBookingPopover(timeslot);
                        } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            self.effect('clip', 'slow', function(){
                                self.remove();
                            });
                            appendPopovers();
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
                        if (startDateVal && startTimeVal) {
                            var newDate = Date.parse(startDateVal);
                            var newTime = Date.parse($.trim(startTimeVal.split(" - ")[0]));
                            if (newDate && newTime) {
                                newDateTime = newDate.toString("yyyy-MM-dd") + " " + newTime.toString("HH:mm:ss");
                            }
                        }
                        var newVenue = $('#updateFormVenue').val();
                        var attendees = $(".optionalAttendees").tokenInput('get');
                        var timeslot = self.parents('.timeslotCell');
                        var returnData = updateBooking(timeslot, newDateTime, newVenue, attendees);
                        if (returnData && returnData.success) {
                            showNotification("INFO", timeslot, null);
                            if (newDateTime !== "" && newDateTime !== timeslot.attr('value')) {
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
                                self.effect('clip', 'slow', function(){
                                   self.remove(); 
                                });
                                delete scheduleData.timeslots[timeslot.attr('value')];
                                scheduleData.timeslots[timeslot.attr('value')] = {id:timeslot.attr('id').split("_")[1], venue:"SIS Seminar Room 2-1", datetime: timeslot.attr('value')}; //TODO: Change SIS Seminar Room 2-1
                                appendCreateBookingPopover(timeslot);
                                bookingDiv.show('clip', 'slow');
                            } else {
                                scheduleData.timeslots[timeslot.attr('value')] = returnData.booking;
                                self.popover('destroy');
                                appendViewBookingPopover(timeslot);
                            }
                        } else {
                            showNotification("WARNING", timeslot, returnData.message);
                        }
                        return false;
                    });
					
                    //Update Timeslot Button
                    $(".timeslotCell").off('click', '#updateTimeslotBtn');
                    $(".timeslotCell").on('click', '#updateTimeslotBtn', function(e) {
                        e.stopPropagation();
                        var newVenue = $('#updateFormVenue').val();
                        var $timeslot = self.closest('.timeslotCell');
                        var returnData = updateTimeslot($timeslot, newVenue);
                        if (returnData && returnData.success) {
							showNotification("INFO", $timeslot, null);
							scheduleData.timeslots[$timeslot.attr('value')].venue = newVenue;
							appendCreateBookingPopover($timeslot);
                        } else {
                            showNotification("WARNING", $timeslot, returnData.message);
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
                    
                    //Update booking validation
                    $(".timeslotCell").on('mouseover', '#updateBookingBtn', function(){
                        var inputDate = $("#updateFormDate").val();
                        if (inputDate) {
                            inputDate = inputDate.replace(/ /g, '-');
                            var date = Date.parse(inputDate);
                            if (!date) {
                                $("#updateFormDate").val("").change();
                                showNotification("WARNING", self, "Please enter a proper date");
                                return false;
                            }
                            var wellFormedDate = date.toString("yyyy-MM-dd");
                            $("#updateFormDate").val(wellFormedDate).change();
                        }
                        var inputTime = $("#updateFormStartTime").val();
                        if (inputTime) {
                            inputTime = inputTime.split(" - ")[0];
                            var time = Date.parse(inputTime);
                            if (!time) {
                                $("#updateFormStartTime").val("").change();
                                showNotification("WARNING", self, "Please enter a proper time");
                                return false;
                            }
                            var wellFormedTime = time.toString("HH:mm");
                            $("#updateFormStartTime").val(wellFormedTime).change();
                        }
                        return false;
                    });
                    
                    //Update booking validation
                    $(".timeslotCell").on('mouseleave', '#updateBookingBtn', function(){
                        var inputDate = $("#updateFormDate").val();
                        if (inputDate) {
                            inputDate = inputDate.replace(/ /g, '-');
                            var date = Date.parse(inputDate);
                            if (!date) {
                                $("#updateFormDate").val("").change();
                                showNotification("WARNING", self, "Please enter a proper date");
                                return false;
                            }
                            var wellFormedDate = date.toString("ddd, dd MMM yyyy");
                            $("#updateFormDate").val(wellFormedDate).change();
                        }
                        var inputTime = $("#updateFormStartTime").val();
                        if (inputTime) {
                            inputTime = inputTime.split(" - ")[0];
                            var time = Date.parse(inputTime);
                            if (!time) {
                                $("#updateFormStartTime").val("").change();
                                showNotification("WARNING", self, "Please enter a proper time");
                                return false;
                            }
                            var wellFormedTime = time.toString("HH:mm") + " - " + time.addMinutes(scheduleData.duration).toString("HH:mm");
                            $("#updateFormStartTime").val(wellFormedTime).change();
                        }
                        return false;
                    });
                    
                    //Week view functions
					$("#weekView").off('switch-change');
                    $("#weekView").on('switch-change', function(e, data){
						setTimeout(function(){
							if (data.value) {
								//Full View
								weekView = null;
								$(".weekNum").remove();
								$(".traverseWeek").hide();
							} else {
								//Week View
								$(".traverseWeek").css('opacity', '100');
								weekView = 0;
								$("#previousWeek").css('opacity', '0');
								if (maxWeekView === 1) $("#nextWeek").css('opacity', '0');
								$(".weekNum").remove();
								$(".traverseWeek").show();
								$("#previousWeek").after($(document.createElement('div')).addClass('weekNum').html('Week ' + (weekView + 1)));
							}
							populateSchedule(milestone, year, semester);
						}, 500);
                        return false;
                    });
					$("#nextWeek").off('click');
                    $("#nextWeek").on('click', function(){
						$(".traverseWeek").css('opacity', '100');
                        if (weekView + 1 < maxWeekView) {
                            ++weekView;
                            $(".weekNum").remove();
                            $("#previousWeek").after($(document.createElement('div')).addClass('weekNum').html('Week ' + (weekView + 1)));
                            populateSchedule(milestone, year, semester);
                        }
						if (weekView + 1 === maxWeekView) $(this).css('opacity', '0');
                        return false;
                    });
					$("#previousWeek").off('click');
                    $("#previousWeek").on('click', function(){
						$(".traverseWeek").css('opacity', '100');
                        if (weekView <= 0) {
                            return false;
                        } else {
                            --weekView;
                            $(".weekNum").remove();
                            $("#previousWeek").after($(document.createElement('div')).addClass('weekNum').html('Week ' + (weekView + 1)));
                            populateSchedule(milestone, year, semester);
                        }
						if (weekView === 0) $(this).css('opacity', '0');
                        return false;
                    });
                }
                
                /***************************
                    AJAX CALL FUNCTIONS
                 ***************************/
                 
                function createBooking(bodyTd, attendees) {
                    var toReturn = null;
                    var data = null;
                    var tName = null;
                    var attendeesArray = new Array();
                    for (var i = 0; i < attendees.length; i++) {
                        attendeesArray.push(attendees[i].id?attendees[i].id:attendees[i].name);
                    }
                    if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            tName = $("#createTeamSelect option:selected").text();
                            var tId = $("#createTeamSelect").val();
                            data = {
                                timeslotId: bodyTd.attr('id').split("_")[1],
                                teamId: tId,
                                "attendees[]": attendeesArray
                            };
                    } else if (<%= activeRole.equals(Role.STUDENT)%>) {
                        tName = teamName;
                        data = {
                            timeslotId: bodyTd.attr('id').split("_")[1],
                            "attendees[]": attendeesArray
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
                function updateBooking(bodyTd, newDateTime, newVenue, attendees) {
                    //getfunction up the timeslotID for that cell and send as request
                    var toReturn = null;
                    var cellId = bodyTd.attr('id').split("_")[1];
                    var attendeesArray = new Array();
                    for (var i = 0; i < attendees.length; i++) {
                        attendeesArray.push(attendees[i].id?attendees[i].id:attendees[i].name);
                    }
                    var data = {timeslotId: cellId, attendees: attendeesArray};
                    if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
                        data["newDateTime"] = newDateTime;
                        data["newVenue"] = newVenue;
                    }
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
				
                //Update timeslot function
                function updateTimeslot(bodyTd, newVenue) {
                    //getfunction up the timeslotID for that cell and send as request
                    var toReturn = null;
                    var cellId = bodyTd.attr('id').split("_")[1];
                    var data = {timeslotId: cellId, venue: newVenue};
                    console.log("Submitting update timeslot data: " + JSON.stringify(data));
                    //Update timeslot AJAX
                    $.ajax({
                        type: 'POST',
                        async: false,
                        url: 'updateTimeslotJson',
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
                    var tableClass = "scheduleTable:first";
                    var timeslots = scheduleData.timeslots;
                    var minTime = scheduleData.dayStartTime;
                    var maxTime = scheduleData.dayEndTime;

                    var timesArray = new Array();
                    for (var i = minTime; i < maxTime; i++) {
                        var timeVal = Date.parse(i + ":00:00");
                        timesArray.push(timeVal.toString("HH:mm:ss"));
                        timeVal.addMinutes(30);
                        timesArray.push(timeVal.toString("HH:mm:ss"));
                    }
                    
                    var datesArray = getDateArrayBetween(scheduleData.startDate, scheduleData.endDate, weekView); //Gets full schedule
//                    var datesArray = datesHashArray; //Gets only timeslot dates

                    //Append header names
					$("." + tableClass)
						.append(
							$(document.createElement('thead'))
								.append(
									$(document.createElement('tr'))
										.attr('id', 'scheduleHeader')
										.append($(document.createElement('td')))
										.append(function(){
											var $tdCollection = new Array();
											for (var i = 0; i < datesArray.length; i++) {
												$tdCollection.push(
													$(document.createElement('td'))
														.addClass('dateCol')
														.attr('id', 'col_' + (i + 1))
														.html(new Date(datesArray[i]).toString('dd MMM') + "<br/>" + new Date(datesArray[i]).toString('ddd'))
												);
											}
											return $tdCollection;
										})
								)
						);
					
					//Append body data
					$("." + tableClass)
						.append(function(){
							var $trCollection = new Array();
							var rowspanArr = new Array();
							for (var i = 0; i < timesArray.length; i++) {
								$trCollection.push(
									$(document.createElement('tr'))
										.append(
											$(document.createElement('td')) //Time display cell
											.addClass('timeDisplayCell')
											.html(timesArray[i].substring(0, 5))
										)
										.append(function(){
											var $tdCollection = new Array();
											rowloop: for (var j = 0; j < datesArray.length; j++) {
												var datetime = new Date(datesArray[j]).toString("yyyy-MM-dd") + " " + timesArray[i];
												for (var k = 0; k < rowspanArr.length; k++) { //Checking if table cell is part of a timeslot
													if (datetime === rowspanArr[k]) {
														continue rowloop;
													}
												}
												var timeslot = timeslots[datetime];
												if (timeslot) {
													var id = timeslot.id;
													var team = timeslot.team;
													var isAvailable = timeslot.available;
													var taId = timeslot.taId;
													var duration = scheduleData.duration;
													for (var t = 30; t < duration; t++) {
														rowspanArr.push(Date.parse(datetime).addMinutes(t).toString("yyyy-MM-dd HH:mm:ss"));
													}
													$tdCollection.push(
														$(document.createElement('td'))
															.attr('id', 'timeslot_' + id)
															.attr('align', 'center')
															.attr('value', datetime)
															.attr('rowspan', duration/30)
															.addClass('timeslotCell')
															.addClass(team?'bookedTimeslot':<%= activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY)%> && !isAvailable?'unavailableTimeslot':'unbookedTimeslot')
															.addClass(<%= activeRole.equals(Role.TA) %> && taId !== undefined?loggedInTa === taId?'taChosenTimeslot':'otherTATimeslot':'')
															.append(team?$(document.createElement('div')).addClass('booking pendingBooking').html(team):false)
														);
												} else {
													$tdCollection.push(
														$(document.createElement('td'))
															.addClass('timeslotCell')
															.addClass(!timeslot?'noTimeslot':'')
													);
												}
											}
											return $tdCollection;
										})
								);
							}
							return $trCollection;
						});
                }
                
                //Get dates between startDate and stopDate
                function getDateArrayBetween(startDate, stopDate, weekNum) {
                    var dateArray = new Array();
                    startDate = Date.parse(startDate);
                    stopDate = Date.parse(stopDate);
					var diffLong = Math.abs(stopDate.getTime() - startDate.getTime());
					var diffDays = Math.ceil(diffLong / (1000 * 3600 * 24));
					maxWeekView = diffDays%7 === 0?(diffDays/7) + 1:Math.ceil(diffDays/7);
                    if (weekNum !== null) {
                        startDate.addWeeks(weekNum);
                        stopDate = startDate.clone().addWeeks(1).addDays(-1);
                    }
                    var currentDate = startDate;
                    while (currentDate <= stopDate) {
                        dateArray.push(currentDate);
                        currentDate = new Date(currentDate).addDays(1);
                    }
                    return dateArray;
                }
                
                /*****************************
                 PLUGINS AND COMPONENTS
                 ****************************/
                
                /* PINES NOTIFY */
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
                            if (!notificationMessage) opts.title = "Booked"; else opts.title = "Note";
                            if (!notificationMessage) opts.text = "Time: " + dateToView + " " + startTimeToView;
                            opts.type = "success";
                            break;
                        case "ERROR":
                            if (!notificationMessage) opts.title = "Deleted"; else opts.title = "Rejected";
                            if (!notificationMessage) opts.text = "Time: " + dateToView + " " + startTimeToView;
                            opts.type = "error";
                            break;
                        case "INFO":
                            if (!notificationMessage) opts.title = "Updated"; else opts.title = "Note";
                            if (!notificationMessage) opts.text = "Time: " + dateToView + " " + startTimeToView;
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
                
                /* TOKEN INPUT */
                function appendTokenInput(booking){
                    booking.find('.optionalAttendees').tokenInput('destroy');
                    var opts = {
                        preventDuplicates: true,
                        theme: "facebook",
                        allowFreeTagging: true,
                        allowTabOut: true,
                        propertyToSearch: "name",
                        resultsLimit: 4,
                        hintText: "Add users or emails",
                        noResultsText: "Press [TAB] to add as email",
                        tokenFormatter: function(item) {
                            if (item && item.id) {
                                return "<li><p>" + item.id + "</p></li>";
                            } else if (item) {
                                return "<li><p>" + item.name + "</p></li>";
                            }
                        }
                    };
                    var timeslot = scheduleData.timeslots[booking.parents('.timeslotCell').attr('value')];
                    if (timeslot) {
                        //View Booking Data
                        opts["prePopulate"] = timeslot.optionals;
                        if (<%= activeRole.equals(Role.FACULTY) %> || (<%= activeRole.equals(Role.STUDENT) %> && timeslot.team !== teamName)) {
                            opts["disabled"] = true;
                        }
                    }
                    booking.find('.optionalAttendees').tokenInput(users, opts);
                }
                
//                $(".booking").draggable({
////                    helper: "clone",
//                    appendTo: "body"
//                });
//                $(".unbookedTimeslot").droppable({
//                        tolerance:"intersect",
//                        drop: function(event, ui) {
//                            var drop_p = $(this).offset();
//                            var drag_p = ui.draggable.offset();
//                            var left_end = drop_p.left - drag_p.left + 1;
//                            var top_end = drop_p.top - drag_p.top + 1;
//                            ui.draggable.animate({
//                                top: '+=' + top_end,
//                                left: '+=' + left_end
//                            });
//                        }
//                });
            };
            
            /* POPOVER */
            function makePopover(container, title, content) {
                container.popover({
                    container: container,
                    trigger: 'manual',
                    html: true,
                    title: title,
//                    title: function(){
//                        var buttonClose = $(document.createElement('button'));
//                        buttonClose.attr('type', 'button');
//                        buttonClose.addClass('close');
//                        buttonClose.append("&times;"); //X sign
//                        return title + buttonClose.outerHTML();
//                    },
                    content: content,
                    placement: function(){
                        if (container.parents("tr").children().index(container.closest(".timeslotCell")) > 9) {
                            return 'left';
                        } else {
                            return 'right';
                        }
                    }
                });
            }
            
            /* POPOVER */
            function makeTooltip(container, title) {
                container.tooltip({
                    container: container,
                    html: true,
                    title: title,
                    placement: function(){
                        if (container.parents("tr").children().index(container.closest(".timeslotCell")) > 9) {
                            return 'left';
                        } else {
                            return 'right';
                        }
                    }
                });
            }

            addLoadEvent(viewScheduleLoad);
        </script>
        <br/>
    </body>
</html>
