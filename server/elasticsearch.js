/* Run this to create the index mapping
   in case we start a new cluster
*/

var elasticSearch = require('elasticsearch');

// Connect to ElasticSearch cluster
var client = new elasticSearch.Client({
    host: 'search-geo-ads-qpjwkygbkgrhsv6wjg7wyk7scu.us-east-1.es.amazonaws.com', 
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
