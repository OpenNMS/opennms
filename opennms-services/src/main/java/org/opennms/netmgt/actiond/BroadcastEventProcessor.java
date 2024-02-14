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
package org.opennms.netmgt.actiond;

import java.util.Enumeration;
import java.util.Queue;

import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IAutoAction;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class BroadcastEventProcessor implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);
    /**
     * The location where executable events are enqueued to be executed.
     */
    private final Queue<String> m_execQ;

    /**
     * This constructor subscribes to eventd for all events
     * 
     * @param execQ
     *            The queue where executable events are stored.
     * 
     */
    BroadcastEventProcessor(Queue<String> execQ) {
        // set up the exectuable queue first
        //
        m_execQ = execQ;

        // subscribe for all events
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this);

    }

    /**
     * Unsubscribe from eventd
     */
    public synchronized void close() {
        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each event's autoactions and trouble tickets
     * are queued to be run
     */
    @Override
    public void onEvent(IEvent event) {

        if (event == null) {
            return;
        }

        // Handle autoactions
        //
        Enumeration<IAutoAction> walker = event.enumerateAutoaction();
        while (walker.hasMoreElements()) {
            IAutoAction aact = walker.nextElement();
            if ("on".equalsIgnoreCase(aact.getState())) {
                m_execQ.add(aact.getContent()); // java.lang.String
            }

            LOG.debug("Added event \'{}\' to execute autoaction \'{}\'", event.getUei(), aact.getContent());
        }

        // Handle trouble tickets
        //
        if (event.getTticket() != null && event.getTticket().getState().equalsIgnoreCase("on")) {
            m_execQ.add(event.getTticket().getContent()); // java.lang.String
            LOG.debug("Added event \'{}\' to execute tticket \'{}\'", event.getUei(), event.getTticket().getContent());
        }

    } // end onMessage()

    /**
     * Return an id for this event listener
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "Actiond:BroadcastEventProcessor";
    }

} // end class
