var app = require('express')();
var morgan = require('morgan');
var bodyParser = require('body-parser');

/*
It is assumed that credentials are set in ~/.aws/credentials or
AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY have been exported
as environment variables.
*/
var AWS = require('aws-sdk');
var SQS = new AWS.SQS({
    region: 'us-east-1'
});

// Configure app for listening for post requests
app.use(bodyParser.json());
app.use(morgan('dev'));
app.listen(8081)

app.post('/location', function(req, res) {

    // We might want to do some input checking / validation here

    var params = {
        MessageBody: JSON.stringify(req.body),
        QueueUrl: 'https://sqs.us-east-1.amazonaws.com/746215537304/geoAds',
    };

    SQS.sendMessage(params, function(error, data) {
        if (error) console.log(error, error.stack);
        else console.log(data);
    });

    res.json(req.body);
});
