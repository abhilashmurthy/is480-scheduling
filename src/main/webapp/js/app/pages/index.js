define([
    'jquery',
    'bootstrap'
], function($, bootstrap) {
    return {
        //Initialize function
        init: function() {
            $("#welcomeText").on('click', function() {
                alert("Hello there!");
            });

            $('#mileStoneTab a').on('click', function() {
                
                console.log("$(this).attr('id')");
                
//                //Tab effects
//                $(this).on('click', function(e) {
//                    e.preventDefault();
//                    $(this).tab('show');
//                    
//                    console.log("Hello");
//
//                    //Content effects
//                    var contentId = $(this).attr('id') + "Content";
//                    console.log(contentId);
//
//                    $(".tab-pane").removeClass("active in");
//                    $("#" + contentId).addClass("active in");
//
//                });
            });

        }
    };
});