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
package org.opennms.web.element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.api.ElementConstants;
import org.opennms.web.api.Util;
import org.springframework.web.util.UriUtils;

/**
 * Serves the legacy node page URL, {@code element/node.jsp?node=<id|foreignSource:foreignId>},
 * as a redirect to the Vue Node Details page.
 * <p>
 * Keep this URL. Links outside the web UI use it: RSS, emails, chat, bookmarks and the event text
 * stored in the database. It also needs no {@code #fragment}, which the browser does not send to
 * the server, so a client that is not a browser can follow it.
 * <p>
 * The node is not looked up here. The Vue page resolves a foreign id and shows its own
 * "invalid node id" and "node not found" states.
 */
public class NodeDetailsRedirectServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static final String NODE_LIST_URI = "ui/index.html#/nodes";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.sendRedirect(Util.calculateUrlBase(request, getTargetPath(request.getParameter("node"))));
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    static String getTargetPath(final String node) {
        if (node == null || node.isBlank()) {
            return NODE_LIST_URI;
        }
        // One path segment of the hash route: '/', '#', '?', '%' and spaces are encoded, ':' is not.
        return ElementConstants.NODE_DETAILS_URI + UriUtils.encodePathSegment(node, StandardCharsets.UTF_8);
    }
}
