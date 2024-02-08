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
package org.opennms.netmgt.rtc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BroadcastEventProcessor is responsible for receiving events from eventd and
 * queuing them to the data updaters.
 * 
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
@EventListener(name="RTC:BroadcastEventProcessor", logPrefix="rtc")
public class BroadcastEventProcessor implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);

    /**
     * The location where incoming events of interest are enqueued
     */
    private ExecutorService m_updater;

    @Autowired
    private DataManager m_dataManager;

    @Autowired
    private RTCConfigFactory m_configFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        m_updater = Executors.newFixedThreadPool(
            m_configFactory.getUpdaters(),
            new LogPreservingThreadFactory(getClass().getSimpleName(), m_configFactory.getUpdaters())
        );
    }

    @EventHandler(ueis={
        // add the outageCreated event
        EventConstants.OUTAGE_CREATED_EVENT_UEI,
        // add the outageResolved event
        EventConstants.OUTAGE_RESOLVED_EVENT_UEI,
        // add the nodeGainedService event
        EventConstants.NODE_GAINED_SERVICE_EVENT_UEI,
        // add the nodeCategoryMembershipChanged event
        EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI,
        // add the interfaceUp event
        EventConstants.SERVICE_DELETED_EVENT_UEI,
        // add the serviceDeleted event
        EventConstants.SERVICE_UNMANAGED_EVENT_UEI,
        // add the interfaceReparented event
        EventConstants.INTERFACE_REPARENTED_EVENT_UEI,
        // add the asset info changed event
        EventConstants.ASSET_INFO_CHANGED_EVENT_UEI
    })
    public void onEvent(IEvent event) {
        if (event == null) {
            return;
        }

        LOG.debug("About to start processing recd. event");

        try {

            String uei = event.getUei();
            if (uei == null) {
                return;
            }

            m_updater.execute(new DataUpdater(m_dataManager, event));

            LOG.debug("Event {} added to updater queue", uei);

        } catch (RejectedExecutionException ex) {
            LOG.error("Failed to process event", ex);
            return;
        } catch (Throwable t) {
            LOG.error("Failed to process event", t);
            return;
        }

    } // end onEvent()

} // end class
