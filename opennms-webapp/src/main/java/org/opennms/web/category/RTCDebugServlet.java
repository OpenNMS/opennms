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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.xml.JaxbUtils;

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
            JaxbUtils.marshal(category.getRtcCategory(), out);
        }

        out.close();
    }

}
