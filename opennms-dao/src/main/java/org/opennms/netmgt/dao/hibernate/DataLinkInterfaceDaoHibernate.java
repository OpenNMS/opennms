/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsCriteria;
import org.springframework.orm.hibernate3.HibernateCallback;

public class DataLinkInterfaceDaoHibernate extends AbstractDaoHibernate<DataLinkInterface, Integer> implements DataLinkInterfaceDao {
    /**
     * <p>Constructor for DataLinkInterfaceDaoHibernate.</p>
     */
    public DataLinkInterfaceDaoHibernate() {
        super(DataLinkInterface.class);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Collection<DataLinkInterface> findAll(final Integer offset, final Integer limit) {
        return getHibernateTemplate().execute(new HibernateCallback<Collection<DataLinkInterface>>() {

            public Collection<DataLinkInterface> doInHibernate(Session session) throws HibernateException {
                return session.createCriteria(DataLinkInterface.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }

    /** {@inheritDoc} */
    public DataLinkInterface findById(final Integer id) {
        return findUnique("from DataLinkInterface as dli where dli.id = ?", id);
    }

    /** {@inheritDoc} */
    public Collection<DataLinkInterface> findByNodeId(final Integer nodeId) {
        return find("from DataLinkInterface as dli where dli.node.id = ?", nodeId);
    }

    /** {@inheritDoc} */
    public Collection<DataLinkInterface> findByNodeParentId(final Integer nodeParentId) {
        return find("from DataLinkInterface as dli where dli.nodeParentId = ?", nodeParentId);
    }

    /** {@inheritDoc} */
    public DataLinkInterface findByNodeIdAndIfIndex(final Integer nodeId, final Integer ifIndex) {
        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("ifIndex", ifIndex));

        final List<DataLinkInterface> interfaces = findMatching(criteria);
        if (interfaces.size() > 0) {
            return interfaces.get(0);
        }
        return null;
    }

    @Override
    public void markDeletedIfNodeDeleted() {
	final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.type", "D"));
        
        for (final DataLinkInterface dataLinkIface : findMatching(criteria)) {
        	dataLinkIface.setStatus(StatusType.DELETED);
        	saveOrUpdate(dataLinkIface);
        }
    }

    @Override
    public void deactivateIfOlderThan(final Date scanTime, String source) {
        // UPDATE datalinkinterface set status = 'N'  WHERE lastpolltime < ? AND status = 'A'

        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.eq("source",source));
        criteria.add(Restrictions.lt("lastPollTime", scanTime));
        criteria.add(Restrictions.eq("status", StatusType.ACTIVE));

        for (final DataLinkInterface iface : findMatching(criteria)) {
            iface.setStatus(StatusType.INACTIVE);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void deleteIfOlderThan(final Date scanTime, String source) {
        // DELETE datalinkinterface WHERE lastpolltime < ? AND status <> 'A'

        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.eq("source",source));
        criteria.add(Restrictions.lt("lastPollTime", scanTime));
        criteria.add(Restrictions.not(Restrictions.eq("status", StatusType.DELETED)));

        for (final DataLinkInterface iface : findMatching(criteria)) {
            delete(iface);
        }
    }


    @Override
    public void setStatusForNode(final Integer nodeid, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE nodeid = ? OR nodeparentid = ?
        
        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.or(Restrictions.eq("node.id", nodeid), Restrictions.eq("nodeParentId", nodeid)));
        
        for (final DataLinkInterface iface : findMatching(criteria)) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeid, String source, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE (nodeid = ? OR nodeparentid = ?) and source = ?
        
        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.and(Restrictions.eq("source",source),
        		Restrictions.or(Restrictions.eq("node.id", nodeid), Restrictions.eq("nodeParentId", nodeid))));
        
        for (final DataLinkInterface iface : findMatching(criteria)) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeid, final Integer ifIndex, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE (nodeid = ? and ifindex = ?) OR (nodeparentid = ? AND parentifindex = ?)

        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(
            Restrictions.or(
                Restrictions.and(
                    Restrictions.eq("node.id", nodeid),
                    Restrictions.eq("ifIndex", ifIndex)
                ),
                Restrictions.and(
                    Restrictions.eq("nodeParentId", nodeid),
                    Restrictions.eq("parentIfIndex", ifIndex)
                )
            )
        );
        
        for (final DataLinkInterface iface : findMatching(criteria)) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }
    
    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeid, final Integer ifIndex, String source, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE source = ? and ((nodeid = ? and ifindex = ?) OR (nodeparentid = ? AND parentifindex = ?))

        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.and(Restrictions.eq("source",source),
            Restrictions.or(
                Restrictions.and(
                    Restrictions.eq("node.id", nodeid),
                    Restrictions.eq("ifIndex", ifIndex)
                ),
                Restrictions.and(
                    Restrictions.eq("nodeParentId", nodeid),
                    Restrictions.eq("parentIfIndex", ifIndex)
                )
            )
        ));
        
        for (final DataLinkInterface iface : findMatching(criteria)) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

}
