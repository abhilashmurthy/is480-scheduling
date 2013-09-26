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
			
			.venueLabel {
				vertical-align: top;
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
            
			#editScheduleSubmitBtn {
				margin-top: -15px;
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
				width: 82px;
				margin: auto;
			}
			
			.pillbox li {
				width: 82px;
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
										<td><input id="semesterInput" type="text" name="semester" placeholder="eg. <%= nextSem %>"/><div id="semesterNameAvailabilityChecker" class="statusText"></div></td><td><input id="editScheduleSubmitBtn" type="submit" value="Save" data-loading-text="Done" class="btn btn-primary"/></td>
									</tr>
								</table>
							</div>
							<!-- Edit Schedule -->
							<div id="editSchedulePanel" class="schedulePanel">
									<table id="editScheduleTable">
										<tr><th>Milestone</th><th colspan="2">Dates</th><th class="dayHours">Day Hours</th></tr>
										<tr id="editScheduleSubmitRow"><td></td></tr>
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
												<option value=""><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></option>
												<s:iterator value="termData">
													<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
												</s:iterator>
											</select>
										</td>
									</tr>
									<tr><td class="formLabelTd">Milestone</td><td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select></td></tr>
									<tr><td class="venueLabel formLabelTd">Venue</td>
										<td>
											<input id="venueInput" type="text" name="venue" placeholder="SIS Seminar Room 2-1"/>
											<button id="editTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Done">Save</button>
											<table class='timeslotsLegend'>
												<tr>
													<td class='legendBox' style="background-color:#B8F79E;"></td><td>Available</td>
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
                var activeSemesterDiplayNameStr = "<%= activeTerm.getDisplayName() %>";
				
				//For Edit Timeslots
				var selectedSchedule = null;
				
                loadInitialValues();
                
                function loadInitialValues() {
					$("#editScheduleTable").empty();
					$("#editScheduleTable").append("<tr id='editScheduleSubmitRow'><td></td></tr>");
					$("#milestoneTimeslotsSelect").empty();
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
//										beforeShowDay: $.datepicker.noWeekends,
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
											$ul.append($(document.createElement('li')).addClass('status-info').append(Date.parse(dates[j]).toString('dd MMM yy')));
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
													minTime: '00:00',
													maxTime: '11:00',
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
											.attr('value', '19:00')
											.addClass('scheduleDayTimeSelect timepicker')
											.timepicker({
													minTime: '12:00',
													maxTime: '23:00',
													step: 60,
													forceRoundTime: true,
													timeFormat: 'H:i',
													scrollDefaultTime: '19:00',
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
                 $(".scheduleLeftNav li a").on('click', function(){
					 var href = $(this).attr('href').split('#')[1];
					 $(".tab-pane, .nav-tabs li").removeClass('active');
					 $(".tab-pane").hide();
					 $("#" + href).addClass('active');
					 $(".nav-tabs ." + href).addClass('active');
					 $("#milestoneTimeslotsSelect").empty();
					 displayEditTimeslots();
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
					if (!semName) {
						$("#semesterNameAvailabilityChecker").empty();
						return false;
					}
					for (var i = 0; i < termNames.length; i++) {
						if (parseInt(activeAcademicYearStr) === parseInt(termNames[i].year) && semName.toLowerCase() === termNames[i].term.toLowerCase()) {
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
								//Date already selected for another milestone
								$(".milestoneOrder_" + orderNum).multiDatesPicker('removeDates', dates[j]);
								showNotification("WARNING", Date.parse(dateStr).toString("dd-MMM-yy") + " is already selected in " + $nextMilestone.attr('id').split("_")[1].toUpperCase());
								return false;
							}
							if (parseInt($nextMilestone.attr('class').split('milestoneOrder_')[1].substring(0, 1)) < orderNum && Date.parse(dateStr) < Date.parse($nextMilestone.multiDatesPicker('getDates')[$nextMilestone.multiDatesPicker('getDates').length - 1])) {
								//Date selected is before the max selected date of the previous milestone
								$(".milestoneOrder_" + orderNum).multiDatesPicker('removeDates', Date.parse(dateStr));
								var thisMilestone = $(".milestoneOrder_" + orderNum).attr('id').split("_")[1].toUpperCase();
								showNotification("WARNING", "Overlap Detected<br/> " + thisMilestone + " [" + Date.parse(dateStr).toString("dd-MMM-yy") + "] < " + $nextMilestone.attr('id').split("_")[1].toUpperCase() + " [" + Date.parse($nextMilestone.multiDatesPicker('getDates')[$nextMilestone.multiDatesPicker('getDates').length - 1]).toString('dd-MMM-yy') + "]");
								return false;
							}
							if (parseInt($nextMilestone.attr('class').split('milestoneOrder_')[1].substring(0, 1)) > orderNum && Date.parse(dateStr) > Date.parse($nextMilestone.multiDatesPicker('getDates')[0])) {
								//Date selected is after the min selected date of the next milestone
								$(".milestoneOrder_" + orderNum).multiDatesPicker('removeDates', Date.parse(dateStr));
								var thisMilestone = $(".milestoneOrder_" + orderNum).attr('id').split("_")[1].toUpperCase();
								showNotification("WARNING", "Overlap Detected<br/> " + thisMilestone + " [" + Date.parse(dateStr).toString("dd-MMM-yy") + "] > " + $nextMilestone.attr('id').split("_")[1].toUpperCase() + " [" + Date.parse($nextMilestone.multiDatesPicker('getDates')[0]).toString('dd-MMM-yy') + "]");
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
						dates.sort();
						for (var i = 0; i < dates.length; i++) {
							$pillBox.append($(document.createElement('li')).addClass('status-info').append(Date.parse(dates[i]).toString('dd MMM yy')));
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
					
                    $.ajax({
                        type: 'POST',
                        url: 'updateScheduleJson',
                        data: {jsonData: JSON.stringify(editScheduleData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
//                            schedules = response.schedules;
                            showNotification("SUCCESS", "Updated dates successfully");
							if ($("#semesterNameAvailabilityChecker").is(":visible")) {
								$(".termPicker").children(":contains('" + activeSemesterStr + "')").text(activeSemesterDiplayNameStr.split(" ")[0] + " " + term);
							}
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
					$("#milestoneTimeslotsSelect").val(schedules[0].milestoneName).change();
                    resetTimeslots(schedules[0].milestoneName, activeAcademicYearStr, activeSemesterStr); //Select first milestone
                }
				
				function resetTimeslots(milestone, year, semester) {
					$(".timeslotsTable").empty();
					selectedSchedule = getScheduleData(milestone, year, semester);
					makeTimeslotTable("timeslotsTable", selectedSchedule.timeslots, selectedSchedule.startDate, selectedSchedule.endDate, selectedSchedule.dayStartTime, selectedSchedule.dayEndTime);
					populateTimeslotsTable(selectedSchedule.timeslots, selectedSchedule.duration);
					$("#venueInput").attr('placeholder', 'Overwrite venue...');
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
                    var inputData = $("div.start-marker, div.defaultadd-marker", ".timeslotsTable").get();
                    for (var i = 0; i < inputData.length; i++) {
                        var obj = inputData[i];
                        timeslots_array.push($(obj).parent().attr("value"));
                    }
                    timeslotsData["scheduleId"] = selectedSchedule.id;
                    timeslotsData["timeslots[]"] = timeslots_array;
                    timeslotsData["venue"] = $("#venueInput").val();
                    console.log('Timeslots data is: ' + JSON.stringify(timeslotsData));
                    $.ajax({
                        type: 'POST',
                        url: 'updateTimeslotsJson',
                        data: {jsonData: JSON.stringify(timeslotsData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            console.log("editTimeslotsJson was successful");
							showNotification("SUCCESS", response.message);
                        } else {
                            showNotification("ERROR", response.message);
                        }
                    }).fail(function(error) {
                        console.log("editTimeslotsJson AJAX FAIL");
                        showNotification("ERROR", "Oops.. something went wrong");
                    });
					$("#editTimeslotsSubmitBtn").button('reset');
                    return false;
                });
                
                
                function makeTimeslotTable(tableClass, timeslots, startDate, endDate, dayStart, dayEnd) {
                    var thead = $(document.createElement("thead"));
					var dateArray = null;
					
					// 1 -- Get dates from getScheduleAction timeslots
					var datesHashSet = new HashSet();
					for (var i = 0; i < timeslots.length; i++) {
						datesHashSet.add(Date.parse(timeslots[i].datetime).toString("yyyy-MM-dd"));
					}
					dateArray = datesHashSet.values().sort();

					// 2 -- Get dates from startdate and enddate
//					dateArray = getDateArrayBetween(startDate, endDate);
					
					// 3 -- Get dates from datepickers
//					var milestoneName = $("#milestoneTimeslotsSelect").val();
//					dateArray = $("#milestone_" + milestoneName.toLowerCase()).multiDatesPicker('getDates');

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
                    triggerTimeslot($(this));
                    return false;
                });
                
				//Method to select timeslots on a table
                function triggerTimeslot($timeslotCell) {
                    if (!$timeslotCell.hasClass('timeslotcell')) return false;
					var slotSize = selectedSchedule.duration / 30;
					if ($timeslotCell.hasClass('chosen')) {
						//Unselect a timeslot
						var $prevTr = $timeslotCell.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($timeslotCell.index()).children('div.start-marker').length) {
								//Unselect this timeslot
								var $nextTr = $prevTr;
								for (var j = 0; j < slotSize; j++) {
									$nextTr.children().eq($timeslotCell.index()).removeClass('chosen').empty();
									$nextTr = $nextTr.next();
								}
								break;
							}
							$prevTr = $prevTr.prev();
						}
					} else {
						//Select a timeslot
						var foundOverlapping = false;
						var $nextTr = $timeslotCell.closest('tr');
						if ($nextTr.parent().children().index($nextTr) + slotSize > $nextTr.parent().children().length) return false; //Invalid timeslot
						$timeslotCell.append($(document.createElement('div')).addClass('start-marker'));
						for (var i = 0; i < slotSize; i++) {
							$nextTr.children().eq($timeslotCell.index()).addClass('chosen');
							$nextTr = $nextTr.next();
							if (i !== slotSize - 1 && $nextTr.children().eq($timeslotCell.index()).children('div.start-marker').length) {
								//Uh oh, overlapping timeslot detected. Let's remove it.
								var $overlappingTimeslot = $nextTr.children().eq($timeslotCell.index());
								triggerTimeslot($overlappingTimeslot);
								foundOverlapping = true;
							}
						}
						if (foundOverlapping) {
							//Actually, let's reset every consequent timeslot
							var $nextTr = $timeslotCell.closest('tr');
							while ($nextTr.parent().children().index($nextTr) + slotSize <= $nextTr.parent().children().length) {
								if (!$nextTr.children().eq($timeslotCell.index()).hasClass('chosen')) {
									var $consequentOverlappingTimeslot = $nextTr.children().eq($timeslotCell.index());
									triggerTimeslot($consequentOverlappingTimeslot);
								}
								$nextTr = $nextTr.next();
							}
						}
					}
					return false;
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
										$this.append($(document.createElement('div')).addClass('defaultadd-marker'));
										var $nextTr = $this.closest('tr');
										for (var k = 0; k < slotSize; k++) {
											$nextTr.children().eq($this.index()).addClass('teamExists');
											var $nextTr = $nextTr.next();
										}
									} else {
										triggerTimeslot($(this));
									}
									break;
								}
							}
						} else {
							//If schedule not yet edited, populate all timeslots
							triggerTimeslot($(this));
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
					   width: "350px",
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
