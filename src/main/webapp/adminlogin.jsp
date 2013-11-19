<%@page import="util.MiscUtil"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Login</title>

        <!-- Le imports -->
        <% 
			if (session.getAttribute("user") != null && !((User) session.getAttribute("user")).getUsername().equals("_")) {
				response.sendRedirect("index");
			}
		%>
        <%@include file="imports.jsp"%>

        <style type="text/css">
            /* Override some defaults */
            html, body {
                background-color: #eee;
            }
			
            body {
                padding-top: 100px; 
            }
			
            .loginBody > .container {
                width: 600px !important;
            }
			
			.container > .content {
            /* The white background content wrapper */
		text-align: center;
                background-color: #fff;
                padding: 20px;
                margin: 0 -20px; 
                -webkit-border-radius: 10px 10px 10px 10px;
                -moz-border-radius: 10px 10px 10px 10px;
                border-radius: 10px 10px 10px 10px;
                -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.15);
                -moz-box-shadow: 0 1px 2px rgba(0,0,0,.15);
                box-shadow: 0 1px 2px rgba(0,0,0,.15);
            }

            .login-form {
                margin-left: 65px;
            }

            legend {
                margin-right: -50px;
                font-weight: bold;
                color: #404040;
            }

            h2 {
                position: relative;
                text-align: center;
            }

            #ssoBtn {
                display: block;
                margin: 0 auto;
                width: 300px;
                text-align: center;
                font-size: 16px;
            }
			
			#testBtn {
				display: block;
				margin: auto;
			}
			
			#testLoginMsg {
				text-align: center;
			}

            .loadingContainer {
                padding-top: 60px;
                text-align: center;
            }

            .loadingContainer p {
                font-weight: 500;
                font-size: 14px;
                color: red;
            }

        </style>

    </head>
    <body>

        <!-- Navigation Login -->
        <div class="navbar navbar-inverse navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
					<a class="brand" href="index">
						<img src="img/IS480-navbar.png" style="height:25px; width:120px; display:inline-block;"/>
					</a>
                    <!--<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">-->
                    <!--</button>-->
                </div>
            </div>
        </div>

		<div class="loginBody">
			<div class="container">
				<div class="content">
					<div>
						<img src="img/IS480-logo.jpg" style="height:150px; width:450px; display:inline-block;" />
					</div>
					<div class="row">

						<h2>IS480 Scheduling System</h2>
						<form method="POST" action="login" >
							<input name="bypass" style="display: none">
							<table style="margin: auto">
								<tr>
									<td>Username:&nbsp;</td>
									<td style="padding-bottom: 10px"><input name="smu_username" type="text" ></td>
								</tr>
								<tr>
									<td>Password:&nbsp;</td>
									<td><input name="password" type="password" ></td>
								</tr>
							</table>
							<div style="margin-bottom: 10px"></div><br />

							<button id="ssoBtn" class="btn btn-inverse" type="submit">Login</button>
						</form>

					</div>
				</div>
			</div> <!-- /container -->
		</div>	
        <!-- To display the login message error -->
        <div class="container" style="margin-top: 50px; margin-bottom: 10px">
            <div class="row">
                <p id ="errorMsg" class="text-error" style="text-align:center">
                </p>
            </div>
        </div> <!-- /container -->

        <%@include file="footer.jsp"%>
		<script type="text/javascript">
			function getParameterByName(name) {
				name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
				var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
					results = regex.exec(location.search);
				return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
			}
			
			errorMsgLoad = function() {
				var errorMessage = getParameterByName("error");
				$("#errorMsg").html("<strong>" + errorMessage + "</strong>");
			};
			addLoadEvent(errorMsgLoad);
		</script>
    </body>
</html>