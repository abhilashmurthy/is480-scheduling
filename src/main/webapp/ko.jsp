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
<!--        <div class="container page" >
            <ul data-bind="foreach: places">
                <li data-bind="text: $data, event: { mouseover: $parent.logMouseOver }"> </li>
            </ul>
            <p>You seem to be interested in: <span data-bind="text: lastInterest"> </span></p>

            <script type="text/javascript">
                function MyViewModel() {
                    var self = this;
                    self.lastInterest = ko.observable();
                    self.places = ko.observableArray(['London', 'Paris', 'Tokyo']);

                    // The current item will be passed as the first parameter, so we know which place was hovered over
                    self.logMouseOver = function(place) {
                        self.lastInterest(place);
                    }
                }
                ko.applyBindings(new MyViewModel());
            </script>
        </div>-->
        
<div class="container page">
        <p>Send me spam: <input type="checkbox" data-bind="checked: wantsSpam" /></p>
        <div data-bind="visible: wantsSpam">
            Preferred flavors of spam:
            <div><input type="checkbox" value="cherry" data-bind="checked: spamFlavors" /> Cherry</div>
            <div><input type="checkbox" value="almond" data-bind="checked: spamFlavors" /> Almond</div>
            <div><input type="checkbox" value="msg" data-bind="checked: spamFlavors" /> Monosodium Glutamate</div>
        </div>
        
            <script type="text/javascript">
                var viewModel = {
                    wantsSpam: ko.observable(true),
                    spamFlavors: ko.observableArray(["cherry", "almond"]) // Initially checks the Cherry and Almond checkboxes
                };

                // ... then later ...
                viewModel.wantsSpam(false); // The checkbox becomes unchecked
                viewModel.spamFlavors.push("msg"); // Now additionally checks the Monosodium Glutamate checkbox
            </script>
        </div>
    </body>
</html>
