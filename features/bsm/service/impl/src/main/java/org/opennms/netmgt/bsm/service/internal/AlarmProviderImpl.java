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
