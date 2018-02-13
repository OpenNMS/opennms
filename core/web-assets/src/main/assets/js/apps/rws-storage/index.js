/*******************************************************************************
 * File-upload JavaScript Module for RWS STORAGE SERVICE - version 0.6
 * 
 * Copyright (c) 2009+ Rocco Rionero Copyright (c) 2009+ The OpenNMS Group, Inc.
 * All rights reserved everywhere.
 * 
 * This program was developed and is maintained by Rocco RIONERO ("the author")
 * and is subject to dual-copyright according to the terms set in "The OpenNMS
 * Project Contributor Agreement".
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * The author can be contacted at the following email address:
 * 
 * Rocco RIONERO rock (at) rionero.com
 * 
 * (please, specify "rws-storage-service" in the subject of your msg.)
 * 
 * ------------------------------------------------------------------------
 * OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc
 * ------------------------------------------------------------------------
 * 
 * UNSUPPORTED BETA-RELEASE SOFTWARE VERSION
 * 
 ******************************************************************************/

/* eslint-disable */

// -------------------------------------------------------------------------------
// This function parses the RWS response (in text format) and builds the
// corresponding JavaScript objects hierarchy tree starting from the passed
// objRWS (which will correspond to the root "RWS" node).
// Returns true if the entire response was parsed successufully, false
// otherwise;
function RWS_ParseInFrameTextResponse(respText, objRWS) {
	var state = 0;
	var buff = "";
	for ( var idx = 0; idx < respText.length; idx++) {
		var c = respText.charAt(idx);
		if ((c != '\r') && (c != '\n')) {
			buff += c;
			continue;
		}
		if (buff == "") {
			continue;
		}
		if (state == 1) {
			if (buff == "---RWS-RESPONSE-END---") {
				state = 2;
				break;
			}
			if (!(mc = /^RWS\.((?:[^\[\.=]+\[[0-9]+\]\.)*)([^\[\.=]+)\[([0-9]+)\]=(.*)$/
					.exec(buff))) {
				break;
			}
			var obj = objRWS;
			var pt = mc[1].split(".");
			var lt = pt.length - 1;
			for (i = 0; i < lt; i++) {
				var mm = /^([^\[]*)\[(.*)\]$/.exec(pt[i]);
				if (!obj[mm[1]]) {
					obj[mm[1]] = new Array();
					obj[mm[1]][mm[2]] = {};
				}
				obj = obj[mm[1]][mm[2]];
			}
			if (!obj[mc[2]]) {
				obj[mc[2]] = new Array();
			}
			obj[mc[2]][mc[3]] = mc[4];
		} else if (buff == "---RWS-RESPONSE-BEGIN---") {
			state = 1
		}
		;
		buff = "";
	}
	return (state == 2);
}
//-------------------------------------------------------------------------------

// -------------------------------------------------------------------------------
// This is the event handler executed as soon as a response is received into the
// target (hidden) IFRAME.

function RWS_InFrameResponseHandler(nameFrame) {

	// workaround.....

	location.reload(true);
	return true;

	// this code is to be enable
	var windowFrame = frames[nameFrame];
	var elementFrame = windowFrame.frameElement;
	var elementFrameDoc = windowFrame.document.documentElement;

	// detach the response-handler from the target frame

	if (elementFrame.detachEvent) {
		elementFrame.detachEvent("onload", elementFrame.RWS_handlerOnLoad);
	} else {
		elementFrame.removeEventListener("load",
				elementFrame.RWS_handlerOnLoad, false);
	}

	elementFrame.RWS_handlerOnLoad = null;

	// get the text response from the target frame

	if (elementFrameDoc.textContent) {
		var respText = elementFrameDoc.textContent;
	} else if (elementFrameDoc.innerText) {
		var respText = elementFrameDoc.innerText;
	} else {
		var respText = "";
	}

	// parse the text response and build the JS response object

	var objectResponse = {};
	objectResponse.RWS = {};

	if (respText != "") {
		if (!RWS_ParseInFrameTextResponse(respText, objectResponse.RWS)) {
			delete objectResponse.RWS;
			objectResponse.RWS = null;
		}
	}

	// call the user function (if callback not defined, show a default alert box)

	var show_default_alertbox = true;

	if (elementFrame.RWS_functionUserCallback) {
		try {
			show_default_alertbox = elementFrame.RWS_functionUserCallback(
					elementFrame.RWS_objectFormSubmit.parentNode.id,
					objectResponse);
		} catch (exception) {
			alert("The following exception occurred while executing the user-defined response handler:\n\n"
					+ exception
					+ "\n\nThe default response handler will be used.");
		}
	}

	elementFrame.RWS_functionUserCallback = null;

	if (show_default_alertbox) {
		var defmsg = "[RWS FORM Client - Default Response Handler]\n\n";

		if (objectResponse.RWS) {
			var rs = objectResponse.RWS.ResponseStatus[0];
			alert(defmsg + rs.Class[0] + ": " + rs.Description[0] + " ["
					+ rs.Code[0] + "]\n\n" + rs.ServiceMessage[0] + "\n");
		} else {
			alert(defmsg
					+ "Warning: the RWS server returned an invalid or malformed response.");
		}
	}

	// this solves partially the "history-back" problem (at least on IE)
	windowFrame.document.location.replace(elementFrame.RWS_hrefLocationOnInit);

	// re-enable the submit button
	if (elementFrame.RWS_objectFormSubmit) {
		elementFrame.RWS_objectFormSubmit.disabled = false;
	}

}
//-------------------------------------------------------------------------------

// -------------------------------------------------------------------------------
// RWS_ProcessInFrameResponse
//
// Called as "onsubmit" handler for the upload form.
//
// Arguments:
//
// objectForm the form object
// functionUserCallback the user-defined callback function
// nameTargetFrame the name of the target frame (see below)
//
// Note:
// The first time this function gets called (i.e. the first time
// a "submit" is done from a given form) it associates a "target
// frame" to the form (it will be used to receive the server's
// response to the POST request); here is what it does:
//
// 1) if the <nameTargetFrame> argument is NOT specified, an hidden
// iframe will be created and assigned a name (and ID) derived
// from the name of the posting form;
// 2) if the <nameTargetFrame> argument IS specified but NO frame
// with such name exists, an hidden iframe will be created (as
// in previous point) and will be assigned the name (and ID)
// specified by the <nameTargetFrame> argument;
// 3) if the <nameTargetFrame> argument IS specified AND a frame
// with such name DOES exist, such frame will be used as target;
//
// This gives you a way to solve cross-browser incompatibilities in
// iframe creation from JavaScript: the implemented code should
// work with most current browsers but if you found problems or
// just want to be on the safe side, create your hidden iframe with
// the usual HTML tag, give it a unique name and ID and pass it as
// <nameTargetFrame> to this function.

function RWS_ProcessInFrameResponse(objectForm, functionUserCallback,
		nameTargetFrame) {

	// identify or create the target frame

	if (!(windowFrame = objectForm.RWS_windowTargetFrame)) {
		if (!nameTargetFrame) {
			var nameTargetFrame = objectForm.id + "_RWS_ResponseTargetFrame";
		}

		if (!(windowFrame = frames[nameTargetFrame])) {
			// must create the frame
			if (document.createElement) {
				// go ahead
				try {
					elementFrame = document
							.createElement('<iframe name="'
									+ nameTargetFrame
									+ '" id="'
									+ nameTargetFrame
									+ '" src="" style="width:0;height:0;border:0px solid #fff;">');
				} catch (exception) {
					elementFrame = document.createElement('iframe');
					elementFrame.setAttribute("name", nameTargetFrame);
					elementFrame.setAttribute("id", nameTargetFrame);
					elementFrame.setAttribute("src", "");
					elementFrame.style.width = "0px";
					elementFrame.style.height = "0px";
					elementFrame.style.border = "0px";
				}
				document.body.appendChild(elementFrame);
			}
			if (!(windowFrame = frames[nameTargetFrame])) {
				alert("Unable to create target IFRAME, upload aborted.");
				return false;
			}
		}

		// store into the form object a reference to the frame's window object
		objectForm.RWS_windowTargetFrame = windowFrame;

		// the frame will be the target for the submit's response
		objectForm.target = nameTargetFrame;
	}

	// obtain the frame element from the frame's window object

	elementFrame = windowFrame.frameElement;

	// identify the submit button and disable the control

	if (elementFrame.RWS_objectFormSubmit) {
		elementFrame.RWS_objectFormSubmit.disabled = true;
	} else {
		elementFrame.RWS_objectFormSubmit = null;
		for (i = 0; i < objectForm.elements.length; i++) {
			if (objectForm.elements[i].type == "submit") {
				elementFrame.RWS_objectFormSubmit = objectForm.elements[i];
				elementFrame.RWS_objectFormSubmit.disabled = true;
				break;
			}
		}
	}

	// this is to partially solve the "history-back" problem (see the
	// response handler for the other half-part of the story)

	if (!elementFrame.RWS_hrefLocationOnInit) {
		elementFrame.RWS_hrefLocationOnInit = windowFrame.document.location.href;
	}

	// call the user function to notify that we're starting...

	if (functionUserCallback) {
		try {
			functionUserCallback(objectForm.id, null);
			elementFrame.RWS_functionUserCallback = functionUserCallback;
		} catch (exception) {
			alert("The following exception occurred while executing the user-defined response handler:\n\n"
					+ exception
					+ "\n\nThe default response handler will be used.");
			elementFrame.RWS_functionUserCallback = null;
		}
	}

	// oh, yeah, this is a dirty (or, maybe, smart?) trick... but no way
	// out as we need to pass the handler a reference to target frame
	// (and with IE this is not possible, unless we want to use the
	// global namespace): moreover we cannot use a "floating" anonymous
	// function here or we won't be able to detach the handler when we're
	// done (btw, an anonymous function which passes some local parameter
	// and gets called by an event handler is anyway a bad idea as it
	// might easily result in memory leaks). Here is the "trick" explained:
	// a function-object is created with the "Function()" contructor and
	// the function body, just a call to RWS_InFrameResponseHandler(), is
	// specified in such a way that the "elementFrame.name" argument is
	// evaluated when the function is "compiled". So the actual call to
	// RWS_InFrameResponseHandler() will have as passed argument a static
	// string (the name of the frame): no local context will be saved
	// because everything is static. The resulting function-object will be
	// assigned to a custom property of the elementFrame object, and such
	// property will be passed to both the attach/add and detach/remove
	// methods... and everything works fine.

	elementFrame.RWS_handlerOnLoad = new Function(
			"RWS_InFrameResponseHandler('" + elementFrame.name + "')");

	// attach the response-handler to the target frame, so to be executed as
	// soon as a response is received by the RWS service

	try {
		if (elementFrame.attachEvent) {
			elementFrame.attachEvent("onload", elementFrame.RWS_handlerOnLoad);
		} else {
			elementFrame.addEventListener("load",
					elementFrame.RWS_handlerOnLoad, false);
		}
	} catch (exception) {
		alert("Unable to attach the upload handler:\n\n" + exception
				+ "\n\nUpload aborted.");
		if (elementFrame.RWS_objectFormSubmit) {
			elementFrame.RWS_objectFormSubmit.disabled = false;
		}
		return false;
	}

	// everything ok, so far: return true and let the show go on

	return true;

}
//-------------------------------------------------------------------------------
