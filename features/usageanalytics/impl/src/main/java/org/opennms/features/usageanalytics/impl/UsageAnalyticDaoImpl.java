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
package org.opennms.features.usageanalytics.impl;

import org.opennms.features.usageanalytics.api.UsageAnalyticDao;
import org.opennms.features.usageanalytics.api.UsageAnalytic;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implementation of the interface for collecting Usage Analytic data.
 */
public class UsageAnalyticDaoImpl extends AbstractDaoHibernate<UsageAnalytic, Long> implements UsageAnalyticDao {

    private static final Logger LOG = LoggerFactory.getLogger(UsageAnalyticDaoImpl.class);

    /**
     * Default constructor for UsageAnalyticDaoImpl
     */
    public UsageAnalyticDaoImpl() {
        super(UsageAnalytic.class);
    }


    /** {@inheritDoc} */
    @Override
    public long getValueByMetricName(String metricName) {
        LOG.info("Getting value of the object with metric name : " + metricName);

        UsageAnalytic ua = null;

        CriteriaBuilder cb = new CriteriaBuilder(UsageAnalytic.class);
        cb.eq("metricName", metricName);
        List<UsageAnalytic> uaList = findMatching(cb.toCriteria());

        if (uaList.isEmpty()) {
            return 0l;
        }
        // since criteriaBuilder returns list of objects we know that metricName unique, thus we will get only 1 object
        ua = uaList.get(0);

        return ua.getCounter();
    }


    /** {@inheritDoc} */
    @Override
    public void incrementCounterByMetricName(String metricName) {
        LOG.info("Going to get UsageAnalytic object with metric name : " + metricName);

        UsageAnalytic ua = null;

        CriteriaBuilder cb = new CriteriaBuilder(UsageAnalytic.class);
        cb.eq("metricName", metricName);
        List<UsageAnalytic> uaList = findMatching(cb.toCriteria());

        if (uaList.isEmpty()) {
            ua = new UsageAnalytic();
            ua.setMetricName(metricName);
            ua.setCounter(0l);
        } else {
            // since criteriaBuilder returns list of objects we know that metricName unique, thus we will get only 1 object
            ua = uaList.get(0);
        }

        ua.setCounter(ua.getCounter() + 1);

        super.saveOrUpdate(ua);
    }

}
