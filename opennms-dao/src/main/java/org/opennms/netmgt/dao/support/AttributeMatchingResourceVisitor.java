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
package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.model.AttributeVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceVisitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>AttributeMatchingResourceVisitor class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class AttributeMatchingResourceVisitor implements ResourceVisitor, InitializingBean {
    private AttributeVisitor m_attributeVisitor;
    private String m_attributeMatch;
    
    /** {@inheritDoc} */
    @Override
    public void visit(OnmsResource resource) {
        for (OnmsAttribute attribute : resource.getAttributes()) {
            if (m_attributeMatch.equals(attribute.getName())) {
                m_attributeVisitor.visit(attribute);
            }
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_attributeVisitor != null, "property attributeVisitor must be set to a non-null value");
        Assert.state(m_attributeMatch != null, "property attributeMatch must be set to a non-null value");
    }

    /**
     * <p>getAttributeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAttributeMatch() {
        return m_attributeMatch;
    }

    /**
     * <p>setAttributeMatch</p>
     *
     * @param attributeMatch a {@link java.lang.String} object.
     */
    public void setAttributeMatch(String attributeMatch) {
        m_attributeMatch = attributeMatch;
    }

    /**
     * <p>getAttributeVisitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.AttributeVisitor} object.
     */
    public AttributeVisitor getAttributeVisitor() {
        return m_attributeVisitor;
    }

    /**
     * <p>setAttributeVisitor</p>
     *
     * @param attributeVisitor a {@link org.opennms.netmgt.model.AttributeVisitor} object.
     */
    public void setAttributeVisitor(AttributeVisitor attributeVisitor) {
        m_attributeVisitor = attributeVisitor;
    }

}
