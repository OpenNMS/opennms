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
package org.opennms.web.category;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.web.api.Util;
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
