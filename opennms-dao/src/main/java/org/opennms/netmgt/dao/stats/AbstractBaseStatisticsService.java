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
package org.opennms.netmgt.dao.stats;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.api.StatisticsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public abstract class AbstractBaseStatisticsService<T> implements StatisticsService<T>, InitializingBean {

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(getDao());
    }

    public abstract OnmsDao<T, Integer> getDao();

    @Transactional
    @Override
	public int getTotalCount(final Criteria criteria) {
        return getDao().countMatching(criteria);
	}


}
