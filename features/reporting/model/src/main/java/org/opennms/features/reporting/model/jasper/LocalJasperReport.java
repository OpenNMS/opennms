package org.opennms.features.reporting.model.jasper;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "report")
public class LocalJasperReport {

    private String m_id;

    private String m_template;

    private String m_engine;

    @XmlAttribute(name = "id")
    public String getId() {
        return m_id;
    }

    @XmlAttribute(name = "template")
    public String getTemplate() {
        return m_template;
    }

    @XmlAttribute(name = "engine")
    public String getEngine() {
        return m_engine;
    }

    public void setId(String id) {
        m_id = id;
    }
    
    public void setTemplate(String template) {
        m_template = template;
    }
    
    public void setEngine(String engine) {
        m_engine = engine;
    }
}
