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
package org.opennms.netmgt.telemetry.config.api;

/**
 * {@link TelemetryBeanDefinition} to define parsers.
 * Besides the common attributes, a parser must define the queue name it dispatches to.
 *
 *  @author mvrueden
 */
public interface ParserDefinition extends TelemetryBeanDefinition {
    /**
     * The name of the queue the parser "writes" to.
     *
     * @return The name of the queue the parser "writes" to. Must not be null.
     */
    String getQueueName();

    /**
     * Returns the full qualified (unique) name of the parser.
     *
     * @return The name uniquely identifying the parser. Must not be null.
     */
    String getFullName();
}
