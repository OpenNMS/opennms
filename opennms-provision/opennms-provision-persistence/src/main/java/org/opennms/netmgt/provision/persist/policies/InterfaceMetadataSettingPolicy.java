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

package org.opennms.netmgt.provision.persist.policies;

import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@Scope("prototype")
@Policy("Set Interface Metadata")
public class InterfaceMetadataSettingPolicy extends BasePolicy<OnmsIpInterface> implements IpInterfacePolicy {
    private String metadataContext = "requisition";
    private String metadataKey;
    private String metadataValue = "";

    @Override
    public OnmsIpInterface act(final OnmsIpInterface iface, final Map<String, Object> attributes) {
        if (Strings.isNullOrEmpty(this.metadataKey) || Strings.isNullOrEmpty(this.metadataContext)) {
            return iface;
        }

        iface.addRequisionedMetaData(new OnmsMetaData(this.metadataContext, this.metadataKey, this.metadataValue != null ? this.metadataValue : ""));
        return iface;
    }

    public void setMetadataKey(final String metadataKey) {
        this.metadataKey = metadataKey;
    }

    @Require(value = { })
    public String getMetadataKey() {
        return this.metadataKey;
    }

    public void setMetadataValue(final String metadataValue) {
        this.metadataValue = metadataValue;
    }

    @Require(value = { })
    public String getMetadataValue() {
        return this.metadataValue;
    }

    public void setMetadataContext(final String metadataContext) {
        this.metadataContext = metadataContext;
    }

    public String getMetadataContext() {
        return this.metadataContext;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void setIpAddress(final String ipAddress) {
        putCriteria("ipAddress", ipAddress);
    }
    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return getCriteria("ipAddress");
    }
    /**
     * <p>setHostName</p>
     *
     * @param hostName a {@link java.lang.String} object.
     */
    public void setHostName(final String hostName) {
        putCriteria("ipHostName", hostName);
    }
    /**
     * <p>getHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostName() {
        return getCriteria("ipHostName");
    }
}