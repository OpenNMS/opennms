package org.opennms.report.inventory.svclayer;

import org.opennms.report.inventory.InventoryReportRunner;

/**
 * <p>DefaultInventoryReportService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultInventoryReportService implements InventoryReportService {

    InventoryReportRunner m_reportRunner;
    
    /**
     * <p>getReportRunner</p>
     *
     * @return a {@link org.opennms.report.inventory.InventoryReportRunner} object.
     */
    public InventoryReportRunner getReportRunner() {
        return m_reportRunner;
    }
    
    /**
     * <p>setReportRunner</p>
     *
     * @param reportRunner a {@link org.opennms.report.inventory.InventoryReportRunner} object.
     */
    public void setReportRunner(InventoryReportRunner reportRunner) {
        m_reportRunner = reportRunner;
    }
        
    
    /** {@inheritDoc} */
    public boolean runReport(InventoryReportCriteria criteria){
        
        m_reportRunner.setUser(criteria.getUser());
        m_reportRunner.setTheDate(criteria.getTheDate());
        m_reportRunner.setReportEmail(criteria.getReportEmail());
        m_reportRunner.setReportFormat(criteria.getReportFormat());
        m_reportRunner.setReportRequestDate(criteria.getReportRequestDate());
        m_reportRunner.setTheField(criteria.getTheField());
        new Thread(m_reportRunner, m_reportRunner.getClass().getSimpleName()).start();    
        
        return true;
    }


}
