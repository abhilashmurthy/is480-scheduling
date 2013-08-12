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
            Team team = user.getTeam();
            String fullName = user.getFullName();
        %>

        <!-- Welcome Text -->
        <div class="container page" >
			<h3 id="activeTermName">
				<%
					String semester = activeTerm.getSemester();
					int startAcademicYear = activeTerm.getAcademicYear();
					int endAcademicYear = startAcademicYear + 1;
					String academicYear = String.valueOf(startAcademicYear) + "-" + 
							String.valueOf(endAcademicYear).substring(2); 
					out.print(academicYear + " " + semester);
				%>
			</h3>
		</div>
	
	<!-- To display the list of active terms -->
	<div class="activeTerms">
		<form id="activeTermForm" action="index" method="post">
			<select name="termId" style="float:right" onchange="this.form.submit()"> 
				<option value="">----- Choose Active Term ----</option>
				<s:iterator value="data">
					<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
				</s:iterator>
			</select>
		</form>
	</div>
	
	<!-- To display number of pending bookings for supervisor/reviewer -->
	<% if (activeRole.equalsIgnoreCase("Supervisor/Reviewer")) { %>
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
	<% } %>  
		
	<!-- To display legend for the calendar -->
    <table class="legend">
        <tr>
            <!-- <td style="width:50px"><b>Legend:</b></td>-->
            <td style="background-color:#AEC7C9;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Available</td> 
            <td style="width:15px"></td>
            <td class="pendingTimeslot" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Pending</td> 
            <td style="width:15px"></td>
            <td class="acceptedTimeslot" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Accepted</td> 
            <td style="width:15px"></td>
            <td class="rejectedTimeslot" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Rejected</td> 
            <td style="width:15px"></td>
            <td style="background-color:#f5f5f5;width:17px;border:1px solid #1E647C;"></td><td>&nbsp;Not Available</td> 
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
            
            //All teams data if for admins
            var teams = null;
            var teamsPendingBooking = new Array();

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
                        
                        //Get Teams data if user is administrator
                        if (<%= activeRole.equalsIgnoreCase("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator") %>) {
                            teams = null;
                            $.ajax({
                                type: 'GET',
                                url: 'getTeams',
                                data: data,
                                async: false,
                                dataType: 'json'
                            }).done(function(response){
                                if (response.success) {
                                    teams = response.teamList;
                                } else {
                                    var eid = btoa(response.message);
                                    window.location = "error.jsp?eid=" + eid;
                                }
                            });
                        }
                        
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
                $(".timeslotCell").trigger('mouseleave');
                $(".timeslotCell").popover('destroy');
                
                //Add View Booking popovers
                $(".bookedTimeslot").each(function(){
                   appendViewBookingPopover($(this));
                });
                
                //Logged in team name
                teamName = "<%= team != null?team.getTeamName():null %>";
                
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
                    
                    //Append bodyTd classes based on status
                    if (viewBookingData.status === "ACCEPTED") {
                        bodyTd.addClass("acceptedTimeslot");
                    } else if (viewBookingData.status === "REJECTED") {
                        bodyTd.addClass("rejectedTimeslot");
                    } else {
                        bodyTd.addClass("pendingTimeslot");
                    }

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
                                ["Team", viewBookingData.teamName],
                                ["Status", viewBookingData.status],
                                ["Date", viewBookingData.startDate],
                                ["Start Time", viewBookingData.startTime],
                                ["Team Wiki", viewBookingData.teamWiki],
                                ["Attendees", viewBookingData.attendees]
                            ];
                            
                            //Add delete button if user if part of team
                            if (viewBookingData.teamName === teamName) {
                                outputData.push(["", "<button id='deleteBookingBtn' class='btn btn-danger'><i class='icon-trash icon-white'></i>Delete</button>"]);
                            }
                            
                            //Add Update and Delete buttons if user is admin
                            if (<%=activeRole.equalsIgnoreCase("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator")%>) {
                                    //viewBookingData.startDate
                                    var outputData = [
                                            ["Team", viewBookingData.teamName],
                                            ["Status", viewBookingData.status],
                                            ["Date",  viewBookingData.startDate],
                                            ["Start Time", viewBookingData.startTime],
                                            ["Team Wiki", viewBookingData.teamWiki],
                                            ["Attendees", viewBookingData.attendees],
                                            ["", "<button id='deleteBookingBtn' class='btn btn-danger'><i class='icon-trash icon-white'></i>Delete</button>"
												+ "&nbsp&nbsp&nbsp&nbsp&nbsp;" 
												+ "<button id='updateBookingBtn' class='btn btn-info'><i class='icon-edit icon-white'></i>Save</button>"]
                                    ];
                            }
                            
                            //Make fields editable is user is admin
                            if (<%=!(activeRole.equalsIgnoreCase("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator"))%>) {
			
                                for (var i = 0; i < outputData.length; i++) {
                                    var outputTr = $(document.createElement('tr'));
                                    var outputTdKey = $(document.createElement('td'))
                                                    .html('<b>' + outputData[i][0] + '</b>');
                                    var outputTdValue = null;
                                    if (outputData[i][1] instanceof Array) {
                                            var outputArray = outputData[i][1];
                                            var outputArrayStr = "";
                                            for (var j = 0; j < outputArray.length; j++) {
                                                    outputArrayStr += outputArray[j].name + "<br/>";
                                            }
                                            outputTdValue = $(document.createElement('td'))
                                                            .html(outputArrayStr);
                                            ;
                                    } else {
                                            outputTdValue = $(document.createElement('td'))
                                                            .html(outputData[i][1]);
                                    }
                                    outputTr.append(outputTdKey);
                                    outputTr.append(outputTdValue);
                                    outputTable.append(outputTr);
                                }
                            } else {
                                var updateForm = "updateForm";

                                for (var i = 0; i < outputData.length; i++) {
                                        var outputTr = $(document.createElement('tr'));
                                        var outputTdKey = $(document.createElement('td'))
                                                        .html('<b>' + outputData[i][0] + '</b>');
                                        var outputTdValue = null;
                                        if (outputData[i][1] instanceof Array) {
                                                var outputArray = outputData[i][1];
                                                var outputArrayStr = "";
                                                for (var j = 0; j < outputArray.length; j++) {
                                                   outputArrayStr += outputArray[j].name + "<br/>";
                                                }
                                                   outputTdValue = $(document.createElement('td'))
                                                         .html(outputArrayStr);
                                                ;
                                        } else {
                                                if(outputData[i][0] === 'Date' || outputData[i][0] === 'Start Time'){

                                                        if(outputData[i][0] === 'Date'){

                                                           updateForm = "updateForm" + 'Date';
                                                           outputTdValue = $(document.createElement('td'))
                                                              .html("<input type='text' id='" + updateForm + "'" + " placeholder='" + outputData[i][1] + "' title='Enter date in YYYY-MM-DD format (e.g. 2013-01-10)' /input>");
                                                      
                                                        }else{

                                                                updateForm = "updateForm" + 'StartTime';
                                                                outputTdValue = $(document.createElement('td'))
                                                                        .html("<input type='text' id='" + updateForm + "'" + " placeholder='" + outputData[i][1] + "' title='Enter time in HH:MM format (e.g. 10:00)' /input>");     

                                                        }

                                                }else{
                                                        outputTdValue = $(document.createElement('td'))
                                                         .html(outputData[i][1]);
                                                }
                                        }
                                        
                                        outputTr.append(outputTdKey);
                                        outputTr.append(outputTdValue);
                                        outputTable.append(outputTr);

                                }

                            }

                            return outputTable;
                        },
                        placement: 'right',
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
                var bookingExists = $("#" + milestoneStr.toLowerCase() + "ScheduleTable").find(":contains(" + teamName + ")").length;
                $(".unbookedTimeslot").each(function(){
                   appendCreateBookingPopover($(this));
                });
                
                function appendCreateBookingPopover(bodyTd) {
                    if (teamName !== null) {
                        //Is student and can book
                        if (bookingExists) {
                            bodyTd.popover({
                               container: bodyTd,
                               html: 'true',
                               trigger: 'manual',
                               placement: 'right',
                               title: "Booking <button type='button' class='close'>&times;</button>",
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
                            
                            //Make a dropdown of all teams that have not booked yet if user is admin
                            if (<%= activeRole.equals("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator") %>) {                               
                                if (teams.length !== 0) {
                                    var teamDropDownSelect = $(document.createElement('select'));
                                    teamDropDownSelect.attr('name', 'team');
                                    teamDropDownSelect.attr('id', 'createTeamSelect');
                                    for (var t = 0; t < teams.length; t++) {
                                        //Append only teams without bookings
                                        if (!$("#" + milestoneStr.toLowerCase() + "ScheduleTable").find(":contains(" + teams[t].teamName + ")").length) {
                                            var teamDropDownOption = $(document.createElement('option'));
                                            teamDropDownOption.attr('value', teams[t].teamName);
                                            teamDropDownOption.html(teams[t].teamName);
                                            teamDropDownSelect.append(teamDropDownOption);
                                            teamsPendingBooking.push(teams[t].teamName);
                                        }
                                    }
                                    teamName = teamDropDownSelect;
                                }
                            }

                            var outputData = [
                                ["Team", teamName],
                                ["Date", dateToView],
                                ["Start Time", startTimeToView],
                                ["End Time", endTimeToView],
                                ["Milestone", milestoneStr],
                                ["", "<button id='createBookingBtn' class='btn btn-primary'><i class='icon-plus-sign icon-white'></i>Create</button>"]
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
                            
                            //Make a notification popover saying all teams have booked if no more teams pending booking
                            if (<%= activeRole.equals("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator") %> && teamsPendingBooking.length === 0) {
                                bodyTd.popover({
                                   container: bodyTd,
                                   html: 'true',
                                   trigger: 'manual',
                                   placement: 'right',
                                   title: "Booking <button type='button' class='close'>&times;</button>",
                                   content: 'All teams have made bookings!'
                                });
                            } else {
                                bodyTd.popover({
                                   container: bodyTd,
                                   html: 'true',
                                   trigger: 'manual',
                                   placement: 'right',
                                   content: outputTable,
                                   title: "Create Booking <button type='button' class='close'>&times;</button>"
                                });
                            }

                        }
                    }
                };
            };

            //Function to create mouse UI events
            function setupMouseEvents() {
                
                //Removed clicked
                $(".timeslotCell").mouseleave(function() {
                    $(this).removeClass("clickedCell");
                });
                
                //Hide all popovers on page click
                $(".page").on('click', function(e) {
                    console.log("page clicked");
                    //Close all timeslots
                    $(".timeslotCell").popover('hide');
                });
                
                //Add clickedCell and initialize common variables
                $(".timeslotCell").on('click', function(e){
                    if (e.target === this) {
                        self = $(this);
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
                    }
                    return false;
                });
                
                //Popover for booked timeslot
                $(".bookedTimeslot").on('click', function(e) {
                    if (e.target === this) {
                        console.log(".bookedTimeslot clicked");
                        self = $(this);
                        self.popover('show');
                    }
                    return false;
                });
                
                $(".unbookedTimeslot").on('click', function(e) {
                    if (e.target === this) {
                        console.log(".unbookedTimeslot clicked");
                        //Only display if user is student or admin
                        if (<%= activeRole.equalsIgnoreCase("Student") || activeRole.equalsIgnoreCase("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator") %>) {
                            self = $(this);
                            self.popover('show');
                            date = Date.parse(self.attr('value')).toString("yyyy-MM-dd");
                            startTime = Date.parse(self.attr('value')).toString("HH:mm:ss");
                            termId = activeAcademicYearStr + "," + activeSemesterStr;
                        }
                    }
                    return false;
                });
                
                //
                //Dynamic button bindings
                //Use this kind of event trigger for dynamic buttons
                //Close Booking Button
                $("td").on('click', '.close', function(e) {
                    e.stopPropagation();
                    self.popover('hide');
                    self.trigger('mouseleave');
                    return false;
                });
                
                //Create Booking Button
                $("td").on('click', '#createBookingBtn', function(e) {
                    e.stopPropagation();
                    if (<%= activeRole.equals("Administrator") || activeRole.equalsIgnoreCase("Course Coordinator") %>) {
                        teamName = $("#createTeamSelect").val();
                    }
                    createBooking(self);
                    setTimeout(function(){appendPopovers();}, 3000); //Refresh all popovers
                    return false;
                });

                //Delete Booking Button
                $("td").on('click', '#deleteBookingBtn', function(e) {
                    e.stopPropagation();
                    deleteBooking(self);
                    setTimeout(function(){appendPopovers();}, 3000); //Refresh all popovers
                    return false;
                });
                
                //Update Booking Button
                $("td").on('click', '#updateBookingBtn', function(e) {
                    e.stopPropagation();
                    updateBooking(self);
                    setTimeout(function() {
                            appendPopovers();
                    }, 3000); //Refresh all popovers
                    //window.location.reload()
                    return false;
                });
            }
            
            //AJAX CALL functions
            function createBooking(self) {
                var data = {
                    date: date,
                    startTime: startTime,
                    termId: termId,
                    milestoneStr: milestoneStr.toLowerCase(),
                    teamName: teamName
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
                        
                        self.html(teamName);
                        self.removeClass();
                        self.addClass("timeslotCell bookedTimeslot");
                        self.addClass("pendingTimeslot");
                        
                        //Popover to mention timeslot created successfully
                        self.popover({
                            container: self,
                            trigger: "manual",
                            title: "Booking <button type='button' class='close'>&times;</button>",
                            placement: "right",
                            content: "Booked <br/> Confirmation email sent!",
                            html: true
                        });
                        console.log('Toggling D');
                        self.popover('show');
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
                        
                        //Popover to mention timeslot created successfully
                        self.popover({
                            container: self,
                            trigger: "manual",
                            title: "Booking <button type='button' class='close'>&times;</button>",
                            placement: "right",
                            content: "Deleted <br/> Notification email sent!",
                            html: true
                        });
                        console.log('Toggling E');
                        self.popover('show');
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
                var cellId = $(self).attr('id').split("_")[1];
                console.log("data");

                var startDate = document.getElementById('updateFormDate').value;
                var startTime = document.getElementById('updateFormStartTime').value;
                var concatStart = "";
                if(startDate!== null && startTime!== null){
                         concatStart = startDate + " " + startTime + ":00";
                }
                console.log(concatStart + " this is concat start");
                //Delete Booking AJAX
                //var test = "2013-08-07 09:00:00";

                //var viewBookingData = appendPopovers();

                //var arrAttendees = "";
                //var oldAttendees = "";

                /*for (var i = 0; i < viewBookingData.attendees.length; i++) {
                         //var personnel = viewBookingData.attendees[i].name;
                         var val2 = document.getElementById('updateFormAttendee'+i).value;

                         oldAttendees += viewBookingData.attendees[i].name + ",";
                         arrAttendees  += val2 + ",";

                     //console.log(arrAttendees.);
                }*/

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
                            var msg = response.message + "";
                            console.log(msg);

                            if(msg === ('Booking updated successfully! Update email has been sent to all attendees. (Coming soon..)')){

                               self.popover('destroy');
                               self.html("");
                               self.removeClass("bookedTimeslot");
                               self.addClass("unbookedTimeslot");

                               //Popover to mention timeslot updated successfully
                                self.popover({
                                    container: self,
                                    trigger: "manual",
                                    title: "Booking <button type='button' class='close'>&times;</button>",
                                    placement: "right",
                                    content: "Edited <br/> Notification email sent!",
                                    html: true
                                });
                                
                                console.log('Toggling E');

                                self.popover('show');
                                window.location.reload();
                            }else{
                                //Popover to mention updating problem
                                self.popover('destroy');
                                //self.html("");

                                self.popover({
                                    container: self,
                                    trigger: "manual",
                                    title: "Booking <button type='button' class='close'>&times;</button>",
                                    placement: "right",
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
