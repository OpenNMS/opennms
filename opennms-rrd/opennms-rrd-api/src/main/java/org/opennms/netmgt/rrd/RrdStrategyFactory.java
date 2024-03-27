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
