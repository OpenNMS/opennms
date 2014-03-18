package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IExpression;

@XmlRootElement(name="expression")
@XmlAccessorType(XmlAccessType.NONE)
public class Expression implements IExpression {

    @XmlElement(name="template")
    public String m_template;

    public String getTemplate() {
        return m_template;
    }

    public void setTemplate(final String template) {
        this.m_template = template;
    }

    public static Expression asExpression(final IExpression expression) {
        if (expression == null) return null;
        if (expression instanceof Expression) {
            return (Expression)expression;
        }
        final Expression newExpression = new Expression();
        newExpression.setTemplate(expression.getTemplate());
        return newExpression;
    }

}
