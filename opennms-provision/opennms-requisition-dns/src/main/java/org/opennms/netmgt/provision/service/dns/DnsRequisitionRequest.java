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

    @XmlAttribute(name = "location")
    private String location;

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
        location = parameters.get("location");
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

    public String getLocation() {
        return location;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setLocation(final String location) {
        this.location = location;
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
                && Objects.equals(location, castOther.location)
                && Objects.equals(foreignIdHashSource, castOther.foreignIdHashSource)
                && Objects.equals(services, castOther.services);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, zone, foreignSource, serial, fallback, expression, location, foreignIdHashSource,
                services);
    }

}
