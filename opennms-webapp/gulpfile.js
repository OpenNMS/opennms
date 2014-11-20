var gulp = require('gulp');
var sass = require('gulp-sass');
var gutil = require('gulp-util');
var bower = require('bower');

var paths = {
	'sass': 'src/main/scss/**/*.scss'
};

var opennmsHome = process.env.OPENNMS_HOME || '../target/opennms-15.0.0-SNAPSHOT'
gutil.log('gulp', 'OpenNMS Home: ' + opennmsHome);

gulp.task('default', ['sass']);

gulp.task('sass', function(done) {
	gulp.src([paths.sass])
		.pipe(sass())
		.pipe(gulp.dest(opennmsHome + '/jetty-webapps/opennms/css/'))
		.on('end', done);
});

gulp.task('watch', function() {
	gulp.watch([paths.sass], ['sass']);
});

gulp.task('install', function() {
	return bower.commands.install()
		.on('log', function(data) {
			gutil.log('bower', gutil.colors.cyan(data.id), data.message);
		});
});
