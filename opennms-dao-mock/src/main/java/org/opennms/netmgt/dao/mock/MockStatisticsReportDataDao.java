package org.opennms.netmgt.dao.mock;

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.StatisticsReportDataDao;
import org.opennms.netmgt.model.StatisticsReportData;
import org.springframework.beans.factory.InitializingBean;

public class MockStatisticsReportDataDao implements InitializingBean, StatisticsReportDataDao {

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
    public void delete(StatisticsReportData entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(Integer key) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<StatisticsReportData> findAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<StatisticsReportData> findMatching(Criteria criteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(Criteria onmsCrit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public StatisticsReportData get(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public StatisticsReportData load(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void save(StatisticsReportData entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveOrUpdate(StatisticsReportData entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void update(StatisticsReportData entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
