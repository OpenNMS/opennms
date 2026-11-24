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
package org.opennms.features.kafka.producer.collection;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

public class MetricIdentifierTest {

    @Test public void snmp() { assertEquals(Optional.of(".1.3.6.1.2.1.31.1.1.1.6.512"), MetricIdentifier.metricId("SNMP", "SNMP_.1.3.6.1.2.1.31.1.1.1.6.512", "ifHCInOctets")); }
    @Test public void jmx() { assertEquals(Optional.of("java.lang:type=Memory.HeapMemoryUsage"), MetricIdentifier.metricId("JMX", "JMX_java.lang:type=Memory.HeapMemoryUsage", "HeapUsage")); }
    @Test public void noProtocol() { assertEquals(Optional.empty(), MetricIdentifier.metricId(null, "SNMP_.1.3", "x")); }
    @Test public void noIdentifier() { assertEquals(Optional.empty(), MetricIdentifier.metricId("SNMP", null, "x")); }
    @Test public void attributeNameIsNotAnIdentifier() { assertEquals(Optional.empty(), MetricIdentifier.metricId("SNMP", "SNMP_session_up", "SNMP_session_up")); }
    @Test public void otherProtocolPrefix() { assertEquals(Optional.empty(), MetricIdentifier.metricId("JMX", "SNMP_.1.3", "x")); }
    @Test public void caseSensitive() { assertEquals(Optional.empty(), MetricIdentifier.metricId("BMP", "bmp_10.0.0.1_rejected", "rejected")); }
    @Test public void barePrefix() { assertEquals(Optional.empty(), MetricIdentifier.metricId("SNMP", "SNMP_", "x")); }
    @Test public void latencyIdentifier() { assertEquals(Optional.empty(), MetricIdentifier.metricId("ICMP", "ICMP/10.0.0.1", "icmp")); }
}
