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

package org.opennms.netmgt.flows.api;

public class EnrichedFlow {

    public enum Locality {
        PUBLIC,
        PRIVATE
    }

    private final Flow flow;

    public EnrichedFlow(Flow flow) {
        this.flow = flow;
    }

    private String application;

    private String host;

    private String location;

    private Locality srcLocality;

    private Locality dstLocality;

    private Locality flowLocality;

    private NodeInfo srcNodeInfo;

    private NodeInfo dstNodeInfo;

    private NodeInfo exporterNodeInfo;

    private long clockCorrection;

    public Flow getFlow() {
        return flow;
    }

    public String getApplication() {
        return application;
    }

    public String getHost() {
        return host;
    }

    public String getLocation() {
        return location;
    }

    public Locality getSrcLocality() {
        return srcLocality;
    }

    public Locality getDstLocality() {
        return dstLocality;
    }

    public Locality getFlowLocality() {
        return flowLocality;
    }

    public NodeInfo getSrcNodeInfo() {
        return srcNodeInfo;
    }

    public NodeInfo getDstNodeInfo() {
        return dstNodeInfo;
    }

    public NodeInfo getExporterNodeInfo() {
        return exporterNodeInfo;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSrcLocality(Locality srcLocality) {
        this.srcLocality = srcLocality;
    }

    public void setDstLocality(Locality dstLocality) {
        this.dstLocality = dstLocality;
    }

    public void setFlowLocality(Locality flowLocality) {
        this.flowLocality = flowLocality;
    }

    public void setSrcNodeInfo(NodeInfo srcNodeInfo) {
        this.srcNodeInfo = srcNodeInfo;
    }

    public void setDstNodeInfo(NodeInfo dstNodeInfo) {
        this.dstNodeInfo = dstNodeInfo;
    }

    public void setExporterNodeInfo(NodeInfo exporterNodeInfo) {
        this.exporterNodeInfo = exporterNodeInfo;
    }

    public long getClockCorrection() {
        return this.clockCorrection;
    }

    public void setClockCorrection(final long clockCorrection) {
        this.clockCorrection = clockCorrection;
    }
}
