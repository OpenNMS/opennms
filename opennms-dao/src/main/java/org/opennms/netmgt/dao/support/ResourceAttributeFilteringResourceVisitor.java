/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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
    @Override
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
    @Override
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
