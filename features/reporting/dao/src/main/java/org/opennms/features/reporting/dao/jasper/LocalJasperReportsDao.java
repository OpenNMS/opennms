package org.opennms.features.reporting.dao.jasper;

import java.io.InputStream;

public interface LocalJasperReportsDao {
    
    /**
     * <p>getEngine</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getEngine(String id);

    InputStream getTemplateStream(String id);

    String getTemplateLocation(String id);
}
