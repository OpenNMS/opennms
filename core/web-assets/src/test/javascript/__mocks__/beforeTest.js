var jQuery = require('jquery');
global.jQuery = jQuery;
window.jQuery = jQuery;
global.$ = jQuery;
window.$ = jQuery;

require('manifest');
require('vendor');

function getBaseHref() {
    return 'http://localhost:8980/opennms/';
}

global.getBaseHref = getBaseHref;
window.getBaseHref = getBaseHref;