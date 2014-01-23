package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.LinkStateDao;
import org.opennms.netmgt.model.OnmsLinkState;
import org.springframework.beans.factory.InitializingBean;

public class MockLinkStateDao implements InitializingBean, LinkStateDao {

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void lock() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void initialize(Object obj) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(OnmsLinkState entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(Integer key) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsLinkState> findAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsLinkState> findMatching(Criteria criteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(Criteria onmsCrit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsLinkState get(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsLinkState load(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void save(OnmsLinkState entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveOrUpdate(OnmsLinkState entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void update(OnmsLinkState entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsLinkState> findAll(Integer offset, Integer limit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsLinkState findById(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsLinkState findByDataLinkInterfaceId(Integer interfaceId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsLinkState> findByNodeId(Integer nodeId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsLinkState> findByNodeParentId(Integer nodeParentId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
