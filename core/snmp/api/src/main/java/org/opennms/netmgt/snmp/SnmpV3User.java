/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

public class SnmpV3User {

    private String engineId;
    private String securityName;
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
            String authenticationPassphrase, String privacyProtocol, String privacyPassphrase) {
        this(securityName, authenticationProtocol, authenticationPassphrase, privacyProtocol, privacyPassphrase);
        this.engineId = engineId;
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

}
