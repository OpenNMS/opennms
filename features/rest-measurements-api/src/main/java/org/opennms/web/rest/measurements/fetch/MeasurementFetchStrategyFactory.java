/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.measurements.fetch;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.rrd.MultiOutputRrdStrategy;
import org.opennms.netmgt.rrd.QueuingRrdStrategy;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Used to instantiate a fetch strategy based on the
 * current persistence strategy.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
public class MeasurementFetchStrategyFactory {

    @Autowired
    private ResourceDao m_resourceDao;

	public MeasurementFetchStrategy getFetchStrategy() {
		RrdStrategy<?, ?> strategy = findRrdStrategy();

		if (strategy instanceof JniRrdStrategy) {
			return new RrdtoolXportFetchStrategy(m_resourceDao);
        } else if (strategy instanceof JRobinRrdStrategy) {
		return new JrobinFetchStrategy(m_resourceDao);
        } else {
            throw new RuntimeException("Unsupported RRD strategy: " + strategy.getClass());
        }
	}

	private static RrdStrategy<?, ?> findRrdStrategy() {
        return findRrdStrategy(RrdUtils.getStrategy());
    }

    private static RrdStrategy<?, ?> findRrdStrategy(final RrdStrategy<?, ?> rrdStrategy) {
        if (rrdStrategy instanceof JniRrdStrategy || rrdStrategy instanceof JRobinRrdStrategy) {
            return rrdStrategy;
        }

        if (rrdStrategy instanceof QueuingRrdStrategy) {
            return findRrdStrategy(((QueuingRrdStrategy) rrdStrategy).getDelegate());
        }

        if (rrdStrategy instanceof MultiOutputRrdStrategy) {
            for (final RrdStrategy<?, ?> delegate : ((MultiOutputRrdStrategy) rrdStrategy).getDelegates()) {
                RrdStrategy<?, ?> x = findRrdStrategy(delegate);

                if (x instanceof JniRrdStrategy || x instanceof JRobinRrdStrategy) {
                    return x;
                }
            }
        }

        return rrdStrategy;
    }
}
