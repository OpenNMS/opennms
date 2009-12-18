package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.opennms.netmgt.config.reportd.Report;
import org.springframework.dao.DataAccessResourceFailureException;

public interface ReportdConfigurationDao {
    
    ReportdConfiguration getConfig();
    
    Report getReport(String defName);
    
    List<Report> getReports();
        
    void reloadConfiguration() throws DataAccessResourceFailureException;
    
    boolean  getPersistFlag();
    
    String getStorageDirectory();
    

}
