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
/* Require gcm with:
var gcm = require('/gcm.js');
*/

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

app.get('/', function (req, res) {
  res.sendfile(__dirname + '/index.html');
});

io.on('connection', function (socket) {
  socket.emit('news', { hello: 'world' });
  socket.on('my other event', function (data) {
    console.log(data);
  });
});


/*
It is assumed that credentials are set in ~/.aws/credentials or
AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY have been exported
as environment variables.
*/
var AWS = require('aws-sdk');
var SQS = new AWS.SQS({ // Look into notifying with SNS instead
    region: 'us-east-1'
});

// Connect to ElasticSearch cluster
var client = new elasticSearch.Client({
    host: 'search-geo-ads-qpjwkygbkgrhsv6wjg7wyk7scu.us-east-1.es.amazonaws.com',
    log: 'trace'
});

// Configure app for listening for post requests
app.use(bodyParser.json());
app.use(morgan('dev'));

// Set up static rendering
app.use(express.static(path.join(__dirname, '/public')));
app.use('socketio', express.static(path.join(__dirname, '/node_modules/socket.io-client')));
app.set('views', path.join(__dirname, 'views'));
app.engine('html', swig.renderFile);
app.set('view engine', 'html');

// app.get('/', function(req, res) {
//     res.render('index');
// });

app.post('/location', function(req, res) {
    // We might want to do some input checking / validation here

    /* {
        id: String, // this is the unique android device ID
        lat: Number,
        lon: Number
    } */

    console.log(req.body);
    io.emit('location_update', {location: req.body});
    // client.update({
    //     index: 'devices',
    //     type: 'device',
    //     id: req.body.id,
    //     body: {
    //         doc: {
    //             location: {
    //                 'lat': req.body.location.lat,
    //                 'lon': req.body.location.lon
    //             }
    //         },
    //         doc_as_upsert: true
    //     }
    // });

    // var params = {
    //     MessageBody: JSON.stringify(req.body),
    //     QueueUrl: 'https://sqs.us-east-1.amazonaws.com/746215537304/geoAds',
    // };
    //
    // SQS.sendMessage(params, function(error, data) {
    //     if (error) console.log(error, error.stack);
    //     else console.log(data);
    // });

    res.json(req.body);
});
