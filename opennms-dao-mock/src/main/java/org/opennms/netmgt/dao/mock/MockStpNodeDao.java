package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.StpNodeDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsStpNode;

public class MockStpNodeDao extends AbstractMockDao<OnmsStpNode,Integer> implements StpNodeDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsStpNode entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final OnmsStpNode entity) {
        entity.setId(m_id.getAndIncrement());
    }


    @Override
    public void markDeletedIfNodeDeleted() {
        for (final OnmsStpNode node : findAll()) {
            if (node.getNode() != null && "D".equals(node.getNode().getType())) {
                node.setStatus(StatusType.DELETED);
            }
        }
    }

    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        for (final OnmsStpNode node : getStpNodesForNodeIdIfOlderThan(nodeId, scanTime)) {
            if (StatusType.ACTIVE.equals(node.getStatus())) {
                node.setStatus(StatusType.INACTIVE);
            }
        }
    }

    @Override
    public void deleteForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsStpNode> nodes = getStpNodesForNodeIdIfOlderThan(nodeId, scanTime);
        for (final OnmsStpNode node : nodes) {
            if (!StatusType.ACTIVE.equals(node.getStatus())) {
                delete(node);
            }
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeId, final StatusType action) {
        for (final OnmsStpNode node : findAll()) {
            if (node.getNode() != null && node.getNode().getId() == nodeId) {
                node.setStatus(action);
            }
        }
    }

    @Override
    public OnmsStpNode findByNodeAndVlan(final Integer nodeId, final Integer baseVlan) {
        for (final OnmsStpNode node : findAll()) {
            if (node.getNode() != null && node.getNode().getId() == nodeId) {
                if (node.getBaseVlan() == baseVlan) return node;
            }
        }
        return null;
    }

    private List<OnmsStpNode> getStpNodesForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsStpNode> nodes = new ArrayList<OnmsStpNode>();
        for (final OnmsStpNode node : findAll()) {
            if (node.getNode() != null && node.getNode().getId() != nodeId) continue;
            if (node.getLastPollTime() != null || node.getLastPollTime().before(scanTime)) {
                nodes.add(node);
            }
        }
        return nodes;
    }

}
