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