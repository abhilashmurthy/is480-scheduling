<%@page import="util.MiscUtil"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Login</title>

        <!-- Le imports -->
        <% if (session.getAttribute("user") != null) {
                response.sendRedirect("index.jsp");
            }%>
        <%@include file="imports.jsp"%>

        <style type="text/css">
            /* Override some defaults */
            html, body {
                background-color: #eee;
            }
            body {
                padding-top: 100px; 
            }
            .container {
                width: 600px !important;
            }

            /* The white background content wrapper */
            .container > .content {
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
                    <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                    </button>
                </div>
            </div>
        </div>


        <div class="container">
            <div class="content">
                <div>
                    <img src="img/IS480-logo.jpg" style="height:150px; width:450px; display:inline-block;" />
                </div>
                <div class="row">

                    <h2>IS480 Scheduling System</h2>
					<form method="POST" action="adminLogin" >
						<table style="margin: auto">
							<tr>
								<td>Username:&nbsp;</td>
								<td style="padding-bottom: 10px"><input name="username" type="text" ></td>
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

        <!-- To display the login message error -->
        <div class="container" style="margin-top: 50px; margin-bottom: 10px">
            <div class="row">
                <% Object loginMsg = request.getAttribute("error");
                    String loginError = "";
                    if (loginMsg != null) {
                        loginError = loginMsg.toString();
                    }
                %>
                <p class="text-error" style="text-align:center">
                    <strong><%= loginError%></strong>
                </p>
            </div>
        </div> <!-- /container -->

        <%@include file="footer.jsp"%>
    </body>
</html>