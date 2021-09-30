/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.dhcp;

import org.opennms.features.dhcpd.Dhcpd;
import org.opennms.netmgt.provision.detector.dhcp.client.DhcpClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.detector.dhcp.request.DhcpRequest;
import org.opennms.netmgt.provision.detector.dhcp.response.DhcpResponse;

public class DhcpDetector extends BasicDetector<DhcpRequest, DhcpResponse> {
    public static final int DEFAULT_RETRIES = 0;
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final String DEFAULT_MAC_ADDRESS = "00:06:0D:BE:9C:B2";

    private String macAddress = DEFAULT_MAC_ADDRESS;
    private boolean relayMode = false;
    private boolean extendedMode = false;
    private String myIpAddress = "127.0.0.1";
    private String requestIpAddress = "127.0.0.1";

    private Dhcpd dhcpd;

    public DhcpDetector() {
        super("DHCP", 0);
        setTimeout(DEFAULT_TIMEOUT);
        setRetries(DEFAULT_RETRIES);
    }

    public void setDhcpd(final Dhcpd dhcpd) {
        this.dhcpd = dhcpd;
    }

    @Override
    protected void onInit() {
        expectBanner(responseTimeGreaterThan(-1));
    }

    private static ResponseValidator<DhcpResponse> responseTimeGreaterThan(final long num) {
        return new ResponseValidator<DhcpResponse>() {
            @Override
            public boolean validate(DhcpResponse response) {
                return response.validate(num);
            }
        };
    }

    @Override
    protected Client<DhcpRequest, DhcpResponse> getClient() {
        return new DhcpClient(this.macAddress, this.relayMode, this.myIpAddress, this.extendedMode, this.requestIpAddress, getTimeout(), getRetries(), this.dhcpd);
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isRelayMode() {
        return this.relayMode;
    }

    public void setRelayMode(final boolean relayMode) {
        this.relayMode = relayMode;
    }

    public boolean isExtendedMode() {
        return this.extendedMode;
    }

    public void setExtendedMode(final boolean extendedMode) {
        this.extendedMode = extendedMode;
    }

    public String getMyIpAddress() {
        return this.myIpAddress;
    }

    public void setMyIpAddress(final String myIpAddress) {
        this.myIpAddress = myIpAddress;
    }

    public String getRequestIpAddress() {
        return this.requestIpAddress;
    }

    public void setRequestIpAddress(final String requestIpAddress) {
        this.requestIpAddress = requestIpAddress;
    }
}
