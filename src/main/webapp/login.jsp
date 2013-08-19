<%@page import="util.MiscUtil"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>IS480 Scheduling System | Login</title>

        <!-- Le styles -->
        <link href="css/bootstrap.css" rel="stylesheet">
        <% if (session.getAttribute("user") != null) {
                response.sendRedirect("index.jsp");
            }%>

        <style type="text/css">
            /* Override some defaults */
            html, body {
                background-color: #eee;
            }
            body {
                padding-top: 100px; 
            }
            .container {
                width: 600px;
            }

            /* The white background content wrapper */
            .container > .content {
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
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="login-form brand">IS480 Scheduling System</a>
                </div>
            </div>
        </div>


        <div class="container">
            <div class="content">
                <div>
                    <img src="img/IS480-logo.jpg"/>
                </div>
                <div class="row">

                    <h2>IS480 Scheduling System</h2>
                    <button id="ssoBtn" class="btn btn-primary" data-loading-text="Logging in..." type="submit">SMU Single Sign-On</button>
                </div>
            </div>
        </div> <!-- /container -->
        <div class="container">
            <div class="loadingContainer">
            </div>
        </div>

        <!-- To display the login message error -->
        <div class="container">
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
        <script type="text/javascript">
            <% if (true) {%>
            $("#ssoBtn").on('click', function() {
                $(this).button('loading');
                var userId = prompt('Please enter the Username', '');
                if (userId !== null && userId !== '') {
                    window.location = 'login?smu_username=' + userId;
                } else {
                    alert('Invalid Username');
                    $(this).button('reset');
                }
            });
            <% } else {%>
            $("#ssoBtn").on('click', function() {
                $(this).button('loading');
                //blink(this);
                window.location = 'https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS';
            });
            <% }%>
        </script>
    </body>
</html>