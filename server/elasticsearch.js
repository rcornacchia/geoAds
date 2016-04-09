/* Run this to create the index mapping
   in case we start a new cluster
*/

var elasticSearch = require('elasticsearch');

// Connect to ElasticSearch cluster
var client = new elasticSearch.Client({
    host: '', // create a new one on AWS
    log: 'trace'
});

// create index
client.indices.create({
    index: 'devices',
    body: {
        mappings: {
            device: {
                properties: {
                    location: {
                        type: 'geo_point'
                    }
                }
            }
        }
    }
});
