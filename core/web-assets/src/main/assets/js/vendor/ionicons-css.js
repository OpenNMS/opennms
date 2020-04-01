const load = require('./vendor-loader');

module.exports = load('ionicons', () => {
  return require('ionicons/css/ionicons.css');
});
