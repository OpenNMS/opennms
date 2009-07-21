package org.opennms.report.configuration.svclayer;


import org.opennms.report.configuration.ConfigurationReportRunner;

public class DefaultConfigurationReportService implements ConfigurationReportService {

    ConfigurationReportRunner m_reportRunner;
    
    public ConfigurationReportRunner getReportRunner() {
        return m_reportRunner;
    }
    
    public void setReportRunner(ConfigurationReportRunner reportRunner) {
        m_reportRunner = reportRunner;
    }
        
    public boolean runReport(ConfigurationReportCriteria criteria){
    
          m_reportRunner.setUser(criteria.getUser());
          m_reportRunner.setTheDate(criteria.getTheDate());
          m_reportRunner.setReportEmail(criteria.getReportEmail());
          m_reportRunner.setReportFormat(criteria.getReportFormat());
          m_reportRunner.setReportRequestDate(criteria.getReportRequestDate());
            
          new Thread(m_reportRunner).start();                    
    
          return true;
    }   

}
