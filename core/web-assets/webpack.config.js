/* eslint-env es6 */
/* eslint no-console: 0 */

var webpack = require('webpack');
var path = require('path');
var file = require('file');
var fs = require('fs');

var AfterChunkHashPlugin = require('webpack-after-chunk-hash-plugin');
var AssetsPlugin = require('assets-webpack-plugin');
var BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
var CopyWebpackPlugin = require('copy-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var ProgressBarPlugin = require('progress-bar-webpack-plugin');
var StringReplacePlugin = require('string-replace-webpack-plugin');
var TypedocWebpackPlugin = require('typedoc-webpack-plugin');
var WebpackMd5Hash = require('webpack-md5-hash');
var createVariants = require('parallel-webpack').createVariants;
var clonedeep = require('lodash.clonedeep');

var extractText = new ExtractTextPlugin({
  allChunks: true,
  filename: '[name].css'
  /*filename: '[name]-[contenthash:8].css'*/
});


var pkginfo = require('./package.json');
var opennmsVersion = pkginfo.version;

var argv = require('yargs').argv;
var isProduction = argv.env === 'production';
var distdir = path.join(__dirname, 'target', 'dist', 'assets');
var variants = {
  production: [ false ]
};

if (isProduction) {
  variants.production = [ true, false, 'vaadin' ];
}

console.log('=== running ' + (isProduction? 'production':'development') + ' build of OpenNMS ' + opennmsVersion + ' assets ===');

var styleroot = path.join(__dirname, 'src/main/assets/style');
var jsroot = path.join(__dirname, 'src/main/assets/js');
var moduleroot = path.join(__dirname, 'src/main/assets/modules');

var styleEntries = {};
var appEntries = {};
var vendorEntries = {};
var vaadinEntries = {};
var allEntries = {};

const checkEntry = (type, entry) => {
  if (allEntries[entry]) {
    console.log('ERROR: resource "' + entry + '" has already been found, but another ' + type + ' resource has the same name!');
    process.exit(1);
  }
};

/* Utilities: entry-point name is based on the .js file name */
const scanUtils = (start, dirs, names) => {
  for (const file of names) {
    if (/\.[jt]sx?$/.test(file) && !/3rdparty/.test(file)) {
      const relative = path.relative(__dirname, path.join(start, file));
      const entry = path.basename(file, path.extname(file));
      if (entry === 'index') {
        continue;
      }
      const entryPath = path.join(start,file);
      if (entryPath.indexOf('/vendor/') >= 0) {
        checkEntry('vendor', entry);
        vendorEntries[entry] = entryPath;
      } else {
        checkEntry('lib', entry);
        appEntries[entry] = entryPath;
      }
      if (entryPath.indexOf('/vaadin/') >= 0) {
        vaadinEntries[entry] = entryPath;
      }
      allEntries[entry] = entryPath;
    }
  }
};

/* Apps: entry-point name is based on the directory name containing and index.js */
const scanApps = (start, dirs, names) => {
  for (const file of names) {
    if (/index\.[jt]sx?$/.test(file) && !/3rdparty/.test(file)) {
      const relative = path.relative(__dirname, path.join(start, file));
      // the entry name is the directory containing index.js
      const entry = path.basename(path.dirname(relative));
      const entryPath = path.join(start,file);
      checkEntry('app', entry);
      if (entryPath.indexOf('/vaadin/') >= 0) {
        vaadinEntries[entry] = entryPath;
      }
      allEntries[entry] = entryPath;
      appEntries[entry] = entryPath;
    }
  }
};

const doWalk = (dirname, callback) => {
  [jsroot, moduleroot].forEach((root) => {
    const dir = path.join(root, dirname);
    if (fs.existsSync(dir)) {
      file.walkSync(dir, callback);
    }
  });
};

/* scan themes/css */
file.walkSync(styleroot, function(start, dirs, names) {
  for (var file of names) {
    if (/\.s?css$/.test(file) && !/3rdparty/.test(file)) {
      var entry = path.basename(file, path.extname(file));
      var relative = path.relative(__dirname, path.join(start, file));
      const entryPath = path.join(styleroot, file);
      checkEntry('stylesheet', entry);
      allEntries[entry] = entryPath;
      styleEntries[entry] = entryPath;
    }
  }
});

/* javascript apps (multi-js apps with one entrypoint ("index.js") */
doWalk('apps', scanApps);

/* scan standalone javascript libraries with one entrypoint ("index.js", 3rdparty-excluded) */
doWalk('lib', scanApps);

/* scan vendor roll-ups */
doWalk('vendor', scanUtils);

/* scan vendor roll-ups */
doWalk('vaadin', scanUtils);

/* special case, make sure jQuery gets included as an endpoint */
//vendorEntries['jquery'] = 'jquery';

const dotPrint = (entry) => {
  console.log('* ' + entry);
};

console.log('Stylesheets:');
Object.keys(styleEntries).sort().forEach(dotPrint);

console.log('\nJavaScript entry points:');
Object.keys(appEntries).sort().forEach(dotPrint);

console.log('\nJavaScript vendor/aggregate scripts:');
Object.keys(vendorEntries).sort().forEach(dotPrint);

console.log('');

//process.exit();

var config = {
  entry: allEntries,
  output: {
    path: distdir,
    libraryTarget: 'umd'/*,
    umdNamedDefine: true,
    publicPath: 'assets/'
    */
  },
  target: 'web',
  module: {
    rules: [
      {
        test: require.resolve('angular'),
        use: [{
          loader: 'expose-loader',
          options: 'angular'
        }]
      },
      {
        test: require.resolve('backshift/dist/backshift.onms'),
        use: [{
          loader: 'expose-loader',
          options: 'Backshift'
        }]
      },
      {
        test: require.resolve('bootbox'),
        use: [{
          loader: 'expose-loader',
          options: 'bootbox'
        }]
      },
      {
        test: require.resolve('bootstrap/dist/js/bootstrap'),
        use: [{
          loader: 'imports-loader',
          options: 'define=>false'
        }]
      },
      {
        test: require.resolve('c3'),
        use: [{
          loader: 'expose-loader',
          options: 'c3'
        }]
      },
      {
        test: require.resolve('d3'),
        use: [{
          loader: 'expose-loader',
          options: 'd3'
        }]
      },
      {
        test: require.resolve('holderjs'),
        use: [{
          loader: 'expose-loader',
          options: 'Holder'
        },{
          loader: 'expose-loader',
          options: 'holder'
        },{
          loader: 'script-loader'
        }]
      },
      {
        test: require.resolve('jquery'),
        use: [{
          loader: 'expose-loader',
          options: 'jQuery'
        },{
          loader: 'expose-loader',
          options: '$'
        }]
      },
      {
        test: require.resolve('jquery-ui-treemap'),
        use: [{
          loader: 'imports-loader',
          options: 'define=>false'
        }]
      },
      {
        test: require.resolve('jquery-sparkline/dist/jquery.sparkline'),
        use: [{
          loader: 'imports-loader',
          options: 'define=>false'
        }]
      },
      {
        test: require.resolve('leaflet'),
        use: [{
          loader: 'expose-loader',
          options: 'L'
        }/*,{
          loader: 'script-loader'
        }*/]
      },
      {
        test: /OpenLayers\.js$/,
        use: [{
          loader: 'expose-loader',
          options: 'OpenLayers'
        }]
      },
      {
        test: require.resolve('underscore'),
        use: [{
          loader: 'expose-loader',
          options: '_'
        }]
      },
      {
        test: /\.(gif|png|jpe?g|svg|eot|otf|ttf|woff2?)$/i,
        use: [{
          loader: 'file-loader',
          options: {
            name: '[name].[ext]?v=[hash:8]'
            /* name: '[name]-[hash:8].[ext]' */
          }
        }]
      },
      {
        test: /\.scss$/,
        /* loader: 'style-loader!css-loader!group-css-media-queries-loader!sass-loader' */
        use: extractText.extract({
          fallback: 'style-loader',
          use: [
            {
              loader: StringReplacePlugin.replace({
                replacements: [
                  {
                    pattern: /\/\*! string-replace-webpack-plugin:\s*(.+?)\s*\*\//,
                    replacement: function(match, p1, offset, string) {
                      //console.log('match:',match);
                      //console.log('p1:',p1);
                      return p1;
                    }
                  }
                ]
              })
            },
            {
              loader: 'css-loader',
              options: {
                minimize: true
              }
            },
            {
              loader: 'sass-loader'
            }
          ]
        })
      },
      {
        test: /\.css$/,
        use: extractText.extract({
          fallback: 'style-loader',
          use: 'css-loader'
        })
      },
      {
        /* run tslint on typescript files before rendering */
        enforce: 'pre',
        test: /\.tsx?$/,
        use: [
          {
            loader: 'tslint-loader',
            options: {
              typeCheck: true
            }
          }
        ],
        exclude: [/node_modules/]
      },
      {
        // special case, include and load globally
        test: /\.html$/,
        use: [
          { loader: 'ngtemplate-loader' },
          { loader: 'html-loader' }
        ]
      },
      /*
      {
        // special case, include and load globally
        test: /\/(jquery|c3|d3)\.js$/,
        use: [
          {
            loader: 'script-loader'
          }
        ]
      },
      */
      {
        /* translate javascript to es2015 */
        test: /(\.jsx?)$/,
        use: [
          {
            loader: 'babel-loader',
            query: {
              compact: false
            }
          }
        ],
        // exclude: [/node_modules/, /(jquery|c3|d3)\.js$/]
        exclude: [/node_modules/]
      },
      {
        /* translate typescript to es2015 */
        test: /(\.tsx?)$/,
        use: [
          {
            loader: 'babel-loader',
            query: {
              compact: false
            }
          },
          {
            loader: 'ts-loader'
          }
        ],
        exclude: [/node_modules/]
      }
    ]
  },
  resolve: {
    alias: {
      /* fix a weird issue in angular-ui-bootstrap not finding its modules */
      uib: path.join(__dirname, 'node_modules', 'angular-ui-bootstrap')
    },
    modules: [
      path.resolve('./src/main/assets/modules'),
      path.resolve('./src/main/assets/js'),
      path.resolve('./src/main/assets/style'),
      path.resolve('./node_modules')
    ],
    descriptionFiles: ['package.json', 'bower.json'],
    extensions: ['.tsx', '.ts', '.jsx', '.js']
  },
  plugins: [
    new ProgressBarPlugin(),
    new WebpackMd5Hash(),
    new StringReplacePlugin()
  ]
};

function getExtension(options) {
  if (options.production === 'vaadin') {
    return '.vaadin.js';
  }
  return options.production? '.min.js' : '.js';
}

function getFile(name, options) {
  return name + getExtension(options);
}

function createConfig(options) {
  var myconf = clonedeep(config);
  //myconf.devtool = options.production? 'source-map' : 'eval-source-map';
  myconf.devtool = 'source-map';

  var defs = {
    IS_PRODUCTION: Boolean(options.production),
    'global.OPENNMS_VERSION': JSON.stringify(pkginfo.version)
  };
  if (options.production) {
    defs['global.GENTLY'] = false;
  }

  var debug = Boolean(!options.production);
  var minify = Boolean(options.production);
  var assetJsonFile = 'assets' + (options.production? '.min' : '') + '.json';
  if (options.production === 'vaadin') {
    assetJsonFile = 'assets.vaadin.json';
    myconf.entry = vaadinEntries;
  } else {
    // only do the vaadin files in the vaadin build
    for (const key of Object.keys(vaadinEntries)) {
      delete myconf.entry[key];
    }
  }
  //console.log('Production=' + options.production + ', entry=', myconf.entry);

  myconf.plugins.push(new webpack.DefinePlugin(defs));
  myconf.plugins.push(new webpack.LoaderOptionsPlugin({
    minimize: minify,
    debug: debug
  }));

  if (options.production !== 'vaadin') {
    myconf.plugins.push(new webpack.optimize.CommonsChunkPlugin({
      name: 'vendor',
      filename: getFile('[name]', options),
      minChunks: (module, count) => {
        //console.log('module=',module);
        return module.context
          && module.context.includes('node_modules')
          && !module.context.includes('d3')
          && !module.context.includes('holderjs')
          && !module.context.includes('leaflet');
      }
    }));
    myconf.plugins.push(new webpack.optimize.CommonsChunkPlugin('manifest'));
    //myconf.plugins.push(new webpack.NamedModulesPlugin());
    //myconf.plugins.push(new webpack.optimize.OccurrenceOrderPlugin(true));
  }
  myconf.plugins.push(extractText);

  if (options.production) {
    myconf.plugins.push(new webpack.optimize.UglifyJsPlugin({
      sourceMap: true,
      mangle: {
        except: [ '$element', '$super', '$scope', '$uib', '$', 'jQuery', 'exports', 'require', 'angular', 'c3', 'd3' ]
      },
      minimize: true,
      compress: true
    }));
  } else {
    //myconf.plugins.push(new BundleAnalyzerPlugin());
  }

  myconf.plugins.push(new AssetsPlugin({
    filename: assetJsonFile,
    path: distdir,
    prettyPrint: true,
    includeManifest: true
  }));

  myconf.output.filename = getFile('[name]' /*'[name]-[chunkhash:8]'*/, options);
  myconf.output.chunkFilename = getFile('[name]' /*'[name]-[chunkhash:8]'*/, options);

  myconf.plugins.push(new CopyWebpackPlugin([
    {
      from: 'src/main/assets/static'
    }
  ]));
  /*
  myconf.plugins.push(new AfterChunkHashPlugin({
    manifestJsonName: assetJsonFile
  }));
  */

  console.log('Building variant: production=' + options.production);
  //console.log(myconf);

  return myconf;
}

module.exports = createVariants({}, variants, createConfig);
