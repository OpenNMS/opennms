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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.web.rest.support.menu.xml.MenuXml;
import org.springframework.core.io.InputStreamSource;

public class MenuProviderTest {
    final static String RESOURCE_PATH = "file:{opennms.home}/jetty-webapps/opennms/WEB-INF/dispatcher-servlet.xml";

    @Test
    public void testParseBeansXml() {
        MenuProvider provider = new MenuProvider(null);
        MenuXml.BeansElement xBeansElem = null;

        try (var inputStream = new FileInputStream(RESOURCE_PATH)) {
            xBeansElem = provider.parseDispatcherServletXml(inputStream);
        } catch (Exception e) {
            Assert.fail("Could not open file resource: " + e.getMessage());
        }

        Assert.assertNotNull(xBeansElem);

        List<MenuXml.BeanElement> topLevelBeans = xBeansElem.getBeans();

        Optional<MenuXml.BeanElement> navBarBean = topLevelBeans.stream()
            .filter(e -> e.getId() != null && e.getId().equals("navBarEntries"))
            .findFirst();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (navBarBean.isPresent()) {
            System.out.println("DEBUG found 'navBarEntries' bean:");

            String json = gson.toJson(navBarBean.get());
            System.out.println(json);

            List<TopMenuEntry> topMenuEntries = null;

            try {
                topMenuEntries = provider.parseXmlToMenuEntries(xBeansElem);
            } catch (Exception e) {
                Assert.fail("Error parsing XML to MenuEntries: " + e.getMessage());
            }

            System.out.println("Parsed xml -> menu entries:");
            json = gson.toJson(topMenuEntries);
            System.out.println(json);
        }

        Assert.assertTrue(topLevelBeans.size() > 0);
    }

    @Test
    public void testParseMainMenu() {
        MainMenu mainMenu = null;
        MenuRequestContext context = new TestMenuRequestContext();

        try (var inputStreamSource = new TestInputStreamSource(RESOURCE_PATH)) {
            MenuProvider provider = new MenuProvider(inputStreamSource);

            mainMenu = provider.getMainMenu(context);
        } catch (Exception e) {
            Assert.fail("Error in MenuProvider.getMainMenu: " + e.getMessage());
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Parsed MainMenu:");
        String json = gson.toJson(mainMenu);
        System.out.println(json);
    }

    public static class TestInputStreamSource implements InputStreamSource, AutoCloseable {
        private FileInputStream inputStream;

        public TestInputStreamSource(String resourcePath) throws FileNotFoundException {
            this.inputStream = new FileInputStream(resourcePath);
        }
        @Override
        public void close() {
            if (this.inputStream != null) {
                try {
                    this.inputStream.close();
                } catch (IOException ignored) {
                } finally {
                    this.inputStream = null;
                }
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.inputStream;
        }
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
