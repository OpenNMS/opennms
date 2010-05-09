
if(typeof(MQA)=='undefined'){MQA=new Object();}
if(typeof(MQA.Common)=='undefined'){MQA.Common=new Object();}
MQA.createXMLDoc=function(strXML){var newDoc;if(document.implementation.createDocument){var parser=new window.DOMParser();if(MQA.BrowserInfo.isSafari)
strXML=strXML.replace(/&/g,'&amp;');newDoc=parser.parseFromString(strXML,"text/xml");}else if(window.ActiveXObject){newDoc=new window.ActiveXObject("Microsoft.XMLDOM");newDoc.async="false";newDoc.loadXML(strXML);}
return newDoc;};mqCreateXMLDoc=MQA.createXMLDoc;MQA.createXMLDocFromNode=function(ndNewRoot){var newDoc;ndNewRoot=ndNewRoot.documentElement;if(document.implementation.createDocument){var newDoc=document.implementation.createDocument("","",null);try{newDoc.appendChild(newDoc.importNode(ndNewRoot,true))}catch(error){alert(error);alert(ndNewRoot.nodeName);};}else if(window.ActiveXObject){newDoc=new ActiveXObject("Microsoft.XMLDOM");newDoc.async="false";newDoc.loadXML(ndNewRoot.xml);}
return newDoc;};mqCreateXMLDocFromNode=MQA.createXMLDocFromNode;MQA.Browser=function(){this.name=null;this.version=null;this.os=null;this.appname=null;this.appVersion=null;this.vMajor=null;this.isNS=null;this.isNS4=null;this.isNS6=null;this.isIE=null;this.isIE4=null;this.isIE5=null;this.isDOM=null;this.isSafari=null;this.platform=null;}
MQBrowser=MQA.Browser;MQA.getBrowserInfo=function()
{var browser=new MQA.Browser();browser.name=browser.version=browser.os="unknown";var userAgent=window.navigator.userAgent.toLowerCase();var appname=window.navigator.appName;var appVersion=window.navigator.appVersion;var browserListArray=new Array("firefox","msie","netscape","opera","safari");var osListArray=new Array("linux","mac","windows","x11");var browserListlength=browserListArray.length;var strPosition="";for(var i=0,n=browserListlength;i<n;i++)
{strPosition=userAgent.indexOf(browserListArray[i])+1;if(strPosition>0)
{browser.name=browserListArray[i];var versionPosition=strPosition+browser.name.length;var incr=((browser.name=="safari")||(userAgent.charAt(versionPosition+4)>0&&userAgent.charAt(versionPosition+4)<9))?5:3;browser.version=userAgent.substring(versionPosition,versionPosition+incr);}}
var osListArrayLength=osListArray.length;for(var j=0,m=osListArrayLength;j<m;j++)
{strPosition=userAgent.indexOf(osListArray[j])+1;if(strPosition>0)
{browser.os=osListArray[j];}}
if(appname=="Netscape")
browser.appname="ns";else if(appname=="Microsoft Internet Explorer")
browser.appname="ie";browser.appVersion=appVersion;browser.vMajor=parseInt(browser.appVersion);browser.isNS=(browser.appname=="ns"&&browser.vMajor>=4);browser.isNS4=(browser.appname=="ns"&&browser.vMajor==4);browser.isNS6=(browser.appname=="ns"&&browser.vMajor==5);browser.isIE=(browser.appname=="ie"&&browser.vMajor>=4);browser.isIE4=(browser.appVersion.indexOf('MSIE 4')>0);browser.isIE5=(browser.appVersion.indexOf('MSIE 5')>0);browser.isDOM=(document.createElement&&document.appendChild&&document.getElementsByTagName)?true:false;browser.isSafari=(browser.name=="safari");if(userAgent.indexOf("win")>-1)
browser.platform="win";else if(userAgent.indexOf("mac")>-1)
browser.platform="mac";else
browser.platform="other";return browser;}
mqGetBrowserInfo=MQA.getBrowserInfo;MQA.BrowserInfo=MQA.getBrowserInfo();MQA.Object=function(){var m_xmlDoc=null;this.getM_XmlDoc=function(){return m_xmlDoc;};this.setM_XmlDoc=function(xmlDoc){m_xmlDoc=xmlDoc;};var m_xpath=null;this.getM_Xpath=function(){return m_xpath;};this.setM_Xpath=function(xpath){m_xpath=xpath;};};MQA.Object.prototype.getClassName=function(){return"MQObject";};MQA.Object.prototype.getObjectVersion=function(){return 0;};MQA.Object.prototype.setProperty=function(strPropName,strPropValue){var strXPathExpression;if(strPropName!==null)strXPathExpression="/"+this.getM_Xpath()+"/"+strPropName;else strXPathExpression="/"+this.getM_Xpath();var ndNewProp=mqSetNodeText(this.getM_XmlDoc(),strXPathExpression,strPropValue);if(ndNewProp===null){var ndNewPropParent=this.getM_XmlDoc().createElement(strPropName);var ndRoot=this.getM_XmlDoc().documentElement.appendChild(ndNewPropParent);ndNewProp=mqSetNodeText(this.getM_XmlDoc(),strXPathExpression,strPropValue);}
return ndNewProp;};MQA.Object.prototype.getProperty=function(strPropName){var strXPathExpression;if(strPropName!==null)strXPathExpression="/"+this.getM_Xpath()+"/"+strPropName;else strXPathExpression="/"+this.getM_Xpath();return mqGetXPathNodeText(this.getM_XmlDoc(),strXPathExpression);};MQA.Object.prototype.copy=function(){var cp=new this.constructor;cp.loadXml(this.saveXml());return cp;};MQA.Object.prototype.internalCopy=function(obj){var strXml="<"+obj.getM_Xpath();if(this.getObjectVersion()>0){strXml=strXml+" Version=\""+this.getObjectVersion()+"\"";}
strXml=strXml+">";var root=this.getM_XmlDoc().documentElement;var nodes=root.childNodes;var maxCount=nodes.length;for(var count=0;count<maxCount;count++){strXml=strXml+mqXmlToStr(nodes[count]);}
strXml=strXml+"</"+obj.getM_Xpath()+">";var cp=new this.constructor;cp.loadXml(strXml);return cp;};MQObject=MQA.Object;MQA.Point=function(param1,param2){this.x=0;this.y=0;this.setM_Xpath("Point");if(arguments.length==1){this.setM_Xpath(param1);}
else if(arguments.length==2){this.x=parseInt(param1);this.y=parseInt(param2);if(isNaN(this.x)||isNaN(this.y))
{throw new Error("1MQPoint constructor called with invalid parameter");}}
else if(arguments.length>2){throw new Error("MQPoint constructor called with "
+arguments.length
+" arguments, but it expects 0, 1, or 2 arguments");}}
MQA.Point.prototype=new MQA.Object();MQA.Point.prototype.constructor=MQA.Point;MQA.Point.prototype.getClassName=function(){return"MQPoint";};MQA.Point.prototype.getObjectVersion=function(){return 0;};MQA.Point.prototype.loadXml=function(strXml){if("undefined"!==typeof(mqutils)){this.setM_XmlDoc(MQA.createXMLDoc(strXml));this.x=this.getProperty("X");this.y=this.getProperty("Y");}};MQA.Point.prototype.saveXml=function(){return"<"+this.getM_Xpath()+"><X>"+this.x+"</X><Y>"+this.y+"</Y></"+this.getM_Xpath()+">";};MQA.Point.prototype.setX=function(x){this.x=parseInt(x);if(isNaN(this.x))
throw new Error("MQPoint.setX called with invalid parameter");};MQA.Point.prototype.getX=function(){return this.x;};MQA.Point.prototype.setY=function(y){this.y=parseInt(y);if(isNaN(this.y))
throw new Error("MQPoint.setY called with invalid parameter");};MQA.Point.prototype.getY=function(){return this.y;};MQA.Point.prototype.setXY=function(x,y){this.x=parseInt(x);this.y=parseInt(y);if(isNaN(this.x)||isNaN(this.y))
throw new Error("MQPoint.setXY called with invalid parameter");};MQA.Point.prototype.valid=function(){if("undefined"!==typeof(mqutils)){return(Math.abs(this.x!=MQCONSTANT.MQPOINT_INVALID)&&Math.abs(this.y!=MQCONSTANT.MQPOINT_INVALID));}
return false;};MQA.Point.prototype.equals=function(pt){if(pt){return(this.x===pt.x&&this.y===pt.y);}
return false;};MQA.Point.prototype.toString=function(){return this.x+","+this.y;};MQPoint=MQA.Point;MQA.LatLng=function(param1,param2){MQA.Object.call(this);this.lat=0.0;this.lng=0.0;this.setM_Xpath("LatLng");if(arguments.length==1){this.setM_Xpath(param1);}
else if(arguments.length==2){this.lat=parseFloat(param1);this.lng=parseFloat(param2);if(isNaN(this.lat)||isNaN(this.lng))
throw new Error("MQA.LatLng constructor called with invalid parameter");}
else if(arguments.length>2){throw new Error("MQA.LatLng constructor called with "
+arguments.length
+" arguments, but it expects 0, 1, or 2 arguments.");}}
MQA.LatLng.prototype=new MQA.Object();MQA.LatLng.prototype.constructor=MQA.LatLng;MQA.LatLng.prototype.getClassName=function(){return"MQLatLng";};MQA.LatLng.prototype.getObjectVersion=function(){return 0;};MQA.LatLng.prototype.loadXml=function(strXml){if("undefined"!==typeof(mqutils)){this.setM_XmlDoc(MQA.createXMLDoc(strXml));this.lat=this.getProperty("Lat");this.lng=this.getProperty("Lng");}};MQA.LatLng.prototype.saveXml=function(){return"<"+this.getM_Xpath()+"><Lat>"+this.lat+"</Lat><Lng>"+this.lng+"</Lng></"+this.getM_Xpath()+">";};MQA.LatLng.prototype.setLatitude=function(fLatitude){this.lat=parseFloat(fLatitude);if(isNaN(this.lat))
throw new Error("MQA.LatLng.setLatitude called with invalid parameter");};MQA.LatLng.prototype.getLatitude=function(){return this.lat;};MQA.LatLng.prototype.setLongitude=function(fLongitude){this.lng=parseFloat(fLongitude);if(isNaN(this.lng))
throw new Error("MQA.LatLng.setLongitude called with invalid parameter");};MQA.LatLng.prototype.getLongitude=function(){return this.lng;};MQA.LatLng.prototype.setLatLng=function(fLatitude,fLongitude){this.lat=parseFloat(fLatitude);this.lng=parseFloat(fLongitude);if(isNaN(this.lat)||isNaN(this.lng))
throw new Error("MQA.LatLng.setLatLng called with invalid parameter");};MQA.LatLng.prototype.arcDistance=function(ll2,lUnits){if("undefined"!==typeof(mqutils)){if(ll2){if(ll2.getClassName()!=="MQLatLng"){alert("failure in arcDistance");throw"failure in arcDistance";}}else{alert("failure in arcDistance");throw"failure in arcDistance";}
if(lUnits){mqIsClass("MQDistanceUnits",lUnits,false);}else{lUnits=new MQDistanceUnits(MQCONSTANT.MQDISTANCEUNITS_MILES);}
if(this.getLatitude()==ll2.getLatitude()&&this.getLongitude()==ll2.getLongitude()){return 0.0;}
var dLon=ll2.getLongitude()-this.getLongitude();var a=MQCONSTANT.MQLATLNG_RADIANS*(90.0-this.getLatitude());var c=MQCONSTANT.MQLATLNG_RADIANS*(90.0-ll2.getLatitude());var cosB=(Math.cos(a)*Math.cos(c))+(Math.sin(a)*Math.sin(c)*Math.cos(MQCONSTANT.MQLATLNG_RADIANS*(dLon)));var radius=(lUnits.getValue()===MQCONSTANT.MQDISTANCEUNITS_MILES)?3963.205:6378.160187;if(cosB<-1.0)
return MQCONSTANT.PI*radius;else if(cosB>=1.0)
return 0;else
return Math.acos(cosB)*radius;}
return-1;};MQA.LatLng.prototype.valid=function(){if("undefined"!==typeof(mqutils)){return(Math.abs(this.getLatitude()-MQCONSTANT.MQLATLNG_INVALID)>MQCONSTANT.MQLATLNG_TOLERANCE&&Math.abs(this.getLongitude()-MQCONSTANT.MQLATLNG_INVALID)>MQCONSTANT.MQLATLNG_TOLERANCE);}
return false;};MQA.LatLng.prototype.equals=function(ll){if(ll!==null){return(this.getLongitude()===ll.getLongitude()&&this.getLatitude()===ll.getLatitude());}
return false;};MQA.LatLng.prototype.toString=function(){return this.lat+","+this.lng;};MQLatLng=MQA.LatLng;MQA.XMLDOC=function(){this.AUTOGEOCODECOVSWITCH=null;this.AUTOROUTECOVSWITCH=null;this.AUTOMAPCOVSWITCH=null;this.DBLAYERQUERY=null;this.LINEPRIMITIVE=null;this.POLYGONPRIMITIVE=null;this.RECTANGLEPRIMITIVE=null;this.ELLIPSEPRIMITIVE=null;this.TEXTPRIMITIVE=null;this.SYMBOLPRIMITIVE=null;this.LATLNG=null;this.POINT=null;this.POINTFEATURE=null;this.LINEFEATURE=null;this.POLYGONFEATURE=null;this.LOCATION=null;this.ADDRESS=null;this.SINGLELINEADDRESS=null;this.GEOADDRESS=null;this.GEOCODEOPTIONS=null;this.MANEUVER=null;this.ROUTEOPTIONS=null;this.ROUTERESULTS=null;this.ROUTEMATRIXRESULTS=null;this.RADIUSSEARCHCRITERIA=null;this.RECTSEARCHCRITERIA=null;this.POLYSEARCHCRITERIA=null;this.CORRIDORSEARCHCRITERIA=null;this.SIGN=null;this.TREKROUTE=null;this.INTCOLLECTION=null;this.DTCOLLECTION=null;this.LATLNGCOLLECTION=null;this.LOCATIONCOLLECTION=null;this.LOCATIONCOLLECTIONCOLLECTION=null;this.MANEUVERCOLLECTION=null;this.SIGNCOLLECTION=null;this.STRINGCOLLECTION=null;this.STRCOLCOLLECTION=null;this.FEATURECOLLECTION=null;this.PRIMITIVECOLLECTION=null;this.POINTCOLLECTION=null;this.TREKROUTECOLLECTION=null;this.FEATURESPECIFIERCOLLECTION=null;this.GEOCODEOPTIONSCOLLECTION=null;this.COVERAGESTYLE=null;this.RECORDSET=null;this.MAPSTATE=null;this.SESSION=null;this.SESSIONID=null;this.DTSTYLE=null;this.DTSTYLEEX=null;this.DTFEATURESTYLEEX=null;this.FEATURESPECIFIER=null;this.BESTFIT=null;this.BESTFITLL=null;this.CENTER=null;this.CENTERLATLNG=null;this.PAN=null;this.ZOOMIN=null;this.ZOOMOUT=null;this.ZOOMTO=null;this.ZOOMTORECT=null;this.ZOOMTORECTLATLNG=null;this.getAUTOGEOCODECOVSWITCH=function(){if(this.AUTOGEOCODECOVSWITCH===null)
this.AUTOGEOCODECOVSWITCH=MQA.createXMLDoc("<AutoGeocodeCovSwitch/>");return this.AUTOGEOCODECOVSWITCH;}
this.getAUTOROUTECOVSWITCH=function(){if(this.AUTOROUTECOVSWITCH===null)
this.AUTOROUTECOVSWITCH=MQA.createXMLDoc("<AutoRouteCovSwitch><Name/><DataVendorCodeUsage>0</DataVendorCodeUsage><DataVendorCodes Count=\"0\"/></AutoRouteCovSwitch>");return this.AUTOROUTECOVSWITCH;}
this.getAUTOMAPCOVSWITCH=function(){if(this.AUTOMAPCOVSWITCH===null)
this.AUTOMAPCOVSWITCH=MQA.createXMLDoc("<AutoMapCovSwitch><Name/><Style/><DataVendorCodeUsage>0</DataVendorCodeUsage><DataVendorCodes Count=\"0\"/><ZoomLevels Count=\"14\"><Item>6000</Item><Item>12000</Item><Item>24000</Item><Item>48000</Item><Item>96000</Item><Item>192000</Item><Item>400000</Item><Item>800000</Item><Item>1600000</Item><Item>3000000</Item><Item>6000000</Item><Item>12000000</Item><Item>24000000</Item><Item>48000000</Item></ZoomLevels></AutoMapCovSwitch>");return this.AUTOMAPCOVSWITCH;}
this.getDBLAYERQUERY=function(){if(this.DBLAYERQUERY===null)
this.DBLAYERQUERY=MQA.createXMLDoc("<DBLayerQuery/>");return this.DBLAYERQUERY;}
this.getLINEPRIMITIVE=function(){if(this.LINEPRIMITIVE===null)
this.LINEPRIMITIVE=MQA.createXMLDoc("<LinePrimitive Version=\"2\"/>");return this.LINEPRIMITIVE;}
this.getPOLYGONPRIMITIVE=function(){if(this.POLYGONPRIMITIVE===null)
this.POLYGONPRIMITIVE=MQA.createXMLDoc("<PolygonPrimitive Version=\"2\"/>");return this.POLYGONPRIMITIVE;}
this.getRECTANGLEPRIMITIVE=function(){if(this.RECTANGLEPRIMITIVE===null)
this.RECTANGLEPRIMITIVE=MQA.createXMLDoc("<RectanglePrimitive Version=\"2\"/>");return this.RECTANGLEPRIMITIVE;}
this.getELLIPSEPRIMITIVE=function(){if(this.ELLIPSEPRIMITIVE===null)
this.ELLIPSEPRIMITIVE=MQA.createXMLDoc("<EllipsePrimitive Version=\"2\"/>");return this.ELLIPSEPRIMITIVE;}
this.getTEXTPRIMITIVE=function(){if(this.TEXTPRIMITIVE===null)
this.TEXTPRIMITIVE=MQA.createXMLDoc("<TextPrimitive Version=\"2\"/>");return this.TEXTPRIMITIVE;}
this.getSYMBOLPRIMITIVE=function(){if(this.SYMBOLPRIMITIVE===null)
this.SYMBOLPRIMITIVE=MQA.createXMLDoc("<SymbolPrimitive Version=\"2\"/>");return this.SYMBOLPRIMITIVE;}
this.getLATLNG=function(){if(this.LATLNG===null)
this.LATLNG=MQA.createXMLDoc("<LatLng/>");return this.LATLNG;}
this.getPOINT=function(){if(this.POINT===null)
this.POINT=MQA.createXMLDoc("<Point/>");return this.POINT;}
this.getPOINTFEATURE=function(){if(this.POINTFEATURE===null)
this.POINTFEATURE=MQA.createXMLDoc("<PointFeature/>");return this.POINTFEATURE;}
this.getLINEFEATURE=function(){if(this.LINEFEATURE===null)
this.LINEFEATURE=MQA.createXMLDoc("<LineFeature/>");return this.LINEFEATURE;}
this.getPOLYGONFEATURE=function(){if(this.POLYGONFEATURE===null)
this.POLYGONFEATURE=MQA.createXMLDoc("<PolygonFeature/>");return this.POLYGONFEATURE;}
this.getLOCATION=function(){if(this.LOCATION===null)
this.LOCATION=MQA.createXMLDoc("<Location/>");return this.LOCATION;}
this.getADDRESS=function(){if(this.ADDRESS===null)
this.ADDRESS=MQA.createXMLDoc("<Address/>");return this.ADDRESS;}
this.getSINGLELINEADDRESS=function(){if(this.SINGLELINEADDRESS===null)
this.SINGLELINEADDRESS=MQA.createXMLDoc("<SingleLineAddress/>");return this.SINGLELINEADDRESS;}
this.getGEOADDRESS=function(){if(this.GEOADDRESS===null)
this.GEOADDRESS=MQA.createXMLDoc("<GeoAddress/>");return this.GEOADDRESS;}
this.getGEOCODEOPTIONS=function(){if(this.GEOCODEOPTIONS===null)
this.GEOCODEOPTIONS=MQA.createXMLDoc("<GeocodeOptions/>");return this.GEOCODEOPTIONS;}
this.getMANEUVER=function(){if(this.MANEUVER===null)
this.MANEUVER=MQA.createXMLDoc("<Maneuver Version=\"1\"><Narrative/><Streets Count=\"0\"/><TurnType>-1</TurnType><Distance>0.0</Distance><Time>-1</Time><Direction>0</Direction><ShapePoints Count=\"0\"/><GEFIDs Count=\"0\"/><Signs  Count=\"0\"/></Maneuver>");return this.MANEUVER;}
this.getROUTEOPTIONS=function(){if(this.ROUTEOPTIONS===null)
this.ROUTEOPTIONS=MQA.createXMLDoc("<RouteOptions Version=\"3\"><RouteType>0</RouteType><NarrativeType>1</NarrativeType><NarrativeDistanceUnitType>0</NarrativeDistanceUnitType><MaxShape>0</MaxShape><MaxGEFID>0</MaxGEFID><Language>English</Language><CoverageName></CoverageName><CovSwitcher><Name></Name><DataVendorCodeUsage>0</DataVendorCodeUsage><DataVendorCodes Count=\"0\"/></CovSwitcher><AvoidAttributeList Count=\"0\"/><AvoidGefIdList Count=\"0\"/><AvoidAbsoluteGefIdList Count=\"0\"/><StateBoundaryDisplay>1</StateBoundaryDisplay><CountryBoundaryDisplay>1</CountryBoundaryDisplay></RouteOptions>");return this.ROUTEOPTIONS;}
this.getROUTERESULTS=function(){if(this.ROUTERESULTS===null)
this.ROUTERESULTS=MQA.createXMLDoc("<RouteResults Version=\"1\"><Locations Count=\"0\"/><CoverageName/><ResultMessages Count=\"0\"/><TrekRoutes Count=\"0\"/></RouteResults>");return this.ROUTERESULTS;}
this.getROUTEMATRIXRESULTS=function(){if(this.ROUTEMATRIXRESULTS===null)
this.ROUTEMATRIXRESULTS=MQA.createXMLDoc("<RouteMatrixResults/>");return this.ROUTEMATRIXRESULTS;}
this.getRADIUSSEARCHCRITERIA=function(){if(this.RADIUSSEARCHCRITERIA===null)
this.RADIUSSEARCHCRITERIA=MQA.createXMLDoc("<RadiusSearchCriteria/>");return this.RADIUSSEARCHCRITERIA;}
this.getRECTSEARCHCRITERIA=function(){if(this.RECTSEARCHCRITERIA===null)
this.RECTSEARCHCRITERIA=MQA.createXMLDoc("<RectSearchCriteria/>");return this.RECTSEARCHCRITERIA;}
this.getPOLYSEARCHCRITERIA=function(){if(this.POLYSEARCHCRITERIA===null)
this.POLYSEARCHCRITERIA=MQA.createXMLDoc("<PolySearchCriteria/>");return this.POLYSEARCHCRITERIA;}
this.getCORRIDORSEARCHCRITERIA=function(){if(this.CORRIDORSEARCHCRITERIA===null)
this.CORRIDORSEARCHCRITERIA=MQA.createXMLDoc("<CorridorSearchCriteria/>");return this.CORRIDORSEARCHCRITERIA;}
this.getSIGN=function(){if(this.SIGN===null)
this.SIGN=MQA.createXMLDoc("<Sign><Type>0</Type><Text></Text><ExtraText></ExtraText><Direction>0</Direction></Sign>");return this.SIGN;}
this.getTREKROUTE=function(){if(this.TREKROUTE===null)
this.TREKROUTE=MQA.createXMLDoc("<TrekRoute><Maneuvers Count=\"0\"/></TrekRoute>");return this.TREKROUTE;}
this.getINTCOLLECTION=function(){if(this.INTCOLLECTION===null)
this.INTCOLLECTION=MQA.createXMLDoc("<IntCollection Count=\"0\"/>");return this.INTCOLLECTION;}
this.getDTCOLLECTION=function(){if(this.DTCOLLECTION===null)
this.DTCOLLECTION=MQA.createXMLDoc("<DTCollection Version=\"1\" Count=\"0\"/>");return this.DTCOLLECTION;}
this.getLATLNGCOLLECTION=function(){if(this.LATLNGCOLLECTION===null)
this.LATLNGCOLLECTION=MQA.createXMLDoc("<LatLngCollection Version=\"1\" Count=\"0\"/>");return this.LATLNGCOLLECTION;}
this.getLOCATIONCOLLECTION=function(){if(this.LOCATIONCOLLECTION===null)
this.LOCATIONCOLLECTION=MQA.createXMLDoc("<LocationCollection Count=\"0\"/>");return this.LOCATIONCOLLECTION;}
this.getLOCATIONCOLLECTIONCOLLECTION=function(){if(this.LOCATIONCOLLECTIONCOLLECTION===null)
this.LOCATIONCOLLECTIONCOLLECTION=MQA.createXMLDoc("<LocationCollectionCollection Count=\"0\"/>");return this.LOCATIONCOLLECTIONCOLLECTION;}
this.getMANEUVERCOLLECTION=function(){if(this.MANEUVERCOLLECTION===null)
this.MANEUVERCOLLECTION=MQA.createXMLDoc("<ManeuverCollection Count=\"0\"/>");return this.MANEUVERCOLLECTION;}
this.getSIGNCOLLECTION=function(){if(this.SIGNCOLLECTION===null)
this.SIGNCOLLECTION=MQA.createXMLDoc("<SignCollection Count=\"0\"/>");return this.SIGNCOLLECTION;}
this.getSTRINGCOLLECTION=function(){if(this.STRINGCOLLECTION===null)
this.STRINGCOLLECTION=MQA.createXMLDoc("<StringCollection Count=\"0\"/>");return this.STRINGCOLLECTION;}
this.getSTRCOLCOLLECTION=function(){if(this.STRCOLCOLLECTION===null)
this.STRCOLCOLLECTION=MQA.createXMLDoc("<StrColCollectin/>");return this.STRCOLCOLLECTION;}
this.getFEATURECOLLECTION=function(){if(this.FEATURECOLLECTION===null)
this.FEATURECOLLECTION=MQA.createXMLDoc("<FeatureCollection Count=\"0\"/>");return this.FEATURECOLLECTION;}
this.getPRIMITIVECOLLECTION=function(){if(this.PRIMITIVECOLLECTION===null)
this.PRIMITIVECOLLECTION=MQA.createXMLDoc("<PrimitiveCollection Count=\"0\"/>");return this.PRIMITIVECOLLECTION;}
this.getPOINTCOLLECTION=function(){if(this.POINTCOLLECTION===null)
this.POINTCOLLECTION=MQA.createXMLDoc("<PointCollection Count=\"0\"/>");return this.POINTCOLLECTION;}
this.getTREKROUTECOLLECTION=function(){if(this.TREKROUTECOLLECTION===null)
this.TREKROUTECOLLECTION=MQA.createXMLDoc("<TrekRouteCollection Count=\"0\"/>");return this.TREKROUTECOLLECTION;}
this.getFEATURESPECIFIERCOLLECTION=function(){if(this.FEATURESPECIFIERCOLLECTION===null)
this.FEATURESPECIFIERCOLLECTION=MQA.createXMLDoc("<FeatureSpecifierCollection Count=\"0\"/>");return this.FEATURESPECIFIERCOLLECTION;}
this.getGEOCODEOPTIONSCOLLECTION=function(){if(this.GEOCODEOPTIONSCOLLECTION===null)
this.GEOCODEOPTIONSCOLLECTION=MQA.createXMLDoc("<GeocodeOptionsCollection Count=\"0\"/>");return this.GEOCODEOPTIONSCOLLECTION;}
this.getCOVERAGESTYLE=function(){if(this.COVERAGESTYLE===null)
this.COVERAGESTYLE=MQA.createXMLDoc("<CoverageStyle/>");return this.COVERAGESTYLE;}
this.getRECORDSET=function(){if(this.RECORDSET===null)
this.RECORDSET=MQA.createXMLDoc("<RecordSet/>");return this.RECORDSET;}
this.getMAPSTATE=function(){if(this.MAPSTATE===null)
this.MAPSTATE=MQA.createXMLDoc("<MapState/>");return this.MAPSTATE;}
this.getSESSION=function(){if(this.SESSION===null)
this.SESSION=MQA.createXMLDoc("<Session Count=\"0\"/>");return this.SESSION;}
this.getSESSIONID=function(){if(this.SESSIONID===null)
this.SESSIONID=MQA.createXMLDoc("<SessionID/>");return this.SESSIONID;}
this.getDTSTYLE=function(){if(this.DTSTYLE===null)
this.DTSTYLE=MQA.createXMLDoc("<DTStyle/>");return this.DTSTYLE;}
this.getDTSTYLEEX=function(){if(this.DTSTYLEEX===null)
this.DTSTYLEEX=MQA.createXMLDoc("<DTStyleEx/>");return this.DTSTYLEEX;}
this.getDTFEATURESTYLEEX=function(){if(this.DTFEATURESTYLEEX===null)
this.DTFEATURESTYLEEX=MQA.createXMLDoc("<DTFeatureStyleEx/>");return this.DTFEATURESTYLEEX;}
this.getFEATURESPECIFIER=function(){if(this.FEATURESPECIFIER===null)
this.FEATURESPECIFIER=MQA.createXMLDoc("<FeatureSpecifier/>");return this.FEATURESPECIFIER;}
this.getBESTFIT=function(){if(this.BESTFIT===null)
this.BESTFIT=MQA.createXMLDoc("<BestFit Version=\"2\"/>");return this.BESTFIT;}
this.getBESTFITLL=function(){if(this.BESTFITLL===null)
this.BESTFITLL=MQA.createXMLDoc("<BestFitLL Version=\"2\"/>");return this.BESTFITLL;}
this.getCENTER=function(){if(this.CENTER===null)
this.CENTER=MQA.createXMLDoc("<Center/>");return this.CENTER;}
this.getCENTERLATLNG=function(){if(this.CENTERLATLNG===null)
this.CENTERLATLNG=MQA.createXMLDoc("<CenterLatLng/>");return this.CENTERLATLNG;}
this.getPAN=function(){if(this.PAN===null)
this.PAN=MQA.createXMLDoc("<Pan/>");return this.PAN;}
this.getZOOMIN=function(){if(this.ZOOMIN===null)
this.ZOOMIN=MQA.createXMLDoc("<ZoomIn/>");return this.ZOOMIN;}
this.getZOOMOUT=function(){if(this.ZOOMOUT===null)
this.ZOOMOUT=MQA.createXMLDoc("<ZoomOut/>");return this.ZOOMOUT;}
this.getZOOMTO=function(){if(this.ZOOMTO===null)
this.ZOOMTO=MQA.createXMLDoc("<ZoomTo/>");return this.ZOOMTO;}
this.getZOOMTORECT=function(){if(this.ZOOMTORECT===null)
this.ZOOMTORECT=MQA.createXMLDoc("<ZoomToRect/>");return this.ZOOMTORECT;}
this.getZOOMTORECTLATLNG=function(){if(this.ZOOMTORECTLATLNG===null)
this.ZOOMTORECTLATLNG=MQA.createXMLDoc("<ZoomToRectLatLng/>");return this.ZOOMTORECTLATLNG;}}
MQXMLDOC=MQA.XMLDOC;MQA.MQXML=new MQA.XMLDOC();MQXML=MQA.MQXML;MQA.ObjectCollection=function(max){MQA.Object.call(this);var m_items=new Array();this.getM_Items=function(){return m_items;};var m_maxItems=(max!==null)?max:-1;var validClassName="MQObject";this.getValidClassName=function(){return validClassName;};this.setValidClassName=function(className){validClassName=className;};this.add=function(obj){if(this.isValidObject(obj)){if(m_maxItems!==-1&&m_items.length===max)return;m_items.push(obj);return m_items.length;}
return;};this.getSize=function(){return m_items.length;};this.get=function(i){return m_items[i];};this.remove=function(iIndex){return m_items.splice(iIndex,1);};this.removeAll=function(){m_items=null;m_items=new Array();};this.contains=function(item){var size=this.getSize();for(var count=0;count<size;count++){if(m_items[count]===item){return true;}}
return false;};this.append=function(collection){if(this.getClassName()===collection.getClassName()){m_items=m_items.concat(collection.getM_Items());}else{alert("Invalid attempt to append "+this.getClassName()+" to "+collection.getClassName()+"!");throw"Invalid attempt to append "+this.getClassName()+" to "+collection.getClassName()+"!";}};this.set=function(i,newO){var oldO=get(i);m_items[i]=newO;return oldO;};this.isValidObject=function(obj){if(obj!==null){if(validClassName==="ALL"){return true;}else if(validClassName==="MQObject"){return true;}else if(validClassName==="String"){return true;}else if(validClassName==="int"){if(isNaN(obj)){return false;}else if(obj===Math.floor(obj)){return true;}}else if(obj.getClassName()===validClassName){return true;}}
return false;};var m_itemXpath="Item";this.getM_itemXpath=function(){return m_itemXpath;};this.setM_itemXpath=function(itemXpath){m_itemXpath=itemXpath;};this.getById=function(strId){try{for(var count=0;count<this.getSize();count++){if(m_items[count].getId()==strId){return m_items[count];}}}catch(Error){}
return null;};this.removeItem=function(item){for(var i=0;i<m_items.length;i++)
{if(m_items[i]==item)
{this.remove(i);i=m_items.length;}}};};MQA.ObjectCollection.prototype=new MQA.Object();MQA.ObjectCollection.prototype.constructor=MQA.ObjectCollection;MQA.ObjectCollection.prototype.getClassName=function(){return"MQObjectCollection";};MQA.ObjectCollection.prototype.getObjectVersion=function(){return 0;};MQA.ObjectCollection.prototype.getAt=function(i){return this.get(i);};MQObjectCollection=MQA.ObjectCollection;MQA.LatLngCollection=function(){MQA.ObjectCollection.call(this,32678);this.setValidClassName("MQLatLng");this.setM_Xpath("LatLngCollection");this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getLATLNGCOLLECTION()));}
MQA.LatLngCollection.prototype=new MQA.ObjectCollection(32678);MQA.LatLngCollection.prototype.constructor=MQA.LatLngCollection;MQA.LatLngCollection.prototype.getClassName=function(){return"MQLatLngCollection";};MQA.LatLngCollection.prototype.getObjectVersion=function(){return 1;};MQA.LatLngCollection.prototype.loadXml=function(strXml){this.removeAll();var xmlDoc=MQA.createXMLDoc(strXml);this.setM_XmlDoc(xmlDoc);if(xmlDoc!==null){this._loadCollection(xmlDoc);}};MQA.LatLngCollection.prototype.loadXmlFromNode=function(xmlNode){this.removeAll();var xmlDoc=mqCreateXMLDocImportNode(xmlNode);this.setM_XmlDoc(xmlDoc);if(xmlDoc!==null){this._loadCollection(xmlDoc);}};MQA.LatLngCollection.prototype._loadCollection=function(xmlDoc){var root=xmlDoc.documentElement;var nodes=root.childNodes;var maxCount=nodes.length;maxCount=(maxCount<32678)?maxCount:32678;var prevLat=0;var prevLng=0;var currentLat=0;var currentLng=0;var latlng=null;if(this.getValidClassName()==="MQLatLng"){for(var count=0;count<maxCount;count++){if(count==0){if(nodes[count].firstChild!==null){currentLat=nodes[count].firstChild.nodeValue/1000000;}
count++;if(nodes[count].firstChild!==null){currentLng=nodes[count].firstChild.nodeValue/1000000;}}else{if(nodes[count].firstChild!==null){currentLat=prevLat+(nodes[count].firstChild.nodeValue/1000000);}
count++;if(nodes[count].firstChild!==null){currentLng=prevLng+(nodes[count].firstChild.nodeValue/1000000);}}
prevLat=currentLat;prevLng=currentLng;latlng=new MQA.LatLng(currentLat,currentLng);this.add(latlng);}}};MQA.LatLngCollection.prototype.saveXml=function(){var strRet="<"+this.getM_Xpath()+" Version=\""+this.getObjectVersion()+"\" Count=\""+this.getSize()+"\">";var size=parseInt(this.getSize());if(size>=1){var nLat=nLng=nPrevLat=nPrevLng=nDeltaLat=nDeltaLng=0;var latLng=null;for(var i=0;i<size;i++){latLng=this.getAt(i);nLat=parseInt(latLng.getLatitude()*1000000);nLng=parseInt(latLng.getLongitude()*1000000);nDeltaLat=nLat-nPrevLat;nDeltaLng=nLng-nPrevLng;strRet+="<Lat>"+nDeltaLat+"</Lat>";strRet+="<Lng>"+nDeltaLng+"</Lng>";nPrevLat=nLat;nPrevLng=nLng;}}
strRet=strRet+"</"+this.getM_Xpath()+">";return strRet;};MQA.LatLngCollection.prototype.generalize=function(dDeviance){var SOrigPoint=function()
{this.pLL=null;this.dSegmentLength=0.0;this.dPriorLength=0.0;};var SDerivedPoint=function()
{this.pLL=null;this.ulOriginalPoint=0;};mqllAnchor=null;var ulAnchor;var i;var dAccumLength=0.0;var nPoints=this.getSize();var pOrigPoints=new Array(nPoints);var pDerivedPoints=new Array(nPoints);var nDerivedPoints=0;if(nPoints<2)
return;for(i=0;i<nPoints;i++)
{pOrigPoints[i]=new SOrigPoint();pDerivedPoints[i]=new SDerivedPoint();pOrigPoints[i].pLL=this.getAt(i);}
for(i=0;i<nPoints-1;i++)
{pOrigPoints[i].dSegmentLength=pOrigPoints[i].pLL.arcDistance(pOrigPoints[(i+1)].pLL);if(i==0)
pOrigPoints[i].dPriorLength=0.0;else
pOrigPoints[i].dPriorLength=dAccumLength;dAccumLength+=pOrigPoints[i].dSegmentLength;}
mqllAnchor=pOrigPoints[0].pLL;ulAnchor=0;pDerivedPoints[0].pLL=mqllAnchor;pDerivedPoints[0].ulOriginalPoint=0;nDerivedPoints=1;for(i=2;i<nPoints;i++)
{if(!this.isEverybodyWithinDeviation(pOrigPoints,ulAnchor,i,dDeviance))
{mqllAnchor=pOrigPoints[(i-1)].pLL;ulAnchor=(i-1);pDerivedPoints[nDerivedPoints].pLL=mqllAnchor;pDerivedPoints[nDerivedPoints].ulOriginalPoint=(i-1);nDerivedPoints++;}}
pDerivedPoints[nDerivedPoints].pLL=pOrigPoints[nPoints-1].pLL;pDerivedPoints[nDerivedPoints].ulOriginalPoint=nPoints-1;nDerivedPoints++;var nPrev=nPoints;var nCount;for(nCount=(nDerivedPoints-1);nCount>=0;nCount--)
{if((nPrev-1)!=pDerivedPoints[nCount].ulOriginalPoint)
{for(var x=(nPrev-1);x>pDerivedPoints[nCount].ulOriginalPoint;x--)
{try
{this.remove(x);}
catch(e)
{}}
nPrev=pDerivedPoints[nCount].ulOriginalPoint;}
else
{nPrev--;}}
pOrigPoints=null;pDerivedPoints=null;};MQA.LatLngCollection.prototype.isEverybodyWithinDeviation=function(pOrigPoints,ulOrigStartPoint,ulOrigEndPoint,dMaxDeviation)
{var dMilesPerLng=0.0;var dMaxDeviationSquared=0.0;var mqllStartPoint=null;var mqllEndPoint=null;var dLineLatMiles=0.0;var dLineLngMiles=0.0;var dLineLengthSquared=0.0;var i;var mqllPoint=null;var dPointLatMiles=0.0;var dPointLngMiles=0.0;var dPointLengthSquared=0.0;var dRatio=0.0;var dNumerator=0.0;var dDenominator=0.0;var dProjectionLengthSquared=0.0;var dDeviationLengthSquared=0.0;dMilesPerLng=MQA.DistanceApproximation.getMilesPerLngDeg(pOrigPoints[ulOrigStartPoint].pLL.getLatitude());dMaxDeviationSquared=dMaxDeviation*dMaxDeviation;mqllStartPoint=pOrigPoints[ulOrigStartPoint].pLL;mqllEndPoint=pOrigPoints[ulOrigEndPoint].pLL;dLineLatMiles=(mqllEndPoint.getLatitude()-mqllStartPoint.getLatitude())*MQA.DistanceApproximation.MILES_PER_LATITUDE;dLineLngMiles=(mqllEndPoint.getLongitude()-mqllStartPoint.getLongitude())*dMilesPerLng;dLineLengthSquared=dLineLatMiles*dLineLatMiles+dLineLngMiles*dLineLngMiles;for(i=ulOrigStartPoint+1;i<ulOrigEndPoint;i++)
{mqllPoint=pOrigPoints[i].pLL;dPointLatMiles=(mqllPoint.getLatitude()-mqllStartPoint.getLatitude())*MQA.DistanceApproximation.MILES_PER_LATITUDE;;dPointLngMiles=(mqllPoint.getLongitude()-mqllStartPoint.getLongitude())*dMilesPerLng;dPointLengthSquared=dPointLatMiles*dPointLatMiles+dPointLngMiles*dPointLngMiles;dNumerator=dLineLatMiles*dPointLatMiles+dLineLngMiles*dPointLngMiles;dDenominator=dLineLatMiles*dLineLatMiles+dLineLngMiles*dLineLngMiles;if(dDenominator==0)
dRatio=0;else
dRatio=dNumerator/dDenominator;dProjectionLengthSquared=dRatio*dRatio*dLineLengthSquared;dDeviationLengthSquared=dPointLengthSquared-dProjectionLengthSquared;if(dDeviationLengthSquared>dMaxDeviationSquared)
return false;}
return true;};MQLatLngCollection=MQA.LatLngCollection;MQA.DistanceApproximation=new function()
{this.m_testLat;this.m_testLng;this.m_mpd;this.m_milesPerLngDeg=new Array(69.170976,69.160441,69.128838,69.076177,69.002475,68.907753,68.792041,68.655373,68.497792,68.319345,68.120088,67.900079,67.659387,67.398085,67.116253,66.813976,66.491346,66.148462,65.785428,65.402355,64.999359,64.576564,64.134098,63.672096,63.190698,62.690052,62.170310,61.631630,61.074176,60.498118,59.903632,59.290899,58.660106,58.011443,57.345111,56.661310,55.960250,55.242144,54.507211,53.755675,52.987764,52.203713,51.403761,50.588151,49.757131,48.910956,48.049882,47.174172,46.284093,45.379915,44.461915,43.530372,42.585570,41.627796,40.657342,39.674504,38.679582,37.672877,36.654698,35.625354,34.585159,33.534429,32.473485,31.402650,30.322249,29.232613,28.134073,27.026963,25.911621,24.788387,23.657602,22.519612,21.374762,20.223401,19.065881,17.902554,16.733774,15.559897,14.381280,13.198283,12.011266,10.820591,9.626619,8.429716,7.230245,6.028572,4.825062,3.620083,2.414002,1.207185,1.000000);this.MILES_PER_LATITUDE=69.170976;this.KILOMETERS_PER_MILE=1.609347;this.getMilesPerLngDeg=function(lat)
{return(Math.abs(lat)<=90.0)?this.m_milesPerLngDeg[parseInt(Math.abs(lat)+0.5)]:69.170976;}};DistanceApproximation=MQA.DistanceApproximation;