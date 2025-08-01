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
package org.opennms.web.svclayer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this class to aggregate status to be presented in a view layer technology.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class AggregateStatus implements SurveillanceStatus {
    private static final Logger LOG = LoggerFactory.getLogger(AggregateStatus.class);

    private String m_label;

    private Integer m_totalEntityCount;

    private final List<OnmsNode> m_downNodes = new ArrayList<>();

    private String m_status;

    private String m_link;

    /** Constant <code>NODES_ARE_DOWN="Critical"</code> */
    public static final String NODES_ARE_DOWN = "Critical";

    /** Constant <code>ONE_SERVICE_DOWN="Warning"</code> */
    public static final String ONE_SERVICE_DOWN = "Warning";

    /** Constant <code>ALL_NODES_UP="Normal"</code> */
    public static final String ALL_NODES_UP = "Normal";

    /**
     * <p>Constructor for AggregateStatus.</p>
     *
     * @param nodes a {@link java.util.Collection} object.
     */
    public AggregateStatus(Collection<OnmsNode> nodes) {
        computeStatusValues(nodes);
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStatus() {
        return m_status;
    }

    private void setStatus(String color) {
        m_status = color;
    }

    /**
     * <p>getDownEntityCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getDownEntityCount() {
        return m_downNodes.size();
    }

    /**
     * <p>getDownNodes</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public List<OnmsNode> getDownNodes() {
        return Collections.unmodifiableList(m_downNodes);
    }

    private void setDownNodes(final Collection<OnmsNode> downNodes) {
        if (m_downNodes == downNodes) return;
        m_downNodes.clear();
        m_downNodes.addAll(downNodes);
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        m_label = label;
    }

    /**
     * <p>getTotalEntityCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getTotalEntityCount() {
        return m_totalEntityCount;
    }

    private void setTotalEntityCount(Integer totalEntityCount) {
        m_totalEntityCount = totalEntityCount;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(m_label == null ? "null" : m_label);
        sb.append(": ");
        sb.append(m_downNodes == null ? -1 : m_downNodes.size());
        sb.append(" down of ");
        sb.append(m_totalEntityCount == null ? -1 : m_totalEntityCount);
        sb.append(" total.");
        return sb.toString();
    }

    final static class AggregateStatusVisitor extends AbstractEntityVisitor {

        Set<OnmsNode> m_downNodes = new LinkedHashSet<>();

        String m_status = AggregateStatus.ALL_NODES_UP;

        boolean m_isCurrentNodeDown = true;

        @Override
        public void visitNode(OnmsNode node) {
            LOG.debug("visitNode({})", node);
            m_isCurrentNodeDown = true;
        }

        @Override
        public void visitNodeComplete(OnmsNode node) {
            LOG.debug("visitNodeComplete({}) -- m_isCurrentNodeDown = {}", node, m_isCurrentNodeDown);
            if (m_isCurrentNodeDown) {
                m_downNodes.add(node);
                m_status = AggregateStatus.NODES_ARE_DOWN;
            }

        }

        @Override
        public void visitMonitoredService(OnmsMonitoredService svc) {
            LOG.debug("visitMonitoredService({}) - currentOutages.isEmpty = {}", svc, svc.getCurrentOutages().isEmpty());
            if ("A".equals(svc.getStatus()) && !svc.getCurrentOutages().isEmpty()) {
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

    /**
     * <p>getLink</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLink() {
        return m_link;
    }

    /**
     * <p>setLink</p>
     *
     * @param link a {@link java.lang.String} object.
     */
    public void setLink(String link) {
        m_link = link;
    }

}
