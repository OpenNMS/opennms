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
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.AllRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
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
    @Override
    public Collection<DataLinkInterface> findAll(final Integer offset, final Integer limit) {
        return getHibernateTemplate().execute(new HibernateCallback<Collection<DataLinkInterface>>() {

            @Override
            public Collection<DataLinkInterface> doInHibernate(Session session) throws HibernateException {
                return session.createCriteria(DataLinkInterface.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public DataLinkInterface findById(final Integer id) {
        return findUnique("from DataLinkInterface as dli where dli.id = ?", id);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<DataLinkInterface> findByNodeId(final Integer nodeId) {
        return find("from DataLinkInterface as dli where dli.node.id = ?", nodeId);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<DataLinkInterface> findByNodeParentId(final Integer nodeParentId) {
        return find("from DataLinkInterface as dli where dli.nodeParentId = ?", nodeParentId);
    }

    /** {@inheritDoc} */
    @Override
    public DataLinkInterface findByNodeIdAndIfIndex(final Integer nodeId, final Integer ifIndex) {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.eq("node.id", nodeId);
        builder.eq("ifIndex", ifIndex);

        final List<DataLinkInterface> interfaces = findMatching(builder.toCriteria());
        if (interfaces.size() > 0) {
            return interfaces.get(0);
        }
        return null;
    }

    @Override
    public void markDeletedIfNodeDeleted() {
	final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.eq("node.type", "D");
        
        for (final DataLinkInterface dataLinkIface : findMatching(builder.toCriteria())) {
        	dataLinkIface.setStatus(StatusType.DELETED);
        	saveOrUpdate(dataLinkIface);
        }
    }

    @Override
    public void deactivateIfOlderThan(final Date scanTime, String source) {
        // UPDATE datalinkinterface set status = 'N'  WHERE lastpolltime < ? AND status = 'A'
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.eq("source",source);
        builder.lt("lastPollTime", scanTime);
        builder.eq("status", StatusType.ACTIVE);

        for (final DataLinkInterface iface : findMatching(builder.toCriteria())) {
            iface.setStatus(StatusType.INACTIVE);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void deleteIfOlderThan(final Date scanTime, String source) {
        // DELETE datalinkinterface WHERE lastpolltime < ? AND status <> 'A'

        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.eq("source", source);
        builder.lt("lastPollTime", scanTime);
        builder.ne("status", StatusType.DELETED);

        for (final DataLinkInterface iface : findMatching(builder.toCriteria())) {
            delete(iface);
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeid, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE nodeid = ? OR nodeparentid = ?
        setStatusForNode(nodeid, null, action);
    }

    @Override
    public void setStatusForNode(final Integer nodeid, final String source, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE (nodeid = ? OR nodeparentid = ?) and source = ?
        
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        if (source != null) builder.eq("source", source);
        builder.or(new EqRestriction("node.id", nodeid), new EqRestriction("nodeParentId", nodeid));
        
        for (final DataLinkInterface iface : findMatching(builder.toCriteria())) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeid, final Integer ifIndex, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE (nodeid = ? and ifindex = ?) OR (nodeparentid = ? AND parentifindex = ?)

        setStatusForNodeAndIfIndex(nodeid, ifIndex, null, action);
    }
    
    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeid, final Integer ifIndex, String source, final StatusType action) {
        // UPDATE datalinkinterface set status = ? WHERE source = ? and ((nodeid = ? and ifindex = ?) OR (nodeparentid = ? AND parentifindex = ?))

        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        if (source != null) builder.eq("source", source);
        builder.or(
            new AllRestriction(
                new EqRestriction("node.id", nodeid),
                new EqRestriction("ifIndex", ifIndex)
            ), 
            new AllRestriction(
                new EqRestriction("nodeParentId", nodeid),
                new EqRestriction("parentIfIndex", ifIndex)
            )
        );
        
        for (final DataLinkInterface iface : findMatching(builder.toCriteria())) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

}
