/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.SortedMap;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceVisitor;
import org.springframework.util.Assert;

/**
 * <p>FilterWalker class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class FilterResourceWalker implements ResourceWalker {

    private NodeDao m_nodeDao;
    private FilterDao m_filterDao;
    private String m_filter;
    private ResourceDao m_resourceDao;
    private ResourceVisitor m_visitor;

    private ResourceTreeWalker m_resourceWalker = new ResourceTreeWalker();

    /**
     * <p>walk</p>
     */
    public void walk() {
        EntityVisitor visitor = new AbstractEntityVisitor() {
            @Override
            public void visitNode(OnmsNode node) {
                walk(node);
            }

        };

        SortedMap<Integer, String> map = getFilterDao().getNodeMap(m_filter);
        for (final Integer nodeId : map.keySet()) {
            final OnmsNode node = getNodeDao().load(nodeId);
            visitor.visitNode(node);
        }
    }

    /**
     * <p>walk</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void walk(OnmsNode node) {
        OnmsResource resource = getResourceDao().getResourceForNode(node);
        m_resourceWalker.walk(Collections.singleton(resource));
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_resourceDao !=  null, "property resourceDao must be set to a non-null value");
        Assert.state(m_visitor !=  null, "property visitor must be set to a non-null value");
        Assert.state(m_filterDao !=  null, "property filterDao must be set to a non-null value");
        Assert.state(m_nodeDao !=  null, "property nodeDao must be set to a non-null value");
        Assert.state(m_filter !=  null, "property filter must be set to a non-null value");

        m_resourceWalker.setResourceDao(getResourceDao());
        m_resourceWalker.setVisitor(getVisitor());
        m_resourceWalker.afterPropertiesSet();
    }

    /**
     * @return the nodeDao
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * @param nodeDao the nodeDao to set
     */
    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
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

    /**
     * <p>getFilterDao</p>
     *
     * @return a {@link org.opennms.netmgt.filter.FilterDao} object.
     */
    public FilterDao getFilterDao() {
        return m_filterDao;
    }

    /**
     * <p>setFilterDao</p>
     *
     * @param filterDao a {@link org.opennms.netmgt.filter.FilterDao} object.
     */
    public void setFilterDao(FilterDao filterDao) {
        m_filterDao = filterDao;
    }

    /**
     * <p>getFilter</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilter() {
        return m_filter;
    }

    /**
     * <p>setFilter</p>
     *
     * @param filter a {@link java.lang.String} object.
     */
    public void setFilter(String filter) {
        m_filter = filter;
    }
}
