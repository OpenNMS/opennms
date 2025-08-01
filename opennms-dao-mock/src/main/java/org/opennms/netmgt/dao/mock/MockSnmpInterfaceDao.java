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
package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSnmpInterfaceDao extends AbstractMockDao<OnmsSnmpInterface, Integer> implements SnmpInterfaceDao {
    private static final Logger LOG = LoggerFactory.getLogger(MockSnmpInterfaceDao.class);
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public Integer save(final OnmsSnmpInterface iface) {
        updateParent(iface);
        return super.save(iface);
    }

    @Override
    public void update(final OnmsSnmpInterface iface) {
        updateParent(iface);
        super.update(iface);
    }

    @Override
    protected void generateId(final OnmsSnmpInterface iface) {
        iface.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsSnmpInterface iface) {
        return iface.getId();
    }

    private void updateParent(final OnmsSnmpInterface iface) {
        OnmsNode node = null;
        if (iface.getNodeId() != null) {
            node = getNodeDao().get(iface.getNodeId());
        } else if (iface.getNode() != null) {
            node = getNodeDao().findByForeignId(iface.getNode().getForeignSource(), iface.getNode().getForeignId());
        }
        if (node != null && node != iface.getNode()) {
            LOG.debug("merging node {} into node {}", iface.getNode(), node);
            node.mergeNode(iface.getNode(), new NullEventForwarder(), false);
            iface.setNode(node);
        }
        if (!iface.getNode().getSnmpInterfaces().contains(iface)) {
            LOG.debug("adding SNMP interface to node {}: {}", iface.getNode().getId(), iface);
            iface.getNode().addSnmpInterface(iface);
        }
    }


    @Override
    public OnmsSnmpInterface findByNodeIdAndIfIndex(final Integer nodeId, final Integer ifIndex) {
        for (final OnmsSnmpInterface iface : findAll()) {
            if (nodeId.equals(iface.getNode().getId()) && ifIndex.equals(iface.getIfIndex())) {
                return iface;
            }
        }
        return null;
    }

    @Override
    public List<OnmsSnmpInterface> findByNodeId(final Integer nodeId) {
        return findAll().stream().filter(itf -> itf.getNode().getId().equals(nodeId)).collect(Collectors.toList());
    }

    @Override
    public List<OnmsSnmpInterface> findByMacLinksOfNode(Integer nodeId) {
        return Collections.emptyList();
    }

    @Override
    public List<OnmsSnmpInterface> findBySnpaAddressOfRelatedIsIsLink(int nodeId) {
        return Collections.emptyList();
    }

    @Override
    public OnmsSnmpInterface findByForeignKeyAndIfIndex(final String foreignSource, final String foreignId, final Integer ifIndex) {
        for (final OnmsSnmpInterface iface : findAll()) {
            final OnmsNode node = iface.getNode();
            if (foreignSource.equals(node.getForeignSource()) && foreignId.equals(node.getForeignId()) && ifIndex.equals(iface.getIfIndex())) {
                return iface;
            }
        }
        return null;
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndDescription(Integer nodeId, String description) {
        for (final OnmsSnmpInterface iface : findAll()) {
            final OnmsNode node = iface.getNode();
            if (description.equals(node.getSysDescription())) {
                return iface;
            }
        }
        return null;
    }

    @Override
    public void markHavingIngressFlows(final Integer nodeId, final Collection<Integer> ingressSnmpIfIndexes) {
    }

    @Override
    public void markHavingEgressFlows(final Integer nodeId, final Collection<Integer> egressSnmpIfIndexes) {
    }

    @Override
    public List<OnmsSnmpInterface> findAllHavingFlows(Integer nodeId) {
        return Collections.emptyList();
    }

    public List<OnmsSnmpInterface> findAllHavingIngressFlows(final Integer nodeId) {
        return Collections.emptyList();
    }


    public List<OnmsSnmpInterface> findAllHavingEgressFlows(final Integer nodeId) {
        return Collections.emptyList();
    }

    @Override
    public long getNumInterfacesWithFlows() {
        return 0;
    }
}
