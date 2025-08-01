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

import org.opennms.netmgt.dao.support.ResourceTreeWalker;
import org.opennms.netmgt.dao.support.ResourceWalker;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>UnfilteredReportInstance class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class UnfilteredReportInstance extends BaseReportInstance implements InitializingBean {

    private final ResourceTreeWalker m_walker = new ResourceTreeWalker();

    /**
     * <p>Constructor for FilteredReportInstance.</p>
     *
     * @param visitor a {@link org.opennms.netmgt.model.AttributeStatisticVisitorWithResults} object.
     */
    public UnfilteredReportInstance(AttributeStatisticVisitorWithResults visitor) {
        super(visitor);
    }

    @Override
    public ResourceWalker getWalker() {
        return m_walker;
    }

}
