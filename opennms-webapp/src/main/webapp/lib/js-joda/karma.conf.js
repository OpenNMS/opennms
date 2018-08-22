module.exports = function(config) {

    const saucelabsLaunchers = {
        sl_ie: {
            base: 'SauceLabs',
            browserName: 'internet explorer',
            platform: 'Windows 10',
            version: 'latest'
        },
        sl_chrome: {
            base: 'SauceLabs',
            browserName: 'chrome',
            platform: 'Windows 10',
            version: 'latest'
        },
        sl_firefox: {
            base: 'SauceLabs',
            browserName: 'firefox',
            platform: 'Windows 10',
            version: 'latest'
        },
        sl_android_simulator: {
            base: 'SauceLabs',
            browserName: 'chrome',
            device: 'Android Emulator',
            platform: 'Android',
            version: 'latest'
        },
        sl_ios_simulator: {
            base: 'SauceLabs',
            browserName: 'safari',
            device: 'iPhone Simulator',
            platform: 'iOS',
            version: 'latest'
        },
        // these don't work yet :(
        sl_safari: {
            base: 'SauceLabs',
            browserName: 'safari',
            platform: 'OS X 10.11',
            version: 'latest'
        },
        sl_ios: {
            base: 'SauceLabs',
            browserName: 'safari',
            device: 'iPhone 6 Device',
            platform: 'iOS',
            version: 'latest'
        },
    };

    config.set({
        files: [
            {pattern: 'test/karmaWebpackTestEntry.js'}
        ],
        frameworks: [
            'mocha',
            'chai'
        ],
        preprocessors: {
            'test/karmaWebpackTestEntry.js': ['webpack']
        },
        webpack: require('./webpack.config.js'),
        webpackMiddleware: {
            noInfo: true
        },
        sauceLabs: {
            testName: 'js-joda karma Tests',
            recordVideo: false,
            recordScreenshots: false,
            connectOptions: {
                logfile: 'sauce_connect.log'
            }
        },
        customLaunchers: saucelabsLaunchers,
        browserDisconnectTimeout: 10000, // default 2000
        // browserDisconnectTolerance: 1, // default 0
        browserNoActivityTimeout: 4 * 60 * 1000, //default 10000
        captureTimeout: 4 * 60 * 1000, //default 60000
        reporters: ['progress'],
        browsers: ['Chrome', 'Firefox', 'PhantomJS'],
        plugins: ['karma-*'],
        client: {
            mocha: {
                timeout : 6000
            }
        }
    });
};
