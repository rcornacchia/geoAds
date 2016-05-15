var express = require('express');
var app = express();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var morgan = require('morgan');
var bodyParser = require('body-parser');
var elasticSearch = require('elasticsearch');
var swig = require('swig');
var path = require('path');
var rp = require('request-promise');
var gcm = require('./gcm.js');



io.on('connection', function (socket) {
  socket.emit('news', { hello: 'world' });
  socket.on('my other event', function (data) {
    console.log(data);
  });
});


// io.emit('location_update', {location: req.body});




// Get devices within radius in meters around center {lat, lon}
function getDevicesAround(center, radius) {
    var requestParams = {
        // Eventually pull this url out of here
        uri: 'https://search-adbrother-2mnwlo4oaulpldztks3rg362i4.us-east-1.es.amazonaws.com/adbrother/_search',
        method: 'POST',
        json: {
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
        },
        transform: function(response) {
            if(!!response){
                var results = response.hits.hits.map(function(hit) {
                    return hit._source;
                });
            }
            return results;
        }
    };
    return rp(requestParams);
}

server.listen(8000);
console.log("Server started on port 8000.");

// Configure app for listening for post requests
app.use(bodyParser.json());
app.use(morgan('dev'));

// Set up static rendering
app.use(express.static(path.join(__dirname, '/public')));

app.post('/targetedAd', function(req, res) {
    var ad = req.body.ad;
    var targetId = req.body.targetId;
    gcm.sendNotification(targetId, ad);
});

app.post('/broadcastAd', function(req, res) {
    var ad = req.body;
    var targetLocation = req.body.targetLocation;
    var radius = req.body.radius;
    gcm.broadcastNotification(targetLocation, radius, ad);
});

app.post('/rejectAd', function(req, res) {
    var androidId = req.body.androidId;
    var adId = req.body.adId;
    console.log("ad rejected: " + androidId + ', ' + adId);

    // If we keep array of ads in browser memory
    // emit on socket.io

    //Else if we keep ads in elasticsearch
    // var updateParams = {
    //     uri: 'https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/adCounters/' + adId + '/_update',
    //     method: 'POST',
    //     json: {
    //         script: "ctx._source.rejected += 1",
    //         upsert: {
    //             "rejected" : 1
    //         }
    //     }
    // }
    // rp(updateParams)
    //     .then(function(response) {
    //         console.log("success: " + response);
    //     })
    //     .catch(function(error) {
    //         console.log("rejectedAd failed: " + _error);
    //     });
});

app.get('/redirect', function(req, res) {
    console.log(req.query);
    var adId = req.query.adId;
    var androidId = req.query.androidId;

    // Emit accepted ad on socket io

    // //or if we store on elasticSearch
    // var updateParams = {
    //     uri: 'https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/adCounters/' + adId + '/_update',
    //     method: 'POST',
    //     json: {
    //         script: "ctx._source.clicks += 1",
    //         upsert: {
    //             "clicks" : 1
    //         }
    //     }
    // }
    // rp(updateParams)
    //     .then(function(response) {
    //         console.log("success: " + response);
    //     })
    //     .catch(function(error) {
    //         console.log("acceptedAd failed: " + _error);
    //     });
    //
    // // Redirect user
    // var link = req.query.link;
    res.redirect(link);
});
