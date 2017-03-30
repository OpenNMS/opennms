/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.web.api.Util;
import org.opennms.web.servlet.InitializerServletContextListener.RTCPostSubscriberTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>RTCPostServlet class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RTCPostServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(RTCPostServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = 5550051932055498432L;

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

            // Subscribe to all categories now that the servlet is initialized.
            //
            // This doesn't actually work because the backend will try to POST
            // RTC updates in the several milliseconds before the servlet can 
            // actually handle requests, resulting in {@link ConnectException} 
            // exceptions and no RTC data.
            // 
            //new RTCPostSubscriberTimerTask().run();
        } catch (MarshalException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        } catch (ValidationException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        } catch (IOException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // the path info will be the category name we need
        String pathInfo = request.getPathInfo();

        // send 400 Bad Request if they did not specify a category in the path
        // info
        if (pathInfo == null) {
            LOG.error("Request with no path info");
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

        try (ServletInputStream inStream = request.getInputStream();
                InputStreamReader isr = new InputStreamReader(inStream)) {
            org.opennms.netmgt.xml.rtc.EuiLevel level = JaxbUtils.unmarshal(org.opennms.netmgt.xml.rtc.EuiLevel.class, isr);

            // for now we only deal with the first category, they're only sent
            // one
            // at a time anyway
            category = level.getCategory().get(0);
        }

        // make sure we got data for the category we are interested in
        // send 400 Bad Request if they did not supply category information
        // for the categoryname in the path info
        if (!categoryName.equals(category.getCatlabel())) {
            LOG.error("Request did not supply information for category specified in path info");
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

        LOG.info("Successfully received information for {}", categoryName);
    }

}
