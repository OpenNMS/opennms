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
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
// 2006 Aug 22: Better error reporting when we can't parse XML. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.category;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.web.api.Util;

/**
 * <p>RTCPostServlet class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RTCPostServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected CategoryModel model;

    protected ThreadCategory log = ThreadCategory.getInstance("RTC");

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        try {
            this.model = CategoryModel.getInstance();
        } catch (IOException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        } catch (MarshalException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        } catch (ValidationException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
    }

    /** {@inheritDoc} */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // the path info will be the category name we need
        String pathInfo = request.getPathInfo();

        // send 400 Bad Request if they did not specify a category in the path
        // info
        if (pathInfo == null) {
            this.log.error("Request with no path info");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No Category name given in path");
            return;
        }

        // remove the preceding slash if present
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1, pathInfo.length());
        }

        // since these category names can contain spaces, etc,
        // we have to URL encode them in the URL
        String categoryName = Util.decode(pathInfo);

        org.opennms.netmgt.xml.rtc.Category category = null;

        try {
            ServletInputStream inStream = request.getInputStream();

            // note the unmarshaller closes the input stream, so don't try to
            // close
            // it again or the servlet container will complain
            org.opennms.netmgt.xml.rtc.EuiLevel level = CastorUtils.unmarshal(org.opennms.netmgt.xml.rtc.EuiLevel.class, inStream);

            // for now we only deal with the first category, they're only sent
            // one
            // at a time anyway
            category = level.getCategory(0);
        } catch (MarshalException ex) {
            this.log.error("Failed to load configuration", ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid XML input: MarshalException: " + ex.getMessage());
            return;
        } catch (ValidationException ex) {
            this.log.error("Failed to load configuration", ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid XML input: ValidationException" + ex.getMessage());
            return;
        }

        // make sure we got data for the category we are interested in
        // send 400 Bad Request if they did not supply category information
        // for the categoryname in the path info
        if (!categoryName.equals(category.getCatlabel())) {
            this.log.error("Request did not supply information for category specified in path info");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No category info found for " + categoryName);
            return;
        }

        // update the category information in the CategoryModel
        this.model.updateCategory(category);

        // return a success message
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("Category data parsed successfully.");
        out.close();

        this.log.info("Successfully received information for " + categoryName);
    }

}
