/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jmx;

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
    void collect(JmxCollectorConfig config, JmxSampleProcessor sampleProcessor) throws JmxServerConnectionException;
}
