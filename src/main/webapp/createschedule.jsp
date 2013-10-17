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
			
			.scheduleBookable {
				padding-left: 40px;
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
			
			.glow-top {
				border-top: 1px solid #fff966 !important;
				border-radius: 5px 5px 0px 0px; 
				box-shadow: inset 0  16px 16px -16px #fff966, inset 16px 0 16px -16px #fff966, inset -16px 0 16px -16px #fff966 !important;
			}
			.glow-sides {
				border-left: 1px solid #fff966 !important;
				border-right: 1px solid #fff966 !important;
				box-shadow: inset 16px 0 16px -16px #fff966, inset -16px 0 16px -16px #fff966;
			}
			.glow-bottom {
				border-bottom: 1px solid #fff966 !important;
				border-radius: 0px 0px 5px 5px; 
				box-shadow: inset 0 -16px 16px -16px #fff966, inset 16px 0 16px -16px #fff966, inset -16px 0 16px -16px #fff966 !important;
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
						<form id="createScheduleForm">
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
										<td><input id="semesterInput" type="text" name="semester" placeholder="eg. <%= nextSem %>"/><div id="semesterNameAvailabilityChecker" class="statusText"></div></td>
									</tr>
								</table>
							</div>
							<!-- Create Schedule -->
							<div id="createSchedulePanel" class="schedulePanel">
									<table id="createScheduleTable">
										<tr><th>Milestone</th><th colspan="2">Dates</th><th class="dayHours">Day Hours</th><th class="scheduleBookable">Bookable</th></tr>
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
								<tr><td class="formLabelTd">Progress</td><td><div id="timeslotsProgressBar" class="progress"><div class="bar bar-success" style="width: 0%;"></div></div></td></tr>
                                <tr><td class="formLabelTd">Milestone</td><td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select></td></tr>
                                <tr>
									<td class="formLabelTd">Venue</td>
									<td><input id="venueInput" type="text" name="venue" placeholder="eg. SIS Seminar Room 2-1"/><button id="createTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Done">Create</button></td>
									<td>
										<table class='timeslotsLegend'>
											<tr>
												<td class='legendBox' style="background-color:#B8F79E;"></td><td>Available</td>
											</tr>
										</table>
									</td>
								</tr>
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
                 
				 //Disable createTimeslotsTab first
                $(".createTimeslotsTab a").removeAttr('data-toggle');
                 $(".scheduleLeftNav li a").on('click', function(){
                     if (!$(this).attr('data-toggle')) {
                        var activeTab = $('.scheduleLeftNav').children('.active').children('a').html();
                        showNotification("WARNING", "Please " + activeTab + " First!");
						return false;
                     }
					 var href = $(this).attr('href').split('#')[1];
					 $(".tab-pane, .nav-tabs li").removeClass('active');
					 $(".tab-pane").hide();
					 $("#" + href).addClass('active');
					 $(".nav-tabs ." + href).addClass('active');
					 $("#" + href).show();
					 return false;
                 });

                /*----------------------------------------
                 CREATE TERM
                 ------------------------------------------*/
				
				//Spinner
				var thisYear = parseInt((new Date()).toString('yyyy'));
				$("#yearSpinnerInput").spinner({
					value: parseInt('<%= nextYear %>'),
					min: thisYear,
					max: 9999,
					step: 1
				});
				$("#yearSpinnerInput").on('focusout', function(){
					var value = $(this).spinner('value');
					if (value < thisYear) {
						$(this).spinner('value', thisYear);
					}
				});
				
				//Term name availability check
				$("#semesterInput").on('keyup', function(){
					var $this = $(this);
					$("#semesterNameAvailabilityChecker").css('color', 'grey').html($(document.createElement('span')).addClass('icon-refresh icon-spin'));
					setTimeout(function(){$this.trigger('change');}, 500);
					return false;
				});
				
				//Term name availability check
				$("#semesterInput, #yearSpinnerInput").on('change blur changed', function(){
					var semName = $.trim($("#semesterInput").val());
					var yearVal = $("#yearSpinnerInput").spinner('value');
					if (!semName) {
						$("#semesterNameAvailabilityChecker").empty();
						return false;
					}
					for (var i = 0; i < termNames.length; i++) {
						if (yearVal === termNames[i].year && semName.toLowerCase() === termNames[i].term.toLowerCase()) {
							$("#semesterNameAvailabilityChecker").css('color', 'red').html($(document.createElement('span')).addClass('icon-remove')).append(' Term name already exists');
							return false;
						}
					}
					$("#semesterNameAvailabilityChecker").css('color', 'green').html($(document.createElement('span')).addClass('icon-ok'));
					return false;
				});
				
				$("#yearSpinnerInput").on('changed', function(){
					var semName = $.trim($("#semesterInput").val());
					var yearVal = $("#yearSpinnerInput").spinner('value');
					//Update multiDatesPickers to year selected
					$(".datepicker").each(function(){
						var $nextMilestone = $(this);
						$nextMilestone.multiDatesPicker('resetDates', 'picked');
						$nextMilestone.datepicker('destroy');
						$nextMilestone.multiDatesPicker({
							dateFormat: "yy-mm-dd",
							defaultDate: Date.today() > Date.parse(yearVal + '-01-01')?Date.today():Date.parse(yearVal + '-01-01'),
//							minDate: Date.today() > Date.parse(yearVal + '-01-01')?Date.today():Date.parse(yearVal + '-01-01'),
//							beforeShowDay: $.datepicker.noWeekends,
							onSelect: function(date) {
								var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
								resetDisabledDates(date, order);
								updatePillbox();
							}
						});
					});
					updatePillbox();
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
					
                    //Order comparator
                    function compare(a, b) {
                        if (a.milestoneOrder < b.milestoneOrder) {
                            return -1;
                        } else if (a.milestoneOrder > b.milestoneOrder) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                        
                    milestones.sort(compare); //Sort by order first
                    for (var i = 0; i < milestones.length; i++) {
                        var milestone = milestones[i];
						milestone["id"] = milestone.name.toLowerCase().replace(' ', '');
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
									.attr('name', milestone.id + "Dates")
									.attr('id', "milestone_" + milestone.id)
									.attr('class', "milestoneOrder_" + milestone.milestoneOrder)
									.addClass('datepicker')
									.multiDatesPicker({
										dateFormat: "yy-mm-dd",
										defaultDate: Date.today(),
//										minDate: Date.today(),
//										beforeShowDay: $.datepicker.noWeekends,
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
									.attr('id', milestone.id + 'Pillbox')
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
									.append("From ")
									.append(
										$(document.createElement('input'))
											.attr('type', 'text')
											.attr('id', "milestoneDayStart_" + milestone.id)
											.attr('name', milestone.id + "DayStartTime")
											.attr('value', '09:00')
											.addClass('scheduleDayTimeSelect timepicker')
											.timepicker({
													step: 60,
													forceRoundTime: true,
													timeFormat: 'H:i',
													scrollDefaultTime: '09:00',
													disableTextInput: true
											})
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
											.attr('id', "milestoneDayEnd_" + milestone.id)
											.attr('name', milestone.id + "DayEndTime")
											.attr('value', '19:00')
											.addClass('scheduleDayTimeSelect timepicker')
											.css('float', 'right')
											.timepicker({
													step: 60,
													forceRoundTime: true,
													timeFormat: 'H:i',
													scrollDefaultTime: '19:00',
													disableTextInput: true
											})
									)
							);
						milestoneTr.append(milestoneDayTimeTd);
						var milestoneBookableTd = $(document.createElement('td'))
							.css('padding-left', '40px')
							.append(
								$(document.createElement('div'))
									.attr('id', 'milestoneBookable_' + milestone.id)
									.addClass('make-switch switch-medium')
									.attr('data-on', 'success')
									.attr('data-off', 'danger')
									.attr('data-on-label', 'Yes')
									.attr('data-off-label', 'No')
									.attr('padding-left', '30px')
									.append($(document.createElement('input')).attr('type', 'checkbox').attr('name', 'milestoneBookable_' + milestone.id).attr('checked', true))
									.bootstrapSwitch()
							);
                        milestoneTr.append(milestoneBookableTd);
                        milestoneTr.insertBefore('#createScheduleSubmitRow');
                    }
                    $(".createScheduleTab a").tab('show');
					disableDatePickers();
                }
				
				function disableDatePickers() {
					//Disabled subsequent
					$(".datepicker").each(function(i, e){
						if (i > 0) {
							//Disable datepicker
							$(this).multiDatesPicker('resetDates', 'picked');
							$(this).datepicker('option', 'maxDate', -2);
						}
					});
				}
                
                //Reset Dates On Adding/Removing from multiDatesPicker
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
							defaultDate: minDate.addDays(1),
							minDate: minDate.addDays(1),
//							beforeShowDay: $.datepicker.noWeekends,
							onSelect: function(date) {
								var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
								resetDisabledDates(date, order);
								updatePillbox();
							}
						});
					} else if ($nextMilestone.length) {
						//A date has been removed
						//Reset all subsequent dates
						var dates = $(".milestoneOrder_" + orderNum).multiDatesPicker('getDates');
						var nextOrder = parseInt($nextMilestone.attr('class').split(" ")[0].split("_")[1]);
						for (var i = nextOrder; i > 0; i++) {
							$nextMilestone = $(".milestoneOrder_" + i);
							if (!$nextMilestone.length) break; //No more milestones
							$nextMilestone.multiDatesPicker('resetDates', 'picked');
							$nextMilestone.datepicker('destroy');
							if (dates.length > 0) {
								//If picked dates are still there
								$nextMilestone.multiDatesPicker({
									dateFormat: "yy-mm-dd",
									defaultDate: Date.parse(dates[dates.length - 1]).addDays(1),
									minDate: Date.parse(dates[dates.length - 1]).addDays(1),
//									beforeShowDay: $.datepicker.noWeekends,
									onSelect: function(date) {
										var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
										resetDisabledDates(date, order);
										updatePillbox();
									}
								});
							} else {
								//If picked dates are not there anymore
								$nextMilestone.multiDatesPicker({
									dateFormat: "yy-mm-dd",
									defaultDate: Date.today(),
//									minDate: Date.today(),
//									beforeShowDay: $.datepicker.noWeekends,
									onSelect: function(date) {
										var order = parseInt($(this).attr('class').split(" ")[0].split("_")[1]);
										resetDisabledDates(date, order);
										updatePillbox();
									}
								});
								//Disable datepicker
								$nextMilestone.datepicker('option', 'maxDate', -2);
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
					resetDisabledDates(date, order);
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
						if ($endPoint.timepicker('getTime') <= Date.parse(selectedTime)) $endPoint.timepicker('setTime', Date.parse(selectedTime).addHours(2));
						$endPoint.timepicker('option', 'minTime', Date.parse(selectedTime).addHours(2).toString('HH:mm'));
					} else {
						//Reset start point timepicker
						var $startPoint = $("#milestoneDayStart_" + milestone);
						if ($startPoint.timepicker('getTime') >= Date.parse(selectedTime)) $startPoint.timepicker('setTime', Date.parse(selectedTime).addHours(-2));
						$startPoint.timepicker('option', 'maxTime', Date.parse(selectedTime).addHours(-2).toString('HH:mm'));
					}
				}

                //Create Schedule Submit - Show timeslots panel
                $("#createScheduleForm").on('submit', function(e) {
                    $("#createScheduleSubmitBtn").button('loading');
                    e.preventDefault();
                    e.stopPropagation();                  
					
					//Validate year and semester
					var year = $("#yearSpinnerInput").spinner('value');
					var term = $("#semesterInput").val();
					if (year === null || term === null || !term.length) {
						showNotification("WARNING", "Please enter a semester name");
						$("#createScheduleSubmitBtn").button('reset');
						return false;
					}
					
					//Validate dates and times
					var milestoneArray = $(this).serializeArray();
                    for (var i = 0; i < milestoneArray.length; i++) {
                        var milestoneItem = milestoneArray[i];
                        for (var j = 0; j < milestones.length; j++) {
                            var milestone = milestones[j];
							milestone["bookable"] = $("#milestoneBookable_" + milestone.id).bootstrapSwitch('status');
							var dates = $("#milestone_" + milestone.id).multiDatesPicker('getDates');
							if (dates.length === 0) {
								showNotification("WARNING", "Please pick dates for milestone: " + milestone.name);
								$("#createScheduleSubmitBtn").button('reset');
								return false;
							}
							milestone["dates[]"] = dates;
                            if (milestoneItem.name.split("DayStartTime")[0].toLowerCase() === milestone.id) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", "Please select valid times for milestone: " + milestone.name);
                                    $("#createScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dayStartTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                            if (milestoneItem.name.split("DayEndTime")[0].toLowerCase() === milestone.id) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", "Please select valid times for milestone: " + milestone.name);
                                    $("#createScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dayEndTime"] = Date.parse(milestoneItem.value).toString('H');
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
                                $("#createScheduleSubmitBtn").button('reset');
                                wrongTime = true;
                            }
                        }
                    });
                    if (wrongTime) return false;
					
					//Everything OK
                    var createScheduleData = {
						year: $("#yearSpinnerInput").spinner('value'), 
						semester: $("#semesterInput").val(),
						"milestones[]":milestones
					};
					
					console.log("Submitting: " + JSON.stringify(createScheduleData));
					
                    $.ajax({
                        type: 'POST',
                        url: 'createScheduleJson',
                        data: {jsonData: JSON.stringify(createScheduleData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            schedules = response.schedules;
                            showNotification("SUCCESS", "Created dates successfully");
							disableScheduleControls();
                            setTimeout(function(){displayCreateTimeslots();}, 1000);
                        } else {
							showNotification("WARNING", response.message);
							$("#createScheduleSubmitBtn").button('reset');
                        }
                    }).fail(function(error) {
                        console.log("createScheduleData AJAX FAIL");
						var eid = btoa("Erro in CreateScheduleAction: Escalate to developers!");
						window.location = "error.jsp?eid=" + eid;
                    });
                    return false;
                });
				
				function disableScheduleControls() {
					$("#createScheduleTab form :input").attr('disabled', true);
					$("#createScheduleTab .datepicker").each(function(){
						var $nextMilestone = $(this);
						var datesArray = $nextMilestone.multiDatesPicker('getDates');
						var dates = new Array();
						for (var i = 0; i < datesArray.length; i++){
							dates.push(Date.parse(datesArray[i]));
						}
						$nextMilestone.datepicker('destroy');
						$nextMilestone.multiDatesPicker({
							minDate: dates[0],
							addDates: dates,
							disabled: true
						});
					});
					$(".make-switch").bootstrapSwitch('setActive', false);
					$(".pillbox ul").off('click');
					$("#createScheduleTitle").css('display', 'inline').after($(document.createElement('div')).addClass('statusText').css('color', 'green').html($(document.createElement('span')).addClass('icon-ok')).append(' Please Create Timeslots'));
				}

                /*----------------------------------------
                 CREATE TIMESLOTS
                 ------------------------------------------*/

                //Display create timeslots
                function displayCreateTimeslots() {
					$('body').animate({scrollTop:0}, '500', 'swing');
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
                
				//Reset timeslots on change milestone dropdown
                $("#milestoneTimeslotsSelect").on('change', function(e){
					$(this).next('.statusText').remove();
					$("#venueInput").attr('disabled', false);
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
                    populateTimeslotsTable();
                    $("#createTimeslotsSubmitBtn").button('reset');
                    if (selectedSchedule.isCreated) { 
						//If schedule is created, don't let them create again
                        $("#createTimeslotsSubmitBtn").button('loading');
                        $("#venueInput").attr('disabled', true);
                        $(this).after($(document.createElement('div')).addClass('statusText').css('color', 'green').html($(document.createElement('span')).addClass('icon-ok')).append(' Timeslots created already'));
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
					selectedSchedule["timeslots"] = timeslots_array;
                    timeslotsData["venue"] = $("#venueInput").val();
//                    console.log('Timeslots data is: ' + JSON.stringify(timeslotsData));
                    $.ajax({
                        type: 'POST',
                        url: 'createTimeslotsJson',
                        data: {jsonData: JSON.stringify(timeslotsData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
							//Set isCreated to true
                            selectedSchedule["isCreated"] = true;
							var totalCreated = 0;
							for (var i = 0; i < schedules.length; i++) {
								if (schedules[i].isCreated) ++totalCreated;
							}
							$("#timeslotsProgressBar").children(".bar").css('width', ((totalCreated/schedules.length) * 100) + '%');
							if ((totalCreated/schedules.length) === 1) {
								//Go to manage active terms page
								showNotification("WARNING", "Schedule ready now");
								setTimeout(function(){window.location = "index";}, 2000);
							} else {
								//Select next milestone
								showNotification("SUCCESS", response.message);
								var nextOrder = null;
								for (var i = 0; i < milestones.length; i++) {
									if (milestones[i].name === selectedSchedule.milestoneName) {
										 nextOrder = milestones[i].milestoneOrder + 1;
										 break;
									}
								}
								for (var i = 0; i < milestones.length; i++) {
									if (milestones[i].milestoneOrder === nextOrder) {
										 $("#milestoneTimeslotsSelect").val(milestones[i].name).change(); //Select next milestone
										 break;
									}
								}
							}
                        } else {
                            var eid = btoa(response.message);
                            console.log(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        console.log("createTimeslotsJson AJAX FAIL");
                        showNotification("ERROR", "Oops.. something went wrong");
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
					if (selectedSchedule.isCreated) return false; //If created already, disable timeslot selection
                    triggerTimeslot($(this));
					$(this).trigger('mouseleave');
					$(this).trigger('mouseenter');
                    return false;
                });
				
				//Hover glow effect
                $('body').on('mouseenter', '.timeslotsTable tr:not(:has(table, th)) td:not(:first-child)', function(e) {
					var $td = $(this);
					var slotSize = selectedSchedule.duration / 30;
					if ($td.hasClass('chosen')) {
						//If hovering over a chosen timeslot
						var $prevTr = $td.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($td.index()).children('div.start-marker').length) {
								//Highlight this timeslot
								var $nextTr = $prevTr;
								for (var j = 0; j < slotSize; j++) {
									if (j === 0) {
										$nextTr.children().eq($td.index()).addClass('glow-top');
									}
									$nextTr.children().eq($td.index()).addClass('glow-sides');
									if (j === slotSize - 1) {
										$nextTr.children().eq($td.index()).addClass('glow-bottom');
									}
									$nextTr = $nextTr.next();
								}
								break;
							}
							$prevTr = $prevTr.prev();
						}
					} else {
						//If hovering over an empty timeslot
						var $nextTr = $td.closest('tr');
						if ($nextTr.parent().children().index($nextTr) + slotSize > $nextTr.parent().children().length) return false;
						for (var j = 0; j < slotSize; j++) {
							if ($nextTr.children().eq($td.index()).hasClass('teamExists')) return false; //Invalid timeslot
							$nextTr = $nextTr.next();
						}
						$nextTr = $td.closest('tr');
						for (var i = 0; i < slotSize; i++) {
							if (i === 0) {
								$nextTr.children().eq($td.index()).addClass('glow-top');
							}
							$nextTr.children().eq($td.index()).addClass('glow-sides');
							if (i === slotSize - 1) {
								$nextTr.children().eq($td.index()).addClass('glow-bottom');
							}
							$nextTr = $nextTr.next();
						}
					}
					return false;
                });
                $('body').on('mouseleave', '.timeslotsTable td', function(e) {
					var $td = $(this);
					console.log('leaving');
					var slotSize = selectedSchedule.duration / 30;
					if ($td.hasClass('glow-sides')) {
						var $prevTr = $td.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($td.index()).hasClass('glow-top')) {
								//Highlight this timeslot
								var $nextTr = $prevTr;
								for (var j = 0; j < slotSize; j++) {
									$nextTr.children().eq($td.index()).removeClass('glow-top glow-bottom glow-sides');
									$nextTr = $nextTr.next();
								}
								break;
							}
							$prevTr = $prevTr.prev();
						}
					}
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

                function populateTimeslotsTable() {
					var slotSize = selectedSchedule.duration / 30;
                    $(".timeslotcell").each(function() {
						var $this = $(this);
						if (selectedSchedule.isCreated) {
							//If schedule is created already, populate only selected timeslots
							var timeslots = selectedSchedule.timeslots;
							for (var i = 0; i < timeslots.length; i++) {
								if ($(this).attr('value') === timeslots[i]) {
									triggerTimeslot($this);
									break;
								}
							}
						} else {
							//If schedule not yet created, populate all timeslots
							var $tr = $this.closest('tr');
							if ($tr.parent().children().index($tr) % slotSize === 0) triggerTimeslot($this);							
						}
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
							alert("Something went wrong - Notifications");
					}
				   $.pnotify(opts);
				}
				
            };
            
            addLoadEvent(createScheduleLoad);
        </script>
    </body>
</html>
