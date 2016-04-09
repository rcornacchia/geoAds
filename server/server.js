var app = require('express')();
var morgan = require('morgan');
var bodyParser = require('body-parser');
var elasticSearch = require('elasticsearch');

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
    host: '', // create a new one on AWS
    log: 'trace'
});

// Configure app for listening for post requests
app.use(bodyParser.json());
app.use(morgan('dev'));
app.listen(8081)

app.post('/location', function(req, res) {

    // We might want to do some input checking / validation here

    /* {
        id: Number, // this is the unique android device ID
        location: {
            lat: Number,
            lon: Number
        }
    } */
    client.update({
        index: 'devices',
        type: 'device',
        id: req.body.id,
        body: {
            doc: {
                location: {
                    'lat': req.body.location.lat,
                    'lon': req.body.location.lon
                }
            },
            doc_as_upsert: true
        }
    });

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
