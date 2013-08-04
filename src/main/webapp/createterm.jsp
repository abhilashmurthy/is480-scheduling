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
<html>
    <head>
        <title>Create Term</title>
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
            
        </style>
    </head>
    <body>
        <!-- Navbar -->
        <%@include file="navbar.jsp" %>

        <!-- Create Term -->
        <div id="createTermPanel" class="container">
            <h3>Create Term</h3>
            <form id="createTermForm">
                <table>
                    <tr>
                        <td class="formLabelTd">
                            Choose Year
                        </td>
                        <td> <!-- Putting default values for testing purposes -->
                            <select name="year"> 
                                <option value="2013">2013-2014</option>
                                <option value="2014">2014-2015</option>
                                <option value="2015">2015-2016</option>
                                <option value="2016">2016-2017</option>
                                <option value="2017">2017-2018</option>
                                <option value="2018">2018-2019</option>
                                <option value="2019">2019-2020</option>
                                <option value="2020">2020-2021</option>
                                <option value="2021">2021-2022</option>
                                <option value="2022">2022-2023</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="formLabelTd">
                            Choose Semester
                        </td>
                        <td>
                            <select name="semester"/> 
                    <option value="Term 1">Semester 1</option>
                    <option value="Modified Term 1A">Semester 1A</option>
                    <option value="Modified Term 1B">Semester 1B</option>
                    <option value="Term 2">Semester 2</option>
                    <option value="Modified Term 2A">Semester 2A</option>
                    <option value="Modified Term 2B">Semester 2B</option>
                    </select>
                    </td>
                    </tr>
                    <tr id="createTermSubmitRow"><td></td><td><input type="submit" class="btn btn-primary" value="Create"/></td></tr>
                </table>
            </form>
            <h4 id="resultMessage"/></h4>
        
        <div class="line-separator"></div>
        
        <!-- Create Schedule -->
        <div id="createSchedulePanel" hidden>
            <%@include file="createterm_createschedule.jsp" %>
        </div>
        
        <div class="line-separator"></div>
        
        <!-- Create Timeslots -->
        <div id="createTimeslotsPanel" hidden>
            <%@include file="createterm_createtimeslots.jsp" %>
        </div>
        
    </div>
    <%@include file="footer.jsp" %>
    <script type="text/javascript" src="js/plugins/jquery-ui.multidatespicker.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            
            /*----------------------------------------
            CREATE TERM
            ------------------------------------------*/
            
            //Create Term AJAX Call
            $("#createTermForm").on('submit', function() {
                var formData = $("#createTermForm").serialize();
                $.ajax({
                    type: 'GET',
                    url: 'checkTermJson',
                    data: formData,
                    dataType: 'json'
                }).done(function(response) {

                    if (!response.exception) {
                        console.log("Checked year: " + response.year);
                        console.log("Checked semester: " + response.semester);
                        console.log("Can Add: " + response.canAdd);

                        if (response.canAdd) {
                            //Remove Create Button - Brought it back
//                            $("#createTermSubmitRow").fadeTo('slow', 0);
                            displayMessage("Term selected", false);
                            displayCreateSchedule();

                        } else {
                            //Display error message
                            displayMessage("Term already exists", true);
                        }
                    } else {
                        var eid = btoa(response.message);
                        window.location="error.jsp?eid=" + eid;
                    }

                }).fail(function(error) {
                    console.log("Create Term Form AJAX Fail");
                });
                return false;
            });

            //Display Schedule
            function displayCreateSchedule() {
                //Display Create Schedule
                $("#createSchedulePanel").show();
                $("#createSchedulePanel").css('padding-top', '20px');
                
                //Scroll to the bottom
                $("html, body").animate({ scrollTop: $(document).height() }, "slow");
            }

            //Display termMessage
            function displayMessage(msg, fade) {
                //Dislay result
                $("#resultMessage").fadeTo('slow', 100);
                $("#resultMessage").css('color', 'darkgreen').html(msg);
                if (fade) {
                    $("#resultMessage").css('color', 'darkred').html(msg).fadeTo('slow', 0);
                }
            }
            
            /*----------------------------------------
            CREATE SCHEDULE
            ------------------------------------------*/
            
            /* Datepicker validation */
            $(".datepicker").multiDatesPicker({
                dateFormat: "yy-mm-dd",
                minDate: Date.today(),
                beforeShowDay: $.datepicker.noWeekends
            });

            resetDisabledDates("acceptanceDatePicker", "midtermDatePicker");
            resetDisabledDates("midtermDatePicker", "finalDatePicker");
            
            function resetDisabledDates(first, second) {
                //Mouseover required to completely reset the datepicker
                $("#" + second).on('mouseover', function(){
                   $("#" + second).datepicker("destroy");
                });
                
                //Limits the dates to > end date of previous milestone
                $("#" + second).on('mousedown', function(){
                    //Enable only if acceptance has values
                    var acceptanceDates = $("#" + first).multiDatesPicker('getDates');
                    if (acceptanceDates) {
                        var lastAcceptanceDate = acceptanceDates[acceptanceDates.length - 1];
                        if (lastAcceptanceDate) {
                            $("#" + second).multiDatesPicker({
                                dateFormat: "yy-mm-dd",
                                minDate: new Date(lastAcceptanceDate).addDays(1),
                                beforeShowDay: $.datepicker.noWeekends
                            });
                        }
                    }
                });
            }
            
            var acceptanceId = null;
            var midtermId = null;
            var finalId = null;

            //Create Schedule Submit - Show timeslots panel
            $("#createScheduleForm").on('submit', function() {
                
                //TODO: Check to ensure that all acceptance, midterm, and final dates have values
                
                //AJAX call to save term and schedule dates
                var termData = $("#createTermForm").serializeArray();
                var scheduleData = $("#createScheduleForm").serializeArray();
                var createScheduleData = $.merge(termData, scheduleData);
                console.log('\n\nData to be sent to create schedule and term: ' + createScheduleData);
                $.ajax({
                    type: 'POST',
                    url: 'createScheduleJson',
                    data: createScheduleData,
                    dataType: 'json'
                }).done(function(response){
                    if (response.success) {
                        console.log("Schedules have been created successfully");
                        acceptanceId = response.acceptanceScheduleId;
                        midtermId = response.midtermScheduleId;
                        finalId = response.finalScheduleId;
                        //Display create timeslots forms
                        displayCreateTimeslots();
                    } else {
                        var eid = btoa(response.message);
                        window.location="error.jsp?eid=" + eid;
                    }
                }).fail(function(error){
                    console.log("createScheduleData AJAX FAIL");
                    displayMessage("Oops.. something went wrong", true);
                });
                return false;
            });
            
            //Display create timeslots
            function displayCreateTimeslots() {
                //Display create timeslots
                $("#createTimeslotsPanel").show();
                $("#createTimeslotsPanel").css('padding-top', '20px');
                
                //Scroll to the bottom
                $("html, body").animate({ scrollTop: $(document).height() }, "slow");
                
                /*----------------------------------------
                CREATE TIMESLOTS
                ------------------------------------------*/
        
                var acceptanceDates = $("#acceptanceDatePicker").multiDatesPicker('getDates');
                var midtermDates = $("#midtermDatePicker").multiDatesPicker('getDates');
                var finalDates = $("#finalDatePicker").multiDatesPicker('getDates');
                
//                console.log(acceptanceDates);
//                console.log(midtermDates);
//                console.log(finalDates);
                
                var dayStart = 9;
                var dayEnd = 16;
                var duration = 60;
                
                //Get times
                var timeArray = getTimes(dayStart, dayEnd, duration);
                
//                console.log('TimeArray: ' + timeArray);
                
                //Append timeslot form details
                if (acceptanceDates.length > 0) {
                    $("#acceptanceTimeslotsTable").show();
                    $("#acceptanceTimeslotsTable").before("<h4>Acceptance</h4>");
                    makeCheckboxTable("acceptanceTimeslotsTable", acceptanceDates);
                }
                if (midtermDates.length > 0) {
                    $("#midtermTimeslotsTable").show();
                    $("#midtermTimeslotsTable").before("<h4>Midterm</h4>");
                    makeCheckboxTable("midtermTimeslotsTable", midtermDates);
                }
                if (finalDates.length > 0) {
                    $("#finalTimeslotsTable").show();
                    $("#finalTimeslotsTable").before("<h4>Final</h4>");
                    makeCheckboxTable("finalTimeslotsTable", finalDates);
                }
                
                //OVERALL SUBMIT TO SERVER
                $("#timeslotsForm").on('submit', function(){
                    var scheduleIdData = { acceptanceId:acceptanceId, midtermId:midtermId, finalId:finalId };
                    //SerializeArray not functional for timeslots
                    var data = $(this).serializeArray();
                    var timeslotsData = {};
                    
                    for (var i = 0; i < data.length; i++){
                        timeslotsData[data[i].name] = timeslotsData[data[i].name] || [];
                        timeslotsData[data[i].name].push(Date.parse(data[i].value).toString('yyyy-MM-dd HH:mm:ss'));
                    }
                    
                    console.log('Timeslots data is: ' + JSON.stringify(timeslotsData));
                    var extendData = $.extend(scheduleIdData, timeslotsData);
                    var mergeData = $.merge(scheduleIdData, timeslotsData);
                    console.log('Final data is: ' + JSON.stringify(extendData));
                    console.log('Merge data is: ' + JSON.stringify(mergeData));
                    $.ajax({
                        type: 'POST',
                        url: 'createTimeslotsJson',
                        data: mergeData,
                        dataType: 'json'
                    }).done(function(response){
                        if (response.success){
                            console.log("createTimeslotsJson was successful");
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error){
                        console.log("createTimeslotsJson AJAX FAIL");
                        displayMessage("Oops.. something went wrong", true);
                    });
                    
                    return false;
                });
                
                function makeCheckboxTable(tableId, dateArray) {
                    //Append checkbox header names
                    var headerString = "<thead><tr><td></td>";
                    for (i = 0; i < dateArray.length; i++) {
                        headerString += "<td>" + new Date(dateArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(dateArray[i]).toString('ddd') + "</td>";
                    }
                    headerString += "</tr></thead>";
                    $("#" + tableId).append(headerString);
                    
                    //Make the table name
                    var tableName = tableId.split("Timeslots")[0];
                    
                    //Append checkbox 'ALL' checkboxes
//                    var allChkString = "<tr id='allChkRow'><td>ALL</td>";
//                    for (i = 0; i < dateArray.length; i++) {
//                        var date = dateArray[i].toString('dd-MMM-yyyy');
//                        allChkString += "<td>" + "<input class='chkALL_" + tableId + "' id='chkALL_" + tableId + "_" + date + "' type='checkbox' value='" + tableId + "_" + date + "' checked/></td>";
//                    }
//                    allChkString += "</tr>";
//                    $("#" + tableId).append(allChkString);
                    
                    //Append checkbox data
                    for (j = 0; j < timeArray.length; j++) {
                        var htmlString = "<tr>";
                        var time = timeArray[j];
                        htmlString += "<td id='timeColumn'>" + time + "</td>";
                        for (i = 0; i < dateArray.length; i++) {
                            var date = dateArray[i].toString('yyyy-MM-dd');
                            htmlString += "<td><input class='chkBox_" + tableName + "' id='chk_" + tableName + "_" + date + "_" + time.replace(/:/g, '-') +"' type='checkbox' checked name='timeslot_" + tableName +"[]' value='" + date + " " + time +"' /></td>";
                        }
                        htmlString += "</tr>";
                        $("#" + tableId).append(htmlString);
                    }
                }
                
                
                function getTimes(start, end, duration) {
                    var times = new Array();
                    var current = start;
                    while (current <= end) {
                        if (current%1 === 0) {
                            times.push(current + ":00:00");
                        } else {
                            times.push(Math.floor(current) + ":30:00");
                        }
                        current += (duration / 60);
                    }
                    return times;
                }
                
            }

        });
    </script>
</body>
</html>
