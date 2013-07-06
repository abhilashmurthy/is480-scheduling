define([
    'jquery',
    'bootstrap'
], function($, bootstrap) {
   return {
       //Initialize function
       init: function() {
           $("#welcomeText").on('click', function(){
              alert("Hello there!"); 
           });
       }
   };
});