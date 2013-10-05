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

			<!-- To display a banner for filling survey. Remove later -->
			<div class="banner alert">
				<button type="button" class="close" data-dismiss="alert">×</button>
				Hi <%= user.getFullName()%>, <br/><br/>
				We have spent a lot of time building this system. We will really appreciate
				if you could give us <a href="https://docs.google.com/forms/d/1dZvPHlAV5VhJjupRCHiYT52hHZ2nIDD4IoLNeX98ogM/viewform" 
										target="_blank">feedback</a> 
				on your experience with our system!<br/><br/>
				Thanking you,<br/>
				IS480 Scheduling Team
			</div>
			
            <!-- To display number of pending bookings for supervisor/reviewer -->
            <% if (activeRole.equals(Role.FACULTY)) {%>
            <s:if test="%{pendingBookingCount > 0}">
                <div class="pendingBookings alert">
                    <button type="button" class="close" data-dismiss="alert">×</button>
                    <a href="approveReject" style="color:#B88A00;">
                        <s:if test="%{pendingBookingCount > 1}">
                            You have <s:property value="pendingBookingCount"/> pending bookings!
                        </s:if><s:else>
                            You have <s:property value="pendingBookingCount"/> pending booking!
                        </s:else>
                    </a>
                </div>
            </s:if>
            <% }%>
            
			
            <div class="settingsView">
				<span id="settingsViewLabel">Select View: </span>
				<div id="weekView" data-on="primary" data-off="info" data-on-label="Full" data-off-label="Week" class="make-switch switch-small">
					<input type="checkbox" checked>
				</div>
				<span id="previousWeek" class="traverseWeek icon-circle-arrow-left" style="color: #5bc0de; display: none; cursor: pointer"></span>
				<span id="nextWeek" class="traverseWeek icon-circle-arrow-right" style="color: #5bc0de; display: none; cursor: pointer"></span>
            </div>
			<% if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) { %>
				<div class="settingsView">
					<span id="settingsViewLabel">Update Bookings Drag-N-Drop: </span>
					<div id="dragDropView" data-on="warning" data-off="success" data-on-label="On" data-off-label="Off" class="make-switch switch-small">
						<input type="checkbox">
					</div>
				</div>
			<% } %>

            <!-- To display legend for the calendar -->
            <table class="legend">
                <tr>
                    <!-- <td style="width:50px"><b>Legend:</b></td>-->
					<% if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY) || activeRole.equals(Role.TA)) {%>
                    <td class="legendBox myTeamBooking" style="text-align: center; font-size: 16px; font-weight: bold; border:1px solid #1E647C; width:17px;">T</td><td>&nbsp;My Team</td> 
					<td style="width:15px"></td>
					<% } %>
                    <td style="background-color:#AEC7C9;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Available</td> 
                    <td style="width:15px"></td>
                    <td class="legendBox pendingBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Pending</td> 
                    <td style="width:15px"></td>
                    <td class="legendBox approvedBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Approved</td> 
                    <td style="width:15px"></td>
					<% if (activeRole.equals(Role.STUDENT)) {%>
                    <td class="legendBox rejectedBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Rejected</td> 
                    <td style="width:15px"></td>
					<% } %>
					<% if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY)) {%>
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
				var dragDropView = false;
                
                //Student-specific variables
                var teamName = "<%= team != null ? team.getTeamName() : null%>"; //Student's active team name
                
                //Admin specific variables
                var teams = JSON.parse('<%= session.getAttribute("allTeams")%>'); //All teams JSON
                var $teamDropDownSelect = null;
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
                        var thisMilestone = milestones[i];
						if (!thisMilestone.bookable) continue;
						$('ul#milestoneTab') //Add the milestone tab
							.append(
								$(document.createElement('li'))
									.addClass(setAsActive?'active':'')
									.append(
										$(document.createElement('a'))
											.attr('id', thisMilestone.name.toLowerCase())
											.attr('href', '#' + thisMilestone.name.toLowerCase())
											.attr('data-toggle', 'tab')
											.html(thisMilestone.name)
									)
							);
						$('div#milestoneTabContent') //Add the milestone table
							.append(
								$(document.createElement('div'))
									.attr('id', thisMilestone.name.toLowerCase() + "Content")
									.addClass('tab-pane fade')
									.addClass(setAsActive?'active in':'')
									.append(
										$(document.createElement('table'))
											.attr('id', thisMilestone.name.toLowerCase() + "ScheduleTable")
											.addClass('scheduleTable table-condensed table-hover table-bordered')
									)
							);
						milestone = setAsActive?thisMilestone.name.toUpperCase():milestone;
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
					$("#dragDropView").bootstrapSwitch('setState', false);
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
						//Append popovers
						if (!dragDropView) appendPopovers();
                        //Setup mouse events
                        setupMouseEvents();
						//Add drag and drop
						if (dragDropView) initDragNDrop();
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
                    $('#milestoneTabContent').hide();
                    $('#scheduleProgressBar').show();
                    
                    //Delete all old popovers
                    $('.timeslotCell').trigger('mouseleave').popover('destroy');
                    
                    refreshScheduleData();

                    //Add View Booking popovers
                    $('.bookedTimeslot').each(function() {
                        appendViewBookingPopover($(this));
                    });

                    //Add Create Booking popover for all timeslots if is student, admin, or course coordinator
                    if (<%= activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                        $('.unbookedTimeslot, .unavailableTimeslot').each(function() {
                            appendCreateBookingPopover($(this));
                        });
                    }
                    if (<%= activeRole.equals(Role.FACULTY) %>) {
                        $('.timeslotCell').each(function() {
                            appendChangeAvailabilityPopover($(this));
                        });
                    }
                    //Show progress bar
                    $('#scheduleProgressBar').hide();
                    $('#milestoneTabContent').show();
                }
                
                function appendViewBookingPopover($td) {
                    var timeslot = scheduleData.timeslots[$td.attr('value')];					
                    var $bookingDetailsTable = $(document.createElement('table'));
                    $bookingDetailsTable.attr('id', 'viewTimeslotTable');
					var outputData = {
						Team: timeslot.team,
						Status: timeslot.status,
						Date: timeslot.startDate,
						Time: timeslot.time,
						Venue: timeslot.venue,
						Students: function(){
							var studentList = '';
							for (var i = 0; i < timeslot.students.length; i++) {
								studentList += (timeslot.students[i].name + "<br/>");
							}
							return studentList;
						},
						Faculty: function(){
							var facultyList = '';
							for (var i = 0; i < timeslot.faculties.length; i++) {
								facultyList += (timeslot.faculties[i].name + " (" + timeslot.faculties[i].status.toLowerCase() + ")" + "<br/>");
							}
							return facultyList;
						},
						TA: timeslot.TA,
						Others: $(document.createElement('input')).attr('id', 'updateAttendees').addClass('optionalAttendees popoverInput')
					};

                    //Allow team to edit booking
                    if (timeslot.team === teamName) {
                        outputData[""] = (
							$(document.createElement('button'))
								.attr('id', 'deleteBookingBtn')
								.addClass('popoverBtn btn btn-danger')
								.append($(document.createElement('i')).addClass('icon-trash icon-white'))
								.append('Delete')
								.outerHTML()
							+
							$(document.createElement('button'))
								.attr('id', 'updateBookingBtn')
								.addClass('popoverBtn btn btn-info')
								.append($(document.createElement('i')).addClass('icon-edit icon-white'))
								.append('Save')
								.css('float', 'right')
								.outerHTML()
						);
                    }

                    //Allow admin to edit fields
                    if (<%=activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
						outputData.Venue =
								$(document.createElement('input'))
									.attr('id', 'updateFormVenue')
									.attr('type', 'text')
									.attr('placeholder', outputData.Venue)
									.addClass('updateFormVenue popoverInput')
									.val(outputData.Venue).change();
						outputData.Date = 
								$(document.createElement('input'))
									.attr('id', 'updateFormDate')
									.attr('type', 'text')
									.attr('placeholder', outputData.Date)
									.attr('title', 'Enter a new date (YYYY-MM-DD)')
									.addClass('updateFormDate popoverInput')
									.val(outputData.Date).change();
						outputData.Time = 
								$(document.createElement('input'))
									.attr('id', 'updateFormStartTime')
									.attr('type', 'text')
									.attr('placeholder', outputData.Time)
									.attr('title', 'Enter a new start time (HH:MM)')
									.addClass('updateFormDate popoverInput')
									.val(outputData.Time).change();	
                        outputData[""] = (
							$(document.createElement('button'))
								.attr('id', 'deleteBookingBtn')
								.addClass('popoverBtn btn btn-danger')
								.append($(document.createElement('i')).addClass('icon-trash icon-white'))
								.append("Delete")
								.outerHTML()
							+
							$(document.createElement('button'))
								.attr('id', 'updateBookingBtn')
								.addClass('popoverBtn btn btn-info')
								.append($(document.createElement('i')).addClass('icon-edit icon-white'))
								.append("Save")
								.outerHTML()
						);
                    }

                    //Append all fields
					for (var key in outputData) {
						if (outputData.hasOwnProperty(key)) {
							$bookingDetailsTable
								.append(
									$(document.createElement('tr'))
										.append($(document.createElement('td')).html(key))
										.append($(document.createElement('td')).html(outputData[key]))
								);
						}
					}

                    //Popover
                    makePopover($td.children('.booking'), timeslot.team === teamName?"<b>Your Booking</b>":"<b>Team Booking</b>", $bookingDetailsTable);
                }				
                
                function appendCreateBookingPopover($td) {
                    if ($td.hasClass('legendBox')) return;
                    var timeslot = scheduleData.timeslots[$td.attr('value')];
					
                    var $createBookingTable = $(document.createElement('table'));
                    $createBookingTable.attr('id', 'createTimeslotTable');
					var outputData = {
						Team: <%= activeRole.equals(Role.STUDENT) %>?teamName:<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>?$teamDropDownSelect.outerHTML():'',
						Date: Date.parse($td.attr('value')).toString("dd MMM"),
						Time: Date.parse($td.attr('value')).toString('HH:mm')+ " - " + Date.parse($td.attr('value')).addMinutes(scheduleData.duration).toString('HH:mm'),
						Venue: timeslot.venue,
						Milestone: milestone,
						TA: timeslot.TA,
						Others: $(document.createElement('input')).attr('id', 'updateAttendees').addClass('optionalAttendees popoverInput')
					};
                   
				   if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
					   //Make fields editable for admin
					   outputData.Venue = 
							$(document.createElement('input'))
								.attr('id', 'updateFormVenue')
								.attr('type', 'text')
								.attr('placeholder', outputData.Venue)
								.addClass('updateFormVenue popoverInput')
								.val(outputData.Venue).change();
						outputData[""] = 
							$(document.createElement('button'))
								.attr('id', 'createBookingBtn')
								.addClass('popoverBtn btn btn-primary')
								.append($(document.createElement('i')).addClass('icon-plus-sign  icon-white'))
								.append("Book")
								.outerHTML()
							+
							$(document.createElement('button'))
								.attr('id', 'updateTimeslotBtn')
								.css('float', 'right')
								.addClass('popoverBtn btn btn-info')
								.append($(document.createElement('i')).addClass('icon-edit icon-white'))
								.append("Save")
								.outerHTML();
				   } else {
					   //For students
					   if ($td.is('.unavailableTimeslot')) {
						   outputData["Unavailable"] = function() {
							   var unavailableList = '';
							   for (var i = 0; i < timeslot.unavailable.length; i++) {
								   unavailableList += timeslot.unavailable[i] + "<br/>"
							   }
							   return unavailableList;
						   };
					   } else {
						outputData[""] = 
							$(document.createElement('button'))
								.attr('id', 'createBookingBtn')
								.addClass('popoverBtn btn btn-primary')
								.append($(document.createElement('i')).addClass('icon-plus-sign icon-white'))
								.append("Book")
								.outerHTML();
							//TODO: Can create booking still? Add button here
					   }	
				   }
					
                    //Append all fields
					for (var key in outputData) {
						if (outputData.hasOwnProperty(key)) {
							$createBookingTable
								.append(
									$(document.createElement('tr'))
										.append($(document.createElement('td')).html(key))
										.append($(document.createElement('td')).html(outputData[key]))
								);
						}
					}
                    
					//Popover
					makePopover($td, $td.is('.unavailableTimeslot')?"Unavailable Timeslot":"Available Timeslot", $createBookingTable);
                }
                
                function appendChangeAvailabilityPopover($td) {
                    if ($td.hasClass('legendBox')) return;
                    var $changeAvailabilityTable = $(document.createElement('table')).attr('id', 'createTimeslotTable');
                    var outputData = {};
					
					outputData["You Are"] = function() {
						if ($td.is('.unavailableTimeslot')) {
							return $(document.createElement('button'))
										.attr('id', 'availableTimeslotBtn')
										.addClass('popoverBtn btn btn-primary')
										.css('float', 'right')
										.append($(document.createElement('i')).addClass('icon-plus-sign icon-white'))
										.append("Available")
										.outerHTML();
						} else {
							return $(document.createElement('button'))
										.attr('id', 'unavailableTimeslotBtn')
										.addClass('popoverBtn btn btn-primary')
										.css('float', 'right')
										.append($(document.createElement('i')).addClass('icon-minus-sign icon-white'))
										.append("Unavailable")
										.outerHTML();
						}
					};
					
                    //Append all fields
					for (var key in outputData) {
						if (outputData.hasOwnProperty(key)) {
							$changeAvailabilityTable
								.append(
									$(document.createElement('tr'))
										.append($(document.createElement('td')).html(key))
										.append($(document.createElement('td')).html(outputData[key]))
								);
						}
					}
                    
					//Popover
					makePopover($td, "Change Availability", $changeAvailabilityTable);
                }
                
                //Function to refresh booking exists
                function refreshScheduleData() {
                    var toReturn = null;
                    var bookingExists = 0;
                    var $existingTimeslot = null;
                    var teamsPendingBooking = null;
					//Initialize dragDropView
					if (!$('.booking').length) {
						$("#dragDropView").parent().hide(); 
						
					} else { 
						$("#dragDropView").parent().show(); 
					}
                    if (<%= activeRole.equals(Role.STUDENT) %>) {
                        for (var key in scheduleData.timeslots) {
                            if (scheduleData.timeslots.hasOwnProperty(key)) {
                                var timeslot = scheduleData.timeslots[key];
                                if (timeslot.team && timeslot.team === teamName) {
                                    bookingExists = 1;
                                    $existingTimeslot = $(document.createElement('div'))
										.attr('value', timeslot.datetime)
										.addClass('existingTimeslot');
                                    break;
                                }
                            }
                        }
                        toReturn = {bookingExists:bookingExists, existingTimeslot: $existingTimeslot};
                    } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR) %>) {
                        teamsPendingBooking = new Array();
                        $teamDropDownSelect = $(document.createElement('select'))
							.attr('name', 'team')
							.attr('id', 'createTeamSelect')
							.addClass('popoverInput');
                        outerTeams: 
                        for (var t = 0; t < teams.length; t++) { //Append only teams without bookings
                            var adminTeamName = teams[t].teamName;
                            for (var key in scheduleData.timeslots) {
                                if (scheduleData.timeslots.hasOwnProperty(key)) {
                                    var timeslot = scheduleData.timeslots[key];
                                    if (timeslot.team && timeslot.team === adminTeamName) {
                                        continue outerTeams;
                                    }
                                }
                            }
                            $teamDropDownSelect
								.append($(document.createElement('option'))
									.attr('value', teams[t].teamId)
									.html(adminTeamName)
								);
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
                    $('.timeslotCell').mouseleave(function() {
                        $(this).removeClass('clickedCell');
                    });
                    
                    //Hide other popovers when others clicked
                    $('body').off('click', '.timeslotCell, .booking');
                    $('body').on('click', '.timeslotCell, .booking', function(e) {
						self = <%= activeRole.equals(Role.FACULTY) %>?$(this):$(this).children('.booking').length?$(this).children('.booking'):$(this);
						$('.timeslotCell, .booking').not(self).popover('hide');
						$(".hasDatepicker").datepicker('destroy');
						$.pnotify_remove_all();
                        return false;
                    });

                    //Popover for booked timeslot
                    $('body').off('click', '.bookedTimeslot:not(.unavailableTimeslot), .bookedTimeslot > .booking');
                    $('body').on('click', '.bookedTimeslot:not(.unavailableTimeslot), .bookedTimeslot > .booking', function(e) {
						if (e.target === this) {
							if ($(this).hasClass('timeslotCell') && <%= activeRole.equals(Role.FACULTY)%>) {
								$(this).popover('show');
								if ($(this).find('tr:last').offset().top - $(window).scrollTop() > window.innerHeight){
									$('body').animate({scrollTop: $(this).find('tr:last').offset().top - $(window).scrollTop()}, 500);
								} else if ($(this).find('tr:first').offset().top - $(window).scrollTop() < window.innerHeight) {
									$('body').animate({scrollTop: $(this).find('tr:last').offset().top - $(window).scrollTop()}, 500);
								}
								return false;
							}
							self = ($(this).is('.booking')) ? $(this) : $(this).children('.booking');
							var timeslot = scheduleData.timeslots[self.closest('.timeslotCell').attr('value')];
							self.popover('show');
							if (self.find('tr:last').offset().top - $(window).scrollTop() > window.innerHeight){
								$('body').animate({scrollTop: self.find('tr:last').offset().top - $(window).scrollTop()}, 500);
							}
							self.find("#updateFormDate").val(timeslot.startDate).change();
							self.find("#updateFormStartTime").val(timeslot.time).change();
							self.find('ul').remove();
							appendTokenInput(self); //Optional attendees
						}
                        return false;
                    });
                    
                    //Popover for unbooked timeslot
                    $('body').off('click', '.unbookedTimeslot:not(.unavailableTimeslot)');
                    $('body').on('click', '.unbookedTimeslot:not(.unavailableTimeslot)', function(e) {
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
                                    showNotification("WARNING", self, "You can only book for " + teamTerm);
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
//                            console.log(".unbookedTimeslot clicked.");
                            self.tooltip('hide');
                            self.popover('show');
							if (self.find('tr:last').offset().top - $(window).scrollTop() > window.innerHeight){
								$('body').animate({scrollTop: self.find('tr:last').offset().top - $(window).scrollTop()}, 500);
							}
                            self.find('ul').remove(); //Remove all old tokenInputs
                            appendTokenInput(self); //Optional attendees
                        }
                        return false;
                    });
					
                    //Popover for unavailable timeslot
                    $('body').off('click', '.unavailableTimeslot');
                    $('body').on('click', '.unavailableTimeslot', function(e) {
						if (e.target === this) {
							var timeslot = scheduleData.timeslots[self.attr('value')];
							self = $(this);
							var refreshData = refreshScheduleData();
							if (<%= activeRole.equals(Role.STUDENT) %>) {
								if (refreshData.bookingExists !== 0) {
									showNotification("WARNING", refreshData.existingTimeslot, "You already have a booking!");
									return false;
								}
								var teamTerm = "<% try {out.print(!team.getTerm().equals(activeTerm)?team.getTerm().getAcademicYear() + " " + team.getTerm().getSemester():"thisTerm");} catch (Exception e) {out.print("thisTerm");} %>";
								if (teamTerm !== "thisTerm") {
									showNotification("WARNING", self, "You can only book for " + teamTerm);
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
							self.popover('show');
							if (self.find('tr:last').offset().top - $(window).scrollTop() > window.innerHeight){
								$('body').animate({scrollTop: self.find('tr:last').offset().top - $(window).scrollTop()}, 500);
							}
							self.find('ul').remove();
							appendTokenInput(self); //Optional attendees
						}
                        return false;
                    });
                    
                    /*****************************
                     POPOVER INTERACTION
                     ****************************/

                    //Close Booking Button
                    $('.timeslotCell').off('click', '.close');
                    $('.timeslotCell').on('click', '.close', function(e) {
                        e.stopPropagation();
                        self.popover('hide');
                        self.trigger('mouseleave');
                        return false;
                    });

                    //Create Booking Button
                    $('.timeslotCell').off('click', '#createBookingBtn');
                    $('.timeslotCell').on('click', '#createBookingBtn', function(e) {
                        //NOTE: self is a .timeslotCell here
                        var attendees = $('.optionalAttendees').tokenInput('get');
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
						bookingDiv.addClass(<%= activeRole.equals(Role.STUDENT) %>?'myTeamBooking':false);
                        bookingDiv.html(returnData.booking.team);
                        bookingDiv.css('display', 'none');
                        self.append(bookingDiv);
                        showNotification('SUCCESS', self, null);
                        scheduleData.timeslots[self.attr('value')] = returnData.booking;
                        if (<%= activeRole.equals(Role.STUDENT)%>) {
                            appendViewBookingPopover(self);
                        } else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
                            appendPopovers();
                        }
                        bookingDiv.show('clip', 'slow');
                        return false;
                    });

                    //Delete Booking Button
                    $('.timeslotCell').off('click', '#deleteBookingBtn');
                    $('.timeslotCell').on('click', '#deleteBookingBtn', function(e) {
						e.stopPropagation();
						bootbox.confirm({
							title: "Delete Booking",
							message: "Are you sure?",
							callback: function(result) {
								if (result) {
									var timeslot = self.parents('.timeslotCell');
									deleteBooking(timeslot);
									setTimeout(function(){showNotification("ERROR", timeslot, null);},500);
									timeslot.removeClass('bookedTimeslot');
									timeslot.addClass('unbookedTimeslot');
									timeslot.popover('destroy');
									delete scheduleData.timeslots[timeslot.attr('value')];
									scheduleData.timeslots[timeslot.attr('value')] = {id:timeslot.attr('id').split("_")[1], venue:"SIS Seminar Room 2-1", datetime: timeslot.attr('value')}; //TODO: Change SIS Seminar Room 2-1
									if (<%= activeRole.equals(Role.STUDENT)%>) {
										self.effect('clip', 'slow', function(){
											self.remove();
											var deletedDiv = $(document.createElement('div'))
												.addClass('deletedBookingOnTimeslot')
												.addClass('icon-info-sign');
											makeTooltip(deletedDiv, 'Removed by ' + "<%= user.getFullName() %>");
											timeslot.append(deletedDiv);
											appendCreateBookingPopover(timeslot);
											refreshScheduleData();
										});
									} else if (<%= activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)%>) {
										self.effect('clip', 'slow', function(){
											self.remove();
											refreshScheduleData();
											appendPopovers();
										});
									}
								}
							}
						});
                        return false;
                    });
                    
                    //Update Booking Button
                    $('.timeslotCell').off('click', '#updateBookingBtn');
                    $('.timeslotCell').on('click', '#updateBookingBtn', function(e) {
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
						refreshScheduleData();
                        return false;
                    });
					
                    //Update Timeslot Button
                    $('.timeslotCell').off('click', '#updateTimeslotBtn');
                    $('.timeslotCell').on('click', '#updateTimeslotBtn', function(e) {
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
                    $('.timeslotCell').off('click', '#availableTimeslotBtn');
                    $('.timeslotCell').on('click', '#availableTimeslotBtn', function(e) {
                        e.stopPropagation();
                        changeAvailability(self, true);
                        self.addClass('existingTimeslot');
                        showNotification("WARNING", self, "Set as available");
                        self.popover('destroy');
                        appendChangeAvailabilityPopover(self);
                        return false;
                    });
                    
                    //Set Unavailable
                    $('.timeslotCell').off('click', '#unavailableTimeslotBtn');
                    $('.timeslotCell').on('click', '#unavailableTimeslotBtn', function(e) {
                        e.stopPropagation();
                        changeAvailability(self, false);
                        self.addClass('existingTimeslot');
                        showNotification("WARNING", self, "Set as unavailable");
                        self.popover('destroy');
                        appendChangeAvailabilityPopover(self);
                        return false;
                    });
                    
                    //Datepicker
                    $('body').off('click', '#updateFormDate');
                    $('body').on('click', '#updateFormDate', function(e){
                        //Add date and timepickers
                        if (e.target === this) {
                            $(this)
								.datepicker({
									dateFormat: "yy-mm-dd"
								})
								.datepicker('setDate', Date.parse($(this).val()))
								.datepicker('show');
                        }
                        return false;
                    });
                    
                    //Timepicker
                    $('body').off('click', '#updateFormStartTime');
                    $('body').on('click', '#updateFormStartTime', function(e){
                        //Add timepicker
                        if (e.target === this) {
                            $(this)
								.timepicker({
									minTime: Date.parse(scheduleData.dayStartTime + ":00").toString("HH:mm"),
									maxTime: Date.parse(scheduleData.dayEndTime + ":00").addHours(-2).toString("HH:mm"),
									step: 30,
									forceRoundTime: true,
									timeFormat: 'H:i'
								});
							$(this).timepicker('setTime', $(this).val().split(" - ")[0]);
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
					
                    //Drag-Drop view functions
					$("#dragDropView").off('switch-change');
                    $("#dragDropView").on('switch-change', function(e, data){
						setTimeout(function(){
							//Timeout for animation of setting change
							dragDropView = data.value;
							populateSchedule(milestone, year, semester);
						}, 500);
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
                                    bodyTd.removeClass('availableTimeslot');
                                    bodyTd.addClass('unavailableTimeslot');
                                } else {
                                    bodyTd.removeClass('unavailableTimeslot');
                                    bodyTd.addClass("availableTimeslot");
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
												var $td = $(document.createElement('td')).addClass('timeslotCell');
												if (timeslot) {
													for (var t = 30; t < scheduleData.duration; t++) {
														rowspanArr.push(Date.parse(datetime).addMinutes(t).toString("yyyy-MM-dd HH:mm:ss"));
													}
													$td
														.attr('id', 'timeslot_' + timeslot.id)
														.attr('align', 'center')
														.attr('value', datetime)
														.attr('rowspan', scheduleData.duration/30)
														.addClass(timeslot.team?'bookedTimeslot':'unbookedTimeslot')
														.addClass(<%= activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY)%> && !timeslot.available?'unavailableTimeslot':'')
														.addClass(<%= activeRole.equals(Role.TA) %> && timeslot.taId !== undefined?loggedInTa === timeslot.taId?'taChosenTimeslot':'otherTATimeslot':'')
														.append(timeslot.team?
															$(document.createElement('div'))
																.addClass('booking pendingBooking')
																.addClass(timeslot.status.toLowerCase() + 'Booking')
																.addClass(
																	(<%= activeRole.equals(Role.FACULTY) %> && timeslot.isMyTeam)
																	|| (<%= activeRole.equals(Role.STUDENT) %> && timeslot.team === teamName)
																	|| (<%= activeRole.equals(Role.TA) %> && timeslot.taId !== undefined && loggedInTa === timeslot.taId)
																	?'myTeamBooking':false)
																.html(timeslot.team)
														:false)
														.append(timeslot.lastBookingWasRemoved?
															function(){
																var $removedDiv = $(document.createElement('div'));
																if (timeslot.lastBookingRejectReason) {
																	$removedDiv.addClass('rejectedBooking');
																	makeTooltip($td, 'Removed by ' + timeslot.lastBookingEditedBy);
																} else {
																	$removedDiv.addClass('deletedBookingOnTimeslot').addClass('icon-info-sign');
																	makeTooltip($removedDiv, 'Removed by ' + timeslot.lastBookingEditedBy);
																}
																return $removedDiv;
															}
															:false
														)
														;
												} else {
													$td.addClass('noTimeslot');
												}
												$tdCollection.push($td);
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
					var diffDays = Math.ceil(Math.abs(stopDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24));
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
				
				function initDragNDrop() {
					var originalTimeslots = {};
					$(".unbookedTimeslot").each(function(){
						if ($(this).data('droppable')) $(this).droppable('destroy');
					});
					$(".unbookedTimeslot").droppable({
						tolerance: 'intersect',
						drop: function(event, ui) {
							var $booking = ui.draggable;
							var $oldTimeslot = $booking.closest('.timeslotCell');
							var timeslot = scheduleData.timeslots[$oldTimeslot.attr('value')];
							var newDateTime = $(this).attr('value');
							var venue = timeslot.venue;
							var optionals = timeslot.optionals;
							var returnData = updateBooking($oldTimeslot, newDateTime, venue, optionals);
							if (returnData && returnData.success) {
								var $newTimeslot = $("#timeslot_" + returnData.booking.id);
								$newTimeslot.removeClass('unbookedTimeslot');
								$newTimeslot.addClass('bookedTimeslot');
								scheduleData.timeslots[$newTimeslot.attr('value')] = returnData.booking;
								$oldTimeslot.removeClass('bookedTimeslot');
								$oldTimeslot.addClass('unbookedTimeslot');
								delete scheduleData.timeslots[$oldTimeslot.attr('value')];
								scheduleData.timeslots[$oldTimeslot.attr('value')] = {
									id: $oldTimeslot.attr('id').split("_")[1],
									venue: venue, 
									datetime: $oldTimeslot.attr('value')
								};
								$booking.detach();
								$newTimeslot.append($booking);
								setTimeout(function(){showNotification("INFO", $oldTimeslot, null);}, 500);
								initDragNDrop();
							} else {
								showNotification("INFO", $oldTimeslot, returnData.message);
							}
//							bootbox.confirm({
//								title: "Update Booking?",
//								message: function(){
//									var message = "Team: <b>" + timeslot.team + "</b><br/>";
//									message += "Old Timeslot: <b style='color:red;'>" + Date.parse(timeslot.datetime).toString('dd-MMM-yy, HH:mm') + " - " + Date.parse(timeslot.datetime).addMinutes(scheduleData.duration).toString('HH:mm') + "</b><br/>";
//									message += "New Timeslot: <b>" + Date.parse(newDateTime).toString('dd-MMM-yy, HH:mm') + " - " + Date.parse(newDateTime).addMinutes(scheduleData.duration).toString('HH:mm') + "</b><br/>";
//									return message;
//								},
//								callback: function(result) {
//									if (result) {
//										var returnData = updateBooking($oldTimeslot, newDateTime, venue, optionals);
//										if (returnData && returnData.success) {
//											var $newTimeslot = $("#timeslot_" + returnData.booking.id);
//											$newTimeslot.removeClass('unbookedTimeslot');
//											$newTimeslot.addClass('bookedTimeslot');
//											scheduleData.timeslots[$newTimeslot.attr('value')] = returnData.booking;
//											$oldTimeslot.removeClass('bookedTimeslot');
//											$oldTimeslot.addClass('unbookedTimeslot');
//											delete scheduleData.timeslots[$oldTimeslot.attr('value')];
//											scheduleData.timeslots[$oldTimeslot.attr('value')] = {
//												id: $oldTimeslot.attr('id').split("_")[1],
//												venue: venue, 
//												datetime: $oldTimeslot.attr('value')
//											};
//											$booking.detach();
//											$newTimeslot.append($booking);
//											setTimeout(function(){showNotification("INFO", $oldTimeslot, null);}, 500);
//											initDragNDrop();
//										} else {
//											showNotification("INFO", $oldTimeslot, returnData.message);
//										}
//									}
//								}
//							});
						},
						out: function(event, ui) {
							ui.draggable.draggable('option', 'revert', function(){
								//Return to original position
								var datetime = $(this).closest('.timeslotCell').attr('value');
								if (!originalTimeslots[datetime]) return true;
								$(this).animate({
									top: '+=' + (originalTimeslots[datetime].start.top - $(this).offset().top),
									left: '+=' + (originalTimeslots[datetime].start.left - $(this).offset().left)
								});
								return false;
							});
						}
					});
					
					$(".booking").each(function(){
						if ($(this).data('draggable')) $(this).draggable('destroy');
					});
					$(".booking").draggable({
						start: function(event, ui) {
							//Register original position
							var datetime = $(this).closest('.timeslotCell').attr('value');
							console.log("Dragging: " + datetime);
							if (originalTimeslots[datetime] && originalTimeslots[datetime].start) return true;
							originalTimeslots[datetime] = {
								start: {
									top: $(this).offset().top,
									left: $(this).offset().left
								}
							};
						},
						stop: function(event, ui) {
							//Register stop position
							var datetime = $(this).closest('.timeslotCell').attr('value');
							originalTimeslots[datetime] = {
								stop: {
									top: $(this).offset().top,
									left: $(this).offset().left
								}
							};
						},
						revert: true
					});
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
                        hintText: "Enter email address...",
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
                        if (<%= activeRole.equals(Role.FACULTY) %> || (<%= activeRole.equals(Role.STUDENT) %> && timeslot.team !== teamName) || booking.is('.unavailableTimeslot')) {
                            opts["disabled"] = true;
                        }
                    }
                    booking.find('.optionalAttendees').tokenInput(users, opts);
                }
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
                        if (container.parents("tr").children().index(container.closest(".timeslotCell")) > 7) {
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
					trigger: 'hover',
                    title: title,
                    placement: function(){
                        if (container.parents("tr").children().index(container.closest(".timeslotCell")) > 7) {
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
