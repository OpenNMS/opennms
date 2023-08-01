/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Constructs the appropriate RRD strategy based on the
 * configured system properties.
 *
 * Optionally wraps the strategy with a queue and/or
 * outputs the metrics to a TCP stream.
 *
 */
public class RrdStrategyFactory implements ApplicationContextAware {

    private ApplicationContext m_context;

    private static enum StrategyName {
        basicRrdStrategy,
        queuingRrdStrategy,
        tcpAndBasicRrdStrategy,
        tcpAndQueuingRrdStrategy
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        m_context = context;
    }

    /**
     * <p>getStrategy</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    @SuppressWarnings("unchecked")
    public <D, F> RrdStrategy<D, F> getStrategy() {
        RrdStrategy<D, F> rrdStrategy = null;
        boolean useQueue = m_context.getBean("useQueue", Boolean.class);
        boolean useTcp = m_context.getBean("useTcp", Boolean.class);

        try {
            if (useQueue) {
                if (useTcp) {
                    rrdStrategy = m_context.getBean(StrategyName.tcpAndQueuingRrdStrategy.toString(), RrdStrategy.class);
                } else {
                    rrdStrategy = m_context.getBean(StrategyName.queuingRrdStrategy.toString(), RrdStrategy.class);
                }
            } else {
                if (useTcp) {
                    rrdStrategy = m_context.getBean(StrategyName.tcpAndBasicRrdStrategy.toString(), RrdStrategy.class);
                } else {
                    rrdStrategy = m_context.getBean(StrategyName.basicRrdStrategy.toString(), RrdStrategy.class);
                }
            }
        } catch (final BeansException e) {
            throw new IllegalStateException(String.format("Invalid RRD configuration useQueue: %s, useTcp: %s", useQueue, useTcp), e);
        }

        return rrdStrategy;
    }
}
