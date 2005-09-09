/*
 * Creato il 27-ott-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.map;

import java.sql.Timestamp;

import org.opennms.web.map.view.*;
import java.text.SimpleDateFormat;


/**
 * @author micmas
 *
 * Per modificare il modello associato al commento di questo tipo generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
public class TestSvgRender implements MapRenderer {

	/* (non Javadoc)
	 * @see org.opennms.web.appmap.MapRenderer#getRenderedMap(org.opennms.web.appmap.view.Map, java.lang.String)
	 */
	private final static String head =
		"<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
			+ "<!-- <?xml version=\"1.0\" standalone=\"no\"?> -->"
			+ "<!--<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20000303 Stylable//EN\""
			+ "\"http://www.w3.org/TR/2000/03/WD-SVG-20000303/DTD/svg-20000303-stylable.dtd\" [-->"
			+ "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\""
			+ "    \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\" ["
			+ "	<!ATTLIST svg"
			+ "	xmlns:a3 CDATA #IMPLIED"
			+ "		a3:scriptImplementation CDATA #IMPLIED"
			+ ">"
			+ "	<!ATTLIST script"
			+ "	a3:scriptImplementation CDATA #IMPLIED"
			+ ">"
			+ "]>"
			+ "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:a3=\"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/\" a3:scriptImplementation=\"Adobe\" onload=\"load(evt)\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"100%\" height=\"100%\">"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"Point2D.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"ApplicationMap.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"Debug.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"SVGElement.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"Map.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"Link.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"Semaphore.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"Label.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\" xlink:href=\"MapElement.js\"/>"
			+ "	<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\"><![CDATA[";

	private static String footer = "";

	private void setupFooter(String mapName, String mapBackground, String mapOwner, String mapAccess, Timestamp mapCreateTime, String userLastModified, Timestamp lastModTime){
		SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
		String createTimeStr ="";
		if(mapCreateTime!=null)
			createTimeStr = fmt.format(mapCreateTime);
		
		String lastModTimeStr ="";
		if(lastModTime!=null)
			lastModTimeStr = fmt.format(lastModTime);
		footer+="		]]></script>"
			+ "	<desc>"
			+ "		<!-- put a description here -->"
			+ "		Only for testing Semaphore class"
			+ "	</desc>"
			+ "	<defs>"
			+ "		<filter id=\"ombra\">"
			+ "			<feGaussianBlur in=\"SourceAlpha\" stdDeviation=\"1\" result=\"passo1\"/>"
			+ "			<feOffset in=\"passo1\" dx=\"1\" dy=\"1\" result=\"passo2\"/>"
			+ "			<feMerge>"
			+ "				<feMergeNode in=\"passo2\"/>"
			+ "				<feMergeNode in=\"SourceGraphic\"/>"
			+ "			</feMerge>"
			+ "		</filter>"
			+ "	</defs>"
			+ "	<svg id=\"ToolbarSvg\" width=\"100%\" height=\"30px\">" +
					"		<script type=\"text/ecmascript\" a3:scriptImplementation=\"Adobe\"><![CDATA[" +
					"			function doPostURL () {" +
					"				var query=\"Nodi\";" +
					"				for (elemToRender in map.nodes){" +
					"					var elem = map.nodes[elemToRender];" +
					"					var x= elem.x;" +
					"					query+= elemToRender+\",\"+x+\"-\";" +
					"					}" +
					"				query+=\"Link\";" +
					"				for (link in map.links){" +
					"					var elem = map.links[link];" +
					"					query+=elem.mapElement1.id+\"-\"+elem.mapElement2.id+\",\";" +
					"					}" +
					"				postURL ( \"ProvaPostUrl?\"+query, null, viewResponse, \"text/xml\", null );" +
					"			}" +
					"					" +
					"			function viewResponse(data) {" +
					"				var msg = '';" +
					"				if(data.success) {" +
					"					msg = data.content;" +
					"				} else {" +
					"					msg = \"Loading has failed\";" +
					"					}" +
					"				alert(msg);" +
					"				}" +
					"			" +
					"			function deleteMapElement()" +
					"			{" +
					"			  " +
					"			if(map.nodes!=null){" +
					"				 if(map.nodeSize>0){" +
					"					deletingMapElem=true;" +
					"					var childNode = svgDocument.getElementById(\"OtherInfoText\");" +
					"" +
					"					if (childNode)" +
					"						svgDocument.getElementById(\"OtherInfo\").removeChild(childNode);" +
					"	" +
					"					svgDocument.getElementById(\"OtherInfo\").appendChild(parseXML(\"<text id=\\\"OtherInfoText\\\" x=\\\"5\\\" y=\\\"20\\\">Actions\"+	\"<tspan x=\\\"5\\\" dy=\\\"30\\\">Select the element to delete</tspan></text>\",svgDocument) );" +
					"	" +
					"					}" +
					"				}" +
					"			}" +
					"" +
					"		]]></script>" +
					"		" +
					"		<rect x=\"0\" y=\"0\" id=\"toolbar\" width=\"100%\" height=\"30px\" />" +
					"		<g  onclick=\"deleteMapElement();\">" +
					"" +
					"			<rect x=\"0\" y=\"0\" width=\"60\" height=\"30\" style=\"fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9\"/>" +
					"			<text x=\"2\" y=\"12\" style=\"fill:white\">delete</text>" +
					"		</g>" +
					"		<g  onclick=\"doPostURL();\">" +
					"			<rect x=\"65\" y=\"0\" width=\"60\" height=\"30\" style=\"fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9\"/>" +
					"			<text x=\"67\" y=\"12\" style=\"fill:white\">postUrl</text>" +
					"		</g>" +
					"	</svg>" +
					"	<svg id=\"ApplicationSvg\" y=\"30px\" width=\"100%\" height=\"400px\">" +
					"		<svg a3:scriptImplementation=\"Adobe\" width=\"100%\" height=\"100%\">" +
					"			<g id=\"Application\">" +
					"			</g>" +
					"		</svg>" +
					"		<svg a3:scriptImplementation=\"Adobe\" x=\"80%\" width=\"20%\" height=\"50%\" id=\"MapInfo\">" +
					"			<text id=\"MapInfoText\" x=\"5\" y=\"20\">Map info" +
					"			<tspan x=\"5\" dy=\"30\">Name: "+mapName+"</tspan>" +
					"				<tspan x=\"5\" dy=\"20\">Background: "+((mapBackground!=null)?mapBackground:" ")+"</tspan>" +
					"				<tspan x=\"5\" dy=\"20\">Owner: "+mapOwner+"</tspan>" +
					"				<tspan x=\"5\" dy=\"20\">Access mode: "+((mapAccess!=null)?mapAccess:" ")+"</tspan>" +
					"				<tspan x=\"5\" dy=\"20\">User last modified: "+((userLastModified!=null)?userLastModified:" ")+"</tspan>" +
					"				<tspan x=\"5\" dy=\"20\">Create time: "+createTimeStr+"</tspan>" +
					"				<tspan x=\"5\" dy=\"20\">Last modified time: "+lastModTimeStr+"</tspan>" +
					"			</text>" +
					"		</svg>" +
					"		<svg a3:scriptImplementation=\"Adobe\" x=\"80%\" y=\"50%\" width=\"20%\" height=\"50%\" id=\"NodeInfo\">" +
					"			<text id=\"NodeInfoText\" x=\"5\" y=\"20\"></text>" +
					"		</svg>" +
					"		<svg a3:scriptImplementation=\"Adobe\" x=\"80%\" y=\"80%\" width=\"20%\" height=\"50%\" id=\"OtherInfo\">" +
					"			<text id=\"OtherInfoText\" x=\"5\" y=\"20\"></text>" +
					"		</svg>" +
					"	</svg>" +
					""
			+ "</svg>";
	}

	/* (non Javadoc)
	 * @see org.opennms.web.appmap.MapRenderer#getRenderedMap(org.opennms.web.appmap.view.Map, java.lang.String)
	 * 
	 * 		var map = new Map("#bbbbbb", "", "Background", "80%", "400px", 0, 0);
	 *		function load(evt)
	 *		{
	 *			var node = svgDocument.getElementById("Application");
	 *			node.appendChild(map.getSvgNode());
	 *			node = map.getSvgNode();
	 *
	 *			map.addElement("a1", "element.svg", "192.168.201.12", "green",  30, 30);
	 *			map.addElement("a2", "element.svg", "192.168.201.14", "red",  150, 150);
	 *			map.addElement("a3", "element.svg", "10.0.0.1", "yellow",  250, 150);
	 *			map.addElement("a4", "element.svg", "10.0.0.125", "pink",  150, 250);						
	 *			map.addLink("a1", "a2", "green", 1);			
	 *			map.addLink("a1", "a3", "green", 1);
	 *			map.addLink("a1", "a4", "green", 1);
	 *			map.addLink("a2", "a3", "green", 1);						
	 *			map.addLink("a2", "a4", "green", 1);						
	 *			map.addLink("a3", "a4", "green", 1);						
	 *			map.render();
	 *			//alertPrintNode(node);
	 *		}
	 * 
	 */
	public byte[] getRenderedMap(VMap map, String displayType) {
		// TODO Stub di metodo generato automaticamente
		String background = map.getBackground();
		
		if(background==null){
			background="black";
		}
		String middle =
			//var deletingLink=false;
			"var map = new Map(\"#bbbbbb\", \""
				+ map.getBackground()
				+ "\", \""+map.getId()+"\", \"80%\", \"400px\", 0, 0);" +
			"var deletingMapElem=false;";
		middle += "function load(evt)"
			+ "{"
			+ "		var node = svgDocument.getElementById(\"Application\");"
			+ "		node.appendChild(map.getSvgNode());"
			+ "		node = map.getSvgNode();";

		VElement[] mapElements = map.getAllElements();
		VElement mapElement;
		int length = mapElements.length;
		for (int i = 0; i < length; i++) {
			mapElement = mapElements[i];
			middle += "map.clear();" +
					"map.addElement(\""
				+ mapElement.getId()
				+ "\", \""
				+ ((mapElement.getIcon()!=null)?mapElement.getIcon():"element.svg")
				+ "\", \""
				+ mapElement.getLabel()
				//+ "\", " + mapElement.getSeverity() + ",  " + mapElement.getX() + ", " + mapElement.getY() + ");"; 
				+ "\", \"green\",  " + mapElement.getX() + ", " + mapElement.getY() + ");";
			}
		middle += " map.render(); }";
		setupFooter(map.getName(),map.getBackground(),map.getOwner(),map.getAccessMode(),map.getCreateTime(),map.getUserLastModifies(),map.getLastModifiedTime());
		//System.out.print(head + middle + footer);
		return (head + middle + footer).getBytes();
	}

}
