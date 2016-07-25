var path = require('path'),
    fs = require('fs'),
    webpack = require('webpack'),
    NgAnnotatePlugin = require('ng-annotate-webpack-plugin'),
    CopyWebpackPlugin = require('copy-webpack-plugin');

var outputDirectory = './target/javascript-dist';

// Execute: export NODE_ENV=production && webpack
//          export NODE_ENV=test && webpack
var debug = process.env.NODE_ENV !== 'production';
var test  = process.env.NODE_ENV === 'test';

// Common Plugins
var plugins = [
  new webpack.ResolverPlugin(
    new webpack.ResolverPlugin.DirectoryDescriptionFilePlugin('bower.json', ['main'])
  ),
  new NgAnnotatePlugin({
    add: true
  })
];

var vendorBundles = {
  angular: [
    'angular',
    'angular-route',
    'angular-cookies',
    'angular-animate',
    'angular-bootstrap',
    'angular-loading-bar',
    'angular-loading-bar/build/loading-bar.css',
    'angular-growl-v2/build/angular-growl',
    'angular-growl-v2/build/angular-growl.css',
    'angular-sanitize'
  ], 
  jquery: [
    'jquery'
  ],
  graphs: [ // TODO Backshift ? 
    'd3',
    'c3/c3',
    'c3/c3.css',
    'cropper',
    'flot/jquery.flot',
    'flot/jquery.flot.canvas',
    'flot/jquery.flot.pie',
    'flot/jquery.flot.time',
    'flot-axislabels/jquery.flot.axislabels',
    'flot-legend/jquery.flot.legend',
    'flot.tooltip/js/jquery.flot.tooltip',
    'flot-saveas/jquery.flot.saveas',
    'flot-datatable/jquery.flot.datatable',
    'flot-navigate/jquery.flot.navigate'
  ],
  underscore: [ // FIXME Can be included as part of Jesse's app, as it is the only place where this is used.
    'underscore'
  ]
};

// Apply ChunkPlugin only when not running Karma
if (!test) {
  var bundles = Object.keys(vendorBundles);
  console.log('Adding bundles ' + bundles + '...');
  plugins.push(
    new webpack.optimize.CommonsChunkPlugin({
      names: Object.keys(vendorBundles),
      minChunks: Infinity,
      filename: '[name].bundle.js'
    })
  );
}

if (!debug) {
  plugins.push(
    new webpack.optimize.DedupePlugin(),
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.optimize.UglifyJsPlugin({ mangle: false, sourcemap: false })
  );
}

var entry = vendorBundles;

var src = path.resolve(__dirname, 'src/main/javascript');
fs.readdirSync(src).filter(function(dir) {
  var p = path.join(src, dir);
  return fs.statSync(p).isDirectory()
      && fs.statSync(path.join(p,'index.js')).isFile();
}).forEach(function(app) {
  console.log('Adding application ' + app + '...');
  entry[app] = [ './src/main/javascript/' + app + '/index.js' ];
});

module.exports = {
  devtool: debug ? 'eval' : null,
  entry: entry,
  output: {
    path: outputDirectory,
    pathinfo: true,
    filename: '[name].bundle.js',
    chunkFilename: '[id].bundle.js'
  },
  resolve: {
    modulesDirectories: ["node_modules", "bower_components"]
  },
  module: {
    noParse: /lie\.js$|\/leveldown\/|min\.js$/,
    preLoaders: [
      { test: /\.js$/, loaders: ['eslint'] }
    ],
    loaders: [
      { test: /\.css$/, loader: 'style!css' },
      { test: /\.scss$/, loader: 'style!css!sass' },
      { test: /\.html$/, loader: 'ngtemplate!html' },
      { test: /\.(eot|otf|ttf|woff2?|svg)$/, loader: 'url?limit=10000' },
      { test: /\.(jpe?g|png|gif)$/i, loader: 'file' }
    ]
  },
  plugins: plugins
};
