<%@page import="model.User"%>

<!-- CSS imports -->
<!-- App -->
<link href="css/app.css" rel="stylesheet">
<!-- Plugins -->
<link href="css/redmond/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
<link href="css/redmond/jquery.timepicker.css" rel="stylesheet">
<link href="css/bootstrap.css" rel="stylesheet" media="screen">
<link href="css/jquery.pnotify.default.css" rel="stylesheet">
<link href="css/token-input.css" rel="stylesheet">
<link href="css/token-input-facebook.css" rel="stylesheet">
<link href="css/bootstrap-formhelpers.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.no-icons.min.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.css" rel="stylesheet">
<link href="css/bootstrap-switch.css" rel="stylesheet">

<!-- Google Fonts -->
<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'>

<!-- Ensure user has logged in -->
<% User user = (User) session.getAttribute("user");
if (session.getAttribute("user") == null && !request.getRequestURI().contains("login.jsp")) {
	response.sendRedirect("login.jsp");
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
</script>