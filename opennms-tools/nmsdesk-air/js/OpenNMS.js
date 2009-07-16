	// https://m.sceur.ch/opennms/rss.jsp?feed=notification
	var hostname;
	var port;
	var protocol;
	var username;
	var password;
	var urlbase;
	
	function loadproperties() {
		var prefsFile;
		var prefsXML; // The XML data
		var stream; // The FileStream object used to read and write prefsFile data.
		
		prefsFile = air.File.applicationStorageDirectory;
		prefsFile = prefsFile.resolvePath("preferences.xml");
	
		stream = new air.FileStream();
			if (prefsFile.exists) {
				stream.open(prefsFile, air.FileMode.READ);
				//processXMLData();
				
				prefsXML = stream.readUTFBytes(stream.bytesAvailable);
				stream.close();
				var domParser = new DOMParser();
				prefsXML = domParser.parseFromString(prefsXML, "text/xml");
				var nmsprefs = prefsXML.getElementsByTagName("nmsdesk")[0];
				hostname = nmsprefs.getElementsByTagName("hostname")[0].textContent;
				protocol = nmsprefs.getElementsByTagName("protocol")[0].textContent;
				port = nmsprefs.getElementsByTagName("port")[0].textContent;
				username = nmsprefs.getElementsByTagName("username")[0].textContent;
				password = nmsprefs.getElementsByTagName("password")[0].textContent;
				path = nmsprefs.getElementsByTagName("path")[0].textContent;
				urlbase = protocol + "://" + hostname + ":" + port + path;
				air.trace("Host " + urlbase );

			}
			else {
				alert("preferences.xml not found at " + air.File.applicationStorageDirectory.nativePath + "; using demo.opennms.org")
			}
	}

	function outages() {
		loadproperties();
		var request = new air.URLRequest(urlbase+"/outages");
		var loader = new air.URLLoader();
		loader.addEventListener(air.Event.COMPLETE, restoutagesCompleteHandler);
		loader.load(request);
		
	}
	
	function alarms() {
		loadproperties();
		var request = new air.URLRequest(urlbase+"/alarms");
		var loader = new air.URLLoader();
		loader.addEventListener(air.Event.COMPLETE, alarmsCompleteHandler);
		loader.load(request);
		
	}	
	
	function notifications() {
		loadproperties();
		var request = new air.URLRequest(urlbase+"/notifications");
		var loader = new air.URLLoader();
		loader.addEventListener(air.Event.COMPLETE, notificationsCompleteHandler);
		loader.load(request);
		
	}	
	
/* Notifications as they come out:
<notifications count="1">
<onmsNotification>
<eventUei>uei.opennms.org/nodes/nodeDown</eventUei>
<notifConfigName>nodeDown</notifConfigName>
<notifyId>1</notifyId>
<numericMsg>111-1</numericMsg>
<pageTime>2008-08-15T22:11:04.810+02:00</pageTime>
<queueId>default</queueId>
<subject>Notice #1: node 192.168.1.150 down.</subject>
<textMsg>
All services are down on node 192.168.1.150.  New Outage records have 
been created and service level availability calculations will 
be impacted until this outage is resolved.  
</textMsg>
</onmsNotification>
</notifications>
 */	

function notificationsCompleteHandler(event){
		var loader2 = event.target;
		air.trace("loaded" + loader2.data);
		var parser = new DOMParser();
		var xmldoc = parser.parseFromString(loader2.data, "text/xml");
		air.trace("calling parser" + xmldoc);
		
		// <id>3</id>
		// <ifLostService>2008-03-22T15:03:09+01:00</ifLostService>
		// <ifRegainedService>2008-03-22T15:03:40+01:00</ifRegainedService>

		var entries = xmldoc.getElementsByTagName("notification");
		air.trace("found " + entries.length + " entries.");
		var position = document.getElementById("results");
		var oldtable = document.getElementById("resultstable");
		oldtable.parentNode.removeChild(oldtable);
		var table = document.createElement("table");
		table.setAttribute("id", "resultstable");
		position.appendChild(table);
		
		for (Index = 0; Index < entries.length; Index++) {
			var id = entries[Index].getAttribute("id");
			var eventUei = entries[Index].getElementsByTagName("uei")[0].textContent;
			var textMsg = entries[Index].getElementsByTagName("textMessage")[0].textContent;
			var subject = entries[Index].getElementsByTagName("subject")[0].textContent;

			RegainedServiceElement = entries[Index].getElementsByTagName("ifRegainedService")[0];
			if (RegainedServiceElement) {
				RegainedService = RegainedServiceElement.textContent;
			}
			else {
				RegainedService = "never";
			}
			//var myline = "" + title + " " + published + "\n";
			//mydataobj.appendChild(document.createTextNode(myline));
			writeOutageResult(table, id, subject, eventUei);
			
		}
	}
	
function restoutagesCompleteHandler(event){
		var loader2 = event.target;
		air.trace("loaded" + loader2.data);
		var parser = new DOMParser();
		var xmldoc = parser.parseFromString(loader2.data, "text/xml");
		air.trace("calling parser" + xmldoc);
		
		// <id>3</id>
		// <ifLostService>2008-03-22T15:03:09+01:00</ifLostService>
		// <ifRegainedService>2008-03-22T15:03:40+01:00</ifRegainedService>

		var entries = xmldoc.getElementsByTagName("outage");
		air.trace("found " + entries.length + " entries.");
		var position = document.getElementById("results");
		var oldtable = document.getElementById("resultstable");
		oldtable.parentNode.removeChild(oldtable);
		var table = document.createElement("table");
		table.setAttribute("id", "resultstable");
		position.appendChild(table);
		
		for (Index = 0; Index < entries.length; Index++) {
			var id = entries[Index].getAttribute("id");
			var LostService = entries[Index].getElementsByTagName("ifLostService")[0].textContent;
			RegainedServiceElement = entries[Index].getElementsByTagName("ifRegainedService")[0];
			if (RegainedServiceElement) {
				RegainedService = RegainedServiceElement.textContent;
			}
			else {
				RegainedService = "never";
			}
			//var myline = "" + title + " " + published + "\n";
			//mydataobj.appendChild(document.createTextNode(myline));
			writeOutageResult(table, id, LostService, RegainedService);
			
		}
	}

/* Alarm Definition
<alarmType>1</alarmType>
<counter>1</counter>
+
<description>
 <p>All interfaces on node 192.168.1.140 are down.</p> <p>This event is generated when node outage processing determines that all interfaces on the node are down.</p> <p>New outage records have been created and service level availability calculations will be impacted until this outage is resolved.</p>
</description>
<firstEventTime>2008-08-15T16:20:45+02:00</firstEventTime>
<id>7</id>
<lastEventTime>2008-08-15T16:20:45+02:00</lastEventTime>
<logMsg>Node 192.168.1.140 is down. </logMsg>
<reductionKey>uei.opennms.org/nodes/nodeDown::4</reductionKey>
<severity>6</severity>
<suppressedTime>2008-08-15T16:20:45+02:00</suppressedTime>
<suppressedUntil>2008-08-15T16:20:45+02:00</suppressedUntil>
<uei>uei.opennms.org/nodes/nodeDown</uei>
<x733ProbableCause>0</x733ProbableCause> 
*/

	function alarmsCompleteHandler(event){
		var loader2 = event.target;
		air.trace("loaded" + loader2.data);
		var parser = new DOMParser();
		var xmldoc = parser.parseFromString(loader2.data, "text/xml");
		air.trace("calling parser" + xmldoc);
		
	

		var entries = xmldoc.getElementsByTagName("alarm");
		air.trace("found " + entries.length + " entries.");
		var position = document.getElementById("results");
		var oldtable = document.getElementById("resultstable");
		oldtable.parentNode.removeChild(oldtable);
		var table = document.createElement("table");
		table.setAttribute("id", "resultstable");
		position.appendChild(table);
		
		for (Index = 0; Index < entries.length; Index++) {
			var id = entries[Index].getElementsByTagName("id")[0].textContent;
			var description = entries[Index].getElementsByTagName("description")[0].textContent;
			var logMsg = entries[Index].getElementsByTagName("logMessage")[0].textContent;

			RegainedServiceElement = entries[Index].getElementsByTagName("ifRegainedService")[0];

			if (RegainedServiceElement) {
				RegainedService = RegainedServiceElement.textContent;
			}
			else {
				RegainedService = "never";
			}
			//var myline = "" + title + " " + published + "\n";
			//mydataobj.appendChild(document.createTextNode(myline));
			writeOutageResult(table, id, logMsg, RegainedService);
			
		}
	}

		function writeOutageResult(table, id, LostService, RegainedService) 
			{
		
    			var tr = document.createElement("tr");
    			tr.setAttribute("class", "grid");
			
    			var td = document.createElement("td");
				td.innerHTML= id;
    			tr.appendChild(td);
			
    			td = document.createElement("td");
				td.innerHTML= LostService;
    			tr.appendChild(td);
			
				td = document.createElement("td");
				td.innerHTML= RegainedService;
    			tr.appendChild(td);
			
    			table.appendChild(tr); 
			}
		
			
		function writeFileResult(table, title, published, link) 
			{
		
    			var tr = document.createElement("tr");
    			tr.setAttribute("class", "grid");
			
    			var td = document.createElement("td");
			td.innerHTML= "<a href=\""+ link + "&nonavbar=true&quiet=true\">" + title + "</a>";
    			tr.appendChild(td);
			
    			td = document.createElement("td");
			td.innerHTML= "<a href=\""+ link + "&nonavbar=true&quiet=true\">" + published + "</a>";
    			tr.appendChild(td);
			
			
    			table.appendChild(tr); 
			}
		 
		 

