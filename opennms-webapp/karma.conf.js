// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

module.exports = function(config) {
  config.set({
    // base path, that will be used to resolve files and exclude
    basePath: '',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    preprocessors: {
      '**/*.html': ['ng-html2js']
    },

    // list of files / patterns to load in the browser
    files: [
      // Third-party libraries
      'src/main/webapp/lib/jquery/dist/jquery.js',
      'src/main/webapp/lib/underscore/underscore.js',
      'src/main/webapp/lib/bootstrap/dist/js/bootstrap.js',
      'src/main/webapp/lib/angular/angular.js',
      'node_modules/angular-mocks/angular-mocks.js',
      'src/main/webapp/lib/angular-resource/angular-resource.js',
      'src/main/webapp/lib/angular-cookies/angular-cookies.js',
      'src/main/webapp/lib/angular-sanitize/angular-sanitize.js',
      'src/main/webapp/lib/angular-route/angular-route.js',
      'src/main/webapp/lib/angular-animate/angular-animate.js',
      'src/main/webapp/lib/angular-bootstrap/ui-bootstrap-tpls.js',
      'src/main/webapp/lib/angular-loading-bar/build/loading-bar.js',
      'src/main/webapp/lib/angular-growl-v2/build/angular-growl.js',
      // OpenNMS applications (expected to be subdirectories inside 'js')
      'src/main/webapp/js/**/*.js',
      // OpenNMS tests (expected to be subdirectories)
      'src/test/javascript/**/*.js'
    ],

    // list of files / patterns to exclude
    exclude: [
      'src/main/webapp/js/*.js'
    ],

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['PhantomJS'],

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: true
  });
};
