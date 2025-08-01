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
package org.opennms.netmgt.enlinkd;

import static org.opennms.netmgt.events.api.EventConstants.PARAM_TOPOLOGY_NAMESPACE;

import java.util.List;

import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.events.EventUtils;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:matt@opennms.org">Matt Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
@EventListener(name="enlinkd")
public final class EventProcessor {

    private EnhancedLinkd m_linkd;

    /**
     * @param linkd the linkd to set
     */
    public void setLinkd(EnhancedLinkd linkd) {
        this.m_linkd = linkd;
    }

    public EnhancedLinkd getLinkd() {
        return m_linkd;
    }

    @EventHandler(uei=EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAdded(IEvent event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        m_linkd.addNode();
    }

    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeleted(IEvent event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        m_linkd.deleteNode(event.getNodeid().intValue());
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void handleNodeGainedService(IEvent event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);
        EventUtils.checkService(event);

        if (event.getService().equals("SNMP"))
        	m_linkd.execSingleSnmpCollection(event.getNodeid().intValue());
    }

    @EventHandler(uei=EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void handleNodeLostService(IEvent event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);
        EventUtils.checkService(event);

        if (event.getService().equals("SNMP"))
        	m_linkd.suspendNodeCollection(event.getNodeid().intValue());
    }

    @EventHandler(uei=EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void handleRegainedService(IEvent event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);
        EventUtils.checkService(event);

        if (event.getService().equals("SNMP"))
        	m_linkd.wakeUpNodeCollection(event.getNodeid().intValue());
    }
    
    /**
     * <p>handleForceRescan</p>
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.FORCE_RESCAN_EVENT_UEI)
    public void handleForceRescan(IEvent e) {
    	m_linkd.execSingleSnmpCollection(e.getNodeid().intValue());
    }
    

    /**
     * <p>handleRealodDaemonconfig</p>
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadDaemonConfig(IEvent e) {
        List<IParm> parmCollection = e.getParmCollection();

        for (IParm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Enlinkd".equalsIgnoreCase(parm.getValue().getContent())) {
                m_linkd.reloadConfig();
                break;
            }
        }
    }
    
    @EventHandler(uei = EventConstants.RELOAD_TOPOLOGY_UEI)
    public void handleReloadTopology(IEvent e) {
        final String topologyNamespace = EventUtils.getParm(e, PARAM_TOPOLOGY_NAMESPACE);
        if (topologyNamespace == null || "all".equalsIgnoreCase(topologyNamespace)) {
            m_linkd.reloadTopology();
        }
    }

} // end class
