/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.stats;

import java.util.Optional;

public class AsnInfo {

    private Long asn;
    private String asName;
    private String orgId;
    private String orgName;
    private String address;
    private String city;
    private String stateProv;
    private String postalCode;
    private String country;
    private String source;
    private String rawOutput;
    private String remarks;

    public AsnInfo(Long asn, String source, String rawOutput) {
        this.asn = asn;
        this.source = source;
        this.rawOutput = rawOutput;
    }

    public Long getAsn() {
        return asn;
    }

    public void setAsn(Long asn) {
        this.asn = asn;
    }

    public String getAsName() {
        return asName;
    }

    public void setAsName(String asName) {
        this.asName = asName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProv() {
        return stateProv;
    }

    public void setStateProv(String stateProv) {
        this.stateProv = stateProv;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public static AsnInfo parseOutput(Long asn, String source, String rawOutput) {
        AsnInfo asnInfo = new AsnInfo(asn, source, rawOutput);
        // These items can be in multiple lines.
        String remarks = "";
        String address = "";
        String orgId = "";
        // Split output into lines
        String[] lines = rawOutput.split("\\r?\\n");
        for (String line : lines) {
            if (line.contains("ASName") || line.contains("as-name")) {
                getSubStringAfterColon(line).ifPresent(asnInfo::setAsName);
                continue;
            }
            if (line.contains("OrgName") || line.contains("org-name")) {
                getSubStringAfterColon(line).ifPresent(asnInfo::setOrgName);
                continue;
            }
            if (line.contains("OrgId") || line.contains("org")) {
                Optional<String> result = getSubStringAfterColon(line);
                if (result.isPresent()) {
                    orgId = orgId + result.get() + " ";
                    asnInfo.setOrgId(orgId);
                }
                continue;
            }
            if (line.contains("Address") || line.contains("address")) {
                Optional<String> result = getSubStringAfterColon(line);
                if (result.isPresent()) {
                    address = address + result.get() + " ";
                    asnInfo.setAddress(address);
                }
                continue;
            }
            if (line.contains("City")) {
                getSubStringAfterColon(line).ifPresent(asnInfo::setCity);
                continue;
            }
            if (line.contains("StateProv")) {
                getSubStringAfterColon(line).ifPresent(asnInfo::setStateProv);

                continue;
            }
            if (line.contains("PostalCode")) {
                getSubStringAfterColon(line).ifPresent(asnInfo::setPostalCode);

                continue;
            }
            if (line.contains("Country") || line.contains("country")) {
                getSubStringAfterColon(line).ifPresent(asnInfo::setCountry);

                continue;
            }
            if (line.contains("remarks") || line.contains("Comment") || line.contains("descr")) {
                Optional<String> result = getSubStringAfterColon(line);
                if (result.isPresent()) {
                    remarks = remarks + result.get() + " ";
                    asnInfo.setRemarks(remarks);
                }
            }
        }
        return asnInfo;
    }

    private static Optional<String> getSubStringAfterColon(String segment) {
        int index = segment.indexOf(":");
        if (index > 0) {
            String value = segment.substring(index + 1);
            return Optional.of(value.trim());
        }
        return Optional.empty();
    }

}
