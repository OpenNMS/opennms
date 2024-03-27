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

import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;

/**
 * This interface provides the contract that implementor must implement in order
 * to receive events from adaptors.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http;//www.opennms.org">OpenNMS </a>
 * @version $Id: $
 */
public class EventHandlerMBeanProxy implements EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(EventHandlerMBeanProxy.class);
    private MBeanServer m_mbserver;

    private ObjectName m_listener;

    private void findServer() throws InstanceNotFoundException {
        
        for (final MBeanServer sx : findMBeanServers()) {
            try {
                if (sx.getObjectInstance(m_listener) != null) {
                    m_mbserver = sx;
                    break;
                }
            } catch (final InstanceNotFoundException e) {
                // do nothing
            }
        }
        if (m_mbserver == null) {
            throw new InstanceNotFoundException("could not locate mbean server instance");
        }
        
    }

    private List<MBeanServer> findMBeanServers() {
        // In java 1.5 this returns a generic ArrayList
        return MBeanServerFactory.findMBeanServer(null);
    }

    /**
     * <p>Constructor for EventHandlerMBeanProxy.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @throws javax.management.MalformedObjectNameException if any.
     * @throws javax.management.InstanceNotFoundException if any.
     */
    public EventHandlerMBeanProxy(final String name) throws MalformedObjectNameException, InstanceNotFoundException {
        m_listener = new ObjectName(name);
        findServer();
    }

    /**
     * <p>Constructor for EventHandlerMBeanProxy.</p>
     *
     * @param name a {@link javax.management.ObjectName} object.
     * @throws javax.management.InstanceNotFoundException if any.
     */
    public EventHandlerMBeanProxy(final ObjectName name) throws InstanceNotFoundException {
        m_listener = name;
        findServer();
    }

    /**
     * <p>Constructor for EventHandlerMBeanProxy.</p>
     *
     * @param name a {@link javax.management.ObjectName} object.
     * @param server a {@link javax.management.MBeanServer} object.
     */
    public EventHandlerMBeanProxy(final ObjectName name, final MBeanServer server) {
        m_listener = name;
        m_mbserver = server;
    }

    /** {@inheritDoc} */
    @Override
    public boolean processEvent(final Event event) {
        boolean result = false;
        try {
            result = (Boolean) m_mbserver.invoke(m_listener, "processEvent", new Object[] { event }, new String[] { "org.opennms.netmgt.xml.event.Event" });
        } catch (final Throwable t) {
            LOG.warn("Invocation on object {} failed", t, m_listener);
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void receiptSent(final EventReceipt receipt) {
        try {
            m_mbserver.invoke(m_listener, "receiptSent", new Object[] { receipt }, new String[] { "org.opennms.netmgt.xml.event.EventReceipt" });
        } catch (final Throwable t) {
            LOG.warn("Invocation on object {} failed", t, m_listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 13)
            .append(m_mbserver)
            .append(m_listener)
            .toHashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        boolean rc = false;
        if (this == obj) {
            rc = true;
        } else if (obj != null && obj instanceof EventHandlerMBeanProxy) {
            EventHandlerMBeanProxy p = (EventHandlerMBeanProxy) obj;
            if (p.m_mbserver.equals(m_mbserver) && p.m_listener.equals(m_listener))
                rc = true;
        }

        return rc;
    }
}
