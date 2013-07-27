<!-- EMBEDDABLE PAGE. NOT TO BE USED ALONE -->
<table id="acceptanceScheduleTable" class="scheduleTable table-condensed table-hover table-bordered">
</table>

<!-- jshashset imports -->
<script type="text/javascript" src="js/plugins/jshashtable-3.0.js"></script>
<script type="text/javascript" src="js/plugins/jshashset-3.0.js"></script>
<script type="text/javascript">
    //Makes use of footer.jsp's jQuery and bootstrap imports
    viewScheduleLoad = function() {
        
        $('.scheduleTable').ready(function() {
            $.ajax({
                type: 'GET',
                url: 'getSchedule',
                dataType: 'json'
            }).done(function(response) {
                makeSchedule(response);
                
                $(".timeslotCell").mouseenter(function(){
                    $(this).css('border', '2px solid #FCFFBA');
                });
                
                $(".timeslotCell").mouseleave(function(){
                    $(this).css('border', '1px solid #dddddd');
                });
                
            });
        });


//	function makeSchedule(data) {
//		var startDate = new Date(data.startDate);
//		var endDate = new Date(data.endDate);
//		var dateArray = [];
//		var tempDate = startDate;
//		
//		while(tempDate <= endDate) {
//			dateArray.push(tempDate);
//			tempDate = new Date(tempDate).addDays(1);
//		}
//		
//		initTable(dateArray);
//	}

        function makeSchedule(data) {

            makeSchedule("scheduleTable", data.timeslots);

            function makeSchedule(tableClass, timeslots) {
                
                //TODO: Get from server/admin console/whatevs
                var minTime = 9;
                var maxTime = 19;
                
                var timesArray = new Array();
                for (var i = minTime; i < maxTime; i++) {
                    var timeVal = Date.parse(i+":00:00");
                    timesArray.push(timeVal.toString("HH:mm"));
                    timeVal.addMinutes(30);
                    timesArray.push(timeVal.toString("HH:mm"));
                }

                //Get unique dates, minDate, and maxDate
                var datesSet = new HashSet();
                for (i = 0; i < timeslots.length; i++) {
                    datesSet.add(Date.parse(timeslots[i].datetime).toString("yyyy-MM-dd"));
                }
                var datesHashArray = datesSet.values().sort();
                var minDate = new Date(datesHashArray[0]);
                var maxDate = new Date(datesHashArray[datesHashArray.length - 1]);
//                console.log("Mindate: " + minDate + ", maxDate: " + maxDate);
                var datesArray = getDates(minDate, maxDate);
                
                function getDates(startDate, stopDate) {
                    var dateArray = new Array();
                    var currentDate = startDate;
                    while (currentDate <= stopDate) {
                        if (new Date(currentDate).isWeekday()) {
                            dateArray.push(currentDate);
                        }
                        currentDate = new Date(currentDate).addDays(1);
                    }
                    return dateArray;
                }
                
                //Get dates between minDate and maxDate

                //Append header names
                var headerString = "<thead><tr id='scheduleHeader'><th></th>";
                for (i = 0; i < datesArray.length; i++) {
                    headerString += "<th>" + new Date(datesArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(datesArray[i]).toString('ddd') + "</th>";
                }
                headerString += "</tr></thead>";
                $("." + tableClass).append(headerString);

                //Append timeslot data
                var rowspanArr = new Array();
                for (var i = 0; i < timesArray.length; i++) {
                    var htmlString = "<tr>";
                    var time = timesArray[i];
                    htmlString += "<td>" + time + "</td>";
                    rowloop: //Loop label
                    for (var j = 0; j < datesArray.length; j++) {
                        var date = datesArray[j];
                        date = new Date(date).toString("yyyy-MM-dd");
                        //Identifier for table cell
                        var datetimeString = date + " " + time + ":00";
                        //Checking if table cell is part of a timeslot
                        for (var k=0; k < rowspanArr.length; k++) {
                            
                            if (datetimeString === rowspanArr[k]) {
                                console.log("Skipped: " + datetimeString);
                                continue rowloop;
                            }
                        }
                        
                        //Table cell not part of timeslot yet. Proceed.

                        //Get the timeslot id from datetime
                        var id = getTimeslotId(timeslots, date, time);
                        htmlString += "<td class='timeslotCell'";
                        
                        //If timeslot is available
                        if (id !== -1) {
                            htmlString += " rowspan='2'";
                            var temp = new Date(Date.parse(datetimeString)).addMinutes(30).toString("yyyy-MM-dd HH:mm:ss");
                            console.log("Temp is: " + temp);
                            rowspanArr.push(temp);
                            htmlString += " id='timeslot_" + id + "'";

                            //Get the team name from id
                            var team = getTeam(timeslots, id);
                            if (team !== null) {
                                htmlString += " style='background-color: #f2dede; border: 1px solid #dddddd'>";
                                htmlString += team;
                            } else {
                                htmlString += " style='background-color: #d9edf7; border: 1px solid #dddddd'>";
                            }
                        } else {
                            htmlString += " style='background-color: #f5f5f5; border: 1px solid #dddddd'>";
                        }

                        //Close td
                        htmlString += "</td>";

                    }
                    htmlString += "</tr>";
                    $("." + tableClass).append(htmlString);
                }
            }

            function getTimeslotId(timeslots, date, time) {
                var datetimeString = (date + " " + time + ":00").trim();
//                console.log("Date string: " + datetimeString);
                for (var i = 0; i < timeslots.length; i++) {
                    if (timeslots[i].datetime === datetimeString) {
                        return timeslots[i].id;
                    }
                }
                return -1;
            }

            function getTeam(timeslots, id) {
                for (var i = 0; i < timeslots.length; i++) {
                    if (timeslots[i].id === id && timeslots[i].team) {
                        return timeslots[i].team;
                    }
                }
                return null;
            }
        }

//        function initTable(dateList) {
//            var dayStr = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
//            //Creating the row time markings (Interval: 30 min)
//            for (var i = 8; i < 19; i++) {
//                $('#scheduleBody').append("<tr id=r" + i + "0><td>" + i + ":" + "00</td>");
//                for (var y = 0; y < dateList.length; y++) {
//                    $('#scheduleBody').append("<td></td>");
//                }
//                $('#scheduleBody').append("</tr><tr id=r" + i + "3><td>" + i + ":" + "30</td>");
//                for (var y = 0; y < dateList.length; y++) {
//                    $('#scheduleBody').append("<td></td>");
//                }
//                $('#scheduleBody').append("</tr>");
//            }
//
//            $('#scheduleHeader').append("<th></th>");
//            //Adding the columns for the days
//            for (var i = 0; i < dateList.length; i++) {
//                var date = dateList[i];
//                var dayOfWeek = dayStr[date.getDay()];
//                var day = date.getDate();
//                $('#scheduleHeader').append("<th id=d" + day + "m" + date.getMonth() + ">" + date.toString('dd MMM') + "<br />"
//                        + dayOfWeek + "</th>");
//            }
//        }

    };

    addLoadEvent(viewScheduleLoad);
</script>