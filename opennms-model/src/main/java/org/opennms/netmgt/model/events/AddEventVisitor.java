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
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddEventVisitor extends AbstractEntityVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(AddEventVisitor.class);

    private static final String m_eventSource = "Provisiond";
    private final EventForwarder m_eventForwarder;
    private String monitorKey;

    public AddEventVisitor(EventForwarder eventForwarder) {
        this(eventForwarder, null);
    }

    /**
     * <p>Constructor for AddEventVisitor.</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param monitorKey a {@link java.lang.String} object. (optional)
     */
    public AddEventVisitor(EventForwarder eventForwarder, String monitorKey) {
        m_eventForwarder = eventForwarder;
        this.monitorKey = monitorKey;
    }

    /** {@inheritDoc} */
    @Override
    public void visitNode(OnmsNode node) {
        LOG.info("Sending nodeAdded Event for {}\n", node);
        m_eventForwarder.sendNow(createNodeAddedEvent(node));
        if (node.getCategories().size() > 0) {
            // Collect the category names into an array
            String[] categoriesAdded = node.getCategories().stream().map(OnmsCategory::getName).toArray(String[]::new);
            m_eventForwarder.sendNow(createNodeCategoryMembershipChangedEvent(node, categoriesAdded, new String[0]));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void visitIpInterface(OnmsIpInterface iface) {
        LOG.info("Sending nodeGainedInterface Event for {}\n", iface);
        m_eventForwarder.sendNow(createNodeGainedInterfaceEvent(iface));
    }

    /** {@inheritDoc} */
    @Override
    public void visitMonitoredService(OnmsMonitoredService monSvc) {
        LOG.info("Sending nodeGainedService Event for {}\n", monSvc);
        m_eventForwarder.sendNow(createNodeGainedServiceEvent(monSvc));
    }

    /**
     * <p>createNodeAddedEvent</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createNodeAddedEvent(OnmsNode node) {
        return EventUtils.createNodeAddedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabelSource(), monitorKey);
    }

    private Event createNodeCategoryMembershipChangedEvent(final OnmsNode node, String[] categoriesAdded, String[] categoriesDeleted) {
        return EventUtils.createNodeCategoryMembershipChangedEvent(m_eventSource, node.getId(), node.getLabel(), categoriesAdded, categoriesDeleted);
    }

    /**
     * <p>createNodeGainedInterfaceEvent</p>
     *
     * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createNodeGainedInterfaceEvent(OnmsIpInterface iface) {
        return EventUtils.createNodeGainedInterfaceEvent(m_eventSource, iface.getNode().getId(), iface.getIpAddress());
    }

    /**
     * <p>createNodeGainedServiceEvent</p>
     *
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createNodeGainedServiceEvent(final OnmsMonitoredService monSvc) {
        final OnmsIpInterface iface = monSvc.getIpInterface();
        final OnmsNode node = iface.getNode();
        LOG.debug("ipinterface = {}", iface);
        LOG.debug("snmpinterface = {}", iface.getSnmpInterface());
        LOG.debug("node = {}", node);
        return EventUtils.createNodeGainedServiceEvent(m_eventSource, monSvc.getNodeId(), iface.getIpAddress(), monSvc.getServiceType().getName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription());
    }


}
