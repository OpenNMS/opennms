
try{var testCommons=new MQObject();testCommons=null;}catch(error){throw"You must include mqcommon.js or toolkit api script prior to mqutils.js.";}
var mqutils=1;if(!Array.prototype.push)Array.prototype.push=function(){var length=Array.push.arguments.length;for(var i=0;i<length;i++)
this[this.length]=Array.push.arguments[i];return this.length;};function mq_ParamExists(varname){var undef;return(varname!==undef);}
function mqGetElementById(fId)
{if(document.getElementById(fId))
{return document.getElementById(fId);}
return null;}
function mqBuildUrl(strQueryData)
{var mqServer=_mqServerPort.replace(/mapquest.com:?\d*/,'mapquest.com');return(_reqPrefix+mqServer+"/oapi/transaction?"+strQueryData+"&key="+_mqKey);}
function mqUrlLimit()
{var maxLength=2048;if(MQA.BrowserInfo.isNS){maxLength=7168;}
return maxLength;}
function mqLimitDisplay()
{var dispLength=2;if(MQA.BrowserInfo.isNS){dispLength=7;}
return dispLength;}
function mqDoRemote(strQueryData,strDivName,strParentTagName,mqObj)
{var parent=document.getElementsByTagName(strParentTagName).item(0);var scMQRemote=mqGetElementById(strDivName);if(scMQRemote)
{parent.removeChild(scMQRemote);}
scMQRemote=document.createElement("script");var srcString=mqBuildUrl(strQueryData);if(strQueryData.substring(0,4)=="http")
{srcString=strQueryData;}
if(srcString.length>mqUrlLimit())
{alert("The request query exceeds the limit ("+mqLimitDisplay()+" Kb) allowed for your browser type. Please reduce the amount of data in the request query!");return;}
scMQRemote.src=srcString;scMQRemote.type="text/javascript";scMQRemote.id=strDivName;parent.appendChild(scMQRemote);}
function mqCreateXMLDocImportNode(ndNewRoot){var newDoc;if(document.implementation.createDocument){var newDoc=document.implementation.createDocument("","",null);try{newDoc.appendChild(newDoc.importNode(ndNewRoot,true))}catch(error){alert(error);alert(ndNewRoot.nodeName);};}else if(window.ActiveXObject){newDoc=new ActiveXObject("Microsoft.XMLDOM");newDoc.async="false";newDoc.loadXML(ndNewRoot.xml);}
return newDoc;}
function mqXmlToStr(xmlDoc){var strXml=new String;var serializer=null;if(xmlDoc==null)return"";if(MQA.BrowserInfo.isNS){serializer=new window.XMLSerializer();strXml=serializer.serializeToString(xmlDoc);}else if(MQA.BrowserInfo.isIE){strXml=xmlDoc.xml;}
if(MQA.BrowserInfo.isSafari)
{serializer=new window.XMLSerializer();strXml=serializer.serializeToString(xmlDoc);strXml=strXml||"";strXml=strXml.replace(/#38;/g,'&');}
return strXml;}
function mqCreateNSManager(namespace){var nsmgr={normalResolver:xmlDoc.createNSResolver(xmlDoc.documentElement),lookupNamespaceURI:function(prefix){switch(prefix){case"_mq":return namespace;default:return this.normalResolver.lookupNamespaceURI(prefix);}}}
return nsmgr;}
function mqGetNode(xmlDoc,strPath){var node;if(MQA.BrowserInfo.isSafari)
{if(!xmlDoc.evaluate)
{var names=new Array();names=strPath.split('/');if(names[names.length-1].indexOf('@')!=-1)
{names.splice(names.length-1,1);}
var tree=xmlDoc.documentElement;var isfound=false;if(names.length==2&&tree.tagName==names[1])
isfound=true;else
{var length=names.length-1;for(var i=1;i<length;i++)
{isfound=false;if(tree.tagName==names[i]&&tree.hasChildNodes())
{var nodes=(tree.hasChildNodes())?tree.childNodes.length:0;for(var j=0;j<nodes;j++)
{if(tree.childNodes[j].tagName==names[i+1])
{tree=tree.childNodes[j];isfound=true;break;}}}
if(names[i+1]&&names[i+1].indexOf('text()')!=-1)
{isfound=true;}
if(names[i+1].indexOf('[')!=-1)
{var index=parseInt(names[i+1].substr(names[i+1].indexOf('[')+1,names[i+1].indexOf(']')-1));names[i+1]=names[i+1].substr(0,names[i+1].indexOf('['));tree=xmlDoc.getElementsByTagName(names[i+1]).item(index-1);isfound=true;}}}
node=(isfound==true)?tree:null;return node;}
else
{node=xmlDoc.evaluate(strPath,xmlDoc,null,9,null);return node.singleNodeValue;}}
else if(MQA.BrowserInfo.isIE){node=xmlDoc.selectSingleNode(strPath);return node;}else if(MQA.BrowserInfo.isNS){node=xmlDoc.evaluate(strPath,xmlDoc,null,9,null);return node.singleNodeValue;}
return null;}
function mqGetNodeText(domNode){var elemText="";if(MQA.BrowserInfo.isIE){elemText=domNode.text;}else if(MQA.BrowserInfo.isNS&&domNode.firstChild){elemText=domNode.firstChild.nodeValue;}
if(MQA.BrowserInfo.isSafari&&domNode.firstChild){elemText=domNode.firstChild.nodeValue;elemText=(elemText?elemText:"");elemText=elemText.replace(/#38;/g,'&');}
return elemText;}
function mqGetXPathNodeText(xmlDoc,strPath){var node;if(MQA.BrowserInfo.isSafari)
{node=mqGetNode(xmlDoc,strPath);var nodeText="";var attribute="";if(strPath.indexOf('@')!=-1)
{attribute=strPath.substr(strPath.indexOf('@')+1,strPath.length);nodeText=node.attributes.getNamedItem(attribute).nodeValue;}
else if(node)
{nodeText=mqGetNodeText(node);}
return nodeText;}
if(MQA.BrowserInfo.isIE){node=xmlDoc.selectSingleNode(strPath);return(node==null?"":node.text);}else if(MQA.BrowserInfo.isNS){try{node=xmlDoc.evaluate(strPath,xmlDoc,null,2,null);}catch(error){alert(strPath);alert(error);}
return node.stringValue;}
return"";}
function mqReplaceNode(xmlDoc,node,strTxt){var ndNewText=xmlDoc.createTextNode(strTxt);if(node.firstChild){return node.replaceChild(ndNewText,node.firstChild);}else{return node.appendChild(ndNewText);}}
function mqReplaceElementNode(xmlDoc,nodeDoc,xpath){var root=xmlDoc.documentElement;var newnode=nodeDoc.documentElement;var oldnode=xmlDoc.getElementsByTagName(xpath).item(0);if(MQA.BrowserInfo.isIE)
node=newnode
else
node=xmlDoc.importNode(newnode,true);if(oldnode){root.replaceChild(node,oldnode);}else{root.appendChild(node);}
return xmlDoc;}
function mqSetNodeText(xmlDoc,strXPath,strTxt){var ndParent=mqGetNode(xmlDoc,strXPath);if(ndParent==null){return null;}
return mqReplaceNode(xmlDoc,ndParent,strTxt);}
function mqTransformXMLFromString(strXml,strXsl,dvParent){var xmlDoc=MQA.createXMLDoc(strXml);var xslDoc=MQA.createXMLDoc(strXsl);var newFragment;if(MQA.BrowserInfo.isNS){var xsltProcessor=new XSLTProcessor();xsltProcessor.importStylesheet(xslDoc);newFragment=xsltProcessor.transformToFragment(xmlDoc,document);dvParent.appendChild(newFragment);}else if(MQA.BrowserInfo.isIE){var newFragment=new ActiveXObject("Msxml2.DOMDocument.5.0");newFragment=xmlDoc.transformNode(xslDoc);dvParent.innerHTML+=newFragment;}}
function mqTransformXMLFromNode(ndXml,strXsl,dvParent){var xslDoc=MQA.createXMLDoc(strXsl);var newFragment;if(MQA.BrowserInfo.isNS){var xsltProcessor=new XSLTProcessor();xsltProcessor.importStylesheet(xslDoc);newFragment=xsltProcessor.transformToFragment(ndXml,document);dvParent.appendChild(newFragment);}else if(MQA.BrowserInfo.isIE){var newFragment=new ActiveXObject("Msxml2.DOMDocument.5.0");newFragment=ndXml.transformNode(xslDoc);dvParent.innerHTML+=newFragment;}}
mqAddEvent(window,"load",alphaBackgrounds);function alphaBackgrounds(){if(navigator.platform=="Win32"&&navigator.appName=="Microsoft Internet Explorer"&&window.attachEvent){var rslt=navigator.appVersion.match(/MSIE (\d+\.\d+)/,'');var itsAllGood=(rslt!=null&&Number(rslt[1])>=5.5);for(i=0;i<document.all.length;i++){var bg=document.all[i].currentStyle.backgroundImage;if(itsAllGood&&bg){if(bg.match(/\.png/i)!=null){var mypng=bg.substring(5,bg.length-2);document.all[i].style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+mypng+"', sizingMethod='scale')";document.all[i].style.backgroundImage="url(/images/background-form-button.gif)";}}}}}
function mqFormatNumber(num,dec){return Math.floor(num*Math.pow(10,dec))/Math.pow(10,dec);}
function mq_display_time(totalTime){var newTime;if(totalTime>3600)
{newTime=totalTime/3600;var result=(" "+Math.floor(newTime)+" hours,");newTime=(totalTime/60)%60;result+=(" "+mqFormatNumber(newTime,2)+" minutes");return result;}
if(totalTime>60)
{newTime=totalTime/60;return(" "+mqFormatNumber(newTime,2)+" minutes");}}
function mq_display_distance(totalDistance){return(" "+mqFormatNumber(totalDistance.value,2)+(totalDistance.units=="mi"?" miles":" kilometers"));}
var isIE5Mac=(navigator.userAgent.indexOf('MSIE 5')!=-1&&navigator.userAgent.indexOf('Mac')!=-1);function mqCreateFormInput(container,id,spanClass,labelTxt,type,name,value,size,maxLength){var div=container.appendChild(document.createElement('div'));div.className='row';var label=div.appendChild(document.createElement('label'));label.htmlFor=id;label.appendChild(document.createTextNode(labelTxt));div.appendChild(document.createElement('br'));var input=document.createElement('input');input.id=id;input.type=type;input.name=name;if(size!=""){input.size=size;}
if(!isNaN(parseInt(maxLength))){input.maxLength=parseInt(maxLength);}
if(value!=""){input.value=value;}
if(spanClass!=""){var span=div.appendChild(document.createElement('span'));span.className=spanClass;span.appendChild(input);}else{div.appendChild(input);}}
function mqCreateInput(container,id,type,name,value,size,maxLength){var input=document.createElement('input');input.id=id;input.type=type;input.name=name;if(size!=""){input.size=size;}
if(!isNaN(parseInt(maxLength))){input.maxLength=parseInt(maxLength);}
if(value!=""){input.value=value;}
container.appendChild(input);}
function mqCreateHiddenInput(form,id,name,value){var input;if(isIE5Mac){input=document.createElement('input type=hidden');}else{input=document.createElement('input');input.type='hidden';}
input.name=name;if(id!=''){input.id=id;}
if(value!=''){input.value=value;}
form.appendChild(input);}
function mqCreateFormSelect(container,id,spanClass,labelTxt,name,elements,node){var div=container.appendChild(document.createElement('div'));div.className='row';var label=div.appendChild(document.createElement('label'));label.htmlFor=id;label.appendChild(document.createTextNode(labelTxt));div.appendChild(document.createElement('br'));if(spanClass!=""){var span=div.appendChild(document.createElement('span'));span.className=spanClass;var select=span.appendChild(document.createElement('select'));}else{var select=div.appendChild(document.createElement('select'));}
select.id=id;select.name=name;length=elements.length;for(x=0;x<length;x++){var option=select.appendChild(document.createElement('option'));eval("option.value = elements[x]."+node);eval("option.appendChild (document.createTextNode (elements[x]."+node+"))");}
return select;}
function mqCreateDiv(container,className,id){var div=container.appendChild(document.createElement('div'));if(className!=""){div.className=className;}
if(id!=""){div.id=id;}
return div;}
function mqCreateA(container,href,title){var a=container.appendChild(document.createElement('a'));a.href=href;if(title!=""){a.title=title;}
return a;}
function mqCreateSpan(container,className,id){var span=container.appendChild(document.createElement('span'));if(className!=""){span.className=className;}
if(id!=""){span.id=id;}
return span;}
function mqCreateImg(container,src,width,height,id,name,alt){var img=container.appendChild(document.createElement('img'));if(src!=""){img.src=src;}
if(!isNaN(parseInt(width))){img.width=parseInt(width);}
if(!isNaN(parseInt(height))){img.height=parseInt(height);}
if(id!=""){img.id=id;}
if(name!=""){img.name=name;}
if(alt!=""){img.alt=alt;}
return img;}
function mqCreateImgDiv(container,src,width,height,id,name,alt){var div=container.appendChild(document.createElement('div'));if(id!=""){div.id=id;}
if(!isNaN(parseInt(width))){div.style.width=parseInt(width)+"px";}
if(!isNaN(parseInt(height))){div.style.height=parseInt(height)+"px";}
if(name!=""){div.name=name;}
if(alt!=""){div.alt=alt;}
return div;}
function mqXMLHttpRequest()
{var request=null;if(window.XMLHttpRequest)
{try
{request=new XMLHttpRequest();}
catch(e)
{request=null;}}
else if(window.ActiveXObject)
{try
{request=new ActiveXObject("Msxml2.XMLHTTP");}
catch(e)
{try
{request=new ActiveXObject("Microsoft.XMLHTTP");}
catch(e)
{request=null;}}}
return request;}
function mqAddEvent(fObj,fEvent,fn)
{if(window.opera&&MQA.BrowserInfo.version<8)
{var r=fObj.attachEvent("on"+fEvent,fn);return r;}
else if(fObj.addEventListener)
{((window.opera)&&(MQA.BrowserInfo.version>=8))?fObj.addEventListener(fEvent,fn,false):fObj.addEventListener(fEvent,fn,true);return true;}
else if(fObj.attachEvent)
{var r=fObj.attachEvent("on"+fEvent,fn);return r;}
else
{fObj["on"+fEvent]=fn;}}
function mqRemoveEvent(fObj,fEvent,fn)
{if(window.opera)
{eval("fObj.on"+fEvent+" = null");}
if(fObj.removeEventListener)
{((window.opera)&&(MQA.BrowserInfo.version>=8))?fObj.removeEventListener(fEvent,fn,false):fObj.removeEventListener(fEvent,fn,true);}
else if(fObj.detachEvent)
{fObj.detachEvent("on"+fEvent,fn);}
else
{fObj["on"+fEvent]=null;}}
function mqGetEventData(evt)
{fEventData=new Object();if(document.addEventListener)
{fEventData.id=evt.target.id;fEventData.type=evt.type;fEventData.element=evt.target;}
else if(window.event)
{fEventData.id=window.event.srcElement.id;fEventData.type=window.event.type;fEventData.element=window.event.srcElement;}
else
{return null;}
return fEventData;}
function mqGetXY(evt)
{xyData=new Object();if(!document.createElement||!document.getElementsByTagName)return;if(!document.createElementNS)
{document.createElementNS=function(ns,elt)
{return document.createElement(elt);}}
if(document.addEventListener&&typeof evt.pageX=="number")
{var Element=evt.target;var CalculatedTotalOffsetLeft=CalculatedTotalOffsetTop=0;while(Element.offsetParent)
{CalculatedTotalOffsetLeft+=Element.offsetLeft;CalculatedTotalOffsetTop+=Element.offsetTop;Element=Element.offsetParent;}
var OffsetXForNS6=evt.pageX-CalculatedTotalOffsetLeft;var OffsetYForNS6=evt.pageY-CalculatedTotalOffsetTop;xyData.elementId=evt.target.id;xyData.elementX=OffsetXForNS6;xyData.elementY=OffsetYForNS6;xyData.pageX=evt.pageX;xyData.pageY=evt.pageY;}
else if(window.event&&typeof window.event.offsetX=="number")
{xyData.elementId=window.event.srcElement.id;xyData.elementX=event.offsetX;xyData.elementY=event.offsetY;xyData.pageX=0;xyData.pageY=0;var element=mqGetElementById(xyData.elementId);while(element)
{xyData.pageX+=element.offsetLeft;xyData.pageY+=element.offsetTop;element=element.offsetParent;}
xyData.pageX+=xyData.elementX;xyData.pageY+=xyData.elementY;}
return xyData;}
function mqGetPDivSize(pMQMapObject)
{size=new MQSize();if(pMQMapObject.parent.style.width.length==0)
pMQMapObject.parent.style.width="800px";if(pMQMapObject.parent.style.height.length==0)
pMQMapObject.parent.style.height="600px";size.setWidth(parseInt(pMQMapObject.parent.style.width)-4);size.setHeight(parseInt(pMQMapObject.parent.style.height)-4);return size;}
function mqSetPDivSize(pMQMapObject,size)
{pMQMapObject.parent.style.width=size.getWidth()+"px";pMQMapObject.parent.style.height=size.getHeight()+"px";}
function mqurlencode(strVal)
{var strEncode;strEncode=strVal.replace(/%/g,"%25");strEncode=strEncode.replace(/&/g,"%26");strEncode=strEncode.replace(/#/g,"%23");strEncode=strEncode.replace(/\//g,"%2F");strEncode=strEncode.replace(/:/g,"%3A");strEncode=strEncode.replace(/;/g,"%3B");strEncode=strEncode.replace(/=/g,"%3D");strEncode=strEncode.replace(/\?/g,"%3F");strEncode=strEncode.replace(/@/g,"%40");strEncode=strEncode.replace(/\$/g,"%24");strEncode=strEncode.replace(/,/g,"%2C");strEncode=strEncode.replace(/\+/g,"%2B");return strEncode;}
function mqGetGuid()
{var org=new Date(2006,0,1);var now=new Date();do{var cur=new Date();}while(cur-now<1);var diff=cur.getTime()-org.getTime();return(Math.ceil(diff));}
function mqPause(numberMillis)
{var now=new Date();var exitTime=now.getTime()+numberMillis;while(true)
{now=new Date();if(now.getTime()>exitTime)
return;}}
var _mqLogStartTime=null;var _mqLogCurTime=null;var _mqLogprevTime=null;function mqLogTime(str)
{if(mqGetElementById("mqTimeLogs"))
{var logtext=mqGetElementById("mqTimeLogs");var mqTimeLogger=new Date();if(_mqLogStartTime==null){logtext.value="Time(ms) Difference\t Message\n";_mqLogStartTime=mqTimeLogger.getTime();_mqLogprevTime=_mqLogStartTime;}
_mqLogCurTime=mqTimeLogger.getTime();var diff=_mqLogCurTime-_mqLogStartTime;var del=_mqLogCurTime-_mqLogprevTime;logtext.value=logtext.value+diff+"\t "+del+"\t\t "+str+"\n";_mqLogprevTime=_mqLogCurTime;}}
function mqResetTimeLogs()
{if(mqGetElementById("mqTimeLogs"))
{var logtext=mqGetElementById("mqTimeLogs");var mqTimeLogger=new Date();logtext.value="Time(ms) Difference\t Message\n";_mqLogStartTime=mqTimeLogger.getTime();_mqLogprevTime=_mqLogStartTime;}}
function mqGetAdvantageResultPath(transaction){var resultsPath;if(transaction=="poiMap")
resultsPath="poiResults";else if(transaction=="locMap")
resultsPath="locations";else if(transaction=="search")
resultsPath="searchResults";return resultsPath;}
function mqGetAdvantageMapPath(transaction){var mapPath;if(transaction=="locMap")
mapPath="/advantage/"+transaction+"/locations/location/map";else
mapPath="/advantage/"+transaction+"/map";return mapPath;}
function mqPrepareMapUrl(strmapUrl){var mapUrl="";mapUrl=strmapUrl.replace(/https?:\/\//,_reqPrefix);mapUrl=mapUrl.replace(/mapquest.com:?\d*/,'mapquest.com');mapUrl=mapUrl.replace(/iwebsys.aol.com:?\d*/,'iwebsys.aol.com');return mapUrl;}
function display(pid,name,value,id,sClass){if(mqGetElementById(pid)){var div=mqGetElementById(pid);var label=div.appendChild(document.createElement('label'));var bb=label.appendChild(document.createElement('b'));bb.appendChild(document.createTextNode(name));div.appendChild(document.createElement('br'));var span=div.appendChild(document.createElement('textarea'));span.className=sClass;span.style.overflow="auto";if(id!=null)span.id=id;span.appendChild(document.createTextNode(value));div.appendChild(document.createElement('br'));div.appendChild(document.createElement('br'));}}