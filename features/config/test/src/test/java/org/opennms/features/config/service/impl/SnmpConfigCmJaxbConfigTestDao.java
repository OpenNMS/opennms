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
package org.opennms.features.config.service.impl;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.callback.DefaultCmJaxbConfigDaoUpdateCallback;
import org.opennms.netmgt.config.snmp.SnmpConfig;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

public class SnmpConfigCmJaxbConfigTestDao extends AbstractCmJaxbConfigDao<SnmpConfig> {
    public static final String CONFIG_NAME = "snmp-config";

    public SnmpConfigCmJaxbConfigTestDao() {
        super(SnmpConfig.class, "SNMP Config");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback() {
        return new DefaultCmJaxbConfigDaoUpdateCallback<>(this);
    }

    @Override
    @PostConstruct
    public void postConstruct() {
        this.addOnReloadedCallback(getDefaultConfigId(), getUpdateCallback());
    }
}
