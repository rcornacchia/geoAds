/**
 * Created by Kupletsky Sergey on 16.09.14.
 *
 * Hierarchical timing
 * Add specific delay for CSS3-animation to elements.
 */

(function($) {
    var speed = 2000;
    var container =  $('.display-animation');
    container.each(function() {
        var elements = $(this).children();
        elements.each(function() {
            var elementOffset = $(this).offset();
            var offset = elementOffset.left*0.8 + elementOffset.top;
            var delay = parseFloat(offset/speed).toFixed(2);
            $(this)
                .css("-webkit-animation-delay", delay+'s')
                .css("-o-animation-delay", delay+'s')
                .css("animation-delay", delay+'s')
                .addClass('animated');
        });
    });
})(jQuery);

/**
 * Created by Kupletsky Sergey on 04.09.14.
 *
 * Ripple-effect animation
 * Tested and working in: ?IE9+, Chrome (Mobile + Desktop), ?Safari, ?Opera, ?Firefox.
 * JQuery plugin add .ink span in element with class .ripple-effect
 * Animation work on CSS3 by add/remove class .animate to .ink span
*/

(function($) {
    $(".ripple-effect").click(function(e){
        var rippler = $(this);

        // create .ink element if it doesn't exist
        if(rippler.find(".ink").length == 0) {
            rippler.append("<span class='ink'></span>");
        }

        var ink = rippler.find(".ink");

        // prevent quick double clicks
        ink.removeClass("animate");

        // set .ink diametr
        if(!ink.height() && !ink.width())
        {
            var d = Math.max(rippler.outerWidth(), rippler.outerHeight());
            ink.css({height: d, width: d});
        }

        // get click coordinates
        var x = e.pageX - rippler.offset().left - ink.width()/2;
        var y = e.pageY - rippler.offset().top - ink.height()/2;

        // set .ink position and add class .animate
        ink.css({
            top: y+'px',
            left:x+'px'
        }).addClass("animate");
    })
})(jQuery);


var map;
var tweets = [];
var gMarkers = [];
var range = "1000";
var dummyData = [];
dummyData.push(40.804826, -73.966101);
dummyData.push(40.805508, -73.963794);
dummyData.push(40.803730, -73.965682);

function initMap() {
    $(function(){
       $(".dropdown-menu li a").click(function(){
         $(".btn:first-child").text($(this).text());
         $(".btn:first-child").val($(this).text());
      });
    });
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 40.8052, lng: -73.9640652},
        zoom: 16
    });



    // delete marker function
    google.maps.Map.prototype.clearOverlays = function() {
        for (var i = 0; i < gMarkers.length; i++ ) {
            gMarkers[i].setMap(null);
        }
        gMarkers = [];
    }
    google.maps.Map.prototype.createMarkers = function() {
        dummyData.forEach(function(location) {
            console.log("test");
            var position_options = {
                lat: parseFloat(location[0]),
                lng: parseFloat(location[1])
            };
            var marker = new google.maps.Marker({
                position: position_options,
                map: map
            });
        });
    }
    var a = new google.maps.LatLng(40.804826, -73.966101);
    var b = new google.maps.LatLng(40.805508, -73.963794);
    var c = new google.maps.LatLng(40.803730, -73.965682);

    var marker = new google.maps.Marker({
      position: a,
      map: map,
      title: 'Hello World!'
    });
    var marker = new google.maps.Marker({
      position: b,
      map: map,
      title: 'Hello World!'
    });
    var marker = new google.maps.Marker({
      position: c,
      map: map,
      title: 'Hello World!'
    });
}
