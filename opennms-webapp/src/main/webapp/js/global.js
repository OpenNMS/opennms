//
//  $Id: global.js 3400 2006-06-05 22:01:41Z brozow $
//

function getBaseHref() {
      return document.getElementsByTagName('base')[0].href;
}

function setLocation(url) {
      window.location.href = getBaseHref() + url;
}
