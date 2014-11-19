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
	public List<BridgeMacLink> findByMacAddress(String mac) {
		return find("from BridgeMacLink rec where rec.macAddress = ?", mac);
	}


	@Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
		for (BridgeMacLink elem: find("from BridgeMacLink rec where rec.node.id = ? and rec.bridgeMacLinkLastPollTime < ?",nodeId,now)) {
			delete(elem);
		}
	}

    @Override
    public List<BridgeMacTopologyLink> getAllBridgeLinksToIpAddrToNodes(){
        List<Object[]> links =  getHibernateTemplate().execute(new HibernateCallback<List<Object[]>>() {
            @Override
            public List<Object[]> doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createSQLQuery("select mlink.*," +
                        "ntm.netaddress, " +
                        "ip.ipaddr, " +
                        "ip.nodeid as targetnodeid, " +
                        "node.nodelabel, " +
                        "ntm.sourceIfIndex " +
                        "from bridgemaclink as mlink " +
                        "left join ipnettomedia as ntm " +
                        "on mlink.macaddress = ntm.physaddress " +
                        "left join ipinterface ip on ip.ipaddr = ntm.netaddress " +
                        "left join node on ip.nodeid = node.nodeid " +
                        "order by bridgeport;").list();
                //where ip.nodeid is not null
            }
        });

        List<BridgeMacTopologyLink> topoLinks = new ArrayList<BridgeMacTopologyLink>();
        for(Object[] link : links) {
            topoLinks.add(new BridgeMacTopologyLink((Integer)link[0], (Integer)link[1], (Integer)link[2],
                    (Integer)link[3], (Integer)link[4], (Integer)link[5], (String)link[6], (String)link[9],
                    (String)link[10], (Integer)link[11], (String)link[12], (Integer)link[13]));
        }

        return topoLinks;
    }

}
