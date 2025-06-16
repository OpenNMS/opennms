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
package org.opennms.netmgt.model.events;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>UpdateEventVisitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class UpdateEventVisitor extends AbstractEntityVisitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(UpdateEventVisitor.class);

    
    private static final String m_eventSource = "Provisiond";
    private EventForwarder m_eventForwarder;
    private String m_rescanExisting;
    private String monitorKey;

    /**
     * <p>Constructor for UpdateEventVisitor.</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     * @param rescanExisting a {@link String} object.
     * @param monitorKey a {@link String} object. (optional)
     */
    public UpdateEventVisitor(EventForwarder eventForwarder, String rescanExisting, String monitorKey) {
        m_eventForwarder = eventForwarder;
        m_rescanExisting = rescanExisting;
        this.monitorKey = monitorKey;
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitNode(OnmsNode node) {
        LOG.info("Sending nodeUpdated Event for {}\n", node);
        m_eventForwarder.sendNow(createNodeUpdatedEvent(node));
    }

    /** {@inheritDoc} */
    @Override
    public void visitIpInterface(OnmsIpInterface iface) {
        //TODO decide what to do here and when to do it
    }

    /** {@inheritDoc} */
    @Override
    public void visitMonitoredService(OnmsMonitoredService monSvc) {
        //TODO decide what to do here and when to do it
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitSnmpInterface(org.opennms.netmgt.model.OnmsEntity snmpIface) {
        //TODO decide what to do here and when to do it
    }

    private Event createNodeUpdatedEvent(OnmsNode node) {
        return EventUtils.createNodeUpdatedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabelSource(), m_rescanExisting, monitorKey);
    }
}
