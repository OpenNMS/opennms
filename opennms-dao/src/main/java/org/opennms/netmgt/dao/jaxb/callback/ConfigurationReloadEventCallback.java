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

import org.opennms.features.config.service.api.CmJaxbConfigDao;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

import java.util.function.Consumer;

public class ConfigurationReloadEventCallback<E> implements Consumer<ConfigUpdateInfo> {
    private EventForwarder eventForwarder;
    private CmJaxbConfigDao<E> cmJaxbConfigDao;

    public ConfigurationReloadEventCallback(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    @Override
    public void accept(ConfigUpdateInfo configUpdateInfo) {
        // Fire reload event
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI,
                "config-rest");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, configUpdateInfo.getConfigName());
        eventForwarder.sendNow(eventBuilder.getEvent());
    }
}
