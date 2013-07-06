define([
    'jquery',
    'bootstrap',
    'plugins/jquery-ui/js/jquery-ui-1.10.3.custom.min',
    'plugins/jquery-ui/js/jquery.timepicker.min'
], function($, bootstrap, jqueryui, timepicker) {
    return {
        //Initialize function
        init: function() {
            $("#datepicker").datepicker({
                beforeShowDay: $.datepicker.noWeekends,
                dateFormat: "yy-mm-dd"
            });

            $("#timepicker").timepicker();
        }
    };
});