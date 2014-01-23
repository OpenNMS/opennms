package org.opennms.netmgt.dao.mock;

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.FilterFavoriteDao;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.netmgt.model.OnmsFilterFavorite.Page;
import org.springframework.beans.factory.InitializingBean;

public class MockFilterFavoriteDao implements InitializingBean, FilterFavoriteDao {

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
    public void delete(OnmsFilterFavorite entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(Integer key) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsFilterFavorite> findAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsFilterFavorite> findMatching(Criteria criteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(Criteria onmsCrit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsFilterFavorite get(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsFilterFavorite load(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void save(OnmsFilterFavorite entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveOrUpdate(OnmsFilterFavorite entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void update(OnmsFilterFavorite entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsFilterFavorite findBy(String userName, String filterName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsFilterFavorite> findBy(String userName, Page page) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean existsFilter(String userName, String filterName, Page page) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
