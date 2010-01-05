var appContext = "/opennms/";

function spaceTrans(stringIn) {
	var result = ""
	for (var i = 0; i < stringIn.length; i++) {
		if (stringIn.charAt(i) == " ") {
			result += "_x0020_"
		} else {
			result += stringIn.charAt(i)
		}
	}
	return result;
}

function escapeSpecialChars(strInput)
{
	strInput=strInput.replace(/&amp;/g,"&");  
	strInput=strInput.replace(/&lt;/g,"<");	
	strInput=strInput.replace(/&gt;/g,">");	
	strInput=strInput.replace(/&quot;/g,'"');	
	strInput=strInput.replace(/&apos;/g,"'");	
	  
	return(strInput);
}

function replaceSpecialChars(myString) {
		if(myString!=null){
			for (i=161;i<256;i++) {
				re = new RegExp("&#"+i+";","g");
				myString = myString.replace(re,String.fromCharCode(i));
			}
		}
		return myString;
}

function assArrayPopulate(arrayKeys,arrayValues) {
	var returnArray = new Array();
	if (arrayKeys.length != arrayValues.length) {
		alert("Error: arrays do not have same length");
	}
	else {
		for (i=0;i<arrayKeys.length;i++) {
			returnArray[arrayKeys[i]] = arrayValues[i];
		}
	}
	return returnArray;
}

function httpRequest()
{
    var xmlhttp;
    try {
        xmlhttp = new XMLHttpRequest();
    } catch (e) {
        xmlhttp = false;
    }
    if (!xmlhttp) {
        try {
            xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
            if (!xmlhttp) {
                try {
                    xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
                } catch (e) {
                    xmlhttp = false;
                }
            }
        } catch (e) {
            xmlhttp = false;
        }
    }
    return xmlhttp;
}

function parseUri(sourceUri){
    var uriPartNames = ["source","protocol","authority","domain","port","path","directoryPath","fileName","query","anchor"];
    var uriParts = new RegExp("^(?:([^:/?#.]+):)?(?://)?(([^:/?#]*)(?::(\\d*))?)?((/(?:[^?#](?![^?#/]*\\.[^?#/.]+(?:[\\?#]|$)))*/?)?([^?#/]*))?(?:\\?([^#]*))?(?:#(.*))?").exec(sourceUri);
    var uri = {};
    
    for(var i = 0; i < 10; i++){
        uri[uriPartNames[i]] = (uriParts[i] ? uriParts[i] : "");
    }
    if(uri.directoryPath.length > 0){
        uri.directoryPath = uri.directoryPath.replace(/\/?$/, "/");
    }
    
    return uri;
}

function getMapRequest(url,data,handler,type,enc){
	postMapRequestAll(url,"GET", "",handler,type,enc, true);
}

function postMapRequest(url,data,handler,type,enc){
	postMapRequestAll(url,"POST", data,handler,type,enc, true);
}
function postMapRequestAll(url,method,data,handler,type,enc,async){
	   var uriObj = parseUri(document.URL);
	   var appdomain = uriObj.protocol+"://"+uriObj.authority;
	   var instantiationSuccess = true;
       var xmlhttp = new httpRequest();
       if (xmlhttp) {
           try{
	            xmlhttp.open(method, appContext+url, async);
	            if (method == 'POST') {
	            	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	            	xmlhttp.setRequestHeader("Content-length", data.length);
	            	xmlhttp.setRequestHeader("Connection", "close");
	            }
	            xmlhttp.onreadystatechange = function() {
	                if (xmlhttp.readyState == 4) {
	                    handler({status:xmlhttp.status, content:xmlhttp.responseText, contentType:xmlhttp.getResponseHeader("Content-Type")});
	                }
	         	}
             	xmlhttp.send(data);
           }catch(e){
           		instantiationSuccess = false;
           }	
       	}
   		
   		if(instantiationSuccess==false){
	   		alert("Sorry, your browser/SVG viewer does not support XMLHttpRequest/ActiveXObject!");
   		}
}

//functions for matching ip with range in input
function ipmatch(ip, ipLike){
	var ottectsLike = ipLike.split(".");
	var ottectsIp = ip.split(".");
	return (ottectMatch(parseInt(ottectsIp[0]),ottectsLike[0]) && ottectMatch(parseInt(ottectsIp[1]), ottectsLike[1]) && ottectMatch(parseInt(ottectsIp[2]),ottectsLike[2]) && ottectMatch(parseInt(ottectsIp[3]),ottectsLike[3]) )

}

function ottectMatch(ott, ottLike){
	//alert(ott+" "+ ottLike);
	try{
	if(ottLike=="*"){
		if(ott<=255 && ott>=0)

		 	return true;

		return false;
	}
	if(ottLike.indexOf("-")>=0){
		var range = ottLike.split("-")
		var start=parseInt(range[0]);
		var end=parseInt(range[1]);
		if(start>end  || start>255 || end >255 ||start<0 || end<0)
			 return false;
		if(ott>=start && ott<=end)
			return true;
		return false;
	}
	if(ott==parseInt(ottLike))
		return true;
	return false;
	}catch(e){
		return false;
	}
}

function isValidOttect(ott){
	if(ott=="*"){
		return true;
	}
	if(ott.indexOf('-')>=0){
		var ottRange = ott.split('-');
		if(ottRange.length>2)
			return false;
		var start=parseInt(ottRange[0]);
		var end=parseInt(ottRange[1]);
		if(start<=end  && start<=255 && end <=255 && start>=0 && end>=0)
			 return true;
		
	}
	if(parseInt(ott)>=0 && parseInt(ott)<=255)
		return true;
	return false;
}

function isValidRange(range){
	var ottects = range.split(".");
	if(ottects.length!=4){
		return false;
	}
	return (isValidOttect(ottects[0]) && isValidOttect(ottects[1]) && isValidOttect(ottects[2]) && isValidOttect(ottects[3]));
}

function trimAll(sString)
{
	while (sString.substring(0,1) == ' ')
	{
		sString = sString.substring(1, sString.length);
	}
	while (sString.substring(sString.length-1, sString.length) == ' ')
	{
		sString = sString.substring(0,sString.length-1);
	}
	return sString;
}

function testResponse(action, response){
		var tmpStr=response.substring(0,action.length+2);
		if(tmpStr==(action+"OK"))
			return true;
		return false;
}

function openLink( link, params){
           var uriObj = parseUri(unescape(link));
	   if ( uriObj.protocol =='' ) {
	   	uriObj = parseUri(document.URL);
	   	var appdomain = uriObj.protocol+"://"+uriObj.authority;
       		open(appdomain+appContext+unescape(link), '', params);	
           } else {
		if ( uriObj.protocol =='telnet' || uriObj.protocol == 'ssh' ) {
			window.location=unescape(link);
		} else {
       			open(unescape(link), '', params);	
		}
           }	
}
