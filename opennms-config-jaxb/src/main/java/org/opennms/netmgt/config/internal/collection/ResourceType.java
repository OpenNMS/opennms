package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IColumn;
import org.opennms.netmgt.config.api.collection.IExpression;
import org.opennms.netmgt.config.api.collection.IResourceType;

/**
 *  <resourceType name="hrStorageIndex" label="Storage (MIB-2 Host Resources)">
 *	  <resourceName>
 *      <m_template>${hrStorageDescr}</m_template>
 *    </resourceName>
 *    <resourceLabel><m_template>${hrStorageDescr}</m_template></resourceLabel>
 *    <resourceKind><m_template>${hrStorageType}</m_template></resourceKind>
 *    <column oid=".1.3.6.1.2.1.25.2.3.1.2" alias="hrStorageType"  type="string" />
 *    <column oid=".1.3.6.1.2.1.25.2.3.1.3" alias="hrStorageDescr" type="string" />
 *  </resourceType>
 *   
 * @author brozow
 *
 */

@XmlRootElement(name="resourceType")
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceType implements IResourceType {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlAttribute(name="label")
    private String m_label;

    @XmlElement(name="resourceName")
    private Expression m_resourceNameExpression;

    @XmlElement(name="resourceLabel")
    private Expression m_resourceLabelExpression;

    @XmlElement(name="resourceKind")
    private Expression m_resourceKindExpression;

    @XmlElement(name="column")
    private Column[] m_columns = new Column[0];

    public ResourceType() {
    }

    public ResourceType(final IResourceType type) {
        setName(type.getTypeName());
        setLabel(type.getLabel());
        setResourceNameExpression(type.getResourceNameExpression());
        setResourceLabelExpression(type.getResourceLabelExpression());
        setResourceKindExpression(type.getResourceKindExpression());
        setColumns(type.getColumns());
    }

    public String getTypeName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public IExpression getResourceNameExpression() {
        return m_resourceNameExpression;
    }

    public void setResourceNameExpression(final IExpression expression) {
        m_resourceNameExpression = Expression.asExpression(expression);
    }

    public Expression getResourceLabelExpression() {
        return m_resourceLabelExpression;
    }

    public void setResourceLabelExpression(final IExpression expression) {
        m_resourceLabelExpression = Expression.asExpression(expression);
    }

    public Expression getResourceKindExpression() {
        return m_resourceKindExpression;
    }

    public void setResourceKindExpression(IExpression expression) {
        m_resourceKindExpression = Expression.asExpression(expression);
    }

    public IColumn[] getColumns() {
        return m_columns;
    }

    public void setColumns(final IColumn[] columns) {
        m_columns = Column.asColumns(columns);
    }

}
