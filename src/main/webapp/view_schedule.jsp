<!-- EMBEDDABLE PAGE. NOT TO BE USED ALONE -->
<table id="scheduleTable" class="table-condensed table-hover table-bordered table-striped">
	<thead>
		<tr id="scheduleHeader">
		</tr>
	</thead>
	<tbody id="scheduleBody">
	</tbody>
</table>
<%@include file="footer.jsp" %>
<script type="text/javascript">
	$('#scheduleTable').ready(function() {
		$.ajax({
			type: 'GET',
			url: 'getSchedule',
			dataType: 'json'
		}).done(function(response) {
			makeSchedule(response);
		});
	});
	
	
	function makeSchedule(data) {
		var startDate = new Date(data.startDate);
		var endDate = new Date(data.endDate);
		var dateArray = [];
		var tempDate = startDate;
		
		while(tempDate <= endDate) {
			dateArray.push(tempDate);
			tempDate = new Date(tempDate).addDays(1);
		}
		
		initTable(dateArray);
	}
	
	function initTable(dateList) {
		var dayStr = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
		//Creating the row time markings (Interval: 30 min)
		for (var i = 8; i < 19; i++) {
			$('#scheduleBody').append("<tr id=r" + i + "0><td>" + i + ":" + "00</td></tr>");
			$('#scheduleBody').append("<tr id=r" + i + "3><td>" + i + ":" + "30</td></tr>");
		}
		
		$('#scheduleHeader').append("<th></th>");
		//Adding the columns for the days
		for (var i = 0; i < dateList.length; i++) {
			var date = dateList[i];
			var dayOfWeek = dayStr[date.getDay()];
			var day = date.getDate();
			$('#scheduleHeader').append("<th id=d" + day + "m" + date.getMonth() + ">" + date.toString('dd MMM') + "<br />"
					+ dayOfWeek + "</th>");
		}
	}
	
</script>