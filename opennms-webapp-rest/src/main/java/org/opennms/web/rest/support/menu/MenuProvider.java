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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import joptsimple.internal.Strings;
import org.opennms.web.rest.support.menu.xml.MenuXml;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class MenuProvider {
    private List<MenuEntry> navBarEntries;

    final private Resource dispatcherServletResource;

    public MenuProvider(Resource dispatcherServletResource) {
        this.dispatcherServletResource = dispatcherServletResource;
    }

    public List<TopMenuEntry> getMenu(final HttpServletRequest request) throws Exception, IOException {
        List<TopMenuEntry> menuEntries = null;

        try {
            File file = getFileFromResource();

            MenuXml.BeansElement xBeans = parseBeansXml(file);
            menuEntries = this.parseXmlToMenuEntries(xBeans);

        } catch (IOException ioe) {
            throw ioe;
        }

        return menuEntries;
    }

    private File getFileFromResource() throws IOException {
        File file = null;

        try {
            file = this.dispatcherServletResource.getFile();
            Assert.notNull(file, "config file must be set to a non-null value");
        } catch (IOException e) {
            String message = String.format("Could not find file object for 'dispatcher-servlet.xml' for resource '%s'",
                this.dispatcherServletResource);
            throw new IOException(message, e);
        }

        return file;
    }

    public MenuXml.BeansElement parseBeansXml(File file) {
        System.out.println("DEBUG in parseBeans");
        MenuXml.BeansElement xBeansElem = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MenuXml.BeansElement.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            xBeansElem = (MenuXml.BeansElement) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            String msg = e.getMessage();
            System.out.println("DEBUG parseBeans JAXBException: " + msg);
        }

        return xBeansElem;
    }

    private List<TopMenuEntry> parseXmlToMenuEntries(MenuXml.BeansElement xBeansElem) throws Exception {
        List<TopMenuEntry> topMenuEntries = new ArrayList<>();

        MenuXml.BeanElement xNavBarEntriesElem =
            xBeansElem.getBeans().stream()
                .filter(b -> b.getId() != null && b.getId().equals("navBarEntries"))
                .findFirst().orElse(null);

        if (xNavBarEntriesElem == null || xNavBarEntriesElem.getConstructorArgElement() == null) {
            throw new Exception("Could not find 'navBarEntries' item");)
        }

        for (var xTopLevelBean : xNavBarEntriesElem.getConstructorArgElement().getBeans()) {
            // Top level menu items, like "Info", "Status"
            TopMenuEntry topEntry = new TopMenuEntry();

            for (var prop : xTopLevelBean.getProperties()) {
                setBeanProperty(prop, "name", (s) -> topEntry.name = s);
                setBeanProperty(prop, "url", (s) -> topEntry.url = s);
                setBeanProperty(prop, "locationMatch", (s) -> topEntry.locationMatch = s);
            }

            if (!Strings.isNullOrEmpty(topEntry.name) && !Strings.isNullOrEmpty(topEntry.url)) {
                topMenuEntries.add(topEntry);

                var ctorArgs = xTopLevelBean.getConstructorArgElement();

                if (ctorArgs != null && ctorArgs.getBeans() != null) {
                    for (var xBean : ctorArgs.getBeans()) {
                        MenuEntry menuEntry = new MenuEntry();

                        for (var prop : xBean.getProperties()) {
                            setBeanProperty(prop, "name", (s) -> menuEntry.name = s);
                            setBeanProperty(prop, "url", (s) -> menuEntry.url = s);
                            setBeanProperty(prop, "locationMatch", (s) -> menuEntry.locationMatch = s);
                        }

                        if (!Strings.isNullOrEmpty(menuEntry.name) && !Strings.isNullOrEmpty(menuEntry.url)) {
                            topEntry.addItem(menuEntry);
                        }
                    }
                }
            }
        }

        return topMenuEntries;
    }

    private void setBeanProperty(MenuXml.BeanPropertyElement propElem, String name, Consumer<String> consumer) {
        if (propElem.getName() != null && propElem.getName().equals(name)) {
            consumer.accept(propElem.getValue());
        }
    }
}
