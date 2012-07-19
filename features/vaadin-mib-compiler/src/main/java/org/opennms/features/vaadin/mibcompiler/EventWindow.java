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

import java.io.StringWriter;

import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

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
     * @param caption the caption
     * @param events the OpenNMS Events
     * @param logger the logger
     */
    public EventWindow(final String caption, final Events events, final Logger logger) {
        super(caption);
        setScrollable(true);
        setModal(false);
        setClosable(false);
        setDraggable(false);
        setResizable(false);
        addStyleName(Runo.WINDOW_DIALOG);
        setSizeFull();
        setContent(new EventPanel(events, logger) {
            void cancelProcessing() {
                close();
            }
            void generateEventFile(Events events) {
                processEvents(events, logger);
                close();
            }
        });
    }

    /**
     * Process events.
     *
     * @param events the OpenNMS Events
     * @param logger the logger
     */
    /*
     * FIXME This is just for testing purposes
     * TODO  Pending processing
     * 1. Verify that the events doesn't exist on eventconf. If they exists, request if you want to override events.
     * 2. Save the file on /opt/opennms/etc/events
     * 3. Add a link (depending on step 1) on eventconf.xml
     */
    public void processEvents(final Events events, final Logger logger) {
        StringWriter writer = new StringWriter();
        try {
            events.marshal(writer);
            logger.debug("The XML file:<pre>" + writer.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</pre>");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
