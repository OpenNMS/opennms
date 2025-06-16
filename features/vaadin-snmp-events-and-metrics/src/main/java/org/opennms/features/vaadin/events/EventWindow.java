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
