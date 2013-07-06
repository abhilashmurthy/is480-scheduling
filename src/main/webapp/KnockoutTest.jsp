<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>IS480 Scheduling</title>
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <div class="container page" >
        <div class="container" data-bind="load: loadData()">
            <div class="new_user">
                <input type="text" class="name" placeholder="name" data-bind="value: user_name, hasfocus: user_name_focus()">
                <input type="text" class="username" placeholder="username" data-bind="value: user_username">
                <button data-bind="click: createUser">Create</button>        
            </div>

            <table data-bind="visible: users().length > 0" class="users">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Username</th>
                        <th>Remove</th>
                        <th>Update</th>
                    </tr>
                </thead>
                <tbody data-bind="foreach: users">
                    <tr>
                        <td>
                            <span data-bind="text: name, click: nameUpdating, event: { mouseover: $parent.logMouseOver }, visible: !nameUpdate()"></span>
                            <!--<input type="text" class="name" data-bind="value: name, visible: nameUpdate, hasfocus: nameUpdate">-->
                        </td>
                        <td>
                            <span data-bind="text: username, event: { mouseover: $parent.logMouseOver }, click: usernameUpdating, visible: !usernameUpdate()"></span>
                            <!--<input type="text" class="username" data-bind="value: username, visible: usernameUpdate, hasfocus: usernameUpdate">-->
                        </td>
                        <td data-bind="click: $root.removeUser"><a href="#">remove</a></td>
                        <td data-bind="click: $root.updateUser"><a href="#">update</a></td>
                    </tr>
                </tbody>
            </table>  
        </div>
    </div>
</body>
</html>
