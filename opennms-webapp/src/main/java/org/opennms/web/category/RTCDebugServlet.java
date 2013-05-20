/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
 * @since 1.8.1
 */
public class RTCDebugServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 2861682556760650660L;
    protected CategoryModel model;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
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
    @Override
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
