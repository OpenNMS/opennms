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

public class DeleteEventVisitor extends AbstractEntityVisitor {
    private final EventForwarder m_eventForwarder;
    private static final String m_eventSource = "Provisiond";

    public DeleteEventVisitor(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    @Override
    public void visitMonitoredServiceComplete(final OnmsMonitoredService monSvc) {
        m_eventForwarder.sendNow(EventUtils.createServiceDeletedEvent(m_eventSource, monSvc.getNodeId(), monSvc.getIpAddress(), monSvc.getServiceType().getName()));
    }

    @Override
    public void visitIpInterfaceComplete(final OnmsIpInterface iface) {
        m_eventForwarder.sendNow(EventUtils.createInterfaceDeletedEvent(m_eventSource, iface.getNode().getId(), iface.getIpAddress(), iface.getId()));
    }

    @Override
    public void visitNodeComplete(final OnmsNode node) {
        m_eventForwarder.sendNow(EventUtils.createNodeDeletedEvent(m_eventSource, node.getId(), node.getLabel(), node.getLabel(), node.getLocation(), node.getForeignId(), node.getForeignSource(), node.getPrimaryInterface()));
    }
}
