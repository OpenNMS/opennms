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
package org.opennms.web.rest.v2.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One outage in a {@link NodeOutageTimelineDto}.
 *
 * Every timestamp is epoch milliseconds in both JSON and XML, unlike {@code OnmsOutage}, which
 * renders epoch milliseconds in JSON but ISO-8601 in XML from the same field. The window parameters
 * of the timeline resource are epoch milliseconds, so request and response speak one unit.
 *
 * {@code ifServiceId} and {@code ipInterfaceId} are the {@code id} fields of the service and
 * interface objects in {@code GET /rest/availability/nodes/{nodeId}}, so the two documents join on
 * those integers without matching on address strings.
 */
@XmlRootElement(name = "outage")
@XmlAccessorType(XmlAccessType.NONE)
@Schema(name = "NodeOutageTimelineEntry", description = "One outage on one monitored service.")
public class NodeOutageTimelineEntryDto {

    @XmlAttribute(name = "id")
    @Schema(description = "Database identifier of the outage.", example = "3543")
    private Integer id;

    @XmlAttribute(name = "ifServiceId")
    @Schema(description = "Database identifier of the monitored service (the ifservices row). Joins "
            + "to the service `id` in the availability resource.", example = "3")
    private Integer ifServiceId;

    @XmlAttribute(name = "ipInterfaceId")
    @Schema(description = "Database identifier of the IP interface. Joins to the interface `id` in "
            + "the availability resource.", example = "1")
    private Integer ipInterfaceId;

    @XmlAttribute(name = "ipAddress")
    @Schema(description = "IP address of the interface the service runs on.", example = "192.168.1.1")
    private String ipAddress;

    @XmlAttribute(name = "serviceId")
    @Schema(description = "Database identifier of the service type, as used by the service detail page.",
            example = "3")
    private Integer serviceId;

    @XmlAttribute(name = "serviceName")
    @Schema(description = "Name of the service type.", example = "SNMP")
    private String serviceName;

    @XmlAttribute(name = "ifLostService")
    @Schema(description = "When the service was lost, epoch milliseconds. Not clamped to the "
            + "requested window: an outage that began before `start` reports its true start.",
            example = "1787700000000")
    private Long ifLostService;

    @XmlAttribute(name = "ifRegainedService")
    @Schema(description = "When the service was regained, epoch milliseconds, or null while it is "
            + "still down. In XML a null is rendered by omitting the attribute.",
            example = "1787710000000", nullable = true)
    private Long ifRegainedService;

    public NodeOutageTimelineEntryDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Integer getIfServiceId() {
        return ifServiceId;
    }

    public void setIfServiceId(final Integer ifServiceId) {
        this.ifServiceId = ifServiceId;
    }

    public Integer getIpInterfaceId() {
        return ipInterfaceId;
    }

    public void setIpInterfaceId(final Integer ipInterfaceId) {
        this.ipInterfaceId = ipInterfaceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(final Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getIfLostService() {
        return ifLostService;
    }

    public void setIfLostService(final Long ifLostService) {
        this.ifLostService = ifLostService;
    }

    public Long getIfRegainedService() {
        return ifRegainedService;
    }

    public void setIfRegainedService(final Long ifRegainedService) {
        this.ifRegainedService = ifRegainedService;
    }
}
