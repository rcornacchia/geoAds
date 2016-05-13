var map;
var gMarkers = [];
var users = [];
var range = "1000";

function mapMarkers(gMarkers) {
    var state;
    var icon;
    gMarkers.forEach(function(gMarker) {
        console.log(gMarker);
        var position_options = {
            lat: parseFloat(gMarker[0].lat),
            lng: parseFloat(gMarker[0].lon)
        };

        state = gMarker[1];
        if(state == "off") {
            icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/purple-dot.png"
        } else {
            icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png"
        }
        var marker = new google.maps.Marker({
            position: position_options,
            icon: icon,
            map: map
        });
    });
}

// Get devices within radius in meters around center {lat, lon}
function getDevicesAround(center, radius) {
    var uri = 'https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/userData/_search';
    var json = {
        query: {
            filtered: {
                query: {
                    match_all: {}
                },
                filter: {
                    geo_distance: {
                        distance: radius + 'm',
                        location: center
                    }
                }
            }
        }
    };
    var json2 = JSON.stringify(json);
    $.post(uri, json2, function(data){
        var hits = [];
        obj = data;
        console.log(data);
        for(var i=0; i<obj.hits.hits.length; i++){
            hits.push([obj.hits.hits[i]._source.location, obj.hits.hits[i]._source.state, obj.hits.hits[i]._source.speed]);
        }
        hits.forEach(function(hit) {
            console.log(hit);
        });
        // add the markers to the map
        mapMarkers(hits);
    });
}

function initMap() {
    $(function(){
       $(".dropdown-menu li a").click(function(){
         $(".btn:first-child").text($(this).text());
         $(".btn:first-child").val($(this).text());
      });
    });
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 40.8052, lng: -73.9640652},
        zoom: 15
    });

    // delete marker function
    google.maps.Map.prototype.clearOverlays = function() {
        for (var i = 0; i < gMarkers.length; i++ ) {
            gMarkers[i].setMap(null);
        }
        gMarkers = [];
    }
    google.maps.Map.prototype.createMarkers = function(gMarkers) {
        gMarkers.forEach(function(location) {
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
    var coordinates = {
        lat: parseFloat(40.805920),
        lng: parseFloat(-73.965749)
    };
    var businessLocation = new google.maps.Marker({
        position: coordinates,
        map: map
    });
}

var coordinates = {
    lat: parseFloat(40.805920),
    lon: parseFloat(-73.965749)
};
getDevicesAround(coordinates, 1000);










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
