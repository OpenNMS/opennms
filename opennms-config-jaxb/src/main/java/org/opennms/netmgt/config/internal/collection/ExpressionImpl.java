/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
