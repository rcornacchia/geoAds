var map;
var markers = [];

function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 40.8052, lng: -73.9640652},
        zoom: 16
    });
}
