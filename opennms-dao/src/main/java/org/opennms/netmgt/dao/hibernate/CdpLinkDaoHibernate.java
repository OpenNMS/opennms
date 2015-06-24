/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.CdpTopologyLink;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

/**
 * <p>CdpLinkDaoHibernate class.</p>
 *
 * @author antonio
 */
public class CdpLinkDaoHibernate extends AbstractDaoHibernate<CdpLink, Integer>  implements CdpLinkDao {

    /**
     * <p>Constructor for IpInterfaceDaoHibernate.</p>
     */
    public CdpLinkDaoHibernate() {
        super(CdpLink.class);
    }

    /** {@inheritDoc} */
    @Override
    public CdpLink get(OnmsNode node, Integer cdpCacheifIndex, Integer cdpCacheDeviceIndex) {
        Assert.notNull(node, "node cannot be null");
        Assert.notNull(cdpCacheifIndex, "cdpCacheifIndex cannot be null");
        Assert.notNull(cdpCacheDeviceIndex, "cdpCacheDeviceIndex cannot be null");
        return findUnique("from CdpLink as cdpLink where cdpLink.node = ? and cdpLink.cdpCacheIfIndex = ? and cdpCacheDeviceIndex = ?", node, cdpCacheifIndex, cdpCacheDeviceIndex);
    }

    /** {@inheritDoc} */
    @Override
    public CdpLink get(Integer nodeId, Integer cdpCacheifIndex, Integer cdpCacheDeviceIndex) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(cdpCacheifIndex, "cdpCacheifIndex cannot be null");
        Assert.notNull(cdpCacheDeviceIndex, "cdpCacheDeviceIndex cannot be null");
        return findUnique("from CdpLink as cdpLink where cdpLink.node.id = ? and cdpLink.cdpCacheIfIndex = ? and cdpCacheDeviceIndex = ?", nodeId, cdpCacheifIndex, cdpCacheDeviceIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<CdpLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from CdpLink cdpLink where cdpLink.node.id = ?", nodeId);
    }

    @Override
    public List<CdpTopologyLink> findLinksForTopology() {
        return getHibernateTemplate().execute(new HibernateCallback<List<CdpTopologyLink>>() {
            @Override
            public List<CdpTopologyLink> doInHibernate(Session session) throws HibernateException, SQLException {
                List<Object[]> list = session.createSQLQuery("select l.id as sourceid, " +
                        "l.nodeid as sourcenodeid, " +
                        "l.cdpcacheifindex as sourceifindex, " +
                        "l.cdpinterfacename as sourceifname, " +
                        "e.id as targetid, " +
                        "e.nodeid as targetnodeid, " +
                        "l.cdpcachedeviceport as targetifname " +
                        "from cdplink l " +
                        "right join ipinterface e " +
                        "on l.cdpcacheaddress = e.ipaddr " +
                        "where l.cdpcacheaddresstype=1;").list();

                List<CdpTopologyLink> topoLinks = new ArrayList<CdpTopologyLink>();
                for (Object[] objs : list) {
                    Integer targetId = (Integer)objs[4];
                    Integer targetNodeId =(Integer)objs[5];
                    if(targetId != null && targetNodeId != null) {
                        topoLinks.add(new CdpTopologyLink((Integer) objs[0], (Integer) objs[1], (Integer) objs[2], (String) objs[3], (Integer) objs[4], (Integer) objs[5], (String) objs[6]));
                    }
                }

                return topoLinks;
            }

        });
    }

    @Override
    public List<CdpTopologyLink> findLinksForTopologyByIds(final Integer... ids) {
        return getHibernateTemplate().execute(new HibernateCallback<List<CdpTopologyLink>>() {
            @Override
            public List<CdpTopologyLink> doInHibernate(Session session) throws HibernateException, SQLException {

                StringBuffer idList = new StringBuffer();
                String conditional = "";
                if(ids.length > 0) {
                    for (int i  = 0; i < ids.length; i++) {
                        if(i > 0) {
                            idList.append(", ");
                        }
                        idList.append(ids[i]);

                    }
                    conditional = " and (l.id in (" + idList.toString() + ") or e.id in (" + idList.toString() + "))";
                }


                List<Object[]> list = session.createSQLQuery("select l.id as sourceid, " +
                        "l.nodeid as sourcenodeid, " +
                        "l.cdpcacheifindex as sourceifindex, " +
                        "l.cdpinterfacename as sourceifname, " +
                        "e.id as targetid, " +
                        "e.nodeid as targetnodeid, " +
                        "l.cdpcachedeviceport as targetifname " +
                        "from cdplink l " +
                        "right join ipinterface e " +
                        "on l.cdpcacheaddress = e.ipaddr " +
                        "where l.cdpcacheaddresstype=1 " +
                         conditional.toString() + ";").list();

                List<CdpTopologyLink> topoLinks = new ArrayList<CdpTopologyLink>();
                for (Object[] objs : list) {
                    Integer targetId = (Integer) objs[4];
                    Integer targetNodeId = (Integer) objs[5];
                    if (targetId != null && targetNodeId != null) {
                        topoLinks.add(new CdpTopologyLink((Integer) objs[0], (Integer) objs[1], (Integer) objs[2], (String) objs[3], (Integer) objs[4], (Integer) objs[5], (String) objs[6]));
                    }
                }

                return topoLinks;
            }

        });
    }

    @Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
		for (CdpLink link: find("from CdpLink cdpLink where cdpLink.node.id = ? and cdpLink.cdpLinkLastPollTime < ?",nodeId,now)) {
			delete(link);
		}
	}
    
}
