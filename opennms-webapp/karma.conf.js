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
      './src/main/javascript/**/*.js',
      './node_modules/angular-mocks/angular-mocks.js',
      './src/test/javascript/**/*.js'
    ],

    preprocessors: {
      './src/main/javascript/**/*.js': ['webpack'],
      './src/test/javascript/**/*.js': ['webpack']
    },

    browsers: [
      'PhantomJS'
    ],

    singleRun: true,

    webpack: webpackConfig
  });
};
