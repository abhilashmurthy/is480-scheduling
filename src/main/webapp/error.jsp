<!DOCTYPE html>

<%@ taglib prefix="s" uri="/struts-tags" %>

<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>IS480 Scheduling System | Error</title>
        
        
        <% // if (request.getAttribute("error") == null) {response.sendRedirect("Index.jsp");} %>
        
        <style type="text/css">
            /* Override some defaults */
            html, body {
                background-color: #eee;
            }
            body {
                padding-top: 100px; 
            }
            .container {
                width: 800px;
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

            h1 {
                position: relative;
                text-align: center;
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
                <div class="row">
                    <h1><%= request.getAttribute("error")%></h1>
                </div>
            </div>
        </div> <!-- /container -->
        <%@include file="footer.jsp"%>
    </body>
</html>