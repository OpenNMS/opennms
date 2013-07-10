package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.StpInterfaceDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsStpInterface;

public class MockStpInterfaceDao extends AbstractMockDao<OnmsStpInterface,Integer> implements StpInterfaceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsStpInterface entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final OnmsStpInterface entity) {
        entity.setId(m_id.getAndIncrement());
    }

    @Override
    public void markDeletedIfNodeDeleted() {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && "D".equals(iface.getNode().getType())) {
                iface.setStatus(StatusType.DELETED);
            }
        }
    }

    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        for (final OnmsStpInterface iface : getStpInterfacesForNodeIdIfOlderThan(nodeId, scanTime)) {
            if (StatusType.ACTIVE.equals(iface.getStatus())) {
                iface.setStatus(StatusType.INACTIVE);
            }
        }
    }

    @Override
    public void deleteForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsStpInterface> ifaces = getStpInterfacesForNodeIdIfOlderThan(nodeId, scanTime);
        for (final OnmsStpInterface iface : ifaces) {
            if (!StatusType.ACTIVE.equals(iface.getStatus())) {
                delete(iface);
            }
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeId, final StatusType action) {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId) {
                iface.setStatus(action);
            }
        }
    }

    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeId, final Integer ifIndex, final StatusType action) {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId) {
                if (iface.getIfIndex() == ifIndex) {
                    iface.setStatus(action);
                }
            }
        }
    }

    @Override
    public OnmsStpInterface findByNodeAndVlan(final Integer nodeId, final Integer bridgePort, final Integer vlan) {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId) {
                if (iface.getBridgePort() == bridgePort && iface.getVlan() == vlan) {
                    return iface;
                }
            }
        }
        return null;
    }


    private List<OnmsStpInterface> getStpInterfacesForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsStpInterface> ifaces = new ArrayList<OnmsStpInterface>();
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() != nodeId) continue;
            if (iface.getLastPollTime() != null || iface.getLastPollTime().before(scanTime)) {
                ifaces.add(iface);
            }
        }
        return ifaces;
    }

}
