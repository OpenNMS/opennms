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

import java.util.SortedMap;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>FilterWalker class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class FilterWalker implements InitializingBean {

    private NodeDao m_nodeDao;
    private FilterDao m_filterDao;
    private String m_filter;
    private EntityVisitor m_visitor;

    /**
     * <p>walk</p>
     */
    public void walk() {
        SortedMap<Integer, String> map = getFilterDao().getNodeMap(m_filter);
        if (map != null) {
            for (final Integer nodeId : map.keySet()) {
                final OnmsNode node = getNodeDao().load(nodeId);
                m_visitor.visitNode(node);
            }
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_visitor !=  null, "property visitor must be set to a non-null value");
        Assert.state(m_filterDao !=  null, "property filterDao must be set to a non-null value");
        Assert.state(m_nodeDao !=  null, "property nodeDao must be set to a non-null value");
        Assert.state(m_filter !=  null, "property filter must be set to a non-null value");
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
     * <p>getVisitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public EntityVisitor getVisitor() {
        return m_visitor;
    }

    /**
     * <p>setVisitor</p>
     *
     * @param visitor a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public void setVisitor(EntityVisitor visitor) {
        m_visitor = visitor;
    }

    /**
     * <p>getFilterDao</p>
     *
     * @return a {@link org.opennms.netmgt.filter.api.FilterDao} object.
     */
    public FilterDao getFilterDao() {
        return m_filterDao;
    }

    /**
     * <p>setFilterDao</p>
     *
     * @param filterDao a {@link org.opennms.netmgt.filter.api.FilterDao} object.
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
