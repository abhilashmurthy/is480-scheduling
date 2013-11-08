<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Prakhar
--%>

<%@page import="manager.UserManager"%>
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
%>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Your Availability</title>
        <style type="text/css">
            .timeslotsTable tr:first-child {
                font-size: 16px !important;
                height: 25px;
                padding: 10px;
                text-align: left;
                /*border-bottom: 1px solid black;*/
            }
			
			.timeslotsTable {
				margin-top: 60px;
				margin-left: 90px !important;
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
            .chosen {
                background-color: #B8F79E !important ;
            }
            .unavailable {
                background-color: #F7A8A8 !important ;
            }
            
/*            .teamExists {
                background-color: #F9FCBD !important;
            }*/
			
			.chosen.teamExists {
				background-color: #00C918 !important ;
			}
			
			.unavailable.teamExists {
				background-color: #F56753 !important ;
			}
			
			.unavailable > .teamName {
				color: white;
				font-weight: bold;
				font-size: 12px;
			}
			
			.chosen > .teamName {
				font-weight: bold;
				font-size: 12px;
			}
            
            .availabilityLegend {
				float: right;
/*                position: absolute;
                left: 70%;
                top: 12%;*/
/*                left: 7%;
                top: 35%;*/
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

        </style>
    </head>
    <body>
        <!-- Navbar -->
        <%@include file="navbar.jsp" %>

        <!-- Kick unauthorized user -->
        <%
            if (activeRole != Role.FACULTY) {
                request.setAttribute("error", "You need to be a faculty member to view this page");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
        %>

        <%
             Faculty facultyUser = UserManager.getUser(user.getId(), Faculty.class);
        %>

        <!-- Edit Availability -->
        <div id="availabilityPanel" class="container">
            <div id="editTimeslotsPanel">
                <h3>My Availability</h3>
					<table class='availabilityLegend'>
						<tr>
							<td style="background-color:#B8F79E;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;I'm available</td><td style="background-color:#00C918;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;I'm available & there's a team</td> 
						</tr>
						<tr><td style='height: 2px'></td></tr>
						<tr>
							<td style="background-color:#F7A8A8;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;I'm unavailable</td><td style="background-color:#F56753;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Oh dear, I'm unavailable & there's a team!</td> 
						</tr>
					</table>
                <div id="timeslotsTableSection">
                    <table>
                        <tr>
                            <td>Milestone</td>
                            <td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select> <button id="editTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Saving...">Save</button></td>
                        </tr>
					</table>
					<table class="timeslotsTable table-condensed table-hover table-bordered table-striped" style='cursor: pointer'></table>
                </div>
                <h4 id="timeslotsResultMessage" class="resultMessage"/></h4>
                <br/><br/>
            </div>
        </div>

        <%@include file="footer.jsp" %>
        <script type="text/javascript">
            //Makes use of footer.jsp's jQuery and bootstrap imports
            supervisorAvailabilityLoad = function() {

                //------------------------------------------//
                // View Schedule Data
                //------------------------------------------//

                //Declare common variables
                //Default milestoneStr is ACCEPTANCE
                var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                var activeSemesterStr = "<%= activeTerm.getSemester()%>";
                var unavailableTimeslots = new Array();
                var scheduleData = null;
                var selectedMilestone = null;
                var milestones = new Array();
                
                loadMilestones();
                loadUnavailableTimeslots();
                loadSelectDropdown();
                
                function loadMilestones() {
                    milestones = getScheduleData(null, activeAcademicYearStr, activeSemesterStr).milestones;
                };

                function loadUnavailableTimeslots() {
                    <s:iterator value="unavailableTimeslotIds">
                    unavailableTimeslots.push("timeslot_<s:property/>");
                    </s:iterator>
                }
                
                function loadSelectDropdown() {
                    for (var i = 0; i < milestones.length; i++) {
						if (!milestones[i].bookable) continue;
							var milestoneOption = $(document.createElement('option'));
							milestoneOption.attr('value', milestones[i].name);
							milestoneOption.html(milestones[i].name);
							$("#milestoneTimeslotsSelect").append(milestoneOption);
                    }
                }
                
                $("#milestoneTimeslotsSelect").on('change', function(e){
                    $(".timeslotsTable").empty();
                    selectedMilestone = $(this).val();
                    scheduleData = getScheduleData(selectedMilestone, activeAcademicYearStr, activeSemesterStr);
                    loadScheduleTimeslots(selectedMilestone, scheduleData);
                    return false; 
                });
                
                $("#milestoneTimeslotsSelect").val($("#milestoneTimeslotsSelect option:first").attr('value')).change(); //Select first milestone

                function loadScheduleTimeslots(milestoneStr, scheduleData) {
                    var tableClass = "timeslotsTable";
                    var table = $("." + tableClass);
                    makeTimeslotTable(tableClass, scheduleData, getDistinctDates(scheduleData, "typeString"));
                    populateTimeslotsTable(tableClass, scheduleData);
                    populateUnavailableTimeslots(tableClass, scheduleData);
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
				
				String.prototype.contains = function(substr) {
				return this.indexOf(substr) > -1;
			  }
				
				$('body').on('change', '.checkBoxClass', function(){
					var $checkbox = $(this);
					console.log('selected ' + $checkbox.attr('id') + ': ' + $checkbox.is(':checked'));

					if($checkbox.is(':checked')){
						var dateTime = $checkbox.attr('id');
						if(dateTime.length===1){
							dateTime = "0" + dateTime;
						}
						for (var i = 0; i < scheduleData.timeslots.length; i++) {
							var timeslot = scheduleData.timeslots[i];
							if (Date.parse(timeslot.datetime).toString('yyyy-MM-dd') === dateTime) {
								$('.timeslotcell').each(function(){
									
									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
										if(($(this).attr('class')).indexOf("chosen") > 0) {
											triggerTimeslot($(this));
										}
									}
								});
							}else if(Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[0]
									+Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[1] === dateTime){
								$('.timeslotcell').each(function(){
									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
										if(($(this).attr('class')).indexOf("chosen") > 0) {
											triggerTimeslot($(this));
										}
									}
								});
							}
							//console.log(Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[0]);
						}
					}else if(!$checkbox.is(':checked')){
						var dateTime = $checkbox.attr('id');
						if(dateTime.length===1){
							dateTime = "0" + dateTime;
						}
						for (var i = 0; i < scheduleData.timeslots.length; i++) {
							var timeslot = scheduleData.timeslots[i];
							if (Date.parse(timeslot.datetime).toString('yyyy-MM-dd') === dateTime) {
								$('.timeslotcell').each(function(){
									
									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
										if(($(this).attr('class')).indexOf("available") > 0) {
											triggerTimeslot($(this));
										}
									}
								});
							}else if(Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[0] 
										+Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[1] === dateTime){
								$('.timeslotcell').each(function(){
									if ($(this).attr('value') && parseInt($(this).attr('value').split("_")[1]) === parseInt(timeslot.id)) {
										if(($(this).attr('class')).indexOf("available") > 0) {
											triggerTimeslot($(this));
										}
									}
								});
							}
							//console.log(Date.parse(timeslot.datetime).toString('HH:mm:ss').split(":")[0]);
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
						//console.log((dateArray[i]).toString('dd MMM yyyy'));
                        th.html(headerVal + "<br/><b> Select All <input class='checkBoxClass' type='checkbox' name='" + dateArray[i] + "' id='" + dateArray[i] + "'/>");
                        thead.append(th);
                    }					
					
                    //Inserting constructed table header into table
                    $("." + tableClass).append($(document.createElement('thead')).append(thead));

                    //Creating table body with times and empty cells
//                    var tbody = $(document.createElement("tbody"));

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
                                if (timeslot.isMyTeam) {
                                    td.html("<span class='teamName'>" + timeslot.team + "</span>");
                                    td.attr("align", "center");
                                    td.addClass("teamExists");
                                }
								td.addClass('markable border-top');
								td.attr("value", "timeslot_" + timeslot.id);
								td.attr("id",j);
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
						//Unavaiable a timeslot
						var $prevTr = $timeslotCell.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($timeslotCell.index()).children('div.start-marker').length) {
								//Unselect this timeslot
								var $nextTr = $prevTr;
								for (var j = 0; j < slotSize; j++) {
									$nextTr.children().eq($timeslotCell.index()).removeClass('chosen');
									$nextTr.children().eq($timeslotCell.index()).addClass('unavailable');
									$nextTr = $nextTr.next();
								}
								break;
							}
							$prevTr = $prevTr.prev();
						}
					} else if ($timeslotCell.hasClass('unavailable')) {
						//Available a timeslot
						var $prevTr = $timeslotCell.closest('tr');
						for (var i = slotSize; i > 0; i--) {
							if ($prevTr.children().eq($timeslotCell.index()).children('div.start-marker').length) {
								//Unselect this timeslot
								var $nextTr = $prevTr;
								for (var j = 0; j < slotSize; j++) {
									$nextTr.children().eq($timeslotCell.index()).removeClass('unavailable');
									$nextTr.children().eq($timeslotCell.index()).addClass('chosen');
									$nextTr = $nextTr.next();
								}
								break;
							}
							$prevTr = $prevTr.prev();
						}
					} else {
						//Select a timeslot
						var $nextTr = $timeslotCell.closest('tr');
						if ($nextTr.parent().children().index($nextTr) + slotSize > $nextTr.parent().children().length) return false; //Invalid timeslot
						$timeslotCell.append($(document.createElement('div')).addClass('start-marker'));
						for (var i = 0; i < slotSize; i++) {
							$nextTr.children().eq($timeslotCell.index()).addClass('chosen');
							$nextTr = $nextTr.next();
						}
					}
					return false;
                }

                $('body').on('click', 'td.chosen , td.unavailable', function(e){
                    triggerTimeslot($(this));
                });
				
				//Hover glow effect
                $('body').on('mouseenter', '.timeslotsTable tr:not(:has(table, th)) td:not(:first-child)', function(e) {
					var $td = $(this);
					var slotSize = scheduleData.duration / 30;
					if ($td.hasClass('chosen') || $td.hasClass('unavailable')) {
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
                        if (self.hasClass("markable")) {
                            triggerTimeslot(self);
                        }
                    });
                    $(".teamExists").each(function(){
                        var tr = $(this).parent();
                        var tbody = $(this).parents("tbody");
                        var row = tr.parent().children().index(tr);
                        var nextRow = $(tbody).children().get(row + i);
                        var slotSize = scheduleData.duration / 30;
                        var col = $(this).parent().children().index(this);
                        for (var i = 0; i < slotSize; i++) {
                            var nextRow = $(tbody).children().get(row + i);
                            var nextCell = $(nextRow).children().get(col);
                            $(nextCell).addClass('teamExists');
                        }
                    });
                }

                function populateUnavailableTimeslots(scheduleData) {
                    $(".timeslotcell").each(function() {
                        var self = $(this);
                        for (var i = 0; i < unavailableTimeslots.length; i++) {
                            if (self.attr('value') === unavailableTimeslots[i]) {
                                triggerTimeslot(self);
                            }
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
                // Change Supervisor Availability
                //------------------------------------------//

                //Update Timeslots AJAX Call            
                $("#editTimeslotsSubmitBtn").on('click', function() {
                    $("#editTimeslotsSubmitBtn").button('loading');
                    //SerializeArray not functional for timeslots
                    var timeslotsData = {};
                    var timeslot_data = new Array();

                    var allTimeslots = $("td.unavailable > div.start-marker", ".timeslotsTable").get();
                    for (var i = 0; i < allTimeslots.length; i++) {
                        var obj = allTimeslots[i];
                        timeslot_data.push($(obj).parent().attr("value"));
                    }
                    timeslotsData["timeslot_data[]"] = timeslot_data;
                    timeslotsData["scheduleId"] = scheduleData.id;
                    $.ajax({
                        type: 'POST',
                        url: 'setAvailabilityJson',
                        data: timeslotsData,
                        dataType: 'json'
                    }).done(function(response) {
                        if (!response.exception) {
                            if (response.success) {
                                showNotification("SUCCESS", "Timeslots saved");
                                $("#editTimeslotsSubmitBtn").button('reset');
								console.log('Unavailable: ' + JSON.stringify(response.unavailableTimeslots));
								unavailableTimeslots = response.unavailableTimeslots;
                            } else {
                                var eid = btoa(response.message);
                                console.log(response.message);
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

                //Display termMessage
                function displayMessage(id, msg, fade) {
                    //Dislay result
                    $("#" + id).fadeTo('slow', 100);
                    $("#" + id).css('color', 'darkgreen').html(msg);
                    if (fade) {
                        $("#" + id).css('color', 'darkred').html(msg).fadeTo('slow', 0);
                    }
                }
                
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

            addLoadEvent(supervisorAvailabilityLoad);
        </script>
    </body>
</html>
