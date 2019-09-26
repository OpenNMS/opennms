/* eslint-env es6 */
/* eslint no-console: 0 */

var webpack = require('webpack');
var path = require('path');
var file = require('file');
var fs = require('fs');

const SpeedMeasurePlugin = require('speed-measure-webpack-plugin');

var AssetsPlugin = require('assets-webpack-plugin');
var BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
var CopyWebpackPlugin = require('copy-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var StringReplacePlugin = require('string-replace-webpack-plugin');
var UglifyJsPlugin = require('uglifyjs-webpack-plugin');
var createVariants = require('parallel-webpack').createVariants;
var clonedeep = require('lodash.clonedeep');

var extractText = new ExtractTextPlugin({
  allChunks: true,
  filename: '[name].css'
});


var pkginfo = require('./package.json');
var opennmsVersion = pkginfo.version;

var argv = require('yargs').argv;
var isProduction = argv.env === 'production';
var doVaadin = true;
if (typeof argv.vaadin !== 'undefined') {
  doVaadin = argv.vaadin;
}
var distdir = path.join(__dirname, 'target', 'dist', 'assets');
var variants = {
  production: [ false ]
};

const scssUse = [
  {
    loader: 'cache-loader',
    options: {
      cacheDirectory: path.resolve(path.join('target', 'cache-loader'))
    }
  },
  {
    loader: 'css-loader',
    options: {
      modules: 'global'
    }
  },
  {
    loader: 'postcss-loader',
    options: {
      plugins: () => {
        const ret = [ require('autoprefixer')() ];
        if (isProduction) {
          ret.push(require('cssnano')());
        }
        return ret;
      }
    }
  },
  {
    loader: 'sass-loader'
  }
];

if (isProduction) {
  variants.production = [ true, false ];
  if (doVaadin) {
    variants.production.push('vaadin');
  }
}

var getLatest = function getLatest(searchPath, latest = 0, recurse = true) {
  var ret = latest;
//  console.log('getLatest(' + searchPath + ')');
  if (!fs.existsSync(searchPath)) {
    return ret;
  }
  var entries = fs.readdirSync(searchPath);
  for (var entry of entries) {
    var entryPath = path.join(searchPath, entry);
    if (!isProduction && entryPath.match(/\.min\./)) {
      continue;
    }
    var stat = fs.statSync(entryPath);
    if (stat.isDirectory()) {
      if (recurse) {
        ret = Math.max(ret, getLatest(entryPath));
      }
    } else if (fs.existsSync(entryPath)) {
      ret = Math.max(ret, stat.ctimeMs);
    }
  }
  return ret;
};

var srcModified = Math.max(getLatest(__dirname, 0, false), getLatest(path.join(__dirname, 'src')));
var targetModified = getLatest(path.join(__dirname, 'target'));

if (targetModified > srcModified) {
  var checkFile = path.join(__dirname, 'target', 'dist', 'assets', isProduction? 'vendor.min.js' : 'vendor.js');
  if (fs.existsSync(checkFile)) {
    console.log('=== Files are unchanged.  Skipping build.');
    process.exit(0);
  }
}

console.log('=== running ' + (isProduction? 'production':'development') + ' build of OpenNMS ' + opennmsVersion + ' assets ===');

var assetsroot = path.join(__dirname, 'src', 'main', 'assets');
var styleroot = path.join(assetsroot, 'style');
var jsroot = path.join(assetsroot, 'js');
var moduleroot = path.join(assetsroot, 'modules');
var staticroot = path.join(assetsroot, 'static');

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
      if (entryPath.indexOf(path.sep + 'vendor' + path.sep) >= 0) {
        checkEntry('vendor', entry);
        vendorEntries[entry] = entryPath;
      } else {
        checkEntry('lib', entry);
        appEntries[entry] = entryPath;
      }
      if (entryPath.indexOf(path.sep + 'vaadin' + path.sep) >= 0) {
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
      if (entryPath.indexOf(path.sep + 'vaadin' + path.sep) >= 0) {
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
    libraryTarget: 'umd'
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
        }]
      },
      {
        test: /OpenLayers\.js$/,
        include: path.resolve(__dirname, 'src', 'main', 'assets'),
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
          }
        }]
      },
      {
        // special case, vaadin-theme.scss needs string-replace-webpack-plugin to fix up header include stuff
        test: /vaadin-theme\.scss$/,
        include: [ styleroot ],
        use: extractText.extract({
          fallback: 'style-loader',
          use: [ {
            loader: StringReplacePlugin.replace({
              replacements: [
                {
                  pattern: /\/\*! string-replace-webpack-plugin:\s*(.+?)\s*\*\//,
                  replacement: function(match, p1, offset, string) {
                    return p1;
                  }
                }
              ]
            })
          } ].concat(scssUse)
        })
      },
      {
        test: /\.scss$/,
        include: [ styleroot ],
        exclude: [ path.join(styleroot, 'vaadin-theme.scss') ],
        use: extractText.extract({
          fallback: 'style-loader',
          use: scssUse
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
        // special case, include and load globally
        test: /\.html$/,
        include: path.resolve(__dirname, 'src', 'main', 'assets'),
        exclude: [/node_modules/],
        use: [
          { loader: 'ngtemplate-loader' },
          { loader: 'html-loader' }
        ]
      },
      {
        /* translate javascript to es2015 */
        test: /(\.[jt]sx?)$/,
        exclude: [/node_modules/],
        use: [
          /*
          {
            loader: 'cache-loader',
            options: {
              cacheDirectory: path.resolve(path.join('target', 'cache-loader'))
            }
          },
          */
          {
            loader: 'babel-loader',
            options: {
              compact: false,
              cacheDirectory: true,
            }
          }
        ]
      }
    ]
  },
  resolve: {
    modules: [
      moduleroot,
      jsroot,
      styleroot,
      path.resolve(path.join(__dirname, 'node_modules'))
    ],
    descriptionFiles: ['package.json', 'bower.json'],
    extensions: ['.tsx', '.ts', '.jsx', '.js']
  },
  plugins: [
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
  myconf.devtool = 'source-map';

  myconf.mode = options.production? 'production':'development';

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

  myconf.plugins.push(new webpack.DefinePlugin(defs));
  myconf.plugins.push(new webpack.LoaderOptionsPlugin({
    minimize: minify,
    debug: debug
  }));

  if (options.production !== 'vaadin') {
    myconf.module.rules.unshift({
      // run eslint on typescript files before rendering
      enforce: 'pre',
      test: /\.(js|ts)x?$/,
      use: [
        {
          loader: 'eslint-loader',
          options: {
            cache: true,
            failOnError: true
          }
        }
      ],
      exclude: [/node_modules/]
    });

    myconf.optimization = {
      runtimeChunk: {
        name: 'vendor'
      },
      splitChunks: {
        chunks: 'all',
        minSize: 1,
        minChunks: 1,
        name: true,
        cacheGroups: {
          default: false,
          vendors: false,
          runtime: false,
          vendor: {
            name: 'vendor',
            enforce: true,
            test: (module, chunks) => {
              return module.context
              && module.context.includes('node_modules')
              && !module.context.includes('d3')
              && !module.context.includes('holderjs')
              && !module.context.includes('leaflet');
            }
          }
        }
      }
    };
  }
  myconf.plugins.push(extractText);

  if (!myconf.optimization) {
    myconf.optimization = {};
  }

  if (options.production) {
    myconf.optimization.minimize = true;
    if (!myconf.optimization.minimizer) {
      myconf.optimization.minimizer = [];
    } else {
      console.log('minimizer exists:',myconf.optimization.minimizer);
    }
    myconf.optimization.minimizer.push(new UglifyJsPlugin({
      cache: true,
      parallel: true,
      sourceMap: true,
      uglifyOptions: {
        mangle: {
          reserved: [ '$element', '$super', '$scope', '$uib', '$', 'jQuery', 'exports', 'require', 'angular', 'c3', 'd3' ]
        },
        compress: true
      }
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

  myconf.output.filename = getFile('[name]', options);
  myconf.output.chunkFilename = getFile('[name]', options);

  myconf.plugins.push(new CopyWebpackPlugin([
    {
      from: staticroot
    }
  ]));

  console.log('Building variant: production=' + options.production);
  //console.log(myconf);

  const smp = new SpeedMeasurePlugin({
    outputTarget: 'target/smp-' + (options.production === 'vaadin' ? 'vaadin' : myconf.mode) + '.log'
  });
  
  return smp.wrap( myconf );
}

module.exports = createVariants({}, variants, createConfig);
