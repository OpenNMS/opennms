/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class is a {@link Callable} task that is responsible for broadcasting
 * an OpenNMS syslog event on the event bus. It also broadcasts newSuspect events
 * if a syslog message has been received that is not associated with a node.</p>
 * 
 * <p>This task is separated out to allow the event receiver to do minimum work 
 * to avoid dropping packets from the syslog agents.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 */
public final class SyslogProcessor implements Callable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogProcessor.class);

    private final boolean m_NewSuspectOnMessage;

    private final String m_localAddr;

    private final Event m_event;

    public SyslogProcessor(Event event, boolean newSuspectOnMessage) {
        m_event = event;
        m_NewSuspectOnMessage = newSuspectOnMessage;
        m_localAddr = InetAddressUtils.getLocalHostName();
    }

    public Event getEvent() {
        return m_event;
    }

    /**
     * The event processing execution context.
     */
    @Override
    public Void call() {
        // get a logger
        try {
            if (LOG.isTraceEnabled())  {
                LOG.trace("Processing a syslog to event dispatch", m_event.toString());
                String uuid = m_event.getUuid();
                LOG.trace("Event {");
                LOG.trace("  uuid  = {}", (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                LOG.trace("  uei   = {}", m_event.getUei());
                LOG.trace("  src   = {}", m_event.getSource());
                LOG.trace("  iface = {}", m_event.getInterface());
                LOG.trace("  time  = {}", m_event.getTime());
                LOG.trace("  Msg   = {}", m_event.getLogmsg().getContent());
                LOG.trace("  Dst   = {}", m_event.getLogmsg().getDest());
                List<Parm> parms = (m_event.getParmCollection() == null ? null : m_event.getParmCollection());
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

            EventIpcManagerFactory.getIpcManager().sendNow(m_event);

            if (m_NewSuspectOnMessage && !m_event.hasNodeid()) {
                LOG.trace("Syslogd: Found a new suspect {}", m_event.getInterface());
                sendNewSuspectEvent(m_localAddr, m_event.getInterface());
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
