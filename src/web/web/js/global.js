//
//  $Id$
//

function getBaseHref() {
      return document.getElementsByTagName('base')[0].href;
}

function setLocation(url) {
      top.location.href = getBaseHref() + url;
}
