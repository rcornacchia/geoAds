var map;
var gMarkers = [];
var users = [];
var ads = [];
var range = "1000";
var numActiveUsers;
var numInactiveUsers;

function upsertDeviceMarker(devices) {
    // console.log("USERS:");
    // console.log(users);
    devices.forEach(function(device) {
        // obj.device.id is in the array
        var deviceId = device.gcm;
        var found = false;
        users.forEach(function(user) {
            if (user.device.gcm == deviceId) {
                found = true;
                var changed = false;
                if (user.device.state != device.state) {
                    changed = true;
                    // update user array
                    user.device = device;
                }
                if (user.device.location != device.location) {
                    changed = true;
                    // update user array
                    user.device = device;
                }
                if (changed) updateMarker(user);
            }

        });
        if (!found) {
            if(!!device.location) {
                var icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png";
                if (device.state == "off") {
                    icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/purple-dot.png"
                }
                // console.log(device);
                var position_options = {
                    lat: parseFloat(device.location.lat),
                    lng: parseFloat(device.location.lon)
                };
                var marker = new google.maps.Marker({
                    position: position_options,
                    icon: icon,
                    map: map
                });
                users.push({ device: device, marker: marker });
            }
        }
    });

    users.forEach(function(user) {
        user.stillHere = false;
        devices.forEach(function(device) {
            if(user.device.gcm == device.gcm) {
                // console.log("User still here");
                // console.log(user.device.gcm);
                // console.log(userStillHere);
                user.stillHere = true;
            }
        });
    });

    users.forEach(function(user) {
        if(user.stillHere == false){
            // console.log("USER LEFT");
            user.marker.setMap(null);
            var index = users.indexOf(user);
            users.splice(index, 1);
        }
    });
    numActiveUsers = 0;
    numInactiveUsers = 0;
    users.forEach(function(user) {
        // console.log(user.device.state);
        if(user.device.state == "off") {
            numInactiveUsers += 1;
        } else {
            numActiveUsers += 1;
        }
    });
    // console.log("Active Users: " + numActiveUsers);
    // console.log("Inactive Users: " + numInactiveUsers);
    $(document).ready(function(){
        $("#num-active-users").text(numActiveUsers);
        $("#num-inactive-users").text(numInactiveUsers);
        $("#num-total-users").text(numActiveUsers+numInactiveUsers);
    });
    // console.log("USERS after updating: ");
    // console.log(users);
}

function updateMarker(userMarker) {
    // Update google maps marker
    var device = userMarker.device;
    var icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png";
    if (device.state == "off") {
        icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/purple-dot.png";
    }

    var position_options = {
        lat: parseFloat(device.location.lat),
        lng: parseFloat(device.location.lon)
    };

    var marker = userMarker.marker;
    marker.setPosition(position_options);
    marker.setIcon(icon);
}

function createMarkers (data) {
    data.forEach(function(device) {
        if(!!device.location) {
            var icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png";
            if (device.state == "off") {
                icon = "http://maps.google.com/intl/en_us/mapfiles/ms/micons/purple-dot.png"
            }
            // console.log(device);
            var position_options = {
                lat: parseFloat(device.location.lat),
                lng: parseFloat(device.location.lon)
            };
            var marker = new google.maps.Marker({
                position: position_options,
                icon: icon,
                map: map
            });
            users.push({ device: device, marker: marker });
        }
    });
}

// Get devices within radius in meters around center {lat, lon}
function getDevicesAround(center, radius) {
    var uri = 'https://search-adbrother-2mnwlo4oaulpldztks3rg362i4.us-east-1.es.amazonaws.com/adbrother/userData/_search';
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
        // console.log(data);
        for(var i=0; i<obj.hits.hits.length; i++){
            var device = obj.hits.hits[i]._source;
            hits.push(device);
        }
        // console.log(hits);
        // add the markers to the map
        // mapDevices(hits);
        // createMarkers(hits);
        upsertDeviceMarker(hits);
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
getDevicesAround(coordinates, range);

setInterval(function() {
    getDevicesAround(coordinates, range)
}, 1000);

$(document).ready(function(){
    $("#submitAd").on('click', function(e){
        e.preventDefault();
        var title = $("#adTitle").val();
        var discount = $("#discount").val();
        var link = $("#link").val();
        // console.log(title);
        // console.log(discount);
        // console.log(link);

        var adObject = {
            title: title,
            msg: discount,
            url: link,
            id: ads.length + 1
        };

        ads.push(adObject);

        // Also insert it into elasticSearch here

        users.forEach(function(user) {
            // $.post('/targetedAd', {ad: adObject, targetId: user.device.gcm}, function(data){
            //     // mapusers(data);
            //     console.log(data);
            // });
            console.log(user);
            console.log(user.device.gcm);
            $.ajax({
              type: "POST",
              url: '/targetedAd',
              data: JSON.stringify({ad: adObject, targetId: user.device.gcm}),
              headers: {'content-type' : 'application/json'},
              success: function(data) {
                  console.log(data);
              }
            });
        });
        var adText = title
        $("#1-title").text(title);
        $("#1-discount").text(discount + "%");
        $("#1-link").text(link);

    });
});

$(document).ready(function(){
    $("#btnSubmit").on('click', function(e){
        e.preventDefault();
        range = $("#range").val();
        $("#range").val('');
        $("#rangeText").text("Current Range: "+range+"m");
    });
});

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
