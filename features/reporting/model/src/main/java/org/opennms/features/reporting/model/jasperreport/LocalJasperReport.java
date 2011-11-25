package org.opennms.features.reporting.model.jasperreport;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "report")
public class LocalJasperReport implements JasperReportDefinition {

    private String m_id;

    private String m_template;

    private String m_engine;

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#getId()
     */
    @Override
    @XmlAttribute(name = "id")
    public String getId() {
        return m_id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#getTemplate()
     */
    @Override
    @XmlAttribute(name = "template")
    public String getTemplate() {
        return m_template;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#getEngine()
     */
    @Override
    @XmlAttribute(name = "engine")
    public String getEngine() {
        return m_engine;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        m_id = id;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#setTemplate(java.lang.String)
     */
    @Override
    public void setTemplate(String template) {
        m_template = template;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#setEngine(java.lang.String)
     */
    @Override
    public void setEngine(String engine) {
        m_engine = engine;
    }
}
