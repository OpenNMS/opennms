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
package org.opennms.web.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Sends the URL of the removed KSC view page, {@code KSC/customView.htm}, to the Graph
 * Collections view of the Vue UI, so bookmarks and links from before the move keep working.
 * The {@code type} and {@code report} parameters pick the collection as they did before, and a
 * {@code timespan} or {@code graphtype} override is carried over. Anything unrecognized lands
 * on the list. {@code KSC/index.jsp} redirects on its own.
 */
public class GraphCollectionRedirectController extends AbstractController {

    static final String LIST = "/ui/index.html#/graph-collections";

    private static final String[] OVERRIDES = { "timespan", "graphtype" };

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new RedirectView(targetFor(request), true));
    }

    /** The context-relative URL of the Vue page that shows what the legacy request asked for. */
    static String targetFor(final HttpServletRequest request) {
        final String type = param(request, "type");
        final String report = param(request, "report");
        String path = LIST;
        if (report != null) {
            if ("custom".equals(type) && report.matches("\\d+")) {
                path = LIST + "/" + report;
            } else if ("node".equals(type) && report.matches("[1-9]\\d*")) {
                path = LIST + "/node/" + report;
            } else if ("nodeSource".equals(type)) {
                path = LIST + "/nodeSource/" + encode(report);
            } else if ("domain".equals(type)) {
                path = LIST + "/domain/" + encode(report);
            }
        }
        final StringBuilder query = new StringBuilder();
        for (final String name : OVERRIDES) {
            final String value = param(request, name);
            // the legacy page spelled "no override" both ways
            if (value != null && !"none".equals(value) && !"null".equals(value)) {
                query.append(query.length() == 0 ? '?' : '&').append(name).append('=').append(encode(value));
            }
        }
        return path + query;
    }

    private static String param(final HttpServletRequest request, final String name) {
        final String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
