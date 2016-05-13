var gcm = require('node-gcm'),
    rp = require('request-promise'),
    sender = new gcm.Sender('AIzaSyBCZRYEHTCOWaiYc0EcuYaGglDE0FXjM3g'),
    log = console.log;

// returns a promise
function getTokenById(androidId) {
    var requestParams = {
        uri: 'https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/userData/' +
             androidId + '/_source?_source_include=gcm',
        transform: function(body) {
            return JSON.parse(body).gcm;
        }
    };
    return rp(requestParams);
}

// returns a promise
function getTokensAround(center, radius) {
    var requestParams = {
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
                return hit._source.gcm;
            });
            return results;
        }
    };
    return rp(requestParams);
}

module.exports = {
    /*
     send notification to a single android device
     @adObject -> JSON string
     {title, msg, url, id}
     @androidId -> string
    */
    sendNotification: function(androidId, adObject) {
        var gcmTokenPromise = getTokenById(androidId);
        gcmTokenPromise.then(function(gcmToken) {
            if (gcmToken) {
                var message = new gcm.Message();
                message.addData('title', adObject.title);
                message.addData('msg', adObject.msg);
                message.addData('url', adObject.url);
                console.log(adObject.id);
                message.addData('id', adObject.id);
                sender.send(message, {registrationTokens: [gcmToken]}, function(err,response) {
                    if (err) log("gcm sendNotification failed: " + err);
                    else log(response);
                });
            } else {
                log("sendNotification failed: gcm token not found.");
            }
        });
    },
    /*
    broadcast notification to several android devices
    @location -> {lat, lon} object, center of broadcast area
    @radius ->  int: area in meters around which to broadcast
    */
    broadcastNotification: function(location, radius, adObject) {
        var gcmTokensPromise = getTokensAround(location, radius);
        gcmTokensPromise.then(function(gcmTokens) {
            if (gcmTokens.length > 0) {
                var message = new gcm.Message();
                message.addData('title', adObject.title);
                message.addData('msg', adObject.msg);
                message.addData('url', adObject.url);
                message.addData('id', adObject.id);
                sender.send(message, {registrationTokens: gcmTokens}, function(err, response) {
                    if (err) log("gcm broadcastNotification failed:" + err);
                    else log(response);
                });
            } else {
                log("broadcastNotification failed: no android devices in range.");
            }
        });
    }
};
