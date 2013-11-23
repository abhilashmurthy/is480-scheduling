<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Prakhar
--%>

<%@page import="java.util.Set"%>
<%@page import="model.role.Faculty"%>
<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<%
    Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
    //User user = (User) session.getAttribute("user");
%>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | TA Video Signup </title>
        <style type="text/css">
            .timeslotsTable tr:first-child {
                font-size: 16px !important;
                height: 25px;
                padding: 10px;
                text-align: left;
            }
			
			.timeslotsTable td, .timeslotsTable th {
				text-align: center;
			}
			
			#timeslotsTableSection, #taStatisticsChart {
				margin-top: 30px;
				margin-left: 10px !important;
			}
			
			#taStatisticsChart {
				display: inline-block;
				margin-left: 20px !important;
				max-width: 100%;
				height: 500px;
			}
            
			#milestoneTimeslotsSelect {
				margin-bottom: 0px !important;
			}
			
            .legend td {
                font-size: 16px !important;
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
                border-left: 5px solid #5C7AFF;
                border-right: 5px solid #5C7AFF;
                border-top: 7px solid #5C7AFF;
                z-index: 1;
				float: left;
            }
            .available {
                background-color: #B8F79E !important ;
            }
            .chosen {
                background-color: #00C918 !important ;
            }
            
            .otherTAChosen {
                background-color: #F9FCBD !important;
            }
			
			.chosen > .teamName {
				color: white;
				font-weight: bold;
				font-size: 12px;
			}
			
			.available > .teamName {
				font-weight: bold;
				font-size: 12px;
			}
            
            .availabilityLegend {
				float: right;
				margin-right: 25%;
            }
			
			.availabilityLegend td {
				height: 10px;
				line-height: 10px;
			}
			
			.border-top {
				border-top: 1px solid #dddddd !important;
			}
			
			.border-left {
				border-left: 1px solid #dddddd !important;
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
			
			.dateHeader {
				font-size: 15px;
			}
			
			.taChartBtn {
				margin-top: 30px;
				margin-left: 10px;
			}
			
			.clickableHelp {
				font-size: 14px;
				padding-bottom: 5px;
				font-style: italic;
			}
			
        </style>
    </head>
    <body>
        <!-- Navbar -->
        <%@include file="navbar.jsp" %>

        <!-- Kick unauthorized user -->
        <%
            if (activeRole != Role.TA && activeRole != Role.ADMINISTRATOR) {
                request.setAttribute("error", "You need to be a TA to view this page");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
        %>

        <!-- Edit Sign ups -->
        <div class="container">
                <h3>TA Video Sign Up</h3>
					<table class='availabilityLegend'>
						<% if (activeRole.equals(Role.TA)) { %>
						<tr><td style="background-color:#B8F79E;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Available Slot</td></tr>
						<tr><td style="background-color:#00C918;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;You signed up</td></tr>
						<tr><td style="background-color:#F9FCBD;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Unavailable Slot</td></tr>
						<% } else { %>
						<tr><td style="background-color:#B8F79E;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;No TA</td></tr>
						<tr><td style="background-color:#F9FCBD;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;TA Signed Up</td></tr>
						<% } %>
					</table>
                    <table>
                        <tr>
                            <td>Milestone</td>
                            <td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select> <% if (activeRole.equals(Role.TA)) %> <button id="editTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Saving...">Save</button> <% ; %></td>
                        </tr>
					</table>
					<button class="taChartBtn btn btn-success" data-toggle="collapse" data-target="#taStatisticsAccordion">
						TA Statistics
					</button>
					<div id="taStatisticsAccordion" class="accordion-body collapse">
						<div class="accordion-inner">
							<div id='taStatisticsChart' class="collapse"></div>
						</div>
					</div>
					<div id="timeslotsTableSection">
						<% if (activeRole.equals(Role.ADMINISTRATOR)) %> <div class="clickableHelp muted">Click on a timeslot to email TAs</div><% ; %>
						<table class="timeslotsTable table-condensed table-hover table-bordered table-striped" style='cursor: pointer'></table>
					</div>
        </div>

        <%@include file="footer.jsp" %>
        <script type="text/javascript">
            //Makes use of footer.jsp's jQuery and bootstrap imports
            taAvailabilityLoad = function() {

                //------------------------------------------//
                // View Schedule Data
                //------------------------------------------//

                //Declare common variables
                //Default milestoneStr is ACCEPTANCE
				var loggedInTaId = <%= user.getId() %>;
                var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                var activeSemesterStr = "<%= activeTerm.getSemester()%>";
                var unavailableTimeslots = new Array();
                var scheduleData = null;
                var selectedMilestone = null;
                var milestones = new Array();
				var taData = JSON.parse('<s:property escape= "false" value= "taJson"/>');
				console.log('TA DATA: ' + JSON.stringify(taData));

                loadMilestones();
                loadSelectDropdown();
                
                function loadMilestones() {
                    milestones = getScheduleData(null, activeAcademicYearStr, activeSemesterStr).milestones;
                };
                
                function loadSelectDropdown() {
                    for (var i = 0; i < milestones.length; i++) {
						if (!milestones[i].bookable) continue;
							var milestoneOption = $(document.createElement('option'));
							milestoneOption.attr('value', milestones[i].name);
							milestoneOption.html(milestones[i].name);
							$("#milestoneTimeslotsSelect").append(milestoneOption);
                    }
                }
                
                $("body").on('change', '#milestoneTimeslotsSelect', function(e){
                    $(".timeslotsTable").empty();
                    selectedMilestone = $(this).val();
                    scheduleData = getScheduleData(selectedMilestone, activeAcademicYearStr, activeSemesterStr);
                    loadScheduleTimeslots(selectedMilestone, scheduleData);
					taData = JSON.parse('<s:property escape= "false" value= "taJson"/>');
					loadTAStatistics();
                    return false;
                });
                
                $("#milestoneTimeslotsSelect").val($("#milestoneTimeslotsSelect option:first").attr('value')).trigger('change');
				
                function loadScheduleTimeslots(milestoneStr, scheduleData) {
                    var tableClass = "timeslotsTable";
                    var table = $("." + tableClass);
                    makeTimeslotTable(tableClass, scheduleData, getDistinctDates(scheduleData, "typeString"));
                    populateTimeslotsTable(tableClass, scheduleData);
                }
				
                function getDatesBetween(startDate, endDate) {
                    var dateArray = new Array();
                    var currentDate = Date.parse(startDate);
                    while (currentDate <= Date.parse(endDate)) {
                        if (currentDate.isWeekday()) {
                            dateArray.push(new Date(currentDate));
                        }
                        currentDate = currentDate.addDays(1);
                    }
                    return dateArray;
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
                    console.log("Submitting data: " + JSON.stringify(data));
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

                function getDistinctDates(scheduleData, type) {
                    var datesSet = new HashSet();
                    for (i = 0; i < scheduleData.timeslots.length; i++) {
                        if (type === "typeDate") {
                            datesSet.add(new Date(Date.parse(scheduleData.timeslots[i].datetime).toString("yyyy-MM-dd")));
                        } else {
                            datesSet.add(Date.parse(scheduleData.timeslots[i].datetime).toString("yyyy-MM-dd"));
                        }
                    }
                    var scheduleDataDates = datesSet.values().sort();
                    return scheduleDataDates;
                }
				
				//ADDED FOR SELECT/DESELECT ALL
				$('body').on('change', '.checkBoxClass', function(){
					var $checkbox = $(this);

					if(!$checkbox.is(':checked')){
						var dateTime = $checkbox.attr('id');
						if(dateTime.length===1){
							dateTime = "0" + dateTime;
						}
						for (var i = 0; i < scheduleData.timeslots.length; i++) {
							var timeslot = scheduleData.timeslots[i];
							if (Date.parse(timeslot.datetime).toString('yyyy-MM-dd') === dateTime) {
								$('.timeslotcell').each(function(){

									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
									
										var valswitch = "switch_" +$(this).attr('value').split("_")[1];
										var valtest = document.getElementById(valswitch);

										//if the TA for this timeslot is not you
										if(valtest !==null){
											valtest = valtest.options[0].text;
											var assignElement = "#" + valswitch;
											 $(assignElement).val(valtest);
										}
			
										if(($(this).attr('class')).indexOf("chosen") > 0) {
												triggerTimeslot($(this));
											}
										}
								});
							}else if(Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[0]
									+Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[1] === dateTime){
								$('.timeslotcell').each(function(){
									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
										
										var valswitch = "switch_" +$(this).attr('value').split("_")[1];
										var valtest = document.getElementById(valswitch);
										
										//if the TA for this timeslot is not you
										if(valtest !==null){
											valtest = valtest.options[0].text;
											var assignElement = "#" + valswitch;
											 $(assignElement).val(valtest);
										}
			
										if(($(this).attr('class')).indexOf("chosen") > 0) {
											triggerTimeslot($(this));
										}
									}
								});
							}
						}
					}else if($checkbox.is(':checked')){
						var dateTime = $checkbox.attr('id');
						if(dateTime.length===1){
							dateTime = "0" + dateTime;
						}
						for (var i = 0; i < scheduleData.timeslots.length; i++) {
							var timeslot = scheduleData.timeslots[i];
							if (Date.parse(timeslot.datetime).toString('yyyy-MM-dd') === dateTime) {
								$('.timeslotcell').each(function(){
										
									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
										
										var valswitch = "switch_" +$(this).attr('value').split("_")[1];
										var valtest = document.getElementById(valswitch);
										
										//if the TA for this timeslot is not you
										if(valtest !==null){
											valtest = valtest.options[valtest.selectedIndex].text;
											var assignElement = "#" + valswitch;
											 $(assignElement).val("You");
										}
						
										if(($(this).attr('class')).indexOf("available") > 0) {
											
											triggerTimeslot($(this));
										}
									}
								});
							}else if(Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[0] 
										+Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[1] === dateTime){
								$('.timeslotcell').each(function(){
									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
										
										var valswitch = "switch_" +$(this).attr('value').split("_")[1];
										var valtest = document.getElementById(valswitch);
										
										//if the TA for this timeslot is not you
										if(valtest !==null){
											valtest = valtest.options[0].text;
											var assignElement = "#" + valswitch;
											 $(assignElement).val("You");
										}	
			
										if(($(this).attr('class')).indexOf("available") > 0) {
											triggerTimeslot($(this));
										}
									}
								});
							}
						}
					}
					
				});

                function makeTimeslotTable(tableClass, scheduleData, dateArray) {
                    var thead = $(document.createElement("tr"));
                    var minTime = scheduleData.dayStartTime;
                    var maxTime = scheduleData.dayEndTime;

                    //Creating table header with dates
                    thead.append("<td></td>"); //Empty cell for time column
                    for (i = 0; i < dateArray.length; i++) {
                        var th = $(document.createElement("td")).addClass('dateHeader');
                        var headerVal = new Date(dateArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(dateArray[i]).toString('ddd');
                        if (<%= activeRole.equals(Role.TA)%>) th.html(headerVal + "<br/>Select All <input class='checkBoxClass' type='checkbox' name='" + dateArray[i] + "' id='" + dateArray[i] + "'/>");
						else th.html(headerVal);
                        thead.append(th);
                    }
                    //Inserting constructed table header into table
                    $("." + tableClass).append($(document.createElement('thead')).append(thead));
					
                    //Generating list of times
                    var timesArray = new Array();
                    for (var i = minTime; i < maxTime; i++) {
                        var timeVal = Date.parse(i + ":00:00");
                        timesArray.push(timeVal.toString("HH:mm"));
                        timeVal.addMinutes(30);
                        timesArray.push(timeVal.toString("HH:mm"));
                    }

					var slotSize = scheduleData.duration / 30;
					var counter = 1;
					var startTime = 0;

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
                            var timeslot = getScheduleDataTimeslot(datetimeString, scheduleData);
                            if (timeslot) {
								td.addClass('border-top');
								td.attr('id', 'timeslot_' + datetimeString);
								td.attr("value", "timeslot_" + timeslot.id);
								if (<%= activeRole.equals(Role.ADMINISTRATOR)%>) td.attr('title', 'Click to send email');
								if (timeslot.hasOwnProperty("taId")) {
									if (timeslot.taId === loggedInTaId) {
										td.addClass('markable');
										td.addClass('available');
										td.append($(document.createElement('div')).addClass('start-marker'));
									} else {
										td.addClass('otherTAChosen');
										//td.append($(document.createElement('div')).addClass('textinside').html(timeslot.TA));
										//ADDED CODE FOR DROPDOWN
										if (<%= activeRole.equals(Role.TA)%>) {
											td.append($(document.createElement('container')).html("<select id='switch_" + timeslot.id +  "' style='width:150px;heigth:30px;'><option selected>" +
												timeslot.TA + "</option><option>" + "You" + "</option></select>"));
										} else {
											td.append($(document.createElement('div')).addClass('timeslotTA textinside').html(timeslot.TA));
										}
									}
								} else {
									td.addClass('markable');
								}
								if (timeslot.team) {
									td.append($(document.createElement('div')).addClass('timeslotTeam textinside').html(timeslot.team));
								}
                            }
							td.addClass('border-left');
                            tr.append(td);
                        }
						counter++;
                        $("." + tableClass).append(tr);
                    }
                }

                /*
                 * METHOD TO MARK TIMESLOTS ON TABLE
                 */
				function triggerTimeslot($timeslotCell) {
                    if (!$timeslotCell.hasClass('timeslotcell')) return false;
					var slotSize = scheduleData.duration / 30;
					if ($timeslotCell.hasClass('chosen')) {
						//Unchoose a timeslot
						var $prevTr = $timeslotCell.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($timeslotCell.index()).attr('id')) {
								//Unselect this timeslot
								var $nextTr = $prevTr;
								for (var j = 0; j < slotSize; j++) {
									$nextTr.children().eq($timeslotCell.index()).removeClass('chosen');
									$nextTr.children().eq($timeslotCell.index()).addClass('available');
									$nextTr = $nextTr.next();
								}
								break;
							}
							$prevTr = $prevTr.prev();
						}
					} else if ($timeslotCell.hasClass('available')) {
						//Choose a timeslot
						var $prevTr = $timeslotCell.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($timeslotCell.index()).attr('id')) {
								//Unselect this timeslot
								var $nextTr = $prevTr;
								for (var j = 0; j < slotSize; j++) {
									$nextTr.children().eq($timeslotCell.index()).removeClass('available');
									$nextTr.children().eq($timeslotCell.index()).addClass('chosen');
									$nextTr = $nextTr.next();
								}
								break;
							}
							$prevTr = $prevTr.prev();
						}
					} else {
						//Mark a timeslot as available
						var $nextTr = $timeslotCell.closest('tr');
						if ($nextTr.parent().children().index($nextTr) + slotSize > $nextTr.parent().children().length) return false; //Invalid timeslot
						if (<%= activeRole.equals(Role.TA) %>) $timeslotCell.append($(document.createElement('div')).addClass('start-marker'));
						for (var i = 0; i < slotSize; i++) {
							$nextTr.children().eq($timeslotCell.index()).addClass('available');
							$nextTr = $nextTr.next();
						}
					}
					return false;
                }
				
				function emailAvailable($timeslotCell) {
                    if (!$timeslotCell.hasClass('timeslotcell')) return false;
					var slotSize = scheduleData.duration / 30;
					var $prevTr = $timeslotCell.closest('tr');
					for (var i = slotSize; i > 0; i--) {
						var $availableTd = $prevTr.children().eq($timeslotCell.index());
						if ($availableTd.attr('id')) {
							if ($availableTd.children('.timeslotTeam').length && !$availableTd.children('.timeslotTA').length) {
								var mailto = 'mailto:';
								for (var j = 0; j < taData.length; j++) {
									mailto += taData[j].username + '@smu.edu.sg; ';
								}
								mailto += '&body=Hi IS480 TAs,%0D%0A%0D%0A';
								mailto += 'Team ' 
										+ $availableTd.children('.timeslotTeam').text() + ' is presenting on ' 
										+ Date.parse($availableTd.attr('id').split('_')[1]).toString('ddd, dd-MMM') 
										+ ' at ' +  Date.parse($availableTd.attr('id').split('_')[1]).toString('HH:mm') 
										+ ' - ' + Date.parse($availableTd.attr('id').split('_')[1]).addMinutes(scheduleData.duration).toString('HH:mm')
										+ ' for their FYP ' + selectedMilestone + ' presentation.'
										+ '%0D%0A';
								mailto += 'There is currently no TA assigned. Please sign up on the IS480 Scheduling System to record their video.';
								window.location.href = mailto;
							} else if (!$availableTd.children('.timeslotTA').length) {
								var mailto = 'mailto:';
								for (var j = 0; j < taData.length; j++) {
									mailto += taData[j].username + '@smu.edu.sg; ';
								}
								mailto += '&body=Hi IS480 TAs,%0D%0A%0D%0A';
								mailto += 'Please sign up for timeslot ' 
										+ Date.parse($availableTd.attr('id').split('_')[1]).toString('ddd, dd-MMM') 
										+ ' at ' +  Date.parse($availableTd.attr('id').split('_')[1]).toString('HH:mm') 
										+ ' - ' + Date.parse($availableTd.attr('id').split('_')[1]).addMinutes(scheduleData.duration).toString('HH:mm')
										+ ' for the FYP ' + selectedMilestone + ' presentation'
										+ ' on the IS480 Scheduling System.';
								window.location.href = mailto;
							} else if ($availableTd.children('.timeslotTA').length) {
								var mailto = 'mailto:';
								var ta = null;
								for (var j = 0; j < taData.length; j++) {
									if (taData[j].name === $availableTd.children('.timeslotTA').text()) {
										ta = taData[j];
										break;
									}
								}
								mailto += ta.username + '@smu.edu.sg;?' + 'body=Hi ' + ta.name + ',%0D%0A%0D%0A';
								window.location.href = mailto;
							}
							break;
						}
						$prevTr = $prevTr.prev();
					}
					return false;
                }

                $('body').on('click', 'td.timeslotcell', function(e){
					if (uatMode) recordHumanInteraction(e);
					if ($(this).is('.chosen, .available') && <%= activeRole.equals(Role.TA) %>) {
						triggerTimeslot($(this));
					} else if (<%= activeRole.equals(Role.ADMINISTRATOR) %>) {
						emailAvailable($(this));
					}
					return false;
                });
				
				//Hover glow effect
                $('body').on('mouseenter', '.timeslotsTable tr:not(:has(table, th)) td:not(:first-child)', function(e) {
					var $td = $(this);
					var slotSize = scheduleData.duration / 30;
					if ($td.hasClass('chosen') || $td.hasClass('available')) {
						//If hovering over selectable timeslot
						var $prevTr = $td.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($td.index()).attr('id')) {
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
					}
					return false;
                });
                $('body').on('mouseleave', '.timeslotsTable td', function(e) {
					var $td = $(this);
					var slotSize = scheduleData.duration / 30;
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

                function populateTimeslotsTable(tableClass, scheduleData) {
                    $(".timeslotcell").each(function(e) {
                        var self = $(this);
                        if (self.hasClass('markable')) {
                            triggerTimeslot(self);
                        }
                    });
                    $(".otherTAChosen").each(function(){
                        var tr = $(this).parent();
                        var tbody = $(this).parents("tbody");
                        var row = tr.parent().children().index(tr);
                        var nextRow = $(tbody).children().get(row + i);
                        var slotSize = scheduleData.duration / 30;
                        var col = $(this).parent().children().index(this);
                        for (var i = 0; i < slotSize; i++) {
                            var nextRow = $(tbody).children().get(row + i);
                            var nextCell = $(nextRow).children().get(col);
                            $(nextCell).addClass('otherTAChosen');
                        }
                    });
                }

                function getScheduleDataTimeslot(datetimeString, scheduleData) {
                    var timeslots = scheduleData.timeslots;
                    var timeslot = null;
                    for (var k = 0; k < timeslots.length; k++) {
                        if (timeslots[k].datetime === datetimeString) {
                            timeslot = timeslots[k];
                            break;
                        }
                    }
                    return timeslot;
                }

                //------------------------------------------//
                // Change TA Chosen
                //------------------------------------------//

                //Update Timeslots AJAX Call            
                $("#editTimeslotsSubmitBtn").on('click', function(e) {
					if (uatMode) recordHumanInteraction(e);
                    $("#editTimeslotsSubmitBtn").button('loading');
                    //SerializeArray not functional for timeslots
                    var timeslotsData = {};
                    var timeslot_data = new Array();

                    var allTimeslots = $("td.chosen > div.start-marker", ".timeslotsTable").get();
                    for (var i = 0; i < allTimeslots.length; i++) {
                        var obj = allTimeslots[i];
                        timeslot_data.push($(obj).parent().attr("value").split("_")[1]);
                    }
					
					//ADDED: POPULATE THE NEW SLOTS THAT ARE TAKEN
					var allTimeslots2 =  $("container", ".timeslotsTable").get()
					var swappedSlotIds = new Array();
					for (var i = 0; i < allTimeslots2.length; i++) {
						
                        var obj = allTimeslots2[i];
						var timeslot = $(obj).parent().attr("value").split("_")[1];
						//console.log("switch_"+timeslot);
						var id = "switch_"+timeslot;
						var valtest = document.getElementById(id);
						var strUser = valtest.options[valtest.selectedIndex].text;
                        
						if(strUser === 'You'){
							 timeslot_data.push(timeslot);
							 swappedSlotIds.push(id);
						}
                    }
					
                    timeslotsData["timeslots"] = timeslot_data;
                    timeslotsData["scheduleId"] = scheduleData.id;
                    $.ajax({
                        type: 'POST',
                        url: 'taSignupJson',
                        data: {jsonData: JSON.stringify(timeslotsData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (!response.exception) {
                            if (response.success) {
                                showNotification("SUCCESS", "Timeslots saved");
								//Update page
                                $("#editTimeslotsSubmitBtn").button('reset');
								unavailableTimeslots = response.unavailableTimeslots;
								$('.otherTAChosen').each(function(){
									var $this = $(this);
									if ($this.find('select').val() === 'You') {
										$this.find('container').remove();
										$this.removeClass();
										$this.addClass('timeslotcell markable');
										var slotSize = scheduleData.duration / 30;
										var $nextTr = $this.closest('tr');
										for (var j = 0; j < slotSize; j++) {
											$nextTr.children().eq($this.index()).removeClass();
											$nextTr.children().eq($this.index()).addClass('timeslotcell');
											$nextTr = $nextTr.next();
										}
										triggerTimeslot($this);
										triggerTimeslot($this);
									}
								});
								//Update JSON
								var oldTimeslotIds = [];
								var mySignups = null;
								for (var i = 0; i < taData.length; i++) {
									if (parseInt(taData[i].id) === parseInt(loggedInTaId)) {
										mySignups = taData[i].mySignups;
										mySignupLoop: for (var j = 0; j < mySignups.length; j++) {
											if (parseInt(mySignups[j].scheduleId) === parseInt(scheduleData.id))
												oldTimeslotIds.push(mySignups[j].timeslotId);
										}
										break;
									}
								}
								var newTimeslotIds = timeslot_data;
								for (var i = 0; i < newTimeslotIds.length; i++) {
									if (oldTimeslotIds.indexOf(newTimeslotIds[i]) === -1) {
										//Add to mySignups
										mySignups.push({scheduleId: scheduleData.id, timeslotId: newTimeslotIds[i]});
									}
								}
								for (var i = 0; i < oldTimeslotIds.length; i++) {
									if (newTimeslotIds.indexOf(oldTimeslotIds[i]) === -1) {
										//Delete from my signups
										for (var j = 0; j < mySignups.length; j++) {
											if (parseInt(mySignups[j].timeslotId) === parseInt(oldTimeslotIds[i])) {
												mySignups.splice(mySignups.indexOf(mySignups[j]), 1);
												break;
											}
										}
									}
								}
								for (var i = 0; i < taData.length; i++) {
									if (parseInt(taData[i].id) === parseInt(loggedInTaId)) {
										taData[i].mySignups = mySignups;
										break;
									}
								}
								console.log('New count should be: ' + mySignups.length);
								loadTAStatistics();
                            } else {
                                var eid = btoa(response.message);
                                window.location = "error.jsp?eid=" + eid;
                            }
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        $("#editTimeslotsSubmitBtn").button('reset');
                    });
                    return false;
                });
				
				/* TOOLTIP */
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
				
				/* JQPLOT */
				loadTAStatistics();
				var barGraph = null;
				function loadTAStatistics() {
					var taNames = getSeriesArray("name", false);
					var signups = getSeriesArray("mySignups", true);
					barGraph = $.jqplot('taStatisticsChart', [signups], {
						seriesDefaults: {
							renderer: $.jqplot.BarRenderer,
							shadow: false,
							rendererOptions: {
								highlightMouseOver: false,
								lineWidth: 5
							},
							pointLabels: {show: false,}
						},
						title: 'TA Signup Count',
						series: [{label: 'Signups'}],
						axesDefaults: {
							tickRenderer: $.jqplot.CanvasAxisTickRenderer
						},
						axes: {
							xaxis: {
								renderer: $.jqplot.CategoryAxisRenderer,
								ticks: taNames,
								tickOptions: {
									showGridline: false,
									angle: 90,
									fontSize: '14px'
								}
							},
							yaxis: {
								padMin: 0
							}
						},
						legend: {
							show: false,
							location: 'e',
							fontSize: 12,
							border: "none",
							marginRight: 30
						},
						seriesColors: ["#B8F79E"],
						grid: {
							drawGridLines: false,
							background: "#ffffff",
							borderColor: "#dddddd",
							shadow: false
						}
					}).replot();
				}
				
				function getSeriesArray(key, getLength) {
				   var data = [];
				   for (var i = 0; i < taData.length; i++) {
					   var ta = taData[i];
					   if (getLength) {
						   for (var j = 0; j < ta[key].length; j++) {
							   if (parseInt(ta[key][j].scheduleId) !== parseInt(scheduleData.id)) {
								   ta[key].splice(ta[key][j], 1);
								   --j;
							   }
						   }
						   data.push(ta[key].length);
					   } else {
						   data.push(ta[key]);
					   }
				   }
				   return data;
				}
				
				/* NOTIFICATION */
                
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

            addLoadEvent(taAvailabilityLoad);
        </script>
    </body>
</html>
