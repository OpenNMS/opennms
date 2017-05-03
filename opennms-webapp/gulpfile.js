var gulp = require('gulp');
var sass = require('gulp-sass');
var gutil = require('gulp-util');
var rename = require('gulp-rename');
var bower = require('bower');
var bowerFiles = require('main-bower-files');
var CONFIG = require('./gulp.config');

gutil.log('gulp', 'OpenNMS Home: ' + CONFIG.OPENNMS_HOME);

gulp.task('default', ['sass', 'jsp']);

gulp.task('sass', function(done) {
	gulp.src([CONFIG.SASS_SOURCE])
		.pipe(sass())
		.pipe(gulp.dest(CONFIG.OPENNMS_HOME + CONFIG.JETTY_WEBAPP + '/css/'))
		.on('end', done);
});

gulp.task('jsp', function() {
	gulp.src([CONFIG.JSP_SOURCE], { 'base': CONFIG.WEBAPP_SOURCE_BASE })
		.pipe(gulp.dest(CONFIG.OPENNMS_HOME + CONFIG.JETTY_WEBAPP));
});

gulp.task('watch', function() {
	gulp.watch([CONFIG.SASS_SOURCE, CONFIG.JSP_SOURCE], ['sass', 'jsp']);
});

gulp.task('install', function() {
	return bower.commands.install()
		.on('log', function(data) {
			gutil.log('bower', gutil.colors.cyan(data.id), data.message);
		});
});

gulp.task('vendor', ['install'], function() {
  gulp.src(bowerFiles(), { base: CONFIG.WEBAPP_SOURCE_BASE + '/lib' })
    .pipe(rename(function(path) {
      var bower_components = 'bower_components';
      path.dirname = '.' + path.dirname.slice(path.dirname.indexOf(bower_components) + bower_components.length);
      return path;
    }))
    .pipe(gulp.dest(CONFIG.WEBAPP_SOURCE_BASE + CONFIG.LIB_DEST));
});
