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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

public class MenuTemplatePathsTest {
    private String savedHome;

    @Before
    public void setHome() {
        savedHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", "/test/opennms");
    }

    @After
    public void restoreHome() {
        if (savedHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", savedHome);
        }
    }

    private static MockServletContext withRealWebInf(String webInf) {
        return new MockServletContext() {
            @Override public String getRealPath(String path) { return "/WEB-INF".equals(path) ? webInf : null; }
        };
    }

    @Test
    public void usesTheServletContextWebInf() {
        assertEquals("/deployed/opennms/WEB-INF/menu/menu-template.json",
                MenuTemplatePaths.resolve(withRealWebInf("/deployed/opennms/WEB-INF")));
    }

    @Test
    public void fallsBackToOpennmsHomeWithoutARealPath() {
        assertEquals("/test/opennms/jetty-webapps/opennms/WEB-INF/menu/menu-template.json",
                MenuTemplatePaths.resolve(withRealWebInf(null)));
    }

    @Test
    public void fallsBackToOpennmsHomeWithoutAServletContext() {
        assertEquals("/test/opennms/jetty-webapps/opennms/WEB-INF/menu/menu-template.json",
                MenuTemplatePaths.resolve(null));
    }
}
