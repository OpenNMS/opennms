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
package org.opennms.netmgt.alarmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.dao.api.DefaultAlarmEntityListener;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class NorthbounderManager extends DefaultAlarmEntityListener {
    private static final Logger LOG = LoggerFactory.getLogger(NorthbounderManager.class);

    private List<Northbounder> m_northboundInterfaces = new ArrayList<>();

    private volatile boolean m_hasActiveAlarmNbis = false;

    /** The event proxy. */
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Override
    public void onAlarmCreated(OnmsAlarm alarm) {
        forwardAlarmToNbis(alarm);
    }

    @Override
    public void onAlarmUpdatedWithReducedEvent(OnmsAlarm alarm) {
        forwardAlarmToNbis(alarm);
    }

    /**
     * Forwards the alarms to the current set of NBIs.
     *
     * This method is synchronized since the event handler is multi-threaded
     * and the NBIs are not necessarily thread safe.
     *
     * @param alarm the alarm to forward
     */
    private synchronized void forwardAlarmToNbis(OnmsAlarm alarm) {
        NorthboundAlarm a = new NorthboundAlarm(alarm);
        for (Northbounder nbi : m_northboundInterfaces) {
            nbi.onAlarm(a);
        }
    }

    public synchronized void onNorthbounderRegistered(final Northbounder northbounder, final Map<String,String> properties) {
        LOG.debug("onNorthbounderRegistered: starting {}", northbounder.getName());
        northbounder.start();
        m_northboundInterfaces.add(northbounder);
        onNorthboundersChanged();
    }

    public synchronized void onNorthbounderUnregistered(final Northbounder northbounder, final Map<String,String> properties) {
        LOG.debug("onNorthbounderUnregistered: stopping {}", northbounder.getName());
        northbounder.stop();
        m_northboundInterfaces.remove(northbounder);
        onNorthboundersChanged();
    }

    private void onNorthboundersChanged() {
        final long numNbisActive = m_northboundInterfaces.stream()
                .filter(Northbounder::isReady)
                .count();
        m_hasActiveAlarmNbis = numNbisActive > 0;
        LOG.debug("handleNorthboundersChanged: {} out of {} NBIs are currently active.",
                numNbisActive, m_northboundInterfaces.size());
    }

    public synchronized List<Northbounder> getNorthboundInterfaces() {
        return Collections.unmodifiableList(m_northboundInterfaces);
    }

    public synchronized void setNorthboundInterfaces(List<Northbounder> northboundInterfaces) {
        m_northboundInterfaces = northboundInterfaces;
        onNorthboundersChanged();
    }

    public void handleReloadEvent(IEvent e) {
        List<IParm> parmCollection = e.getParmCollection();
        for (IParm parm : parmCollection) {
            String parmName = parm.getParmName();
            if ("daemonName".equals(parmName)) {
                if (parm.getValue() == null || parm.getValue().getContent() == null) {
                    LOG.warn("The daemonName parameter has no value, ignoring.");
                    return;
                }
                final String daemonName = parm.getValue().getContent();

                List<Northbounder> nbis = getNorthboundInterfaces();
                for (Northbounder nbi : nbis) {
                    if (daemonName.contains(nbi.getName())) {
                        LOG.debug("Handling reload event for NBI: {}", nbi.getName());
                        LOG.debug("Reloading NBI configuration for interface {} not yet implemented.", nbi.getName());
                        EventBuilder ebldr = null;
                        try {
                            nbi.reloadConfig();
                            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, Alarmd.NAME);
                            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, Alarmd.NAME);
                        } catch (NorthbounderException ex) {
                            LOG.error("Can't reload the northbound configuration for " + nbi.getName(), ex);
                            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, Alarmd.NAME);
                            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, Alarmd.NAME);
                            ebldr.addParam(EventConstants.PARM_REASON, ex.getMessage());
                        } finally {
                            if (ebldr != null)
                                try {
                                    m_eventProxy.send(ebldr.getEvent());
                                } catch (EventProxyException ep) {
                                    LOG.error("Can't send reload status event", ep);
                                }
                        }
                        return;
                    }
                }
            }
        }
    }

    public void stop() {
        // On shutdown, stop all of the NBIs
        m_northboundInterfaces.forEach(nb -> {
            LOG.debug("destroy: stopping {}", nb.getName());
            nb.stop();
        });
    }

    public synchronized void onAlarmCreatedOrUpdatedWithReducedEvent(OnmsAlarm alarm) {
        if (m_hasActiveAlarmNbis) {
            final NorthboundAlarm a = new NorthboundAlarm(alarm);
            for (Northbounder nbi : m_northboundInterfaces) {
                nbi.onAlarm(a);
            }
        }
    }

}
