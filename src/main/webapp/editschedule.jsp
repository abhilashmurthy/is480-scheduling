<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Prakhar
--%>

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
        <title>Edit Term</title>
        <style type="text/css">
            table {
                margin-left: 20px;
                table-layout: fixed;
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

        </style>
    </head>
    <body>
        <!-- Navbar -->
        <%@include file="navbar.jsp" %>

        <!-- Edit Term -->
        <div id="editTermPanel" class="container">
            <h3>Edit Term</h3>
            <form id="editTermForm">
                <table>
                    <tr><td class="formLabelTd">Edit Year</td><td><input id="yearInput" type="text" name="year" /></td></tr>
                    <tr><td class="formLabelTd">Edit Semester</td><td><input id="semesterInput" type="text" name="semester" /></td></tr>
                    <tr id="editTermRow"><td></td><td><input type="submit" class="btn btn-primary" value="Save"/></td></tr>
                </table>
            </form>
            <h4 id="termResult" class="resultMessage"/></h4>

        <div class="line-separator"></div>

        <!-- Edit Schedule -->
        <div id="editSchedulePanel">
            <h3>Edit Schedule</h3>
            <form id="editScheduleForm">
                <table>
                    <th>Milestone</th><th>Dates</th>
                    <tr>
                        <td class="formLabelTd">Acceptance</td>
                        <td><input type="text" id="acceptanceDatePicker" class="input-medium datepicker" name="acceptanceDates"/></td>
                    </tr>
                    <tr>
                        <td class="formLabelTd">Midterm</td>
                        <td><input type="text" id="midtermDatePicker" class="input-medium datepicker" name="midtermDates"/></td>
                    </tr>
                    <tr>
                        <td class="formLabelTd">Final</td>
                        <td><input type="text" id="finalDatePicker" class="input-medium datepicker" name="finalDates"/></td>
                    </tr>
                    <tr class="editScheduleSubmitRow">
                        <td></td>
                        <td><input id="editScheduleSubmitBtn" type="submit" class="btn btn-primary" value="Save"/></td>
                    </tr>
                </table>
            </form>
            <h4 id="scheduleResult" class="resultMessage"/></h4>
        </div>

        <div class="line-separator"></div>

        <!-- Edit Timeslots -->
        <div id="editTimeslotsPanel">
            <h3>Edit Timeslots</h3>
            <div id="timeslotsTableSection">
                <table id="acceptanceTimeslotsTable" class="table-condensed table-hover table-bordered table-striped">
                </table> 
                <br/>
                <table id="midtermTimeslotsTable" class="table-condensed table-hover table-bordered table-striped">
                </table>
                <br/>
                <table id="finalTimeslotsTable" class="table-condensed table-hover table-bordered table-striped">
                </table>
                <br/>
                <button id="editTimeslotsSubmitBtn" class="btn btn-primary">Save</button>
                <br />
            </div>
            <h4 id="timeslotsResult" class="resultMessage"/></h4>
        </div>

    </div>
    <%@include file="footer.jsp" %>
    <!-- jshashset imports -->
    <script type="text/javascript" src="js/plugins/jshashtable-3.0.js"></script>
    <script type="text/javascript" src="js/plugins/jshashset-3.0.js"></script>
    <script type="text/javascript" src="js/plugins/jquery-ui.multidatespicker.js"></script>
    <script type="text/javascript">
        //Makes use of footer.jsp's jQuery and bootstrap imports
        editScheduleLoad = function() {
            
            //------------------------------------------//
            // View Schedule Data
            //------------------------------------------//
            
            //Declare common variables
            //Default milestoneStr is ACCEPTANCE
            var activeAcademicYearStr = "<%= activeTerm.getAcademicYear()%>";
            var activeSemesterStr = "<%= activeTerm.getSemester()%>";
            var acceptanceId = null;
            var midtermId = null;
            var finalId = null;
            
            loadInitialValues();
            
            /* Datepicker validation */
            $(".datepicker").multiDatesPicker({
                dateFormat: "yy-mm-dd",
                minDate: Date.today(),
                beforeShowDay: $.datepicker.noWeekends
            });
            
            function loadInitialValues() {
                $("#yearInput").val(activeAcademicYearStr);
                $("#semesterInput").val(activeSemesterStr);
                loadScheduleDates();
            }
            
            function loadScheduleDates() {
                //Get acceptance schedule data
                var milestoneStr = "ACCEPTANCE";
                var scheduleData = getScheduleData(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                acceptanceId = scheduleData.id;
                var distinctDates = getDistinctDates(scheduleData, "typeDate");
                $("#acceptanceDatePicker").datepicker('destroy');
                $("#acceptanceDatePicker").multiDatesPicker({
                    dateFormat: "yy-mm-dd",
                    defaultDate: new Date(scheduleData.startDate),
                    maxPicks: 2,
                    beforeShowDay: $.datepicker.noWeekends
                });
                $("#acceptanceDatePicker").multiDatesPicker('addDates', [new Date(scheduleData.startDate), new Date(scheduleData.endDate)]);
                loadScheduleTimeslots(milestoneStr, scheduleData);
                
                //Get midterm schedule data
                var milestoneStr = "MIDTERM";
                var scheduleData = getScheduleData(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                midtermId = scheduleData.id;
                var distinctDates = getDistinctDates(scheduleData, "typeDate");
                $("#midtermDatePicker").datepicker('destroy');
                $("#midtermDatePicker").multiDatesPicker({
                    dateFormat: "yy-mm-dd",
                    defaultDate: new Date(scheduleData.startDate),
                    maxPicks: 2,
                    beforeShowDay: $.datepicker.noWeekends
                });
                $("#midtermDatePicker").multiDatesPicker('addDates', [new Date(scheduleData.startDate), new Date(scheduleData.endDate)]);
                loadScheduleTimeslots(milestoneStr, scheduleData);
                
                //Get final schedule data
                var milestoneStr = "FINAL";
                var scheduleData = getScheduleData(milestoneStr, activeAcademicYearStr, activeSemesterStr);
                finalId = scheduleData.id;
                var distinctDates = getDistinctDates(scheduleData, "typeDate");
                $("#finalDatePicker").datepicker('destroy');
                $("#finalDatePicker").multiDatesPicker({
                    dateFormat: "yy-mm-dd",
                    defaultDate: new Date(scheduleData.startDate),
                    maxPicks: 2,
                    beforeShowDay: $.datepicker.noWeekends
                });
                $("#finalDatePicker").multiDatesPicker('addDates', [new Date(scheduleData.startDate), new Date(scheduleData.endDate)]);
                loadScheduleTimeslots(milestoneStr, scheduleData);
            }
            
            function loadScheduleTimeslots(milestoneStr, scheduleData) {
                var tableId = milestoneStr.toLowerCase() + "TimeslotsTable";
                var table = $("#" + tableId);
                var dates = $("#" + milestoneStr.toLowerCase() + "DatePicker").multiDatesPicker('getDates');
                var dateArray = getDatesBetween(dates[0], dates[1]);
                table.before("<h4>" + milestoneStr.toUpperCase() + "</h4>"); //Add milestone title
                makeTimeslotTable(tableId, scheduleData, dateArray);
                populateTimeslotsTable(tableId, scheduleData);
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
            
            function getScheduleData(milestoneString, academicYearString, semesterString) {
                var toReturn = null;
                var data = {
                    milestoneString: milestoneString,
                    academicYearString: academicYearString,
                    semesterString: semesterString
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
                var thead = $(document.createElement("thead"));
                var minTime = 9;
                var maxTime = 19;

                //Creating table header with dates
                thead.append("<th></th>"); //Empty cell for time column
                for (i = 0; i < dateArray.length; i++) {
                    var th = $(document.createElement("th"));
                    var headerVal = new Date(dateArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(dateArray[i]).toString('ddd');
                    th.html(headerVal);
                    thead.append(th);
                }
                //Inserting constructed table header into table
                $("#" + tableId).append(thead);

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
                $("#" + tableId).append(tbody);
            }
            
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

             $("td.timeslotcell", "#acceptanceTimeslotsTable").on("click", function() {
                 triggerTimeslot(this, 60);
             });
             $("td.timeslotcell", "#midtermTimeslotsTable").on("click", function() {
                 triggerTimeslot(this, 90);
             });
             $("td.timeslotcell", "#finalTimeslotsTable").on("click", function() {
                 triggerTimeslot(this, 90);
             });
             
            function populateTimeslotsTable(tableId, scheduleData) {
                $("#" + tableId).find("td").each(function(){
                    var self = $(this);
                    var timeslot = getScheduleDataTimeslot(self.attr('value'), scheduleData);
                    if (timeslot) {
                        triggerTimeslot(this, scheduleData.duration);
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
            // Change Schedule Data
            //------------------------------------------//
            
            //Update Term AJAX Call
            $("#editTermForm").on('submit', function() {
                console.log('clicked');
                var editTermData = {
                    year: $("#yearInput").val(), 
                    semester: $("#semesterInput").val(), 
                    activeYear:activeAcademicYearStr, 
                    activeSemester:activeSemesterStr
                };
                $.ajax({
                    type: 'GET',
                    url: 'updateTermJson',
                    data: editTermData,
                    dataType: 'json'
                }).done(function(response) {
                    if (!response.exception) {
                        if (response.success) {
                            displayMessage("termResult", response.message, false);
                        } else {
                            displayMessage("termResult", response.message, true);
                        }
                        setTimeout(function(){window.location.reload();}, 1000);
                    } else {
                        var eid = btoa(response.message);
                        window.location="error.jsp?eid=" + eid;
                    }
                }).fail(function(error) {
                    console.log("Edit Term Form AJAX Fail");
                });
                return false;
            });
            
            //Update Schedule AJAX Call
            $("#editScheduleForm").on('submit', function() {
                console.log('clicked');
                editScheduleData = {
                    year:activeAcademicYearStr, 
                    semester:activeSemesterStr,
                    acceptanceId:acceptanceId,
                    midtermId:midtermId,
                    finalId:finalId,
                    acceptanceDates:$("#acceptanceDatePicker").multiDatesPicker('getDates'),
                    midtermDates:$("#midtermDatePicker").multiDatesPicker('getDates'),
                    finalDates:$("#finalDatePicker").multiDatesPicker('getDates')
                };
                $.ajax({
                    type: 'POST',
                    url: 'updateScheduleJson',
                    data: editScheduleData,
                    async: false,
                    dataType: 'json'
                }).done(function(response) {
                    if (!response.exception) {
                        if (response.success) {
                            displayMessage("scheduleResult", response.message, false);
                        } else {
                            displayMessage("scheduleResult", response.message, true);
                        }
                        setTimeout(function(){window.location.reload();}, 1000);
                    } else {
                        var eid = btoa(response.message);
                        window.location="error.jsp?eid=" + eid;
                    }
                }).fail(function(error) {
                    console.log("Edit Term Form AJAX Fail");
                });
                return false;
            });
            
            //Update Timeslots AJAX Call            
            $("#editTimeslotsSubmitBtn").on('click', function() {
                //SerializeArray not functional for timeslots
                var timeslotsData = {};
                var timeslot_acceptance = new Array();
                var timeslot_midterm = new Array();
                var timeslot_final = new Array();

                var accData = $("div.start-marker", "#acceptanceTimeslotsTable").get();
                for (var i = 0; i < accData.length; i++) {
                    var obj = accData[i];
                    timeslot_acceptance.push($(obj).parent().attr("value"));
                }

                var midData = $("div.start-marker", "#midtermTimeslotsTable").get();
                for (var i = 0; i < midData.length; i++) {
                    var obj = midData[i];
                    timeslot_midterm.push($(obj).parent().attr("value"));
                }

                var finData = $("div.start-marker", "#finalTimeslotsTable").get();
                for (var i = 0; i < finData.length; i++) {
                    var obj = finData[i];
                    timeslot_final.push($(obj).parent().attr("value"));
                }

                timeslotsData["timeslot_acceptance[]"] = timeslot_acceptance;
                timeslotsData["timeslot_midterm[]"] = timeslot_midterm;
                timeslotsData["timeslot_final[]"] = timeslot_final;
                timeslotsData["acceptanceId"] = acceptanceId;
                timeslotsData["midtermId"] = midtermId;
                timeslotsData["finalId"] = finalId;
                
                console.log('Timeslots data is: ' + JSON.stringify(timeslotsData));
                $.ajax({
                    type: 'POST',
                    url: 'updateTimeslotsJson',
                    data: timeslotsData,
                    dataType: 'json'
                }).done(function(response) {
                    if (!response.exception) {
                        if (response.success) {
                            displayMessage("timeslotsResult", response.message, false);
                        } else {
                            displayMessage("timeslotsResult", response.message, true);
                        }
                        setTimeout(function(){window.location.reload();}, 1000);
                    } else {
                        var eid = btoa(response.message);
                        window.location="error.jsp?eid=" + eid;
                    }
                }).fail(function(error) {
                    console.log("createTimeslotsJson AJAX FAIL");
                    displayMessage("Oops.. something went wrong", true);
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

        };

        addLoadEvent(editScheduleLoad);
    </script>
</body>
</html>
