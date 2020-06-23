/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.bsm.service.AlarmProvider;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AlarmProviderImpl implements AlarmProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmProviderImpl.class);

    private static final long DEFAULT_THRESHOLD = 1000;

    private static final String THRESHOLD_KEY = "org.opennms.features.bsm.reductionKeyFullLoadThreshold";

    private final long threshold = getThreshold();

    @Autowired
    private AlarmDao alarmDao;

    @Override
    public Map<String, AlarmWrapper> lookup(Set<String> reductionKeys) {
        if (reductionKeys == null || reductionKeys.isEmpty()) {
            return new HashMap<>();
        }
        if (reductionKeys.size() <= threshold) {
            List<OnmsAlarm> alarms = alarmDao.findMatching(new CriteriaBuilder(OnmsAlarm.class).in("reductionKey", reductionKeys).toCriteria());
            return alarms.stream().collect(Collectors.toMap(OnmsAlarm::getReductionKey, AlarmWrapperImpl::new));
        } else {
            return alarmDao.findAll().stream()
                    .filter(a -> reductionKeys.contains(a.getReductionKey()))
                    .collect(Collectors.toMap(OnmsAlarm::getReductionKey, AlarmWrapperImpl::new));
        }
    }

    private static long getThreshold() {
        String thresholdProperty = System.getProperty(THRESHOLD_KEY, Long.toString(DEFAULT_THRESHOLD));
        try {
            long threshold = Long.valueOf(thresholdProperty);
            if (threshold <= 0) {
                LOG.warn("Defined threshold must be greater than 0, but was {}. Falling back to default: {}", threshold, DEFAULT_THRESHOLD);
                return DEFAULT_THRESHOLD;
            }
            LOG.debug("Using threshold {}", threshold);
            return threshold;
        } catch (Exception ex) {
            LOG.warn("The defined threshold {} could not be interpreted as long value. Falling back to default: {}", thresholdProperty, DEFAULT_THRESHOLD);
            return DEFAULT_THRESHOLD;
        }
    }
}
