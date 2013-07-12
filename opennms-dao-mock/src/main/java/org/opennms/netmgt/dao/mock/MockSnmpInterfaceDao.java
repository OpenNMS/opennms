package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class MockSnmpInterfaceDao extends AbstractMockDao<OnmsSnmpInterface, Integer> implements SnmpInterfaceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsSnmpInterface iface) {
        iface.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsSnmpInterface iface) {
        return iface.getId();
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndIfIndex(final Integer nodeId, final Integer ifIndex) {
        for (final OnmsSnmpInterface iface : findAll()) {
            if (nodeId.equals(iface.getNode().getId()) && ifIndex.equals(iface.getIfIndex())) {
                return iface;
            }
        }
        return null;
    }

    @Override
    public OnmsSnmpInterface findByForeignKeyAndIfIndex(final String foreignSource, final String foreignId, final Integer ifIndex) {
        for (final OnmsSnmpInterface iface : findAll()) {
            final OnmsNode node = iface.getNode();
            if (foreignSource.equals(node.getForeignSource()) && foreignId.equals(node.getForeignId()) && ifIndex.equals(iface.getIfIndex())) {
                return iface;
            }
        }
        return null;
    }

}
