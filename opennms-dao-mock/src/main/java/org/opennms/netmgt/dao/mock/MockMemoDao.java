package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.MemoDao;
import org.opennms.netmgt.model.OnmsMemo;

public class MockMemoDao extends AbstractMockDao<OnmsMemo, Integer> implements MemoDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsMemo memo) {
        return memo.getId();
    }

    @Override
    protected void generateId(final OnmsMemo entity) {
        entity.setId(m_id.incrementAndGet());
    }



}
