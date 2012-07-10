/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler;

import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.FileSystemResource;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;

/**
 * The Class MIB Compiler Application.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibCompilerApplication extends Application {

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        setTheme(Runo.THEME_NAME);
        Window mainWindow = new Window("MIB Compiler Application", new MibCompilerPanel());
        setMainWindow(mainWindow);
    }

    
    // TODO This method is for testing purposes only
    /**
     * Gets the OpenNMS events.
     *
     * @return the OpenNMS events
     */
    public void displayEVentPanelWithSampleData() {
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        eventConfDao.setConfigResource(new FileSystemResource("src/test/resources/Cisco2.events.xml"));
        eventConfDao.afterPropertiesSet();
        Events events = new Events();
        events.setEvent(eventConfDao.getEventsByLabel());
        setMainWindow(new Window("Testing EventPanel", new EventPanel(events) {
            void cancelProcessing() {
                getApplication().getMainWindow().showNotification("Comming soon!");
            }
            void generateEventFile() {
                getApplication().getMainWindow().showNotification("Comming soon!");
            }
        }));
    }

}
