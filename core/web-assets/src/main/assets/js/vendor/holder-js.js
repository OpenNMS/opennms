const load = require('./vendor-loader');

module.exports = load('holder', () => {
  const holder = require('holderjs');

  window['Holder'] = window['holder'] = holder;
  return holder;
});
