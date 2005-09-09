// ---------------------------------------------------------------------------
// --- Name:    Easy DHTML Treeview                                         --
// --- Original idea by : D.D. de Kerf                  --
// --- Updated by Jean-Michel Garnier, garnierjm@yahoo.fr                   --
// ---------------------------------------------------------------------------

/*****************************************************************************
Name : toggle
Parameters :  node , DOM element (<a> tag)
Description :     Description, collapse or unfold a branch
Author : Jean-Michel Garnier /  D.D. de Kerf
*****************************************************************************/

function toggle(node) {
    // Get the next tag (read the HTML source)
	var nextDIV = node.nextSibling;

	// find the next DIV
	while(nextDIV.nodeName != "DIV") {
		nextDIV = nextDIV.nextSibling;
	}

	// Unfold the branch if it isn't visible
	if (nextDIV.style.display == 'none') {

		// Change the image (if there is an image)
		if (node.childNodes.length > 0) {

			if (node.childNodes.item(0).nodeName == "IMG") {
				node.childNodes.item(0).src = getImgDirectory(node.childNodes.item(0).src) + "minus.gif";
			}
		}

		nextDIV.style.display = 'block';
	}
	// Collapse the branch if it IS visible
	else {

		// Change the image (if there is an image)
		if (node.childNodes.length > 0) {
			if (node.childNodes.item(0).nodeName == "IMG") {
  				node.childNodes.item(0).src = getImgDirectory(node.childNodes.item(0).src) + "plus.gif";
			}
		}
		nextDIV.style.display = 'none';
	}
}

/*****************************************************************************
Name : toggle2
Parameters :  node DOM element (<a> tag), folderCode String
Description :    if you use the "code" attribute in a folder element, toggle2 is called
instead of toggle. The consequence is that you MUST implement a selectFolder function in your page.
Author : Jean-Michel Garnier
*****************************************************************************/
function toggle2(node, folderCode) {
    toggle(node);
    selectFolder(folderCode);
}

/*****************************************************************************
Name : getImgDirectory
Parameters : Image source path
Return : Image source Directory
Author : Jean-Michel Garnier
*****************************************************************************/

function getImgDirectory(source) {
    return source.substring(0, source.lastIndexOf('/') + 1);
}

/************************************
************* IMPORTANT *************
*************************************

The functions above are NOT used by the DHTML treeview. Netherless, have a look bc some be useful if you
need to make XSLT on the client (since IE 5.5 and soon Mozilla !)

*/


/*****************************************************************************
Name : stringExtract
Parameters :
- st String input string, contains n separators
- position int, from 0 to n, position of the token wanted
- separator char, separator between token

Return : the token at the position wanted if it exists

Description : Equivalent to class java.util.StringTokenizer
Example -> stringExtract("A; B; C", 0, ";") = "A"

Author : Jean-Michel Garnier
*****************************************************************************/

function stringExtract( st, position, separator ) {
	var array;
	var result = new String('');
	var s = new String(st);
	if (s != '' ) {
		array = s.split( separator);
		// @TODO, add a control on position value ...
		result = array[position];
	}
	return result;
}

/*****************************************************************************
Name : jsTrim
Parameters : value, String
Return : the same String, with space characters removed
Description : equivalent to trim function
Author : Jean-Michel Garnier
*****************************************************************************/

function jsTrim(value) {
    var result = "";
    for (i=0; i < value.length; i++) {
        if (value.charAt(i) != ' ') {
            result += value.charAt(i);
        }
    }
    return result;
}

/*****************************************************************************
Name : findObj
Parameters :
- n String object's name
- d Document document
Return : a reference on the object if it exists
Description : Search an object in a document from its name.
Author : Macromedia
*****************************************************************************/

function findObj(n, d) {
  var p, i, x;
  if (!d)
    d = document;
  if ( (p=n.indexOf("?") )>0 && parent.frames.length ) {
		d = parent.frames[n.substring(p+1)].document;
		n = n.substring(0,p);
  }
  if (!(x=d[n])&& d.all )
	x = d.all[n];
  for (i=0; !x && i < d.forms.length; i++)
	x = d.forms[i][n];
  for (i=0; !x && d.layers && i<d.layers.length; i++)
	x = findObj(n, d.layers[i].document);

  return x;
}

/*****************************************************************************
Name : isInSelectInput
Parameters :
- v String Option value
- select_input input SELECT
Return : true if the SELECT already value
Author : Jean-Michel Garnier
*****************************************************************************/

function isInSelectInput(v, select_input) {
	for(var i=0; i<select_input.options.length; i++) {
		if (select_input.options[i].value == v) {
			return true;
		}
	}
	return false;
}

/*****************************************************************************
Name : selectOption
Parameters :
- v_value String Option value
- select_input SELECT
Description : Select all options whose value
Author : Jean-Michel Garnier
*****************************************************************************/

function selectOption(v_value, select_input) {
	var i, nb_item;
	nb_item = select_input.options.length;
	for (i = 0; i < nb_item ; i++) {
		if ( select_input.options[i].value == v_value )
			select_input.options[i].selected = true;
	}
}

/*****************************************************************************
Name : selectRemoveSelectedOption
Parameters : select_input SELECT
Description : removes all the selected options
Author : Jean-Michel Garnier
*****************************************************************************/

function selectRemoveSelectedOption(select_input) {
    for(var i=0; i<select_input.options.length; i++) {
        if ( select_input.options[i].selected ) {
  			select_input.options[i] = null;
   		}
	}
	select_input.selectedIndex = -1;
}

/*****************************************************************************
Name : selectRemoveAll
Parameters : select_input
Description : This Function removes all options
Author : Jean-Michel Garnier
*****************************************************************************/

function selectRemoveAll(select_input) {

    var linesNumber = select_input.options.length;
    for(i=0; i < linesNumber; i++) {
		select_input.options[0] = null;
    }
	select_input.selectedIndex = -1;
}

/*****************************************************************************
Name : buildXMLSource
Parameters : xmlSource_name, String, can be a file name (.xml or .xslt) or
a String containing the xml 
!!!BE SURE xml and xlt are lowercase
Return : a reference on a ActiveX Msxml2.FreeThreadedDOMDocument with the xml loaded
Author : Jean-Michel Garnier
*****************************************************************************/

function buildXMLSource(xmlSource_name) {

    var obj, file_extension;
    obj = new ActiveXObject("Msxml2.FreeThreadedDOMDocument");
    obj.async = false;
    obj.resolveExternals = false;

    file_extension = stringExtract(xmlSource_name, 1, ".");
    // if there is a file extension, then load the file
    if (file_extension == "xml" || file_extension == "xslt" ) {
        obj.load(xmlSource_name);
    }
    else {
        // else load the XML String
        obj.loadXML(xmlSource_name);
    }

    return obj;
}

/*****************************************************************************
Name : transform
Parameters :
- xmlSource Msxml2.FreeThreadedDOMDocument ActiveX XML
- xsltSource Msxml2.FreeThreadedDOMDocument ActiveX XSLT
Return : String with the result of the transformation (not an ActiveX object !)
Description :

Author : Jean-Michel Garnier
*****************************************************************************/

function transform(xmlSource, xsltSource) {

    var xslt;
    var xslProc, paramName, paramValue;

    // Create XLST
    xslt = new ActiveXObject("Msxml2.XSLTemplate");
    xslt.stylesheet = xsltSource;

    // Add parameters
    xslProc = xslt.createProcessor();

    xslProc.input = xmlSource;

    // add parameters if present
    if (arguments.length >2 && arguments.length % 2 == 0){
        for (var i=0; i < Math.floor((arguments.length)/2)-1; i++){
            paramName = arguments[2*i+2];
            paramValue = arguments[2*i+3];
            xslProc.addParameter(paramName, paramValue);
        }
    }

    xslProc.transform();
    return xslProc.output;
}

function BrowserDetectLite() {
	var ua = navigator.userAgent.toLowerCase();

	// browser name
	this.isGecko     = (ua.indexOf('gecko') != -1);
	this.isMozilla   = (this.isGecko && ua.indexOf("gecko/") + 14 == ua.length);
	this.isNS        = ( (this.isGecko) ? (ua.indexOf('netscape') != -1) : ( (ua.indexOf('mozilla') != -1) && (ua.indexOf('spoofer') == -1) && (ua.indexOf('compatible') == -1) && (ua.indexOf('opera') == -1) && (ua.indexOf('webtv') == -1) && (ua.indexOf('hotjava') == -1) ) );
	this.isIE        = ( (ua.indexOf("msie") != -1) && (ua.indexOf("opera") == -1) && (ua.indexOf("webtv") == -1) );
	this.isOpera     = (ua.indexOf("opera") != -1);
	this.isKonqueror = (ua.indexOf("konqueror") != -1);
	this.isIcab      = (ua.indexOf("icab") != -1);
	this.isAol       = (ua.indexOf("aol") != -1);
	this.isWebtv     = (ua.indexOf("webtv") != -1);

	// spoofing and compatible browsers
	this.isIECompatible = ( (ua.indexOf("msie") != -1) && !this.isIE);
	this.isNSCompatible = ( (ua.indexOf("mozilla") != -1) && !this.isNS && !this.isMozilla);

	// browser version
	this.versionMinor = parseFloat(navigator.appVersion);

	// correct version number
	if (this.isNS && this.isGecko) {
		this.versionMinor = parseFloat( ua.substring( ua.lastIndexOf('/') + 1 ) );
	}
	else if (this.isIE && this.versionMinor >= 4) {
		this.versionMinor = parseFloat( ua.substring( ua.indexOf('msie ') + 5 ) );
	}
	else if (this.isMozilla) {
      this.versionMinor = parseFloat( ua.substring( ua.indexOf('rv:') + 3 ) );
   }
   else if (this.isOpera) {
		if (ua.indexOf('opera/') != -1) {
			this.versionMinor = parseFloat( ua.substring( ua.indexOf('opera/') + 6 ) );
		}
		else {
			this.versionMinor = parseFloat( ua.substring( ua.indexOf('opera ') + 6 ) );
		}
	}
	else if (this.isKonqueror) {
		this.versionMinor = parseFloat( ua.substring( ua.indexOf('konqueror/') + 10 ) );
	}
	else if (this.isIcab) {
		if (ua.indexOf('icab/') != -1) {
			this.versionMinor = parseFloat( ua.substring( ua.indexOf('icab/') + 6 ) );
		}
		else {
			this.versionMinor = parseFloat( ua.substring( ua.indexOf('icab ') + 6 ) );
		}
	}
	else if (this.isWebtv) {
		this.versionMinor = parseFloat( ua.substring( ua.indexOf('webtv/') + 6 ) );
	}

	this.versionMajor = parseInt(this.versionMinor);
	this.geckoVersion = ( (this.isGecko) ? ua.substring( (ua.lastIndexOf('gecko/') + 6), (ua.lastIndexOf('gecko/') + 14) ) : -1 );

	// dom support
   this.isDOM1 = (document.getElementById);
	this.isDOM2Event = (document.addEventListener && document.removeEventListener);

   // css compatibility mode
   this.mode = document.compatMode ? document.compatMode : 'BackCompat';

	// platform
	this.isWin   = (ua.indexOf('win') != -1);
	this.isWin32 = (this.isWin && ( ua.indexOf('95') != -1 || ua.indexOf('98') != -1 || ua.indexOf('nt') != -1 || ua.indexOf('win32') != -1 || ua.indexOf('32bit') != -1 || ua.indexOf('xp') != -1) );
	this.isMac   = (ua.indexOf('mac') != -1);
	this.isUnix  = (ua.indexOf('unix') != -1 || ua.indexOf('linux') != -1 || ua.indexOf('sunos') != -1 || ua.indexOf('bsd') != -1 || ua.indexOf('x11') != -1)

	// specific browser shortcuts
	this.isNS4x = (this.isNS && this.versionMajor == 4);
	this.isNS40x = (this.isNS4x && this.versionMinor < 4.5);
	this.isNS47x = (this.isNS4x && this.versionMinor >= 4.7);
	this.isNS4up = (this.isNS && this.versionMinor >= 4);
	this.isNS6x = (this.isNS && this.versionMajor == 6);
	this.isNS6up = (this.isNS && this.versionMajor >= 6);
	this.isNS7x = (this.isNS && this.versionMajor == 7);
	this.isNS7up = (this.isNS && this.versionMajor >= 7);

	this.isIE4x = (this.isIE && this.versionMajor == 4);
	this.isIE4up = (this.isIE && this.versionMajor >= 4);
	this.isIE5x = (this.isIE && this.versionMajor == 5);
	this.isIE55 = (this.isIE && this.versionMinor == 5.5);
	this.isIE5up = (this.isIE && this.versionMajor >= 5);
	this.isIE6x = (this.isIE && this.versionMajor == 6);
	this.isIE6up = (this.isIE && this.versionMajor >= 6);

	this.isIE4xMac = (this.isIE4x && this.isMac);
}
