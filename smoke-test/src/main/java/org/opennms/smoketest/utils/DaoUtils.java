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
package org.opennms.smoketest.utils;

import java.util.concurrent.Callable;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * DAO utility thingies.
 *
 * @author jwhite
 */
public class DaoUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DaoUtils.class);

    public static Callable<Integer> countMatchingCallable(OnmsDao<?,?> dao, Criteria criteria) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Counting records matching {} on {}. DAO currently has {} total records.", criteria, dao, dao.countAll());
            }
            Integer count = dao.countMatching(criteria);
            LOG.info("Count: {}", count);
            return count;
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Callable<T> findMatchingCallable(OnmsDao<?,?> dao, Criteria criteria) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finding object matching {} on {}. DAO currently has {} total records.", criteria, dao, dao.countAll());
            }
            T entity =  (T) Iterables.getFirst(dao.findMatching(criteria), null);
            LOG.debug("Found: {}", entity);
            return entity;
        };
    }
}
