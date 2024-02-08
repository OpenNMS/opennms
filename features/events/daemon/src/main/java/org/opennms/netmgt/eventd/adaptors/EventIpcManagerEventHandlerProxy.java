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
package org.opennms.netmgt.eventd.adaptors;

import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>EventIpcManagerEventHandlerProxy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EventIpcManagerEventHandlerProxy implements EventHandler, InitializingBean {
    private EventIpcManager m_eventIpcManager;

    /**
     * <p>Constructor for EventIpcManagerEventHandlerProxy.</p>
     */
    public EventIpcManagerEventHandlerProxy() {
    }

    /** {@inheritDoc} */
    @Override
    public boolean processEvent(Event event) {
        m_eventIpcManager.sendNow(event);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void receiptSent(EventReceipt event) {
        // do nothing
    }

    /**
     * <p>getEventIpcManager</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_eventIpcManager != null, "property eventIpcManager must be set");
    }
}
