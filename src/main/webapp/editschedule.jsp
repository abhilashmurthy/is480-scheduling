<%-- 
    Document   : AcceptReject
    Editd on : Jul 2, 2013, 11:14:06 PM
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
                    <li class="editTermTab active">
                        <a href="#editTermTab" data-toggle="tab">Edit Term</a>
                    </li>
                    <li class="editScheduleTab">
                        <a href="#editScheduleTab" data-toggle="tab">Edit Schedule</a>
                    </li>
                    <li class="editTimeslotsTab">
                        <a href="#editTimeslotsTab" data-toggle="tab">Edit Timeslots</a>
                    </li>
                </ul>
                <div class="tab-content">
                    <div class="tab-pane active" id="editTermTab">
                        <!-- Edit Term -->
                        <div id="editTermPanel" class="schedulePanel">
                            <h3>Edit Term</h3>
                            <form id="editTermForm">
                                <table id="editTermTable">
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
                                    <tr id="editTermSubmitRow"><td></td><td><input id="editTermSubmitBtn" type="submit" class="btn btn-primary" value="Save" data-loading-text="Done"/></td></tr>
                                </table>
                            </form>
                            <h4 id="termResultMessage"></h4>
                        </div>
                    </div>
                    <div class="tab-pane" id="editScheduleTab">
                        <!-- Edit Schedule -->
                        <div id="editSchedulePanel" class="schedulePanel">
                            <h3 id="editScheduleTitle">Edit Schedule</h3>
                            <form id="editScheduleForm">
                                <table id="editScheduleTable">
                                    <tr><th>Milestone</th><th>Dates</th><th>Day Start</th><th>Day End</th></tr>
                                    <tr id="editScheduleSubmitRow"><td></td><td><input id="editScheduleSubmitBtn" type="submit" value="Save" data-loading-text="Done" class="btn btn-primary"/></td></tr>
                                </table>
                            </form>
                            <h4 id="scheduleResultMessage"></h4>
                        </div>
                    </div>
                    <div class="tab-pane" id="editTimeslotsTab">
                        <!-- Edit Timeslots -->
                        <div id="editTimeslotsPanel" class="schedulePanel">
                            <h3 id="editTimeslotsTitle">Edit Timeslots</h3>
                            <table id="editTimeslotsTable">
                                <tr><td>Milestone</td><td><select name="milestoneTimeslots" id="milestoneTimeslotsSelect"></select></td></tr>
                                <tr><td>Venue</td><td><input id="venueInput" type="text" name="venue" placeholder="SIS Seminar Room 2-1"/></td><td><button id="editTimeslotsSubmitBtn" class="btn btn-primary" data-loading-text="Done">Edit</button></td></tr>
                                <tr><td></td><td><table class="timeslotsTable table-condensed table-hover table-bordered table-striped" hidden></table></td></tr>
                            </table>
                            <h4 id="timeslotResultMessage"></h4>
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
                var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
                var activeSemesterStr = "<%= activeTerm.getSemester()%>";

                var scheduleData = null;
                var milestones = null;
                
                //Initialize variables
                var milestones = null;
                var schedules = null;
                var selectedSchedule = null;
                
                loadInitialValues();
                
                function loadInitialValues() {
                    $("#yearInput").val(activeAcademicYearStr);
                    $("#semesterInput").val(activeSemesterStr);
                    loadScheduleDates();
                }
                
                function loadScheduleDates() {
                    milestones = getScheduleData(null, activeAcademicYearStr, activeSemesterStr).milestones;
                    for (var i = 0; i < milestones.length; i++) {
                        var milestone = milestones[i];
                        scheduleData = getScheduleData(milestone.name, activeAcademicYearStr, activeSemesterStr);
                        milestone["scheduleId"] = scheduleData.id;
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
                            var distinctDates = getDistinctDates("typeString");
                            milestoneInput.multiDatesPicker({
                                defaultDate: distinctDates[0],
                                dateFormat: "yy-mm-dd",
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
                                    maxTime: '18:00',
                                    step: 60,
                                    forceRoundTime: true,
                                    timeFormat: 'H:i',
                                    scrollDefaultTime: '09:00'
                                });
                                milestoneInput.attr('value', Date.parse(scheduleData.dayStartTime + ":00").toString("HH:mm"));
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
                                    maxTime: '21:00',
                                    step: 60,
                                    forceRoundTime: true,
                                    timeFormat: 'H:i',
                                    scrollDefaultTime: '18:00'
                                });
                                milestoneInput.attr('value', Date.parse(scheduleData.dayEndTime + ":00").toString("HH:mm"));
                            milestoneDayEndTd.append(milestoneInput);
                        milestoneTr.append(milestoneDayEndTd);
                        milestoneTr.insertBefore('#editScheduleSubmitRow');
                        $("#milestone_" + milestone.id).val(distinctDates).change();;
                        $("#milestone_" + milestone.id).multiDatesPicker('addDates', distinctDates);
                        $("#milestone_" + milestone.id).datepicker('refresh');
                    }
                }
                
                function getScheduleData(milestoneString, academicYearString, semesterString) {
                    var toReturn = null;
                    var data = {
                        year: academicYearString,
                        semester: semesterString
                    };
                    if (milestoneString) {
                        data["milestone"] = milestoneString;
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
                
                function getDistinctDates(type) {
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

                //Edit Term AJAX Call
                $("#editTermForm").on('submit', function() {
                    $("#editTermSubmitBtn").button('loading');
                    $("#yearInput").attr('disabled', false);
                    termData = {
                        year: $("#yearInput").val(),
                        semester: $("#semesterInput").val()
                    };
                    $.ajax({
                        type: 'GET',
                        url: 'updateTermJson',
                        data: {jsonData: JSON.stringify(termData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            showNotification("SUCCESS", "Term updated");
                        } else {
                            showNotification("ERROR", response.message);
                        }
                        $("#editTermSubmitBtn").button('reset');
                    }).fail(function(error) {
                        console.log("Edit Term Form AJAX Fail");
                    });
                    $("#yearInput").attr('disabled', true);
                    return false;
                });
                
                /*----------------------------------------
                 CREATE SCHEDULE
                 ------------------------------------------*/

                //Display Schedule
//                function displayEditSchedule(data) {
//                
//                    //Add milestones info
//                    milestones = data.milestones;
//                
//                    //Display Edit Schedule
//                    $("#editSchedulePanel").show();
//                        
//                    //Order comparator
//                    function compare(a, b) {
//                        if (a.order < b.order) {
//                            return -1;
//                        } else if (a.order > b.order) {
//                            return 1;
//                        } else {
//                            return 0;
//                        }
//                    }
//                        
//                    milestones.sort(compare); //Sort by order first
//                    for (var i = 0; i < milestones.length; i++) {
//                        var milestone = milestones[i];
//                        var milestoneTr = $(document.editElement('tr'));
//                        //Milestone name
//                        var milestoneTd = $(document.editElement('td'));
//                            milestoneTd.addClass('formLabelTd');
//                            milestoneTd.html(milestone.name);
//                            milestoneTr.append(milestoneTd);
//                        //Milestone dates[]
//                        var milestoneDatesTd = $(document.editElement('td'));
//                            var milestoneInput = $(document.editElement('input'));
//                            milestoneInput.attr('type', 'text');
//                            milestoneInput.attr('name', milestone.name.toLowerCase() + "Dates");
//                            milestoneInput.attr('id', "milestone_" + milestone.id);
//                            milestoneInput.attr('class', "milestoneOrder_" + milestone.order);
//                            milestoneInput.addClass('input-medium datepicker');
//                            milestoneInput.multiDatesPicker({
//                                dateFormat: "yy-mm-dd",
//                                defaultDate: Date.today(),
//                                minDate: Date.today(),
//                                beforeShowDay: $.datepicker.noWeekends
//                            });
//                            milestoneDatesTd.append(milestoneInput);
//                        milestoneTr.append(milestoneDatesTd);
//                        //Milestone Day Start
//                        var milestoneDayStartTd = $(document.editElement('td'));
//                            var milestoneInput = $(document.editElement('input'));
//                                milestoneInput.attr('type', 'text');
//                                milestoneInput.attr('id', "milestoneDayStart_" + milestone.id);
//                                milestoneInput.attr('class', "scheduleDayTimeSelect");
//                                milestoneInput.attr('name', milestone.name.toLowerCase() + "DayStartTime");
//                                milestoneInput.timepicker({
//                                    minTime: '07:00',
//                                    maxTime: '18:00',
//                                    step: 60,
//                                    forceRoundTime: true,
//                                    timeFormat: 'H:i',
//                                    scrollDefaultTime: '09:00'
//                                });
//                                milestoneInput.attr('value', '09:00');
//                            milestoneDayStartTd.append(milestoneInput);
//                        milestoneTr.append(milestoneDayStartTd);
//                        var milestoneDayEndTd = $(document.editElement('td'));
//                            var milestoneInput = $(document.editElement('input'));
//                                milestoneInput.attr('type', 'text');
//                                milestoneInput.attr('id', "milestoneDayEnd_" + milestone.id);
//                                milestoneInput.attr('class', "scheduleDayTimeSelect");
//                                milestoneInput.attr('name', milestone.name.toLowerCase() + "DayEndTime");
//                                milestoneInput.timepicker({
//                                    minTime: '07:00',
//                                    maxTime: '21:00',
//                                    step: 60,
//                                    forceRoundTime: true,
//                                    timeFormat: 'H:i',
//                                    scrollDefaultTime: '18:00'
//                                });
//                                milestoneInput.attr('value', '18:00');
//                            milestoneDayEndTd.append(milestoneInput);
//                        milestoneTr.append(milestoneDayEndTd);
//                        milestoneTr.insertBefore('#editScheduleSubmitRow');
//                    }
//                    $(".editScheduleTab a").tab('show');
//                    console.log("Showed tab");
//                    
//                    //Reset Disabled Dates for validation
//                    for (var i = 0; i < milestones.length; i++) {
//                        var milestone = milestones[i];
//                        if (milestone.order > 1) {
//                            resetDisabledDates("milestoneOrder_" + (milestone.order - 1), "milestoneOrder_" + milestone.order);
//                        }
//                    }
//                }
                
                //Reset Dates On Change
                function resetDisabledDates(first, second) {
                    var firstDates = $("." + first).multiDatesPicker('getDates');
                    var lastFirstDate = null;
                    if (firstDates) lastFirstDate = firstDates[firstDates.length - 1];
                    
                    //Disable consequent select dates when first dates are not selected 
                    if (!lastFirstDate) {
                        $("." + second).attr('disabled', true);
                    } else {
                        $("." + second).attr('disabled', false);
                    }
                    
                    $("body").on('mouseover', function(){
                        firstDates = $("." + first).multiDatesPicker('getDates');
                        if (firstDates) {
                            lastFirstDate = firstDates[firstDates.length - 1];
                            if (lastFirstDate) {
                                $("." + second).attr('disabled', false);
                            } else {
                                $("." + second).attr('disabled', true);
                            };
                        }
                    });
                    
                    //Mouseover required to completely reset the datepicker
                    $("." + second).on('mouseover', function() {
                        $("." + second).datepicker("destroy");
                    });

                    //Limits the dates to > end date of previous milestone
                    $("." + second).on('mousedown', function() {
                        //Enable only if acceptance has values
                        if (lastFirstDate) {
                            $("." + second).multiDatesPicker({
                                dateFormat: "yy-mm-dd",
                                minDate: new Date(lastFirstDate).addDays(1),
                                beforeShowDay: $.datepicker.noWeekends
                            });
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

                //Edit Schedule Submit - Show timeslots panel
                $("#editScheduleForm").on('submit', function(e) {
                    $("#editScheduleSubmitBtn").button('loading');
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
                                $("#editScheduleSubmitBtn").button('reset');
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
                                    $("#editScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dates[]"] = milestoneItem.value.split(",");
                            }
                            if (milestoneItem.name.split("DayStartTime")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", errorMessage);
                                    $("#editScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dayStartTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                            if (milestoneItem.name.split("DayEndTime")[0].toLowerCase() === milestone.name.toLowerCase()) {
                                if (milestoneItem.value.length < 1) {
                                    showNotification("WARNING", errorMessage);
                                    $("#editScheduleSubmitBtn").button('reset');
                                    return false;
                                }
                                milestone["dayEndTime"] = Date.parse(milestoneItem.value).toString('H');
                            }
                        }
                    }
                    console.log("Milestone array is now: " + JSON.stringify(milestones));
                    var editScheduleData = {"milestones[]":milestones};
                    $.ajax({
                        type: 'POST',
                        url: 'updateScheduleJson',
                        data: {jsonData: JSON.stringify(editScheduleData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            console.log("Received: " + JSON.stringify(response));
                            schedules = response.schedules;
                            showNotification("SUCCESS", "Edited dates successfully");
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                        $("#editScheduleSubmitBtn").button('reset');
                    }).fail(function(error) {
                        console.log("editScheduleData AJAX FAIL");
                        showNotification("ERROR", "Oops.. something went wrong");
                    });
                    return false;
                });

                /*----------------------------------------
                 CREATE TIMESLOTS
                 ------------------------------------------*/

                //Display edit timeslots
                function displayEditTimeslots() {
                    //Display edit timeslots
                    $("#editTimeslotsPanel").show();
                    $(".editTimeslotsTab a").attr('data-toggle', 'tab');
                    for (var i = 0; i < schedules.length; i++) {
                        var schedule = schedules[i];
                        var milestoneOption = $(document.editElement('option'));
                        milestoneOption.attr('value', schedule.id);
                        milestoneOption.html(schedule.milestoneName);
                        $("#milestoneTimeslotsSelect").append(milestoneOption);
                    }
                    
                    $("#milestoneTimeslotsSelect").val(schedules[0].milestoneName).change(); //Select first milestone
                    $(".editTimeslotsTab a").tab('show');
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
                    $("#editTimeslotsSubmitBtn").button('reset');
                    if (selectedSchedule.isEditd) { //Don't let them edit again
                        $("#editTimeslotsSubmitBtn").button('loading');
                    }
                    $(".timeslotsTable").show();
                    return false; 
                });
                
                //Submit to server
                $("#editTimeslotsSubmitBtn").on('click', function() {
                    $("#editTimeslotsSubmitBtn").button('loading');
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
                        url: 'editTimeslotsJson',
                        data: {jsonData: JSON.stringify(timeslotsData)},
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            console.log("editTimeslotsJson was successful");
                            showNotification("SUCCESS", response.message);
//                            displayMessage("timeslotResultMessage", response.message, false);
                            selectedSchedule["isEditd"] = true;
                        } else {
                            var eid = btoa(response.message);
                            console.log(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        console.log("editTimeslotsJson AJAX FAIL");
                        showNotification("ERROR", "Oops.. something went wrong");
//                        displayMessage("timeslotResultMessage", "Oops.. something went wrong", true);
                    });
                    return false;
                });
                
                
                function makeTimeslotTable(tableClass, dateArray, dayStart, dayEnd) {
                    var thead = $(document.editElement("thead"));
                    var minTime = dayStart;
                    var maxTime = dayEnd;

                    //Creating table header with dates
                    thead.append("<th></th>"); //Empty cell for time column
                    for (i = 0; i < dateArray.length; i++) {
                        var th = $(document.editElement("th"));
                        var headerVal = new Date(dateArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(dateArray[i]).toString('ddd');
                        th.html(headerVal);
                        thead.append(th);
                    }
                    //Inserting constructed table header into table
                    $("." + tableClass).append(thead);

                    //Creating table body with times and empty cells
                    var tbody = $(document.editElement("tbody"));

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
                        var tr = $(document.editElement("tr"));
                        var timeTd = $(document.editElement("td"));
                        timeTd.html(timesArray[i]);
                        tr.append(timeTd);

                        for (var j = 0; j < dateArray.length; j++) {
                            var td = $(document.editElement("td"));
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
                            var marker = document.editElement("div");
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
                
                /*----------------------------------------
                 NOTIFICATIONS
                 ------------------------------------------*/
                 
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
                            opts.title = "Editd";
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
            
            addLoadEvent(editScheduleLoad);
        </script>
    </body>
</html>
