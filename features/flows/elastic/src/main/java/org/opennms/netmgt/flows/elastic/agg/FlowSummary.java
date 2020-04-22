/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.flows.elastic.agg;

import java.util.Objects;

import org.opennms.netmgt.flows.elastic.NodeDocument;

import com.google.gson.annotations.SerializedName;

public class FlowSummary {

    public static final String UNKNOWN_APPLICATION_NAME_KEY = "__unknown";
    public static final String UNKNOWN_APPLICATION_NAME_DISPLAY = "Unknown";

    public static final String OTHER_APPLICATION_NAME_KEY = "__other";
    public static final String OTHER_APPLICATION_NAME_DISPLAY = "Other";

    @SerializedName("@timestamp")
    private long timestamp;

    @SerializedName("range_start")
    private long rangeStartMs;

    @SerializedName("range_end")
    private long rangeEndMs;

    @SerializedName("ranking")
    private int ranking;

    @SerializedName("grouped_by")
    private GroupedBy groupedBy;

    @SerializedName("type")
    private AggregationType type;

    @SerializedName("bytes_ingress")
    private Long bytesIngress;

    @SerializedName("bytes_egress")
    private Long bytesEgress;

    @SerializedName("bytes_total")
    private Long bytesTotal;

    @SerializedName("exporter")
    private NodeDocument exporter;

    @SerializedName("if_index")
    private Integer ifIndex;

    @SerializedName("application")
    private String application;

    @SerializedName("host_address")
    private String hostAddress;

    @SerializedName("host_name")
    private String hostName;

    @SerializedName("conversation_key")
    private String conversationKey;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getRangeStartMs() {
        return rangeStartMs;
    }

    public void setRangeStartMs(long rangeStartMs) {
        this.rangeStartMs = rangeStartMs;
    }

    public long getRangeEndMs() {
        return rangeEndMs;
    }

    public void setRangeEndMs(long rangeEndMs) {
        this.rangeEndMs = rangeEndMs;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public GroupedBy getGroupedBy() {
        return groupedBy;
    }

    public void setGroupedBy(GroupedBy groupedBy) {
        this.groupedBy = groupedBy;
    }

    public AggregationType getType() {
        return type;
    }

    public void setType(AggregationType type) {
        this.type = type;
    }

    public Long getBytesIngress() {
        return bytesIngress;
    }

    public void setBytesIngress(Long bytesIngress) {
        this.bytesIngress = bytesIngress;
    }

    public Long getBytesEgress() {
        return bytesEgress;
    }

    public void setBytesEgress(Long bytesEgress) {
        this.bytesEgress = bytesEgress;
    }

    public Long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(Long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    public NodeDocument getExporter() {
        return exporter;
    }

    public void setExporter(NodeDocument exporter) {
        this.exporter = exporter;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getConversationKey() {
        return conversationKey;
    }

    public void setConversationKey(String conversationKey) {
        this.conversationKey = conversationKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowSummary)) return false;
        FlowSummary summary = (FlowSummary) o;
        return timestamp == summary.timestamp &&
                rangeStartMs == summary.rangeStartMs &&
                rangeEndMs == summary.rangeEndMs &&
                ranking == summary.ranking &&
                groupedBy == summary.groupedBy &&
                type == summary.type &&
                Objects.equals(bytesIngress, summary.bytesIngress) &&
                Objects.equals(bytesEgress, summary.bytesEgress) &&
                Objects.equals(bytesTotal, summary.bytesTotal) &&
                Objects.equals(exporter, summary.exporter) &&
                Objects.equals(ifIndex, summary.ifIndex) &&
                Objects.equals(application, summary.application) &&
                Objects.equals(hostAddress, summary.hostAddress) &&
                Objects.equals(hostName, summary.hostName) &&
                Objects.equals(conversationKey, summary.conversationKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, rangeStartMs, rangeEndMs, ranking, groupedBy, type, bytesIngress,
                bytesEgress, bytesTotal, exporter, ifIndex, application, hostAddress, hostName, conversationKey);
    }

    @Override
    public String toString() {
        return "FlowSummary{" +
                "timestamp=" + timestamp +
                ", rangeStartMs=" + rangeStartMs +
                ", rangeEndMs=" + rangeEndMs +
                ", ranking=" + ranking +
                ", groupedBy=" + groupedBy +
                ", type=" + type +
                ", bytesIngress=" + bytesIngress +
                ", bytesEgress=" + bytesEgress +
                ", bytesTotal=" + bytesTotal +
                ", exporter=" + exporter +
                ", ifIndex=" + ifIndex +
                ", application='" + application + '\'' +
                ", hostAddress='" + hostAddress + '\'' +
                ", hostName='" + hostName + '\'' +
                ", conversationKey='" + conversationKey + '\'' +
                '}';
    }
}
