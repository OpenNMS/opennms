// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

module.exports = function(config) {
  config.set({
    // base path, that will be used to resolve files and exclude
    basePath: '../../../../',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    preprocessors: {
      '**/*.html': ['ng-html2js']
    },

    // list of files / patterns to load in the browser
    files: [
      // Third-party libraries
      'http://localhost:8080/opennms/webjars/jquery/2.2.4/dist/jquery.js',
      'http://localhost:8080/opennms/webjars/underscore/1.8.3/underscore.js',
      'http://localhost:8080/opennms/webjars/bootstrap/3.3.1/dist/js/bootstrap.js',
      'http://localhost:8080/opennms/webjars/angular/1.5.8/angular.js',
      'node_modules/angular-mocks/angular-mocks.js',
      'src/main/webapp/lib/angular-resource/angular-resource.js',
      'http://localhost:8080/opennms/webjars/angular-cookies/1.5.8/angular-cookies.js',
      'src/main/webapp/lib/angular-sanitize/angular-sanitize.js',
      'src/main/webapp/lib/angular-route/angular-route.js',
      'http://localhost:8080/opennms/webjars/angular-animate/1.5.8/angular-animate.js',
      'src/main/webapp/lib/angular-bootstrap/ui-bootstrap-tpls.js',
      'src/main/webapp/lib/angular-loading-bar/build/loading-bar.js',
      'http://localhost:8080/opennms/webjars/angular-growl-v2/build/angular-growl.js',
      // OpenNMS applications (expected to be subdirectories inside 'js')
      'src/main/webapp/js/**/*.js',
      // OpenNMS tests (expected to be subdirectories)
      'src/test/javascript/**/*.js'
    ],

    // list of files / patterns to exclude
    exclude: [
      'src/main/webapp/js/*.js'
      'src/test/webapp/js/*.js'
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
