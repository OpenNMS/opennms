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
package org.opennms.netmgt.scriptd;

import org.opennms.netmgt.config.ScriptdConfigFactory;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class receives all events on behalf of the <em>Scriptd</em> service.
 * All events are placed on a queue, so they can be handled by the "Executor"
 * (this allows the Executor to pause and resume without losing events).
 * 
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class BroadcastEventProcessor implements AutoCloseable, EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);

    /**
     * The location where executable events are enqueued to be executed.
     */
    private final Executor m_executor;

    /**
     * This constructor subscribes to eventd for all events
     * 
     * @param executor Executor that runs Scriptd tasks
     * 
     */
    BroadcastEventProcessor(Executor executor) {
        // set up the executable queue first
        m_executor = executor;

        // do we need to run at all?
        ScriptdConfigFactory aFactory = ScriptdConfigFactory.getInstance();
        if(aFactory.getEventScripts().isEmpty() && aFactory.getStopScripts().isEmpty()
            && aFactory.getStartScripts().isEmpty() && aFactory.getReloadScripts().isEmpty()) {
            LOG.debug("No scriptd scripts are configured; not subscribing to events");
            return;
        }

        // subscribe for all events
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this);
    }

    /**
     * Close the BroadcastEventProcessor
     */
    @Override
    public synchronized void close() {
        // unsubscribe all events
        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each event is queued for handling by the
     * Executor.
     */
    @Override
    public void onEvent(IEvent event) {
        if (event == null) {
            return;
        }

        m_executor.addTask(event);

        LOG.debug("Added event \'{}\' to scriptd execution queue.", event.getUei());

    } // end onEvent()

    /**
     * Return an id for this event listener
     *
     * @return The ID of this event listener.
     */
    @Override
    public String getName() {
        return "Scriptd:BroadcastEventProcessor";
    }

} // end class
