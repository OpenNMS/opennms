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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
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
        return new Callable<Integer>() {
              public Integer call() throws Exception {
                  Integer count = dao.countMatching(criteria);
                  LOG.info("Count: {}", count);
                  return count;
              }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Callable<T> findMatchingCallable(OnmsDao<?,?> dao, Criteria criteria) {
        return () -> (T) Iterables.getFirst(dao.findMatching(criteria), null);
    }
}
