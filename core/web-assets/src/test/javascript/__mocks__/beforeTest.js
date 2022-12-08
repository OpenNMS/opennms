require('manifest');
require('vendor');

function getBaseHref() {
    return 'http://localhost:8980/opennms/';
}

global.getBaseHref = getBaseHref;
window.getBaseHref = getBaseHref;