package org.opennms.features.reporting.model.jasperreport;

import javax.xml.bind.annotation.XmlAttribute;

public interface JasperReportDefinition {

    @XmlAttribute(name = "id")
    public abstract String getId();

    @XmlAttribute(name = "template")
    public abstract String getTemplate();

    @XmlAttribute(name = "engine")
    public abstract String getEngine();

    public abstract void setId(String id);

    public abstract void setTemplate(String template);

    public abstract void setEngine(String engine);

}