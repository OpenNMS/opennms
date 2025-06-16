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

import java.util.List;

import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class TopologyEntityDaoHibernate extends HibernateDaoSupport implements TopologyEntityDao {

    @Override
    public List<NodeTopologyEntity> getNodeTopologyEntities() {
        return (List<NodeTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.NodeTopologyEntity(n.id, n.type, n.sysObjectId, n.label, n.location) from org.opennms.netmgt.model.OnmsNode n");
    }

    @Override
    public List<CdpLinkTopologyEntity> getCdpLinkTopologyEntities() {
        return (List<CdpLinkTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity(l.id, l.node.id, l.cdpCacheIfIndex, " +
                        "l.cdpInterfaceName, l.cdpCacheAddress, l.cdpCacheDeviceId, l.cdpCacheDevicePort) from org.opennms.netmgt.enlinkd.model.CdpLink l");
    }

    @Override
    public List<IsIsLinkTopologyEntity> getIsIsLinkTopologyEntities() {
        return (List<IsIsLinkTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity(l.id, l.node.id, l.isisISAdjIndex, l.isisCircIfIndex, l.isisISAdjNeighSysID, " +
                        "l.isisISAdjNeighSNPAAddress) from org.opennms.netmgt.enlinkd.model.IsIsLink l");
    }

    @Override
    public List<LldpLinkTopologyEntity> getLldpLinkTopologyEntities() {
        return (List<LldpLinkTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity(l.id, l.node.id, l.lldpRemChassisId, l.lldpRemSysname, l.lldpRemPortId, l.lldpRemPortIdSubType, l.lldpRemPortDescr, l.lldpPortId, l.lldpPortIdSubType, l.lldpPortDescr, l.lldpPortIfindex) from org.opennms.netmgt.enlinkd.model.LldpLink l");
    }

    @Override
    public List<OspfLinkTopologyEntity> getOspfLinkTopologyEntities() {
        return (List<OspfLinkTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity(l.id, l.node.id, l.ospfIpAddr, l.ospfIpMask, l.ospfRemIpAddr, l.ospfIfIndex, l.ospfIfAreaId) from org.opennms.netmgt.enlinkd.model.OspfLink l");
    }

    @Override
    public List<OspfAreaTopologyEntity> getOspfAreaTopologyEntities() {
        return (List<OspfAreaTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity(a.id, a.node.id, a.ospfAreaId, a.ospfAuthType, a.ospfImportAsExtern, a.ospfAreaBdrRtrCount, a.ospfAsBdrRtrCount, a.ospfAreaLsaCount ) from org.opennms.netmgt.enlinkd.model.OspfArea a");
    }


    @Override
    public List<SnmpInterfaceTopologyEntity> getSnmpTopologyEntities() {
        return (List<SnmpInterfaceTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity(" +
                        "i.id, i.ifIndex, i.ifName, i.ifAlias, i.ifSpeed, i.node.id) from org.opennms.netmgt.model.OnmsSnmpInterface i");
    }

    @Override
    public List<IpInterfaceTopologyEntity> getIpTopologyEntities() {
        return (List<IpInterfaceTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity(" +
                        "i.id, i.ipAddress, i.netMask, i.isManaged, i.snmpPrimary, i.node.id, i.snmpInterface.id) " +
                        "from org.opennms.netmgt.model.OnmsIpInterface i");
    }

    @Override
    public List<CdpElementTopologyEntity> getCdpElementTopologyEntities() {
        return (List<CdpElementTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity(e.id, e.cdpGlobalDeviceId, e.node.id)" +
                        "from org.opennms.netmgt.enlinkd.model.CdpElement e");
    }

    @Override
    public List<LldpElementTopologyEntity> getLldpElementTopologyEntities() {
        return (List<LldpElementTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity(e.id, e.lldpChassisId, e.lldpSysname, e.node.id)" +
                        "from org.opennms.netmgt.enlinkd.model.LldpElement e");
    }

    @Override
    public List<IsIsElementTopologyEntity> getIsIsElementTopologyEntities() {
        return (List<IsIsElementTopologyEntity>)getHibernateTemplate().find(
                "select new org.opennms.netmgt.enlinkd.model.IsIsElementTopologyEntity(e.id, e.isisSysID, e.node.id)" +
                        "from org.opennms.netmgt.enlinkd.model.IsIsElement e");
    }
}
