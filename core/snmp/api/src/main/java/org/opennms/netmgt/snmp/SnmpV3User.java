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
package org.opennms.netmgt.snmp;

import java.util.Objects;

public class SnmpV3User {

    private String engineId;
    private String securityName;
    private Integer securityLevel;
    private String authPassPhrase;
    private String privPassPhrase;
    private String authProtocol;
    private String privProtocol;

    public SnmpV3User() {
        super();
    }

    public SnmpV3User(String securityName, String authenticationProtocol,
            String authenticationPassphrase, String privacyProtocol,
            String privacyPassphrase) {
        super();
        this.securityName = securityName;
        this.authProtocol = authenticationProtocol;
        this.authPassPhrase = authenticationPassphrase;
        this.privProtocol = privacyProtocol;
        this.privPassPhrase = privacyPassphrase;
    }

    public SnmpV3User(String engineId, String securityName, String authenticationProtocol,
            String authenticationPassphrase, String privacyProtocol, String privacyPassphrase, Integer securityLevel) {
        this(securityName, authenticationProtocol, authenticationPassphrase, privacyProtocol, privacyPassphrase);
        this.engineId = engineId;
        this.securityLevel = securityLevel;
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

    public String getAuthPassPhrase() {
        return authPassPhrase;
    }

    public void setAuthPassPhrase(String authenticationPassphrase) {
        this.authPassPhrase = authenticationPassphrase;
    }

    public String getPrivPassPhrase() {
        return privPassPhrase;
    }

    public void setPrivPassPhrase(String privacyPassphrase) {
        this.privPassPhrase = privacyPassphrase;
    }

    public String getAuthProtocol() {
        return authProtocol;
    }

    public void setAuthProtocol(String authenticationProtocol) {
        this.authProtocol = authenticationProtocol;
    }

    public String getPrivProtocol() {
        return privProtocol;
    }

    public void setPrivProtocol(String privacyProtocol) {
        this.privProtocol = privacyProtocol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(engineId, securityName, securityLevel, authPassPhrase, privPassPhrase, authProtocol, privProtocol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnmpV3User that = (SnmpV3User) o;
        return Objects.equals(engineId, that.engineId) &&
                Objects.equals(securityName, that.securityName) &&
                Objects.equals(securityLevel, that.securityLevel) &&
                Objects.equals(authPassPhrase, that.authPassPhrase) &&
                Objects.equals(privPassPhrase, that.privPassPhrase) &&
                Objects.equals(authProtocol, that.authProtocol) &&
                Objects.equals(privProtocol, that.privProtocol);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("authPassPhrase", authPassPhrase)
            .append("authProtocol", authProtocol)
            .append("engineId", engineId)
            .append("privPassPhrase", privPassPhrase)
            .append("privProtocol", privProtocol)
            .append("securityName", securityName)
            .append("securityLevel", securityLevel)
            .toString();
    }
}
