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
package org.opennms.netmgt.dao.jaxb.callback;

import com.google.common.base.Strings;
import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONObject;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;

import java.util.function.Consumer;

public class SnmpConfigConfigurationValidationCallback implements Consumer<ConfigUpdateInfo> {
    @Override
    public void accept(ConfigUpdateInfo configUpdateInfo) {
        JSONObject json = configUpdateInfo.getConfigJson();
        if (json == null) {
            throw new ValidationException(String.format("%s config is empty.", configUpdateInfo.getConfigName()));
        }
        try {
            SnmpConfig snmpConfig = ConfigConvertUtil.jsonToObject(json.toString(), SnmpConfig.class);
            this.validateIpAddresses(snmpConfig);
        } catch (ConfigConversionException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private static void validateIpAddresses(final SnmpConfig snmpConfig) {
        if (!Strings.isNullOrEmpty(snmpConfig.getProxyHost()) && !InetAddressUtils.isIPv4Address(snmpConfig.getProxyHost()) && !InetAddressUtils.isIPv6Address(snmpConfig.getProxyHost())) {
            throw new ValidationException(String.format("Invalid proxy-host IP address. %s", snmpConfig.getProxyHost()));
        }

        for(final Definition definition : snmpConfig.getDefinitions()) {
            for(final String specific : definition.getSpecifics()) {
                if (Strings.isNullOrEmpty(specific) ||!InetAddressUtils.isIPv4Address(specific) && !InetAddressUtils.isIPv6Address(specific)) {
                    throw new ValidationException(String.format("Invalid specific IP address. %s", specific));
                }
            }

            for(final var range : definition.getRanges()) {
                if (Strings.isNullOrEmpty(range.getBegin()) || Strings.isNullOrEmpty(range.getEnd()) || !(InetAddressUtils.isIPv4Address(range.getBegin()) && InetAddressUtils.isIPv4Address(range.getEnd()) ||
                      InetAddressUtils.isIPv6Address(range.getBegin()) && InetAddressUtils.isIPv6Address(range.getEnd()))) {
                    throw new ValidationException(String.format("Invalid range IP address. %s, %s", range.getBegin(), range.getEnd()));
                }
            }

            if (!Strings.isNullOrEmpty(definition.getProxyHost()) && !InetAddressUtils.isIPv4Address(definition.getProxyHost()) && !InetAddressUtils.isIPv6Address(definition.getProxyHost())) {
                throw new ValidationException(String.format("Invalid proxy-host IP address. %s", definition.getProxyHost()));
            }
        }
    }
}
