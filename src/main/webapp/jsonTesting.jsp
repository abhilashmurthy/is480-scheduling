<%-- 
    Document   : test
    Created on : Sep 24, 2013, 2:57:24 PM
    Author     : suresh
--%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
		<%@include file="header.jsp" %>
        <title>JSON Testing</title>
		<style type="text/css">
			textarea {
				
			}
		</style>
    </head>
    <body class="container">
        <%@include file="navbar.jsp" %>
		<h1>JSON Testing Central</h1>
		<h3>URL</h3>
		<input type="text" name="url" id="testingURL"/>
		<h3>Input</h3>
		<textarea id="testingInput" style="font-family: monospace !important; width: 600px; height: 100px;"></textarea>
		<br /><button class="btn btn-primary" id="testSubmit" data-loading-text="Testing...">Test!</button><br />
		<!-- SOURCE CODE SYNTAX HIGHLIGHTING -->
		<script src="https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js"></script>
		<h3>Response</h3>
		<div class="well well-large">
			<pre class="prettyprint" id="jsonResponse"></pre>
		</div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
		$(document).ready(function(){
			$("#testSubmit").click(function() {
				$(this).button('loading');
				var testingInput = $("#testingInput").val();
				$.ajax({
					type: 'POST',
					async: false,
					url: $("#testingURL").val(),
					data: {jsonData: testingInput}
				}).done(function(response) {
					$("#testSubmit").button('reset');
					$("#jsonResponse").text(JSON.stringify(response, null, 4));
					$("#jsonResponse").removeClass("prettyprinted");
					colorSyntax();
				}).fail(function(response) {
					$("#testSubmit").button('reset');
					$("#jsonResponse").text("AJAX Call failed");
				});
			});		
			
			function colorSyntax() {
				prettyPrint();
			}
		});
		</script>
    </body>
</html>
