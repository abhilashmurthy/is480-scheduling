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
%>
<html>
    <head>
        <title>TA Availability</title>
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
                background-color: #F7A8A8 !important ;
            }
            
            .teamExists {
                background-color: #F9FCBD !important;
            }
            
            .availabilityLegend {
                position: absolute;
                left: 70%;
                top: 12%;
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
                                        <td style="background-color:#B8F79E;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Available</td> 
                                        <!--<td style="background-color:#F7A8A8;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;I'm Unavailable</td>--> 
                                    </tr>
                                    <tr>
                                        <td style="background-color:#F7A8A8;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Chosen</td> 
                                    </tr>
                                    <tr>
                                        <td style="background-color:#F9FCBD;border:1px solid #1E647C;width:17px;"></td><td>&nbsp;Taken</td> 
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <table id="timeslotsTable" class="timeslotsTable table-condensed table-hover table-bordered table-striped"></table>
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
                var milestones = ["ACCEPTANCE", "MIDTERM", "FINAL"]; //TODO: Remove hardcoding

                loadUnavailableTimeslots();
//                loadScheduleDates();
                loadSelectDropdown();

                function loadUnavailableTimeslots() {
                    <s:iterator value="unavailableTimeslotIds">
                    unavailableTimeslots.push("timeslot_<s:property/>");
                    </s:iterator>
                }
                
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

                function loadScheduleDates() {
                    //Get acceptance schedule data
                    var milestoneStr = "ACCEPTANCE";
                    scheduleData = getScheduleData(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                    acceptanceId = scheduleData.id;
                    loadScheduleTimeslots(milestoneStr, scheduleData);

                    //Get midterm schedule data
                    var milestoneStr = "MIDTERM";
                    scheduleData = getScheduleData(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                    midtermId = scheduleData.id;
                    loadScheduleTimeslots(milestoneStr, scheduleData);

                    //Get final schedule data
                    var milestoneStr = "FINAL";
                    scheduleData = getScheduleData(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                    finalId = scheduleData.id;
                    loadScheduleTimeslots(milestoneStr, scheduleData);
                }

                function loadScheduleTimeslots(milestoneStr, scheduleData) {
                    var tableId = "timeslotsTable";
                    var table = $("#" + tableId);
//                    table.prev('h4').remove();
//                    table.before("<h4>" + milestoneStr.toUpperCase() + "</h4>"); //Add milestone title
                    makeTimeslotTable(tableId, scheduleData, getDistinctDates(scheduleData, "typeString"));
                    populateTimeslotsTable(tableId, scheduleData);
                    populateUnavailableTimeslots(tableId, scheduleData);
                }

                function getScheduleData(milestoneString, academicYearString, semesterString) {
                    var toReturn = null;
                    var data = {
                        milestone: milestoneString,
                        year: academicYearString,
                        semester: semesterString
                    };
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

                function makeTimeslotTable(tableId, scheduleData, dateArray) {
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
                    $("#" + tableId).append(thead);

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
                                td.attr("value", "timeslot_" + timeslot.id);
                                if (timeslot.hasOwnProperty("taId")) { //Timeslot has been chosen by some TA
									if (timeslot.taId === loggedInTaId) { //Slot chosen by logged in TA
										td.addClass("markable");
										td.addClass("unavailable");
									} else { //Slot chosen by someone else
										td.addClass("teamExists");
									}
								} else { //Timeslot is free for TA to choose
									td.addClass("markable");
								}
                            }
                            tr.append(td);
                        }
                        $("#" + tableId).append(tr);
                    }

                    //Inserting constructed table body into table
//                    $("#" + tableId).append(tbody);
                }

                /*
                 * METHOD TO MARK TIMESLOTS ON TABLE
                 */
                function triggerTimeslot(e, duration) {
                    if (!$(e).hasClass('markable')) return false;
                    var col = $(e).parent().children().index(e);
                    var tr = $(e).parent();
                    var row = $(tr).parent().children().index(tr);
                    var tbody = $(e).parents('.timeslotsTable').children('tbody');
                    var slotSize = duration / 30;
                    
                    if ($(e).hasClass("chosen")) {
                        //Section for a cell thats available
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

                        //Add unavailable class
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
                            $(e).addClass("unavailable");
                            var marker = document.createElement("div");
                            $(marker).addClass("start-marker");
                            $(e).append(marker);
                            for (i = 1; i < slotSize; i++) {
                                var nextRow = $(tbody).children().get(row + i);
                                var nextCell = $(nextRow).children().get(col);
                                $(nextCell).addClass("unavailable");
                            }
                        }

                    } else {
                        //Section for a cell thats available
                        if ($(e).children().index(".start-marker") !== -1) {
                            $(e).removeClass("unavailable");
                            $(e).children().remove();
                            for (i = 1; i < slotSize; i++) {
                                var nextRow = $(tbody).children().get(row + i);
                                var nextCell = $(nextRow).children().get(col);
                                $(nextCell).removeClass("unavailable");
                            }
                        }

                        //Checking if there will be an overlap of timeslots
                        //Abort if there is going to be an overlap
                        for (i = 1; i < slotSize; i++) {
                            var nextRow = $(tbody).children().get(row + i);
                            var nextCell = $(nextRow).children().get(col);
                            if ($(nextCell).hasClass("unavailable")) {
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

                $('body').on('click', 'td.chosen , td.unavailable', function(e){
                    triggerTimeslot(e.target, scheduleData.duration);
                });

                function populateTimeslotsTable(tableId, scheduleData) {
                    $(".timeslotcell").each(function(e) {
                        var self = $(this);
                        if (self.hasClass("markable")) {
                            triggerTimeslot(this, scheduleData.duration);
                        }
                    });
					$(".unavailable").each(function(e) {
                        triggerTimeslot(this, scheduleData.duration);
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

                function populateUnavailableTimeslots(tableId, scheduleData) {
                    $(".timeslotcell").each(function() {
                        var self = $(this);
                        for (var i = 0; i < unavailableTimeslots.length; i++) {
                            if (self.attr('value') === unavailableTimeslots[i]) {
                                triggerTimeslot(this, scheduleData.duration);
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

                    var allTimeslots = $("td.unavailable > div.start-marker", "#timeslotsTable").get();
                    for (var i = 0; i < allTimeslots.length; i++) {
                        var obj = allTimeslots[i];
						var timeslotId = parseInt(($(obj).parent().attr("value").split("_"))[1]);
                        timeslot_data.push(timeslotId);
                    }

                    console.log('TA chosen data is: ' + JSON.stringify(timeslot_data));
					timeslotData["timeslots"] = timeslot_data;
					timeslotsData["scheduleId"] = scheduleData.id;
                    $.ajax({
                        type: 'POST',
                        url: 'taSignupJson',
                        data: JSON.stringify(timeslotsData),
                        dataType: 'json'
                    }).done(function(response) {
                        if (!response.exception) {
                            if (response.success) {
                                showNotification("SUCCESS", "Chosen slots saved");
                                $("#editTimeslotsSubmitBtn").button('reset');
                                setTimeout(function(){window.location.reload();}, 1000);
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

            addLoadEvent(taAvailabilityLoad);
        </script>
    </body>
</html>
