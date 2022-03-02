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

import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@Scope("prototype")
@Policy("Set Node Metadata")
public class NodeMetadataSettingPolicy extends BasePolicy<OnmsNode> implements NodePolicy {
    private String metadataContext = "requisition";
    private String metadataKey;
    private String metadataValue = "";

    @Override
    public OnmsNode act(final OnmsNode node, final Map<String, Object> attributes) {
        if (Strings.isNullOrEmpty(this.metadataKey) || Strings.isNullOrEmpty(this.metadataContext)) {
            return node;
        }

        node.addRequisionedMetaData(new OnmsMetaData(this.metadataContext, this.metadataKey, this.metadataValue != null ? this.metadataValue : ""));
        return node;
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

    public String getType() {
        return getCriteria("type");
    }

    public void setType(final String type) {
        putCriteria("type", type);
    }

    public String getSysObjectId() {
        return getCriteria("sysObjectId");
    }

    public void setSysObjectId(final String sysObjectId) {
        putCriteria("sysObjectId", sysObjectId);
    }

    public String getSysName() {
        return getCriteria("sysName");
    }

    public void setSysName(final String sysName) {
        putCriteria("sysName", sysName);
    }

    public String getSysDescription() {
        return getCriteria("sysDescription");
    }

    public void setSysDescription(final String sysDescription) {
        putCriteria("sysDescription", sysDescription);
    }

    public String getSysLocation() {
        return getCriteria("sysLocation");
    }

    public void setSysLocation(final String sysLocation) {
        putCriteria("sysLocation", sysLocation);
    }

    public String getSysContact() {
        return getCriteria("sysContact");
    }

    public void setSysContact(final String sysContact) {
        putCriteria("sysContact", sysContact);
    }

    public String getLabel() {
        return getCriteria("label");
    }

    public void setLabel(final String label) {
        putCriteria("label", label);
    }

    public String getLabelSource() {
        return getCriteria("labelSource");
    }

    public void setLabelSource(final String labelSource) {
        putCriteria("labelSource", labelSource);
    }

    public String getNetBiosName() {
        return getCriteria("netBiosName");
    }

    public void setNetBiosName(final String netBiosName) {
        putCriteria("netBiosName", netBiosName);
    }

    public String getNetBiosDomain() {
        return getCriteria("netBiosDomain");
    }

    public void setNetBiosDomain(final String netBiosDomain) {
        putCriteria("netBiosDomain", netBiosDomain);
    }

    public String getOperatingSystem() {
        return getCriteria("operatingSystem");
    }

    public void setOperatingSystem(final String operatingSystem) {
        putCriteria("operatingSystem", operatingSystem);
    }

    public String getForeignId() {
        return getCriteria("foreignId");
    }

    public void setForeignId(final String foreignId) {
        putCriteria("foreignId", foreignId);
    }

    public String getForeignSource() {
        return getCriteria("foreignSource");
    }

    public void setForeignSource(final String foreignSource) {
        putCriteria("foreignSource", foreignSource);
    }
}
