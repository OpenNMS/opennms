package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.VlanDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsVlan;

public class MockVlanDao extends AbstractMockDao<OnmsVlan,Integer> implements VlanDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsVlan entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final OnmsVlan entity) {
        entity.setId(m_id.getAndIncrement());
    }

    @Override
    public void markDeletedIfNodeDeleted() {
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && "D".equals(vlan.getNode().getType())) {
                vlan.setStatus(StatusType.DELETED);
            }
        }
    }

    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        for (final OnmsVlan vlan : getVlansForNodeIdIfOlderThan(nodeId, scanTime)) {
            if (StatusType.ACTIVE.equals(vlan.getStatus())) {
                vlan.setStatus(StatusType.INACTIVE);
            }
        }
    }

    @Override
    public void deleteForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsVlan> vlans = getVlansForNodeIdIfOlderThan(nodeId, scanTime);
        for (final OnmsVlan vlan : vlans) {
            if (!StatusType.ACTIVE.equals(vlan.getStatus())) {
                delete(vlan);
            }
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeId, final StatusType action) {
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && vlan.getNode().getId() == nodeId) {
                vlan.setStatus(action);
            }
        }
    }

    private List<OnmsVlan> getVlansForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsVlan> vlans = new ArrayList<OnmsVlan>();
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && vlan.getNode().getId() != nodeId) continue;
            if (vlan.getLastPollTime() != null || vlan.getLastPollTime().before(scanTime)) {
                vlans.add(vlan);
            }
        }
        return vlans;
    }

    @Override
    public OnmsVlan findByNodeAndVlan(final Integer nodeId, final Integer vlanId) {
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && vlan.getNode().getId() != nodeId) continue;
            if (vlan.getVlanId() == vlanId) {
                return vlan;
            }
        }
        return null;
    }

}
