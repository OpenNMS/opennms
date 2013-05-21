/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
