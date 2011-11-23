package org.opennms.feature.reporting.dao.jasper;

import java.io.InputStream;

public interface JasperReportDataProvider {
    
    public String getTemplateLocation(String id);
    
    public InputStream getTemplateStream(String id);

    public String getEngine(String id);
}
