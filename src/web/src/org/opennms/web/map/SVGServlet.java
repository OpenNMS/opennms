//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.Document;

/**
 * This class should be called from inside of an <embed>tag.  We
 * generate and emit an SVG document of the tree map of nodes.  (this
 * used to be svg.jsp but it's neater to put it into a servlet where
 * it really belongs.
 *
 * @author <A HREF="mailto:dglidden@opennms.org">Derek Glidden</A>
 * @author <A HREF="http://www.nksi.com/">NKSi</A>
 */

public class SVGServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        response.setContentType("image/svg+xml");

        try {
            // get the object that will make our SVG for us
            // we should find this in our HttpSession object
            DocumentGenerator docgen = (DocumentGenerator) request.getSession().getAttribute("docgen");

            // generate and retrieve the SVG DOM we're generating
            Document doc = docgen.getHostDocument(false);

            // get the PrintWriter we'll use to output the SVG
            PrintWriter docwriter = response.getWriter();

            // send the SVG to the other end
            DOMUtilities.writeDocument(doc, docwriter);

            // flush and close
            docwriter.flush();
            docwriter.close();
        } catch (IOException e) {
            log("IOException in SVGServlet");
            log(e.toString());
        } catch (Exception e) {
            log("Exception in SVGServlet");
            log(e.toString());
        }

    }

}
