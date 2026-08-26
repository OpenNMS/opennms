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

import io.swagger.v3.oas.annotations.media.Schema;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.web.rest.support.MaskedCredential;

@Schema(description = """
        An SNMPv3 user that trapd accepts traps from.

        `securityName` is the only required field. `authProtocol`/`authPassphrase` and
        `privacyProtocol`/`privacyPassphrase` each have to be supplied together or not at all, and
        when `securityLevel` is present it has to agree with which of those pairs are set.
        Passphrases must be at least 8 bytes and must not begin with `*`.""")
public class Snmpv3UserDto {
    @Schema(description = "Stable identifier for this user, assigned on first persist. Carry it back on "
            + "an update so a masked passphrase can be resolved against the stored value. Leave it out "
            + "for a new user.",
            example = "b0019905-75f8-4856-8c0b-84381e9485a3")
    private String id;

    @Schema(description = "SNMP engine ID this user is scoped to, as a hex string. Optional.",
            example = "0x0102030405")
    private String engineId;

    @Schema(description = "SNMPv3 user name. Required.", example = "trapUser", required = true)
    private String securityName;

    @Schema(description = "1 = noAuthNoPriv, 2 = authNoPriv, 3 = authPriv. Optional; when present it must "
            + "match the credentials supplied.",
            example = "3", allowableValues = {"1", "2", "3"})
    private Integer securityLevel;

    @Schema(description = "Authentication digest.", example = "SHA-256",
            allowableValues = {"MD5", "SHA", "SHA-224", "SHA-256", "SHA-512"})
    private String authProtocol;

    @Schema(description = "Authentication passphrase, at least 8 bytes. Read back as `******`; send that "
            + "masked value unchanged to keep the stored passphrase.",
            example = "******")
    @MaskedCredential
    private String authPassphrase;

    @Schema(description = "Privacy cipher.", example = "AES256",
            allowableValues = {"DES", "AES", "AES192", "AES256"})
    private String privacyProtocol;

    @Schema(description = "Privacy passphrase, at least 8 bytes. Read back as `******`; send that masked "
            + "value unchanged to keep the stored passphrase.",
            example = "******")
    @MaskedCredential
    private String privacyPassphrase;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        dto.setId(user.getId());
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
        user.setId(id);
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
