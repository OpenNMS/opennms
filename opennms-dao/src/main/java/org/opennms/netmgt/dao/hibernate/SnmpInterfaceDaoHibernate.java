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
package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.util.Assert;

public class SnmpInterfaceDaoHibernate extends AbstractDaoHibernate<OnmsSnmpInterface, Integer> implements SnmpInterfaceDao {
    /**
     * <p>Constructor for SnmpInterfaceDaoHibernate.</p>
     */
    public SnmpInterfaceDaoHibernate() {
        super(OnmsSnmpInterface.class);
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndIfIndex(Integer nodeId, Integer ifIndex) {
        Assert.notNull(nodeId, "nodeId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");
        return findUnique("select snmpIf from OnmsSnmpInterface as snmpIf where snmpIf.node.id = ?1 and snmpIf.ifIndex = ?2",
                          nodeId, 
                          ifIndex);
        
    }

    @Override
    public List<OnmsSnmpInterface> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId may not be null");
        return find("select snmpIf from OnmsSnmpInterface as snmpIf where snmpIf.node.id = ?1",
                nodeId);

    }

    @Override
    public List<OnmsSnmpInterface> findByMacLinksOfNode(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId may not be null");
        return find("from OnmsSnmpInterface snmpIf where snmpIf.physAddr in (select l.macAddress from BridgeMacLink l where l.node.id = ?1)",
                nodeId);

    }

    @Override
    public List<OnmsSnmpInterface> findBySnpaAddressOfRelatedIsIsLink(int nodeId) {
        return find("from OnmsSnmpInterface snmpIf where snmpIf.physAddr in (select l.isisISAdjNeighSNPAAddress from IsIsLink l where l.node.id = ?1)",
                nodeId);
    }

    @Override
    public OnmsSnmpInterface findByForeignKeyAndIfIndex(String foreignSource, String foreignId, Integer ifIndex) {
        Assert.notNull(foreignSource, "foreignSource may not be null");
        Assert.notNull(foreignId, "foreignId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");
        return findUnique("select snmpIf from OnmsSnmpInterface as snmpIf join snmpIf.node as node where node.foreignSource = ?1 and node.foreignId = ?2 and node.type = 'A' and snmpIf.ifIndex = ?3",
                          foreignSource, 
                          foreignId, 
                          ifIndex);
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndDescription(Integer nodeId, String description) {
        Assert.notNull(nodeId, "nodeId may not be null");
        Assert.notNull(description, "description may not be null");

        return findUnique("SELECT snmpIf FROM OnmsSnmpInterface AS snmpIf WHERE snmpIf.node.id = ?1 AND (LOWER(snmpIf.ifDescr) = LOWER(?2) OR LOWER(snmpIf.ifName) = LOWER(?3))",
            nodeId, 
            description,
            description
        );
    }

    @Override
    public void markHavingIngressFlows(final Integer nodeId, final Collection<Integer> ingressSnmpIfIndexes) {
        getHibernateTemplate().executeWithNativeSession(session -> session.createSQLQuery("update snmpinterface set last_ingress_flow = NOW() where nodeid = :nodeid and snmpifindex in (:snmpIfIndexes)")
                .setParameter("nodeid", nodeId)
                .setParameterList("snmpIfIndexes", ingressSnmpIfIndexes)
                .executeUpdate());
    }

    @Override
    public void markHavingEgressFlows(final Integer nodeId, final Collection<Integer> egressSnmpIfIndexes) {
        getHibernateTemplate().executeWithNativeSession(session -> session.createSQLQuery("update snmpinterface set last_egress_flow = NOW() where nodeid = :nodeid and snmpifindex in (:snmpIfIndexes)")
                .setParameter("nodeid", nodeId)
                    .setParameterList("snmpIfIndexes", egressSnmpIfIndexes)
                    .executeUpdate());
    }

    @Override
    public List<OnmsSnmpInterface> findAllHavingFlows(final Integer nodeId) {
        if (OnmsSnmpInterface.INGRESS_AND_EGRESS_REQUIRED) {
            return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ?1 and (EXTRACT(EPOCH FROM (NOW() - lastIngressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE +" AND EXTRACT(EPOCH FROM (NOW() - lastEgressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE + ")", nodeId);
        } else {
            return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ?1 and (EXTRACT(EPOCH FROM (NOW() - lastIngressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE +" OR EXTRACT(EPOCH FROM (NOW() - lastEgressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE + ")", nodeId);
        }
    }

    public List<OnmsSnmpInterface> findAllHavingIngressFlows(final Integer nodeId) {
        return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ?1 and EXTRACT(EPOCH FROM (NOW() - lastIngressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE, nodeId);
    }

    public List<OnmsSnmpInterface> findAllHavingEgressFlows(final Integer nodeId) {
        return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ?1 and EXTRACT(EPOCH FROM (NOW() - lastEgressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE, nodeId);
    }

    @Override
    public long getNumInterfacesWithFlows() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsSnmpInterface.class);
        criteriaBuilder.or(Restrictions.isNotNull("lastIngressFlow"), Restrictions.isNotNull("lastEgressFlow"));
        return countMatching(criteriaBuilder.toCriteria());
    }

}
