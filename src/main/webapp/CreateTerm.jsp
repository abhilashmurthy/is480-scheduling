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
            <div class="line-separator"></div>
            <!-- Create Schedule -->
            <div id="createSchedulePanel" style="visibility: hidden;">
                <h3>Create Schedule</h3>
                <form id="createScheduleForm">
                    <table>
                        <th>Milestone</th><th>Start Date</th><th>End Date</th>
                        <tr>
                            <td class="formLabelTd">Acceptance</td>
                            <td><input type="text" class="input-medium datepicker" name="acceptanceStartDate"/></td>
                            <td><input type="text" class="input-medium datepicker" name="acceptanceEndDate"/></td>
                        </tr>
                        <tr>
                            <td class="formLabelTd">Midterm</td>
                            <td><input type="text" class="input-medium datepicker" name="midtermStartDate"/></td>
                            <td><input type="text" class="input-medium datepicker" name="midtermEndDate"/></td>
                        </tr>
                        <tr>
                            <td class="formLabelTd">Final</td>
                            <td><input type="text" class="input-medium datepicker" name="finalStartDate"/></td>
                            <td><input type="text" class="input-medium datepicker" name="finalEndDate"/></td>
                        </tr>
                        <tr class="submitBtnRow">
                            <td></td>
                            <td></td>
                            <td><input id="createScheduleSubmitBtn" type="submit" class="btn btn-primary" value="Create"/></td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>


        <%@include file="footer.jsp" %>
        <script type="text/javascript">

            //Create Term AJAX Call
            $("#createTermForm").on('submit', function() {
                var formData = $("#createTermForm").serialize();
                $.ajax({
                    type: 'GET',
                    url: 'createTermJson',
                    data: formData,
                    dataType: 'json'
                }).done(function(response) {
                    
                    if (response) {
                        console.log("Created year: " + response.year);
                        console.log("Create semester: " + response.semester);
                        console.log("Added: " + response.hasBeenAdded);
                        
                        if (response.hasBeenAdded) {
                            //Remove Create Button
                            $("#createTermSubmitRow").fadeOut('slow',function(){});
                            displayMessage("createTermForm", "Term added successfully", false);
                            displayCreateSchedule();
                            
                        } else {
                            //Display error message
                            displayMessage("createTermForm", "Term already exists", true);
                        }
                    } else {
                        //response error
                        displayMessage("createTermForm", "Term already exists", true);
                    }

                }).fail(function(error) {
                    console.log("Create Term Form AJAX Fail");
                });
                return false;
            });
            
            //Create Schedule AJAX Call
            $("#createScheduleForm").on('submit', function() {
                var formData = $("#createSchedule").serialize();
                $.ajax({
                    type: 'GET',
                    url: 'createScheduleJson',
                    data: formData,
                    dataType: 'json'
                }).done(function(response) {
                    //Look at create term for example

                }).fail(function(error){
                    console.log("Create Schedule Form AJAX Fail");
                });
            });
            
            //Display Schedule
            function displayCreateSchedule() {
                //Display Create Schedule
                $("#createSchedulePanel").css('visibility', '');
                $("#createSchedulePanel").css('padding-top', '20px');
            }
            
            function displayMessage(afterElement, msg, fade) {
                //Dislay result
                $("#" + afterElement).after("<h4 id='resultMessage'>" + msg + "</h4>");
                $("#resultMessage").css('color', 'darkgreen');
                if (fade) {
                    $("#resultMessage").fadeOut(2000, function(){});
                    $("#resultMessage").css('color', 'darkred');
                }
            }
            
            //Datepicker and timepicker
            $(".datepicker").datepicker({
                beforeShowDay: $.datepicker.noWeekends,
                dateFormat: "yy-mm-dd"
            });

            $("#timepicker").timepicker({
                timeFormat: 'H:i:s',
                minTime: '9:00am',
                maxTime: '18:00pm'
            });
            
        </script>
    </body>
</html>
