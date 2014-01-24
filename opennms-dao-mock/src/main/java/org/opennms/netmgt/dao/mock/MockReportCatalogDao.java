package org.opennms.netmgt.dao.mock;

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.springframework.beans.factory.InitializingBean;

public class MockReportCatalogDao implements InitializingBean, ReportCatalogDao {

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
    public void delete(ReportCatalogEntry entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<ReportCatalogEntry> findAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<ReportCatalogEntry> findMatching(Criteria criteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(Criteria onmsCrit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public ReportCatalogEntry get(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public ReportCatalogEntry load(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void save(ReportCatalogEntry entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveOrUpdate(ReportCatalogEntry entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void update(ReportCatalogEntry entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
