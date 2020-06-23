/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.events;

import java.io.File;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.ui.Window;

/**
 * The Class Event Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class EventWindow extends Window {

    /**
     * Instantiates a new events window.
     *
     * @param eventConfDao the OpenNMS Events Configuration DAO
     * @param eventProxy the OpenNMS Events Proxy
     * @param eventFile the events file
     * @param events the OpenNMS events object
     * @param logger the logger object
     * @throws Exception the exception
     */
    public EventWindow(final EventConfDao eventConfDao, final EventProxy eventProxy, final File eventFile, final Events events, final Logger logger) {
        super(eventFile.getAbsolutePath()); // Using fileName for as the window's name.
        setModal(false);
        setClosable(false);
        setDraggable(false);
        setResizable(false);
        addStyleName("dialog");
        setSizeFull();
        setContent(new EventPanel(eventConfDao, eventProxy, eventFile, events, logger) {
            @Override
            public void cancel() {
                close();
            }
            @Override
            public void success() {
                close();
            }
            @Override
            public void failure(String reason) {
                close();
            }
        });
    }

}
