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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

/** One interface's state and windowed rates. */
@XmlRootElement(name = "snmpInterfaceMetrics")
@Schema(description = "Rates are the latest stored sample at or before the instant, each an average over the response's step. "
        + "A field is absent rather than zero when it was not collected or cannot be derived.")
public class SnmpInterfaceMetricsDTO {

    private Integer nodeId;
    private Integer ifIndex;
    private Integer ifAdminStatus;
    private Integer ifOperStatus;
    private Long sampledAt;
    private Long speedBps;
    private Double inBitsPerSecond;
    private Double outBitsPerSecond;
    private Double inUtilizationPercent;
    private Double outUtilizationPercent;
    private Double inUnicastPacketsPerSecond;
    private Double inMulticastPacketsPerSecond;
    private Double inBroadcastPacketsPerSecond;
    private Double outUnicastPacketsPerSecond;
    private Double outMulticastPacketsPerSecond;
    private Double outBroadcastPacketsPerSecond;
    private Double inErrorsPerSecond;
    private Double outErrorsPerSecond;
    private Double inDiscardsPerSecond;
    private Double outDiscardsPerSecond;

    @XmlElement(name = "nodeId")
    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(final Integer nodeId) {
        this.nodeId = nodeId;
    }

    @XmlElement(name = "ifIndex")
    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(final Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    @XmlElement(name = "ifAdminStatus")
    @Schema(description = "IF-MIB ifAdminStatus as the database holds it now: 1 up, 2 down, 3 testing. Absent when the instant is in the past.")
    public Integer getIfAdminStatus() {
        return ifAdminStatus;
    }

    public void setIfAdminStatus(final Integer ifAdminStatus) {
        this.ifAdminStatus = ifAdminStatus;
    }

    @XmlElement(name = "ifOperStatus")
    @Schema(description = "IF-MIB ifOperStatus as the database holds it now: 1 up, 2 down, and so on. Absent when the instant is in the past.")
    public Integer getIfOperStatus() {
        return ifOperStatus;
    }

    public void setIfOperStatus(final Integer ifOperStatus) {
        this.ifOperStatus = ifOperStatus;
    }

    @XmlElement(name = "sampledAt")
    @Schema(description = "Timestamp of the stored sample the rates come from, epoch milliseconds. Absent when no counter had a sample in the lookback.")
    public Long getSampledAt() {
        return sampledAt;
    }

    public void setSampledAt(final Long sampledAt) {
        this.sampledAt = sampledAt;
    }

    @XmlElement(name = "speedBps")
    @Schema(description = "Interface speed in bits per second: the provisioned ifSpeed, or the collected ifHighSpeed when the row has none usable. Absent when neither is known.")
    public Long getSpeedBps() {
        return speedBps;
    }

    public void setSpeedBps(final Long speedBps) {
        this.speedBps = speedBps;
    }

    @XmlElement(name = "inBitsPerSecond")
    @Schema(description = "Average inbound bits per second at the sample.")
    public Double getInBitsPerSecond() {
        return inBitsPerSecond;
    }

    public void setInBitsPerSecond(final Double inBitsPerSecond) {
        this.inBitsPerSecond = inBitsPerSecond;
    }

    @XmlElement(name = "outBitsPerSecond")
    @Schema(description = "Average outbound bits per second at the sample.")
    public Double getOutBitsPerSecond() {
        return outBitsPerSecond;
    }

    public void setOutBitsPerSecond(final Double outBitsPerSecond) {
        this.outBitsPerSecond = outBitsPerSecond;
    }

    @XmlElement(name = "inUtilizationPercent")
    @Schema(description = "inBitsPerSecond as a percentage of speedBps. Absent when either is unknown.")
    public Double getInUtilizationPercent() {
        return inUtilizationPercent;
    }

    public void setInUtilizationPercent(final Double inUtilizationPercent) {
        this.inUtilizationPercent = inUtilizationPercent;
    }

    @XmlElement(name = "outUtilizationPercent")
    @Schema(description = "outBitsPerSecond as a percentage of speedBps. Absent when either is unknown.")
    public Double getOutUtilizationPercent() {
        return outUtilizationPercent;
    }

    public void setOutUtilizationPercent(final Double outUtilizationPercent) {
        this.outUtilizationPercent = outUtilizationPercent;
    }

    @XmlElement(name = "inUnicastPacketsPerSecond")
    @Schema(description = "Average inbound unicast packets per second at the sample.")
    public Double getInUnicastPacketsPerSecond() {
        return inUnicastPacketsPerSecond;
    }

    public void setInUnicastPacketsPerSecond(final Double inUnicastPacketsPerSecond) {
        this.inUnicastPacketsPerSecond = inUnicastPacketsPerSecond;
    }

    @XmlElement(name = "inMulticastPacketsPerSecond")
    @Schema(description = "Average inbound multicast packets per second at the sample. Absent when only the 32-bit counters are collected, so a client summing classes must not read absence as zero.")
    public Double getInMulticastPacketsPerSecond() {
        return inMulticastPacketsPerSecond;
    }

    public void setInMulticastPacketsPerSecond(final Double inMulticastPacketsPerSecond) {
        this.inMulticastPacketsPerSecond = inMulticastPacketsPerSecond;
    }

    @XmlElement(name = "inBroadcastPacketsPerSecond")
    @Schema(description = "Average inbound broadcast packets per second at the sample. Absent when only the 32-bit counters are collected, so a client summing classes must not read absence as zero.")
    public Double getInBroadcastPacketsPerSecond() {
        return inBroadcastPacketsPerSecond;
    }

    public void setInBroadcastPacketsPerSecond(final Double inBroadcastPacketsPerSecond) {
        this.inBroadcastPacketsPerSecond = inBroadcastPacketsPerSecond;
    }

    @XmlElement(name = "outUnicastPacketsPerSecond")
    @Schema(description = "Average outbound unicast packets per second at the sample.")
    public Double getOutUnicastPacketsPerSecond() {
        return outUnicastPacketsPerSecond;
    }

    public void setOutUnicastPacketsPerSecond(final Double outUnicastPacketsPerSecond) {
        this.outUnicastPacketsPerSecond = outUnicastPacketsPerSecond;
    }

    @XmlElement(name = "outMulticastPacketsPerSecond")
    @Schema(description = "Average outbound multicast packets per second at the sample. Absent when only the 32-bit counters are collected, so a client summing classes must not read absence as zero.")
    public Double getOutMulticastPacketsPerSecond() {
        return outMulticastPacketsPerSecond;
    }

    public void setOutMulticastPacketsPerSecond(final Double outMulticastPacketsPerSecond) {
        this.outMulticastPacketsPerSecond = outMulticastPacketsPerSecond;
    }

    @XmlElement(name = "outBroadcastPacketsPerSecond")
    @Schema(description = "Average outbound broadcast packets per second at the sample. Absent when only the 32-bit counters are collected, so a client summing classes must not read absence as zero.")
    public Double getOutBroadcastPacketsPerSecond() {
        return outBroadcastPacketsPerSecond;
    }

    public void setOutBroadcastPacketsPerSecond(final Double outBroadcastPacketsPerSecond) {
        this.outBroadcastPacketsPerSecond = outBroadcastPacketsPerSecond;
    }

    @XmlElement(name = "inErrorsPerSecond")
    @Schema(description = "Average inbound errors per second at the sample.")
    public Double getInErrorsPerSecond() {
        return inErrorsPerSecond;
    }

    public void setInErrorsPerSecond(final Double inErrorsPerSecond) {
        this.inErrorsPerSecond = inErrorsPerSecond;
    }

    @XmlElement(name = "outErrorsPerSecond")
    @Schema(description = "Average outbound errors per second at the sample.")
    public Double getOutErrorsPerSecond() {
        return outErrorsPerSecond;
    }

    public void setOutErrorsPerSecond(final Double outErrorsPerSecond) {
        this.outErrorsPerSecond = outErrorsPerSecond;
    }

    @XmlElement(name = "inDiscardsPerSecond")
    @Schema(description = "Average inbound discards per second at the sample.")
    public Double getInDiscardsPerSecond() {
        return inDiscardsPerSecond;
    }

    public void setInDiscardsPerSecond(final Double inDiscardsPerSecond) {
        this.inDiscardsPerSecond = inDiscardsPerSecond;
    }

    @XmlElement(name = "outDiscardsPerSecond")
    @Schema(description = "Average outbound discards per second at the sample.")
    public Double getOutDiscardsPerSecond() {
        return outDiscardsPerSecond;
    }

    public void setOutDiscardsPerSecond(final Double outDiscardsPerSecond) {
        this.outDiscardsPerSecond = outDiscardsPerSecond;
    }
}
