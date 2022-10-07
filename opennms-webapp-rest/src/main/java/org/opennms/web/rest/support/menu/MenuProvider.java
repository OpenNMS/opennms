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
import java.util.Optional;
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
        MenuXml.BeansElement xBeansElem = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MenuXml.BeansElement.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            xBeansElem = (MenuXml.BeansElement) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            String msg = e.getMessage();
        }

        return xBeansElem;
    }

    public List<TopMenuEntry> parseXmlToMenuEntries(MenuXml.BeansElement xBeansElem) throws Exception {
        List<TopMenuEntry> topMenuEntries = new ArrayList<>();

        Optional<MenuXml.BeanElement> xNavBarEntriesElem =
            xBeansElem.getBeans().stream()
                .filter(b -> b.getId() != null && b.getId().equals("navBarEntries"))
                .findFirst();

        if (!xNavBarEntriesElem.isPresent() || xNavBarEntriesElem.get().getConstructorArgElement() == null) {
            throw new Exception("Could not find 'navBarEntries' item");
        }

        List<MenuXml.BeanOrRefElement> xBeansOrRefs = xNavBarEntriesElem.get().getConstructorArgElement().getBeansOrRefs();

        for (MenuXml.BeanOrRefElement xTopLevelBeanOrRef : xBeansOrRefs) {
            Optional<TopMenuEntry> topEntry = Optional.empty();

            if (xTopLevelBeanOrRef instanceof MenuXml.BeanElement) {
                topEntry = parseTopMenuEntry((MenuXml.BeanElement) xTopLevelBeanOrRef);
            } else if (xTopLevelBeanOrRef instanceof MenuXml.BeanRefElement) {
                topEntry = parseTopMenuEntryFromRef((MenuXml.BeanRefElement) xTopLevelBeanOrRef, xBeansElem);
            }

            topEntry.ifPresent(topMenuEntries::add);
        }

        return topMenuEntries;
    }

    private Optional<TopMenuEntry> parseTopMenuEntry(MenuXml.BeanElement xTopLevelBean) {
        // Top level menu items, like "Info", "Status"
        TopMenuEntry topEntry = new TopMenuEntry();
        topEntry.id = xTopLevelBean.getId();
        topEntry.className = xTopLevelBean.getClassName();

        for (var prop : xTopLevelBean.getProperties()) {
            setFromBeanProperty(prop, "name", (s) -> topEntry.name = s);
            setFromBeanProperty(prop, "url", (s) -> topEntry.url = s);
            setFromBeanProperty(prop, "locationMatch", (s) -> topEntry.locationMatch = s);
        }

        boolean isValid = false;

        if (!Strings.isNullOrEmpty(topEntry.name) && !Strings.isNullOrEmpty(topEntry.url)) {
            isValid = true;

            MenuXml.BeanPropertyElement xEntries =
                xTopLevelBean.getProperties().stream()
                    .filter(p -> !Strings.isNullOrEmpty(p.getName()) && p.getName().equals("entries"))
                    .findFirst().orElse(null);

            if (xEntries != null) {
                for (var xBean : xEntries.getBeans()) {
                    MenuEntry menuEntry = new MenuEntry();
                    menuEntry.id = xBean.getId();
                    menuEntry.className = xBean.getClassName();

                    for (var prop : xBean.getProperties()) {
                        setFromBeanProperty(prop, "name", (s) -> menuEntry.name = s);
                        setFromBeanProperty(prop, "url", (s) -> menuEntry.url = s);
                        setFromBeanProperty(prop, "locationMatch", (s) -> menuEntry.locationMatch = s);
                    }

                    if (!Strings.isNullOrEmpty(menuEntry.name) && !Strings.isNullOrEmpty(menuEntry.url)) {
                        topEntry.addItem(menuEntry);
                    }
                }
            }
        }

        return isValid ? Optional.of(topEntry) : Optional.empty();
    }

    private Optional<TopMenuEntry> parseTopMenuEntryFromRef(MenuXml.BeanRefElement xBeanRefElement, MenuXml.BeansElement xBeansElem) {
        String refName = xBeanRefElement.getBeanRef();

        Optional<MenuXml.BeanElement> xBean =
            xBeansElem.getBeans().stream()
                .filter(b -> b.getName() != null && b.getName().equals(refName))
                .findFirst();

        if (xBean.isPresent()) {
            return parseTopMenuEntry(xBean.get());
        }

        return Optional.empty();
    }


    private void setFromBeanProperty(MenuXml.BeanPropertyElement propElem, String name, Consumer<String> consumer) {
        if (propElem.getName() != null && propElem.getName().equals(name)) {
            consumer.accept(propElem.getValue());
        }
    }
}
