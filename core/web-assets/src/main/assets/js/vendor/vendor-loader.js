if (!window['onms-module-loader']) {
  window['onms-module-loader'] = {};
}

module.exports = (pluginName, callback) => {
  if (window['onms-module-loader'][pluginName]) {
    console.debug(`onms-module-loader: ${pluginName} already loaded`); // eslint-disable-line no-console
  } else {
    console.info(`onms-module-loader: ${pluginName} is being loaded`); // eslint-disable-line no-console
    window['onms-module-loader'][pluginName] = callback();
  }
  return window['onms-module-loader'][pluginName];
};