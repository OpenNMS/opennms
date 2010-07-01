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

import java.util.Collection;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceVisitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>ResourceTreeWalker class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class ResourceTreeWalker implements InitializingBean {
    private ResourceDao m_resourceDao;
    private ResourceVisitor m_visitor;

    /**
     * <p>walk</p>
     */
    public void walk() {
        walk(m_resourceDao.findTopLevelResources());
    }
    
    /**
     * <p>walk</p>
     *
     * @param resources a {@link java.util.Collection} object.
     */
    public void walk(Collection<OnmsResource> resources) {
        for (OnmsResource resource : resources) {
            m_visitor.visit(resource);
            walk(resource.getChildResources());
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.state(m_resourceDao !=  null, "property resourceDao must be set to a non-null value");
        Assert.state(m_visitor !=  null, "property visitor must be set to a non-null value");
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>getVisitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public ResourceVisitor getVisitor() {
        return m_visitor;
    }

    /**
     * <p>setVisitor</p>
     *
     * @param visitor a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public void setVisitor(ResourceVisitor visitor) {
        m_visitor = visitor;
    }
}
