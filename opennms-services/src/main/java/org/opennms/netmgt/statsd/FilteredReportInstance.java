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

package org.opennms.netmgt.statsd;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilterResourceWalker;
import org.opennms.netmgt.dao.support.ResourceWalker;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>FilteredReportInstance class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class FilteredReportInstance extends BaseReportInstance implements InitializingBean {

    private final FilterResourceWalker m_walker = new FilterResourceWalker();

    /**
     * <p>Constructor for FilteredReportInstance.</p>
     *
     * @param visitor a {@link org.opennms.netmgt.model.AttributeStatisticVisitorWithResults} object.
     */
    public FilteredReportInstance(AttributeStatisticVisitorWithResults visitor) {
        super(visitor);
    }

    @Override
    public ResourceWalker getWalker() {
        return m_walker;
    }

    /**
     * <p>setFilterDao</p>
     *
     * @param filterDao a {@link org.opennms.netmgt.filter.api.FilterDao} object.
     */
    public void setFilterDao(FilterDao filterDao) {
        m_walker.setFilterDao(filterDao);
    }

    /**
     * <p>setFilter</p>
     *
     * @param filter a {@link java.lang.String} object.
     */
    public void setFilter(String filter) {
        m_walker.setFilter(filter);
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_walker.setNodeDao(nodeDao);
    }

}
