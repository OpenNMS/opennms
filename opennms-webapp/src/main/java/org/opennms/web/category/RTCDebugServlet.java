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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

/**
 * <p>RTCDebugServlet class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class RTCDebugServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected CategoryModel model;

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryName = request.getParameter("category");

        if (categoryName == null) {
            categoryName = CategoryModel.OVERALL_AVAILABILITY_CATEGORY;
        }

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        Category category = model.getCategory(categoryName);

        if (category == null) {
            out.write("No data exists for this category.  Please check your spelling of the category name.");
        } else {
            try {
                Marshaller.marshal(category.getRtcCategory(), out);
            } catch (MarshalException e) {
                throw new ServletException("Could not marshal the RTC info", e);
            } catch (ValidationException e) {
                throw new ServletException("Could not marshal the RTC info", e);
            }
        }

        out.close();
    }

}
