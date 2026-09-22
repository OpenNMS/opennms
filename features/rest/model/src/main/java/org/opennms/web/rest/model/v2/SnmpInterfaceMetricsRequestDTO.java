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
package org.opennms.web.rest.model.v2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

/** The interfaces to summarize, and how. */
@XmlRootElement(name = "snmpInterfaceMetricsRequest")
public class SnmpInterfaceMetricsRequestDTO {

    private List<SnmpInterfaceRefDTO> interfaces = new ArrayList<>();
    private Integer windowSeconds;
    private List<String> metrics = new ArrayList<>();

    @XmlElement(name = "interfaces")
    @Schema(description = "Interfaces to summarize. Duplicates are read once.")
    public List<SnmpInterfaceRefDTO> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(final List<SnmpInterfaceRefDTO> interfaces) {
        this.interfaces = interfaces == null ? new ArrayList<>() : interfaces;
    }

    @XmlElement(name = "windowSeconds")
    @Schema(description = "How far back to average, in seconds. Clamped to between 300 and 86400; 900 when omitted.")
    public Integer getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(final Integer windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    @XmlElement(name = "metrics")
    @Schema(description = "Counter groups to read: traffic, packets, errors, discards. All of them when omitted.")
    public List<String> getMetrics() {
        return metrics;
    }

    public void setMetrics(final List<String> metrics) {
        this.metrics = metrics == null ? new ArrayList<>() : metrics;
    }
}
