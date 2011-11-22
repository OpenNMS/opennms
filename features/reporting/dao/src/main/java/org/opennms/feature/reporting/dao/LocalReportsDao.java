package org.opennms.feature.reporting.dao;

import java.util.List;

import org.opennms.features.reporting.model.Report;

public interface LocalReportsDao {
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    List <Report> getReports();
    
    /**
     * <p>getOnlineReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    List <Report> getOnlineReports();
    
    /**
     * <p>getReportService</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getReportService(String id);
    
    /**
     * <p>getDisplayName</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getDisplayName(String id);
}
