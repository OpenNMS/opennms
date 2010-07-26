package org.opennms.report.configuration.svclayer;


import org.opennms.report.configuration.ConfigurationReportRunner;

/**
 * <p>DefaultConfigurationReportService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultConfigurationReportService implements ConfigurationReportService {

    ConfigurationReportRunner m_reportRunner;
    
    /**
     * <p>getReportRunner</p>
     *
     * @return a {@link org.opennms.report.configuration.ConfigurationReportRunner} object.
     */
    public ConfigurationReportRunner getReportRunner() {
        return m_reportRunner;
    }
    
    /**
     * <p>setReportRunner</p>
     *
     * @param reportRunner a {@link org.opennms.report.configuration.ConfigurationReportRunner} object.
     */
    public void setReportRunner(ConfigurationReportRunner reportRunner) {
        m_reportRunner = reportRunner;
    }
        
    /** {@inheritDoc} */
    public boolean runReport(ConfigurationReportCriteria criteria){
    
          m_reportRunner.setUser(criteria.getUser());
          m_reportRunner.setTheDate(criteria.getTheDate());
          m_reportRunner.setReportEmail(criteria.getReportEmail());
          m_reportRunner.setReportFormat(criteria.getReportFormat());
          m_reportRunner.setReportRequestDate(criteria.getReportRequestDate());
            
          new Thread(m_reportRunner, m_reportRunner.getClass().getSimpleName()).start();                    
    
          return true;
    }   

}
