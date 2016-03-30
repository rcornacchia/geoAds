var map;
var tweets = [];
var gMarkers = [];
var range = "1000";

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
    google.maps.event.addListener(map, "click", function(event) {
        var lat = event.latLng.lat();
        var lng = event.latLng.lng();
        console.log("Lat=" + lat + "; Lng=" + lng + "; Range= " + range);
        map.clearOverlays();
        $.post(locationURL, {candidate: currentCandidate, lat: lat, lng: lng, range: range}, function(data){
            mapTweets(data);
        });
    });
    // fetch all tweets
    $.post(tweetsURL, {candidate: currentCandidate}, function(data){
        mapTweets(data);
    });
    // delete marker function
    google.maps.Map.prototype.clearOverlays = function() {
        for (var i = 0; i < gMarkers.length; i++ ) {
            gMarkers[i].setMap(null);
        }
        gMarkers = [];
    }
}
