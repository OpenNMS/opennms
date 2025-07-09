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
package org.opennms.netmgt.enlinkd.persistence.impl;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.springframework.util.Assert;

/**
 * <p>IpInterfaceDaoHibernate class.</p>
 *
 * @author antonio
 */
public class LldpLinkDaoHibernate extends AbstractDaoHibernate<LldpLink, Integer>  implements LldpLinkDao {

    /**
     * <p>Constructor for IpInterfaceDaoHibernate.</p>
     */
    public LldpLinkDaoHibernate() {
        super(LldpLink.class);
    }

    /** {@inheritDoc} */
    @Override
    public LldpLink get(OnmsNode node, Integer lldpRemLocalPortNum, Integer lldpRemIndex) {
        return findUnique("from LldpLink as lldpLink where lldpLink.node = ?1 and lldpLink.lldpRemLocalPortNum = ?2 and lldpRemIndex = ?3", node, lldpRemLocalPortNum, lldpRemIndex);
    }

    /** {@inheritDoc} */
    @Override
    public LldpLink get(Integer nodeId, Integer lldpRemLocalPortNum, Integer lldpRemIndex) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(lldpRemLocalPortNum, "lldpRemLocalPortNum cannot be null");
        Assert.notNull(lldpRemIndex, "lldpRemIndex cannot be null");
        return findUnique("from LldpLink as lldpLink where lldpLink.node.id = ?1 and lldpLink.lldpRemLocalPortNum = ?2 and lldpLink.lldpRemIndex = ?3", nodeId, lldpRemLocalPortNum, lldpRemIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<LldpLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from LldpLink lldpLink where lldpLink.node.id = ?1", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from LldpLink lldpLink where lldpLink.node.id = ?1 and lldpLink.lldpLinkLastPollTime < ?2",
                nodeId, now);
    }

   @Override
   public void deleteByNodeId(Integer nodeId) {
       getHibernateTemplate().bulkUpdate("delete from LldpLink lldpLink where lldpLink.node.id = ?1 ",
                                         new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from LldpLink");
    }

    public List<LldpLink> findLinksForIds(List<Integer> linkIds) {

        final StringBuilder sql = new StringBuilder();
        sql.append("FROM LldpLink lldplink ");
        if(linkIds.size() == 1){
            sql.append("WHERE lldplink.id = ").append(linkIds.get(0)).append(" ");
        } else{
            sql.append("where lldplink.id in (");
            int counter = 0;
            for (Integer id : linkIds) {
                sql.append(id);
                if(counter < linkIds.size() - 1 ) {
                    sql.append(",");
                }
                counter++;
            }
            sql.append(")");
        }

        return find(sql.toString());
    }

    @Override
    public Integer getIfIndex(Integer nodeid, String portId) {
        Assert.notNull(nodeid, "nodeId may not be null");
        Assert.notNull(portId, "portId may not be null");

        List<?> ifaces=
                getHibernateTemplate().find("SELECT snmpIf FROM OnmsSnmpInterface AS snmpIf WHERE snmpIf.node.id = ?1 AND (LOWER(snmpIf.ifDescr) = LOWER(?2) OR LOWER(snmpIf.ifName) = LOWER(?3) OR snmpIf.physAddr = ?4)",
                       nodeid,
                        portId,
                       portId,
                        portId
                );
        if (ifaces.size() == 1) {
            return ((OnmsSnmpInterface) ifaces.iterator().next()).getIfIndex();
        }
        ifaces = getHibernateTemplate().find("SELECT ipIf FROM OnmsIpInterface AS ipIf WHERE ipIf.node.id = ?1 AND ipIf.ipAddress = ?2", nodeid, portId);
        if (ifaces.size() == 1) {
            OnmsIpInterface ipif = (OnmsIpInterface) ifaces.iterator().next();
            if (ipif.getSnmpInterface() != null) {
                ifaces = getHibernateTemplate().find("SELECT snmpIf FROM OnmsSnmpInterface AS snmpIf WHERE snmpIf.id = ?1)",
                        ipif.getSnmpInterface().getId());
            }
            if (ifaces.size() == 1) {
                return ((OnmsSnmpInterface) ifaces.iterator().next()).getIfIndex();
            }
        }
        return -1;
    }

}
