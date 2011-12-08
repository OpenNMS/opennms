package org.opennms.features.reporting.repository;

import java.io.InputStream;
import java.util.List;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;

public interface ReportRepository {
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    List <BasicReportDefinition> getReports();
    
    /**
     * <p>getOnlineReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    List <BasicReportDefinition> getOnlineReports();
    
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

    String getEngine(String id);
    
    InputStream getTemplateStream(String id);

    String getRepositoryId();
}
