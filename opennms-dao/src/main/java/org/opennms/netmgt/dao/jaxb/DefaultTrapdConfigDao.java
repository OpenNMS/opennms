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
package org.opennms.netmgt.dao.jaxb;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.netmgt.dao.jaxb.callback.ConfigurationReloadEventCallback;
import org.opennms.netmgt.events.api.EventForwarder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public class DefaultTrapdConfigDao extends AbstractCmJaxbConfigDao<TrapdConfiguration> implements TrapdConfigDao {
    public static final String CONFIG_NAME = "trapd-config";
    public static final String DAEMON_NAME = "trapd";

    @Autowired
    private EventForwarder eventForwarder;

    public DefaultTrapdConfigDao() {
        super(TrapdConfiguration.class, "Trapd Config");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public TrapdConfiguration getConfig() {
        return this.getConfig(this.getDefaultConfigId());
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback(){
        return new ConfigurationReloadEventCallback(eventForwarder, this, DAEMON_NAME);
    }

    @Override
    public Consumer getValidationCallback() {
        return super.getValidationCallback();
    }

    @Override
    public void replaceConfig(TrapdConfiguration config) {
        this.updateConfig(this.getDefaultConfigId(), ConfigConvertUtil.objectToJson(config), true);
    }
}
