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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;
import org.springframework.orm.hibernate3.HibernateCallback;


public class BridgeMacLinkDaoHibernate extends AbstractDaoHibernate<BridgeMacLink, Integer> implements BridgeMacLinkDao {

    /**
     * <p>
     * Constructor for BridgeMacLinkDaoHibernate.
     * </p>
     */
    public BridgeMacLinkDaoHibernate() {
        super(BridgeMacLink.class);
    }


	@Override
	public List<BridgeMacLink> findByNodeId(Integer id) {
		return find("from BridgeMacLink rec where rec.node.id = ?", id);
	}


	@Override
	public BridgeMacLink getByNodeIdBridgePortMac(Integer id, Integer port, String mac) {
		return findUnique("from BridgeMacLink rec where rec.node.id = ?  and rec.bridgePort = ? and rec.macAddress = ? ", id,port,mac);
	}


       @Override
        public List<BridgeMacLink> findByNodeIdBridgePort(Integer id, Integer port) {
                return find("from BridgeMacLink rec where rec.node.id = ?  and rec.bridgePort = ? ", id,port);
        }

	@Override
	public List<BridgeMacLink> findByMacAddress(String mac) {
		return find("from BridgeMacLink rec where rec.macAddress = ?", mac);
	}


    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        for (BridgeMacLink elem : find("from BridgeMacLink rec where rec.node.id = ? and rec.bridgeMacLinkLastPollTime < ?",
                                       nodeId, now)) {
            delete(elem);
        }
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        for (BridgeMacLink elem : find("from BridgeMacLink rec where rec.node.id = ? ",
                                       nodeId)) {
            delete(elem);
        }
    }

	private final static String SQL_GET_MAC_LINKS=
	        "select mlink.id as source_id, "
	        + "mlink.nodeid as source_nodeid, "
	        + "n.nodelabel as sourcenodelabel, "
	        + "n.nodesysoid as sourcenodesysoid, "
	        + "n.nodesyslocation as sourcenodelocation,  "
	        + "n.nodetype as sourcenodetype,  "
	        + "mlink.bridgeport as bridgeport, "
	        + "mlink.bridgeportifindex as bridgeportifindex, "
	        + "mlink.bridgeportifname as bridgeportifname, "
	        + "mlink.vlan as vlan, "
	        + "ip.nodeid as target_nodeid, "
	        + "np.nodelabel as targetnodelabel,"
	        + "np.nodesysoid as targetnodesysoid,"
	        + "np.nodesyslocation as targetnodelocation, "
	        + "np.nodetype as targetnodetype, "
	        + "ntm.physaddress as target_mac,  "
                + "snmp.snmpifindex as target_ifindex, "
                + "ip.ipaddr as target_ifname, "
                + "snmp.snmpifindex as target_bridgeport, "
                + "ip.id as target_id, "
                + "mlink.bridgemaclinklastpolltime as lastPollTime "
	        + "from bridgemaclink as mlink "
	        + "left join ipnettomedia as ntm on mlink.macaddress = ntm.physaddress "
	        + "left join ipinterface ip on ip.ipaddr = ntm.netaddress "
                + "left join snmpinterface snmp on ip.snmpInterfaceId = snmp.id "
	        + "left join node n on mlink.nodeid = n.nodeid "
	        + "left join node np on ip.nodeid = np.nodeid "
	        + "where ip.nodeid is not null "
	        + "order by source_nodeid, bridgeport;";

	private final static String SQL_GET_BRIDGE_LINKS=
	        "select mlink.id as id, "
	        + "mlink.nodeid as source_nodeid, "
	        + "n.nodelabel as sourcenodelabel, "
	        + "n.nodesysoid as sourcenodesysoid, "
	        + "n.nodesyslocation as sourcenodelocation, "
	        + "n.nodetype as sourcenodetype, "
	        + "mlink.bridgeport as bridgeport, "
	        + "mlink.bridgeportifindex as bridgeportifindex, "
	        + "mlink.bridgeportifname as bridgeportifname, "
	        + "mlink.vlan as vlan, "
	        + "np.nodeid as target_nodeid, "
	        + "np.nodelabel as targetnodelabel, "
	        + "np.nodesysoid as targetnodesysoid, "
	        + "np.nodesyslocation as targetnodelocation, "
	        + "np.nodetype as targetnodetype, "
	        + "plink.macaddress as target_mac, "
                + "plink.bridgeportifindex as target_ifindex, "
                + "plink.bridgeportifname as target_ifname, "
                + "plink.bridgeport as target_bridgeport, "
                + "plink.id as target_id, "
                + "mlink.bridgemaclinklastpolltime as lastPollTime "
	        + "from bridgemaclink as mlink "
	        + "left join bridgemaclink as plink on mlink.macaddress = plink.macaddress "
	        + "left join node n on mlink.nodeid = n.nodeid "
	        + "left join node np on plink.nodeid = np.nodeid "
	        + "where mlink.nodeid < plink.nodeid "
	        + "order by source_nodeid, bridgeport;";
        
	private List<BridgeMacTopologyLink> convertObjectToTopologyLink(List<Object[]> list) {
            List<BridgeMacTopologyLink> topoLinks = new ArrayList<BridgeMacTopologyLink>();
            for (Object[] objs : list) {
                    topoLinks.add(
                                  new BridgeMacTopologyLink(
                                    (Integer) objs[0],
                                    (Integer) objs[1], 
                                    (String) objs[2], 
                                    (String) objs[3],
                                    (String) objs[4],
                                    NodeType.getNodeTypeFromChar((char)objs[5]),
                                    (Integer) objs[6], 
                                    (Integer) objs[7], 
                                    (String) objs[8], 
                                    (Integer) objs[9], 
                                    (Integer) objs[10], 
                                    (String) objs[11], 
                                    (String) objs[12],
                                    (String) objs[13],
                                    NodeType.getNodeTypeFromChar((char)objs[14]),
                                    (String) objs[15],
                                    (Integer) objs[16],
                                    (String) objs[17],
                                    (Integer) objs[18],
                                    (Integer) objs[19],
                                    (Date) objs[20]
                                            )
                                  );
            }

            return topoLinks;

    }
	        
    @Override
    public List<BridgeMacTopologyLink> getAllBridgeLinksToIpAddrToNodes(){
        return  getHibernateTemplate().execute(new HibernateCallback<List<BridgeMacTopologyLink>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<BridgeMacTopologyLink> doInHibernate(Session session) throws HibernateException, SQLException {
                return convertObjectToTopologyLink(session.createSQLQuery(SQL_GET_MAC_LINKS).list());
            }
        });

    }

    @Override
    public List<BridgeMacTopologyLink> getAllBridgeLinksToBridgeNodes(){
        return  getHibernateTemplate().execute(new HibernateCallback<List<BridgeMacTopologyLink>>() {
            @SuppressWarnings("unchecked")
            @Override
            public List<BridgeMacTopologyLink> doInHibernate(Session session) throws HibernateException, SQLException {
                return convertObjectToTopologyLink(session.createSQLQuery(SQL_GET_BRIDGE_LINKS).list());
            }
        });

    }

}
