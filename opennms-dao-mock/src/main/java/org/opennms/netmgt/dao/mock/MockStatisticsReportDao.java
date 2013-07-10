package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.StatisticsReportDao;
import org.opennms.netmgt.model.StatisticsReport;

public class MockStatisticsReportDao extends AbstractMockDao<StatisticsReport, Integer> implements StatisticsReportDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final StatisticsReport report) {
        return report.getId();
    }

    @Override
    protected void generateId(final StatisticsReport report) {
        report.setId(m_id.incrementAndGet());
    }

}
