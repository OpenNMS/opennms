/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
