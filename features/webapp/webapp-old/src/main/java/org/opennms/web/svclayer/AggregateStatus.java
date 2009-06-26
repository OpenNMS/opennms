/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 9, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Use this class to aggregate status to be presented in a view layer technology.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class AggregateStatus {

    private String m_label;

    private Integer m_totalEntityCount;

    private Set<OnmsNode> m_downNodes;

    private String m_status;

    private String m_link;

    public static final String NODES_ARE_DOWN = "Critical";

    public static final String ONE_SERVICE_DOWN = "Warning";

    public static final String ALL_NODES_UP = "Normal";

    public AggregateStatus(Collection<OnmsNode> nodes) {
        computeStatusValues(nodes);
    }

    public String getStatus() {
        return m_status;
    }

    private void setStatus(String color) {
        m_status = color;
    }

    public Integer getDownEntityCount() {
        return m_downNodes.size();
    }

    public Set<OnmsNode> getDownNodes() {
        return Collections.unmodifiableSet(m_downNodes);
    }

    private void setDownNodes(Set<OnmsNode> downNodes) {
        m_downNodes = downNodes;
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public Integer getTotalEntityCount() {
        return m_totalEntityCount;
    }

    private void setTotalEntityCount(Integer totalEntityCount) {
        m_totalEntityCount = totalEntityCount;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(m_label == null ? "null" : m_label);
        sb.append(": ");
        sb.append(m_downNodes == null ? -1 : m_downNodes.size());
        sb.append(" down of ");
        sb.append(m_totalEntityCount == null ? -1 : m_totalEntityCount);
        sb.append(" total.");
        return sb.toString();
    }

    final class AggregateStatusVisitor extends AbstractEntityVisitor {

        Set<OnmsNode> m_downNodes = new LinkedHashSet<OnmsNode>();

        String m_status = AggregateStatus.ALL_NODES_UP;

        boolean m_isCurrentNodeDown = true;

        @Override
        public void visitNode(OnmsNode node) {
            m_isCurrentNodeDown = true;
        }

        @Override
        public void visitNodeComplete(OnmsNode node) {
            if (m_isCurrentNodeDown) {
                m_downNodes.add(node);
                m_status = AggregateStatus.NODES_ARE_DOWN;
            }

        }

        @Override
        public void visitMonitoredService(OnmsMonitoredService svc) {
            if ("A".equals(svc.getStatus())
                    && !svc.getCurrentOutages().isEmpty()) {
                if (AggregateStatus.ALL_NODES_UP.equals(m_status)) {
                    m_status = AggregateStatus.ONE_SERVICE_DOWN;
                }
            } else if ("A".equals(svc.getStatus())) {
                m_isCurrentNodeDown = false;
            }
        }

        public String getStatus() {
            return m_status;
        }

        public Set<OnmsNode> getDownNodes() {
            return m_downNodes;
        }

    }

    private void visitNodes(Collection<OnmsNode> nodes,
            AggregateStatusVisitor statusVisitor) {

        if (nodes == null) {
            return;
        }

        for (OnmsNode node : nodes) {
            node.visit(statusVisitor);
        }
    }

    private AggregateStatus computeStatusValues(Collection<OnmsNode> nodes) {
        AggregateStatusVisitor statusVisitor = new AggregateStatusVisitor();
        visitNodes(nodes, statusVisitor);

        setDownNodes(statusVisitor.getDownNodes());
        setTotalEntityCount(nodes.size());
        setStatus(statusVisitor.getStatus());
        return this;
    }

    public String getLink() {
        return m_link;
    }

    public void setLink(String link) {
        m_link = link;
    }

}
