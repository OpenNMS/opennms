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

/** Summaries for a set of interfaces at one instant. */
@XmlRootElement(name = "snmpInterfaceMetricsResponse")
public class SnmpInterfaceMetricsResponseDTO {

    private long at;
    private int lookbackSeconds;
    private Long stepSeconds;
    private List<SnmpInterfaceMetricsDTO> interfaces = new ArrayList<>();

    public SnmpInterfaceMetricsResponseDTO() {
    }

    public SnmpInterfaceMetricsResponseDTO(final long at, final int lookbackSeconds, final Long stepSeconds,
                                           final List<SnmpInterfaceMetricsDTO> interfaces) {
        this.at = at;
        this.lookbackSeconds = lookbackSeconds;
        this.stepSeconds = stepSeconds;
        this.interfaces = interfaces;
    }

    @XmlElement(name = "at")
    @Schema(description = "The instant summarized, epoch milliseconds: the request's 'at', or now when it was omitted or lay in the future.")
    public long getAt() {
        return at;
    }

    public void setAt(final long at) {
        this.at = at;
    }

    @XmlElement(name = "lookbackSeconds")
    @Schema(description = "How far before 'at' the latest sample was searched for, after clamping.")
    public int getLookbackSeconds() {
        return lookbackSeconds;
    }

    public void setLookbackSeconds(final int lookbackSeconds) {
        this.lookbackSeconds = lookbackSeconds;
    }

    @XmlElement(name = "stepSeconds")
    @Schema(description = "The width of the average each rate represents, as stored: 300 for recent data, coarser once the store has consolidated it. Absent when the store was not queried.")
    public Long getStepSeconds() {
        return stepSeconds;
    }

    public void setStepSeconds(final Long stepSeconds) {
        this.stepSeconds = stepSeconds;
    }

    @XmlElement(name = "interfaces")
    @Schema(description = "One entry per distinct requested interface the database knows, in request order.")
    public List<SnmpInterfaceMetricsDTO> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(final List<SnmpInterfaceMetricsDTO> interfaces) {
        this.interfaces = interfaces == null ? new ArrayList<>() : interfaces;
    }
}
