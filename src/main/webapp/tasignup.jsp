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
                /*border-bottom: 1px solid black;*/
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
                border-left: 5px solid transparent;
                border-right: 5px solid transparent;
                border-top: 10px solid #5C7AFF;
                z-index: 1;
            }
            .chosen {
                background-color: #B8F79E !important ;
            }
            .unavailable {
                background-color: #00C918 !important ;
            }
            
            .teamExists {
                background-color: #F9FCBD !important;
            }
            
            .availabilityLegend {
                position: absolute;
                left: 70%;
                top: 12%;
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
            if (activeRole != Role.TA) {
                request.setAttribute("error", "You need to be a TA to view this page");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
        %>

        <!-- Edit Availability -->
        <div id="availabilityPanel" class="container">
            <div id="editTimeslotsPanel">
                <h3>Your Availability</h3>
                <div id="timeslotsTableSection">
                    <table>
                        <tr>
                            <td>Milestone</td>
                            <td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <button id="editTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Saving...">Save</button>
                            </td>
                            <td>
                                <table class='availabilityLegend'>
                                    <tr>
                                        <!-- <td style="width:50px"><b>Legend:</b></td>-->
                                        <td style="background-color:#B8F79E;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Available Slot</td> 
                                    </tr>
                                    <tr>
                                        <td style="background-color:#00C918;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;You signed up</td> 
                                    </tr>
                                    <tr>
                                        <td style="background-color:#F9FCBD;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Unavailable Slot</td> 
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <table class="timeslotsTable table-condensed table-hover table-bordered table-striped"></table>
                            </td>
                        </tr>
                    </table>
                </div>
                <h4 id="timeslotsResultMessage" class="resultMessage"/></h4>
                <br/><br/>
            </div>
        </div>

        <%@include file="footer.jsp" %>
        <!-- jshashset imports -->
        <script type="text/javascript" src="js/plugins/jshashtable-3.0.js"></script>
        <script type="text/javascript" src="js/plugins/jshashset-3.0.js"></script>
        <script type="text/javascript" src="js/plugins/jquery-ui.multidatespicker.js"></script>
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
                var acceptanceId = null;
                var midtermId = null;
                var finalId = null;
                var unavailableTimeslots = new Array();
                var scheduleData = null;
                var selectedMilestone = null;
                var milestones = new Array();

                loadMilestones();
                loadSelectDropdown();
                
                function loadMilestones() {
                    var milestonesData = getScheduleData(null, activeAcademicYearStr, activeSemesterStr);
                    for (var i = 0; i < milestonesData.milestones.length; i++) {
                        milestones.push(milestonesData.milestones[i].name);
                    }
                };
                
                function loadSelectDropdown() {
                    for (var i = 0; i < milestones.length; i++) {
                        var milestoneOption = $(document.createElement('option'));
                        milestoneOption.attr('value', milestones[i]);
                        milestoneOption.html(milestones[i]);
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
                
                $("#milestoneTimeslotsSelect").val(milestones[0]).change(); //Select first milestone

                function loadScheduleTimeslots(milestoneStr, scheduleData) {
                    var tableClass = "timeslotsTable";
                    var table = $("." + tableClass);
                    makeTimeslotTable(tableClass, scheduleData, getDistinctDates(scheduleData, "typeString"));
					convertScheduleData();
                    populateTimeslotsTable(tableClass, scheduleData);
                    populateUnavailableTimeslots(tableClass, scheduleData);
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
				
                function convertScheduleData() {
                    var timeslots = scheduleData.timeslots;
                    var newTimeslots = {};
                    for (var i = 0; i < timeslots.length; i++) {
                        var timeslot = timeslots[i];
                        var key = timeslot.id;
                        newTimeslots[key] = timeslot;
                    }
                    scheduleData["timeslots"] = newTimeslots;
                }

                function makeTimeslotTable(tableClass, scheduleData, dateArray) {
                    var thead = $(document.createElement("tr"));
                    var minTime = 9;
                    var maxTime = 19;

                    //Creating table header with dates
                    thead.append("<td></td>"); //Empty cell for time column
                    for (i = 0; i < dateArray.length; i++) {
                        var th = $(document.createElement("td"));
                        var headerVal = new Date(dateArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(dateArray[i]).toString('ddd');
                        th.html(headerVal);
                        thead.append(th);
                    }
                    //Inserting constructed table header into table
                    $("." + tableClass).append(thead);

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
								td.addClass("markable");
								td.attr("value", "timeslot_" + timeslot.id);
                            }
                            tr.append(td);
                        }
                        $("." + tableClass).append(tr);
                    }

                    //Inserting constructed table body into table
//                    $("#" + tableClass).append(tbody);
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
				

                $('body').on('click', 'td.chosen , td.unavailable', function(e){
                    triggerTimeslot($(this));
                });

                function populateTimeslotsTable(tableClass, scheduleData) {
                    $(".timeslotcell").each(function(e) {
                        var self = $(this);
                        if (self.hasClass("markable")) {
                            triggerTimeslot(self);
                        }
                    });
                }

                function populateUnavailableTimeslots(tableClass, scheduleData) {
                    $(".timeslotcell").each(function() {
                        var self = $(this);
						if (self.attr('value') && scheduleData.timeslots[self.attr('value').split("_")[1]].taId === loggedInTaId) {
							triggerTimeslot(self);
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
						var timeslotId = parseInt(($(obj).parent().attr("value").split("_"))[1]);
                        timeslot_data.push(timeslotId);
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
                                showNotification("SUCCESS", "Chosen slots saved");
                                $("#editTimeslotsSubmitBtn").button('reset');
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
                            opts.title = "Updated";
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
