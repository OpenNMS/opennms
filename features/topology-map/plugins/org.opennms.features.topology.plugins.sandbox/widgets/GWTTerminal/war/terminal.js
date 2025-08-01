/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
var query0 = "w=" + 80 + "&h=" + 24;
var query1 = query0 + "&k=";
var buf = "";
var timeout;
var error_timeout;
var keybuf = [];
var sending = 0;
var rmax = 1;
var force = 1;

function debug(s) {
	
}

function error() {
	debug("Connection lost timeout ts:" + ((new Date).getTime()));
}

function update() {
	if (sending == 0) {
		sending = 1;
		var r = new XMLHttpRequest();
		var send = "";
		while (keybuf.length > 0) {
			send += keybuf.pop();
		}
		if (send.length > 0) {
			document.getElementById("sshSpan").innerHTML = send;
		}
		var query = query1 + send;
		if (force) {
			query = query + "&f=1";
			force = 0;
		}
		r.open("POST", "gogo", true);
		r.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		var rState = 4;
		var rStatus = 200;
		r.onreadystatechange = function () {
			if (rState == 4) {
				if (rStatus == 200) {
					window.clearTimeout(error_timeout);
					if (send.length > 0) {
						rmax = 100;
					} else {
						rmax *= 2;
						if (rmax > 2000)
							rmax = 2000;
					}
					sending=0;
					timeout = window.setTimeout(update, 20000);
				} else {
					debug("Connection error status:" + r.status);
				}
			}
		}
		//error_timeout = window.setTimeout(error, 5000);
		if (send.length > 0) {
			document.getElementById("inputSpan").innerHTML = decodeURIComponent(send);
		}
		r.send(query);
	}
}

function queue(s) {
	keybuf.unshift(s);
	if (sending == 0) {
		window.clearTimeout(timeout);
		timeout = window.setTimeout(update, 1);
	}
}

function keypress(ev, fromkeydown) {
	// Translate to standard keycodes
//	if (!ev)
//		ev = window.event;
//	var kc;
//	if (ev.keyCode)
//		kc = ev.keyCode;
//	if (!fromkeydown && ev.which)
//		kc = ev.which;
//	if (ev.ctrlKey) {
//		if (kc >= 0 && kc <= 32)
//			kc = kc;
//		else if (kc >= 65 && kc <= 90)
//			kc -= 64;
//		else if (kc >= 97 && kc <= 122)
//			kc -= 96;
//		else {
//			switch (kc) {
//			case 54:  kc=30; break;	// Ctrl-^
//			case 109: kc=31; break;	// Ctrl-_
//			case 219: kc=27; break;	// Ctrl-[
//			case 220: kc=28; break;	// Ctrl-\
//			case 221: kc=29; break;	// Ctrl-]
//			default: return true;
//			}
//		}
//	} else if (fromkeydown) {
//		switch(kc) {
//		case 8: break;			     // Backspace
//		case 9: break;               // Tab
//		case 27: break;			     // ESC
//		case 33:  kc = 63276; break; // PgUp
//		case 34:  kc = 63277; break; // PgDn
//		case 35:  kc = 63275; break; // End
//		case 36:  kc = 63273; break; // Home
//		case 37:  kc = 63234; break; // Left
//		case 38:  kc = 63232; break; // Up
//		case 39:  kc = 63235; break; // Right
//		case 40:  kc = 63233; break; // Down
//		case 45:  kc = 63302; break; // Ins
//		case 46:  kc = 63272; break; // Del
//		case 112: kc = 63236; break; // F1
//		case 113: kc = 63237; break; // F2
//		case 114: kc = 63238; break; // F3
//		case 115: kc = 63239; break; // F4
//		case 116: kc = 63240; break; // F5
//		case 117: kc = 63241; break; // F6
//		case 118: kc = 63242; break; // F7
//		case 119: kc = 63243; break; // F8
//		case 120: kc = 63244; break; // F9
//		case 121: kc = 63245; break; // F10
//		case 122: kc = 63246; break; // F11
//		case 123: kc = 63247; break; // F12
//		default: return true;
//		}
//		
//	}
//
//	var k = "";
//	// Build character
//	switch (kc) {
//	case 126:   k = "~~"; break;
//	case 63232: k = "~A"; break; // Up
//	case 63233: k = "~B"; break; // Down
//	case 63234: k = "~D"; break; // Left
//	case 63235: k = "~C"; break; // Right
//	case 63276: k = "~1"; break; // PgUp
//	case 63277: k = "~2"; break; // PgDn
//	case 63273: k = "~H"; break; // Home
//	case 63275: k = "~F"; break; // End
//	case 63302: k = "~3"; break; // Ins
//	case 63272: k = "~4"; break; // Del
//	case 63236: k = "~a"; break; // F1
//	case 63237: k = "~b"; break; // F2
//	case 63238: k = "~c"; break; // F3
//	case 63239: k = "~d"; break; // F4
//	case 63240: k = "~e"; break; // F5
//	case 63241: k = "~f"; break; // F6
//	case 63242: k = "~g"; break; // F7
//	case 63243: k = "~h"; break; // F8
//	case 63244: k = "~i"; break; // F9
//	case 63245: k = "~j"; break; // F10
//	case 63246: k = "~k"; break; // F11
//	case 63247: k = "~l"; break; // F12
//	default:    k = String.fromCharCode(kc); break;
//	}	
//	queue(encodeURIComponent(k));
//
//	ev.cancelBubble = true;
//	if (ev.stopPropagation) ev.stopPropagation();
//	if (ev.preventDefault) ev.preventDefault();
//
//	return true;
	document.getElementById("inputSpan").innerHTML += ev.charCode;
	return true;
}

function keydown(ev) {
//	if (!ev)
//		ev = window.event;
//	o = { 9:1, 8:1, 27:1, 33:1, 34:1, 35:1, 36:1, 37:1, 38:1, 39:1, 40:1, 45:1, 46:1, 112:1,
//			113:1, 114:1, 115:1, 116:1, 117:1, 118:1, 119:1, 120:1, 121:1, 122:1, 123:1 };
//	if (o[ev.keyCode] || ev.ctrlKey || ev.altKey) {
//		keypress(ev, true);
//	}
	document.getElementById("inputSpan").innerHTML += ev.keyCode;
}

function init() {
	if (typeof(XMLHttpRequest) == "undefined") {
		XMLHttpRequest = function() {
			try { return new ActiveXObject("Msxml2.XMLHTTP.6.0"); }
			catch(e) {}
			try { return new ActiveXObject("Msxml2.XMLHTTP.3.0"); }
			catch(e) {}
			try { return new ActiveXObject("Msxml2.XMLHTTP"); }
			catch(e) {}
			try { return new ActiveXObject("Microsoft.XMLHTTP"); }
			catch(e) {}
			throw new Error("This browser does not support XMLHttpRequest.");
		};
	}

	timeout = window.setTimeout(update, 100);
}

init();