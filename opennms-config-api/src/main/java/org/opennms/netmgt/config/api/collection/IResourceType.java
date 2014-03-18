package org.opennms.netmgt.config.api.collection;

public interface IResourceType {

    String getTypeName();
    String getLabel();
    IExpression getResourceNameExpression();
    IExpression getResourceLabelExpression();
    IExpression getResourceKindExpression();
    IColumn[] getColumns();

}
