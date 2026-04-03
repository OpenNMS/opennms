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

import org.opennms.netmgt.config.trapd.Snmpv3User;

public class Snmpv3UserDto {
    private String engineId;
    private String securityName;
    private Integer securityLevel;
    private String authProtocol;
    private String authPassphrase;
    private String privacyProtocol;
    private String privacyPassphrase;


    public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public String getSecurityName() {
        return securityName;
    }

    public void setSecurityName(String securityName) {
        this.securityName = securityName;
    }

    public Integer getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getAuthProtocol() {
        return authProtocol;
    }

    public void setAuthProtocol(String authProtocol) {
        this.authProtocol = authProtocol;
    }

    public String getAuthPassphrase() {
        return authPassphrase;
    }

    public void setAuthPassphrase(String authPassphrase) {
        this.authPassphrase = authPassphrase;
    }

    public String getPrivacyProtocol() {
        return privacyProtocol;
    }

    public void setPrivacyProtocol(String privacyProtocol) {
        this.privacyProtocol = privacyProtocol;
    }

    public String getPrivacyPassphrase() {
        return privacyPassphrase;
    }

    public void setPrivacyPassphrase(String privacyPassphrase) {
        this.privacyPassphrase = privacyPassphrase;
    }

    public static Snmpv3UserDto toDto(Snmpv3User user) {
        if (user == null) {
            return null;
        }
        Snmpv3UserDto dto = new Snmpv3UserDto();
        dto.setEngineId(user.getEngineId());
        dto.setSecurityName(user.getSecurityName());
        dto.setSecurityLevel(user.getSecurityLevel());
        dto.setAuthProtocol(user.getAuthProtocol());
        dto.setAuthPassphrase(user.getAuthPassphrase());
        dto.setPrivacyProtocol(user.getPrivacyProtocol());
        dto.setPrivacyPassphrase(user.getPrivacyPassphrase());
        return dto;
    }

    public Snmpv3User toEntity() {
        Snmpv3User user = new Snmpv3User();
        user.setEngineId(engineId);
        user.setSecurityName(securityName);
        if (securityLevel != null) user.setSecurityLevel(securityLevel);
        user.setAuthProtocol(authProtocol);
        user.setAuthPassphrase(authPassphrase);
        user.setPrivacyProtocol(privacyProtocol);
        user.setPrivacyPassphrase(privacyPassphrase);
        return user;
    }
}
