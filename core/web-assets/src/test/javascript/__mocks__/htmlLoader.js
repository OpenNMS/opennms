const html = require('html-loader');

module.exports = {
    process(src, filename, config, options) {
        return html(src);
    }
}
