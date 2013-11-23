<%@page import="model.User"%>

<!-- CSS imports -->
<!-- Plugins -->
<link href="css/jqueryui/redmond/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
<link href="css/jqueryui/redmond/jquery.timepicker.css" rel="stylesheet">
<link href="css/jquery/jquery.pnotify.default.css" rel="stylesheet">
<link href="css/jquery/token-input.css" rel="stylesheet">
<link href="css/jquery/token-input-facebook.css" rel="stylesheet">
<link href="css/bootstrap/bootstrap-formhelpers.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.no-icons.min.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/4.0.0/css/font-awesome.min.css" rel="stylesheet">
<link href="css/bootstrap/bootstrap-switch.css" rel="stylesheet">
<link href="css/jquery/jquery.dataTables.css" rel="stylesheet">
<link href="css/bootstrap/bootstrap-multiselect.css" rel="stylesheet">
<link href="css/jquery/jquery.jqplot.min.css" rel="stylesheet">
<!-- CSS specific to Fuel UX -->
<link href="css/bootstrap/fuelux.css" rel="stylesheet" />
<!-- Google Fonts -->
<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'>
<!-- App -->
<link href="css/app.css" rel="stylesheet">

<!-- Ensure user has logged in -->
<% User user = (User) session.getAttribute("user");
if (user == null && !request.getRequestURI().contains("hello.jsp")) {
	response.sendRedirect("hello.jsp");
	return;
} else if (user.getUsername().equals("_") && !request.getRequestURI().contains("login.jsp")) {
	session.invalidate();
	response.sendRedirect("hello.jsp");
	return; 
}
%>

<script type="text/javascript">
    //This is used for multiple window.onload's	
    function addLoadEvent(func) {
        var oldonload = window.onload;
        if (typeof window.onload !== 'function') {
            window.onload = func;
        } else {
            window.onload = function() {
                if (oldonload) {
                    oldonload();
                }
                func();
            };
        }
    }
	
	//UAT mode
	var uatMode = true;
	function recordHumanInteraction(e) {
		var $this = $(e.target);
		while ($this[0].tagName !== 'BODY' && !$this.attr('class')
				|| $this[0].tagName === 'LI'
				|| ($this[0].tagName === 'TD' && !$this.attr('id'))
				|| ($this[0].tagName === 'TR' && !$this.attr('id'))
				|| $this[0].tagName === 'TH'
				|| $this[0].tagName === 'THEAD'
				|| $this[0].tagName === 'TBODY') {
			$this = $this.parent();
		}
		var tagName = $this[0].tagName;
		var data = {
			action: 'clicked',
			url: location.href,
			target: $(e.target)[0].tagName,
			clickedItem: tagName + '[class = ' + $this.attr('class') + ', id = ' + $this.attr('id') + ']'
		};
		if (uatMode) {
			console.log(JSON.stringify(data));
			$.ajax({
				type: 'POST',
				url: 'recordHumanInteraction',
				data: {jsonData: JSON.stringify(data)}
			}).done(function(response) {
			}).fail(function(error) {
			});
		}
	}
	
	var documentLoaded = function() {
		var data = {
			action: 'loaded',
			url: location.href
		};
		if (uatMode) {
			console.log(JSON.stringify(data));
			$.ajax({
				type: 'POST',
				url: 'recordHumanInteraction',
				data: {jsonData: JSON.stringify(data)}
			}).done(function(response) {
			}).fail(function(error) {
			});
		}
	};
	
	addLoadEvent(documentLoaded);
	
</script>