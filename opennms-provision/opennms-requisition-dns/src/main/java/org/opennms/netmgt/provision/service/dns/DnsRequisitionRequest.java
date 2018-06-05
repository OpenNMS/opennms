/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.dns;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.RequisitionRequest;
import java.util.Objects;

@XmlRootElement(name = "dns-requisition-request")
@XmlAccessorType(XmlAccessType.NONE)
public class DnsRequisitionRequest implements RequisitionRequest {

    private static final int DEFAULT_PORT = 53;
    private static final long DEFAULT_SERIAL = 0;
    private static final boolean DEFAULT_FALLBACK = false;
    public static final ForeignIdHashSource DEFAULT_FOREIGN_ID_HASH_SOURCE = ForeignIdHashSource.NODE_LABEL;
    private static final List<String> DEFAULT_SERVICES = Arrays.asList("ICMP", "SNMP");

    @XmlAttribute(name = "host")
    private String host;

    @XmlAttribute(name = "port")
    private Integer port;

    @XmlAttribute(name = "zone")
    private String zone;

    @XmlAttribute(name = "foreign-source")
    private String foreignSource;

    @XmlAttribute(name = "serial")
    private Long serial;

    @XmlAttribute(name = "fallback")
    private Boolean fallback;

    @XmlAttribute(name = "expression")
    private String expression;

    @XmlAttribute(name = "foreign-id-hash-source")
    private ForeignIdHashSource foreignIdHashSource;

    @XmlElement(name = "service")
    private List<String> services;

    public DnsRequisitionRequest() { }
    
    public DnsRequisitionRequest(Map<String, String> parameters) {
        host = parameters.get("host");
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("host is required.");
        }
        final String portStr = parameters.get("port");
        if (portStr != null) {
            port = Integer.valueOf(portStr);
        }
        zone = parameters.get("zone");
        if (zone == null || zone.isEmpty()) {
            throw new IllegalArgumentException("zone is required.");
        }
        foreignSource = parameters.get("foreignSource");
        if (foreignSource == null || foreignSource.isEmpty()) {
            throw new IllegalArgumentException("foreignSource is required.");
        }
        final String serialStr = parameters.get("serial");
        if (serialStr != null) {
            serial = Long.valueOf(serialStr);
        }
        final String fallbackStr = parameters.get("fallback");
        if (fallbackStr != null) {
            fallback = Boolean.valueOf(fallbackStr);
        }
        expression = parameters.get("expression");
        final String foreignIdHashSourceStr = parameters.get("foreignIdHashSource");
        if (foreignIdHashSourceStr != null) {
            foreignIdHashSource = ForeignIdHashSource.valueOf(foreignIdHashSourceStr);
        }
        final String servicesStr = parameters.get("services");
        if (servicesStr != null) {
            services = Arrays.asList(servicesStr.split(","));
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port != null ? port : DEFAULT_PORT;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    public Long getSerial() {
        return serial != null ? serial : DEFAULT_SERIAL;
    }

    public void setSerial(Long serial) {
        this.serial = serial;
    }

    public Boolean getFallback() {
        return fallback != null ? fallback : DEFAULT_FALLBACK;
    }

    public void setFallback(Boolean fallback) {
        this.fallback = fallback;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public ForeignIdHashSource getForeignIdHashSource() {
        return foreignIdHashSource != null ? foreignIdHashSource : DEFAULT_FOREIGN_ID_HASH_SOURCE;
    }

    public void setForeignIdHashSource(ForeignIdHashSource foreignIdHashSource) {
        this.foreignIdHashSource = foreignIdHashSource;
    }

    public List<String> getServices() {
        return services != null ? services : DEFAULT_SERVICES;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof DnsRequisitionRequest)) {
            return false;
        }
        DnsRequisitionRequest castOther = (DnsRequisitionRequest) other;
        return Objects.equals(host, castOther.host) && Objects.equals(port, castOther.port)
                && Objects.equals(zone, castOther.zone) && Objects.equals(foreignSource, castOther.foreignSource)
                && Objects.equals(serial, castOther.serial) && Objects.equals(fallback, castOther.fallback)
                && Objects.equals(expression, castOther.expression)
                && Objects.equals(foreignIdHashSource, castOther.foreignIdHashSource)
                && Objects.equals(services, castOther.services);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, zone, foreignSource, serial, fallback, expression, foreignIdHashSource,
                services);
    }

}
