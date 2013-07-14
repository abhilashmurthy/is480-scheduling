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
                    <option value="1">Semester 1</option>
                    <option value="2">Semester 2</option>
                    </select>
                    </td>
                    </tr>
                    <tr id="createTermSubmitRow"><td></td><td><input type="submit" class="btn btn-primary" value="Create"/></td></tr>
                </table>
            </form>
            <h4 id="resultMessage"/></h4>
        
        <div class="line-separator"></div>
        
        <!-- Create Schedule -->
        <div id="createSchedulePanel">
            <%@include file="createterm_createschedule.jsp" %>
        </div>
        
        <div class="line-separator"></div>
        
        <!-- Create Timeslots -->
        <div id="createTimeslotsPanel">
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

                    if (response) {
                        console.log("Checked year: " + response.year);
                        console.log("Checked semester: " + response.semester);
                        console.log("Can Add: " + response.canAdd);

                        if (response.canAdd) {
                            //Remove Create Button
                            $("#createTermSubmitRow").fadeTo('slow', 0);
                            displayMessage("Creating Term...", false);
                            displayCreateSchedule();

                        } else {
                            //Display error message
                            displayMessage("Term already exists", true);
                        }
                    } else {
                        alert("Oops. something went wrong..");
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
                dateFormat: "yy-mm-dd"
            });
            
            $("#midtermDatePicker").on('focus', function(){
                //Enable only if acceptance has values
               if ($("#acceptanceDatePicker").val()) {
                    var acceptanceDates = $("#acceptanceDatePicker").multiDatesPicker('getDates');
                    var lastAcceptanceDate = acceptanceDates[acceptanceDates.length - 1];
                    $(this).multiDatesPicker({
//                       minDate: new Date(lastAcceptanceDate).toDateString()
                    });
               }
            });

            //Create Schedule AJAX Call
            $("#createScheduleForm").on('submit', function() {
                displayCreateTimeslots();
                return false;
            });
            
            
            function displayCreateTimeslots() {
                //Display Create Schedule
                $("#createTimeslotsPanel").show();
                $("#createTimeslotsPanel").css('padding-top', '20px');
                
                /*----------------------------------------
                CREATE TIMESLOTS
                ------------------------------------------*/
        
                var acceptanceDates = $("#acceptanceDatePicker").multiDatesPicker('getDates');
                var midtermDates = $("#midtermDatePicker").multiDatesPicker('getDates');
                var finalDates = $("#finalDatePicker").multiDatesPicker('getDates');
                
                console.log(acceptanceDates);
                console.log(midtermDates);
                console.log(finalDates);
                
                var dayStart = 9;
                var dayEnd = 16;
                var duration = 60;
                
                //Get times
                var timeArray = getTimes(dayStart, dayEnd, duration);
                
                console.log('TimeArray: ' + timeArray);
                
                //Append Header Names
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
                
                $("#timeslotsForm").on('submit', function(){
                    var scheduleData = $("#createScheduleForm").serializeArray();
                    var timeslotsData = $(this).serializeArray();
                    var finalData = $.merge(scheduleData, timeslotsData);
                    console.log('Final serialized: ' + JSON.stringify(finalData));
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
                    
                    //Append checkbox 'ALL' checkboxes
                    var allChkString = "<tr id='allChkRow'><td>ALL</td>";
                    for (i = 0; i < dateArray.length; i++) {
                        var date = dateArray[i].toString('dd-MMM-yyyy');
                        allChkString += "<td>" + "<input class='chkALL_" + tableId + "' id='chkALL_" + tableId + "_" + date + "' type='checkbox' value='" + tableId + "_" + date + "' checked/></td>";
                    }
                    allChkString += "</tr>";
                    $("#" + tableId).append(allChkString);
                    
                    //Append checkbox data
                    for (j = 0; j < timeArray.length; j++) {
                        var htmlString = "<tr>";
                        var time = timeArray[j];
                        htmlString += "<td id='timeColumn'>" + time + "</td>";
                        for (i = 0; i < dateArray.length; i++) {
                            var date = dateArray[i].toString('dd-MMM-yyyy');
                            htmlString += "<td><input class='chkBox_" + tableId + "_" + date + "' id='chk_" + tableId + "_" + date + "_" + time.replace(/:/g, '-') +"' type='checkbox' checked name='timeslot_" + tableId +"' value='" + date + " " + time +"' /></td>";
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
