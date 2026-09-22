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

/** Summaries for a set of interfaces and the window their rates cover. */
@XmlRootElement(name = "snmpInterfaceMetricsResponse")
public class SnmpInterfaceMetricsResponseDTO {

    private int windowSeconds;
    private long end;
    private List<SnmpInterfaceMetricsDTO> interfaces = new ArrayList<>();

    public SnmpInterfaceMetricsResponseDTO() {
    }

    public SnmpInterfaceMetricsResponseDTO(final int windowSeconds, final long end,
                                           final List<SnmpInterfaceMetricsDTO> interfaces) {
        this.windowSeconds = windowSeconds;
        this.end = end;
        this.interfaces = interfaces;
    }

    @XmlElement(name = "windowSeconds")
    @Schema(description = "The window every rate in this response is averaged over, ending at 'end'.")
    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(final int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    @XmlElement(name = "end")
    @Schema(description = "End of the window, epoch milliseconds.")
    public long getEnd() {
        return end;
    }

    public void setEnd(final long end) {
        this.end = end;
    }

    @XmlElement(name = "interfaces")
    @Schema(description = "One entry per distinct requested interface, in request order.")
    public List<SnmpInterfaceMetricsDTO> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(final List<SnmpInterfaceMetricsDTO> interfaces) {
        this.interfaces = interfaces == null ? new ArrayList<>() : interfaces;
    }
}
