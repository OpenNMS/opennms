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
package org.opennms.netmgt.correlation.jmx;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.logging.Logging;
import org.opennms.core.spring.BeanUtils;

public class Correlator implements CorrelatorMBean {

    org.opennms.netmgt.correlation.Correlator m_correlator;

    /**
     * Initialization.
     * 
     * Retrieves the Spring context for the correlator.
     */
    @Override
    public void init() {
        Map<String,String> mdc = Logging.getCopyOfContextMap();
        Logging.putPrefix("correlator");
        m_correlator = BeanUtils.getBean("correlatorContext", "correlator", org.opennms.netmgt.correlation.Correlator.class);
        Logging.setContextMap(mdc);
    }

    private org.opennms.netmgt.correlation.Correlator getBean() {
        return m_correlator;
    }

    /**
     * Start the correlator daemon.
     */
    @Override
    public void start() {
        if (getBean() != null) getBean().start();
    }

    /**
     * Stop the correlator daemon.
     */
    @Override
    public void stop() {
        if (getBean() != null) getBean().stop();
    }

    /**
     * Get the current status of the correlator daemon.
     * 
     * @return The integer constant from {@link Fiber} that represents the daemon's status.
     */
    @Override
    public int getStatus() {
        return getBean() == null? Fiber.STOPPED : getBean().getStatus();
    }

    /**
     * Get the current status of the correlator.
     * 
     * @return The status, as text.
     */
    @Override
    public String getStatusText() {
        return Fiber.STATUS_NAMES[getStatus()];
    }

    @Override
    public long getStartTimeMilliseconds() {
        throw new NotImplementedException();
    }

    /**
     * Get the current status of the correlator.
     * 
     * @return The status, as text.
     */
    @Override
    public String status() {
        return Fiber.STATUS_NAMES[getStatus()];
    }
}
