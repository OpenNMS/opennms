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

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceVisitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>ResourceAttributeFilteringResourceVisitor class.</p>
 *
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class ResourceAttributeFilteringResourceVisitor implements ResourceVisitor, InitializingBean {
    private ResourceVisitor m_delegatedVisitor;
    private String m_resourceAttributeKey;
    private String m_resourceAttributeValueMatch;

    /** {@inheritDoc} */
    public void visit(OnmsResource resource) {
        if (m_resourceAttributeValueMatch.equals(resource.getExternalValueAttributes().get(m_resourceAttributeKey))
                || m_resourceAttributeValueMatch.equals(resource.getStringPropertyAttributes().get(m_resourceAttributeKey))) {
            m_delegatedVisitor.visit(resource);
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_delegatedVisitor != null, "property delegatedVisitor must be set to a non-null value");
        Assert.state(m_resourceAttributeKey != null, "property resourceAttributeKey must be set to a non-null value");
        Assert.state(m_resourceAttributeValueMatch != null, "property resourceAttributeValueMatch must be set to a non-null value");
    }

    /**
     * <p>getDelegatedVisitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public ResourceVisitor getDelegatedVisitor() {
        return m_delegatedVisitor;
    }

    /**
     * <p>setDelegatedVisitor</p>
     *
     * @param delegatedVisitor a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public void setDelegatedVisitor(ResourceVisitor delegatedVisitor) {
        m_delegatedVisitor = delegatedVisitor;
    }

    /**
     * <p>getResourceAttributeKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeKey() {
        return m_resourceAttributeKey;
    }

    /**
     * <p>setResourceAttributeKey</p>
     *
     * @param resourceAttributeKey a {@link java.lang.String} object.
     */
    public void setResourceAttributeKey(String resourceAttributeKey) {
        m_resourceAttributeKey = resourceAttributeKey;
    }

    /**
     * <p>getResourceAttributeValueMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeValueMatch() {
        return m_resourceAttributeValueMatch;
    }

    /**
     * <p>setResourceAttributeValueMatch</p>
     *
     * @param resourceAttributeValueMatch a {@link java.lang.String} object.
     */
    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch) {
        m_resourceAttributeValueMatch = resourceAttributeValueMatch;
    }

}
