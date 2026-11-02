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
 * Resolves the configured menu template path against the deployed webapp.
 */
public final class MenuTemplatePaths {
    private static final String WEB_INF_PREFIX = "/WEB-INF";
    private static final String HOME_VARIABLE = "${opennms.home}";

    private MenuTemplatePaths() {
    }

    /**
     * If the path is of the form "${opennms.home}.../WEB-INF/...", re-root it at the servlet
     * context's real WEB-INF directory. Otherwise return it unchanged.
     */
    public static String resolve(String configuredPath, ServletContext servletContext) {
        if (configuredPath != null && configuredPath.contains(HOME_VARIABLE)) {
            int index = configuredPath.indexOf(WEB_INF_PREFIX);

            if (index >= 0) {
                String webInfRealPath = servletContext.getRealPath(WEB_INF_PREFIX);
                return webInfRealPath + configuredPath.substring(index + WEB_INF_PREFIX.length());
            }
        }

        return configuredPath;
    }
}
