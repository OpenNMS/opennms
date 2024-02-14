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
package org.opennms.netmgt.collectd;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ForceRescanState class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ForceRescanState {
    
    private static final Logger LOG = LoggerFactory.getLogger(ForceRescanState.class);
    
    private CollectionAgent m_agent;
    private EventProxy m_eventProxy;
    
    private boolean m_forceRescanSent = false;

    /**
     * <p>Constructor for ForceRescanState.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param eventProxy a {@link org.opennms.netmgt.events.api.EventProxy} object.
     */
    public ForceRescanState(CollectionAgent agent, EventProxy eventProxy) {
        m_agent = agent;
        m_eventProxy = eventProxy;
    }
    
    /**
     * <p>getEventProxy</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventProxy} object.
     */
    public EventProxy getEventProxy() {
        return m_eventProxy;
    }

    /**
     * <p>createForceResanEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createForceRescanEvent() {
        // create the event to be sent
        EventBuilder bldr = new EventBuilder(EventConstants.FORCE_RESCAN_EVENT_UEI, "SnmpCollector");
        
        bldr.setNodeid(m_agent.getNodeId());

        bldr.setInterface(m_agent.getAddress());
        
        bldr.setService(AbstractSnmpCollector.SERVICE_NAME);
        
        bldr.setHost(InetAddressUtils.getLocalHostName());

        return bldr.getEvent();
    }

    /**
     * <p>getAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public CollectionAgent getAgent() {
        return m_agent;
    }

    /**
     * This method is responsible for building a Capsd forceRescan event object
     * and sending it out over the EventProxy.
     */
    void sendForceRescanEvent() {
        // Log4j category
	LOG.debug("generateForceRescanEvent: interface = {}", getAgent().getHostAddress());
    
    	// Send event via EventProxy
    	try {
            getEventProxy().send(createForceRescanEvent());
    	} catch (EventProxyException e) {
		LOG.error("generateForceRescanEvent: Unable to send forceRescan event.", e);
    	}
    }
    
    void rescanIndicated() {
        if (!m_forceRescanSent) {
            sendForceRescanEvent();
            m_forceRescanSent = true;
        }
    }

}
