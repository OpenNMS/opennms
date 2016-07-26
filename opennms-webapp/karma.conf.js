var webpackConfig = require('./webpack.config.js');
webpackConfig.entry = {};
webpackConfig.output = {};

module.exports = function (config) {
  config.set({
    basePath: __dirname,

    frameworks: [
      'jasmine'
    ],

    reporters: [
      'progress',
    ],

    files: [
      './src/main/javascript/**/index.js',
      './node_modules/angular-mocks/angular-mocks.js',
      './bower_components/bootstrap/disk/js/bootstrap.js',
      './src/test/javascript/**/*.js'
    ],

    preprocessors: {
      './src/main/javascript/**/index.js': ['webpack'],
      './src/test/javascript/**/*.js': ['webpack']
    },

    browsers: [
      'PhantomJS'
    ],

    singleRun: true,

    logLevel: config.LOG_INFO,

    webpack: webpackConfig
  });
};
