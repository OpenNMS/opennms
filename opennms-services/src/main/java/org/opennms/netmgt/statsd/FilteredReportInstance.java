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
