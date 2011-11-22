package org.opennms.feature.reporting.dao.jasper;

public interface JasperReportDataProvider {
    
    public String getTemplateLocation(String id);

    public String getEngine(String id);
}
