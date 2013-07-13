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
        <%@include file="footer.jsp"%>
        <script type="text/javascript">
            console.log("knockoutapp init");

            //KNOCKOUT STUFF
            var userModel = function(id, name, username) {
                var self = this; //caching so that it can be accessed later in a different context
                this.id = ko.observable(id); //unique id for the student (auto increment primary key from the database)
                this.name = ko.observable(name); //name of the student
                this.username = ko.observable(username);
                this.nameUpdate = ko.observable(false); //if the name is currently updated
                this.usernameUpdate = ko.observable(false); //if the username is currently updated

                //executed if the user clicks on the span for the student name
                this.nameUpdating = function() {
                    self.nameUpdate(true); //make nameUpdate equal to true
                };

                //executed if the user clicks on the span for the student username
                this.usernameUpdating = function() {
                    self.usernameUpdate(true); //make usernameUpdate equal to true
                };

            };

            var model = function() {
                var self = this; //cache the current context
                this.user_name = ko.observable(""); //default value for the student name
                this.user_username = ko.observable("");
                this.user_name_focus = ko.observable(true); //if the student name text field has focus
                this.users = ko.observableArray([]); //this will store all the students

                //Create User
                this.createUser = function() {
                    if (self.validateUser()) { //if the validation succeeded
                        console.log("Create clicked");
                        //build the data to be submitted to the server

                        //submit the data to the server        
                        $.ajax({
                            url: 'knockout',
                            type: 'POST',
                            data: {'name': this.user_name(), 'username': this.user_username(), 'action': 'insert'}
                        }).done(function(id) {
                            //push a new record to the student array
                            console.log("Got response");
                            self.users.push(new userModel(id, self.user_name(), self.user_username()));
                            self.user_name(""); //empty the text field for the student name
                            self.user_username("");
                        }).fail(function(error) {
                            console.log("Received an error");
                        });

                    } else { //if the validation fails
                        alert("Name and username are required and username should be a number!");
                    }
                };

                //Validate
                this.validateUser = function() {
                    if (self.user_name() !== "" && self.user_username() !== "") {
                        return true;
                    }
                    return false;
                };

                //Load records
                this.loadData = function() {
                    console.log("reached Load");
                    //fetch existing student data from database
                    $.ajax({
                        url: 'knockout',
                        dataType: 'json'
                    }).done(function(data) {
                        var records = data.result;
                        console.log(data);

                        for (var x in records) {
                            console.log("Record found");
                            //student details
                            var id = records[x].id;
                            var name = records[x].name;
                            var username = records[x].username;

                            //push each of the student record to the observable array for 
                            //storing student data
                            self.users.push(new userModel(id, name, username));
                        }
                    }).fail(function(error) {

                    });
                };

                //DELETE
                this.removeUser = function(user) {
                    $.ajax({
                        type: 'POST',
                        url: 'knockout',
                        data: {'action': 'delete', 'student_id': user.id()}
                    }).done(function(response) {
                        //remove the currently selected student from the array
                        self.users.remove(user);
                    }).fail(function(error) {

                    });
                };

                //UPDATE
                this.updateUser = function(user) {
                    //get the student details
                    var id = user.id();
                    var name = user.name();
                    var username = user.username();

                    //build the data
                    var student = {'id': id, 'name': name, 'username': username};

                    //submit to server via POST
                    $.ajax({
                        type: 'POST',
                        url: 'knockout',
                        data: {'action': 'update', 'student': student}
                    });
                };

            };

            ko.applyBindings(new model());
        </script>
    </body>
</html>
