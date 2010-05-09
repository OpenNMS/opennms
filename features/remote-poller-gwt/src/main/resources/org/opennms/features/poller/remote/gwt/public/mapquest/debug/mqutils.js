try{
   var testCommons = new MQObject();
   testCommons = null;
}catch(error){
   throw "You must include mqcommon.js or toolkit api script prior to mqutils.js.";
}
var mqutils = 1;
/**
 * @fileoverview This file contains the common utility functions used throughout the js API.
 */
 /**
 * Create a function for array to push onto the stack if the browser does
 * not support it.
 * Older broswer including IE5 don't support the array.push() method.
 * @param Arguments[] arguments Objects to push onto the array.
 * @return Returns the length of the array.
 * @type int
 * @private
 */
if (!Array.prototype.push) Array.prototype.push = function() {
   var length = Array.push.arguments.length;
   for (var i=0; i < length; i++) 
      this[this.length] = Array.push.arguments[i];
   return this.length;   
}; 

function mq_ParamExists (varname) {
    var undef;
    return (varname !== undef);
}

/**
 * =GET ELEMENT BY ID
 */
function mqGetElementById(fId)
{
   if(document.getElementById(fId))
   {
      return document.getElementById(fId);
   }
   return null;
} //mqGetElementById(fId)


/*******************************************************************************/
/* OAPI Functions                                                              */
/*******************************************************************************/
//function used to update a script node used for remote data calls.
//takes a string of the query data and string of the div name
function mqBuildUrl(strQueryData)
{
   var mqServer = _mqServerPort.replace(/mapquest.com:?\d*/,'mapquest.com');
   return (_reqPrefix + mqServer + "/oapi/transaction?" + strQueryData + "&key=" + _mqKey);
}

function mqUrlLimit()
{
   var maxLength = 2048;
   if (MQA.BrowserInfo.isNS) {
      maxLength = 7168;
   }
   return maxLength;
}

function mqLimitDisplay()
{
   var dispLength = 2;
   if (MQA.BrowserInfo.isNS) {
      dispLength = 7;
   }
   return dispLength;
}

function mqDoRemote(strQueryData, strDivName, strParentTagName, mqObj)
{
   var parent      = document.getElementsByTagName(strParentTagName).item(0);
   var scMQRemote   = mqGetElementById(strDivName);
   if(scMQRemote)
   {
      parent.removeChild(scMQRemote);
   }
   scMQRemote    = document.createElement("script");
   var srcString = mqBuildUrl(strQueryData);
   if (strQueryData.substring(0,4) == "http")
   {
      srcString = strQueryData;
   }
   if (srcString.length > mqUrlLimit())
   {
      alert("The request query exceeds the limit ("+mqLimitDisplay()+" Kb) allowed for your browser type. Please reduce the amount of data in the request query!");
      return;
   }
   scMQRemote.src = srcString;
   scMQRemote.type     = "text/javascript";
   scMQRemote.id       = strDivName;
   parent.appendChild(scMQRemote);
}//mqDoRemote()

//crossbrowser wrapper to create an xml document object
//by importing a node
function mqCreateXMLDocImportNode(ndNewRoot) {
   var newDoc;
   if (document.implementation.createDocument){
      var newDoc = document.implementation.createDocument("", "", null);
      try{newDoc.appendChild(newDoc.importNode(ndNewRoot,true))}catch(error){alert(error);alert(ndNewRoot.nodeName);};
   } else if (window.ActiveXObject){
      // Internet Explorer, create a new XML document using ActiveX
      // and use loadXML as a DOM parser.
      newDoc = new ActiveXObject("Microsoft.XMLDOM");
      newDoc.async="false";
      newDoc.loadXML(ndNewRoot.xml);
   }

   return newDoc;
}
//crossbrowser wrapper to convert an xml document object into a string
/**
 * Crossbrowser wrapper to convert an xml document object into a string
 * @param Document xmlDoc Xml Document to be converted into a string
 * @return Returns the converted xmlDoc as a String.
 * @type String
 * @private
 */
function mqXmlToStr(xmlDoc) {
   var strXml = new String;
   var serializer = null;
   if (xmlDoc == null) return "";

   if (MQA.BrowserInfo.isNS) {
      serializer = new window.XMLSerializer();
      strXml = serializer.serializeToString(xmlDoc);
   } else if (MQA.BrowserInfo.isIE) {
      strXml = xmlDoc.xml;      
   }
   
   if(MQA.BrowserInfo.isSafari)   
   {
      serializer = new window.XMLSerializer();
      strXml = serializer.serializeToString(xmlDoc);
      strXml = strXml || "";
      //un-escaping & for safari -start
      strXml = strXml.replace( /#38;/g,'&');
      //escaping & for safari -stop
   }
   return strXml;
}


function mqCreateNSManager(namespace) {

   var nsmgr ={
            normalResolver:
               xmlDoc.createNSResolver(xmlDoc.documentElement),
                  lookupNamespaceURI : function (prefix) {
                     switch (prefix) {
                     case "_mq":
                        return namespace;
                     default:
                           return this.normalResolver.lookupNamespaceURI(prefix);
                  }
                }
            }
    return nsmgr; 
}





//crossbrowser wrapper used to return a node object given a specified xpath expression:
//ie xml doc <location><address></address></location>
//xpath expression /location/address
//will return a pointer to the address node
/** 
 * Crossbrowser wrapper used to return a node object given a specified xpath expression:
 * ie xml doc <location><address></address></location>
 * xpath expression /location/address will return a pointer to the address node
 * @param Document xmlDoc Xml Document to be searched
 * @param String strPath Path to search for in xmlDoc
 * @return Returns the node if found.
 * @type Node
 * @private
  */
function mqGetNode(xmlDoc, strPath) {
   var node;
//Safari adaptation start--
   if (MQA.BrowserInfo.isSafari)
   {
   		if (!xmlDoc.evaluate)
   		{
		        var names = new Array();
		        names = strPath.split('/');
		        if(names[names.length-1].indexOf('@') != -1)
		        {
		            names.splice(names.length-1,1);
		        }
		        var tree = xmlDoc.documentElement;
		        var isfound = false;
		        if( names.length == 2 && tree.tagName == names[1])
		         isfound = true;
		        else
		        {
		           var length = names.length -1 ;
		           for(var i=1; i < length ; i++)
		           {
		            isfound = false;
		               if(tree.tagName == names[i] && tree.hasChildNodes())
		               {
		                 var nodes=(tree.hasChildNodes())?tree.childNodes.length:0;
		                 for(var j=0; j<nodes; j++)
		                 {
		
		                  if(tree.childNodes[j].tagName == names[i+1])
		                  {
		                   tree = tree.childNodes[j];
		                   isfound = true;
		                   break;
		                  }
		                 }
		               }
		
		               if (names[i+1] && names[i+1].indexOf('text()') != -1)
		               {
		                  isfound = true;
		               }
		                  if(names[i + 1].indexOf('[') != -1)
		               {
		                  var index = parseInt(names[i+1].substr(names[i+1].indexOf('[')+1,names[i+1].indexOf(']')-1));
		                  names[i+1] = names[i+1].substr(0,names[i+1].indexOf('['));
		                  tree = xmlDoc.getElementsByTagName(names[i+1]).item(index -1);//-1 for safari
		                  isfound = true;
		               }
		           }
		         }
		   node = (isfound==true)? tree: null;
		   return node;
   		}
   		else
   		{
         node = xmlDoc.evaluate(strPath, xmlDoc, null, 9, null);
         return node.singleNodeValue;
   		}
   }
//Safari adaptation stop--
   else if (MQA.BrowserInfo.isIE) {
      node = xmlDoc.selectSingleNode(strPath);
      return node;
   } else if (MQA.BrowserInfo.isNS) {
         node = xmlDoc.evaluate(strPath, xmlDoc, null, 9, null);
         return node.singleNodeValue;
   }
   return null;
}


//crossbrowser wrapper used to return the text of a given node
//ie loop on all <request> nodes in xml to change text
// document.getElementsByTagName("request");
/**
 * crossbrowser wrapper used to return the text of a given node
 * ie loop on all <request> nodes in xml to change text
 * document.getElementsByTagName("request");
 * @param Node domNode Node to extract the text from 
 * @return Returns the text of the node.
 * @type String
 * @private
 */
function mqGetNodeText(domNode) {
   var elemText = "";
   if (MQA.BrowserInfo.isIE) {
      elemText = domNode.text;
   } else if (MQA.BrowserInfo.isNS && domNode.firstChild) {//without domNode.firstChild condition giving error in Mac(safari).
      elemText = domNode.firstChild.nodeValue;
   }
   if(MQA.BrowserInfo.isSafari && domNode.firstChild) {//without domNode.firstChild condition giving error in Mac(safari).
      elemText = domNode.firstChild.nodeValue;
      // for Safari only elemText was null in some cases
      elemText = (elemText ? elemText:"");
      //un-escaping & for safari -start
      elemText = elemText.replace( /#38;/g,'&');
      //un-escaping & for safari -stop
   }
   return elemText;
}

//crossbrowser wrapper used to return the text of a given node given a specified xpath expression:
//ie xml doc <location><address>122 N Plum St.</address></location>
//xpath expression /location/address
//will return the string "122 N Plum St." or
//ie xml doc <locationCollection count="3">....</locationCollection>
//xpath expression /locationCollection/@count
//will return a value of 3
/**
 * crossbrowser wrapper used to return the text of a given node given a specified xpath expression:
 * ie xml doc <location><address>122 N Plum St.</address></location>
 * xpath expression /location/address
 * will return the string "122 N Plum St." or
 * ie xml doc <locationCollection count="3">....</locationCollection>
 * xpath expression /locationCollection/@count
 * will return a value of 3
 * @param Document xmlDoc Xml Document to be searched
 * @param String strPath XPath where the text is
 * @return Returns the text of the node.
 * @type String
 * @private
 */
function mqGetXPathNodeText(xmlDoc, strPath) {
   var node;
//Safari adaptation start--
   if(MQA.BrowserInfo.isSafari)
   {
   node = mqGetNode(xmlDoc, strPath);
   var nodeText="";
   var attribute="";
   //for now support for @ only
      if(strPath.indexOf('@') != -1)
      {
         attribute = strPath.substr(strPath.indexOf('@')+1, strPath.length);
         nodeText = node.attributes.getNamedItem(attribute).nodeValue ;
      }
      else if(node)
      {
         nodeText = mqGetNodeText(node) ;
      }
      return nodeText;
   }
//Safari adaptation stop--
   if (MQA.BrowserInfo.isIE) {
      node = xmlDoc.selectSingleNode(strPath);
      return (node == null ? "" : node.text);
   } else if (MQA.BrowserInfo.isNS) {
      try{node = xmlDoc.evaluate(strPath, xmlDoc, null, 2, null);} catch(error) {alert(strPath); alert(error);}
      return node.stringValue;
   }
   return "";
}

// used by mqSetNodeText after it finds the node
// using an XPath expr, so other funcs that have
// a nodeList can set text on each node in a loop
/** 
 * Used by mqSetNodeText after it finds the node
 * using an XPath expr, so other funcs that have
 * a nodeList can set text on each node in a loop
 * @param Document xmlDoc Xml Document to be added to
 * @param Node node Xml node to be replaced
 * @param String strTxt text to add in the node
 * @return Returns the node.
 * @type Node
 * @private
 */
function mqReplaceNode(xmlDoc,node,strTxt) {
   var ndNewText = xmlDoc.createTextNode(strTxt);
   if (node.firstChild) {
      return node.replaceChild(ndNewText,node.firstChild);
   } else {
      return node.appendChild(ndNewText);
   }
}

/** 
 * Used by saveXml method to replace a node
 * using an XPath expr
 * @param Document xmlDoc Xml Document to be added to
 * @param Node nodeDoc Xml node to be replaced
 * @param String xpath Xpath to node being replaced
 * @return Returns the node.
 * @type Node
 * @private
 */
function mqReplaceElementNode(xmlDoc, nodeDoc, xpath) {
   var root = xmlDoc.documentElement;
   var newnode = nodeDoc.documentElement;
   var oldnode = xmlDoc.getElementsByTagName(xpath).item(0); 
   if(MQA.BrowserInfo.isIE)
      node = newnode
   else
      node = xmlDoc.importNode(newnode, true);
   if (oldnode){
      root.replaceChild(node, oldnode); 
   } else {
      root.appendChild(node);
   }
   return xmlDoc;
}

//used to replace/add text to an existing node
//ie <location><address></address></location>
//if this function is given the xpath /location/address
//it will add text to the address node if not present or replace
//existing text. It will not add an address node.
/** 
 * Used to replace/add text to an existing node
 * ie <location><address></address></location>
 * if this function is given the xpath /location/address
 * it will add text to the address node if not present or replace
 * existing text. It will not add an address node.
 * @param Document xmlDoc Xml Document to be changed
 * @param String strXPath XPath where the text is to be set
 * @param String strTxt Text to change in the node
 * @return Returns the node.
 * @type Node
 * @private
 */
function mqSetNodeText(xmlDoc,strXPath,strTxt) {
   var ndParent = mqGetNode(xmlDoc,strXPath);
   if (ndParent == null) {
      return null;
   }
   return mqReplaceNode(xmlDoc,ndParent,strTxt);
}

//crossbrowser wrapper used to return xhtml from xml xsl strings.
function mqTransformXMLFromString(strXml,strXsl,dvParent) {
   var xmlDoc = MQA.createXMLDoc(strXml);
   var xslDoc = MQA.createXMLDoc(strXsl);
   var newFragment;

   if (MQA.BrowserInfo.isNS) {
      var xsltProcessor = new XSLTProcessor();
      xsltProcessor.importStylesheet(xslDoc);
      newFragment = xsltProcessor.transformToFragment(xmlDoc, document);
      dvParent.appendChild(newFragment);
   } else if (MQA.BrowserInfo.isIE) {
      var newFragment = new ActiveXObject("Msxml2.DOMDocument.5.0");
      newFragment = xmlDoc.transformNode(xslDoc);
      dvParent.innerHTML += newFragment;
   }
}

//crossbrowser wrapper used to return xhtml from an xml node an xsl string.
function mqTransformXMLFromNode(ndXml,strXsl,dvParent) {
   var xslDoc = MQA.createXMLDoc(strXsl);
   var newFragment;

   if (MQA.BrowserInfo.isNS) {
      var xsltProcessor = new XSLTProcessor();
      xsltProcessor.importStylesheet(xslDoc);
      newFragment = xsltProcessor.transformToFragment(ndXml, document);
      dvParent.appendChild(newFragment);
   } else if (MQA.BrowserInfo.isIE) {
      var newFragment = new ActiveXObject("Msxml2.DOMDocument.5.0");
      newFragment = ndXml.transformNode(xslDoc);
      dvParent.innerHTML += newFragment;
   }
}


/**
 * =PNG/IE FIX
 * Work around to make PNG alpha transparency for IE - Thanks to Drew McLellan @ allinthehead.com
 */
mqAddEvent(window, "load",alphaBackgrounds);

function alphaBackgrounds(){
   if(navigator.platform == "Win32" && navigator.appName == "Microsoft Internet Explorer" && window.attachEvent) {
      var rslt = navigator.appVersion.match(/MSIE (\d+\.\d+)/, '');
      var itsAllGood = (rslt != null && Number(rslt[1]) >= 5.5);
      for (i=0; i<document.all.length; i++){
         var bg = document.all[i].currentStyle.backgroundImage;
         if (itsAllGood && bg){
            if (bg.match(/\.png/i) != null){
               var mypng = bg.substring(5,bg.length-2);
               document.all[i].style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+mypng+"', sizingMethod='scale')";
               document.all[i].style.backgroundImage = "url(/images/background-form-button.gif)";
            }
         }
      }
   }
}

/*******************************************************************************/
/* Following section contains functions for formatting numbers/time/distance   */
/*******************************************************************************/
//function used to format a number (num) to x(dec) decimal places
function mqFormatNumber(num,dec) {
   return Math.floor(num * Math.pow(10,dec))/Math.pow(10,dec);
}

//function displays the time in format x hours, x.xx minutes or x.xx minutes
function mq_display_time(totalTime) {
   var newTime;
   // more than a minute
   if(totalTime > 3600)
   {
      newTime = totalTime/3600;
      var result = (" " + Math.floor(newTime) + " hours,");
      newTime = (totalTime/60)%60;
      result += (" " + mqFormatNumber(newTime,2) + " minutes");
      return result;
   }
   if(totalTime > 60)
   {
      newTime = totalTime/60;
      return (" " + mqFormatNumber(newTime,2) + " minutes");
   }
}

//function used to display distance formated in a div with right alignment.
//outputs miles or kilometers with 2 digits of precision
function mq_display_distance(totalDistance) {
   return (" " + mqFormatNumber(totalDistance.value,2) + (totalDistance.units == "mi" ? " miles" : " kilometers"));
}

/*******************************************************************************/
/* Following section contains functions for adding common DOM elements to tree */
/*******************************************************************************/
var isIE5Mac = (navigator.userAgent.indexOf('MSIE 5') != -1 && navigator.userAgent.indexOf('Mac') != -1);

function mqCreateFormInput (container, id, spanClass, labelTxt, type, name, value, size, maxLength) {
   var div = container.appendChild (document.createElement ('div'));
   div.className = 'row';
   var label = div.appendChild (document.createElement ('label'));
   label.htmlFor = id;
   label.appendChild (document.createTextNode (labelTxt));
   div.appendChild (document.createElement ('br'));
   var input = document.createElement ('input');
   input.id        = id;
   input.type      = type;
   input.name      = name;
   if (size != "") {
      input.size      = size;
   }
   if (!isNaN(parseInt(maxLength))) {
      input.maxLength = parseInt(maxLength);
   }
   if (value != "") {
      input.value = value;
   }

   if (spanClass != "") {
      var span = div.appendChild (document.createElement ('span'));
      span.className = spanClass;
      span.appendChild (input);
   } else {
      div.appendChild (input);
   }
}

function mqCreateInput (container, id, type, name, value, size, maxLength) {
   var input = document.createElement ('input');
   input.id        = id;
   input.type      = type;
   input.name      = name;
   if (size != "") {
      input.size      = size;
   }
   if (!isNaN(parseInt(maxLength))) {
      input.maxLength = parseInt(maxLength);
   }
   if (value != "") {
      input.value = value;
   }
   container.appendChild (input);
}

function mqCreateHiddenInput (form, id, name, value) {
   var input;
   if (isIE5Mac) {
      input = document.createElement ('input type=hidden');
   } else {
      input = document.createElement ('input');
      input.type  = 'hidden';
   }
   input.name  = name;
   if (id != '') {
      input.id    = id;
   }
   if (value != '') {
      input.value = value;
   }
   form.appendChild (input);
}

function mqCreateFormSelect (container, id, spanClass, labelTxt, name, elements, node) {
   var div = container.appendChild (document.createElement ('div'));
   div.className = 'row';
   var label = div.appendChild (document.createElement ('label'));
   label.htmlFor = id;
   label.appendChild (document.createTextNode (labelTxt));
   div.appendChild (document.createElement ('br'));
   if (spanClass != "") {
      var span = div.appendChild (document.createElement ('span'));
      span.className = spanClass;
      var select = span.appendChild (document.createElement ('select'));
   } else {
      var select = div.appendChild (document.createElement ('select'));
   }
   select.id = id;
   select.name = name;
   length = elements.length;
   for (x=0; x < length; x++) {
      var option = select.appendChild (document.createElement ('option'));
      eval ("option.value = elements[x]." + node);
      eval ("option.appendChild (document.createTextNode (elements[x]." + node + "))");
   }
   return select;
}

function mqCreateDiv (container, className, id) {
   var div = container.appendChild (document.createElement ('div'));
   if (className != "") {
      div.className = className;
   }
   if (id != "") {
      div.id = id;
   }
   return div;
}

function mqCreateA (container, href, title) {
   var a = container.appendChild (document.createElement ('a'));
   a.href = href;
   if (title != "") {
      a.title = title;
   }
   return a;
}

function mqCreateSpan (container, className, id) {
   var span = container.appendChild (document.createElement ('span'));

   if (className != "") {
      span.className = className;
   }
   if (id != "") {
      span.id = id;
   }
   return span;
}

function mqCreateImg (container, src, width, height, id, name, alt) {
   var img = container.appendChild (document.createElement ('img'));

   if (src != "") {
      img.src = src;
   }

   if (!isNaN(parseInt(width))) {
      img.width = parseInt(width);
   }
   if (!isNaN(parseInt(height))) {
      img.height = parseInt(height);
   }
   if (id != "") {
      img.id = id;
   }
   if (name != "") {
      img.name = name;
   }
   if (alt != "") {
      img.alt = alt;
   }
   return img;
}

function mqCreateImgDiv (container, src, width, height, id, name, alt) {
    var div = container.appendChild (document.createElement ('div'));
    if (id != "") {
        div.id = id;
    }
    if (!isNaN(parseInt(width))) {
        div.style.width = parseInt(width)+"px";
    }
    if (!isNaN(parseInt(height))) {
        div.style.height = parseInt(height)+"px";
    }
    if (name != "") {
        div.name = name;
    }
    if (alt != "") {
        div.alt = alt;
    }
    return div;
}
/**
 * =HTTP XML REQUEST
 * @makes a XMLHttpRequest standardized for supported browsers
 */
function mqXMLHttpRequest()
{
    var request = null;
    if(window.XMLHttpRequest)
    {   //moz, safari1.2+, opera8
        try
        {
            request = new XMLHttpRequest();
        }
        catch(e)
        {
            request = null;
        }
    }
    else if(window.ActiveXObject)
    {   //ie5.5+
        try
        {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch(e)
        {
            try
            {
                request = new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch(e)
            {
                request = null;
            }
        }
    }
    return request;
} //mqXMLHttpRequest()


/*******************************************************************************/
/*Event Listener Code                                                          */
/*******************************************************************************/
/**
 * =ADD EVENT
 * @attach event listener
 */
function mqAddEvent(fObj, fEvent, fn)
{
    if(window.opera && MQA.BrowserInfo.version < 8)
    {   // opera has bad dynamic event handling
        var r = fObj.attachEvent("on"+fEvent, fn);
        return r;
    }
    else if (fObj.addEventListener)
    {   // moz, w3c
        ((window.opera) && (MQA.BrowserInfo.version >= 8))?fObj.addEventListener(fEvent, fn, false):fObj.addEventListener(fEvent, fn, true);
        return true;
    }
    else if (fObj.attachEvent)
    {   // IE
        var r = fObj.attachEvent("on"+fEvent, fn);
        return r;
    }
    else
    {   //other
        fObj["on" + fEvent] = fn;
    }
}//addEvent()

/**
 * =REMOVE EVENT
 * @detach event listener
 */
function mqRemoveEvent(fObj, fEvent, fn)
{
    if(window.opera)
    {   // opera has bad dynamic event handling
        eval("fObj.on" + fEvent + " = null");
    }
    if(fObj.removeEventListener)
    {   //w3c
        ((window.opera) && (MQA.BrowserInfo.version >= 8))?fObj.removeEventListener(fEvent, fn, false):fObj.removeEventListener(fEvent, fn, true);
    }
    else if(fObj.detachEvent)
    {   //ie
        fObj.detachEvent("on" + fEvent, fn);
    }
    else
    {   //opera and other
        fObj["on" + fEvent] = null;
    }
} //mqRemoveEvent()

/**
 * =GET EVENT DATA
 * @return the id that event is attached to
 */
function mqGetEventData(evt)
{
    fEventData = new Object();
    if(document.addEventListener)
    {
        fEventData.id   = evt.target.id;
        fEventData.type = evt.type;
        fEventData.element = evt.target;
    }
    else if(window.event)
    {
        fEventData.id   = window.event.srcElement.id;
        fEventData.type = window.event.type;
        fEventData.element = window.event.srcElement;
    }
    else
    {
        return null;
    }
    return fEventData;
} //mqGetEventData()


/*******************************************************************************/
/*Document Coordinate calculations                                             */
/*******************************************************************************/
/**
 * GET XY
 * @get the XY coordinates
 * @returns an array containing the event target id, and xy data for page and target
 *
 */
function mqGetXY(evt)
{
    xyData = new Object();
    if(!document.createElement || !document.getElementsByTagName) return;
    if(!document.createElementNS)
    {   // to work in html and xml namespaces
        document.createElementNS = function(ns,elt)
        {
            return document.createElement(elt);
        }
    }
    if(document.addEventListener && typeof evt.pageX == "number")
    {   // Moz and Opera
        var Element                     = evt.target;
        var CalculatedTotalOffsetLeft   = CalculatedTotalOffsetTop = 0;
        while(Element.offsetParent)
        {
            CalculatedTotalOffsetLeft   += Element.offsetLeft;
            CalculatedTotalOffsetTop    += Element.offsetTop;
            Element                      = Element.offsetParent;
        }
        var OffsetXForNS6   = evt.pageX - CalculatedTotalOffsetLeft;
        var OffsetYForNS6   = evt.pageY - CalculatedTotalOffsetTop;
        xyData.elementId    = evt.target.id;
        xyData.elementX     = OffsetXForNS6;
        xyData.elementY     = OffsetYForNS6;
        xyData.pageX        = evt.pageX;
        xyData.pageY        = evt.pageY;
    }
    else if(window.event && typeof window.event.offsetX == "number")
    {   //ie
        xyData.elementId    = window.event.srcElement.id;
        xyData.elementX     = event.offsetX;
        xyData.elementY     = event.offsetY;
        xyData.pageX        = 0;
        xyData.pageY        = 0;
        var element         = mqGetElementById(xyData.elementId);
        while(element)
        {
            xyData.pageX += element.offsetLeft;
            xyData.pageY += element.offsetTop;
            element = element.offsetParent;
        }
        xyData.pageX += xyData.elementX;
        xyData.pageY += xyData.elementY;
    }
    return xyData;
}//mqGetXY()

/**
 * =GET parentDiv SIZE
 * @get height and width of containing div canvas
 */
function mqGetPDivSize( pMQMapObject )
{
    // for openapi
    // 2 pixels padding overall div
    size = new MQSize();

    // Temporary defaults if the user hasn't set div size
    if( pMQMapObject.parent.style.width.length == 0)
      pMQMapObject.parent.style.width = "800px";
    if( pMQMapObject.parent.style.height.length == 0)
      pMQMapObject.parent.style.height = "600px";
    size.setWidth(parseInt(pMQMapObject.parent.style.width) - 4);
    size.setHeight(parseInt(pMQMapObject.parent.style.height) - 4);
    return size;
}

/**
 * =SET parentDiv SIZE
 * @set height and width of containing div canvas
 */
function mqSetPDivSize( pMQMapObject, size )
{
    pMQMapObject.parent.style.width  = size.getWidth() + "px";
    pMQMapObject.parent.style.height = size.getHeight() + "px";
}

/**
*  urlencode used to fix url before server call
*/
function mqurlencode(strVal)
{
   var strEncode;
   strEncode = strVal.replace(/%/g,"%25");
   strEncode = strEncode.replace(/&/g,"%26");
   strEncode = strEncode.replace(/#/g,"%23");
   strEncode = strEncode.replace(/\//g,"%2F");
   strEncode = strEncode.replace(/:/g,"%3A");
   strEncode = strEncode.replace(/;/g,"%3B");
   strEncode = strEncode.replace(/=/g,"%3D");
   strEncode = strEncode.replace(/\?/g,"%3F");
   strEncode = strEncode.replace(/@/g,"%40");
   strEncode = strEncode.replace(/\$/g,"%24");
   strEncode = strEncode.replace(/,/g,"%2C");
   strEncode = strEncode.replace(/\+/g,"%2B");
   return strEncode;
}

function mqGetGuid()
{
   var org = new Date(2006,0,1);
   var now = new Date();
   do {
      var cur = new Date();
   }while(cur - now < 1);

   var diff = cur.getTime()-org.getTime();
   return (Math.ceil(diff));

}
function mqPause(numberMillis)
{
   var now = new Date();
   var exitTime = now.getTime() + numberMillis;
   while (true)
   {
      now = new Date();
   if (now.getTime() > exitTime)
      return;
   }
}
var _mqLogStartTime = null;
var _mqLogCurTime = null;
var _mqLogprevTime = null;
function mqLogTime(str)
{
   if(mqGetElementById("mqTimeLogs"))
   {
     var logtext = mqGetElementById("mqTimeLogs");
     var mqTimeLogger = new Date();
     if( _mqLogStartTime == null) {
     	logtext.value = "Time(ms) Difference\t Message\n";
     	_mqLogStartTime = mqTimeLogger.getTime();
     	_mqLogprevTime = _mqLogStartTime;
     } 
     _mqLogCurTime = mqTimeLogger.getTime();
	 var diff = _mqLogCurTime - _mqLogStartTime;
	 var del = _mqLogCurTime - _mqLogprevTime;
     logtext.value = logtext.value + diff + "\t " + del + "\t\t " + str + "\n";
     _mqLogprevTime = _mqLogCurTime;
   }

}
function mqResetTimeLogs()
{
	if(mqGetElementById("mqTimeLogs"))
    {
	     var logtext = mqGetElementById("mqTimeLogs");
	     var mqTimeLogger = new Date();
	 	 logtext.value = "Time(ms) Difference\t Message\n";
	 	 _mqLogStartTime = mqTimeLogger.getTime();
	 	 _mqLogprevTime = _mqLogStartTime;
    }
}

function mqGetAdvantageResultPath(transaction) {
   var resultsPath ;
   if (transaction == "poiMap")
      resultsPath = "poiResults";
   else if (transaction == "locMap")
      resultsPath = "locations";
   else if (transaction == "search")
      resultsPath = "searchResults";

   return resultsPath;
}

function mqGetAdvantageMapPath(transaction) {
   var mapPath ;
   if (transaction == "locMap")
      mapPath = "/advantage/"+transaction+"/locations/location/map";
   else
      mapPath = "/advantage/"+transaction+"/map";
   return mapPath;
}

function mqPrepareMapUrl(strmapUrl) {
   var mapUrl = "";
   mapUrl = strmapUrl.replace(/https?:\/\//,_reqPrefix);
   mapUrl = mapUrl.replace(/mapquest.com:?\d*/,'mapquest.com');
   mapUrl = mapUrl.replace(/iwebsys.aol.com:?\d*/,'iwebsys.aol.com');
   return mapUrl;
}
  
  
function display(pid, name, value, id , sClass) {

   if (mqGetElementById(pid)) {
      var div = mqGetElementById(pid);
      var label = div.appendChild (document.createElement ('label'));
      var bb = label.appendChild (document.createElement ('b'));
      bb.appendChild (document.createTextNode (name));
      div.appendChild (document.createElement ('br'));
      var span = div.appendChild (document.createElement ('textarea'));
      span.className = sClass;
      span.style.overflow = "auto";
      if(id != null) span.id = id;
      span.appendChild (document.createTextNode (value));
      div.appendChild (document.createElement ('br'));
      div.appendChild (document.createElement ('br'));
   }
}   
