//Config
require.config({
    baseUrl: 'js/',
    paths: {
        jquery: 'plugins/jquery-2.0.2',
        bootstrap: 'plugins/bootstrap',
        knockout: 'plugins/knockout'
    }
});

//Main logic
require(['jquery', 'bootstrap', 'knockout'], function($, bootstrap, knockout) {

    $(".dropdown-toggle").on('click', function() {
        this.dropdown;
    });

    $("#logoutLink").on('click', function() {
        document.location.href = '/is480-scheduling/logout';
    });

    function blink(selector) {
        $(selector).fadeOut('slow', function() {
            $(this).fadeIn('slow', function() {
                blink(this);
            });
        });
    }

    $("#ssoBtn").on('click', function() {
        //Send an AJAX call to the IS480PSAS website
        $(".loadingContainer").append("<p>Logging in</p>");
        blink('p');
        window.location = 'https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS';
    });

    $(".nav li").on('click', function() {
        $(".nav li").removeClass("active");
        this.addClass("active");
    });




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
        
        self.logMouseOver = function() {
            self.user_name = ko.observable(""); //default value for the student name
            self.user_username = ko.observable("");
            self.user_name_focus = ko.observable(true); //if the student name text field has focus
            self.users = ko.observableArray([]); //this will store all the students
            self.loadData();
        };

        //Create User
        this.createUser = function() {
            if (self.validateUser()) { //if the validation succeeded
                console.log("Create clicked");
                //build the data to be submitted to the server

                //submit the data to the server        
                $.ajax(
                        {
                            url: 'knockout',
                            type: 'POST',
                            data: {'name': this.user_name(), 'username': this.user_username(), 'action': 'insert'},
                            success: function(id) {//id is returned from the server
                                //push a new record to the student array
                                self.users.push(new userModel(id, self.user_name(), self.user_username()));
                                self.user_name(""); //empty the text field for the student name
                                self.user_username("");
                            }
                        }
                );

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
        }

        //DELETE
        this.removeUser = function(user) {
            $.post(
                    'knockout',
                    {'action': 'delete', 'student_id': user.id()},
            function(response) {

                //remove the currently selected student from the array
                self.users.remove(user);
            }
            );
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
            $.post(
                    'knockout',
                    {'action': 'update', 'student': student}
            );
        };

    };

    ko.applyBindings(new model());


});