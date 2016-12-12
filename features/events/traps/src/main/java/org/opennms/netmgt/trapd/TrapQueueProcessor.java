/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.trapd.jmx.TrapdInstrumentation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * The TrapQueueProcessor handles the conversion of V1 and V2 traps to events
 * and sending them out the JSDT channel that eventd is listening on.
 * 
 * @see Java Shared Data Toolkit
 * @see http://www.drdobbs.com/collaborative-applications-and-the-java/184403999
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *  
 */
public class TrapQueueProcessor implements InitializingBean {

    public static final TrapdInstrumentation trapdInstrumentation = new TrapdInstrumentation();

    private static final Logger LOG = LoggerFactory.getLogger(TrapQueueProcessor.class);

    /**
     * The name of the local host.
     */
    private static final String LOCALHOST_ADDRESS = InetAddressUtils.getLocalHostName();

    /**
     * Whether or not a newSuspect event should be generated with a trap from an
     * unknown IP address
     */
    private Boolean m_newSuspect;

    /**
     * The event IPC manager to which we send events created from traps.
     */
    private EventForwarder m_eventForwarder;

    /**
     * The event configuration DAO that we use to convert from traps to events.
     */
    private EventConfDao m_eventConfDao;

    private InterfaceToNodeCache m_interfaceToNodeCache;

    /**
     * Process a V2 trap and convert it to an event for transmission.
     *
     * <p>
     * From RFC2089 ('Mapping SNMPv2 onto SNMPv1'), section 3.3 ('Processing an
     * outgoing SNMPv2 TRAP')
     * </p>
     *
     * <p>
     * <strong>2b </strong>
     * <p>
     * If the snmpTrapOID.0 value is one of the standard traps the specific-trap
     * field is set to zero and the generic trap field is set according to this
     * mapping:
     * <p>
     * 
     * <pre>
     * 
     *  
     *   
     *    
     *     
     *      
     *            value of snmpTrapOID.0                generic-trap
     *            ===============================       ============
     *            1.3.6.1.6.3.1.1.5.1 (coldStart)                  0
     *            1.3.6.1.6.3.1.1.5.2 (warmStart)                  1
     *            1.3.6.1.6.3.1.1.5.3 (linkDown)                   2
     *            1.3.6.1.6.3.1.1.5.4 (linkUp)                     3
     *            1.3.6.1.6.3.1.1.5.5 (authenticationFailure)      4
     *            1.3.6.1.6.3.1.1.5.6 (egpNeighborLoss)            5
     *       
     *      
     *     
     *    
     *   
     *  
     * </pre>
     * 
     * <p>
     * The enterprise field is set to the value of snmpTrapEnterprise.0 if this
     * varBind is present, otherwise it is set to the value snmpTraps as defined
     * in RFC1907 [4].
     * </p>
     * 
     * <p>
     * <strong>2c. </strong>
     * </p>
     * <p>
     * If the snmpTrapOID.0 value is not one of the standard traps, then the
     * generic-trap field is set to 6 and the specific-trap field is set to the
     * last subid of the snmpTrapOID.0 value.
     * </p>
     * 
     * <p>
     * If the next to last subid of snmpTrapOID.0 is zero, then the enterprise
     * field is set to snmpTrapOID.0 value and the last 2 subids are truncated
     * from that value. If the next to last subid of snmpTrapOID.0 is not zero,
     * then the enterprise field is set to snmpTrapOID.0 value and the last 1
     * subid is truncated from that value.
     * </p>
     *
     * <p>
     * In any event, the snmpTrapEnterprise.0 varBind (if present) is ignored in
     * this case.
     * </p>
     */

    public void process(final TrapInformation trap) {
        try {
            final EventCreator eventCreator = new EventCreator(m_interfaceToNodeCache);
            final Event event = eventCreator.getEvent(trap);
            processTrapEvent((event));
        } catch (IllegalArgumentException e) {
            LOG.info(e.getMessage());
        } catch (Throwable e) {
            LOG.error("Unexpected error processing trap: {}", e, e);
            trapdInstrumentation.incErrorCount();
        }
    }

    private void processTrapEvent(final Event event) {
        final InetAddress trapInterface = event.getInterfaceAddress();

        final org.opennms.netmgt.xml.eventconf.Event econf = m_eventConfDao.findByEvent(event);
        if (econf == null || econf.getUei() == null) {
            event.setUei("uei.opennms.org/default/trap");
        } else {
            event.setUei(econf.getUei());
        }

        if (event.getSnmp() != null) {
            final String version = event.getSnmp().getVersion();
            trapdInstrumentation.incTrapsReceivedCount(version);
        }

        if (econf != null) {
            final Logmsg logmsg = econf.getLogmsg();
            if (logmsg != null) {
                final String dest = logmsg.getDest();
                if ("discardtraps".equals(dest)) {
                    LOG.debug("Trap discarded due to matching event having logmsg dest == discardtraps");
                    trapdInstrumentation.incDiscardCount();
                    return;
                }
            }
        }

        // send the event to eventd
        m_eventForwarder.sendNow(event);

        LOG.debug("Trap successfully converted and sent to eventd with UEI {}", event.getUei());

        if (!event.hasNodeid() && m_newSuspect) {
            sendNewSuspectEvent(InetAddressUtils.str(trapInterface));

            LOG.debug("Sent newSuspectEvent for interface: {}", trapInterface);

        }
    }

    private void sendNewSuspectEvent(String trapInterface) {
        // construct event with 'trapd' as source
        EventBuilder bldr = new EventBuilder(org.opennms.netmgt.events.api.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        bldr.setInterface(addr(trapInterface));
        bldr.setHost(LOCALHOST_ADDRESS);

        // send the event to eventd
        m_eventForwarder.sendNow(bldr.getEvent());
    }


    void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }

    void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    void setNewSuspect(Boolean newSuspect) {
        m_newSuspect = newSuspect;
    }

    void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        m_interfaceToNodeCache = interfaceToNodeCache;
    }

    @Override
    public void afterPropertiesSet() throws IllegalStateException {
        Objects.requireNonNull(m_eventConfDao, "property eventConfDao must be set");
        Objects.requireNonNull(m_eventForwarder, "property eventForwarder must be set");
        Objects.requireNonNull(m_newSuspect, "property newSuspect must be set");
        Objects.requireNonNull(m_interfaceToNodeCache, "property interfaceToNodeCache must be set");
    }
}
