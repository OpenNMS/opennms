/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;
import java.util.List;

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
        return findUnique("select snmpIf from OnmsSnmpInterface as snmpIf where snmpIf.node.id = ? and snmpIf.ifIndex = ?",
                          nodeId, 
                          ifIndex);
        
    }

    @Override
    public List<OnmsSnmpInterface> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId may not be null");
        return find("select snmpIf from OnmsSnmpInterface as snmpIf where snmpIf.node.id = ?",
                nodeId);

    }

    @Override
    public List<OnmsSnmpInterface> findByMacLinksOfNode(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId may not be null");
        return find("from OnmsSnmpInterface snmpIf where snmpIf.physAddr in (select l.macAddress from BridgeMacLink l where l.node.id = ?)",
                nodeId);

    }

    @Override
    public List<OnmsSnmpInterface> findBySnpaAddressOfRelatedIsIsLink(int nodeId) {
        return find("from OnmsSnmpInterface snmpIf where snmpIf.physAddr in (select l.isisISAdjNeighSNPAAddress from IsIsLink l where l.node.id = ?)",
                nodeId);
    }

    @Override
    public OnmsSnmpInterface findByForeignKeyAndIfIndex(String foreignSource, String foreignId, Integer ifIndex) {
        Assert.notNull(foreignSource, "foreignSource may not be null");
        Assert.notNull(foreignId, "foreignId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");
        return findUnique("select snmpIf from OnmsSnmpInterface as snmpIf join snmpIf.node as node where node.foreignSource = ? and node.foreignId = ? and node.type = 'A' and snmpIf.ifIndex = ?",
                          foreignSource, 
                          foreignId, 
                          ifIndex);
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndDescription(Integer nodeId, String description) {
        Assert.notNull(nodeId, "nodeId may not be null");
        Assert.notNull(description, "description may not be null");

        return findUnique("SELECT snmpIf FROM OnmsSnmpInterface AS snmpIf WHERE snmpIf.node.id = ? AND (LOWER(snmpIf.ifDescr) = LOWER(?) OR LOWER(snmpIf.ifName) = LOWER(?))",
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
            return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ? and (EXTRACT(EPOCH FROM (NOW() - lastIngressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE +" AND EXTRACT(EPOCH FROM (NOW() - lastEgressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE + ")", nodeId);
        } else {
            return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ? and (EXTRACT(EPOCH FROM (NOW() - lastIngressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE +" OR EXTRACT(EPOCH FROM (NOW() - lastEgressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE + ")", nodeId);
        }
    }

    public List<OnmsSnmpInterface> findAllHavingIngressFlows(final Integer nodeId) {
        return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ? and EXTRACT(EPOCH FROM (NOW() - lastIngressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE, nodeId);
    }

    public List<OnmsSnmpInterface> findAllHavingEgressFlows(final Integer nodeId) {
        return find("select iface from OnmsSnmpInterface as iface where iface.node.id = ? and EXTRACT(EPOCH FROM (NOW() - lastEgressFlow)) <= " + OnmsSnmpInterface.MAX_FLOW_AGE, nodeId);
    }
}
