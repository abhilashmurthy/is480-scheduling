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
        <title>Create Term</title>
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
                text-align: left;
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
                padding: 20px 10px 20px 10px !important;
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
            
            .scheduleDayTimeSelect {
                width: 45px;
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
                    <li class="createTermTab active">
                        <a href="#createTermTab" data-toggle="tab">Create Term</a>
                    </li>
                    <li class="createScheduleTab">
                        <a href="#createScheduleTab" data-toggle="tab">Create Schedule</a>
                    </li>
                    <li class="createTimeslotsTab">
                        <a href="#createTimeslotsTab" data-toggle="tab">Create Timeslots</a>
                    </li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active" id="createTermTab">
                        <!-- Create Term -->
                        <div id="createTermPanel" class="schedulePanel">
                            <h3>Create Term</h3>
                            <form id="createTermForm">
                                <table id="createTermTable">
                                    <tr>
                                        <td class="formLabelTd">
                                            Choose Year
                                        </td>
                                        <td> <!-- Putting default values for testing purposes -->
                                            <div class="input-append">
                                                <input id="yearInput" type="text" name="year" value="<%= nextYear %>" disabled/>
                                                <div class="btn-group">
                                                    <button class="btn" type="button" id="plusYearBtn">&#9650;</button>
                                                    <button class="btn" type="button" id="minusYearBtn">&#9660;</button>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="formLabelTd">
                                            Semester Name
                                        </td>
                                        <td>
                                            <input id="semesterInput" type="text" name="semester" placeholder="<%= nextSem %>"/>
                                        </td>
                                    </tr>
                                    <tr id="createTermSubmitRow"><td></td><td><input id="createTermSubmitBtn" type="submit" class="btn btn-primary" value="Create" data-loading-text="Done"/></td></tr>
                                </table>
                            </form>
                            <h4 id="termResultMessage"></h4>
                        </div>
                    </div>
                    <div class="tab-pane" id="createScheduleTab">
                        <!-- Create Schedule -->
                        <div id="createSchedulePanel" class="schedulePanel">
                            <h3 id="createScheduleTitle">Create Schedule</h3>
                            <form id="createScheduleForm">
                                <table id="createScheduleTable">
                                    <tr><th>Milestone</th><th>Dates</th><th>Day Start</th><th>Day End</th></tr>
                                    <tr id="createScheduleSubmitRow"><td></td><td><input id="createScheduleSubmitBtn" type="submit" value="Create" data-loading-text="Done" class="btn btn-primary"/></td></tr>
                                </table>
                            </form>
                            <h4 id="scheduleResultMessage"></h4>
                        </div>
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

                var termData = null;
                var scheduleData = null;
                var timeslotsData = null;
                var acceptanceId = null;
                var midtermId = null;
                var finalId = null;
                
                //Initialize variables
                var milestones = null;
                var schedules = null;
                var selectedSchedule = null;

                /*----------------------------------------
                 CREATE TERM
                 ------------------------------------------*/
                 
                //Plus Minus Buttons 
                $("#plusYearBtn").on('click', function(){
                    var currentYear = parseInt($("#yearInput").val());
                    $("#yearInput").attr('value', (currentYear + 1));
                });
                $("#minusYearBtn").on('click', function(){
                    var currentYear = parseInt($("#yearInput").val());
                    $("#yearInput").attr('value', (currentYear - 1));
                });

                //Create Term AJAX Call
                $("#createTermForm").on('submit', function() {
                    $("#createTermSubmitBtn").button('loading');
                    $("#yearInput").attr('disabled', false);
                    termData = {
                        year: $("#yearInput").val(),
                        semester: $("#semesterInput").val()
                    };
                    $.ajax({
                        type: 'GET',
                        url: 'createTermJson',
                        data: {jsonData: JSON.stringify(termData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            displayMessage("termResultMessage", "Term added", false);
                            setTimeout(function(){displayCreateSchedule(response);}, 2000);
                        } else {
                            //Display error message
                            displayMessage("termResultMessage", response.message, true);
                            $("#createTermSubmitBtn").button('reset');
                        }

                    }).fail(function(error) {
                        console.log("Create Term Form AJAX Fail");
                    });
                    $("#yearInput").attr('disabled', true);
                    return false;
                });
                
                /*----------------------------------------
                 CREATE SCHEDULE
                 ------------------------------------------*/

                //Display Schedule
                function displayCreateSchedule(data) {
                
                    //Add milestones info
                    milestones = data.milestones;
                
                    //Display Create Schedule
                    $("#createSchedulePanel").show();
                        
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
                        var milestoneTd = $(document.createElement('td'));
                            milestoneTd.addClass('formLabelTd');
                            milestoneTd.html(milestone.name);
                            milestoneTr.append(milestoneTd);
                        //Milestone dates[]
                        var milestoneDatesTd = $(document.createElement('td'));
                            var milestoneInput = $(document.createElement('input'));
                            milestoneInput.attr('type', 'text');
                            milestoneInput.attr('name', milestone.name.toLowerCase() + "Dates");
                            milestoneInput.attr('id', "milestone_" + milestone.id);
                            milestoneInput.attr('class', "milestoneOrder_" + milestone.order);
                            milestoneInput.addClass('input-medium datepicker');
                            milestoneInput.multiDatesPicker({
                                dateFormat: "yy-mm-dd",
                                defaultDate: Date.today(),
                                minDate: Date.today(),
                                beforeShowDay: $.datepicker.noWeekends
                            });
                            milestoneDatesTd.append(milestoneInput);
                        milestoneTr.append(milestoneDatesTd);
                        //Milestone Day Start
                        var milestoneDayStartTd = $(document.createElement('td'));
                            var milestoneInput = $(document.createElement('input'));
                                milestoneInput.attr('type', 'text');
                                milestoneInput.attr('id', "milestoneDayStart_" + milestone.id);
                                milestoneInput.attr('class', "scheduleDayTimeSelect");
                                milestoneInput.attr('name', milestone.name.toLowerCase() + "DayStartTime");
                                milestoneInput.timepicker({
                                    minTime: '07:00',
                                    maxTime: '20:00',
                                    step: 60,
                                    forceRoundTime: true,
                                    timeFormat: 'H:i',
                                    scrollDefaultTime: '09:00'
                                });
                                milestoneInput.attr('value', '09:00');
                            milestoneDayStartTd.append(milestoneInput);
                        milestoneTr.append(milestoneDayStartTd);
                        var milestoneDayEndTd = $(document.createElement('td'));
                            var milestoneInput = $(document.createElement('input'));
                                milestoneInput.attr('type', 'text');
                                milestoneInput.attr('id', "milestoneDayEnd_" + milestone.id);
                                milestoneInput.attr('class', "scheduleDayTimeSelect");
                                milestoneInput.attr('name', milestone.name.toLowerCase() + "DayEndTime");
                                milestoneInput.timepicker({
                                    minTime: '07:00',
                                    maxTime: '20:00',
                                    step: 60,
                                    forceRoundTime: true,
                                    timeFormat: 'H:i',
                                    scrollDefaultTime: '18:00'
                                });
                                milestoneInput.attr('value', '18:00');
                            milestoneDayEndTd.append(milestoneInput);
                        milestoneTr.append(milestoneDayEndTd);
                        milestoneTr.insertBefore('#createScheduleSubmitRow');
                    }
                    $(".createScheduleTab a").tab('show');
                    console.log("Showed tab");
                    
                    //Reset Disabled Dates for validation
                    for (var i = 0; i < milestones.length; i++) {
                        var milestone = milestones[i];
                        if (milestone.order > 1) {
                            resetDisabledDates("milestoneOrder_" + (milestone.order - 1), "milestoneOrder_" + milestone.order);
                        }
                    }
                }

                function resetDisabledDates(first, second) {
                    //Mouseover required to completely reset the datepicker
                    $("." + second).on('mouseover', function() {
                        $("." + second).datepicker("destroy");
                    });

                    //Limits the dates to > end date of previous milestone
                    $("." + second).on('mousedown', function() {
                        //Enable only if acceptance has values
                        var acceptanceDates = $("." + first).multiDatesPicker('getDates');
                        if (acceptanceDates) {
                            var lastAcceptanceDate = acceptanceDates[acceptanceDates.length - 1];
                            if (lastAcceptanceDate) {
                                $("." + second).multiDatesPicker({
                                    dateFormat: "yy-mm-dd",
                                    minDate: new Date(lastAcceptanceDate).addDays(1),
                                    beforeShowDay: $.datepicker.noWeekends
                                });
                                console.log("Reset");
                            }
                        }
                    });
                }

                //Create Schedule Submit - Show timeslots panel
                $("#createScheduleForm").on('submit', function(e) {
                    $("#createScheduleSubmitBtn").button('loading');
                    e.preventDefault();
                    e.stopPropagation();
                    //AJAX call to save term and schedule dates
                    var milestoneArray = $(this).serializeArray();
                    for (var i = 0; i < milestoneArray.length; i++) {
                        var milestoneItem = milestoneArray[i];
                        for (var j = 0; j < milestones.length; j++) {
                            var milestone = milestones[j];
                            if (milestoneItem.name.split("Dates")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                milestone["dates[]"] = milestoneItem.value.split(",");
                            }
                            if (milestoneItem.name.split("DayStartTime")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                milestone["dayStartTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                            if (milestoneItem.name.split("DayEndTime")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                milestone["dayEndTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                        }
                    }
//                    console.log("Milestone array is now: " + JSON.stringify(milestones));
                    var createScheduleData = {"milestones[]":milestones};
                    $.ajax({
                        type: 'POST',
                        url: 'createScheduleJson',
                        data: {jsonData: JSON.stringify(createScheduleData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            console.log("Received: " + JSON.stringify(response));
                            schedules = response.schedules;
                            //Display create timeslots forms
                            setTimeout(function(){displayCreateTimeslots();}, 2000);
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        console.log("createScheduleData AJAX FAIL");
                        displayMessage("scheduleResultMessage", "Oops.. something went wrong", true);
                    });
                    return false;
                });

                /*----------------------------------------
                 CREATE TIMESLOTS
                 ------------------------------------------*/

                //Display create timeslots
                //TODO: Change to MilestoneConfig
                function displayCreateTimeslots() {
                    //Display create timeslots
                    $("#createTimeslotsPanel").show();
                    
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
                
                //OVERALL SUBMIT TO SERVER
                $("#createTimeslotsSubmitBtn").on('click', function() {
                    $("#createTimeslotsSubmitBtn").button('loading');
                    timeslotsData = {};
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
                            displayMessage("timeslotResultMessage", response.message, false);
                            selectedSchedule["isCreated"] = true;
                        } else {
                            var eid = btoa(response.message);
                            console.log(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        console.log("createTimeslotsJson AJAX FAIL");
                        displayMessage("timeslotResultMessage", "Oops.. something went wrong", true);
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
                    var col = $(e).parent().children().index(e);
                    var tr = $(e).parent();
                    var row = $(tr).parent().children().index(tr);
                    var tbody = $(e).parents("tbody");
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
                    $(".timeslotsTable").find(".timeslotcell").each(function() {
                        triggerTimeslot(this, duration);
                    });
                }

                //Display termMessage
                function displayMessage(id, msg, fade) {
                    //Dislay result
                    var e = $("#" + id);
                    $(e).fadeTo('slow', 2000);
                    $(e).css('color', 'darkgreen').html(msg).fadeTo('slow', 0);
                    if (fade) {
                        $(e).css('color', 'darkred').html(msg).fadeTo('slow', 0);
                    }
                }

            };
            
            addLoadEvent(createScheduleLoad);
        </script>
    </body>
</html>
