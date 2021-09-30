require('vendor/jquery-ui-js');
require('vendor/bootstrap-js');

import Util from 'lib/util';

console.log('init: global'); // eslint-disable-line no-console

window.getBaseHref = Util.getBaseHref;
window.setLocation = Util.setLocation;
window.toggle = Util.toggle;
