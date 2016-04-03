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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
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

    private static String SQL_CDP_LINK_BASE_QUERY =                         
            "select l.id as sourceid, " +
            "l.nodeid as sourcenodeid, " +
            "n.nodelabel as sourcenodelabel, " +
            "n.nodesysoid as sourcenodesysoid, " +
            "n.nodesyslocation as sourcenodelocation, " +
            "n.nodetype as sourcenodetype, " +
            "l.cdpcacheifindex as sourceifindex, " +
            "l.cdpinterfacename as sourceifname, " +
            "e.id as targetid, " +
            "e.nodeid as targetnodeid, " +
            "np.nodelabel as targetnodelabel, " +
            "np.nodesysoid as targetnodesysoid, " +
            "np.nodesyslocation as targetnodelocation, " +
            "np.nodetype as targetnodetype, " +
            "l.cdpcachedeviceport as targetifname, " +
            "l.cdplinklastpolltime as lastPollTime " +
            "from cdplink l " +
            "left join node n " +
            "on l.nodeid = n.nodeid " +
            "right join ipinterface e " +
            "on l.cdpcacheaddress = e.ipaddr " +
            "left join node np " +
            "on e.nodeid = np.nodeid " +
            "where l.cdpcacheaddresstype=1";

    private List<CdpTopologyLink> convertObjectToTopologyLink(List<Object[]> list) {
        List<CdpTopologyLink> topoLinks = new ArrayList<CdpTopologyLink>();
        for (Object[] objs : list) {
            Integer targetId = (Integer)objs[8];
            Integer targetNodeId =(Integer)objs[9];
            if(targetId != null && targetNodeId != null) {
                topoLinks.add(
                              new CdpTopologyLink(
                                (Integer) objs[0], 
                                (Integer) objs[1], 
                                (String) objs[2], 
                                (String) objs[3],
                                (String) objs[4],
                                NodeType.getNodeTypeFromChar((char)objs[5]),
                                (Integer) objs[6], 
                                (String) objs[7], 
                                (Integer) objs[8], 
                                (Integer) objs[9], 
                                (String) objs[10], 
                                (String) objs[11],
                                (String) objs[12],
                                NodeType.getNodeTypeFromChar((char)objs[13]),
                                (String) objs[14],
                                (Date) objs[15]
                                        )
                              );
            }
        }

        return topoLinks;
        
    }
    
    @Override
    public List<CdpTopologyLink> findLinksForTopology() {
        return getHibernateTemplate().execute(new HibernateCallback<List<CdpTopologyLink>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<CdpTopologyLink> doInHibernate(Session session) throws HibernateException, SQLException {
               Map<String, CdpTopologyLink> mapToLink = new HashMap<String,CdpTopologyLink>();
               List<CdpTopologyLink> alllinks = convertObjectToTopologyLink(session.createSQLQuery(SQL_CDP_LINK_BASE_QUERY+";").list());
               for (CdpTopologyLink link: alllinks){
                    String sourcekey=link.getSrcNodeId()+link.getSrcIfName();
                    String targetkey=link.getTargetNodeId()+link.getTargetIfName();
                    if (mapToLink.containsKey(sourcekey)) {
                        if (link.getLastPollTime().after(mapToLink.get(sourcekey).getLastPollTime())) {
                            CdpTopologyLink oldlink = mapToLink.get(sourcekey);
                            String oldsourcekey=oldlink.getSrcNodeId()+oldlink.getSrcIfName();
                            String oldtargetkey=oldlink.getTargetNodeId()+oldlink.getTargetIfName();
                            mapToLink.remove(oldsourcekey);
                            mapToLink.remove(oldtargetkey);
                            mapToLink.put(sourcekey, link);
                            mapToLink.put(targetkey, link);
                            continue;
                        }
                    } 
                    if (mapToLink.containsKey(targetkey)) {
                        if (link.getLastPollTime().after(mapToLink.get(targetkey).getLastPollTime())) {
                            CdpTopologyLink oldlink = mapToLink.get(targetkey);
                            String oldsourcekey=oldlink.getSrcNodeId()+oldlink.getSrcIfName();
                            String oldtargetkey=oldlink.getTargetNodeId()+oldlink.getTargetIfName();
                            mapToLink.remove(oldsourcekey);
                            mapToLink.remove(oldtargetkey);
                            mapToLink.put(sourcekey, link);
                            mapToLink.put(targetkey, link);
                            continue;
                        }
                    }
                    mapToLink.put(sourcekey, link);
                    mapToLink.put(targetkey, link);
               }
               List<Integer> ids = new ArrayList<Integer>();
               List<CdpTopologyLink> links = new ArrayList<CdpTopologyLink>();
               for (CdpTopologyLink link: mapToLink.values()) {
                    if (ids.contains(link.getSourceId()))
                        continue;
                    links.add(link);
                    ids.add(link.getSourceId());
               }
               return links;
            }
        });
    }

    @Override
    public List<CdpTopologyLink> findLinksForTopologyByIds(final Integer... ids) {
        return getHibernateTemplate().execute(new HibernateCallback<List<CdpTopologyLink>>() {
            @Override
            @SuppressWarnings("unchecked")
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


                return convertObjectToTopologyLink(session.createSQLQuery(SQL_CDP_LINK_BASE_QUERY +
                         conditional.toString() + ";").list());


            }

        });
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
	for (CdpLink link: find("from CdpLink cdpLink where cdpLink.node.id = ? and cdpLink.cdpLinkLastPollTime < ?",nodeId,now)) {
	    delete(link);
	}
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        for (CdpLink link: find("from CdpLink cdpLink where cdpLink.node.id = ? ",nodeId)) {
            delete(link);
        }
    }

}
