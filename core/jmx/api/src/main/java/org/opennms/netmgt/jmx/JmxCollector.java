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
package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;

/**
 * The JmxCollector is responsible to collect the configured data at the configured ip address.
 */
public interface JmxCollector {

    /**
     * Implements the logic for the jmx data collection.
     * <p/>
     * Therefore it should somehow:
     * 1. establish the connection to the configured ip address.
     * 2. collect the configured data.
     * 3. inform the {@link org.opennms.netmgt.jmx.JmxSampleProcessor} about the collected data.
     * <p/>
     * The {@link org.opennms.netmgt.jmx.JmxSampleProcessor} is a callback for each sample collected.
     * Therefore each sample can be transformed to another data structure (e.g. collectd).
     *
     * @param config          The configuration to use for the collection.
     * @param sampleProcessor The callback to process each sample.
     * @throws JmxServerConnectionException If the connection to the jmx server could not be established (whatever the reason).
     */
    void collect(JmxCollectorConfig config, MBeanServer mBeanServer, JmxSampleProcessor sampleProcessor) throws JmxServerConnectionException;
}
