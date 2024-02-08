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
