var express = require('express'),
    swig = require('swig'),
    path = require('path'),
    morgan = require('morgan'),
    app = express();


app.use(express.static(path.join(__dirname, '/public')));
app.set('views', path.join(__dirname, 'views'));
app.use(morgan('dev'));
app.engine('html', swig.renderFile);
app.set('view engine', 'html');

app.listen(8081)

app.get('/', function(req, res) {
    res.render('index');
});
