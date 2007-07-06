// Copyright 2000 Adobe Systems Incorporated. All rights reserved. Permission
// to use, modify, distribute, and publicly display this file is hereby
// granted. This file is provided "AS-IS" with absolutely no warranties of any
// kind. Portions (C) Netscape Communications 1999.

// If you modify this file, please share your changes with Adobe and other SVG
// developers at http://www.adobe.com/svg/.

// Version 3/23/00

function getBrowser()
{
var agt=navigator.userAgent.toLowerCase();
var v_maj=parseInt(navigator.appVersion);
var v_min=parseFloat(navigator.appVersion);
is_nav=((agt.indexOf('mozilla')!=-1)&&(agt.indexOf('spoofer')==-1)&&
	(agt.indexOf('compatible')==-1)&&(agt.indexOf('opera')==-1)&&
	(agt.indexOf('webtv')==-1));
is_nav3=(is_nav&&(v_maj==3));
is_nav4up=(is_nav&&(v_maj>=4));
is_nav407up=(is_nav&&(v_min>=4.07));
is_nav408up=(is_nav&&(v_min>=4.08));
is_ie=(agt.indexOf("msie")!=-1);
is_ie3=(is_ie&&(v_maj<4));
is_ie4=(is_ie&&(v_maj==4)&&(agt.indexOf("msie 5.0")==-1));
is_ie4up=(is_ie&&(v_maj>=4));
is_ie5=(is_ie&&(v_maj==4)&&(agt.indexOf("msie 5.0")!=-1)); 
is_ie5up=(is_ie&&!is_ie3&&!is_ie4);
is_win=((agt.indexOf("win")!=-1)||(agt.indexOf("16bit")!=-1));
is_win95=((agt.indexOf("win95")!=-1)||(agt.indexOf("windows 95")!=-1));
is_win98=((agt.indexOf("win98")!=-1)||(agt.indexOf("windows 98")!=-1));
is_winnt=((agt.indexOf("winnt")!=-1)||(agt.indexOf("windows nt")!=-1));
is_win32=(is_win95||is_winnt||is_win98||
	((v_maj>=4)&&(navigator.platform=="Win32"))||
	(agt.indexOf("win32")!=-1)||(agt.indexOf("32bit")!=-1));
is_mac=(agt.indexOf("mac")!=-1);
is_macPPC=(is_mac&&((agt.indexOf("ppc")!=-1)||(agt.indexOf("powerpc")!=-1)));
}

function setCookie(name, value, expires, path, domain, secure) {
var curCookie=name+"="+escape(value)+
	((expires)?"; expires="+expires.toGMTString():"")+
	((path)?"; path="+path:"")+
	((domain)?"; domain="+domain:"")+
	((secure)?"; secure":"");
document.cookie=curCookie;
}

// returns null if cookie not found
function getCookie(name) {
var dc=document.cookie;
var prefix=name+"=";
var begin=dc.indexOf("; "+prefix);
if(begin==-1) {
	begin=dc.indexOf(prefix);
	if(begin!=0)
		return null;
	}
else
	begin+=2;
var end=document.cookie.indexOf(";",begin);
if(end==-1)
end=dc.length;
return unescape(dc.substring(begin+prefix.length,end));
}

function deleteCookie(name, path, domain) {
if(getCookie(name))
	document.cookie=name+"="+((path)?"; path="+path:"")+
	((domain)?"; domain="+domain:"")+"; expires=Thu, 01-Jan-70 00:00:01 GMT";
}

function fixDate(date) {
var base=new Date(0);
var skew=base.getTime();
if(skew>0)
	date.setTime(date.getTime()-skew);
}

var svgInstallBase="http://www.adobe.com/svg/viewer/install/";
var svgInstallPage="http://download.adobe.com/pub/adobe/magic/svgviewer/win/6.x/6.0x38363/en/SVGView.exe";
var svgInfoPage="http://www.adobe.com/svg/";
var svgDownloadPage=svgInstallBase;
var checkIntervalDays=30;
var firstSVG=true; // Ask only once per page even without cookies

function getSVGInstallPage() {
return svgInstallPage;
}

function getCheckInterval() {
return checkIntervalDays*24*60*60*1000;
}

// The value of the cookie is '0'. We need some value, but it doesn't matter what.
// We set the cookie for the entire site by specifying the path '/'.
// We could include something from adobe.com and set the cookie for that site.
// This would enable only asking once no matter how many sites a user encounters
// with SVG.
function setSVGCookie() {
if(getCheckInterval()>0) {
	var expires=new Date();
	fixDate(expires); // NN2/Mac bug
	expires.setTime(expires.getTime()+getCheckInterval());
	setCookie('SVGCheck','0',expires,'/')
	}
}

function isSVGPluginInstalled() {
return (navigator.mimeTypes["image/svg"]&&navigator.mimeTypes["image/svg"].enabledPlugin!=null)||
       (navigator.mimeTypes["image/svg-xml"]&&navigator.mimeTypes["image/svg-xml"].enabledPlugin!=null);
}

function checkSVGViewer() {
window.askForSVGViewer=false;
if(window.svgInstalled)
	return;
getBrowser();
if(is_win32 && is_ie4up) {
	window.svgViewerAvailable=true;
	window.svgInstalled=isSVGControlInstalled();
	if(!window.svgInstalled)
		window.askForSVGViewer=true;
	}
else if((is_win32 && is_nav4up) || (is_macPPC && is_nav407up)) {
	window.svgViewerAvailable=true;
	window.svgInstalled=isSVGPluginInstalled();
	if(!window.svgInstalled&&is_nav408up&&navigator.javaEnabled())
		window.askForSVGViewer=true;
	}
else if(is_macPPC && is_ie5up)
	window.svgViewerAvailable=true;
}

function getSVGViewer() {
if(confirm('The Adobe SVG Viewer is not installed. Download now?'))
	location=getSVGInstallPage();
}

function checkAndGetSVGViewer() {
checkSVGViewer();
var svgCookie=getCookie('SVGCheck');
//if(firstSVG&&!svgCookie) {
	if(window.askForSVGViewer) {
		setSVGCookie();
		getSVGViewer();
		}
//	firstSVG=false;
//	}
}

function emitSVG(embedAttrs) {
if(window.svgInstalled)
	document.writeln('<embed '+embedAttrs+'>');
else if(window.askForSVGViewer)	{
	if(navigator.appName=='Netscape') {
		document.writeln('<p>To view this page you need an SVG viewer.');
		document.writeln('<a href="'+getSVGInstallPage()+'">Click here</a> for more information.</p>');
		}
	else
		document.writeln('<embed '+embedAttrs+' pluginspage="'+getSVGInstallPage()+'">');
	}
else if(window.svgViewerAvailable)
	document.writeln('<embed '+embedAttrs+' pluginspage="'+svgDownloadPage+'">');
else {
	document.writeln('<p>To view this page you need an SVG viewer. There is currently no Adobe SVG ');
	document.writeln('Viewer available for your browser. ');
	document.writeln('<a href="'+svgInfoPage+'">Click here</a> for more information.</p>');
	}
}
