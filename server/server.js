var express = require('express');
var app = express();
var morgan = require('morgan');
var bodyParser = require('body-parser');
var elasticSearch = require('elasticsearch');
var swig = require('swig');
var path = require('path');
var io = require('socket.io')();

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
app.listen(8081)

// Set up static rendering
app.use(express.static(path.join(__dirname, '/public')));
app.use('socketio', express.static(path.join(__dirname, '/node_modules/socket.io-client')));
app.set('views', path.join(__dirname, 'views'));
app.engine('html', swig.renderFile);
app.set('view engine', 'html');

app.get('/', function(req, res) {
    res.render('index');
});

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
