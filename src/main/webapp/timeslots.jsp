<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>IS480 Scheduling</title>
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
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>

        <!-- Welcome Text -->
        <div class="container page">
            <table>
                <tr>
                    <td>Start Date</td><td><input type="text" id="startDate" class="datepicker" name="startDate"/>
                </tr>
                <tr>
                    <td>End Date</td><td><input type="text" id="endDate" class="datepicker" name="endDate"/>
                </tr>
                <tr>
                    <td>Slot Duration</td>
                    <td>
                        <select id="duration" name="duration"/> 
                           <option value="60">60 minutes</option>
                           <option value="90">90 minutes</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><input id="getChkBoxBtn" type="submit" value="Submit" class="btn btn-primary"/></td>
                </tr>
            </table>
            <form id="checkboxForm">
                <table id="timeslotsTable" style="visibility: hidden;">
                </table>
            </form>
        </div>
        
        <%@include file="footer.jsp"%>
        <script type="text/javascript">
            
            //Process the checkboxes
            $("#getChkBoxBtn").on('click', function(){
                var dayStart = 9;
                var dayEnd = 16;
                var noWeekends = true;
                
                var start = Date.parseExact($("#startDate").val(), 'yyyy-mm-dd');
                var end = Date.parseExact($("#endDate").val(), 'yyyy-mm-dd');
                var duration = $("#duration").val();
                
                //Get dates and times
                var dateArray = getDates(start, end, noWeekends);
                var timeArray = getTimes(dayStart, dayEnd, duration);
                
                //Make the table visible
                $("#timeslotsTable").css('visibility', 'visible');
                
                //Append checkbox header names
                var headerString = "<thead><tr><td></td>";
                for (i = 0; i < dateArray.length; i++) {
                    headerString += "<td>" + dateArray[i].toString('ddd, dd MMM yyyy') + "</td>";
                }
                headerString += "</tr></thead>";
                $("#timeslotsTable").append(headerString);
                
                //Append checkbox 'ALL' checkboxes
                var allChkString = "<tr id='allChkRow'><td>ALL</td>";
                for (i = 0; i < dateArray.length; i++) {
                    var date = dateArray[i].toString('dd-MMM-yyyy');
                    allChkString += "<td>" + "<input class='chkALL' id='chkALL" + date + "' type='checkbox' value='" + date + "' checked/></td>";
                }
                allChkString += "</tr>";
                $("#timeslotsTable").append(allChkString);
                
                //Append checkbox data
                for (j = 0; j < timeArray.length; j++) {
                    var htmlString = "<tr>";
                    var time = timeArray[j];
                    htmlString += "<td id='timeColumn'>" + time + "</td>";
                    for (i = 0; i < dateArray.length; i++) {
                        var date = dateArray[i].toString('dd-MMM-yyyy');
                        htmlString += "<td><input class='chkBox" + date + "' id='chk" + date + "_" + time.replace(/:/g, '-') +"' type='checkbox' checked value='" + date + " " + time +"' /></td>";
                    }
                    htmlString += "</tr>";
                    $("#timeslotsTable").append(htmlString);
                }
                
                $(".chkALL").on('click', function(){
                   console.log('All checkbox ' + $(this).attr('id') + ' clicked');
                   var changeBoxes = $(".chkBox" + $(this).val());
                   if ($(this).is(':checked')) {
                       changeBoxes.attr('checked');
                   } else {
                       changeBoxes.removeAttr('checked');
                   }
                });
                
            });
            
            function getDates(startDate, endDate, noWeekends) {
                var dateArray = new Array();
                var currentDate = startDate;
                while (currentDate <= endDate) {
                        if (noWeekends) {
                            if (currentDate.getDay() > 0 && currentDate.getDay() < 6) {
                                dateArray.push(new Date(currentDate));
                            }
                        } else {
                            dateArray.push(new Date(currentDate));
                        }
                    currentDate = currentDate.addDays(1);
                }
                return dateArray;
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
            
            //Datepicker and timepicker
            $(".datepicker").datepicker({
                beforeShowDay: $.datepicker.noWeekends,
                dateFormat: "yy-mm-dd"
            });

            $(".timepicker").timepicker({
                timeFormat: 'H:i:s',
                minTime: '9:00am',
                maxTime: '18:00pm'
            });
        </script>
    </body>
</html>