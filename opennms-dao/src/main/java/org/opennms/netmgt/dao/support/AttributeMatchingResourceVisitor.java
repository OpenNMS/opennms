/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 05: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
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
