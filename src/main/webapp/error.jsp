<!DOCTYPE html>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>IS480 Scheduling System | Error</title>

        <% if (request.getAttribute("error") == null) {
            request.setAttribute("error", "Oops, something went wrong.<br/>");
        } %>
        <style type="text/css">

            h1 {
                position: relative;
                text-align: center;
            }

        </style>

    </head>
    <body>
        <%@include file="navbar.jsp"%>
        <!-- Navigation Login -->
<!--        <div class="navbar navbar-inverse navbar-fixed-top">
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
        </div>-->


        <div class="container">
            <div class="content">
                <div class="row">
                    <h1><%= request.getAttribute("error")%></h1><br/>
                    <h1><a href="/is480-scheduling">Back to Home</a></h1>
                </div>
            </div>
        </div> <!-- /container -->
        <%@include file="footer.jsp"%>
    </body>
</html>