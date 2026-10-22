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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Every core-poller outage overlapping a time window, for every monitored service on one node.
 *
 * This is the JSON replacement for the per-service PNG strips of {@code GET /rest/timeline/image/...}:
 * the caller draws the strip. It is not a {@code JaxbListWrapper} because the document is not a page
 * of entities -- it carries the window that was used and the node's creation time, and there is no
 * paging, so {@code totalCount} and {@code offset} would be meaningless.
 */
@XmlRootElement(name = "outage-timeline")
@XmlAccessorType(XmlAccessType.NONE)
@Schema(name = "NodeOutageTimeline",
        description = "Core-poller outages for every monitored service on one node over a time window.")
public class NodeOutageTimelineDto {

    @XmlAttribute(name = "nodeId")
    @Schema(description = "Database identifier of the node.", example = "1")
    private Integer nodeId;

    @XmlAttribute(name = "start")
    @Schema(description = "Window start actually used, epoch milliseconds. Echoes the request so the "
            + "caller can lay out an axis against the window the server queried.",
            example = "1787641143996")
    private Long start;

    @XmlAttribute(name = "end")
    @Schema(description = "Window end actually used, epoch milliseconds.", example = "1787727543996")
    private Long end;

    @XmlAttribute(name = "nodeCreateTime")
    @Schema(description = "When the node was first provisioned, epoch milliseconds. Not clamped to "
            + "the window: before this instant the node was not monitored at all, which is what the "
            + "v1 strip drew as an unpainted region rather than as green.",
            example = "1436881400000")
    private Long nodeCreateTime;

    @XmlAttribute(name = "count")
    @Schema(description = "Number of outages in this document.", example = "2")
    private Integer count;

    @XmlAttribute(name = "truncated")
    @Schema(description = "True when more outages matched than the limit allowed, so these are the "
            + "most recent ones and the window is not fully described. A caller drawing a timeline "
            + "should say so rather than present an incomplete strip as complete.",
            example = "false")
    private Boolean truncated = Boolean.FALSE;

    @XmlElement(name = "outage")
    @Schema(description = "The outages, most recent lost-service time first.")
    private List<NodeOutageTimelineEntryDto> outages = new ArrayList<>();

    public NodeOutageTimelineDto() {
    }

    public NodeOutageTimelineDto(final Integer nodeId, final Long start, final Long end,
                                 final Long nodeCreateTime,
                                 final List<NodeOutageTimelineEntryDto> outages,
                                 final boolean truncated) {
        this.nodeId = nodeId;
        this.start = start;
        this.end = end;
        this.nodeCreateTime = nodeCreateTime;
        this.truncated = truncated;
        this.outages = (outages == null) ? new ArrayList<>() : outages;
        this.count = this.outages.size();
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(final Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(final Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(final Long end) {
        this.end = end;
    }

    public Long getNodeCreateTime() {
        return nodeCreateTime;
    }

    public void setNodeCreateTime(final Long nodeCreateTime) {
        this.nodeCreateTime = nodeCreateTime;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
    }

    public Boolean getTruncated() {
        return truncated;
    }

    public void setTruncated(final Boolean truncated) {
        this.truncated = truncated;
    }

    public List<NodeOutageTimelineEntryDto> getOutages() {
        return outages;
    }

    public void setOutages(final List<NodeOutageTimelineEntryDto> outages) {
        this.outages = (outages == null) ? new ArrayList<>() : outages;
        this.count = this.outages.size();
    }
}
