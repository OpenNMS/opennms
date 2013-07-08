/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;
import java.util.concurrent.Callable;

import org.opennms.core.concurrent.EndOfTheWaterfall;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Parm;

/**
 * This class encapsulates the execution context for processing syslog messages
 * received via UDP from remote agents. This is a separate event context to
 * allow the event receiver to do minimum work to avoid dropping packets from
 * the agents.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 */
final class SyslogProcessor implements EndOfTheWaterfall {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogProcessor.class);

    private final boolean m_NewSuspectOnMessage;

    private final String m_localAddr;

    private final ConvertToEvent m_event;

    public SyslogProcessor(ConvertToEvent event) {
        m_event = event;
        m_NewSuspectOnMessage = SyslogdConfigFactory.getInstance().getNewSuspectOnMessage();
        m_localAddr = InetAddressUtils.getLocalHostName();
    }

    /**
     * The event processing execution context.
     */
    @Override
    public Callable<Void> call() {
        // get a logger
        try {
            if (LOG.isTraceEnabled())  {
                LOG.trace("Processing a syslog to event dispatch", m_event.toString());
                String uuid = m_event.getEvent().getUuid();
                LOG.trace("Event {");
                LOG.trace("  uuid  = {}", (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                LOG.trace("  uei   = {}", m_event.getEvent().getUei());
                LOG.trace("  src   = {}", m_event.getEvent().getSource());
                LOG.trace("  iface = {}", m_event.getEvent().getInterface());
                LOG.trace("  time  = {}", m_event.getEvent().getTime());
                LOG.trace("  Msg   = {}", m_event.getEvent().getLogmsg().getContent());
                LOG.trace("  Dst   = {}", m_event.getEvent().getLogmsg().getDest());
                List<Parm> parms = (m_event.getEvent().getParmCollection() == null ? null : m_event.getEvent().getParmCollection());
                if (parms != null) {
                    LOG.trace("  parms {");
                    for (Parm parm : parms) {
                        if ((parm.getParmName() != null)
                                && (parm.getValue().getContent() != null)) {
                            LOG.trace("    ({}, {})", parm.getParmName().trim(), parm.getValue().getContent().trim());
                        }
                    }
                    LOG.trace("  }");
                }
                LOG.trace("}");
            }

            EventIpcManagerFactory.getIpcManager().sendNow(m_event.getEvent());

            if (m_NewSuspectOnMessage && !m_event.getEvent().hasNodeid()) {
                LOG.trace("Syslogd: Found a new suspect {}", m_event.getEvent().getInterface());
                sendNewSuspectEvent(m_localAddr, m_event.getEvent().getInterface());
            }

        } catch (Throwable t) {
            LOG.error("Unexpected error processing SyslogMessage - Could not send", t);
        }

        // This task is the terminal task of syslogd so it doesn't return a Callable
        return null;
    }

    private static void sendNewSuspectEvent(String localAddr, String trapInterface) {
        EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "syslogd");
        bldr.setInterface(addr(trapInterface));
        bldr.setHost(localAddr);
        EventIpcManagerFactory.getIpcManager().sendNow(bldr.getEvent());
    }
}
