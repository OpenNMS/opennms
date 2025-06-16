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

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.web.rest.support.menu.xml.MenuXml;

public class MenuProviderTest {
    final static String RESOURCE_PATH = "src/test/resources/dispatcher-servlet.xml";

    @Test
    public void testParseBeansXml() {
        MenuProvider provider = new MenuProvider(getResourcePath());
        MenuXml.BeansElement xBeansElem = null;

        try (var inputStream = new FileInputStream(getResourcePath())) {
            xBeansElem = provider.parseDispatcherServletXml(inputStream);
        } catch (Exception e) {
            Assert.fail("Could not open file resource: " + e.getMessage());
        }

        Assert.assertNotNull(xBeansElem);

        List<MenuXml.BeanElement> topLevelBeans = xBeansElem.getBeans();

        Optional<MenuXml.BeanElement> navBarBean = topLevelBeans.stream()
            .filter(e -> e.getId() != null && e.getId().equals("navBarEntries"))
            .findFirst();

        if (navBarBean.isPresent()) {
            List<TopMenuEntry> topMenuEntries = null;

            try {
                topMenuEntries = provider.parseXmlToMenuEntries(xBeansElem);
            } catch (Exception e) {
                Assert.fail("Error parsing XML to MenuEntries: " + e.getMessage());
            }
        }

        Assert.assertTrue(topLevelBeans.size() > 0);
    }

    @Test
    public void testParseMainMenu() {
        MainMenu mainMenu = null;
        MenuRequestContext context = new TestMenuRequestContext();

        try {
            MenuProvider provider = new MenuProvider(getResourcePath());
            mainMenu = provider.getMainMenu(context);
        } catch (Exception e) {
            Assert.fail("Error in MenuProvider.getMainMenu: " + e.getMessage());
        }
    }

    private String getResourcePath() {
        Path p = Paths.get(RESOURCE_PATH);
        return p.toFile().getAbsolutePath();
    }

    public static class TestMenuRequestContext implements MenuRequestContext {
        public String getRemoteUser() {
            return "admin1";
        }

        public String calculateUrlBase() {
            return "opennms/";
        }

        public boolean isUserInRole(String role) {
            return true;
        }

        public String getFormattedTime() {
            return "2022-10-11T20:30:00.000Z";
        }

        public String getNoticeStatus() {
            return "On";
        }
    }
}
