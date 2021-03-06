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

// Get devices within radius in meters around center {lat, lon}
function getDevicesAround(center, radius) {
    var requestParams = {
        // Eventually pull this url out of here
        uri: 'https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/userData/_search',
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
            var results = response.hits.hits.map(function(hit) {
                return hit._source;
            });
            return results;
        }
    };
    return rp(requestParams);
}

server.listen(8000);

// Configure app for listening for post requests
app.use(bodyParser.json());
app.use(morgan('dev'));

// Set up static rendering
app.use(express.static(path.join(__dirname, '/public')));
app.use('socketio', express.static(path.join(__dirname, '/node_modules/socket.io-client')));
app.set('views', path.join(__dirname, 'views'));
app.engine('html', swig.renderFile);
app.set('view engine', 'html');

app.post('/rejectAd', function(req, res) {
    var androidId = req.body.androidId;
    var adId = req.body.adId;

    // If we keep array of ads in browser memory
    // emit on socket.io

    // Else if we keep ads in elasticsearch
    // var updateParams = {
    //     uri: 'https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/userData/' + androidId + '/_update',
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

    // or if we store on elasticSearch
    // var updateParams = {
    //     uri: 'https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/userData/' + androidId + '/_update',
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

    // Redirect user
    var link = req.query.link;
    res.redirect(link);
});
