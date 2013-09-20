<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
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
        <title>IS480 Scheduling System | Create Schedule</title>
        <style type="text/css">
            table {
                table-layout: fixed;
            }
            
            #createTermTable, #createScheduleTable, #createTimeslotsTable {
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
            
            .createScheduleTabList {
                position: relative;
                /*padding-top: 50px;*/
                height: 100%;
            }
            
            .createScheduleTab {
                width: 180px;
            }
            
            .createScheduleTabList li a, .createScheduleTabList li p {
                font-size: 20px;
                font-weight: bold;
                padding: 20px 0px 20px 10px !important;
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
			
			#semesterNameAvailabilityChecker {
				display: inline-table;
				margin-left: 10px;
				margin-top: -3px;
			}
			
			.pillbox {
				border: 1px solid grey;
				border-radius: 10px;
				width: 80px;
				margin: auto;
			}
			
			.pillbox li {
				border-radius: 5px !important;
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
        
        <!-- Create Term Suggestion Code -->
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

        <!-- Create Schedule Container -->
        <div id="createSchedulePage" class="container">
            
            <div class="createScheduleTabList tabbable tabs-left">
                <ul class="scheduleLeftNav nav nav-tabs">
                    <li class="emptyHiddenTab">
                        <p></p>
                    </li>
                    <li class="createScheduleTab active">
                        <a href="#createScheduleTab" data-toggle="tab">Create Schedule</a>
                    </li>
                    <li class="createTimeslotsTab">
                        <a href="#createTimeslotsTab" data-toggle="tab">Create Timeslots</a>
                    </li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active" id="createScheduleTab">
                        <!-- Create Term -->
						<form id="createTermForm">
							<div id="createTermPanel" class="schedulePanel">
								<h3 id="createScheduleTitle">Create Schedule</h3>
								<table id="createTermTable">
									<tr>
										<td class="formLabelTd">Year</td>
										<td class="fuelux"> <!-- Putting default values for testing purposes -->
											<div id="yearSpinnerInput" class="spinner">
												<input id="yearInput" type="text" class="spinner-input"/>
												<div class="spinner-buttons  btn-group btn-group-vertical">
													<button class="btn spinner-up" type="button">
														<i class="icon-chevron-up"></i>
													</button>
													<button class="btn spinner-down" type="button">
														<i class="icon-chevron-down"></i>
													</button>
												</div>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formLabelTd">Semester Name</td>
										<td><input id="semesterInput" type="text" name="semester" placeholder="<%= nextSem %>"/><div id="semesterNameAvailabilityChecker"></div></td>
									</tr>
								</table>
							</div>
							<!-- Create Schedule -->
							<div id="createSchedulePanel" class="schedulePanel">
									<table id="createScheduleTable">
										<tr><th>Milestone</th><th colspan="2">Dates</th><th class="dayHours">Day Hours</th></tr>
										<tr id="createScheduleSubmitRow"><td></td><td><input id="createScheduleSubmitBtn" type="submit" value="Create" data-loading-text="Done" class="btn btn-primary"/></td></tr>
									</table>
								<h4 id="scheduleResultMessage"></h4>
							</div>
						</form>
                    </div>
                    <div class="tab-pane" id="createTimeslotsTab">
                        <!-- Create Timeslots -->
                        <div id="createTimeslotsPanel" class="schedulePanel">
                            <h3 id="createTimeslotsTitle">Create Timeslots</h3>
                            <table id="createTimeslotsTable">
                                <tr><td>Milestone</td><td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select></td></tr>
                                <tr><td>Venue</td><td><input id="venueInput" type="text" name="venue" placeholder="SIS Seminar Room 2-1"/></td><td><button id="createTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Done">Create</button></td></tr>
                                <tr><td></td><td><table class="timeslotsTable table-condensed table-hover table-bordered table-striped" hidden></table></td></tr>
                            </table>
                            <h4 id="timeslotResultMessage"></h4>
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <%@include file="footer.jsp" %>
        <script type="text/javascript" src="js/plugins/jquery-ui.multidatespicker.js"></script>
        <script type="text/javascript">
            createScheduleLoad = function() {                
                //Initialize variables
                var milestones = JSON.parse('<s:property escape="false" value="milestoneJson"/>');
                var termNames = JSON.parse('<s:property escape="false" value="termNameJson"/>');
                var schedules = null;
                var selectedSchedule = null;
                
                /*----------------------------------------
                 NAV
                 ------------------------------------------*/
                 
                $(".createTimeslotsTab a").removeAttr('data-toggle');
                 $(".scheduleLeftNav li a").on('click', function(){
                     if (!$(this).attr('data-toggle')) {
                        var activeTab = $('.scheduleLeftNav').children('.active').children('a').html();
                        showNotification("WARNING", "Please " + activeTab + " First!");
                     }
					 return false;
                 });

                /*----------------------------------------
                 CREATE TERM
                 ------------------------------------------*/
				
				//Spinner
				$("#yearSpinnerInput").spinner({
					value: parseInt('<%= nextYear %>'),
					min: 2013,
					max: 9999,
					step: 1
				});
				
				//Term name availability check
				$("#semesterInput").on('keyup', function(){
					$("#semesterNameAvailabilityChecker").css('color', 'grey').html($(document.createElement('span')).addClass('icon-refresh icon-spin'));
					return false;
				});
				
				$("#semesterInput, #yearSpinnerInput").on('change blur changed', function(){
					var semName = $.trim($("#semesterInput").val());
					var yearVal = $("#yearSpinnerInput").spinner('value');
					if (!semName) {
						$("#semesterNameAvailabilityChecker").empty();
						return false;
					}
					for (var i = 0; i < termNames.length; i++) {
						if (yearVal === termNames[i].year && semName === termNames[i].term) {
							$("#semesterNameAvailabilityChecker").css('color', 'red').html($(document.createElement('span')).addClass('icon-remove')).append(' Term name already exists');
							return false;
						}
					}
					$("#semesterNameAvailabilityChecker").css('color', 'green').html($(document.createElement('span')).addClass('icon-ok'));
					return false;
				});
                
                /*----------------------------------------
                 CREATE SCHEDULE
                 ------------------------------------------*/

				displayCreateSchedule();

                //Display Schedule
                function displayCreateSchedule() {
                
                    //Display Create Schedule
                    $("#createSchedulePanel").show();
                    $(".createScheduleTab a").attr('data-toggle', 'tab');
                        
                    //Order comparator
                    function compare(a, b) {
                        if (a.order < b.order) {
                            return -1;
                        } else if (a.order > b.order) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                        
                    milestones.sort(compare); //Sort by order first
                    for (var i = 0; i < milestones.length; i++) {
                        var milestone = milestones[i];
                        var milestoneTr = $(document.createElement('tr'));
                        //Milestone name
                        var milestoneTd = $(document.createElement('td'))
								.addClass('formLabelTd')
								.html(milestone.name);
                            milestoneTr.append(milestoneTd);
                        var milestoneDatesTd = $(document.createElement('td'))
							.append(
								//Milestone dates[] MultiDatesPicker
								$(document.createElement('div'))
									.attr('name', milestone.name.toLowerCase() + "Dates")
									.attr('id', "milestone_" + milestone.name.toLowerCase())
									.attr('class', "milestoneOrder_" + milestone.order)
									.addClass('datepicker')
									.multiDatesPicker({
										dateFormat: "yy-mm-dd",
										defaultDate: Date.today(),
										minDate: Date.today(),
										beforeShowDay: $.datepicker.noWeekends,
										onSelect: function(date) {
											var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
											resetDisabledDates(date, order);
											updatePillbox();
										}
									})
							);
						var milestonePillboxTd = $(document.createElement('td'))
							.addClass('fuelux')
							.append(
								//Milestone dates[] Pillbox
								$(document.createElement('div'))
									.attr('id', milestone.name.toLowerCase() + 'Pillbox')
									.css('opacity', '0')
									.addClass('pillbox')
									.append($(document.createElement('ul')))
									.pillbox()
							);
                        milestoneTr.append(milestoneDatesTd).append(milestonePillboxTd);
                        var milestoneDayTimeTd = $(document.createElement('td'))
							.append(
								//Milestone Day Start
								$(document.createElement('span'))
									.addClass('scheduleDayTimePoint')
									.append("Start ")
									.append(
										$(document.createElement('input'))
											.attr('type', 'text')
											.attr('id', "milestoneDayStart_" + milestone.id)
											.attr('name', milestone.name.toLowerCase() + "DayStartTime")
											.attr('value', '09:00')
											.addClass('scheduleDayTimeSelect')
											.timepicker({
													minTime: '07:00',
													maxTime: '18:00',
													step: 60,
													forceRoundTime: true,
													timeFormat: 'H:i',
													scrollDefaultTime: '09:00'
											})
									)
							)
							.append(
								//Milestone Day End
								$(document.createElement('span'))
									.addClass('scheduleDayTimePoint')
									.append("End ")
									.append(
										$(document.createElement('input'))
											.attr('type', 'text')
											.attr('id', "milestoneDayEnd_" + milestone.id)
											.attr('name', milestone.name.toLowerCase() + "DayEndime")
											.attr('value', '18:00')
											.addClass('scheduleDayTimeSelect')
											.timepicker({
													minTime: '07:00',
													maxTime: '18:00',
													step: 60,
													forceRoundTime: true,
													timeFormat: 'H:i',
													scrollDefaultTime: '18:00'
											})
									)
							);
                        milestoneTr.append(milestoneDayTimeTd);
                        milestoneTr.insertBefore('#createScheduleSubmitRow');
                    }
                    $(".createScheduleTab a").tab('show');
					disableDatePickers();
                }
				
				function disableDatePickers() {
					//Disabled subsequent
					$(".datepicker").each(function(i, e){
						if (i > 0) {
							$(this).multiDatesPicker('resetDates', 'picked');
							$(this).datepicker('option', 'maxDate', -2);
						}
					});
				}
                
                //Reset Dates On Change
                function resetDisabledDates(minDateStr, orderNum) {
					var isSelected = $(".milestoneOrder_" + orderNum).multiDatesPicker('gotDate', Date.parse(minDateStr));
					var $nextMilestone = $(".milestoneOrder_" + (orderNum + 1));
					if ($nextMilestone.length && isSelected) {
						//A date has been added
						//Reset all subsequent dates
						for (var i = orderNum + 1; i > 0; i++) {
							var $subseqMilestone = $(".milestoneOrder_" + i);
							if (!$subseqMilestone.length) break; //No more milestones
							$subseqMilestone.multiDatesPicker('resetDates', 'picked');
						}
						$nextMilestone.datepicker('destroy');
						var minDate = Date.parse(minDateStr);
						$nextMilestone.multiDatesPicker({
							dateFormat: "yy-mm-dd",
							defaultDate: minDate,
							minDate: minDate.addDays(1),
							beforeShowDay: $.datepicker.noWeekends,
							onSelect: function(date) {
								var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
								resetDisabledDates(date, order);
								updatePillbox();
							}
						});
					} else if ($nextMilestone.length) {
						// A date has been removed
						var dates = $(".milestoneOrder_" + orderNum).multiDatesPicker('getDates');
						if (dates.length > 0) {
							$nextMilestone.multiDatesPicker('resetDates', 'picked');
							$nextMilestone.datepicker('destroy');
							$nextMilestone.multiDatesPicker({
								dateFormat: "yy-mm-dd",
								defaultDate: Date.parse(dates[dates.length - 1]),
								minDate: Date.parse(dates[dates.length - 1]).addDays(1),
								beforeShowDay: $.datepicker.noWeekends,
								onSelect: function(date) {
									var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
									resetDisabledDates(date, order);
									updatePillbox();
								}
							});
						} else {
							//Reset all subsequent dates
							var nextOrder = parseInt($nextMilestone.attr('class').split(" ")[0].split("_")[1]);
							for (var i = nextOrder; i > 0; i++) {
								$nextMilestone = $(".milestoneOrder_" + i);
								if (!$nextMilestone.length) break; //No more milestones
								$nextMilestone.multiDatesPicker('resetDates', 'picked');
								$nextMilestone.datepicker('destroy');
								$nextMilestone.multiDatesPicker({
									dateFormat: "yy-mm-dd",
									defaultDate: Date.today(),
									minDate: Date.today(),
									beforeShowDay: $.datepicker.noWeekends,
									onSelect: function(date) {
										var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
										resetDisabledDates(date, order);
										updatePillbox();
									}
								});
								$nextMilestone.datepicker('option', 'maxDate', -2);
							}
						}
					}
                }
				
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
                $("body").on('mouseover', '.scheduleDayTimeSelect', function(e){
                    var id = $(this).attr('id');
                    var endTimeSelect = id.split("milestoneDayEnd_")[1];
                    if (endTimeSelect) {
                        var startTimeVal = $("#milestoneDayStart_" + endTimeSelect).val();
                        $(this).timepicker('remove');
                        $(this).timepicker({
                            minTime: Date.parse(startTimeVal).addHours(2).toString('HH:mm'),
                            maxTime: '21:00',
                            step: 60,
                            forceRoundTime: true,
                            timeFormat: 'H:i',
                            scrollDefaultTime: '18:00'
                       });
                    }
                    return false;
                });

                //Create Schedule Submit - Show timeslots panel
                $("#createScheduleForm").on('submit', function(e) {
                    $("#createScheduleSubmitBtn").button('loading');
                    e.preventDefault();
                    e.stopPropagation();
                    //AJAX call to save term and schedule dates
                    var milestoneArray = $(this).serializeArray();
                    var wrongDate = false;
                    $(".scheduleDayTimeSelect").each(function(){
                        var id = $(this).attr('id');
                        var endTimeSelect = id.split("milestoneDayEnd_")[1];
                        if (endTimeSelect) {
                            var startTimeVal = $("#milestoneDayStart_" + endTimeSelect).val();
                            if (Date.parse(startTimeVal) >= Date.parse($(this).val())) {
                                showNotification("WARNING", "Start time should be less than end time");
                                $("#createScheduleSubmitBtn").button('reset');
                                wrongDate = true;
                            }
                        }
                    });
                    if (wrongDate) return false;
                    var errorMessage = "Please type in dates for all milestones!";
                    for (var i = 0; i < milestoneArray.length; i++) {
                        var milestoneItem = milestoneArray[i];
                        for (var j = 0; j < milestones.length; j++) {
                            var milestone = milestones[j];
                            if (milestoneItem.name.split("Dates")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", errorMessage);
                                    $("#createScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dates[]"] = milestoneItem.value.split(",");
                            }
                            if (milestoneItem.name.split("DayStartTime")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", errorMessage);
                                    $("#createScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dayStartTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                            if (milestoneItem.name.split("DayEndTime")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", errorMessage);
                                    $("#createScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dayEndTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                        }
                    }
//                    console.log("Milestone array is now: " + JSON.stringify(milestones));
                    var createScheduleData = {
						year: $("#yearSpinnerInput").spinner('value'), 
						semester: $("#semesterInput").val(),
						"milestones[]":milestones};
                    $.ajax({
                        type: 'POST',
                        url: 'createScheduleJson',
                        data: {jsonData: JSON.stringify(createScheduleData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            console.log("Received: " + JSON.stringify(response));
                            schedules = response.schedules;
                            showNotification("SUCCESS", "Created dates successfully");
                            //Display create timeslots forms
                            setTimeout(function(){displayCreateTimeslots();}, 2000);
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        console.log("createScheduleData AJAX FAIL");
                        showNotification("ERROR", "Oops.. something went wrong");
//                        displayMessage("scheduleResultMessage", "Oops.. something went wrong", true);
                    });
                    return false;
                });

                /*----------------------------------------
                 CREATE TIMESLOTS
                 ------------------------------------------*/

                //Display create timeslots
                function displayCreateTimeslots() {
                    //Display create timeslots
                    $("#createTimeslotsPanel").show();
                    $(".createTimeslotsTab a").attr('data-toggle', 'tab');
                    for (var i = 0; i < schedules.length; i++) {
                        var schedule = schedules[i];
                        var milestoneOption = $(document.createElement('option'));
                        milestoneOption.attr('value', schedule.id);
                        milestoneOption.html(schedule.milestoneName);
                        $("#milestoneTimeslotsSelect").append(milestoneOption);
                    }
                    
                    $("#milestoneTimeslotsSelect").val(schedules[0].milestoneName).change(); //Select first milestone
                    $(".createTimeslotsTab a").tab('show');
                }
                
                $("#milestoneTimeslotsSelect").on('change', function(e){
                    $(".timeslotsTable").empty();
                    var selectedMilestone = $(this).val();
                    selectedSchedule = null;
                    for (var i = 0; i < schedules.length; i++) {
                        var schedule = schedules[i];
                        if ((schedule.milestoneName) === selectedMilestone) {
                            selectedSchedule = schedule;
                            break;
                        }
                    }
                    makeTimeslotTable("timeslotsTable", selectedSchedule.dates, selectedSchedule.dayStartTime, selectedSchedule.dayEndTime);
                    populateTimeslotsTable(selectedSchedule.duration);
                    $("#createTimeslotsSubmitBtn").button('reset');
                    if (selectedSchedule.isCreated) { //Don't let them create again
                        $("#createTimeslotsSubmitBtn").button('loading');
                    }
                    $(".timeslotsTable").show();
                    return false; 
                });
                
                //Submit to server
                $("#createTimeslotsSubmitBtn").on('click', function() {
                    $("#createTimeslotsSubmitBtn").button('loading');
                    var timeslotsData = {};
                    var timeslots_array = new Array();

                    var inputData = $("div.start-marker", ".timeslotsTable").get();
                    for (var i = 0; i < inputData.length; i++) {
                        var obj = inputData[i];
                        timeslots_array.push($(obj).parent().attr("value"));
                    }
                    
                    timeslotsData["scheduleId"] = selectedSchedule.scheduleId;
                    timeslotsData["timeslots"] = timeslots_array;
                    timeslotsData["venue"] = $("#venueInput").val();
                    console.log('Timeslots data is: ' + JSON.stringify(timeslotsData));
                    $.ajax({
                        type: 'POST',
                        url: 'createTimeslotsJson',
                        data: {jsonData: JSON.stringify(timeslotsData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            console.log("createTimeslotsJson was successful");
                            showNotification("SUCCESS", response.message);
//                            displayMessage("timeslotResultMessage", response.message, false);
                            selectedSchedule["isCreated"] = true;
                        } else {
                            var eid = btoa(response.message);
                            console.log(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        console.log("createTimeslotsJson AJAX FAIL");
                        showNotification("ERROR", "Oops.. something went wrong");
//                        displayMessage("timeslotResultMessage", "Oops.. something went wrong", true);
                    });
                    return false;
                });
                
                
                function makeTimeslotTable(tableClass, dateArray, dayStart, dayEnd) {
                    var thead = $(document.createElement("thead"));
                    var minTime = dayStart;
                    var maxTime = dayEnd;

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
                    for (var i = minTime; i < maxTime; i++) {
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
                    console.log("clicked timeslotcell");
                    triggerTimeslot(this, selectedSchedule.duration);
                    return false;
                });
                
                /*
                 * METHOD TO CHOOSE TIMESLOTS ON THE CREATED TABLE
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

                function populateTimeslotsTable(duration) {
                    $(".timeslotcell").each(function() {
                        triggerTimeslot(this, duration);
                    });
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
						   opts.title = "Created";
						   break;
						case "ERROR":
						   opts.type = "error";
						   opts.title = "Warning";
						   break;
						default:
							alert("Something went wrong");
					}
				   $.pnotify(opts);
				}
				
            };
            
            addLoadEvent(createScheduleLoad);
        </script>
    </body>
</html>
