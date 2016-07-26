module.exports = function (grunt) {

  var srcFiles = [
    'src/header.js',
    'src/constants.js',
    'src/renderers.js',
    'src/main.js',
    'src/footer.js'
  ];

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    concat: {
      dist: {
        src: srcFiles,
        dest: 'jquery.flot.legend.js'
      }
    },
    uglify: {
      options: {
        banner: '/*! <%= pkg.name %> <%= pkg.version %> - built on <%= grunt.template.today("yyyy-mm-dd") %> */\n'
      },
      dist: {
        files: {
          'jquery.flot.legend.min.js': ['<%= concat.dist.dest %>']
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-uglify');

  grunt.loadNpmTasks('grunt-contrib-concat');

  // Default task(s).
  grunt.registerTask('default', ['concat', 'uglify']);

};
