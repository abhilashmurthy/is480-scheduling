<!-- EMBEDDABLE PAGE. NOT TO BE USED ALONE -->
<table id="acceptanceScheduleTable" class="scheduleTable table-condensed table-hover table-bordered table-striped">
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

                //Get unique times
                var timesSet = new HashSet();
                for (i = 0; i < timeslots.length; i++) {
                    timesSet.add(Date.parse(timeslots[i].datetime).toString("HH:mm:ss"));
                }

                //Get unique dates
                var datesSet = new HashSet();
                for (i = 0; i < timeslots.length; i++) {
                    datesSet.add(Date.parse(timeslots[i].datetime).toString("yyyy-MM-dd"));
                }

                //Sort arrays
                var timesArray = timesSet.values().sort();
                var datesArray = datesSet.values().sort();

//            for (i = 0; i < datesArray.length; i++) {
//                console.log("Date is: " + datesArray[i]);
//            }
//
//            for (i = 0; i < timesArray.length; i++) {
//                console.log("Time is: " + timesArray[i]);
//            }

                //Append header names
                var headerString = "<thead><tr id='scheduleHeader'><td></td>";
                for (i = 0; i < datesArray.length; i++) {
                    headerString += "<td>" + new Date(datesArray[i]).toString('dd MMM yyyy') + "<br/>" + new Date(datesArray[i]).toString('ddd') + "</td>";
                }
                headerString += "</tr></thead>";
                $("." + tableClass).append(headerString);

                //Append timeslot data
                for (var i = 0; i < timesArray.length; i++) {
                    var htmlString = "<tr>";
                    var time = timesArray[i];
                    htmlString += "<td>" + time + "</td>";
                    for (var j = 0; j < datesArray.length; j++) {
                        var date = datesArray[j];

                        //Get the timeslot id from datetime
                        var id = getTimeslotId(timeslots, date, time);
                        htmlString += "<td id='timeslot_" + id + "'>";

                        //Get the team name from id
                        var team = getTeam(timeslots, id);
                        if (team !== null) {
                            htmlString += team;
                        }

                        //Close td
                        htmlString += "</td>";

                    }
                    htmlString += "</tr>";
                    $("." + tableClass).append(htmlString);
                }
            }

            function getTimeslotId(timeslots, date, time) {
                var datetimeString = (date + " " + time).trim();
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