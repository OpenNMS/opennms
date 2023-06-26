/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

