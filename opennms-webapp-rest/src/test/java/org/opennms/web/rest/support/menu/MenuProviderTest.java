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

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.web.rest.support.menu.xml.MenuXml;

public class MenuProviderTest {
    final static String RESOURCE_PATH = "file:{opennms.home}/jetty-webapps/opennms/WEB-INF/dispatcher-servlet.xml";

    @Test
    public void testParseBeansXml() {
        System.out.println("DEBUG In testParseBeansXml");
        File file;

        try {
            file = new File(RESOURCE_PATH);
        } catch (Exception e) {
            Assert.fail("Could not open file: " + e.getMessage());
            return;
        }

        MenuProvider provider = new MenuProvider(null);
        MenuXml.BeansElement topBeans = provider.parseBeansXml(file);
        System.out.println("DEBUG parsed beans");

        Assert.assertNotNull(topBeans);
        System.out.println("DEBUG topBeans not null");

        List<MenuXml.BeanElement> topLevelBeans = topBeans.getBeans();
        System.out.println("DEBUG topLevelBeans size: " + topLevelBeans.size());


        Optional<MenuXml.BeanElement> navBarBean = topLevelBeans.stream()
            .filter(e -> e.getId() != null && e.getId().equals("navBarEntries"))
            .findFirst();

        if (navBarBean.isPresent()) {
            System.out.println("DEBUG found 'navBarEntries' bean");

            var c = navBarBean.get().getConstructorArgElement();
            var topLevelMenuBeans = c != null ? c.getBeans() : null;

            if (topLevelMenuBeans != null) {
                System.out.println("Found topLevelMenuBeans: " + topLevelMenuBeans.size());

                for (var bean : topLevelMenuBeans) {
                    var properties = bean.getProperties();

                    if (properties != null) {
                        System.out.println("Found properties: " + properties.size());

                        for (var prop : properties) {
                            String msg = String.format("prop.name: %s, value: %s",
                                prop.getName(), prop.getValue());
                            System.out.println(msg);

                            var menuEntryBeans = prop.getBeans();

                            if (menuEntryBeans != null && !menuEntryBeans.isEmpty()) {
                                System.out.println("DEBUG found menuEntry beans: " + menuEntryBeans.size());

                                for (var menuEntry : menuEntryBeans) {
                                    System.out.println("  MenuEntry bean:");
                                    var props = menuEntry.getProperties();

                                    for (var p : props) {
                                        String propMsg = String.format("    MenuEntry name: %s, value: %s",
                                            p.getName(), p.getValue());
                                        System.out.println(propMsg);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Assert.assertTrue(topLevelBeans.size() > 0);
    }
}
