/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.AllRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;

public class MockDataLinkInterfaceDao extends AbstractMockDao<DataLinkInterface, Integer> implements DataLinkInterfaceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final DataLinkInterface dli) {
        dli.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final DataLinkInterface dli) {
        return dli.getId();
    }

    @Override
    public Collection<DataLinkInterface> findAll(final Integer offset, final Integer limit) {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.offset(offset).limit(limit);
        return findMatching(builder.toCriteria());
    }

    @Override
    public DataLinkInterface findById(final Integer id) {
        return get(id);
    }

    @Override
    public Collection<DataLinkInterface> findByNodeId(final Integer nodeId) {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.eq("node.id", nodeId);
        return findMatching(builder.toCriteria());
    }

    @Override
    public Collection<DataLinkInterface> findByNodeParentId(final Integer nodeParentId) {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.eq("nodeParentId", nodeParentId);
        return findMatching(builder.toCriteria());
    }

    @Override
    public void markDeletedIfNodeDeleted() {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.alias("node", "node").eq("node.type", "D");
        
        for (final DataLinkInterface dataLinkIface : findMatching(builder.toCriteria())) {
                dataLinkIface.setStatus(StatusType.DELETED);
                saveOrUpdate(dataLinkIface);
        }
    }

    @Override
    public Collection<DataLinkInterface> findByNodeIdAndIfIndex(final Integer nodeId, final Integer ifIndex) {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.eq("node.id", nodeId);
        builder.eq("ifIndex", ifIndex);

        return findMatching(builder.toCriteria());
    }

    @Override
    public Collection<DataLinkInterface> findByParentNodeIdAndIfIndex(final Integer nodeParentId, final Integer parentIfIndex) {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.eq("nodeParentId", nodeParentId);
        builder.eq("parentIfIndex", parentIfIndex);

        return findMatching(builder.toCriteria());
    }

    @Override
    public void deactivateIfOlderThan(final Date scanTime, final String source) {
        final CriteriaBuilder builder = new CriteriaBuilder(DataLinkInterface.class);
        builder.eq("source", source);
        builder.lt("lastPollTime", scanTime);
        builder.eq("status", StatusType.ACTIVE);

        for (final DataLinkInterface iface : findMatching(builder.toCriteria())) {
            iface.setStatus(StatusType.INACTIVE);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void deleteIfOlderThan(final Date scanTime, final String source) {
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
