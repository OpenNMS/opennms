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
package org.opennms.netmgt.wsman.eventlog;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.wsman.eventlog.WsmanEventlogConfiguration;

public class WsManEventLogConfigDaoJaxb extends AbstractJaxbConfigDao<WsmanEventlogConfiguration, WsmanEventlogConfiguration> implements WsManEventLogConfigDao {

    public WsManEventLogConfigDaoJaxb() {
        super(WsmanEventlogConfiguration.class, "WS-Man Event Log Configuration");
    }

    @Override
    public WsmanEventlogConfiguration translateConfig(WsmanEventlogConfiguration config) {
        return config;
    }

    @Override
    public WsmanEventlogConfiguration getConfig() {
        return getContainer().getObject();
    }

    @Override
    public void reload() {
        getContainer().reload();
    }
}
