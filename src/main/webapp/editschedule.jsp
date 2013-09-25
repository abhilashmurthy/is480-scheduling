<%-- 
    Document   : AcceptReject
    Updated on : Jul 2, 2013, 11:14:06 PM
    Author     : Prakhar
--%>

<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Edit Schedule</title>
        <style type="text/css">
            table {
                table-layout: fixed;
            }
            
            #editTermTable, #editScheduleTable, #editTimeslotsTable {
                margin-left: 20px;
            }

            th {
                font-size: 18px;
                height: 25px;
                padding: 10px;
                text-align: center;
                /*border-bottom: 1px solid black;*/
            }

            td {
                padding: 10px;
                text-align: left;
            }

            .formLabelTd {
                font-size: 16px;
                color: darkblue;
                padding-bottom: 20px;
            }

            .submitBtnRow {
                border-bottom: none;
            }

            #allChkRow {
                border-bottom: 1px solid black;
            }

            #timeColumn {
                border-right: 1px solid black;
            }

            .start-marker { /* Triangle marker for the start of a timeslot */
                width: 0;
                height: 0;
                border-left: 5px solid transparent;
                border-right: 5px solid transparent;
                border-top: 10px solid #5C7AFF;
                z-index: 1;
            }
            .chosen {
                background-color: #B8F79E !important ;
            }
            
            .editScheduleTabList {
                position: relative;
                /*padding-top: 50px;*/
                height: 100%;
            }
            
            .editScheduleTab {
                width: 180px;
            }
            
            .editScheduleTabList li a, .editScheduleTabList li p {
                font-size: 20px;
                font-weight: bold;
                padding: 20px 0px 20px 5px !important;
            }
            
            .schedulePanel {
                padding-left: 5%;
            }
            
            html, body, .container {
                height: 100%;
            }
            
            .scheduleLeftNav {
                height: 100%;
            }
            
            .tab-content {
                padding-top: 50px;
            }
			
			.fuelux .spinner input {
				width: 206px !important;
			}
			
			input {
				margin-top: -5px;
			}
			
			.statusText {
				display: inline-table;
				margin-left: 10px;
				margin-top: -3px;
			}
			
			.pillbox {
				width: 80px;
				margin: auto;
			}
			
			.dayHours {
				padding-left: 20px;
			}
			
			.scheduleDayTimePoint {
				display: block;
				vertical-align: middle;
				padding-bottom: 10px;
				padding-left: 20px;
			}
			
            .scheduleDayTimeSelect {
                width: 45px;
				margin-bottom: 0 !important;
            }
			
        </style>
    </head>
    <body>
        <!-- Navbar -->
        <%@include file="navbar.jsp" %>

        <!-- Kick unauthorized user -->
        <%
            if (activeRole != Role.ADMINISTRATOR && activeRole != Role.COURSE_COORDINATOR) {
                request.setAttribute("error", "You need administrator privileges for this page");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
        %>
        
        <!-- Edit Term Suggestion Code -->
        <% 
            Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
            String activeSem = activeTerm.getSemester();
            String nextSem = activeSem;
            String nextYear = "" + (activeTerm.getAcademicYear());
            int semNum = -1;
            String letter = null;
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(activeSem);
            if (m.find()) {
                semNum = Integer.parseInt(m.group());
            }
            p = Pattern.compile("[A-B]");
            m = p.matcher(activeSem);
            if (m.find()) {
                letter = m.group();
            }

            if (letter != null) {
                if (semNum == 3){
                    nextYear = "" + ((activeTerm.getAcademicYear()) + 1);
                    nextSem = "Term 1";
                } else if (letter.equals("B")) {
                    nextSem = "Term " + (++semNum);
                } else if (letter.equals("A")) {
                    nextSem = "Modified Term " + semNum + "B";
                }
            } else {
                nextSem = "Modified Term " + semNum + "A";
            }
        %>

        <!-- Edit Schedule Container -->
        <div id="editSchedulePage" class="container">            
            <div class="editScheduleTabList tabbable tabs-left">
                <ul class="scheduleLeftNav nav nav-tabs">
                    <li class="emptyHiddenTab">
                        <p></p>
                    </li>
                    <li class="editScheduleTab active">
                        <a href="#editScheduleTab" data-toggle="tab">Edit Schedule</a>
                    </li>
                    <li class="editTimeslotsTab">
                        <a href="#editTimeslotsTab" data-toggle="tab">Edit Timeslots</a>
                    </li>
                </ul>
                <div class="tab-content editScheduleTabContent">
                    <div class="tab-pane active" id="editScheduleTab">
                        <!-- Edit Term -->
						<form id="editScheduleForm" method="POST">
							<div id="editTermPanel" class="schedulePanel">
								<h3 id="editScheduleTitle">Edit Schedule</h3>
								<table id="editTermTable">
									<tr>
									<td class="formLabelTd">Select Term</td>
									<td>
										<select class="termPicker" name="termId" onchange="this.form.submit()">
											<option value='<%= ((Term)session.getAttribute("currentActiveTerm")).getId() %>'><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></option>
											<s:iterator value="termData">
												<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
											</s:iterator>
										</select>
									</td>
									</tr>
									<tr>
										<td class="formLabelTd">Semester Name</td>
										<td><input id="semesterInput" type="text" name="semester" placeholder="eg. <%= nextSem %>"/><div id="semesterNameAvailabilityChecker" class="statusText"></div></td>
									</tr>
								</table>
							</div>
							<!-- Edit Schedule -->
							<div id="editSchedulePanel" class="schedulePanel">
									<table id="editScheduleTable">
										<tr><th>Milestone</th><th colspan="2">Dates</th><th class="dayHours">Day Hours</th></tr>
										<tr id="editScheduleSubmitRow"><td></td><td><input id="editScheduleSubmitBtn" type="submit" value="Edit" data-loading-text="Done" class="btn btn-primary"/></td></tr>
									</table>
								<h4 id="scheduleResultMessage"></h4>
							</div>
						</form>
                    </div>
                    <div class="tab-pane" id="editTimeslotsTab">
                        <!-- Edit Timeslots -->
                        <div id="editTimeslotsPanel" class="schedulePanel">
                            <h3 id="editTimeslotsTitle">Edit Timeslots</h3>		
								<table id="editTimeslotsTable">
									<tr>
										<td class="formLabelTd">Select Term</td>
										<td>
											<select class="termPicker" id="editTimeslotsTermId" name="editTimeslotsTermId">
												<option value='<%= ((Term)session.getAttribute("currentActiveTerm")).getId() %>'><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></option>
												<s:iterator value="termData">
													<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
												</s:iterator>
											</select>
										</td>
									</tr>
									<tr><td class="formLabelTd">Milestone</td><td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select></td></tr>
									<tr><td class="formLabelTd">Venue</td>
										<td>
											<input id="venueInput" type="text" name="venue" placeholder="SIS Seminar Room 2-1"/>
											<button id="editTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Done">Edit</button>
											<table class='timeslotsLegend'>
												<tr>
													<td class='legendBox' style="background-color:#B8F79E;"><div class='start-marker'></div></td><td>Available</td>
													<td class='legendBox' style="background-color:#faf4a8;"></td><td>Team Booking</td>
												</tr>
											</table>
										</td>
									</tr>
									<tr><td></td><td><table class="timeslotsTable table-condensed table-hover table-bordered table-striped"></table></td></tr>
								</table>	
                        </div>
                    </div>
                </div>				
            </div>
        </div>
        <%@include file="footer.jsp" %>
		<script type="text/javascript" src="js/plugins/jshashtable-3.0.js"></script>
		<script type="text/javascript" src="js/plugins/jshashset-3.0.js"></script>
		<script type="text/javascript" src="js/plugins/jquery-ui.multidatespicker.js"></script>
        <script type="text/javascript">
            editScheduleLoad = function() {                
                //Initialize variables
                var termNames = null;
				var schedules = null;
                var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                var activeSemesterStr = "<%= activeTerm.getSemester()%>";
				
				//For Edit Timeslots
				var selectedSchedule = null;
				
                loadInitialValues();
                
                function loadInitialValues() {
                    $("#semesterInput").val(activeSemesterStr);
					termNames = JSON.parse('<s:property escape="false" value="termNameJson"/>');
					schedules = JSON.parse('<s:property escape="false" value="scheduleJson"/>');
                    displayEditSchedules();
					displayEditTimeslots();
                }
				
                function displayEditSchedules() {
					//Get milestones and sort by order
					schedules = schedules.sort(function(a, b) {
						return (a["milestoneOrder"] > b["milestoneOrder"]) ? 1 : ((a["milestoneOrder"] < b["milestoneOrder"]) ? -1 : 0);
					});
					
					//Draw and populate milestones' datepickers and timepickers
                    for (var i = 0; i < schedules.length; i++) {
                        var schedule = schedules[i];
						var dates = schedule.dates;
                        var milestoneTr = $(document.createElement('tr'));
                        //Milestone name
                        var milestoneTd = $(document.createElement('td'))
								.addClass('formLabelTd')
								.html(schedule.milestoneName);
                            milestoneTr.append(milestoneTd);
                        var milestoneDatesTd = $(document.createElement('td'))
							.append(
								//Milestone dates[] MultiDatesPicker
								$(document.createElement('div'))
									.attr('name', schedule.milestoneName.toLowerCase() + "Dates")
									.attr('id', "milestone_" + schedule.milestoneName.toLowerCase())
									.attr('class', "milestoneOrder_" + schedule.milestoneOrder)
									.addClass('datepicker')
									.multiDatesPicker({
										dateFormat: "yy-mm-dd",
										defaultDate: dates.length > 0?dates[0]:Date.today(),
										minDate: Date.today(),
										beforeShowDay: $.datepicker.noWeekends,
										onSelect: function(date) {
											var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
											checkPickedDates(date, order);
											updatePillbox();
										}
									})
									.multiDatesPicker(dates.length > 0?'addDates':'resetDates', dates.length > 0?dates:'picked')
									.datepicker('refresh')
							);
						var milestonePillboxTd = $(document.createElement('td'))
							.addClass('fuelux')
							.append(
								//Milestone dates[] Pillbox
								$(document.createElement('div'))
									.attr('id', schedule.milestoneName.toLowerCase() + 'Pillbox')
									.addClass('pillbox')
									.append(function() {
										var $ul = $(document.createElement('ul'));
										for (var j = 0; j < dates.length; j++) {
											$ul.append($(document.createElement('li')).addClass('status-info').append(Date.parse(dates[j]).toString('dd MMM')));
										}
										return $ul;
									})
									.pillbox()
							);
                        milestoneTr.append(milestoneDatesTd).append(milestonePillboxTd);
                        var milestoneDayTimeTd = $(document.createElement('td'))
							.append(
								//Milestone Day Start
								$(document.createElement('span'))
									.addClass('scheduleDayTimePoint')
									.append("From ")
									.append(
										$(document.createElement('input'))
											.attr('type', 'text')
											.attr('id', "milestoneDayStart_" + schedule.milestoneName.toLowerCase())
											.attr('name', schedule.milestoneName + "DayStartTime")
											.attr('value', '09:00')
											.addClass('scheduleDayTimeSelect timepicker')
											.timepicker({
													minTime: '07:00',
													maxTime: '16:00',
													step: 60,
													forceRoundTime: true,
													timeFormat: 'H:i',
													scrollDefaultTime: '09:00',
													disableTextInput: true
											})
											.val(Date.parse(schedule.dayStartTime + ":00").toString("HH:mm")).change()
									)
							)
							.append(
								//Milestone Day End
								$(document.createElement('span'))
									.addClass('scheduleDayTimePoint')
									.append("To ")
									.append(
										$(document.createElement('input'))
											.attr('type', 'text')
											.attr('id', "milestoneDayEnd_" + schedule.milestoneName.toLowerCase())
											.attr('name', schedule.milestoneName.toLowerCase() + "DayEndTime")
											.attr('value', '18:00')
											.addClass('scheduleDayTimeSelect timepicker')
											.timepicker({
													minTime: '09:00',
													maxTime: '18:00',
													step: 60,
													forceRoundTime: true,
													timeFormat: 'H:i',
													scrollDefaultTime: '18:00',
													disableTextInput: true
											})
											.val(Date.parse(schedule.dayEndTime + ":00").toString("HH:mm")).change()
									)
							);
                        milestoneTr.append(milestoneDayTimeTd);
                        milestoneTr.insertBefore('#editScheduleSubmitRow');
                    }
                }
				
                /*----------------------------------------
                 NAV
                 ------------------------------------------*/
                 
				 //Manual navigation because of struts URL
                $(".editTimeslotsTab a").removeAttr('data-toggle');
                 $(".scheduleLeftNav li a").on('click', function(){
					 var href = $(this).attr('href').split('#')[1];
					 $(".tab-pane, .nav-tabs li").removeClass('active');
					 $(".tab-pane").hide();
					 $("#" + href).addClass('active');
					 $(".nav-tabs ." + href).addClass('active');
					 $("#" + href).show();
					 return false;
                 });

                /*----------------------------------------
                 EDIT TERM
                 ------------------------------------------*/
				
				//Term name availability check
				$("#semesterInput").on('keyup', function(){
					$("#semesterNameAvailabilityChecker").css('color', 'grey').html($(document.createElement('span')).addClass('icon-refresh icon-spin'));
					return false;
				});
				
				//Term name availability check
				$("#semesterInput").on('change blur', function(){
					var semName = $.trim($("#semesterInput").val());
					var yearVal = $.trim($("#yearInput").val());
					if (!semName) {
						$("#semesterNameAvailabilityChecker").empty();
						return false;
					}
					for (var i = 0; i < termNames.length; i++) {
						if (parseInt(yearVal) === parseInt(termNames[i].year) && semName.toLowerCase() === termNames[i].term.toLowerCase()) {
							$("#semesterNameAvailabilityChecker").css('color', 'red').html($(document.createElement('span')).addClass('icon-remove')).append(' Term name already exists');
							return false;
						}
					}
					$("#semesterNameAvailabilityChecker").css('color', 'green').html($(document.createElement('span')).addClass('icon-ok'));
					return false;
				});
                
                /*----------------------------------------
                 EDIT SCHEDULE
                 ------------------------------------------*/
                
                //Reset Dates On Adding/Removing from multiDatesPicker
                function checkPickedDates(dateStr, orderNum) {
					for (var i = 1; i > 0; i++) {
						var $nextMilestone = $(".milestoneOrder_" + i);
						if (!$nextMilestone.length) break; //No more milestones
						if (i === orderNum) continue; //Same milestone
						var dates = $nextMilestone.multiDatesPicker('getDates');
						for (var j = 0; j < dates.length; j++) {
							if (dates[j] === dateStr) {
								$(".milestoneOrder_" + orderNum).multiDatesPicker('removeDates', dates[j]);
								showNotification("WARNING", Date.parse(dateStr).toString("dd MMM") + " is already selected in " + $nextMilestone.attr('id').split("_")[1].toUpperCase());
								return false;
							}
						}
					}
                }
				
				//Reset Dates on crossing from Pillbox
				$(".pillbox ul").on('click', function(e){
					var $pill = $(e.target);
					if (!$pill.is('li')) return false;
					var date = $pill.text();
					var milestone = $pill.parents('.pillbox').attr('id').split('Pillbox')[0];
					var $datepicker = $("#milestone_" + milestone);
					$datepicker.multiDatesPicker('removeDates', Date.parse(date));
					var order = parseInt($datepicker.attr('class').split(" ")[0].split("_")[1]);
					checkPickedDates(date, order);
					updatePillbox();
				});
				
				function updatePillbox() {
					$(".datepicker").each(function(){
						var milestone = $(this).attr('id').split("_")[1];
						var $pillBox = $("#" + milestone + "Pillbox ul");
						$pillBox.empty();
						var dates = $("#milestone_" + milestone).multiDatesPicker('getDates');
						if (dates.length > 0) $pillBox.parent().css('opacity', '100'); else $pillBox.parent().css('opacity', '0');
						for (var i = 0; i < dates.length; i++) {
							$pillBox.append($(document.createElement('li')).addClass('status-info').append(Date.parse(dates[i]).toString('dd MMM')));
						}
					});
				}
				
                //Reset Start and End Times
				$(".timepicker").on('change', function(){
					var selectedTime = $(this).val();
					var milestone = $(this).attr('id').split('_')[1];
					var thisPoint = $(this).attr('id').split('Start').length > 1?"start":"end";
					resetDisabledTimes(selectedTime, milestone, thisPoint);
				});
				
				function resetDisabledTimes(selectedTime, milestone, thisPoint) {
					if (thisPoint === "start") {
						//Reset end point timepicker
						var $endPoint = $("#milestoneDayEnd_" + milestone);
						$endPoint.timepicker('option', 'minTime', Date.parse(selectedTime).addHours(2).toString('HH:mm'));
					} else {
						//Reset start point timepicker
						var $startPoint = $("#milestoneDayStart_" + milestone);
						$startPoint.timepicker('option', 'maxTime', Date.parse(selectedTime).addHours(-2).toString('HH:mm'));
					}
				}

                //Edit Schedule Submit - Show timeslots panel
                $("#editScheduleForm").on('submit', function(e) {
                    $("#editScheduleSubmitBtn").button('loading');
                    e.preventDefault();
                    e.stopPropagation();                  
					
					//Validate year and semester
					var year = activeAcademicYearStr;
					var term = $("#semesterInput").val();
					if (year === null || term === null || !term.length) {
						showNotification("WARNING", "Please enter a year and semester name");
						$("#editScheduleSubmitBtn").button('reset');
						return false;
					}
					
					//Validate dates and times
					var milestoneArray = $(this).serializeArray();
                    for (var i = 0; i < milestoneArray.length; i++) {
                        var milestoneItem = milestoneArray[i];
                        for (var j = 0; j < schedules.length; j++) {
                            var schedule = schedules[j];
							var dates = $("#milestone_" + schedule.milestoneName.toLowerCase()).multiDatesPicker('getDates');
							if (dates.length === 0) {
								showNotification("WARNING", "Please pick dates for milestone: " + schedule.milestoneName);
								$("#editScheduleSubmitBtn").button('reset');
								return false;
							}
							schedule["dates[]"] = dates;
                            if (milestoneItem.name.split("DayStartTime")[0].toLowerCase() === schedule.milestoneName.toLowerCase()) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", "Please select valid times for milestone: " + schedule.milestoneName);
                                    $("#editScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                schedule["dayStartTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                            if (milestoneItem.name.split("DayEndTime")[0].toLowerCase() === schedule.milestoneName.toLowerCase()) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", "Please select valid times for milestone: " + schedule.milestoneName);
                                    $("#editScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                schedule["dayEndTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                        }
                    }
					
					//Validate timepicker range
                    var wrongTime = false;
                    $(".timepicker").each(function(){
                        var id = $(this).attr('id');
                        var endTimeSelect = id.split("milestoneDayEnd_")[1];
                        if (endTimeSelect) {
                            var startTimeVal = $("#milestoneDayStart_" + endTimeSelect).val();
                            if (Date.parse(startTimeVal) > Date.parse($(this).val()).addHours(-2)) {
                                showNotification("WARNING", "Start time should be at least 2 hours less than end time");
                                $("#editScheduleSubmitBtn").button('reset');
                                wrongTime = true;
                            }
                        }
                    });
                    if (wrongTime) return false;
					
					//Everything OK
                    var editScheduleData = {
						semester: term,
						schedules: schedules
					};
					
					console.log("editScheduleData is: " + JSON.stringify(editScheduleData));
                    $.ajax({
                        type: 'POST',
                        url: 'updateScheduleJson',
                        data: {jsonData: JSON.stringify(editScheduleData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            schedules = response.schedules;
                            showNotification("SUCCESS", "Edited dates successfully");
                        } else {
							showNotification("WARNING", response.message);
							$("#editScheduleSubmitBtn").button('reset');
                        }
                    }).fail(function(error) {
                        console.log("editScheduleData AJAX FAIL");
						var eid = btoa("Erro in EditScheduleAction: Escalate to developers!");
						window.location = "error.jsp?eid=" + eid;
                    });
					$("#editScheduleSubmitBtn").button('reset');
                    return false;
                });

                /*----------------------------------------
                 EDIT TIMESLOTS
                 ------------------------------------------*/

                //Display edit timeslots
                function displayEditTimeslots() {
                    for (var i = 0; i < schedules.length; i++) {
                        var schedule = schedules[i];
                        var milestoneOption = $(document.createElement('option'));
						milestoneOption.attr('value', schedule.milestoneName);
						milestoneOption.html(schedule.milestoneName);
                        $("#milestoneTimeslotsSelect").append(milestoneOption);
                    }
                    resetTimeslots(schedules[0].milestoneName, activeAcademicYearStr, activeSemesterStr); //Select first milestone
                }
				
				function resetTimeslots(milestone, year, semester) {
					$(".timeslotsTable").empty();
					selectedSchedule = getScheduleData(milestone, year, semester);
					makeTimeslotTable("timeslotsTable", selectedSchedule.timeslots, selectedSchedule.startDate, selectedSchedule.endDate, selectedSchedule.dayStartTime, selectedSchedule.dayEndTime);
					populateTimeslotsTable(selectedSchedule.timeslots, selectedSchedule.duration);
					//Set venue to the venue present in the largest number of timeslots
					var venuesSet = new HashSet();
					for (var i = 0; i < selectedSchedule.timeslots.length; i++) {
						venuesSet.add(selectedSchedule.timeslots[i].venue);
					}
					var venueCount = {};
					for (var i = 0; i < venuesSet.values().length; i++) {
						venueCount[venuesSet.values()[i]] = 0;
					}
					for (var i = 0; i < selectedSchedule.timeslots.length; i++) {
						venueCount[selectedSchedule.timeslots[i].venue] = ++venueCount[selectedSchedule.timeslots[i].venue];
					}
					var maxVenue = function(){
						var max = 0;
						var venue = null;
						for (var key in venueCount) {
							if (venueCount.hasOwnProperty(key)) {
								if (venueCount[key] > max) {
									max = venueCount[key];
									venue = key;
								}
							}
						}
						return venue;
					};
					$("#venueInput").val(maxVenue).change();
				}

				$("#editTimeslotsTermId").on('change', function(e){
					$.ajax({
						url: 'editSchedule',
						data: {termId: $(this).val()}
					});
					var selectedTerm = $(this).val();
					var selectedMilestone = $("#milestoneTimeslotsSelect").val();
					resetTimeslots(selectedMilestone, parseInt(selectedTerm.split("_")[0]), selectedTerm.split("_")[1]);
					return false;
				});
                
				//Reset timeslots on change milestone dropdown
                $("#milestoneTimeslotsSelect").on('change', function(e){
                    var selectedMilestone = $(this).val();
					resetTimeslots(selectedMilestone, activeAcademicYearStr, activeSemesterStr);
                    return false; 
                });
                
                //Submit Update Timeslots
                $("#editTimeslotsSubmitBtn").on('click', function() {
                    $("#editTimeslotsSubmitBtn").button('loading');
                    var timeslotsData = {};
                    var timeslots_array = new Array();
                    var inputData = $("div.start-marker", ".timeslotsTable").get();
                    for (var i = 0; i < inputData.length; i++) {
                        var obj = inputData[i];
                        timeslots_array.push($(obj).parent().attr("value"));
                    }
                    timeslotsData["scheduleId"] = selectedSchedule.id;
                    timeslotsData["timeslots"] = timeslots_array;
                    timeslotsData["venue"] = $("#venueInput").val();
                    console.log('Timeslots data is: ' + JSON.stringify(timeslotsData));
//                    $.ajax({
//                        type: 'POST',
//                        url: 'editTimeslotsJson',
//                        data: {jsonData: JSON.stringify(timeslotsData)},
//                        dataType: 'json'
//                    }).done(function(response) {
//                        if (response.success) {
//                            console.log("editTimeslotsJson was successful");
//							//Set isUpdated to true
//                            selectedSchedule["isUpdated"] = true;
//							var totalUpdated = 0;
//							for (var i = 0; i < schedules.length; i++) {
//								if (schedules[i].isUpdated) ++totalUpdated;
//							}
//							$("#timeslotsProgressBar").children(".bar").css('width', ((totalUpdated/schedules.length) * 100) + '%');
//							if ((totalUpdated/schedules.length) === 1) {
//								//Go to manage active terms page
//								showNotification("WARNING", "Schedule ready now");
//								setTimeout(function(){window.location = "index";}, 2000);
//							} else {
//								//Select next milestone
//								showNotification("SUCCESS", response.message);
//								var nextOrder = null;
//								for (var i = 0; i < milestones.length; i++) {
//									if (milestones[i].name === selectedSchedule.milestoneName) {
//										 nextOrder = milestones[i].order + 1;
//										 break;
//									}
//								}
//								for (var i = 0; i < milestones.length; i++) {
//									if (milestones[i].order === nextOrder) {
//										 $("#milestoneTimeslotsSelect").val(milestones[i].name).change(); //Select next milestone
//										 break;
//									}
//								}
//							}
//                        } else {
//                            var eid = btoa(response.message);
//                            console.log(response.message);
//                            window.location = "error.jsp?eid=" + eid;
//                        }
//                    }).fail(function(error) {
//                        console.log("editTimeslotsJson AJAX FAIL");
//                        showNotification("ERROR", "Oops.. something went wrong");
//                    });
					$("#editTimeslotsSubmitBtn").button('reset');
                    return false;
                });
                
                
                function makeTimeslotTable(tableClass, timeslots, startDate, endDate, dayStart, dayEnd) {
                    var thead = $(document.createElement("thead"));
					var dateArray = null;
					
					//Get dates from timeslots -- This won't work because new timeslots are being added
					var datesHashSet = new HashSet();
					for (var i = 0; i < timeslots.length; i++) {
						datesHashSet.add(Date.parse(timeslots[i].datetime).toString("yyyy-MM-dd"));
					}
//					dateArray = datesHashSet.values().sort();
					//Get dates from startdate and enddate
					dateArray = getDateArrayBetween(startDate, endDate);

                    //Creating table header with dates
                    thead.append("<th></th>"); //Empty cell for time column
                    for (i = 0; i < dateArray.length; i++) {
                        var th = $(document.createElement("th"));
                        var headerVal = new Date(dateArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(dateArray[i]).toString('ddd');
                        th.html(headerVal);
                        thead.append(th);
                    }
                    //Inserting constructed table header into table
                    $("." + tableClass).append(thead);

                    //Creating table body with times and empty cells
                    var tbody = $(document.createElement("tbody"));

                    //Generating list of times
                    var timesArray = new Array();
                    for (var i = dayStart; i < dayEnd; i++) {
                        var timeVal = Date.parse(i + ":00:00");
                        timesArray.push(timeVal.toString("HH:mm"));
                        timeVal.addMinutes(30);
                        timesArray.push(timeVal.toString("HH:mm"));
                    }

                    //Constructing table body
                    for (i = 0; i < timesArray.length; i++) {
                        var tr = $(document.createElement("tr"));
                        var timeTd = $(document.createElement("td"));
                        timeTd.html(timesArray[i]);
                        tr.append(timeTd);

                        for (var j = 0; j < dateArray.length; j++) {
                            var td = $(document.createElement("td"));
                            td.addClass("timeslotcell");
                            var date = dateArray[j];
                            date = new Date(date).toString("yyyy-MM-dd");
                            var datetimeString = date + " " + timesArray[i] + ":00";
                            td.attr("value", datetimeString);
                            tr.append(td);
                        }
                        tbody.append(tr);
                    }

                    //Inserting constructed table body into table
                    $("." + tableClass).append(tbody);
                }
                
                $('body').on('click', '.timeslotcell', function(e) {
					if ($(this).is('.teamExists')) return false;
                    console.log("clicked timeslotcell");
                    triggerTimeslot(this, selectedSchedule.duration);
                    return false;
                });
                
				
                /*
                 * METHOD TO CHOOSE TIMESLOTS ON THE EDITD TABLE
                 */
                function triggerTimeslot(e, duration) {
                    if (!$(e).hasClass('timeslotcell')) return false;
                    var col = $(e).parent().children().index(e);
                    var tr = $(e).parent();
                    var row = $(tr).parent().children().index(tr);
                    var tbody = $(e).parents('.timeslotsTable').children('tbody');
                    var slotSize = duration / 30;

                    if ($(e).hasClass("chosen")) { //Section for a cell thats already highlighted
                        //Checking if the cell clicked is the start of the chosen timeslot (Important!)
                        if ($(e).children().index(".start-marker") !== -1) {
                            $(e).removeClass("chosen");
                            $(e).children().remove();
                            for (i = 1; i < slotSize; i++) {
                                var nextRow = $(tbody).children().get(row + i);
                                var nextCell = $(nextRow).children().get(col);
                                $(nextCell).removeClass("chosen");
                            }
                        }
                    } else { //Section for a non-highlighted cell
                        //Checking if there will be an overlap of timeslots
                        //Abort if there is going to be an overlap
                        for (i = 1; i < slotSize; i++) {
                            var nextRow = $(tbody).children().get(row + i);
                            var nextCell = $(nextRow).children().get(col);
                            if ($(nextCell).hasClass("chosen")) {
                                return;
                            }
                        }

                        var numRows = $(tbody).children().length;
                        //Checking if there are enough cells for the slot duration
                        if ((row + slotSize) <= numRows) {
                            $(e).addClass("chosen");
                            var marker = document.createElement("div");
                            $(marker).addClass("start-marker");
                            $(e).append(marker);
                            for (i = 1; i < slotSize; i++) {
                                var nextRow = $(tbody).children().get(row + i);
                                var nextCell = $(nextRow).children().get(col);
                                $(nextCell).addClass("chosen");
                            }
                        }
                    }
                }

                function populateTimeslotsTable(timeslots, duration) {
                    $(".timeslotcell").each(function() {
						var $this = $(this);
						if (timeslots) {
							//populate only selected timeslots
							for (var i = 0; i < timeslots.length; i++) {
								if ($this.attr('value') === timeslots[i].datetime) {
									if (timeslots[i].team) {
										//If team exists, don't clickable
										var slotSize = selectedSchedule.duration / 30;
										$this.css('text-align', 'center').html('Booking');
										var $nextTr = $this.closest('tr');
										for (var k = 0; k < slotSize; k++) {
											$nextTr.children().eq($this.index()).addClass('teamExists');
											var $nextTr = $nextTr.next();
										}
									} else {
										triggerTimeslot(this, duration);
									}
									break;
								}
							}
						} else {
							//If schedule not yet edited, populate all timeslots
							triggerTimeslot(this, duration);
						}
                    });
                }
				
                function getScheduleData(milestone, year, semester) {
                    var toReturn = null;
                    var data = {
                        year: year,
                        semester: semester
                    };
                    if (milestone) {
                        data["milestone"] = milestone;
                    }
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
				
                //Get dates between startDate and stopDate
                function getDateArrayBetween(startDate, stopDate) {
                    var dateArray = new Array();
                    startDate = Date.parse(startDate);
                    stopDate = Date.parse(stopDate);
                    var currentDate = startDate;
                    while (currentDate <= stopDate) {
                        dateArray.push(currentDate);
                        currentDate = new Date(currentDate).addDays(1);
                    }
                    return dateArray;
                }
				
				/**********************/
				/*   NOTIFICATIONS    */
				/**********************/
				
				function showNotification(action, message) {
					var opts = {
					   title: "Note",
					   text: message,
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
					switch (action) {
						case "WARNING":
							opts.type = "warning";
							opts.title = "Note";
							break;
						case "SUCCESS":
						   opts.type = "success";
						   opts.title = "Updated";
						   break;
						case "ERROR":
						   opts.type = "error";
						   opts.title = "Warning";
						   break;
						default:
							alert("Something went wrong - Notifications");
					}
				   $.pnotify(opts);
				}
				
            };
            
            addLoadEvent(editScheduleLoad);
        </script>
    </body>
</html>
