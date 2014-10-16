/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IExpression;

@XmlRootElement(name="expression")
@XmlAccessorType(XmlAccessType.NONE)
public class ExpressionImpl implements IExpression {

    @XmlElement(name="template")
    public String m_template;

    public ExpressionImpl() {
    }

    public ExpressionImpl(final String template) {
        m_template = template;
    }

    public String getTemplate() {
        return m_template;
    }

    public void setTemplate(final String template) {
        m_template = template;
    }

    public static ExpressionImpl asExpression(final IExpression expression) {
        if (expression == null) return null;
        if (expression instanceof ExpressionImpl) {
            return (ExpressionImpl)expression;
        }
        final ExpressionImpl newExpression = new ExpressionImpl();
        newExpression.setTemplate(expression.getTemplate());
        return newExpression;
    }

    @Override
    public String toString() {
        return "ExpressionImpl [template=" + m_template + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_template == null) ? 0 : m_template.hashCode());
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
        if (!(obj instanceof ExpressionImpl)) {
            return false;
        }
        final ExpressionImpl other = (ExpressionImpl) obj;
        if (m_template == null) {
            if (other.m_template != null) {
                return false;
            }
        } else if (!m_template.equals(other.m_template)) {
            return false;
        }
        return true;
    }

}
