//
// Copyright (C) 2003 Networked Knowledge Systems, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      Derek Glidden   <dglidden@opennms.org>
//      http://www.nksi.com/
//
//

package org.opennms.web.map;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.util.XMLResourceDescriptor;
import org.opennms.web.category.CategoryModel;
import org.opennms.web.category.CategoryUtil;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The core of the SVG generation.  We get a Vector of MapNode
 * objects, do all our calculations to figure out how to draw the map,
 * where everything goes, and generate the SVG here.
 *
 * @author <A HREF="mailto:dglidden@opennms.org">Derek Glidden</A>
 * @author <A HREF="http://www.nksi.com/">NKSi</A>
 */

public class DocumentGenerator {

    private int documentWidth;
    private int documentHeight;

    private Document document;
    private String namespace;

    private Vector nodes;
    private Hashtable iconNames;

    private ServletContext ctx;

    private String mapType;
    private String urlBase;

    /**
     * constructor
     */

    public DocumentGenerator() {
        this.documentWidth = 0;
        this.documentHeight = 0;

        this.document = null;
        this.namespace = null;

        this.mapType = new String();
        this.urlBase = new String();

        this.nodes = new Vector();
        this.iconNames = new Hashtable();

        // this maps from the "server type" to the filename for the
        // SVG icon.  except that loading the icons from disk into
        // the SVG we generate isn't working, so at the moment, this
        // is dead code.
        iconNames.put("infrastructure", "images/infrastructure.svg");
        iconNames.put("laptop", "images/laptop.svg");
        iconNames.put("opennms", "images/opennms.svg");
        iconNames.put("other", "images/other.svg");
        iconNames.put("printer", "images/printer.svg");
        iconNames.put("server", "images/server.svg");
        iconNames.put("telephony", "images/telephony.svg");
        iconNames.put("unspecified", "images/unspecified.svg");
        iconNames.put("workstation", "images/workstation.svg");

        // loadIcons();
    }


    /**
     * log a message.  preferably use the ServletContext' log method,
     * but if we don't have one, just go out to System.err
     */

    private void log(String message) {
        if(this.ctx == null) {
            System.err.println(message);
        } else {
            this.ctx.log(message);
        }
    }


    /**
     * set the Vector of nodes that we need to map
     */

    public void setNodes(Vector nodes) {
        this.nodes = nodes;
    }


    /**
     * set the ServletContext so we can find the path to load icons
     * from the filesystem directly and use the log method.
     */

    public void setServletContext(ServletContext ctx) {
        this.ctx = ctx;
    }


    /**
     * set the URL base so we can create absolute references to
     * content embedded in our SVG
     */

    public void setUrlBase(String base) {
        this.urlBase = base;
    }


    /**
     * set the type of map we want to draw: "tree" or "boring"
     */

    public void setMapType(String type) {
        this.mapType = type;
    }


    /**
     * load the SVG icon data so we can embed it directly into the SVG
     * we output instead of loading each icon as an external entity.
     *
     * this doesn't work right for some reason.  when the SVG is
     * rendered, the icons are all messed up or don't show up.  For
     * now, forget trying to load the icons as "symbol" elements
     * inside of the SVG document and just refer to them as external
     * entities...
     *
     * When I have more time, I will try to fix this.
     */

    private void loadIcons() {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();

            Enumeration e = iconNames.keys();
            System.err.println("Loading icons");
            while(e.hasMoreElements()) {
                String icon = (String) e.nextElement();
                String filename = (String) iconNames.get(icon);
                String uriStr = "file:///opt/tomcat/webapps/batik/images/" + filename;
                URI uri = new URI(uriStr);

                // log("loading icon " + icon + " from " + uriStr);
                // log("URI is " + uri.toString());
                // log("Scheme is " + uri.getScheme());

                Document iconDoc = f.createDocument(uri.toString());
                Element iconRootElement = iconDoc.getDocumentElement();

                // log("Icon loaded");

                Element symbol = this.document.createElementNS(this.namespace, "symbol");
                symbol.setAttributeNS(null, "id", icon);

                // log("Symbol element created");

                Node clonedIcon = this.document.importNode(iconRootElement, true);

                // log("icon cloned");

                symbol.appendChild(clonedIcon);
                this.document.getDocumentElement().appendChild(symbol);
            }
        } catch(IOException e) {
            log("IOException in DocumentGenerator.loadIcons()");
            log(e.toString());
        } catch(Exception e) {
            log("Exception in DocumentGenerator.loadIcons()");
            log(e.toString());
        }
    }


    /**
     * tell me if a given node is child of another node
     *
     * @param parent the parent node you are testing for the
     * parent-child relationship
     * @param child the child node you are testing for the
     * parent-child relationship
     */

    private boolean isChildNode(MapNode parent, MapNode child) {
        return parent.getNodeID() == child.getNodeParent();
    }


    /**
     * find out how many immediate parent hosts for this
     * host. (adapted from nagios code)
     *
     * @param child the child node for which you wish to find the number of
     * parents
     */

    private int numberOfImmediateParents(MapNode child) {
        int parents = 0;
        Iterator i = this.nodes.iterator();
        while(i.hasNext()) {
            MapNode parent = (MapNode) i.next();
            if(isChildNode(parent, child)) {
                parents++;
            }
        }

        return parents;
    }


    /**
     * figure out the max child width for the map. (adapted from
     * nagios code)
     *
     * @param parent the parent node for which you wish to find the
     * maximum width of children somewhere down its tree of child
     * nodes
     */


    private int findMaxChildWidth(MapNode parent) {
        int childWidth = 0;
        Iterator i = this.nodes.iterator();
        while(i.hasNext()) {
            MapNode child = (MapNode) i.next();
            if(isChildNode(parent, child)) {
                childWidth += findMaxChildWidth(child);
            }
        }

        if(childWidth == 0) {
            return 1;
        } else {
            return childWidth;
        }
    }


    /**
     * calculate coordinates for all hosts, doing a balanced-tree
     * drawing thingie.  (heavily adapted from nagios code.  in fact,
     * it would be better to say this is based on the algorithms
     * nagios uses than to say it's in any way based on that code...)
     *
     * @param parent the parent node for which you will start to
     * calculate the rest of the tree coordinates
     */

    private void calculateBalancedTreeCoordinates(MapNode parent) {
        int parentWidth = findMaxChildWidth(parent);
        int startDrawingX = parent.getX() - ((parentWidth - 1) / 2);
        int currentDrawingX = startDrawingX;

        Iterator i = this.nodes.iterator();
        while(i.hasNext()) {
            MapNode n = (MapNode) i.next();
            if(isChildNode(parent, n)) {
                int thisWidth = findMaxChildWidth(n);
                n.setX(currentDrawingX + ((thisWidth - 1) / 2));
                n.setY(parent.getY() + 1);

                currentDrawingX += (thisWidth);
                calculateBalancedTreeCoordinates(n);
            }
        }
    }


    /**
     * start the job of figuring out where on the screen to draw each
     * host by starting to calculate the Balanced Tree coordinates
     * from the root node (node "0")
     */

    private void calculateTreeHostCoordinates() {
        MapNode rootNode = (MapNode) this.nodes.get(0);
        int maxWidth = findMaxChildWidth(rootNode);
        rootNode.setX((maxWidth / 2));
        rootNode.setY(0);

        calculateBalancedTreeCoordinates(rootNode);
    }


    /**
     * do a really stupid and simple "draw hosts left to right, 10
     * columns by as many rows as necessary
     */

    private void calculateBoringHostCoordinates() {
        int row = 0;
        int col = 0;

        // we start at 1 since the "pseudo-node" for the OpenNMS
        // monitor is inserted into the node array at 0 and will never
        // have any real status associated with it
        for(int i = 1; i < this.nodes.size(); i++) {
            MapNode mn = (MapNode) this.nodes.get(i);
            mn.setX(col);
            mn.setY(row);

            if(i % 8 == 0) {
                col = 0;
                row++;
            } else {
                col++;
            }
        }
    }

    /**
     * draw a line from parent to child
     *
     * @param parent the starting point of the line
     * @param child the ending point of the line
     */

    private void drawLine(MapNode parent, MapNode child) {

        Element root = this.document.getDocumentElement();
        Element line = this.document.createElementNS(this.namespace, "line");
        line.setAttributeNS(null, "x1", parent.getLineFromX());
        line.setAttributeNS(null, "y1", parent.getLineFromY());
        line.setAttributeNS(null, "x2", child.getLineToX());
        line.setAttributeNS(null, "y2", child.getLineToY());
        line.setAttributeNS(null, "style", "stroke:black;stroke-width:2");

        root.appendChild(line);
    }


    /**
     * draw the lines between parents and children
     */

    private void drawLines(MapNode parent) {
        Iterator i = this.nodes.iterator();
        while(i.hasNext()) {
            MapNode n = (MapNode) i.next();
            if(isChildNode(parent, n)) {
                drawLine(parent, n);
                drawLines(n);
            }
        }
    }


    /**
     * generate SVG for each host
     *
     * @param loadIcons tell me whether I should generate file://
     * references to load icons from the filesystem or generate URL
     * references.  "true" will generate file:// references for when
     * we are sending the output to the Batik Transcoder, while
     * "false" will generate http:// references for when we are
     * downloading the SVG directly into an external viewer.
     */

    private void drawHosts(boolean loadIcons) {
        Element root = this.document.getDocumentElement();
        Iterator i = this.nodes.iterator();
        while(i.hasNext()) {
            MapNode n = (MapNode) i.next();
            Element host = createHostElement(n, loadIcons);
            root.appendChild(host);
        }
    }


    /**
     * figure out the width and height of the document
     */

    private void calculateDocumentWidthAndHeight() {
        int maxX = 0;
        int maxY = 0;

        Iterator i = this.nodes.iterator();
        while(i.hasNext()) {
            MapNode n = (MapNode) i.next();

            int nodeX = n.getX();
            int nodeY = n.getY();

            if(nodeX > maxX) {
                maxX = nodeX;
            }

            if(nodeY > maxY) {
                maxY = nodeY;
            }
        }

        this.documentWidth = (maxX + 1) * (MapNode.defaultNodeWidth + MapNode.widthBuffer);
        this.documentHeight = (maxY + 1) * (MapNode.defaultNodeHeight + MapNode.heightBuffer);
    }

    /**
     * create an SVG subtree, contained inside <g></g> elements, for a
     * given node
     *
     * @param loadIcons tell me whether I should generate file://
     * references to load icons from the filesystem or generate URL
     * references.  "true" will generate file:// references for when
     * we are sending the output to the Batik Transcoder, while
     * "false" will generate http:// references for when we are
     * downloading the SVG directly into an external viewer.
     */

    private Element createHostElement(MapNode n, boolean loadIcons) {
        // a "g" element is just a container for other elements
        // we're "containing" each host inside of "g" elements
        Element host = this.document.createElementNS(this.namespace, "g");
        host.setAttributeNS(null, "id", n.getHostname());

        int x = n.getX();
        int y = n.getY();

        // try to get an OpenNMS CategoryModel so we can figure out
        // colors for our text and stuff
        CategoryModel cModel = null;
        double normalThreshold = 0.0;
        double warningThreshold = 0.0;

        try {
            cModel = CategoryModel.getInstance();
            normalThreshold = cModel.getCategoryNormalThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
            warningThreshold = cModel.getCategoryWarningThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
        } catch(Exception e) {
            log("Exception in DocumentGenerator.createHostElement()");
            log("Exception in CategoryModel.getInstance()");
            log(e.toString());
        }

        // create an "a" link reference for the host icon
        Element link = this.document.createElementNS(this.namespace, "a");
        link.setAttributeNS(null, "xlink:href", this.urlBase + "element/node.jsp?node=" + n.getNodeID());

        // create the icon for the host
        Element icon = this.document.createElementNS(this.namespace, "image");
        icon.setAttributeNS(null, "x", n.getIconX());
        icon.setAttributeNS(null, "y", n.getIconY());
        icon.setAttributeNS(null, "width", "40px");
        icon.setAttributeNS(null, "height", "40px");
        // icon.setAttributeNS(null, "xlink:href", "#" + n.getIconName());

        if(loadIcons) {
            // we're using the Transcoder, so generate filesystem references
            String path = "file://" + ctx.getRealPath("map/images/svg/" + n.getIconName() + ".svg");
            icon.setAttributeNS(null, "xlink:href", path);
        } else {
            // we're sending out SVG so generate http:// references
            icon.setAttributeNS(null, "xlink:href", this.urlBase + "map/images/svg/" + n.getIconName() + ".svg");
        }

        // create the hostname text for the host
        Element hostname = this.document.createElementNS(this.namespace, "text");
        hostname.setAttributeNS(null, "x", n.getHostnameX());
        hostname.setAttributeNS(null, "y", n.getHostnameY());
        hostname.setAttributeNS(null, "font-family", "sans-serif");
        hostname.setAttributeNS(null, "font-size", "12");
        hostname.setAttributeNS(null, "fill", "black");
        hostname.setAttributeNS(null, "style", "text-anchor: middle");
        org.w3c.dom.Node textString = this.document.createTextNode(n.getHostname());
        hostname.appendChild(textString);

        // create the RTC Value text for the host
        Element rtc = this.document.createElementNS(this.namespace, "text");
        rtc.setAttributeNS(null, "x", n.getRTCX());
        rtc.setAttributeNS(null, "y", n.getRTCY());
        rtc.setAttributeNS(null, "font-family", "sans-serif");
        rtc.setAttributeNS(null, "font-size", "12");

        if(cModel != null) {
            try {
                rtc.setAttributeNS(null,
                        "fill",
                        CategoryUtil.getCategoryColor(normalThreshold,
                                warningThreshold,
                                n.getRTC()));
            } catch(IOException e) {
                log("IOException in CategoryUtil.getCategoryColor");
                log(e.toString());
            } catch(org.exolab.castor.xml.MarshalException e) {
                log("org.exolab.castor.xml.MarshalException in CategoryUtil.getCategoryColor");
                log(e.toString());
            } catch(org.exolab.castor.xml.ValidationException e) {
                log("org.exolab.castor.xml.ValidationException in CategoryUtil.getCategoryColor");
                log(e.toString());
            }

        } else {
            rtc.setAttributeNS(null, "fill", "black");
        }

        rtc.setAttributeNS(null, "style", "text-anchor: middle");
        textString = this.document.createTextNode(CategoryUtil.formatValue(n.getRTC()) + " %");
        rtc.appendChild(textString);

        // create the status text for the host
        Element status = this.document.createElementNS(this.namespace, "text");
        status.setAttributeNS(null, "x", n.getStatusX());
        status.setAttributeNS(null, "y", n.getStatusY());
        status.setAttributeNS(null, "font-family", "sans-serif");
        status.setAttributeNS(null, "font-size", "12");
        status.setAttributeNS(null, "style", "text-anchor: middle");
        textString = this.document.createTextNode(n.getStatus());

        if(n.getStatus().equals("Up")) {
            status.setAttributeNS(null, "fill", "green");
        } else {
            status.setAttributeNS(null, "fill", "red");
        }

        status.appendChild(textString);

        // put the icon inside of the link element
        link.appendChild(icon);

        // append all the host elements inside of the g element
        host.appendChild(link);
        host.appendChild(hostname);
        host.appendChild(rtc);
        host.appendChild(status);

        return host;
    }


    /**
     * calculate the coordinates of each node in the map based on the map type
     */

    public void calculateHostCoordinates() {
        if(this.mapType.equals("tree")) {
            calculateTreeHostCoordinates();
        } else {
            calculateBoringHostCoordinates();
        }

        calculateDocumentWidthAndHeight();
    }

    /**
     * generate HTML that will output imagemap information for the map
     * that corresponds to the HostDocument created later.  This is
     * currently dependent on the NodeFactory generating the same
     * nodes with the same parent/child relationship since the
     * createJavascriptMap and createHostDocument each call it
     * independently.  It would be A Good Idea(tm) to call it
     * somewhere externally and, e.g. put it into a Session attribute
     * or something along those lines so we don't run the risk of
     * having the information change between method calls.  Will do
     * later...
     *
     * @param mapname the "name" of the HTML element that will have
     * the imagemap attached.  e.g. <map name="mapname">
     * @param uri the page and parameters that will be created for the
     * link for the given node.  e.g. "node.jsp?node=" will result in
     * a link to "[urlbase]/node.jsp?node=[nodeid]"
     */

    public String getImageMap(String mapname, String uri) {
        StringBuffer map = new StringBuffer();

        map.append("<map name=" + mapname + ">\n");
        Iterator i = nodes.iterator();
        while(i.hasNext()) {
            MapNode mn = (MapNode) i.next();
            map.append("<area shape=\"rect\" ");
            map.append("coords=");
            map.append(mn.getIconMinX() + ",");
            map.append(mn.getIconMinY() + ",");
            map.append(mn.getIconMaxX() + ",");
            map.append(mn.getIconMaxY() + " ");
            map.append("href=" + this.urlBase + uri + mn.getNodeID());
            map.append(">\n");
        }
        map.append("</map>\n");

        return map.toString();
    }


    /**
     * create a SVGDOMDocument containing all the elements for each
     * host including icon, hostname, IP address and status.
     *
     * @param loadIcons tell me whether I should load the icons from
     * the filesystem (true) or generate them as URLs (false)
     */

    public Document getHostDocument(boolean loadIcons) {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();

        this.namespace = SVGDOMImplementation.SVG_NAMESPACE_URI;
        this.document = impl.createDocument(this.namespace, "svg", null);

        Element root = this.document.getDocumentElement();

        if(this.mapType.equals("tree")) {
            drawLines((MapNode) this.nodes.get(0));
        }
        drawHosts(loadIcons);

        root.setAttributeNS(null, "width", new Integer(this.documentWidth).toString());
        root.setAttributeNS(null, "height", new Integer(this.documentHeight).toString());

        // put a datestamp in the SVG
        Element currtime = this.document.createElementNS(this.namespace, "text");
        currtime.setAttributeNS(null, "x", "10");
        currtime.setAttributeNS(null, "y", "16");
        currtime.setAttributeNS(null, "font-family", "sans-serif");
        currtime.setAttributeNS(null, "font-size", "12");
        currtime.setAttributeNS(null, "fill", "black");
        // currtime.setAttributeNS(null, "style", "text-anchor: right");
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss");
        org.w3c.dom.Node textString = this.document.createTextNode(df.format(new java.util.Date()));
        currtime.appendChild(textString);

        root.appendChild(currtime);

        Element rect = this.document.createElementNS(this.namespace, "rect");
        rect.setAttributeNS(null, "x", "0");
        rect.setAttributeNS(null, "y", "0");
        rect.setAttributeNS(null, "width", new Integer(this.documentWidth).toString());
        rect.setAttributeNS(null, "height", new Integer(this.documentHeight).toString());
        rect.setAttributeNS(null, "style", "fill:none;stroke:black;stroke-width:2");

        root.appendChild(rect);

        return this.document;
    }


    /**
     * get the width of the SVG created.  this is useful for embedding
     * the SVG in an HTML page.  you'll need to call getHostDocument
     * first so that there is a width and height to retrieve.
     */

    public int getDocumentWidth() {
        return this.documentWidth;
    }


    /**
     * get the height of the SVG created.  this is useful for
     * embedding the SVG in an HTML page.  you'll need to call
     * getHostDocument first so that there is a width and height to
     * retrieve.
     */

    public int getDocumentHeight() {
        return this.documentHeight;
    }

}

