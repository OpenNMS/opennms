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

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.batik.swing.*;
import org.apache.batik.svggen.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.dom.util.*;
import org.apache.batik.transcoder.image.*;
import org.apache.batik.transcoder.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;

import org.opennms.web.map.*;

/**
 * This class should be called from inside of an <embed> tag.  We
 * generate and emit an SVG document of the tree map of nodes.  (this
 * used to be svg.jsp but it's neater to put it into a servlet where
 * it really belongs.
 *
 * @author <A HREF="mailto:dglidden@opennms.org">Derek Glidden</A>
 * @author <A HREF="http://www.nksi.com/">NKSi</A>
 */

public class SVGServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException {

	// the docbase to which all our elements will be relative
	String base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";

	response.setContentType("image/svg+xml");

	try {
	    // create the object that will make our SVG for us
	    // DocumentGenerator docgen = new DocumentGenerator();
	    // we should find this in our HttpSession object
	    DocumentGenerator docgen = (DocumentGenerator)request.getSession().getAttribute("docgen");

	    // pass the servlet context so the DocumentGenerator can find its icons
	    // these should be set from the jsp page now
	    // ServletContext ctx = getServletContext();
	    // docgen.setServletContext(ctx);
	    // docgen.setNodes(nodes);
	    // docgen.setUrlBase(base); 

	    // generate and retrieve the SVG DOM we're generating
	    Document doc = docgen.getHostDocument(false);

	    // get the PrintWriter we'll use to output the SVG
	    PrintWriter docwriter = response.getWriter();

	    // send the SVG to the other end
	    DOMUtilities.writeDocument(doc, docwriter);

	    // flush and close
	    docwriter.flush();
	    docwriter.close();
	}
	catch(IOException e) {
	    log("IOException in SVGServlet");
	    log(e.toString());
	}
	catch(Exception e) {
	    log("Exception in SVGServlet");
	    log(e.toString());
	}

    }

}
