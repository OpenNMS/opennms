package org.opennms.netmgt.config.internal.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public ResourceType(final String name, final String label) {
        m_name = name;
        m_label = label;
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

    public void setResourceNameTemplate(final String template) {
        m_resourceNameExpression = new Expression(template);
    }

    public Expression getResourceLabelExpression() {
        return m_resourceLabelExpression;
    }

    public void setResourceLabelExpression(final IExpression expression) {
        m_resourceLabelExpression = Expression.asExpression(expression);
    }

    public void setResourceLabelTemplate(final String template) {
        m_resourceLabelExpression = new Expression(template);
    }

    public Expression getResourceKindExpression() {
        return m_resourceKindExpression;
    }

    public void setResourceKindExpression(final IExpression expression) {
        m_resourceKindExpression = Expression.asExpression(expression);
    }

    public void setResourceKindTemplate(final String template) {
        m_resourceKindExpression = new Expression(template);
    }

    public IColumn[] getColumns() {
        return m_columns;
    }

    public void setColumns(final IColumn[] columns) {
        m_columns = Column.asColumns(columns);
    }

    public void addColumn(final Column column) {
        final List<Column> columns = m_columns == null? new ArrayList<Column>() : new ArrayList<Column>(Arrays.asList(m_columns));
        columns.add(column);
        m_columns = columns.toArray(new Column[columns.size()]);
    }

    public void addColumn(final String oid, final String alias, final String type) {
        addColumn(new Column(oid, alias, type));
    }

    @Override
    public String toString() {
        return "ResourceType [name=" + m_name + ", label=" + m_label + ", resourceNameExpression=" + m_resourceNameExpression + ", resourceLabelExpression=" + m_resourceLabelExpression
                + ", resourceKindExpression=" + m_resourceKindExpression + ", columns=" + Arrays.toString(m_columns) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_columns);
        result = prime * result + ((m_label == null) ? 0 : m_label.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_resourceKindExpression == null) ? 0 : m_resourceKindExpression.hashCode());
        result = prime * result + ((m_resourceLabelExpression == null) ? 0 : m_resourceLabelExpression.hashCode());
        result = prime * result + ((m_resourceNameExpression == null) ? 0 : m_resourceNameExpression.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ResourceType)) {
            return false;
        }
        final ResourceType other = (ResourceType) obj;
        if (!Arrays.equals(m_columns, other.m_columns)) {
            return false;
        }
        if (m_label == null) {
            if (other.m_label != null) {
                return false;
            }
        } else if (!m_label.equals(other.m_label)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_resourceKindExpression == null) {
            if (other.m_resourceKindExpression != null) {
                return false;
            }
        } else if (!m_resourceKindExpression.equals(other.m_resourceKindExpression)) {
            return false;
        }
        if (m_resourceLabelExpression == null) {
            if (other.m_resourceLabelExpression != null) {
                return false;
            }
        } else if (!m_resourceLabelExpression.equals(other.m_resourceLabelExpression)) {
            return false;
        }
        if (m_resourceNameExpression == null) {
            if (other.m_resourceNameExpression != null) {
                return false;
            }
        } else if (!m_resourceNameExpression.equals(other.m_resourceNameExpression)) {
            return false;
        }
        return true;
    }

}
