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
