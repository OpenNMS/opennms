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
package org.opennms.web.rest.support.menu;

import javax.servlet.ServletContext;

/**
 * The one place that knows where the active menu template lives. Both the side menu
 * (MenuRestService) and the search "Open ..." results (TemplateMenuProvider) read it from here,
 * so they can never read different files.
 */
public final class MenuTemplatePaths {
    private static final String WEB_INF = "/WEB-INF";

    /** The active menu template, relative to the webapp's WEB-INF directory. */
    public static final String TEMPLATE_IN_WEB_INF = "/menu/menu-template.json";

    private MenuTemplatePaths() {
    }

    /**
     * The active menu template of the deployed webapp: under the servlet context's real WEB-INF
     * directory, or under ${opennms.home}/jetty-webapps/opennms/WEB-INF when there is no servlet
     * context or it has no real path (e.g. an unexpanded WAR).
     */
    public static String resolve(ServletContext servletContext) {
        final String webInf = servletContext != null ? servletContext.getRealPath(WEB_INF) : null;

        if (webInf != null) {
            return webInf + TEMPLATE_IN_WEB_INF;
        }

        return System.getProperty("opennms.home", "/opt/opennms") + "/jetty-webapps/opennms" + WEB_INF + TEMPLATE_IN_WEB_INF;
    }
}
