package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;

class JMXCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {

    String m_alias;
    String m_value;
    JMXCollectionResource m_resource;
    CollectionAttributeType m_attribType;

    JMXCollectionAttribute(JMXCollectionResource resource, CollectionAttributeType attribType, String alias, String value) {
        super();
        m_resource=resource;
        m_attribType=attribType;
        m_alias = alias;
        m_value = value;
    }

    @Override
    public CollectionAttributeType getAttributeType() {
        return m_attribType;
    }

    @Override
    public String getName() {
        return m_alias;
    }

    @Override
    public String getNumericValue() {
        return m_value;
    }

    @Override
    public CollectionResource getResource() {
        return m_resource;
    }

    @Override
    public String getStringValue() {
        return m_value;
    }

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    @Override
    public String getType() {
        return m_attribType.getType();
    }

    @Override
    public String toString() {
         return "alias " + m_alias + ", value " + m_value + ", resource "
             + m_resource + ", attributeType " + m_attribType;
    }

    @Override
    public String getMetricIdentifier() {
        String metricId = m_attribType.getGroupType().getName();
        metricId = metricId.replace("_type_", ":type=");
        metricId = metricId.replace("_", ".");
        metricId = metricId.concat(".");
        metricId = metricId.concat(getName());
        return "JMX_".concat(metricId);

    }

}